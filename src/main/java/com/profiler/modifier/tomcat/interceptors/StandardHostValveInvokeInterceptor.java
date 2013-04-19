package com.profiler.modifier.tomcat.interceptors;

import java.util.Enumeration;
import java.util.UUID;
import com.profiler.logging.Logger;
import com.profiler.logging.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

import com.profiler.common.AnnotationKey;
import com.profiler.common.ServiceType;
import com.profiler.context.*;
import com.profiler.interceptor.ByteCodeMethodDescriptorSupport;
import com.profiler.interceptor.MethodDescriptor;
import com.profiler.interceptor.StaticAroundInterceptor;
import com.profiler.interceptor.TraceContextSupport;
import com.profiler.sampler.util.SamplingFlagUtils;
import com.profiler.util.NetworkUtils;
import com.profiler.util.NumberUtils;

public class StandardHostValveInvokeInterceptor implements StaticAroundInterceptor, ByteCodeMethodDescriptorSupport, TraceContextSupport {

    private final Logger logger = LoggerFactory.getLogger(StandardHostValveInvokeInterceptor.class.getName());
    private final boolean isDebug = logger.isInfoEnabled();

    private MethodDescriptor descriptor;

    private TraceContext traceContext;

    @Override
    public void before(Object target, String className, String methodName, String parameterDescription, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, className, methodName, parameterDescription, args);
        }

        try {
//            traceContext.getActiveThreadCounter().start();

            HttpServletRequest request = (HttpServletRequest) args[0];

            boolean sampling = samplingEnable(request);
            if (!sampling) {
                // 샘플링 대상이 아닐 경우도 TraceObject를 생성하여, sampling 대상이 아니라는것을 명시해야 한다.
                // sampling 대상이 아닐경우 rpc 호출에서 sampling 대상이 아닌 것에 rpc호출 파라미터에 sampling disable 파라미터를 박을수 있다.
                traceContext.disableSampling();
                return;
            }

            String requestURL = request.getRequestURI();
            String remoteAddr = request.getRemoteAddr();

            TraceID traceId = populateTraceIdFromRequest(request);
            Trace trace;
            if (traceId != null) {
                if (isDebug) {
                    logger.debug("TraceID exist. continue trace. {} requestUrl:{}, remoteAddr:{}", new Object[] {traceId, requestURL, remoteAddr });
                }
                trace = traceContext.continueTraceObject(traceId);
            } else {
                trace = traceContext.newTraceObject();
                if (isDebug) {
                    logger.debug("TraceID not exist. start new trace. {} requestUrl:{}, remoteAddr:{}", new Object[] {traceId, requestURL, remoteAddr });
                }
            }

            trace.markBeforeTime();

            trace.recordServiceType(ServiceType.TOMCAT);
            trace.recordRpcName(requestURL);

            int port = request.getServerPort();
            trace.recordEndPoint(request.getServerName() + ((port > 0) ? ":" + port : ""));
            trace.recordRemoteAddr(remoteAddr);
            
            // 서버 맵을 통계정보에서 조회하려면 remote로 호출되는 WAS의 관계를 알아야해서 부모의 application name을 전달받음.
            if (traceId != null && !traceId.isRoot()) {
            	String parentApplicationName = populateParentApplicationNameFromRequest(request);
            	short parentApplicationType = populateParentApplicationTypeFromRequest(request);
            	if (parentApplicationName != null) {
            		trace.recordParentApplication(parentApplicationName, parentApplicationType);
            		trace.recordAcceptorHost(NetworkUtils.getHostFromURL(request.getRequestURL().toString()));
            	}
            } else {
            	// TODO 여기에서 client 정보를 수집할 수 있다.
            }
        } catch (Throwable e) {
            if (logger.isWarnEnabled()) {
                logger.warn("Tomcat StandardHostValve trace start fail. Caused:" + e.getMessage(), e);
            }
        }
    }

    @Override
    public void after(Object target, String className, String methodName, String parameterDescription, Object[] args, Object result) {
        if (isDebug) {
            logger.afterInterceptor(target, className, methodName, parameterDescription, args, result);
        }

//        traceContext.getActiveThreadCounter().end();

        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }
        traceContext.detachTraceObject();

        HttpServletRequest request = (HttpServletRequest) args[0];
        String parameters = getRequestParameter(request);
        if (parameters != null && parameters.length() > 0) {
            trace.recordAttribute(AnnotationKey.HTTP_PARAM, parameters);
        }


        trace.recordApi(descriptor);

        trace.recordException(result);

        trace.markAfterTime();
        trace.traceRootBlockEnd();
    }

    /**
     * Pupulate source trace from HTTP Header.
     *
     * @param request
     * @return
     */
    private TraceID populateTraceIdFromRequest(HttpServletRequest request) {

        String strUUID = request.getHeader(Header.HTTP_TRACE_ID.toString());
        if (strUUID != null) {

            UUID uuid = UUID.fromString(strUUID);
            int parentSpanID = NumberUtils.parseInteger(request.getHeader(Header.HTTP_PARENT_SPAN_ID.toString()), SpanID.NULL);
            int spanID = NumberUtils.parseInteger(request.getHeader(Header.HTTP_SPAN_ID.toString()), SpanID.NULL);
            short flags = NumberUtils.parseShort(request.getHeader(Header.HTTP_FLAGS.toString()), (short) 0);

            TraceID id = this.traceContext.createTraceId(uuid, parentSpanID, spanID, true, flags);
            if (logger.isInfoEnabled()) {
                logger.info("TraceID exist. continue trace. {}", id);
            }
            return id;
        } else {
            return null;
        }
    }

    private boolean samplingEnable(HttpServletRequest request) {
        // optional 값.
        String samplingFlag = request.getHeader(Header.HTTP_SAMPLED.toString());
        return SamplingFlagUtils.isSamplingFlag(samplingFlag);
    }

    private String populateParentApplicationNameFromRequest(HttpServletRequest request) {
		return request.getHeader(Header.HTTP_PARENT_APPLICATION_NAME.toString());
	}
	
	private short populateParentApplicationTypeFromRequest(HttpServletRequest request) {
		String type = request.getHeader(Header.HTTP_PARENT_APPLICATION_TYPE.toString());
		if (type != null) {
			return Short.valueOf(type);
		}
		return ServiceType.UNDEFINED.getCode();
	}
    
    private String getRequestParameter(HttpServletRequest request) {
        Enumeration<?> attrs = request.getParameterNames();
        final StringBuilder params = new StringBuilder(32);

        while (attrs.hasMoreElements()) {
            String keyString = attrs.nextElement().toString();
            Object value = request.getParameter(keyString);

            if (value != null) {
                String valueString = value.toString();
                int valueStringLength = valueString.length();

                if (valueStringLength > 0 && valueStringLength < 100) {
                    params.append(keyString).append("=").append(valueString);
                }

                if (attrs.hasMoreElements()) {
                    params.append(", ");
                }
            }
        }
        return params.toString();
    }

    @Override
    public void setMethodDescriptor(MethodDescriptor descriptor) {
        this.descriptor = descriptor;
        this.traceContext.cacheApi(descriptor);
    }

    @Override
    public void setTraceContext(TraceContext traceContext) {
        this.traceContext = traceContext;
    }
}
