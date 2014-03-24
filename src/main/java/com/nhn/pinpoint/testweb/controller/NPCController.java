package com.nhn.pinpoint.testweb.controller;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.nhncorp.lucy.net.invoker.InvocationFuture;
import com.nhncorp.lucy.net.invoker.InvocationFutureListener;
import com.nhncorp.lucy.npc.connector.ConnectionFactory;
import com.nhncorp.lucy.npc.connector.KeepAliveNpcHessianConnector;
import com.nhncorp.lucy.npc.connector.NpcConnectionFactory;
import com.nhncorp.lucy.npc.connector.NpcHessianConnector;

/**
 * 
 * @author netspider
 * 
 */
@Controller
public class NPCController {

	/**
	 * using basic connector
	 * 
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/npc/1")
	public String npc(Model model) {
		NpcHessianConnector connector = null;
		try {
			InetSocketAddress serverAddress = new InetSocketAddress("127.0.0.1", 5000);
			connector = new NpcHessianConnector(serverAddress, true);

			Map<String, Object> params = new HashMap<String, Object>();
			params.put("message", "hello pinpoint");

			InvocationFuture future = connector.invoke("welcome/com.nhncorp.lucy.bloc.welcome.EchoBO", "execute", params);

			future.await();

			// Object result = future.get();
			Object result = future.getReturnValue();
			System.out.println("npc result=" + result);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			connector.dispose();
		}
		return "npc";
	}

	/**
	 * using keepalive connector
	 * 
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/npc/2")
	public String npc2(Model model) {
		KeepAliveNpcHessianConnector connector = null;
		try {
			InetSocketAddress serverAddress = new InetSocketAddress("127.0.0.1", 5000);

			connector = new KeepAliveNpcHessianConnector(serverAddress);

			Map<String, Object> params = new HashMap<String, Object>();
			params.put("message", "hello pinpoint");

			InvocationFuture future = connector.invoke("welcome/com.nhncorp.lucy.bloc.welcome.EchoBO", "execute", params);

			future.await();

			// Object result = future.get();
			Object result = future.getReturnValue();
			System.out.println("npc result=" + result);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			connector.dispose();
		}
		return "npc";
	}

	/**
	 * using connection factory
	 * 
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/npc/3")
	public String npc3(Model model) {
		NpcHessianConnector connector = null;
		try {
			InetSocketAddress serverAddress = new InetSocketAddress("127.0.0.1", 5000);

			ConnectionFactory npcConnectionFactory = new NpcConnectionFactory();

			npcConnectionFactory.setTimeout(1000L);
			npcConnectionFactory.setAddress(serverAddress);

			connector = npcConnectionFactory.create();

			Map<String, Object> params = new HashMap<String, Object>();
			params.put("message", "hello pinpoint");

			InvocationFuture future = connector.invoke("welcome/com.nhncorp.lucy.bloc.welcome.EchoBO", "execute", params);

			future.await();

			// Object result = future.get();
			Object result = future.getReturnValue();
			System.out.println("npc result=" + result);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			connector.dispose();
		}
		return "npc";
	}

	/**
	 * using lightweight connector
	 * 
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/npc/4")
	public String npc4(Model model) {
		NpcHessianConnector connector = null;
		try {
			InetSocketAddress serverAddress = new InetSocketAddress("127.0.0.1", 5000);

			ConnectionFactory npcConnectionFactory = new NpcConnectionFactory();

			npcConnectionFactory.setTimeout(1000L);
			npcConnectionFactory.setAddress(serverAddress);
			npcConnectionFactory.setLightWeight(true);

			connector = npcConnectionFactory.create();

			Map<String, Object> params = new HashMap<String, Object>();
			params.put("message", "hello pinpoint");

			InvocationFuture future = connector.invoke("welcome/com.nhncorp.lucy.bloc.welcome.EchoBO", "execute", params);

			future.await();

			// Object result = future.get();
			Object result = future.getReturnValue();
			System.out.println("npc result=" + result);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			connector.dispose();
		}
		return "npc";
	}

	/**
	 * using lightweight connector and listener
	 * 
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/npc/5")
	public String npc5(Model model) {
		NpcHessianConnector connector = null;
		try {
			InetSocketAddress serverAddress = new InetSocketAddress("127.0.0.1", 5000);

			ConnectionFactory npcConnectionFactory = new NpcConnectionFactory();

			npcConnectionFactory.setTimeout(1000L);
			npcConnectionFactory.setAddress(serverAddress);
			npcConnectionFactory.setLightWeight(true);

			connector = npcConnectionFactory.create();

			Map<String, Object> params = new HashMap<String, Object>();
			params.put("message", "hello pinpoint");

			InvocationFuture future = connector.invoke("welcome/com.nhncorp.lucy.bloc.welcome.EchoBO", "execute", params);

			future.addListener(new InvocationFutureListener() {
				@Override
				public void invocationComplete(InvocationFuture future) throws Exception {
					Object result = future.getReturnValue();
					System.out.println("npc result=" + result);
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			connector.dispose();
		}
		return "npc";
	}

	@RequestMapping(value = "/npc/6")
	public String npcStream(Model model) {
		// TODO test NPC stream
		return "npc";
	}
}
