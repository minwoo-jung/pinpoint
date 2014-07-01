package com.nhn.pinpoint.profiler.modifier.db.interceptor;

import com.nhn.pinpoint.bootstrap.context.Trace;
import com.nhn.pinpoint.bootstrap.context.TraceContext;
import com.nhn.pinpoint.bootstrap.interceptor.*;
import com.nhn.pinpoint.bootstrap.logging.PLoggerFactory;
import com.nhn.pinpoint.bootstrap.context.DatabaseInfo;
import com.nhn.pinpoint.bootstrap.logging.PLogger;
import com.nhn.pinpoint.bootstrap.util.MetaObject;

/**
 * @author netspider
 * @author emeroad
 */
public class StatementExecuteQueryInterceptor extends SpanEventSimpleAroundInterceptor {


    private final MetaObject<DatabaseInfo> getDatabaseInfo = new MetaObject<DatabaseInfo>(UnKnownDatabaseInfo.INSTANCE, "__getDatabaseInfo");

    public StatementExecuteQueryInterceptor() {
        super(PLoggerFactory.getLogger(StatementExecuteQueryInterceptor.class));
    }

    @Override
    public void doInBeforeTrace(Trace trace, final Object target, Object[] args) {
        trace.markBeforeTime();
        /**
         * If method was not called by request handler, we skip tagging.
         */
        DatabaseInfo databaseInfo = this.getDatabaseInfo.invoke(target);
        if (databaseInfo == null) {
            databaseInfo = UnKnownDatabaseInfo.INSTANCE;
        }
        trace.recordServiceType(databaseInfo.getExecuteQueryType());
        trace.recordEndPoint(databaseInfo.getMultipleHost());
        trace.recordDestinationId(databaseInfo.getDatabaseId());

    }


    @Override
    public void doInAfterTrace(Trace trace, Object target, Object[] args, Object result, Throwable throwable) {

        trace.recordApi(getMethodDescriptor());
        if (args.length > 0) {
            Object arg = args[0];
            if (arg instanceof String) {
                trace.recordSqlInfo((String) arg);
                // TODO parsing result 추가 처리 고려
            }
        }
        trace.recordException(throwable);
        trace.markAfterTime();

    }

}
