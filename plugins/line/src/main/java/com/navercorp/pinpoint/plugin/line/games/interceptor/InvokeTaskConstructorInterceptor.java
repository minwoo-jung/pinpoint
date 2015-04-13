package com.navercorp.pinpoint.plugin.line.games.interceptor;

import com.navercorp.pinpoint.bootstrap.MetadataAccessor;
import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.Name;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.TargetConstructor;
import com.navercorp.pinpoint.plugin.line.LineConstants;

/**
 * 
 * @author netspider
 * 
 */
@TargetConstructor({"com.linecorp.games.common.baseFramework.handlers.HttpCustomServerHandler", "org.jboss.netty.channel.ChannelHandlerContext", "org.jboss.netty.channel.MessageEvent"})
public class InvokeTaskConstructorInterceptor implements SimpleAroundInterceptor, LineConstants {
    
    private final MetadataAccessor channelHandlerContextAccessor;
    private final MetadataAccessor messageEventAccessor;

    public InvokeTaskConstructorInterceptor(@Name(CHANNEL_HANDLER_CONTEXT) MetadataAccessor channelHandlerContextAccessor, @Name(MESSAGE_EVENT) MetadataAccessor messageEvent) {
        this.channelHandlerContextAccessor = channelHandlerContextAccessor;
        this.messageEventAccessor = messageEvent;
    }

    @Override
    public void before(Object target, Object[] args) {
        // 대상 class가 non-static이기 때문에 코드상의 argument length는 2이지만 byte code상으로는 3이다.
        if (args.length != 3) {
            return;
        }

        if (!(args[1] instanceof org.jboss.netty.channel.ChannelHandlerContext)) {
            return;
        }

        if (!(args[2] instanceof org.jboss.netty.channel.MessageEvent)) {
            return;
        }

        channelHandlerContextAccessor.set(target, args[1]);
        messageEventAccessor.set(target, args[2]);
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {

    }
}
