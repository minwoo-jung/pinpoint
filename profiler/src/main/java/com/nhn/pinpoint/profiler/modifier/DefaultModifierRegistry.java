package com.nhn.pinpoint.profiler.modifier;

import java.util.HashMap;
import java.util.Map;

import com.nhn.pinpoint.bootstrap.Agent;
import com.nhn.pinpoint.bootstrap.config.ProfilerConfig;
import com.nhn.pinpoint.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.nhn.pinpoint.profiler.modifier.arcus.ArcusClientModifier;
import com.nhn.pinpoint.profiler.modifier.arcus.BaseOperationModifier;
import com.nhn.pinpoint.profiler.modifier.arcus.CacheManagerModifier;
import com.nhn.pinpoint.profiler.modifier.arcus.CollectionFutureModifier;
import com.nhn.pinpoint.profiler.modifier.arcus.FrontCacheGetFutureModifier;
import com.nhn.pinpoint.profiler.modifier.arcus.FrontCacheMemcachedClientModifier;
import com.nhn.pinpoint.profiler.modifier.arcus.GetFutureModifier;
import com.nhn.pinpoint.profiler.modifier.arcus.ImmediateFutureModifier;
import com.nhn.pinpoint.profiler.modifier.arcus.MemcachedClientModifier;
import com.nhn.pinpoint.profiler.modifier.arcus.OperationFutureModifier;
import com.nhn.pinpoint.profiler.modifier.bloc.handler.HTTPHandlerModifier;
import com.nhn.pinpoint.profiler.modifier.bloc4.NettyInboundHandlerModifier;
import com.nhn.pinpoint.profiler.modifier.connector.asynchttpclient.AsyncHttpClientModifier;
import com.nhn.pinpoint.profiler.modifier.connector.httpclient4.BasicFutureModifier;
import com.nhn.pinpoint.profiler.modifier.connector.httpclient4.ClosableHttpAsyncClientModifier;
import com.nhn.pinpoint.profiler.modifier.connector.httpclient4.ClosableHttpClientModifier;
import com.nhn.pinpoint.profiler.modifier.connector.httpclient4.HttpClient4Modifier;
import com.nhn.pinpoint.profiler.modifier.connector.jdkhttpconnector.HttpURLConnectionModifier;
import com.nhn.pinpoint.profiler.modifier.connector.lucynet.CompositeInvocationFutureModifier;
import com.nhn.pinpoint.profiler.modifier.connector.lucynet.DefaultInvocationFutureModifier;
import com.nhn.pinpoint.profiler.modifier.connector.nimm.NimmInvokerModifier;
import com.nhn.pinpoint.profiler.modifier.connector.npc.KeepAliveNpcHessianConnectorModifier;
import com.nhn.pinpoint.profiler.modifier.connector.npc.LightWeightConnectorModifier;
import com.nhn.pinpoint.profiler.modifier.connector.npc.NioNpcHessianConnectorModifier;
import com.nhn.pinpoint.profiler.modifier.connector.npc.NpcHessianConnectorModifier;
import com.nhn.pinpoint.profiler.modifier.db.cubrid.CubridConnectionModifier;
import com.nhn.pinpoint.profiler.modifier.db.cubrid.CubridDriverModifier;
import com.nhn.pinpoint.profiler.modifier.db.cubrid.CubridPreparedStatementModifier;
import com.nhn.pinpoint.profiler.modifier.db.cubrid.CubridResultSetModifier;
import com.nhn.pinpoint.profiler.modifier.db.cubrid.CubridStatementModifier;
import com.nhn.pinpoint.profiler.modifier.db.dbcp.DBCPBasicDataSourceModifier;
import com.nhn.pinpoint.profiler.modifier.db.dbcp.DBCPPoolGuardConnectionWrapperModifier;
import com.nhn.pinpoint.profiler.modifier.db.jtds.*;
import com.nhn.pinpoint.profiler.modifier.db.mysql.MySQLConnectionImplModifier;
import com.nhn.pinpoint.profiler.modifier.db.mysql.MySQLConnectionModifier;
import com.nhn.pinpoint.profiler.modifier.db.mysql.MySQLNonRegisteringDriverModifier;
import com.nhn.pinpoint.profiler.modifier.db.mysql.MySQLPreparedStatementJDBC4Modifier;
import com.nhn.pinpoint.profiler.modifier.db.mysql.MySQLPreparedStatementModifier;
import com.nhn.pinpoint.profiler.modifier.db.mysql.MySQLStatementModifier;
import com.nhn.pinpoint.profiler.modifier.db.oracle.OracleDriverModifier;
import com.nhn.pinpoint.profiler.modifier.db.oracle.OraclePreparedStatementWrapperModifier;
import com.nhn.pinpoint.profiler.modifier.db.oracle.OracleStatementWrapperModifier;
import com.nhn.pinpoint.profiler.modifier.db.oracle.PhysicalConnectionModifier;
import com.nhn.pinpoint.profiler.modifier.linegame.HandlerInvokeTaskModifier;
import com.nhn.pinpoint.profiler.modifier.linegame.HttpCustomServerHandlerModifier;
import com.nhn.pinpoint.profiler.modifier.method.MethodModifier;
import com.nhn.pinpoint.profiler.modifier.orm.ibatis.SqlMapClientImplModifier;
import com.nhn.pinpoint.profiler.modifier.orm.ibatis.SqlMapSessionImplModifier;
import com.nhn.pinpoint.profiler.modifier.orm.mybatis.DefaultSqlSessionModifier;
import com.nhn.pinpoint.profiler.modifier.orm.mybatis.SqlSessionTemplateModifier;
import com.nhn.pinpoint.profiler.modifier.redis.GatewayModifier;
import com.nhn.pinpoint.profiler.modifier.redis.GatewayServerModifier;
import com.nhn.pinpoint.profiler.modifier.redis.JedisClientModifier;
import com.nhn.pinpoint.profiler.modifier.redis.JedisModifier;
import com.nhn.pinpoint.profiler.modifier.redis.JedisPipelineModifier;
import com.nhn.pinpoint.profiler.modifier.redis.RedisClusterModifier;
import com.nhn.pinpoint.profiler.modifier.redis.RedisClusterPipelineModifier;
import com.nhn.pinpoint.profiler.modifier.servlet.HttpServletModifier;
import com.nhn.pinpoint.profiler.modifier.servlet.SpringFrameworkServletModifier;
import com.nhn.pinpoint.profiler.modifier.spring.beans.AbstractAutowireCapableBeanFactoryModifier;
import com.nhn.pinpoint.profiler.modifier.spring.orm.ibatis.SqlMapClientTemplateModifier;
import com.nhn.pinpoint.profiler.modifier.tomcat.StandardHostValveInvokeModifier;
import com.nhn.pinpoint.profiler.modifier.tomcat.StandardServiceModifier;
import com.nhn.pinpoint.profiler.modifier.tomcat.TomcatConnectorModifier;
import com.nhn.pinpoint.profiler.modifier.tomcat.WebappLoaderModifier;

/**
 * @author emeroad
 * @author netspider
 * @author hyungil.jeong
 */
public class DefaultModifierRegistry implements ModifierRegistry {

    // 왠간해서는 동시성 상황이 안나올것으로 보임. 사이즈를 크게 잡아서 체인을 가능한 뒤지지 않도록함.
    private final Map<String, AbstractModifier> registry = new HashMap<String, AbstractModifier>(512);

    private final ByteCodeInstrumentor byteCodeInstrumentor;
    private final ProfilerConfig profilerConfig;
    private final Agent agent;

    public DefaultModifierRegistry(Agent agent, ByteCodeInstrumentor byteCodeInstrumentor) {
        this.agent = agent;
        // classLoader계층 구조 때문에 직접 type을 넣기가 애매하여 그냥 casting
        this.byteCodeInstrumentor = byteCodeInstrumentor;
        this.profilerConfig = agent.getProfilerConfig();
    }

    @Override
    public AbstractModifier findModifier(String className) {
        return registry.get(className);
    }

    private void addModifier(AbstractModifier modifier) {
        AbstractModifier old = registry.put(modifier.getTargetClass(), modifier);
        if (old != null) {
            throw new IllegalStateException("Modifier already exist new:" + modifier.getClass() + " old:" + old.getTargetClass());
        }
    }

    public void addMethodModifier() {
        MethodModifier methodModifier = new MethodModifier(byteCodeInstrumentor, agent);
        addModifier(methodModifier);
    }

    public void addConnectorModifier() {
        // TODO FilterModifier는 인터페이스라서 변경못할 것으로 보임 확인 필요.
        // FilterModifier filterModifier = new
        // FilterModifier(byteCodeInstrumentor, agent);
        // addModifier(filterModifier);

        HttpClient4Modifier httpClient4Modifier = new HttpClient4Modifier(byteCodeInstrumentor, agent);
        addModifier(httpClient4Modifier);

        // jdk HTTPUrlConnector
        HttpURLConnectionModifier httpURLConnectionModifier = new HttpURLConnectionModifier(byteCodeInstrumentor, agent);
        addModifier(httpURLConnectionModifier);

        // ning async http client
        addModifier(new AsyncHttpClientModifier(byteCodeInstrumentor, agent));

        // apache nio http client
        // addModifier(new InternalHttpAsyncClientModifier(byteCodeInstrumentor,
        // agent));
        addModifier(new ClosableHttpAsyncClientModifier(byteCodeInstrumentor, agent));
        addModifier(new ClosableHttpClientModifier(byteCodeInstrumentor, agent));
        addModifier(new BasicFutureModifier(byteCodeInstrumentor, agent));
    }

    public void addArcusModifier() {
        final boolean arcus = profilerConfig.isArucs();
        boolean memcached;
        if (arcus) {
            // arcus가 true일 경우 memcached는 자동으로 true가 되야 한다.
            memcached = true;
        } else {
            memcached = profilerConfig.isMemcached();
        }

        if (memcached) {
            BaseOperationModifier baseOperationModifier = new BaseOperationModifier(byteCodeInstrumentor, agent);
            addModifier(baseOperationModifier);

            MemcachedClientModifier memcachedClientModifier = new MemcachedClientModifier(byteCodeInstrumentor, agent);
            addModifier(memcachedClientModifier);

            // FrontCacheMemcachedClientModifier
            // frontCacheMemcachedClientModifier = new
            // FrontCacheMemcachedClientModifier(byteCodeInstrumentor, agent);
            // 관련 수정에 사이드 이펙트가 있이서 일단 disable함.
            // addModifier(frontCacheMemcachedClientModifier);

            if (arcus) {
                ArcusClientModifier arcusClientModifier = new ArcusClientModifier(byteCodeInstrumentor, agent);
                addModifier(arcusClientModifier);
                // arcus의 Future임
                CollectionFutureModifier collectionFutureModifier = new CollectionFutureModifier(byteCodeInstrumentor, agent);
                addModifier(collectionFutureModifier);
            }

            // future modifier start
            // ---------------------------------------------------

            GetFutureModifier getFutureModifier = new GetFutureModifier(byteCodeInstrumentor, agent);
            addModifier(getFutureModifier);

            ImmediateFutureModifier immediateFutureModifier = new ImmediateFutureModifier(byteCodeInstrumentor, agent);
            addModifier(immediateFutureModifier);

            OperationFutureModifier operationFutureModifier = new OperationFutureModifier(byteCodeInstrumentor, agent);
            addModifier(operationFutureModifier);

            // FrontCacheGetFutureModifier frontCacheGetFutureModifier = new
            // FrontCacheGetFutureModifier(byteCodeInstrumentor, agent);
            // 관련 수정에 사이드 이펙트가 있이서 일단 disable함.
            // addModifier(frontCacheGetFutureModifier);

            // future modifier end
            // ---------------------------------------------------

            CacheManagerModifier cacheManagerModifier = new CacheManagerModifier(byteCodeInstrumentor, agent);
            addModifier(cacheManagerModifier);
        }
    }

    /**
     * BLOC 3.x
     */
    public void addBLOC3Modifier() {
        HTTPHandlerModifier httpHandlerModifier = new HTTPHandlerModifier(byteCodeInstrumentor, agent);
        addModifier(httpHandlerModifier);
    }

    /**
     * BLOC 4.x
     */
    public void addBLOC4Modifier() {
        NettyInboundHandlerModifier nettyInboundHandlerModifier = new NettyInboundHandlerModifier(byteCodeInstrumentor, agent);
        addModifier(nettyInboundHandlerModifier);
    }

    public void addTomcatModifier() {
        StandardHostValveInvokeModifier standardHostValveInvokeModifier = new StandardHostValveInvokeModifier(byteCodeInstrumentor, agent);
        addModifier(standardHostValveInvokeModifier);

        HttpServletModifier httpServletModifier = new HttpServletModifier(byteCodeInstrumentor, agent);
        addModifier(httpServletModifier);

        SpringFrameworkServletModifier springServletModifier = new SpringFrameworkServletModifier(byteCodeInstrumentor, agent);
        addModifier(springServletModifier);

        AbstractModifier tomcatStandardServiceModifier = new StandardServiceModifier(byteCodeInstrumentor, agent);
        addModifier(tomcatStandardServiceModifier);

        AbstractModifier tomcatConnectorModifier = new TomcatConnectorModifier(byteCodeInstrumentor, agent);
        addModifier(tomcatConnectorModifier);

        AbstractModifier tomcatWebappLoaderModifier = new WebappLoaderModifier(byteCodeInstrumentor, agent);
        addModifier(tomcatWebappLoaderModifier);
    }

    public void addJdbcModifier() {
        // TODO 드라이버 존재 체크 로직을 앞단으로 이동 시킬수 없는지 검토
        if (!profilerConfig.isJdbcProfile()) {
            return;
        }

        if (profilerConfig.isJdbcProfileMySql()) {
            addMySqlDriver();
        }

        if (profilerConfig.isJdbcProfileJtds()) {
            addJtdsDriver();
        }

        if (profilerConfig.isJdbcProfileOracle()) {
            addOracleDriver();
        }
        if (profilerConfig.isJdbcProfileCubrid()) {
            addCubridDriver();
        }

        if (profilerConfig.isJdbcProfileDbcp()) {
            addDbcpDriver();
        }
    }

    private void addMySqlDriver() {
        // TODO MySqlDriver는 버전별로 Connection이 interface인지 class인지가 다름. 문제 없는지
        // 확인필요.

        AbstractModifier mysqlNonRegisteringDriverModifier = new MySQLNonRegisteringDriverModifier(byteCodeInstrumentor, agent);
        addModifier(mysqlNonRegisteringDriverModifier);

        // Mysql Dirver가 5.0.x에서 5.1.x로 버전업되면서 MySql Driver가 호환성을 깨버려서 호환성 보정작업을
        // 해야함.
        // MySql 5.1.x드라이버사용시 Driver가 리턴하는 Connection이
        // com.mysql.jdbc.Connection에서 com.mysql.jdbc.JDBC4Connection으로 변경되었음.
        // http://devcafe.nhncorp.com/Lucy/forum/342628
        AbstractModifier mysqlConnectionImplModifier = new MySQLConnectionImplModifier(byteCodeInstrumentor, agent);
        addModifier(mysqlConnectionImplModifier);

        AbstractModifier mysqlConnectionModifier = new MySQLConnectionModifier(byteCodeInstrumentor, agent);
        addModifier(mysqlConnectionModifier);

        AbstractModifier mysqlStatementModifier = new MySQLStatementModifier(byteCodeInstrumentor, agent);
        addModifier(mysqlStatementModifier);

        AbstractModifier mysqlPreparedStatementModifier = new MySQLPreparedStatementModifier(byteCodeInstrumentor, agent);
        addModifier(mysqlPreparedStatementModifier);

        MySQLPreparedStatementJDBC4Modifier myqlPreparedStatementJDBC4Modifier = new MySQLPreparedStatementJDBC4Modifier(byteCodeInstrumentor, agent);
        addModifier(myqlPreparedStatementJDBC4Modifier);
        // result set fectch counter를 만들어야 될듯.
        // Modifier mysqlResultSetModifier = new
        // MySQLResultSetModifier(byteCodeInstrumentor, agent);
        // addModifier(mysqlResultSetModifier);
    }

    private void addJtdsDriver() {
        JtdsDriverModifier jtdsDriverModifier = new JtdsDriverModifier(byteCodeInstrumentor, agent);
        addModifier(jtdsDriverModifier);

        AbstractModifier jdbc2ConnectionModifier = new Jdbc2ConnectionModifier(byteCodeInstrumentor, agent);
        addModifier(jdbc2ConnectionModifier);

        AbstractModifier jdbc4_1ConnectionModifier = new Jdbc4_1ConnectionModifier(byteCodeInstrumentor, agent);
        addModifier(jdbc4_1ConnectionModifier);

        AbstractModifier mssqlStatementModifier = new JtdsStatementModifier(byteCodeInstrumentor, agent);
        addModifier(mssqlStatementModifier);

        AbstractModifier mssqlPreparedStatementModifier = new JtdsPreparedStatementModifier(byteCodeInstrumentor, agent);
        addModifier(mssqlPreparedStatementModifier);

        AbstractModifier mssqlResultSetModifier = new JtdsResultSetModifier(byteCodeInstrumentor, agent);
        addModifier(mssqlResultSetModifier);

    }

    private void addOracleDriver() {
        AbstractModifier oracleDriverModifier = new OracleDriverModifier(byteCodeInstrumentor, agent);
        addModifier(oracleDriverModifier);

        // TODO PhysicalConnection으로 하니 view에서 api가 phy로 나와 모양이 나쁘다.
        // 최상위인 클래스인 T4C T2C, OCI 따로 다 처리하는게 이쁠듯하다.
        AbstractModifier oracleConnectionModifier = new PhysicalConnectionModifier(byteCodeInstrumentor, agent);
        addModifier(oracleConnectionModifier);

        AbstractModifier oraclePreparedStatementModifier = new OraclePreparedStatementWrapperModifier(byteCodeInstrumentor, agent);
        addModifier(oraclePreparedStatementModifier);

        AbstractModifier oracleStatement = new OracleStatementWrapperModifier(byteCodeInstrumentor, agent);
        addModifier(oracleStatement);
        //
        // Modifier oracleResultSetModifier = new
        // OracleResultSetModifier(byteCodeInstrumentor, agent);
        // addModifier(oracleResultSetModifier);
    }

    private void addCubridDriver() {
        // TODO cubrid의 경우도 connection에 대한 impl이 없음. 확인필요.
        addModifier(new CubridConnectionModifier(byteCodeInstrumentor, agent));
        addModifier(new CubridDriverModifier(byteCodeInstrumentor, agent));
        addModifier(new CubridStatementModifier(byteCodeInstrumentor, agent));
        addModifier(new CubridPreparedStatementModifier(byteCodeInstrumentor, agent));
        addModifier(new CubridResultSetModifier(byteCodeInstrumentor, agent));
        // addModifier(new CubridUStatementModifier(byteCodeInstrumentor,
        // agent));
    }

    private void addDbcpDriver() {

        // TODO cubrid의 경우도 connection에 대한 impl이 없음. 확인필요.
        AbstractModifier dbcpBasicDataSourceModifier = new DBCPBasicDataSourceModifier(byteCodeInstrumentor, agent);
        addModifier(dbcpBasicDataSourceModifier);

        if (profilerConfig.isJdbcProfileDbcpConnectionClose()) {
            AbstractModifier dbcpPoolModifier = new DBCPPoolGuardConnectionWrapperModifier(byteCodeInstrumentor, agent);
            addModifier(dbcpPoolModifier);
        }
    }

    public void addNpcModifier() {
        addModifier(new KeepAliveNpcHessianConnectorModifier(byteCodeInstrumentor, agent));
        // addModifier(new
        // LightWeightNbfpConnectorModifier(byteCodeInstrumentor, agent));
        // addModifier(new
        // LightWeightNpcHessianConnectorModifier(byteCodeInstrumentor, agent));
        addModifier(new LightWeightConnectorModifier(byteCodeInstrumentor, agent));
        addModifier(new NioNpcHessianConnectorModifier(byteCodeInstrumentor, agent));
        addModifier(new NpcHessianConnectorModifier(byteCodeInstrumentor, agent));
    }

    public void addNimmModifier() {
        addModifier(new NimmInvokerModifier(byteCodeInstrumentor, agent));
    }

    public void addLucyNetModifier() {
        addModifier(new DefaultInvocationFutureModifier(byteCodeInstrumentor, agent));
        addModifier(new CompositeInvocationFutureModifier(byteCodeInstrumentor, agent));
    }

    /**
     * line game에서 사용하는 baseframework의 http handler를 지원.
     */
    public void addLineGameBaseFrameworkModifier() {
        addModifier(new HandlerInvokeTaskModifier(byteCodeInstrumentor, agent));
        addModifier(new HttpCustomServerHandlerModifier(byteCodeInstrumentor, agent));
    }

    /**
     * orm (iBatis, myBatis 등) 지원.
     */
    public void addOrmModifier() {
        addIBatisSupport();
        addMyBatisSupport();
    }

    public void addRedisSupport() {
        if (profilerConfig.isRedisEnabled()) {
            addModifier(new JedisModifier(byteCodeInstrumentor, agent));
        }

        if (profilerConfig.isRedisPipelineEnabled()) {
            addModifier(new JedisClientModifier(byteCodeInstrumentor, agent));
            addModifier(new JedisPipelineModifier(byteCodeInstrumentor, agent));
        }
    }

    public void addNbaseArcSupport() {
        if (profilerConfig.isNbaseArcEnabled() || profilerConfig.isNbaseArcPipelineEnabled()) {
            addModifier(new GatewayModifier(byteCodeInstrumentor, agent));
            addModifier(new GatewayServerModifier(byteCodeInstrumentor, agent));

            if (profilerConfig.isNbaseArcEnabled()) {
                addModifier(new RedisClusterModifier(byteCodeInstrumentor, agent));
            }

            if (profilerConfig.isNbaseArcPipelineEnabled()) {
                addModifier(new RedisClusterPipelineModifier(byteCodeInstrumentor, agent));
            }
        }
    }

    public void addSpringBeansModifier() {
        if (profilerConfig.isSpringBeansEnabled()) {
            addModifier(new AbstractAutowireCapableBeanFactoryModifier(byteCodeInstrumentor, agent));
        }
    }

    private void addIBatisSupport() {
        if (profilerConfig.isIBatisEnabled()) {
            addModifier(new SqlMapSessionImplModifier(byteCodeInstrumentor, agent));
            addModifier(new SqlMapClientImplModifier(byteCodeInstrumentor, agent));
            addModifier(new SqlMapClientTemplateModifier(byteCodeInstrumentor, agent));
        }
    }

    private void addMyBatisSupport() {
        if (profilerConfig.isMyBatisEnabled()) {
            addModifier(new DefaultSqlSessionModifier(byteCodeInstrumentor, agent));
            addModifier(new SqlSessionTemplateModifier(byteCodeInstrumentor, agent));
        }
    }
}
