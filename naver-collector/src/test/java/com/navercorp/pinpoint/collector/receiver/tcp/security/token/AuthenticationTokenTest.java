package com.navercorp.pinpoint.collector.receiver.tcp.security.token;

import com.navercorp.pinpoint.collector.receiver.tcp.DefaultTCPPacketHandlerFactory;
import com.navercorp.pinpoint.collector.service.TokenService;
import com.navercorp.pinpoint.collector.vo.PaaSOrganizationInfo;
import com.navercorp.pinpoint.collector.vo.Token;
import com.navercorp.pinpoint.collector.vo.TokenType;
import com.navercorp.pinpoint.rpc.Future;
import com.navercorp.pinpoint.rpc.ResponseMessage;
import com.navercorp.pinpoint.rpc.client.ClientCodecPipelineFactory;
import com.navercorp.pinpoint.rpc.client.DefaultConnectionFactoryProvider;
import com.navercorp.pinpoint.rpc.client.DefaultPinpointClientFactory;
import com.navercorp.pinpoint.rpc.client.PinpointClient;
import com.navercorp.pinpoint.rpc.client.PinpointClientFactory;
import com.navercorp.pinpoint.rpc.server.ServerMessageListenerFactory;
import com.navercorp.pinpoint.rpc.util.PinpointRPCTestUtils;
import com.navercorp.pinpoint.rpc.util.TimerFactory;
import com.navercorp.pinpoint.test.server.TestPinpointServerAcceptor;
import com.navercorp.pinpoint.test.utils.TestAwaitTaskUtils;
import com.navercorp.pinpoint.test.utils.TestAwaitUtils;
import com.navercorp.pinpoint.thrift.dto.TApiMetaData;
import com.navercorp.pinpoint.thrift.dto.TResult;
import com.navercorp.pinpoint.thrift.dto.command.TCmdAuthenticationToken;
import com.navercorp.pinpoint.thrift.dto.command.TCmdAuthenticationTokenRes;
import com.navercorp.pinpoint.thrift.dto.command.TTokenResponseCode;
import org.apache.thrift.TBase;
import org.jboss.netty.util.HashedWheelTimer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Taejin Koo
 */
public class AuthenticationTokenTest {

    private static final String LOCAL_HOST = "127.0.0.1";

    private static String AUTH_KEY = "secret";

    private static ExecutorService executors;

    private static HashedWheelTimer timer;

    private final TokenSerDes tokenSerDes = new TokenSerDes();


    @Before
    public void setUp() {
        executors = Executors.newFixedThreadPool(1);

        timer = TimerFactory.createHashedWheelTimer(AuthenticationTokenTest.class.getName() + "-TIMER", 100, TimeUnit.MILLISECONDS, 512);
        timer.start();
    }

    @After
    public void tearDown() {
        if (timer != null) {
            timer.stop();
        }

        if (executors != null) {
            executors.shutdownNow();
        }
    }

    // 1. success authentication,
    // 2. success send & receive
    @Test
    public void authenticationSuccessTest() throws Exception {
        TestPinpointServerAcceptor testPinpointServerAcceptor = createServer();

        PinpointClientFactory clientFactory = null;
        PinpointClient client = null;
        try {
            int bindPort = testPinpointServerAcceptor.bind();

            clientFactory = createClientFactory();
            client = clientFactory.connect(LOCAL_HOST, bindPort);

            TCmdAuthenticationTokenRes tokenResponse = authenticate(client, AUTH_KEY);
            Assert.assertTrue(tokenResponse.getCode() == TTokenResponseCode.OK);

            TApiMetaData tApiMetaData = new TApiMetaData("test", System.currentTimeMillis(), 1, "TestApi");
            TResult resultResponse = request(client, tApiMetaData, TResult.class);
            Assert.assertTrue(resultResponse.isSuccess());
        } finally {
            testPinpointServerAcceptor.close();
            PinpointRPCTestUtils.close(client);

            if (clientFactory != null) {
                clientFactory.release();
            }
        }
    }

    // 1. fail authentication,
    // 2. channel disconnect
    @Test
    public void authenticationFailTest() throws Exception {
        TestPinpointServerAcceptor testPinpointServerAcceptor = createServer();

        PinpointClientFactory clientFactory = null;
        PinpointClient client = null;
        try {
            int bindPort = testPinpointServerAcceptor.bind();

            clientFactory = createClientFactory();
            client = clientFactory.connect(LOCAL_HOST, bindPort);

            TCmdAuthenticationTokenRes tokenResponse = authenticate(client, AUTH_KEY + "fail");
            Assert.assertTrue(tokenResponse.getCode() == TTokenResponseCode.UNAUTHORIZED);

            final PinpointClient finalClient = client;
            boolean await = TestAwaitUtils.await(new TestAwaitTaskUtils() {
                @Override
                public boolean checkCompleted() {
                    return !finalClient.isConnected();
                }
            }, 100, 1000);

            Assert.assertTrue(await);
        } finally {
            testPinpointServerAcceptor.close();
            PinpointRPCTestUtils.close(client);

            if (clientFactory != null) {
                clientFactory.release();
            }
        }
    }

    private TestPinpointServerAcceptor createServer() {
        TokenService tokenService = createMockTokenService();
        CountingDispatchHandler dispahtchHandler = new CountingDispatchHandler(true);

        ServerMessageListenerFactory messageListenerFactory = new TokenMessageListenerFactory(executors, tokenService, new DefaultTCPPacketHandlerFactory(), dispahtchHandler);

        return new TestPinpointServerAcceptor(messageListenerFactory);
    }

    private PinpointClientFactory createClientFactory() {
        DefaultConnectionFactoryProvider connectionFactoryProvider = new DefaultConnectionFactoryProvider(new ClientCodecPipelineFactory());
        return new DefaultPinpointClientFactory(connectionFactoryProvider);
    }

    private TokenService createMockTokenService() {
        long startTime = System.currentTimeMillis();
        long endTime = startTime + 3000;

        PaaSOrganizationInfo paaSOrganizationInfo = new PaaSOrganizationInfo("org", "namespace", "hbaseNamespace");

        Token token = new Token("key", paaSOrganizationInfo, endTime, "127.0.0.1", TokenType.SPAN);

        TokenService mockTokenService = Mockito.mock(TokenService.class);
        Mockito.when(mockTokenService.getAndRemove(AUTH_KEY, TokenType.ALL)).thenReturn(token);
        return mockTokenService;
    }

    private TCmdAuthenticationTokenRes authenticate(PinpointClient client, String tokenKey) {
        TCmdAuthenticationToken token = new TCmdAuthenticationToken();
        token.setToken(tokenKey.getBytes());

        return request(client, token, TCmdAuthenticationTokenRes.class);
    }

    private <T extends TBase> T request(PinpointClient client, TBase request, Class<T> responseType) {
        byte[] payload = tokenSerDes.serialize(request);

        Future<ResponseMessage> future = client.request(payload);
        future.await(3000);
        ResponseMessage responseMessage = future.getResult();
        return tokenSerDes.deserialize(responseMessage.getMessage(), responseType);
    }

}