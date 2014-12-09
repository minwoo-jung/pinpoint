package com.navercorp.pinpoint.profiler.modifier.linegame;

import java.security.ProtectionDomain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.bootstrap.Agent;
import com.navercorp.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.profiler.modifier.AbstractModifier;

/**
 * 
 * @author netspider
 * 
 */
public class HttpCustomServerHandlerModifier extends AbstractModifier {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public HttpCustomServerHandlerModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
		super(byteCodeInstrumentor, agent);
	}

	public String getTargetClass() {
		return "com/linecorp/games/common/baseFramework/handlers/HttpCustomServerHandler";
	}

	public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
		if (logger.isInfoEnabled()) {
			logger.info("Modifing. {} @ {}", javassistClassName, classLoader);
		}

		try {
			InstrumentClass aClass = byteCodeInstrumentor.getClass(classLoader, javassistClassName, classFileBuffer);

//			/**
//			 * modify inner class
//			 */
//			InstrumentClass iClass = aClass.getNestedClass("com.linecorp.games.common.baseFramework.handlers.HttpCustomServerHandler$InvokeTask");
//
//			// constructor parameter trace object
//			iClass.addTraceVariable("__channelHandlerContext", "__setChannelHandlerContext", "__getChannelHandlerContext", "org.jboss.netty.channel.ChannelHandlerContext");
//			iClass.addTraceVariable("__messageEvent", "__setMessageEvent", "__getMessageEvent", "org.jboss.netty.channel.MessageEvent");
//
//			// non static inner class는 constructor argument의 첫번째가 parent class임.
//			Interceptor constInterceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.navercorp.pinpoint.profiler.modifier.linegame.interceptor.InvokeTaskConstructorInterceptor");
//			iClass.addConstructorInterceptor(new String[] { "com.linecorp.games.common.baseFramework.handlers.HttpCustomServerHandler", "org.jboss.netty.channel.ChannelHandlerContext", "org.jboss.netty.channel.MessageEvent" }, constInterceptor);
//
//			Interceptor runInterceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.navercorp.pinpoint.profiler.modifier.linegame.interceptor.InvokeTaskRunInterceptor");
//			iClass.addInterceptor("run", null, runInterceptor);
//
//			return aClass.toBytecode();
			
			// DO NOTHING
			return null;
		} catch (InstrumentException e) {
			logger.info("modify fail. Cause:{}", e.getMessage(), e);
			return null;
		}
	}
}