/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.receiver.grpc;

import com.navercorp.pinpoint.collector.namespace.NameSpaceInfo;
import com.navercorp.pinpoint.collector.receiver.grpc.security.GrpcSecurityContext;
import com.navercorp.pinpoint.collector.vo.PaaSOrganizationInfo;
import com.navercorp.pinpoint.collector.vo.Token;
import com.navercorp.pinpoint.grpc.Header;
import com.navercorp.pinpoint.grpc.server.ServerContext;
import com.navercorp.pinpoint.grpc.server.TransportMetadata;
import com.navercorp.pinpoint.io.request.Message;
import com.navercorp.pinpoint.io.request.ServerRequest;

import io.grpc.Context;
import io.grpc.StatusException;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.net.InetSocketAddress;

/**
 * @author Taejin Koo
 */
public class AddNameSpaceInfoServerRequestFactoryTest {

    @Test
    public void setNameSpaceInfoTest() throws StatusException {
        addHeader();
        addTransport();
        Token expectedToken = addToken();

        AddNameSpaceInfoServerRequestFactory addNameSpaceInfoServerRequestFactory = new AddNameSpaceInfoServerRequestFactory();
        ServerRequest serverRequest = addNameSpaceInfoServerRequestFactory.newServerRequest(Mockito.mock(Message.class));

        NameSpaceInfo nameSpaceInfo = (NameSpaceInfo) serverRequest.getAttribute(NameSpaceInfo.NAMESPACE_INFO);

        PaaSOrganizationInfo paaSOrganizationInfo = expectedToken.getPaaSOrganizationInfo();
        Assert.assertEquals(paaSOrganizationInfo.getOrganization(), nameSpaceInfo.getOrganization());
        Assert.assertEquals(paaSOrganizationInfo.getDatabaseName(), nameSpaceInfo.getMysqlDatabaseName());
        Assert.assertEquals(paaSOrganizationInfo.getHbaseNameSpace(), nameSpaceInfo.getHbaseNamespace());
    }

    private void addHeader() {
        Header header = Mockito.mock(Header.class);
        Context current = Context.current();
        Context newContext = current.withValue(ServerContext.getAgentInfoKey(), header);
        newContext.attach();
    }

    private void addTransport() {
        TransportMetadata transportMetadata = Mockito.mock(TransportMetadata.class);
        Mockito.when(transportMetadata.getRemoteAddress()).thenReturn(new InetSocketAddress("127.0.0.1", 12345));

        Context current = Context.current();
        Context newContext = current.withValue(ServerContext.getTransportMetadataKey(), transportMetadata);
        newContext.attach();
    }

    private Token addToken() {
        Token token = Mockito.mock(Token.class);
        Mockito.when(token.getPaaSOrganizationInfo()).thenReturn(new PaaSOrganizationInfo("org", "database", "hbase"));

        Context context = GrpcSecurityContext.setAuthTokenHolder(token);
        context.attach();
        return token;
    }

}
