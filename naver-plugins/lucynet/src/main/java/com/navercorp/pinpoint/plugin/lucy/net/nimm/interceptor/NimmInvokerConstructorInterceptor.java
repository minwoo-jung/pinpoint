package com.navercorp.pinpoint.plugin.lucy.net.nimm.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.plugin.lucy.net.LucyNetConstants;
import com.navercorp.pinpoint.plugin.lucy.net.nimm.NimmAddressAccessor;
import com.nhncorp.lucy.nimm.connector.address.NimmAddress;

/**
 * target lib = com.nhncorp.lucy.lucy-nimmconnector-2.1.4
 *
 * @author netspider
 */
public class NimmInvokerConstructorInterceptor implements AroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final MethodDescriptor descriptor;
    private final TraceContext traceContext;

    // TODO nimm socket도 수집해야하나?? nimmAddress는 constructor에서 string으로 변환한 값을 들고 있음.

    public NimmInvokerConstructorInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        this.traceContext = traceContext;
        this.descriptor = descriptor;
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        if (ArrayUtils.hasLength(args) && args[0] instanceof NimmAddress) {
            NimmAddress nimmAddress = (NimmAddress) args[0];
            if (target instanceof NimmAddressAccessor) {
                ((NimmAddressAccessor) target)._$PINPOINT$_setNimmAddress(addressToString(nimmAddress));
            }
        }
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {

    }

    private String addressToString(NimmAddress nimmAddress) {
        if (nimmAddress == null) {
            return LucyNetConstants.UNKOWN_ADDRESS;
        }

        return nimmAddress.getDomainId() + "." + nimmAddress.getIdcId() + "." + nimmAddress.getServerId() + "."+ nimmAddress.getSocketId();
    }

}