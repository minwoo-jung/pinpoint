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
package com.navercorp.pinpoint.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author minwoo.jung
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
@Controller
public class NmsController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final static String NMS_CONN_RS_PORT_API_URL = "http://view.nms.navercorp.com/nms-api/dashboard.php?cmd=getVipsByRip&rip={hostIp}";
    private final static String NMS_CONN_CPS_API_URL = "http://view.nms.navercorp.com/nms-api/topology.php?cmd=vsByRsip&rsIp={hostIp}&rsPort={rsPort}";
    
    @Value("${nms.error.message}")
    private String nmsErrorMessage;
    
    @Autowired  
    private RestTemplate restTemplate;
    
    @PreAuthorize("hasPermission(#hostIp, 'authorize_kind')")
    @RequestMapping(value = "/nms", method = RequestMethod.GET)
    @ResponseBody
    public Object nms(@RequestParam(value= "hostIp", required=true) String hostIp) throws Exception {
        ResponseEntity<String> rsPortResponseEntity = this.restTemplate.exchange(NMS_CONN_RS_PORT_API_URL, HttpMethod.GET, null, String.class, hostIp);
        
        if (rsPortResponseEntity.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("NMS_CONN_RS_PORT_API_URL api call is not success.");
        }
        
        List<String> rsPorts = parseRsPort(rsPortResponseEntity);
        List nmsInfos = new ArrayList<>();
        
        for (String rsPort : rsPorts) {
            ResponseEntity<Map> responseEntity = this.restTemplate.exchange(NMS_CONN_CPS_API_URL, HttpMethod.GET, null, Map.class, hostIp, rsPort);
            
            if (responseEntity.getStatusCode() != HttpStatus.OK) {
                throw new RuntimeException("NMS_CONN_CPS_API_URL api call is not success.");
            }
            
            List nmsInfo = parseNmsInfo(responseEntity);
            nmsInfos.add(nmsInfo);
        }

        if (nmsInfos.isEmpty()) {
            return getNMSGuideMessage();
        }
        
        return nmsInfos;
  }

    private Map<String, String> getNMSGuideMessage() {
        Map<String, String> result = new HashMap<>();
        result.put("errorCode", "204");
        result.put("errorMessage", nmsErrorMessage);
        return result;
    }

    private List<String> parseRsPort(ResponseEntity<String> rsPortResponseEntity) throws Exception {
        Set<String> rsPorts = new HashSet();
        
        Map<String, Object> responseData = new ObjectMapper().readValue(rsPortResponseEntity.getBody(), Map.class);
        int portCount = (int)responseData.get("records");
        
        if (portCount <= 0) {
            return new ArrayList<>();
        }
        
        List<Map> rsPortInfos = (List<Map>)responseData.get("rows");
        
        for (Map<String, String> rsPortInfo : rsPortInfos) {
            String rsPort  = rsPortInfo.get("vsport");
            
            if (!StringUtils.isEmpty(rsPort)) {
                rsPorts.add(rsPort);
            }
        }
        
        List<String> ports = new ArrayList<>(rsPorts);
        ports.sort(new Comparator<String>() {
            public int compare(String o1, String o2) {
                final int i1 = Integer.parseInt(o1);
                final int i2 = Integer.parseInt(o2);
                return Integer.compare(i1, i2);
            }
        });
        
        return ports;
    }

    private List<Map<String, Object>> parseNmsInfo(ResponseEntity<Map> responseEntity) {
        List<Map<String, Object>> nmsInfo = new ArrayList<>(2);
        Map<String, Object> responseData = responseEntity.getBody();
        Map<String, String> nmsUrlInfo = (Map<String, String>) responseData.get("result");
        
        Map<String, Object> realServerInfo = new HashMap<>();
        realServerInfo.put("name", nmsUrlInfo.get("rsName"));
        realServerInfo.put("ip", nmsUrlInfo.get("rsIp"));
        realServerInfo.put("port", nmsUrlInfo.get("rsPort"));
        
        List<Map> rsImageInfos = new ArrayList<>(2);
        Map<String, String> rsConnImageInfo = new HashMap<>();
        rsConnImageInfo.put("title", "Concurrent Connection");
        rsConnImageInfo.put("url", nmsUrlInfo.get("rsConnUrl"));
        rsImageInfos.add(rsConnImageInfo);
        Map<String, String> rsCpsImageInfo = new HashMap<>();
        rsCpsImageInfo.put("title", "Connection Per Seconds");
        rsCpsImageInfo.put("url", nmsUrlInfo.get("rsCpsUrl"));
        rsImageInfos.add(rsCpsImageInfo);
        
        realServerInfo.put("image", rsImageInfos);
        nmsInfo.add(realServerInfo);
        
        Map<String, Object> virtualServerInfo = new HashMap<>();
        virtualServerInfo.put("name", "L4");
        virtualServerInfo.put("ip", nmsUrlInfo.get("vsIp"));
        virtualServerInfo.put("port", nmsUrlInfo.get("vsPort"));
        
        List<Map> vsImageInfos = new ArrayList<>(2);
        Map<String, String> vsConnImageInfo = new HashMap<>();
        vsConnImageInfo.put("title", "Concurrent Connection");
        vsConnImageInfo.put("url", nmsUrlInfo.get("vsConnUrl"));
        vsImageInfos.add(vsConnImageInfo);
        Map<String, String> vsCpsImageInfo = new HashMap<>();
        vsCpsImageInfo.put("title", "Connection Per Seconds");
        vsCpsImageInfo.put("url", nmsUrlInfo.get("vsCpsUrl"));
        vsImageInfos.add(vsCpsImageInfo);
        
        virtualServerInfo.put("image", vsImageInfos);
        nmsInfo.add(virtualServerInfo);
        
        return nmsInfo;
    }
    
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Map<String, String> handleException(Exception e) {
        logger.error(" Exception occured while trying to call NMS API", e);
        
        Map<String, String> result = new HashMap<>();
        result.put("errorCode", "500");
        result.put("errorMessage", "Can not communicate NMS server.");
        return result;
    }
}
