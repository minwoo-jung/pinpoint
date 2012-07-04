package com.profiler;

import org.apache.log4j.PropertyConfigurator;

import com.profiler.data.thread.FetchTPSDataThread;
import com.profiler.receiver.tcp.TCPReceiver;
import com.profiler.receiver.udp.JVMStatDataReceiver;
import com.profiler.receiver.udp.RequestDataReceiver;
import com.profiler.receiver.udp.RequestTransactionDataReceiver;

public class TomcatProfileDataReceiver {
	public static void main(String[] args) {
		PropertyConfigurator.configure("log4j.properties");
		TomcatProfileDataReceiver receiver = new TomcatProfileDataReceiver();
		receiver.collect();
	}

	public void collect() {
		// System.out.println("***** Start Thrift server *****");
		// ThriftReceiver thrift=new ThriftReceiver();
		// thrift.start();

		System.out.println("******************************************************");
		System.out.println("***** Start TomcatTransactionData Receive UDP Thread *****"); // request
																							// start
																							// stop
		RequestTransactionDataReceiver requestTransactionDataReceiver = new RequestTransactionDataReceiver(1024);

		System.out.println("***** Start TomcatRequestData Receive UDP Thread *****"); // query
																						// information
		RequestDataReceiver requestDataReceiver = new RequestDataReceiver(64512);

		System.out.println("***** Start TomcatJVMData Receive UDP Thread     *****"); // jvm
																						// data
		JVMStatDataReceiver jvmReceiver = new JVMStatDataReceiver(256);

		System.out.println("***** Start Tomcat Agent Data Receive TDP Thread *****"); // was
																						// start
																						// stop
		TCPReceiver tcpReceiver = new TCPReceiver();

		System.out.println("***** Start Fetch data Thread                    *****");
		FetchTPSDataThread fetchRPS = new FetchTPSDataThread();

		System.out.println("******************************************************");

		requestTransactionDataReceiver.start();
		requestDataReceiver.start();
		jvmReceiver.start();
		tcpReceiver.start();
		fetchRPS.start();
	}
}
