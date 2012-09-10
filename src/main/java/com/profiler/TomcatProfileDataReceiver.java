package com.profiler;

import com.profiler.receiver.tcp.TCPReceiver;
import com.profiler.receiver.udp.DataReceiver;
import com.profiler.receiver.udp.MulplexedUDPReceiver;

public class TomcatProfileDataReceiver {
	public static void main(String[] args) {
		// Log4jConfigurer.configure("log4j.xml");
		TomcatProfileDataReceiver receiver = new TomcatProfileDataReceiver();
		receiver.collect();
	}

	public void collect() {
		System.out.println("Start MulplexedUDPReceiver Receive UDP Thread");
		DataReceiver mulplexDataReceiver = new MulplexedUDPReceiver();
		mulplexDataReceiver.start();

		System.out.println("Start Tomcat Agent Data Receive TDP Thread");
		TCPReceiver tcpReceiver = new TCPReceiver();
		tcpReceiver.start();

		// System.out.println("***** Start Fetch data Thread                    ********");
		// FetchTPSDataThread fetchRPS = new FetchTPSDataThread();
		// fetchRPS.start();
		// System.out.println("*********************************************************");

	}
}
