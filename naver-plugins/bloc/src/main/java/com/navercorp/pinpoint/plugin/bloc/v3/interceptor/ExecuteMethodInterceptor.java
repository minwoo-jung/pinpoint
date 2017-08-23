package com.navercorp.pinpoint.plugin.bloc.v3.interceptor;

import com.navercorp.pinpoint.bootstrap.context.Header;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.SpanId;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.plugin.proxy.ProxyHttpHeaderHandler;
import com.navercorp.pinpoint.bootstrap.sampler.SamplingFlagUtils;
import com.navercorp.pinpoint.bootstrap.util.InterceptorUtils;
import com.navercorp.pinpoint.bootstrap.util.NumberUtils;
import com.navercorp.pinpoint.bootstrap.util.StringUtils;
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.plugin.bloc.AbstractBlocAroundInterceptor;
import com.navercorp.pinpoint.plugin.bloc.BlocConstants;
import external.org.apache.coyote.Request;

import java.util.Enumeration;

/**
 * @author netspider
 * @author emeroad
 */
public class ExecuteMethodInterceptor extends AbstractBlocAroundInterceptor {

    public ExecuteMethodInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        super(traceContext, descriptor, ExecuteMethodInterceptor.class);
    }

    @Override
    protected boolean validateArgument(Object[] args) {
        if (ArrayUtils.isEmpty(args)) {
            if (isDebug) {
                logger.debug("Invalid args={}.", args);
            }
            return false;
        }

        if (!(args[0] instanceof Request)) {
            if (isDebug) {
                logger.debug("Invalid args[0]={}. Need {}", args[0], Request.class.getName());
            }
            return false;
        }

        return true;
    }

    @Override
    protected Trace createTrace(Object target, Object[] args) {
        final Request request = (Request) args[0];
        final boolean sampling = samplingEnable(request);
        if (!sampling) {
            // 샘플링 대상이 아닐 경우도 TraceObject를 생성하여, sampling 대상이 아니라는것을 명시해야 한다.
            // sampling 대상이 아닐경우 rpc 호출에서 sampling 대상이 아닌 것에 rpc호출 파라미터에 sampling disable 파라미터를 박을수 있다.
            final Trace trace = traceContext.disableSampling();
            if (isDebug) {
                logger.debug("mark disable sampling. skip trace");
            }
            return trace;
        }

        final TraceId traceId = populateTraceIdFromRequest(request);
        if (traceId != null) {
            final Trace trace = traceContext.continueTraceObject(traceId);
            if (trace.canSampled()) {
                if (isDebug) {
                    logger.debug("TraceID exist. continue trace. traceId:{}, requestUrl:{}, remoteAddr:{}", traceId, request.requestURI(), request.remoteAddr());
                }
                return trace;
            } else {
                if (isDebug) {
                    logger.debug("TraceID exist. camSampled is false. skip trace. traceId:{}, requestUrl:{}, remoteAddr:{}", traceId, request.requestURI(), request.remoteAddr());
                }
                return trace;
            }
        } else {
            final Trace trace = traceContext.newTraceObject();
            if (trace.canSampled()) {
                if (isDebug) {
                    logger.debug("TraceID not exist. start new trace. requestUrl:{}, remoteAddr:{}", request.requestURI(), request.remoteAddr());
                }
                return trace;
            } else {
                if (isDebug) {
                    logger.debug("TraceID not exist. camSampled is false. skip trace. requestUrl:{}, remoteAddr:{}", request.requestURI(), request.remoteAddr());
                }
                return trace;
            }
        }
    }

    private boolean samplingEnable(Request request) {
        // optional 값.
        String samplingFlag = request.getHeader(Header.HTTP_SAMPLED.toString());
        return SamplingFlagUtils.isSamplingFlag(samplingFlag);
    }

    /**
     * Populate source trace from HTTP Header.
     *
     * @param request
     * @return
     */
    private TraceId populateTraceIdFromRequest(Request request) {
        String transactionId = request.getHeader(Header.HTTP_TRACE_ID.toString());
        if (transactionId != null) {
            long parentSpanId = NumberUtils.parseLong(request.getHeader(Header.HTTP_PARENT_SPAN_ID.toString()), SpanId.NULL);
            // TODO NULL이 되는게 맞는가?
            long spanId = NumberUtils.parseLong(request.getHeader(Header.HTTP_SPAN_ID.toString()), SpanId.NULL);
            short flags = NumberUtils.parseShort(request.getHeader(Header.HTTP_FLAGS.toString()), (short) 0);

            TraceId id = traceContext.createTraceId(transactionId, parentSpanId, spanId, flags);
            if (isDebug) {
                logger.debug("TraceID exist. continue trace. {}", id);
            }
            return id;
        } else {
            return null;
        }
    }

    @Override
    protected void doInBeforeTrace(Trace trace, Object target, Object[] args) {
        try {
            SpanRecorder spanRecorder = trace.getSpanRecorder();

            final Request request = (Request) args[0];
            if (request.remoteAddr() != null) {
                spanRecorder.recordRemoteAddress(request.remoteAddr().toString());
            }

            spanRecorder.recordServiceType(BlocConstants.BLOC);

            final String requestURL = request.requestURI().toString();
            spanRecorder.recordRpcName(requestURL);

            // TODO tomcat과 로직이 미묘하게 다름 차이점 알아내서 고칠것.
            // String remoteAddr = request.remoteAddr().toString();
            spanRecorder.recordEndPoint(getEndPoint(request.serverName().toString(), request.getServerPort()));
            spanRecorder.recordAttribute(AnnotationKey.HTTP_URL, InterceptorUtils.getHttpUrl(request.requestURI().toString(), traceRequestParam));

            if (!spanRecorder.isRoot()) {
                recordParentInfo(spanRecorder, request);
            }
            spanRecorder.recordApi(blocMethodApiTag);
            // record proxy HTTP headers.
            proxyHttpHeaderRecorder.record(spanRecorder, new ProxyHttpHeaderHandler() {
                @Override
                public String read(String name) {
                    return request.getHeader(name);
                }

                @Override
                public void remove(String name) {
                    request.getMimeHeaders().removeHeader(name);
                }
            });
        } finally {
            SpanEventRecorder spanEventRecorder = trace.traceBlockBegin();
            spanEventRecorder.recordApi(methodDescriptor);
            spanEventRecorder.recordServiceType(BlocConstants.BLOC_INTERNAL_METHOD);
        }
    }

    private String getEndPoint(String host, int port) {
        if (host == null) {
            return "unknown";
        }
        port = HostAndPort.getPortOrNoPort(port);
        return HostAndPort.toHostAndPortString(host, port);
    }

    private void recordParentInfo(SpanRecorder recorder, Request request) {
        String parentApplicationName = request.getHeader(Header.HTTP_PARENT_APPLICATION_NAME.toString());
        if (parentApplicationName != null) {
            final String host = request.getHeader(Header.HTTP_HOST.toString());
            if (host != null) {
                recorder.recordAcceptorHost(host);
            } else {
                recorder.recordAcceptorHost(request.serverName().toString());
            }
            final String type = request.getHeader(Header.HTTP_PARENT_APPLICATION_TYPE.toString());
            final short parentApplicationType = NumberUtils.parseShort(type, ServiceType.UNDEFINED.getCode());
            recorder.recordParentApplication(parentApplicationName, parentApplicationType);
        }
    }

    @Override
    protected void doInAfterTrace(Trace trace, Object target, Object[] args, Object result, Throwable throwable) {
        SpanEventRecorder spanEventRecorder = null;
        try {
            spanEventRecorder = trace.currentSpanEventRecorder();
            if (traceRequestParam) {
                Request request = (Request) args[0];
                // skip
                String parameters = getRequestParameter(request, MAX_EACH_PARAMETER_SIZE, MAX_ALL_PARAMETER_SIZE);
                spanEventRecorder.recordAttribute(AnnotationKey.HTTP_PARAM, parameters);
            }
        } finally {
            if (spanEventRecorder != null) {
                spanEventRecorder.recordException(throwable);
            }
            trace.traceBlockEnd();
        }

    }

    private String getRequestParameter(Request request, int eachLimit, int totalLimit) {
        Enumeration<?> attrs = request.getParameters().getParameterNames();
        final StringBuilder params = new StringBuilder(64);
        while (attrs.hasMoreElements()) {
            if (params.length() != 0) {
                params.append('&');
            }
            if (params.length() > totalLimit) {
                // 데이터 사이즈가 너무 클 경우 뒷 파라미터 생략.
                params.append("...");
                return params.toString();
            }
            String key = attrs.nextElement().toString();
            params.append(StringUtils.abbreviate(key, eachLimit));
            params.append("=");
            Object value = request.getParameters().getParameter(key);
            if (value != null) {
                params.append(StringUtils.abbreviate(StringUtils.toString(value), eachLimit));
            }
        }

        return params.toString();
    }

}