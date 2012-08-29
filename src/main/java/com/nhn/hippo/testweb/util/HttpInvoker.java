package com.nhn.hippo.testweb.util;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

public class HttpInvoker {

	public final static int SLOW_REQUEST_TIME = 1000;

	private HttpConnectorOptions connectorOptions;

	public HttpInvoker() {
	}

	public HttpInvoker(HttpConnectorOptions cnnectorOptions) {
		this.connectorOptions = cnnectorOptions;
	}

	private HttpClient getHttpClient(HttpParams params) {
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		if (connectorOptions != null && connectorOptions.getPort() > 0) {
			schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), connectorOptions.getPort()));
		}
		schemeRegistry.register(new Scheme("https", PlainSocketFactory.getSocketFactory(), 443));

		SingleClientConnManager cm = new SingleClientConnManager(getHttpParams(), schemeRegistry);
		DefaultHttpClient httpClient = new DefaultHttpClient(cm, getHttpParams());
		httpClient.setParams(params);
		return httpClient;
	}

	public String executeToBloc(String uri, Map<String, Object> paramMap) {
		if (null == uri) {
			return null;
		}

		String responseBody = null;
		HttpClient httpClient = null;
		try {
			HttpPost post = new HttpPost(uri);
			post.setEntity(getEntity(paramMap));
			post.setParams(getHttpParams());
			post.addHeader("Content-Type", "application/json;charset=UTF-8");

			ResponseHandler<String> responseHandler = new BasicResponseHandler();

			httpClient = getHttpClient(getHttpParams());

			responseBody = httpClient.execute(post, responseHandler);

			return responseBody;
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		} finally {
			if (null != httpClient && null != httpClient.getConnectionManager()) {
				httpClient.getConnectionManager().shutdown();
			}
		}
	}

	public int executeToBlocWithReturnInt(String uri, Map<String, Object> paramMap) {
		if (null == uri) {
			return 0;
		}

		String responseBody = null;
		HttpClient httpClient = null;
		try {
			HttpPost post = new HttpPost(uri);
			post.setEntity(getEntity(paramMap));
			post.setParams(getHttpParams());
			post.addHeader("Content-Type", "application/json;charset=UTF-8");

			ResponseHandler<String> responseHandler = new BasicResponseHandler();

			httpClient = getHttpClient(getHttpParams());

			responseBody = httpClient.execute(post, responseHandler);

			return Integer.parseInt(responseBody);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		} finally {
			if (null != httpClient && null != httpClient.getConnectionManager()) {
				httpClient.getConnectionManager().shutdown();
			}
		}
	}

	private HttpEntity getEntity(Map<String, Object> paramMap) throws UnsupportedEncodingException {
		return new StringEntity(paramMap.toString(), "UTF-8");
	}

	private HttpParams getHttpParams() {
		HttpParams params = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(params, (int) connectorOptions.getConnectionTimeout());
		HttpConnectionParams.setSoTimeout(params, connectorOptions.getSoTimeout());
		params.setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET, "UTF-8");
		params.setParameter(CoreProtocolPNames.HTTP_ELEMENT_CHARSET, "UTF-8");
		return params;
	}
}
