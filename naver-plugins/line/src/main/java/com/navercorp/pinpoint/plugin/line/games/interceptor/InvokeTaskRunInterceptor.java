package com.navercorp.pinpoint.plugin.line.games.interceptor;

import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.navercorp.pinpoint.bootstrap.plugin.request.RequestTraceReader;
import com.navercorp.pinpoint.bootstrap.plugin.request.ServerRequestTrace;
import com.navercorp.pinpoint.bootstrap.plugin.request.ServerRequestRecorder;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.plugin.line.LineConfig;
import com.navercorp.pinpoint.plugin.line.LineServerRequestTrace;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.jboss.netty.util.CharsetUtil;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanSimpleAroundInterceptor;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.plugin.line.MessageEventAccessor;

/**
 * <pre>
 * com/linecorp/games/common/baseFramework/handlers/HttpCustomServerHandler$InvokeTask.run()를 인터셉트한다.
 * invoke task의 run메소드는 http request를 분석하고 bo를 찾아 실행 후 결과를 반환하는 역할을 담당하는 클래스이다.
 * </pre>
 *
 * @author netspider
 * @author emeroad
 */
public class InvokeTaskRunInterceptor extends SpanSimpleAroundInterceptor {

    private static final String DEFAULT_CHARSET = "UTF-8";

    private final int paramDumpSize;
    private final int entityDumpSize;
    private final boolean param;
    private final ServerRequestRecorder serverRequestRecorder = new ServerRequestRecorder();
    private final RequestTraceReader requestTraceReader;

    public InvokeTaskRunInterceptor(TraceContext traceContext, MethodDescriptor descriptor, int paramDumpSize, int entityDumpSize) {
        super(traceContext, descriptor, InvokeTaskRunInterceptor.class);

        LineConfig config = new LineConfig(traceContext.getProfilerConfig());
        this.paramDumpSize = paramDumpSize;
        this.entityDumpSize = entityDumpSize;
        this.param = config.isParam();

        this.requestTraceReader = new RequestTraceReader(traceContext);
    }

    @Override
    public void doInBeforeTrace(SpanRecorder recorder, Object target, Object[] args) {
    }

    @Override
    protected Trace createTrace(Object target, Object[] args) {
        final MessageEvent messageEvent = ((MessageEventAccessor) target)._$PINPOINT$_getMessageEvent();
        if (messageEvent == null) {
            logger.debug("MessageEvent is null.");
            return null;
        }
        if (!(messageEvent.getMessage() instanceof org.jboss.netty.handler.codec.http.HttpRequest)) {
            logger.debug("MessageEvent is not instance of org.jboss.netty.handler.codec.http.HttpRequest. {}", messageEvent.getMessage());
            return null;
        }
        final org.jboss.netty.handler.codec.http.HttpRequest request = (org.jboss.netty.handler.codec.http.HttpRequest) messageEvent.getMessage();
        final Channel channel = messageEvent.getChannel();
        if (channel == null) {
            logger.debug("Channel is null.");
            return null;
        }
        final String endPoint = getLocalAddress(channel);
        final String remoteAddr = getRemoteAddress(channel);
        final ServerRequestTrace serverRequestTrace = new LineServerRequestTrace(request, endPoint, remoteAddr, endPoint);
        final Trace trace = this.requestTraceReader.read(serverRequestTrace);
        if (trace.canSampled()) {
            final SpanRecorder recorder = trace.getSpanRecorder();
            recorder.recordServiceType(ServiceType.STAND_ALONE);
            recorder.recordApi(this.methodDescriptor);
            this.serverRequestRecorder.record(recorder, serverRequestTrace);
        }
        return trace;
    }

    private String getLocalAddress(Channel channel) {
        return channel.getLocalAddress().toString().substring(1);
    }

    private String getRemoteAddress(Channel channel) {
        return channel.getRemoteAddress().toString().substring(1);
    }

    @Override
    public void doInAfterTrace(SpanRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        if (this.param) {
            MessageEvent e = ((MessageEventAccessor) target)._$PINPOINT$_getMessageEvent();
            if (e != null) {
                recordHttpParameter2(recorder, e);
            }
        }
        recorder.recordApi(methodDescriptor);
        recorder.recordException(throwable);
    }

    private void recordHttpParameter2(SpanRecorder recorder, MessageEvent e) {
        final Object message = e.getMessage();
        if (message instanceof org.jboss.netty.handler.codec.http.HttpRequest) {
            final org.jboss.netty.handler.codec.http.HttpRequest request = (org.jboss.netty.handler.codec.http.HttpRequest) message;
            HttpMethod reqMethod = request.getMethod();

            if (reqMethod.equals(HttpMethod.POST) || reqMethod.equals(HttpMethod.PUT) || reqMethod.equals(HttpMethod.DELETE)) {
                ChannelBuffer content = request.getContent();
                if (content.readable()) {
                    // FIXME request body type에 따른 처리가 필요함.
                    // HttpPostRequestDecoder
                    int contentSize = content.array().length;
                    if (contentSize > entityDumpSize) {
                        contentSize = entityDumpSize;
                    }
                    // netty 3.9 면 getHeader가 deprecated 되었다. 추후 없어질수는 있기는 한데 일단 동작에 문제가 없는듯 함.
                    final Charset charset = getCharset(request);
                    String bodyStr = content.toString(0, contentSize, charset);
                    if (StringUtils.hasLength(bodyStr)) {
                        recorder.recordAttribute(AnnotationKey.HTTP_PARAM_ENTITY, bodyStr);
                    }
                }
            } else if (reqMethod.equals(HttpMethod.GET)) {
                // String parameters = getRequestParameter_old(request, 64,
                // 512);
                String parameters = getRequestParameter(request, paramDumpSize);
                if (StringUtils.hasLength(parameters)) {
                    recorder.recordAttribute(AnnotationKey.HTTP_PARAM, parameters);
                }
            }
        }
    }

    // buffer index를 정확하게 계산하는 로직이나 before에서 데이터를 읽어야 함.
    private void recordHttpParameter(SpanRecorder recorder, MessageEvent e) {
        final Object message = e.getMessage();
        if (message instanceof org.jboss.netty.handler.codec.http.HttpRequest) {
            final org.jboss.netty.handler.codec.http.HttpRequest request = (org.jboss.netty.handler.codec.http.HttpRequest) message;

            final HttpMethod reqMethod = request.getMethod();

            if (equalMethod(HttpMethod.POST, reqMethod) || equalMethod(HttpMethod.PUT, reqMethod) || equalMethod(HttpMethod.DELETE, reqMethod)) {
                ChannelBuffer content = request.getContent();
                int contentSize = content.readableBytes();
                if (contentSize > 0) {
                    // FIXME request body type에 따른 처리가 필요함.
                    // HttpPostRequestDecoder
                    if (contentSize > entityDumpSize) {
                        contentSize = entityDumpSize;
                    }
                    final Charset charset = getCharset(request);

                    // 해당 메소드의 경우 readerIndex를 after에서 읽을경우 다음으로 갈수 있음.
                    String bodyStr = content.toString(content.readerIndex(), contentSize, charset);
                    if (StringUtils.hasLength(bodyStr)) {
                        recorder.recordAttribute(AnnotationKey.HTTP_PARAM_ENTITY, bodyStr);
                    }
                }
            } else if (equalMethod(HttpMethod.GET, reqMethod)) {
                // String parameters = getRequestParameter_old(request, 64, 512);
                final String parameters = getRequestParameter(request, paramDumpSize);
                if (StringUtils.hasLength(parameters)) {
                    recorder.recordAttribute(AnnotationKey.HTTP_PARAM, parameters);
                }
            }
        }
    }

    private Charset getCharset(HttpRequest request) {
        return CharsetUtil.UTF_8;
        // http 스펙상으로는 아래가 맞긴한데. 강제 설정 변경수가 있어서. 아래 처럼할 경우 문제가 생길수 있는것 같음.
        // 코드에 utf8로 박혀 있으면 그냥 UTF-8로 하는게 더 안전할듯함.
//        // netty 3.9 면 getHeader가 deprecated 되었다. 추후 없어질수는 있기는 한데 일단 동작에 문제가 없는듯 함.
//        final String contentTypeValue = request.getHeader("Content-ServiceTypeInfo");
//        // charset.forName을 부르는게 그렇게 좋지는 않은것 같음.
//        return Charset.forName(HttpUtils.parseContentTypeCharset(contentTypeValue, DEFAULT_CHARSET));
    }


    private boolean equalMethod(HttpMethod thisMethod, HttpMethod thatMethod) {
        if (thatMethod == null) {
            return false;
        }
        return thisMethod.getName().equals(thatMethod.getName());
    }

    /**
     * request uri에서 query string 추출 (복잡한 처리 없이 그냥 ? 뒤로 붙어있는 문자열만 추출.)
     *
     * @param request
     * @param totalLimit
     * @return
     */
    private String getRequestParameter(final org.jboss.netty.handler.codec.http.HttpRequest request, int totalLimit) {
        String uri = request.getUri();

        if (uri == null || uri.length() < 2) {
            return null;
        }

        int pos = uri.indexOf('?') + 1;

        if (pos > 1 && pos < uri.length()) {
            return StringUtils.abbreviate(uri.substring(pos), totalLimit);
        } else {
            return null;
        }
    }

    /**
     * request uri에서 querystring 추출
     *
     * @param request
     * @param eachLimit
     * @param totalLimit
     * @return
     */
    @Deprecated
    private String getRequestParameter_old(final org.jboss.netty.handler.codec.http.HttpRequest request, int eachLimit, int totalLimit) {
        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.getUri());
        Map<String, List<String>> queries = queryStringDecoder.getParameters();

        if (queries.isEmpty()) {
            return null;
        }

        StringBuilder params = new StringBuilder(64);
        Iterator<Entry<String, List<String>>> entryIterator = queries.entrySet().iterator();

        while (entryIterator.hasNext()) {
            Entry<String, List<String>> entry = entryIterator.next();

            params.append(entry.getKey());
            params.append("=");

            List<String> values = entry.getValue();

            if (values.size() > 1) {
                Iterator<String> valueIterator = values.iterator();
                while (valueIterator.hasNext()) {
                    params.append(StringUtils.abbreviate(valueIterator.next(), eachLimit));
                    if (valueIterator.hasNext()) {
                        params.append(",");
                    }
                }
            } else {
                params.append(StringUtils.abbreviate(values.get(0), eachLimit));
            }

            if (params.length() > totalLimit) {
                if (entryIterator.hasNext()) {
                    params.append("...");
                }
                break;
            }

            if (entryIterator.hasNext()) {
                params.append("&");
            }
        }

        return params.toString();
    }
}
