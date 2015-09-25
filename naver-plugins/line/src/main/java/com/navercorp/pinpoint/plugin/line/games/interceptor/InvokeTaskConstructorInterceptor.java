package com.navercorp.pinpoint.plugin.line.games.interceptor;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.annotation.TargetConstructor;
import com.navercorp.pinpoint.plugin.line.ChannelHandlerContextAccessor;
import com.navercorp.pinpoint.plugin.line.LineConstants;
import com.navercorp.pinpoint.plugin.line.MessageEventAccessor;

/**
 * 
 * @author netspider
 * 
 */
@TargetConstructor({"com.linecorp.games.common.baseFramework.handlers.HttpCustomServerHandler", "org.jboss.netty.channel.ChannelHandlerContext", "org.jboss.netty.channel.MessageEvent"})
public class InvokeTaskConstructorInterceptor implements AroundInterceptor {

    @Override
    public void before(Object target, Object[] args) {
        // 대상 class가 non-static이기 때문에 코드상의 argument length는 2이지만 byte code상으로는 3이다.
        if (args.length != 3) {
            return;
        }

        if (!(args[1] instanceof ChannelHandlerContext)) {
            return;
        }

        if (!(args[2] instanceof MessageEvent)) {
            return;
        }

        ((ChannelHandlerContextAccessor)target)._$PINPOINT$_setChannelHandlerContext((ChannelHandlerContext)args[1]);
        ((MessageEventAccessor)target)._$PINPOINT$_setMessageEvent((MessageEvent)args[2]);
    }

    @Override
    public void after(Object target, Object result, Throwable throwable, Object[] args) {

    }
}
