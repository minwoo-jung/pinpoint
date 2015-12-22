package com.navercorp.pinpoint.plugin.lucy.net.nimm.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.lucy.net.nimm.NimmAddressAccessor;
import com.nhncorp.lucy.nimm.connector.address.NimmAddress.Species;

/**
 * target lib = com.nhncorp.lucy.lucy-nimmconnector-2.1.4
 * 
 * @author netspider
 * 
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

        if (args != null && args.length >= 1 && args[0] instanceof com.nhncorp.lucy.nimm.connector.address.NimmAddress) {
            com.nhncorp.lucy.nimm.connector.address.NimmAddress nimmAddress = (com.nhncorp.lucy.nimm.connector.address.NimmAddress) args[0];

            StringBuilder address = new StringBuilder();
            if (Species.Service.equals(nimmAddress.getSpecies())) {
                address.append("S");
            } else if (Species.Management.equals(nimmAddress.getSpecies())) {
                address.append("M");
            } else {
                address.append("unknown");
            }
            address.append(":");
            address.append(nimmAddress.getDomainId()).append(":");
            address.append(nimmAddress.getIdcId()).append(":");
            address.append(nimmAddress.getServerId()).append(":");
            address.append(nimmAddress.getSocketId());

            if (target instanceof NimmAddressAccessor) {
                ((NimmAddressAccessor) target)._$PINPOINT$_setNimmAddress(address.toString());
            }
        }
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {

    }
}