package com.nhn.pinpoint.testweb.controller;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nhn.pinpoint.testweb.connector.apachehttp4.ApacheHttpClient4;
import com.nhn.pinpoint.testweb.connector.apachehttp4.HttpConnectorOptions;
import com.nhn.pinpoint.testweb.util.Description;

/**
 * 
 * @author netspider
 * 
 */
@Controller
public class HttpClient4Controller {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Description("에러시 cookie덤프")
	@RequestMapping(value = "/httpclient4/cookie")
	public @ResponseBody
	String cookie(@RequestHeader(value = "Cookie", required = false) String cookie) {
		logger.info("Cookie:{}", cookie);

		ApacheHttpClient4 client = new ApacheHttpClient4(new HttpConnectorOptions());
		client.execute("http://localhost:" + 9999 + "/combination.pinpoint", new HashMap<String, Object>(), cookie);

		return "OK";
	}

	@Description("에러시 post덤프")
	@RequestMapping(value = "/httpclient4/post")
	public @ResponseBody
	String post() {
		logger.info("Post");
		// String[] ports = new String[] { "9080", "10080", "11080" };
		// Random random = new Random();
		// String port = ports[random.nextInt(3)];
		//
		ApacheHttpClient4 client = new ApacheHttpClient4(new HttpConnectorOptions());
		HashMap<String, Object> post = new HashMap<String, Object>();
		post.put("test", "1");
		post.put("test2", "2");
		client.execute("http://localhost:" + 9999 + "/combination.pinpoint", post);

		return "OK";
	}
}
