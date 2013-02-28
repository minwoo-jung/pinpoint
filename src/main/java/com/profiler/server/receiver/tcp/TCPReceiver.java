package com.profiler.server.receiver.tcp;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.profiler.common.dto.AgentInfoDTO;
import com.profiler.server.config.TomcatProfilerReceiverConfig;

@Deprecated
public class TCPReceiver extends Thread {

	private static final Logger logger = LoggerFactory.getLogger("com.profiler.receiver.tcp.TCPReceiver");

	ServerSocket serverSocket = null;

	public TCPReceiver() {
	}

	public void run() {
		try {
			serverSocket = new ServerSocket(TomcatProfilerReceiverConfig.SERVER_TCP_LISTEN_PORT, 100);
			logger.info("Waiting for Agent data");
			while (true) {
				Socket socket = serverSocket.accept();
				InputStream stream = socket.getInputStream();
				ObjectInputStream objStream = new ObjectInputStream(stream);
				Object receivedObj = objStream.readObject();

				logger.debug("Got a data. {}", receivedObj);

				if (receivedObj instanceof AgentInfoDTO) {
					// AgentTableCreator creator = new AgentTableCreator();
					// creator.setAgentTable((AgentInfoDTO) receivedObj);
				}
				logger.debug(receivedObj.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
