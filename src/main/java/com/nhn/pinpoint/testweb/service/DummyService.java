package com.nhn.pinpoint.testweb.service;

import java.util.HashMap;

import org.springframework.stereotype.Service;

import com.nhn.pinpoint.testweb.util.HttpConnectorOptions;
import com.nhn.pinpoint.testweb.util.HttpInvoker;

@Service
public class DummyService {

	/**
	 * <pre>
	 * doSomething();		// 2
	 * 		a();			// 3
	 * 		b();			// 3
	 * 			ba();		// 4
	 * 				baa();	// 5
	 * 			bb();		// 4
	 * 		c();			// 3
	 * 			ca();		// 4
	 * </pre>
	 */
	public void doSomething() {
		System.out.println("do something.");
		a();
		b();
		c();
	}

	private void a() {
		System.out.println("a");
	}

	private void b() {
		System.out.println("b");
		ba();
		bb();
	}

	private void ba() {
		System.out.println("ba");
		baa();
	}

	private void bb() {
		System.out.println("bb");
	}

	private void baa() {
		System.out.println("baa");
	}

	private void c() {
		System.out.println("c");
		ca();
	}

	private void ca() {
		System.out.println("ca");
		HttpInvoker client = new HttpInvoker(new HttpConnectorOptions());
		client.executeToBloc("http://localhost:8080/mysqlsimple.pinpoint", new HashMap<String, Object>());
	}
}
