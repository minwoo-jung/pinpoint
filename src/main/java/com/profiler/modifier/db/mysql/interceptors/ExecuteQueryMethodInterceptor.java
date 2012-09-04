package com.profiler.modifier.db.mysql.interceptors;

import com.profiler.StopWatch;
import com.profiler.context.Annotation;
import com.profiler.context.Trace;
import com.profiler.interceptor.StaticAroundInterceptor;
import com.profiler.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author netspider
 * 
 */
public class ExecuteQueryMethodInterceptor implements StaticAroundInterceptor {

    private final Logger logger = Logger.getLogger(ExecuteQueryMethodInterceptor.class.getName());

	@Override
	public void before(Object target, String className, String methodName, Object[] args) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("before className:" + className + " methodName:" + methodName + " args:" + Arrays.toString(args));
        }
        if (Trace.getCurrentTraceId() == null) {
            return;
        }

		try {
			/**
			 * If method was not called by request handler, we skip tagging.
			 */
            Method getUrl = target.getClass().getMethod("__getUrl");
            String url = (String) getUrl.invoke(target);
            System.out.println("url:" + url);

            Trace.recordRpcName("mysql", url);

			//
			// TODO: add destination address
			//

			if (args.length > 0) {
				Trace.recordAttibute("Query", args[0]);
			}

			Trace.record(Annotation.ClientSend);

			StopWatch.start("ExecuteQueryMethodInterceptor");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void after(Object target, String className, String methodName, Object[] args, Object result) {
		if (logger.isLoggable(Level.INFO)) {
            logger.info("after className:" + className + " methodName:" + methodName + " args:" + Arrays.toString(args) + " result:" + result);
        }

        if (Trace.getCurrentTraceId() == null) {
			return;
		}


		Trace.record(Annotation.ClientRecv, StopWatch.stopAndGetElapsed("ExecuteQueryMethodInterceptor"));
	}
}
