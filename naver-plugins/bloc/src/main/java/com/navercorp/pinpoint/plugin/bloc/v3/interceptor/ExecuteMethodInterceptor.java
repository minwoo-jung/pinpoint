package com.navercorp.pinpoint.plugin.bloc.v3.interceptor;

import java.util.Enumeration;

import com.navercorp.pinpoint.bootstrap.context.Header;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanId;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanSimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.annotation.TargetMethod;
import com.navercorp.pinpoint.bootstrap.sampler.SamplingFlagUtils;
import com.navercorp.pinpoint.bootstrap.util.InterceptorUtils;
import com.navercorp.pinpoint.bootstrap.util.NumberUtils;
import com.navercorp.pinpoint.bootstrap.util.StringUtils;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.plugin.bloc.BlocConstants;

import com.navercorp.pinpoint.plugin.bloc.BlocPluginConfig;
import external.org.apache.coyote.Request;

/**
 * @author netspider
 * @author emeroad
 */
@TargetMethod(name = "execute", paramTypes = {"external.org.apache.coyote.Request", "external.org.apache.coyote.Response"})
public class ExecuteMethodInterceptor extends SpanSimpleAroundInterceptor {

    private final boolean traceRequestParam;

    public ExecuteMethodInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        super(traceContext, descriptor, ExecuteMethodInterceptor.class);

        BlocPluginConfig config = new BlocPluginConfig(traceContext.getProfilerConfig());
        traceRequestParam = config.isTraceRequestParam();
    }

    // check args.
    private boolean validate(final Object[] args) {
        if (args == null || args.length == 0) {
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
    public void doInBeforeTrace(SpanRecorder recorder, Object target, Object[] args) {
        if (!validate(args)) {
            return;
        }

        final Request request = (Request) args[0];
        if (recorder.canSampled()) {
            recorder.recordServiceType(BlocConstants.BLOC);

            final String requestURL = request.requestURI().toString();
            recorder.recordRpcName(requestURL);

            // TODO tomcat과 로직이 미묘하게 다름 차이점 알아내서 고칠것.
            // String remoteAddr = request.remoteAddr().toString();

            recorder.recordEndPoint(getEndPoint(request.serverName().toString(), request.getServerPort()));
            recorder.recordAttribute(AnnotationKey.HTTP_URL, InterceptorUtils.getHttpUrl(request.requestURI().toString(), traceRequestParam));
        }

        if (!recorder.isRoot()) {
            recordParentInfo(recorder, request);
        }
    }

    private String getEndPoint(String host, int port) {
        if (host == null) {
            return "unknown";
        }
        if (port < 0) {
            return host;
        }
        StringBuilder sb = new StringBuilder(host.length() + 8);
        sb.append(host);
        sb.append(':');
        sb.append(port);
        return sb.toString();
    }


    @Override
    protected Trace createTrace(Object target, Object[] args) {
        if (!validate(args)) {
            return null;
        }

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


    @Override
    public void doInAfterTrace(SpanRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        if (!validate(args)) {
            return;
        }

        if (recorder.canSampled()) {
            if (this.traceRequestParam) {
                Request request = (Request) args[0];
                // skip
                String parameters = getRequestParameter(request, 64, 512);
                if (parameters != null && parameters.length() > 0) {
                    recorder.recordAttribute(AnnotationKey.HTTP_PARAM, parameters);
                }
            }
            recorder.recordApi(methodDescriptor);
        }
        recorder.recordException(throwable);
    }

    private boolean samplingEnable(Request request) {
        // optional 값.
        String samplingFlag = request.getHeader(Header.HTTP_SAMPLED.toString());
        return SamplingFlagUtils.isSamplingFlag(samplingFlag);
    }

    /**
     * Pupulate source trace from HTTP Header.
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

            TraceId id = this.traceContext.createTraceId(transactionId, parentSpanId, spanId, flags);
            if (isDebug) {
                logger.debug("TraceID exist. continue trace. {}", id);
            }
            return id;
        } else {
            return null;
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
            params.append(StringUtils.drop(key, eachLimit));
            params.append("=");
            Object value = request.getParameters().getParameter(key);
            if (value != null) {
                params.append(StringUtils.drop(StringUtils.toString(value), eachLimit));
            }
        }

        return params.toString();
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
}