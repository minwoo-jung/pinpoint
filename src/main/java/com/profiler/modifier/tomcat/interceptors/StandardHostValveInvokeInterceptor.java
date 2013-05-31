package com.profiler.modifier.tomcat.interceptors;

import java.util.Enumeration;
import java.util.UUID;

import com.profiler.interceptor.*;
import com.profiler.logging.Logger;
import com.profiler.logging.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

import com.nhn.pinpoint.common.AnnotationKey;
import com.nhn.pinpoint.common.ServiceType;
import com.profiler.context.*;
import com.profiler.sampler.util.SamplingFlagUtils;
import com.profiler.util.NetworkUtils;
import com.profiler.util.NumberUtils;

public class StandardHostValveInvokeInterceptor implements SimpleAroundInterceptor, ByteCodeMethodDescriptorSupport, TraceContextSupport {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isInfoEnabled();

    private MethodDescriptor descriptor;

    private TraceContext traceContext;
//    private ContainerAcceptor acceptor = new ContainerAcceptor(logger, ServiceType.TOMCAT);

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        try {
//            traceContext.getActiveThreadCounter().start();

            HttpServletRequest request = (HttpServletRequest) args[0];
            String requestURL = request.getRequestURI();
            String remoteAddr = request.getRemoteAddr();
            String port = Integer.toString(request.getServerPort());
            String endPoint = request.getServerName() + ":" + port;

            // remote call에 sampling flag가 설정되어있을 경우는 샘플링 대상으로 삼지 않는다.
            boolean sampling = samplingEnable(request);
            if (!sampling) {
                // 샘플링 대상이 아닐 경우도 TraceObject를 생성하여, sampling 대상이 아니라는것을 명시해야 한다.
                // sampling 대상이 아닐경우 rpc 호출에서 sampling 대상이 아닌 것에 rpc호출 파라미터에 sampling disable 파라미터를 박을수 있다.
                traceContext.disableSampling();
                if (isDebug) {
                    logger.debug("remotecall sampling flag found. skip trace requestUrl:{}, remoteAddr:{}", requestURL, remoteAddr);
                }
                return;
            }


            TraceID traceId = populateTraceIdFromRequest(request);
            Trace trace;
            if (traceId != null) {
                // TODO remote에서 sampling flag로 마크가되는 대상으로 왔을 경우도 추가로 샘플링 칠수 있어야 할것으로 보임.
                trace = traceContext.continueTraceObject(traceId);
                if (!trace.canSampled()) {
                    if (isDebug) {
                        logger.debug("TraceID exist. camSampled is false. skip trace. traceId:{}, requestUrl:{}, remoteAddr:{}", new Object[] {traceId, requestURL, remoteAddr });
                        return;
                    }
                } else {
                    if (isDebug) {
                        logger.debug("TraceID exist. continue trace. traceId:{}, requestUrl:{}, remoteAddr:{}", new Object[] {traceId, requestURL, remoteAddr });
                    }
                }
            } else {
                trace = traceContext.newTraceObject();
                if (!trace.canSampled()){
                    logger.debug("TraceID not exist. camSampled is false. skip trace. requestUrl:{}, remoteAddr:{}", new Object[] {requestURL, remoteAddr });
                    return;
                } else {
                    if (isDebug) {
                        logger.debug("TraceID not exist. start new trace. requestUrl:{}, remoteAddr:{}", new Object[] { requestURL, remoteAddr });
                    }
                }
            }

            trace.markBeforeTime();

            trace.recordServiceType(ServiceType.TOMCAT);
            trace.recordRpcName(requestURL);


            trace.recordEndPoint(endPoint);
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
    public void after(Object target, Object[] args, Object result) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result);
        }

//        traceContext.getActiveThreadCounter().end();

        Trace trace = traceContext.currentRawTraceObject();
        if (trace == null) {
            return;
        }
        traceContext.detachTraceObject();
        if (!trace.canSampled()) {
            return;
        }

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

            TraceID id = this.traceContext.createTraceId(uuid, parentSpanID, spanID, flags);
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
        if (isDebug) {
            logger.debug("SamplingFlag:{}", samplingFlag);
        }
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
