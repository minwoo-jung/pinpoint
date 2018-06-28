package com.navercorp.pinpoint.plugin.bloc.v4.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.plugin.request.ServerRequestWrapper;
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.plugin.bloc.AbstractBlocAroundInterceptor;
import com.navercorp.pinpoint.plugin.bloc.BlocConstants;
import com.navercorp.pinpoint.plugin.bloc.NettyServerRequestWrapper;
import com.navercorp.pinpoint.plugin.bloc.v4.UriEncodingGetter;
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
        final io.netty.channel.ChannelHandlerContext ctx = (io.netty.channel.ChannelHandlerContext) args[0];
        final io.netty.handler.codec.http.FullHttpRequest request = (io.netty.handler.codec.http.FullHttpRequest) args[1];
        final HttpHeaders headers = request.headers();
        final String rpcName = request.getUri();
        final String endPoint = getIpPort(ctx.channel().localAddress());
        final String remoteAddress = getIp(ctx.channel().remoteAddress());
        final ServerRequestWrapper serverRequestWrapper = new NettyServerRequestWrapper(headers, rpcName, endPoint, remoteAddress, endPoint);
        final Trace trace = this.requestTraceReader.read(serverRequestWrapper);
        if (trace.canSampled()) {
            SpanRecorder spanRecorder = trace.getSpanRecorder();
            spanRecorder.recordServiceType(BlocConstants.BLOC);
            spanRecorder.recordApi(blocMethodApiTag);
            this.serverRequestRecorder.record(spanRecorder, serverRequestWrapper);
            this.proxyHttpHeaderRecorder.record(spanRecorder, serverRequestWrapper);
        }
        return trace;
    }

    @Override
    protected void doInBeforeTrace(Trace trace, Object target, Object[] args) {
        SpanEventRecorder spanEventRecorder = trace.traceBlockBegin();
        spanEventRecorder.recordApi(methodDescriptor);
        spanEventRecorder.recordServiceType(BlocConstants.BLOC_INTERNAL_METHOD);
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