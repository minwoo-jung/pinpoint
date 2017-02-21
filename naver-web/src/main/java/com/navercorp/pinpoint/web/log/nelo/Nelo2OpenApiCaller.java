/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.log.nelo;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author minwoo.jung
 */
public class Nelo2OpenApiCaller {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	//NELO2 open API server domain 
	private static final String NELO_OPEN_API_DOMAIN = "http://api.nelo2.navercorp.com";
	
	//oAuth API path
	private static final String AUTHENTICATION_CODE_REQUEST_PATH = "/auth/oauth2/authenticate";
	private static final String ACCESS_TOKEN_REQUEST_PATH = "/auth/oauth2/token";
	private static final String LOG_SEARCH_PATH = "/search";
	
	//aAuth API params value
	private static final String CLIENT_ID = "023379c455da3ebb1890d1aa3b990cd0c047abc6";
	private static final String CLIENT_SECRET = "6d157ed7c695a3e47b614c9fe8ca9f9320fc10bb";
	private static final String REDIRECT_URI = "http://localhost";
	private static final String RESPONSE_TYPE = "code";
	private static final String SCOPE = "projects";
	private static final String GRANT_TYPE = "authorization_code";
	private static final long SEARCH_INTERVAL = 1000*60*60*12;
	
	private static final String AUTHENTICATION_CODE_RESPONSE_HEADER_NAME = "location";

	@Autowired	
	private RestTemplate restTemplate;
	
	@Autowired
	@Qualifier("objectMapperForNeloLog")
	private ObjectMapper objectMapper;

	public List<NeloRawLog> requestNeloLog(String transactionId, String spanId, long time) throws Exception {
	        logger.info("require Nelo2 server to Log.");

	        String authenticationCode = callAuthenticationCodeApi();
	        String accessToken = callAccessTokenApi(authenticationCode);
	        String responseJsonTypeData = callLogOpenAPI(accessToken, transactionId, spanId, time);
	        
	        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
	        NeloRawData neloRawData = objectMapper.readValue(responseJsonTypeData, new TypeReference<NeloRawData>(){ });
	        
	        return neloRawData.getLogs();
	}
	
	private String callAuthenticationCodeApi() throws Exception {
	    logger.info("call Authentication code API");
	    
		final String authenticationCodeRequestUrl = NELO_OPEN_API_DOMAIN + AUTHENTICATION_CODE_REQUEST_PATH + "?client_id={client_id}&redirect_uri={redirect_uri}&response_type={response_type}&scope={scope}";
		ResponseEntity<String> responseEntity = this.restTemplate.exchange(authenticationCodeRequestUrl, HttpMethod.GET, null, String.class, CLIENT_ID, REDIRECT_URI, RESPONSE_TYPE, SCOPE);

		if (responseEntity.getStatusCode() != HttpStatus.FOUND) {
			throw new Exception("Nelo2 Open API(" + AUTHENTICATION_CODE_REQUEST_PATH + ") call is fail. response code is " + responseEntity.getStatusCode() + ".");
		}

		final String code = extractAuthenticationCode(responseEntity);
		if (code == null) {
		    throw new Exception("Nelo2 Open API(" + AUTHENTICATION_CODE_REQUEST_PATH + ") call result is invalid. Authentication Code does not exist in response header.");
		}

		return code;
	}

	private String callAccessTokenApi(final String authenticationCode) throws Exception {
	    logger.info("call Access Token API");
	    
		final String accessTokenRequestUrl = NELO_OPEN_API_DOMAIN + ACCESS_TOKEN_REQUEST_PATH;

		HttpHeaders header = new HttpHeaders();
		header.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<>();
		paramMap.add("code", authenticationCode);
        paramMap.add("client_id", CLIENT_ID);
        paramMap.add("client_secret", CLIENT_SECRET);
        paramMap.add("redirect_uri", REDIRECT_URI);
        paramMap.add("grant_type", GRANT_TYPE);

		HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(paramMap, header);
		ResponseEntity<Nelo2OpenApiToken> responseEntity = this.restTemplate.exchange(accessTokenRequestUrl, HttpMethod.POST, entity, Nelo2OpenApiToken.class);

		if (responseEntity.getStatusCode() != HttpStatus.OK) {
		    throw new Exception("Nelo2 Open API(" + ACCESS_TOKEN_REQUEST_PATH + ") call is fail. response code is " + responseEntity.getStatusCode() + ".");
		}
		
		Nelo2OpenApiToken nelo2OpenApiToken = responseEntity.getBody();
		if (nelo2OpenApiToken == null || nelo2OpenApiToken.getAccess_token() == null) {
		    throw new Exception("Nelo2 Open API(" + ACCESS_TOKEN_REQUEST_PATH + ") call result is invalid. Access_Token does not exist in response.");
		}

		return nelo2OpenApiToken.getAccess_token();
	}

	private String extractAuthenticationCode(ResponseEntity<String> responseEntity) throws UnsupportedEncodingException {
	    logger.info("call Log search API");
	    
		List<String> headerValues = responseEntity.getHeaders().get(AUTHENTICATION_CODE_RESPONSE_HEADER_NAME);
		if (headerValues == null || headerValues.isEmpty()) {
		    return null;
		}
		
		String code = headerValues.get(0);
		if (code == null || !code.contains("code=")) {
		    return null;
		}
		
		return URLDecoder.decode(code.split("code=")[1], "UTF-8");
	}

    private String callLogOpenAPI(String accessToken, String transactionId, String spanId, long time) throws Exception {
        String url = null;
        long from  = time - SEARCH_INTERVAL;
        long to = time + SEARCH_INTERVAL;
        final String interval = "&from=" + from + "&to=" + to;
        final String sort = "&sort=logTime:asc";
        
        if (spanId == null) {
            url = NELO_OPEN_API_DOMAIN + LOG_SEARCH_PATH + "?query=PxId%3A%22" + URLEncoder.encode(transactionId, "UTF-8") + "%22" + interval + sort;
        } else {
            url = NELO_OPEN_API_DOMAIN + LOG_SEARCH_PATH + "?query=PxId%3A%22" + URLEncoder.encode(transactionId, "UTF-8") + "%22%20AND%20PspanId%3A%22" + URLEncoder.encode(spanId, "UTF-8") +"%22" + interval + sort;
        }
        
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Authorization", "Bearer " + accessToken);
        CloseableHttpResponse response = null;
        
        try {
            response = httpclient.execute(httpGet);
            if (response.getStatusLine().getStatusCode() != HttpStatus.OK.value()) {
                throw new Exception("Nelo2 Open API(" + LOG_SEARCH_PATH + ") call is fail. response status code is " + response.getStatusLine().getStatusCode() + ".");
            }
            
            org.apache.http.HttpEntity entity = response.getEntity();
            return EntityUtils.toString(entity);
        } catch(Exception e) {
            throw new Exception("Nelo2 Open API(" + LOG_SEARCH_PATH + ") call is fail.", e);
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    throw e;
                }
            }
        }
    }

}
