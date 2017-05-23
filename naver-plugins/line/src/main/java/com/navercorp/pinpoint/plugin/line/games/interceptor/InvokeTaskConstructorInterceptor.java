package com.navercorp.pinpoint.plugin.line.games.interceptor;

import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.plugin.line.ChannelHandlerContextAccessor;
import com.navercorp.pinpoint.plugin.line.MessageEventAccessor;

/**
 * @author netspider
 */
public class InvokeTaskConstructorInterceptor implements AroundInterceptor {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private boolean validate(final Object[] args) {
        // 대상 class가 non-static이기 때문에 코드상의 argument length는 2이지만 byte code상으로는 3이다.
        if (args == null || args.length != 3) {
            if (isDebug) {
                logger.debug("Invalid args={}.", args);
            }
            return false;
        }

        if (!(args[1] instanceof ChannelHandlerContext)) {
            if (isDebug) {
                logger.debug("Invalid args[1]={}. Need {}", args[1], ChannelHandlerContext.class.getName());
            }
            return false;
        }

        if (!(args[2] instanceof MessageEvent)) {
            if (isDebug) {
                logger.debug("Invalid args[2]={}. Need {}", args[2], MessageEvent.class.getName());
            }
            return false;
        }

        return true;
    }

    @Override
    public void before(Object target, Object[] args) {
        if (!validate(args)) {
            return;
        }

        ((ChannelHandlerContextAccessor) target)._$PINPOINT$_setChannelHandlerContext((ChannelHandlerContext) args[1]);
        ((MessageEventAccessor) target)._$PINPOINT$_setMessageEvent((MessageEvent) args[2]);
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
    }
}
