package com.nhn.pinpoint.profiler.modifier.bloc4.interceptor;

import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.nhn.pinpoint.bootstrap.context.Header;
import com.nhn.pinpoint.bootstrap.context.Trace;
import com.nhn.pinpoint.bootstrap.context.TraceContext;
import com.nhn.pinpoint.bootstrap.context.TraceId;
import com.nhn.pinpoint.bootstrap.interceptor.ByteCodeMethodDescriptorSupport;
import com.nhn.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.nhn.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.nhn.pinpoint.bootstrap.interceptor.TargetClassLoader;
import com.nhn.pinpoint.bootstrap.interceptor.TraceContextSupport;
import com.nhn.pinpoint.bootstrap.logging.PLogger;
import com.nhn.pinpoint.bootstrap.logging.PLoggerFactory;
import com.nhn.pinpoint.bootstrap.sampler.SamplingFlagUtils;
import com.nhn.pinpoint.bootstrap.util.MetaObject;
import com.nhn.pinpoint.bootstrap.util.NumberUtils;
import com.nhn.pinpoint.bootstrap.util.StringUtils;
import com.nhn.pinpoint.common.AnnotationKey;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.profiler.context.SpanId;

/**
 * @author netspider
 */
public class ChannelRead0Interceptor implements SimpleAroundInterceptor, ByteCodeMethodDescriptorSupport, TraceContextSupport, TargetClassLoader {

	private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
	private final boolean isDebug = logger.isDebugEnabled();

	private MethodDescriptor descriptor;
	private TraceContext traceContext;
	
	private MetaObject<java.nio.charset.Charset> getUriEncoding = new MetaObject<java.nio.charset.Charset>("__getUriEncoding");

	@Override
	public void before(Object target, Object[] args) {
		if (isDebug) {
			logger.beforeInterceptor(target, args);
		}

		if (args.length != 2) {
			return;
		}

		if (!(args[0] instanceof io.netty.channel.ChannelHandlerContext) || !(args[1] instanceof io.netty.handler.codec.http.FullHttpRequest)) {
			return;
		}

		try {
			io.netty.channel.ChannelHandlerContext ctx = (io.netty.channel.ChannelHandlerContext) args[0];
			io.netty.handler.codec.http.FullHttpRequest request = (io.netty.handler.codec.http.FullHttpRequest) args[1];

			boolean sampling = samplingEnable(request);
			if (!sampling) {
				// 샘플링 대상이 아닐 경우도 TraceObject를 생성하여, sampling 대상이 아니라는것을 명시해야
				// 한다.
				// sampling 대상이 아닐경우 rpc 호출에서 sampling 대상이 아닌 것에 rpc호출 파라미터에
				// sampling disable 파라미터를 박을수 있다.
				traceContext.disableSampling();
				if (isDebug) {
					logger.debug("mark disable sampling. skip trace");
				}
				return;
			}

			String requestURL = request.getUri();
			String remoteAddr = ((SocketChannel) ctx.channel()).remoteAddress().toString();

			TraceId traceId = populateTraceIdFromRequest(request);
			Trace trace;
			if (traceId != null) {
				trace = traceContext.continueTraceObject(traceId);
				if (!trace.canSampled()) {
					if (isDebug) {
						logger.debug("TraceID exist. camSampled is false. skip trace. traceId:{}, requestUrl:{}, remoteAddr:{}", new Object[] { traceId, requestURL, remoteAddr });
					}
					return;
				} else {
					if (isDebug) {
						logger.debug("TraceID exist. continue trace. traceId:{}, requestUrl:{}, remoteAddr:{}", new Object[] { traceId, requestURL, remoteAddr });
					}
				}
			} else {
				trace = traceContext.newTraceObject();
				if (!trace.canSampled()) {
					if (isDebug) {
						logger.debug("TraceID not exist. camSampled is false. skip trace. requestUrl:{}, remoteAddr:{}", new Object[] { requestURL, remoteAddr });
					}
					return;
				} else {
					if (isDebug) {
						logger.debug("TraceID not exist. start new trace. requestUrl:{}, remoteAddr:{}", new Object[] { requestURL, remoteAddr });
					}
				}
			}

			trace.markBeforeTime();

			trace.recordServiceType(ServiceType.BLOC);
			trace.recordRpcName(requestURL);
			trace.recordEndPoint(request.getProtocolVersion().protocolName() + ":" + ctx.channel().localAddress());
			trace.recordAttribute(AnnotationKey.HTTP_URL, request.getUri());
		} catch (Throwable e) {
			if (logger.isWarnEnabled()) {
				logger.warn("BLOC4.x http handler trace start fail. Caused:{}", e.getMessage(), e);
			}
		}
	}

	@Override
	public void after(Object target, Object[] args, Object result, Throwable throwable) {
		if (isDebug) {
			logger.afterInterceptor(target, args, result);
		}

		// traceContext.getActiveThreadCounter().end();

		Trace trace = traceContext.currentRawTraceObject();
		if (trace == null) {
			return;
		}
		traceContext.detachTraceObject();

		if (!trace.canSampled()) {
			return;
		}

		try {
			io.netty.handler.codec.http.FullHttpRequest request = (io.netty.handler.codec.http.FullHttpRequest) args[1];

			if (HttpMethod.POST.name().equals(request.getMethod().name()) || HttpMethod.PUT.name().equals(request.getMethod().name())) {
				// TODO record post body
			} else {
				java.nio.charset.Charset uriEncoding = getUriEncoding.invoke(target);
				String parameters = getRequestParameter(request, 64, 512, uriEncoding);
				if (parameters != null && parameters.length() > 0) {
					trace.recordAttribute(AnnotationKey.HTTP_PARAM, parameters);
				}
			}

			trace.recordApi(descriptor);
			trace.recordException(throwable);
			trace.markAfterTime();
		} finally {
			trace.traceRootBlockEnd();
		}
	}

	private boolean samplingEnable(io.netty.handler.codec.http.FullHttpRequest request) {
		// optional 값.
		String samplingFlag = request.headers().get(Header.HTTP_SAMPLED.toString());
		return SamplingFlagUtils.isSamplingFlag(samplingFlag);
	}

	/**
	 * Pupulate source trace from HTTP Header.
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

			TraceId id = this.traceContext.createTraceId(transactionId, parentSpanId, spanId, flags);
			if (isDebug) {
				logger.debug("TraceID exist. continue trace. {}", id);
			}
			return id;
		} else {
			return null;
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

			params.append(StringUtils.drop(key, eachLimit));
			params.append("=");

			Object value = entry.getValue().get(0);

			if (value != null) {
				params.append(StringUtils.drop(StringUtils.toString(value), eachLimit));
			}
		}

		return params.toString();
	}

	@Override
	public void setMethodDescriptor(MethodDescriptor descriptor) {
		this.descriptor = descriptor;
		traceContext.cacheApi(descriptor);
	}

	@Override
	public void setTraceContext(TraceContext traceContext) {
		this.traceContext = traceContext;
	}
}