package com.navercorp.pinpoint.testweb.connector.apachehttp4.nhnent;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.testweb.connector.apachehttp4.nhnent.HttpUtil;
import com.navercorp.pinpoint.testweb.connector.apachehttp4.nhnent.HttpUtilException;

/**
 * 
 * @author netspider
 * 
 */
public class HttpUtilTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final String URL = "http://www.naver.com/";

	@Test
	public void callUrl() {
		try {
            String response = HttpUtil.url(URL).method(HttpUtil.Method.POST).connectionTimeout(10000).readTimeout(10000).getContents();
            logger.debug(response);
		} catch (HttpUtilException e) {
		}
	}

}
