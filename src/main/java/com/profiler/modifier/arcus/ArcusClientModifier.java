package com.profiler.modifier.arcus;

import java.security.ProtectionDomain;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.profiler.interceptor.bci.InstrumentClass;
import com.profiler.modifier.AbstractModifier;

/**
 * 
 * @author netspider
 * 
 */
public class ArcusClientModifier extends AbstractModifier {

	private final Logger logger = Logger.getLogger(ArcusClientModifier.class.getName());

	public ArcusClientModifier(ByteCodeInstrumentor byteCodeInstrumentor) {
		super(byteCodeInstrumentor);
	}

	public String getTargetClass() {
		return "net/spy/memcached/protocol/BaseOperationImpl";
	}

	public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
		if (logger.isLoggable(Level.INFO)) {
			logger.info("Modifing. " + javassistClassName);
		}

		try {
			classLoader.loadClass("net.spy.memcached.ops.OperationState");

			InstrumentClass aClass = byteCodeInstrumentor.getClass(javassistClassName);

			/**
			 * inject both current and next traceId.
			 */
			aClass.addTraceVariable("__traceId", "__setTraceId", "__getTraceId", "com.profiler.context.TraceID");
			aClass.addTraceVariable("__nextTraceId", "__setNextTraceId", "__getNextTraceId", "com.profiler.context.TraceID");
			aClass.insertCodeAfterConstructor(null, "{ __setTraceId(com.profiler.context.Trace.getCurrentTraceId()); __setNextTraceId(com.profiler.context.Trace.getNextId()); }");

			aClass.insertCodeBeforeMethod("transitionState", new String[] { "net.spy.memcached.ops.OperationState" }, getTransitionStateAfterCode());

			return aClass.toBytecode();
		} catch (Exception e) {
			if (logger.isLoggable(Level.WARNING)) {
				logger.log(Level.WARNING, e.getMessage(), e);
			}
			return null;
		}
	}

	/**
	 * Logic is different in OperationState.
	 * 
	 * @return
	 */
	private String getTransitionStateAfterCode() {
		StringBuilder code = new StringBuilder();
		code.append("{");

		/**
		 * If current traceID is not exists, take nextId for current traceID.
		 */
		code.append("if (com.profiler.context.Trace.getCurrentTraceId() == null) { com.profiler.context.Trace.setTraceId(__nextTraceId); }");

		// code.append("System.out.println(__traceId);");
		// code.append("System.out.println($1);");
		// code.append("System.out.println(\"Change state \" + state + \" -> \" + newState);");
		// code.append("System.out.println(handlingNode);");
		// code.append("System.out.println(\"cmd=\" + ((cmd == null) ? null : new String(cmd.array())));");
		// code.append("System.out.println(Thread.currentThread().getId());");
		// code.append("System.out.println(Thread.currentThread().getName());");
		// code.append("System.out.println(\"\");");
		// code.append("System.out.println(\"\");");
		// code.append("System.out.println(\"\");");

		code.append("if (newState == net.spy.memcached.ops.OperationState.READING) {");
		code.append("	java.net.SocketAddress socketAddress = handlingNode.getSocketAddress();");
		code.append("	if (socketAddress instanceof java.net.InetSocketAddress) {");
		code.append("		java.net.InetSocketAddress addr = (java.net.InetSocketAddress) handlingNode.getSocketAddress();");
		code.append("		com.profiler.context.Trace.recordEndPoint(addr.getHostName(), addr.getPort());");
		code.append("	}");
		code.append("	com.profiler.context.Trace.recordRpcName(\"arcus\", \"\");");
		code.append("	com.profiler.context.Trace.recordAttribute(\"arcus.command\", ((cmd == null) ? \"UNKNOWN\" : new String(cmd.array())));");
		code.append("	System.out.println(\"CS\");");
		code.append("	com.profiler.context.Trace.record(com.profiler.context.Annotation.ClientSend);");
		code.append("} else if (newState == net.spy.memcached.ops.OperationState.COMPLETE) {");
		code.append("	System.out.println(\"CR\");");
		code.append("	com.profiler.context.Trace.record(com.profiler.context.Annotation.ClientRecv);");
		code.append("}");

		code.append("}");

		return code.toString();
	}
}