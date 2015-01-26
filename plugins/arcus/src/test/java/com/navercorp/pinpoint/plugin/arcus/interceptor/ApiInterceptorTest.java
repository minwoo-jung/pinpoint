package com.navercorp.pinpoint.plugin.arcus.interceptor;

import static org.mockito.Mockito.*;

import org.junit.Test;

import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.instrument.MethodInfo;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.plugin.ObjectAccessor;
import com.navercorp.pinpoint.profiler.interceptor.DefaultMethodDescriptor;

public class ApiInterceptorTest {

    @Test
    public void testAround() {
        String[] parameterTypes = new String[] { "java.lang.String", "int", "java.lang.Object" };
        String[] parameterNames = new String[] { "key", "exptime", "value" };
        Object[] args = new Object[] { "key", 10, "my_value" };

        TraceContext traceContext = mock(TraceContext.class);
        MethodDescriptor methodDescriptor = new DefaultMethodDescriptor(Object.class.getName(), "set", parameterTypes, parameterNames);
        MethodInfo methodInfo = mock(MethodInfo.class);
        ObjectAccessor target = mock(ObjectAccessor.class);

        when(methodInfo.getDescriptor()).thenReturn(methodDescriptor);
        when(methodInfo.getParameterTypes()).thenReturn(parameterTypes);
        when(target._$PINPOINT$_getObject()).thenReturn("serviceCode");

        ApiInterceptor interceptor = new ApiInterceptor(traceContext, methodInfo, true);


        interceptor.before(target, args);
        interceptor.after(target, args, null, null);
    }
}