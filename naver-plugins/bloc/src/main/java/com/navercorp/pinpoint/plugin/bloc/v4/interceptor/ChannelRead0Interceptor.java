package com.navercorp.pinpoint.plugin.bloc.v4.interceptor;

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
import com.navercorp.pinpoint.plugin.bloc.AbstractBlocAroundInterceptor;
import com.navercorp.pinpoint.plugin.bloc.BlocConstants;
import com.navercorp.pinpoint.plugin.bloc.v4.UriEncodingGetter;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author netspider
 */
public class ChannelRead0Interceptor extends AbstractBlocAroundInterceptor {

    public ChannelRead0Interceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        super(traceContext, descriptor, ChannelRead0Interceptor.class);
    }

    @Override
    protected boolean validateArgument(Object[] args) {
        if (args == null || args.length < 2) {
            if (isDebug) {
                logger.debug("Invalid args={}.", args);
            }
            return false;
        }

        if (!(args[0] instanceof io.netty.channel.ChannelHandlerContext)) {
            if (isDebug) {
                logger.debug("Invalid args[0]={}. Need {}", args[0], io.netty.channel.ChannelHandlerContext.class.getName());
            }
            return false;
        }

        if (!(args[1] instanceof io.netty.handler.codec.http.FullHttpRequest)) {
            if (isDebug) {
                logger.debug("Invalid args[1]={}. Need {}", args[1], io.netty.handler.codec.http.FullHttpRequest.class.getName());
            }
            return false;
        }

        return true;
    }


    @Override
    protected Trace createTrace(Object target, Object[] args) {
        final io.netty.handler.codec.http.FullHttpRequest request = (io.netty.handler.codec.http.FullHttpRequest) args[1];

        final boolean sampling = samplingEnable(request);
        if (!sampling) {
            // 샘플링 대상이 아닐 경우도 TraceObject를 생성하여, sampling 대상이 아니라는것을 명시해야
            // 한다.
            // sampling 대상이 아닐경우 rpc 호출에서 sampling 대상이 아닌 것에 rpc호출 파라미터에
            // sampling disable 파라미터를 박을수 있다.
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
                    String requestURL = request.getUri();
                    io.netty.channel.ChannelHandlerContext ctx = (io.netty.channel.ChannelHandlerContext) args[0];
                    String remoteAddr = ((SocketChannel) ctx.channel()).remoteAddress().toString();
                    logger.debug("TraceID exist. continue trace. traceId:{}, requestUrl:{}, remoteAddr:{}", traceId, requestURL, remoteAddr);
                }
                return trace;
            } else {
                if (isDebug) {
                    String requestURL = request.getUri();
                    io.netty.channel.ChannelHandlerContext ctx = (io.netty.channel.ChannelHandlerContext) args[0];
                    String remoteAddr = ((SocketChannel) ctx.channel()).remoteAddress().toString();
                    logger.debug("TraceID exist. camSampled is false. skip trace. traceId:{}, requestUrl:{}, remoteAddr:{}", traceId, requestURL, remoteAddr);
                }
                return trace;
            }
        } else {
            final Trace trace = traceContext.newTraceObject();
            if (trace.canSampled()) {
                if (isDebug) {
                    String requestURL = request.getUri();
                    io.netty.channel.ChannelHandlerContext ctx = (io.netty.channel.ChannelHandlerContext) args[0];
                    String remoteAddr = ((SocketChannel) ctx.channel()).remoteAddress().toString();
                    logger.debug("TraceID not exist. start new trace. requestUrl:{}, remoteAddr:{}", requestURL, remoteAddr);
                }
                return trace;
            } else {
                if (isDebug) {
                    String requestURL = request.getUri();
                    io.netty.channel.ChannelHandlerContext ctx = (io.netty.channel.ChannelHandlerContext) args[0];
                    String remoteAddr = ((SocketChannel) ctx.channel()).remoteAddress().toString();
                    logger.debug("TraceID not exist. camSampled is false. skip trace. requestUrl:{}, remoteAddr:{}", requestURL, remoteAddr);
                }
                return trace;
            }
        }
    }

    private boolean samplingEnable(io.netty.handler.codec.http.FullHttpRequest request) {
        // optional 값.
        String samplingFlag = request.headers().get(Header.HTTP_SAMPLED.toString());
        return SamplingFlagUtils.isSamplingFlag(samplingFlag);
    }

    /**
     * Populate source trace from HTTP Header.
     *
     * @param request
     * @return
     */
    private TraceId populateTraceIdFromRequest(io.netty.handler.codec.http.FullHttpRequest request) {
        final HttpHeaders headers = request.headers();

        String transactionId = headers.get(Header.HTTP_TRACE_ID.toString());
        if (transactionId != null) {
            long parentSpanId = NumberUtils.parseLong(headers.get(Header.HTTP_PARENT_SPAN_ID.toString()), SpanId.NULL);
            // TODO NULL이 되는게 맞는가?
            long spanId = NumberUtils.parseLong(headers.get(Header.HTTP_SPAN_ID.toString()), SpanId.NULL);
            short flags = NumberUtils.parseShort(headers.get(Header.HTTP_FLAGS.toString()), (short) 0);

            final TraceId id = traceContext.createTraceId(transactionId, parentSpanId, spanId, flags);
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

            final io.netty.channel.ChannelHandlerContext ctx = (io.netty.channel.ChannelHandlerContext) args[0];
            final io.netty.handler.codec.http.FullHttpRequest request = (io.netty.handler.codec.http.FullHttpRequest) args[1];

            spanRecorder.recordServiceType(BlocConstants.BLOC);

            final String requestURL = request.getUri();
            spanRecorder.recordRpcName(requestURL);

            String endPoint = getIpPort(ctx.channel().localAddress());
            spanRecorder.recordEndPoint(endPoint);

            String remoteAddress = getIp(ctx.channel().remoteAddress());
            spanRecorder.recordRemoteAddress(remoteAddress);

            spanRecorder.recordAttribute(AnnotationKey.HTTP_URL, InterceptorUtils.getHttpUrl(request.getUri(), traceRequestParam));
            spanRecorder.recordApi(blocMethodApiTag);
            proxyHttpHeaderRecorder.record(spanRecorder, new ProxyHttpHeaderHandler() {
                @Override
                public String read(String name) {
                    return request.headers().get(name);
                }

                @Override
                public void remove(String name) {
                    request.headers().remove(name);
                }
            });

            if (!spanRecorder.isRoot()) {
                recordParentInfo(spanRecorder, request, ctx);
            }
        } finally {
            SpanEventRecorder spanEventRecorder = trace.traceBlockBegin();
            spanEventRecorder.recordApi(methodDescriptor);
            spanEventRecorder.recordServiceType(BlocConstants.BLOC_INTERNAL_METHOD);
        }
    }

    private String getIpPort(SocketAddress socketAddress) {
        String address = socketAddress.toString();

        if (socketAddress instanceof InetSocketAddress) {
            InetSocketAddress inetSocketAddress = (InetSocketAddress) socketAddress;
            return HostAndPort.toHostAndPortString(inetSocketAddress.getAddress().getHostAddress(), inetSocketAddress.getPort());
        }

        if (address.startsWith("/")) {
            return address.substring(1);
        } else {
            if (address.contains("/")) {
                return address.substring(address.indexOf('/') + 1);
            } else {
                return address;
            }
        }
    }

    private String getIp(SocketAddress socketAddress) {
        if (socketAddress instanceof InetSocketAddress) {
            InetSocketAddress inetSocketAddress = (InetSocketAddress) socketAddress;
            return inetSocketAddress.getAddress().getHostAddress();
        } else {
            return "NOT_SUPPORTED_ADDRESS";
        }
    }

    private void recordParentInfo(SpanRecorder recorder, io.netty.handler.codec.http.FullHttpRequest request, io.netty.channel.ChannelHandlerContext ctx) {
        HttpHeaders headers = request.headers();
        String parentApplicationName = headers.get(Header.HTTP_PARENT_APPLICATION_NAME.toString());

        if (parentApplicationName != null) {
            final String host = headers.get(Header.HTTP_HOST.toString());
            if (host != null) {
                recorder.recordAcceptorHost(headers.get(Header.HTTP_HOST.toString()));
            } else {
                // FIXME record Acceptor Host는 URL상의 host를 가져와야한다. 일단 가져올 수 있는 방법이 없어보여 IP라도 추가해둠.
                String acceptorHost = getIpPort(ctx.channel().localAddress());
                recorder.recordAcceptorHost(acceptorHost);
            }
            final String type = headers.get(Header.HTTP_PARENT_APPLICATION_TYPE.toString());
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
                io.netty.handler.codec.http.FullHttpRequest request = (io.netty.handler.codec.http.FullHttpRequest) args[1];
                if (HttpMethod.POST.name().equals(request.getMethod().name()) || HttpMethod.PUT.name().equals(request.getMethod().name())) {
                    // TODO record post body
                } else {
                    // skip
                    Charset uriEncoding = ((UriEncodingGetter) target)._$PINPOINT$_getUriEncoding();
                    String parameters = getRequestParameter(request, MAX_EACH_PARAMETER_SIZE, MAX_ALL_PARAMETER_SIZE, uriEncoding);
                    spanEventRecorder.recordAttribute(AnnotationKey.HTTP_PARAM, parameters);
                }
            }
        } finally {
            if (spanEventRecorder != null) {
                spanEventRecorder.recordException(throwable);
            }
            trace.traceBlockEnd();
        }
    }

    private String getRequestParameter(io.netty.handler.codec.http.FullHttpRequest request, int eachLimit, int totalLimit, java.nio.charset.Charset uriEncoding) {
        String uri = request.getUri();
        QueryStringDecoder decoder = new QueryStringDecoder(uri, uriEncoding);
        Map<String, List<String>> parameters = decoder.parameters();

        final StringBuilder params = new StringBuilder(64);

        for (Entry<String, List<String>> entry : parameters.entrySet()) {
            if (params.length() != 0) {
                params.append('&');
            }

            if (params.length() > totalLimit) {
                params.append("...");
                break;
            }

            String key = entry.getKey();

            params.append(StringUtils.abbreviate(key, eachLimit));
            params.append("=");

            Object value = entry.getValue().get(0);

            if (value != null) {
                params.append(StringUtils.abbreviate(StringUtils.toString(value), eachLimit));
            }
        }

        return params.toString();
    }

}