package com.nhn.pinpoint.profiler.modifier.db.cubrid;

import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.List;

import com.nhn.pinpoint.profiler.Agent;
import com.nhn.pinpoint.profiler.interceptor.Interceptor;
import com.nhn.pinpoint.profiler.interceptor.ScopeDelegateSimpleInterceptor;
import com.nhn.pinpoint.profiler.interceptor.ScopeDelegateStaticInterceptor;
import com.nhn.pinpoint.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.nhn.pinpoint.profiler.interceptor.bci.InstrumentClass;
import com.nhn.pinpoint.profiler.interceptor.bci.InstrumentException;
import com.nhn.pinpoint.profiler.interceptor.bci.NotFoundInstrumentException;
import com.nhn.pinpoint.profiler.modifier.AbstractModifier;
import com.nhn.pinpoint.profiler.modifier.db.interceptor.*;
import com.nhn.pinpoint.profiler.util.JavaAssistUtils;
import com.nhn.pinpoint.profiler.util.PreparedStatementUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CubridPreparedStatementModifier extends AbstractModifier {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());


	public CubridPreparedStatementModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
		super(byteCodeInstrumentor, agent);
	}

	public String getTargetClass() {
		return "cubrid/jdbc/driver/CUBRIDPreparedStatement";
	}

	public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
		if (logger.isInfoEnabled()) {
			logger.info("Modifing. {}", javassistClassName);
		}
		this.byteCodeInstrumentor.checkLibrary(classLoader, javassistClassName);
		try {
			InstrumentClass preparedStatementClass = byteCodeInstrumentor.getClass(javassistClassName);

            Interceptor executeInterceptor = new ScopeDelegateSimpleInterceptor(new PreparedStatementExecuteQueryInterceptor(), JDBCScope.SCOPE);
            preparedStatementClass.addInterceptor("execute", null, executeInterceptor);

            Interceptor executeQueryInterceptor = new ScopeDelegateSimpleInterceptor(new PreparedStatementExecuteQueryInterceptor(), JDBCScope.SCOPE);
            preparedStatementClass.addInterceptor("executeQuery", null, executeQueryInterceptor);

            Interceptor executeUpdateInterceptor = new ScopeDelegateSimpleInterceptor(new PreparedStatementExecuteQueryInterceptor(), JDBCScope.SCOPE);
            preparedStatementClass.addInterceptor("executeUpdate", null, executeUpdateInterceptor);

			preparedStatementClass.addTraceVariable("__databaseInfo", "__setDatabaseInfo", "__getDatabaseInfo", "java.lang.Object");
			preparedStatementClass.addTraceVariable("__sql", "__setSql", "__getSql", "java.lang.Object");
			preparedStatementClass.addTraceVariable("__bindValue", "__setBindValue", "__getBindValue", "java.util.Map", "java.util.Collections.synchronizedMap(new java.util.HashMap());");

			bindVariableIntercept(preparedStatementClass, classLoader, protectedDomain);

			return preparedStatementClass.toBytecode();
		} catch (InstrumentException e) {
			if (logger.isWarnEnabled()) {
				logger.warn("{} modify fail. Cause:{}", this.getClass().getSimpleName(), e.getMessage(), e);
			}
			return null;
		}
	}

	private void bindVariableIntercept(InstrumentClass preparedStatement, ClassLoader classLoader, ProtectionDomain protectedDomain) throws InstrumentException {
		List<Method> bindMethod = PreparedStatementUtils.findBindVariableSetMethod();

		Interceptor interceptor = new ScopeDelegateStaticInterceptor(new PreparedStatementBindVariableInterceptor(), JDBCScope.SCOPE);
		int interceptorId = -1;
		for (Method method : bindMethod) {
			String methodName = method.getName();
			String[] parameterType = JavaAssistUtils.getParameterType(method.getParameterTypes());
			try {
				if (interceptorId == -1) {
					interceptorId = preparedStatement.addInterceptor(methodName, parameterType, interceptor);
				} else {
					preparedStatement.reuseInterceptor(methodName, parameterType, interceptorId);
				}
			} catch (NotFoundInstrumentException e) {
				// bind variable setter메소드를 못찾을 경우는 그냥 경고만 표시, 에러 아님.
                if (logger.isDebugEnabled()) {
                    logger.debug("bindVariable api not found. method:{} param:{} Cause:{}", methodName, Arrays.toString(parameterType), e.getMessage());
                }
			}
		}
	}
}
