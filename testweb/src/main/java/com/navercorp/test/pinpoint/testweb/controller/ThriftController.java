/*
 * Copyright 2016 NAVER Corp.
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
 *
 */

package com.navercorp.test.pinpoint.testweb.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import com.navercorp.test.pinpoint.testweb.service.thrift.EchoService;
import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.apache.thrift.async.TAsyncClientManager;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingSocket;
import org.apache.thrift.transport.TNonblockingTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author HyunGil Jeong
 */
@Controller
public class ThriftController {
    
    public static final String MESSAGE = "MESSAGE";
    
    @Value("#{thriftServerProps['thrift.server.ip']}")
    private String thriftServerIp;
    
    @Value("#{thriftServerProps['thrift.server.port']}")
    private int thriftServerPort;
    
    @RequestMapping(value = "/thrift/echo")
    @ResponseBody
    public String echo() {
        TTransport transport = null;
        try {
            transport = new TFramedTransport(new TSocket(thriftServerIp, thriftServerPort));
            transport.open();
            TProtocol protocol = new TBinaryProtocol(transport);
            EchoService.Client client = new EchoService.Client(protocol);
            String echo = client.echo(MESSAGE);
            return "echo = " + echo;
        } catch (TException e) {
            return "exception = " + e.getMessage();
        } finally {
            if (transport != null) {
                transport.close();
            }
        }
    }
    
    @RequestMapping(value = "/thrift/echo/async")
    @ResponseBody
    public String echoAsync() throws IOException {
        final TNonblockingTransport transport = new TNonblockingSocket(thriftServerIp, thriftServerPort);
        try {
            EchoService.AsyncClient client = new EchoService.AsyncClient(new TBinaryProtocol.Factory(), new TAsyncClientManager(), transport);
            AsyncMethodCallback<EchoService.AsyncClient.echo_call> callback = new AsyncMethodCallback<EchoService.AsyncClient.echo_call>() {
                @Override
                public void onComplete(EchoService.AsyncClient.echo_call response) {
                    try {
                        response.getResult();
                    } catch (TException e) {
                        // do nothing
                    } finally {
                        if (transport != null) {
                            transport.close();
                        }
                    }
                }
                @Override
                public void onError(Exception exception) {
                    transport.close();
                }
            };
            client.echo(MESSAGE, callback);
            return "done";
        } catch (TException e) {
            return "exception = " + e.getMessage();
        }
    }
    
    @RequestMapping(value = "/thrift/error/api")
    @ResponseBody
    public String errorApi() {
        TTransport transport = null;
        try {
            transport = new TFramedTransport(new TSocket(thriftServerIp, thriftServerPort));
            transport.open();
            TProtocol protocol = new TBinaryProtocol(transport);
            EchoService.Client client = new EchoService.Client(protocol);
            String echo = client.unexisting(MESSAGE);
            return "echo = " + echo;
        } catch (TException e) {
            return "exception = " + e.getMessage();
        } finally {
            if (transport != null) {
                transport.close();
            }
        }
    }
    
    @RequestMapping(value = "/thrift/error/api/async")
    @ResponseBody
    public String errorApiAsync() throws IOException {
        final TNonblockingTransport transport = new TNonblockingSocket(thriftServerIp, thriftServerPort);
        try {
            EchoService.AsyncClient client = new EchoService.AsyncClient(new TBinaryProtocol.Factory(), new TAsyncClientManager(), transport);
            AsyncMethodCallback<EchoService.AsyncClient.unexisting_call> callback = new AsyncMethodCallback<EchoService.AsyncClient.unexisting_call>() {
                @Override
                public void onComplete(EchoService.AsyncClient.unexisting_call response) {
                    try {
                        response.getResult();
                    } catch (TException e) {
                        // do nothing
                    } finally {
                        if (transport != null) {
                            transport.close();
                        }
                    }
                }
                @Override
                public void onError(Exception exception) {
                    transport.close();
                }
            };
            client.unexisting(MESSAGE, callback);
            return "done";
        } catch (TException e) {
            return "exception = " + e.getMessage();
        }
    }
    
    @RequestMapping(value = "/thrift/error/closed")
    @ResponseBody
    public String errorClosed() {
        TTransport transport = null;
        try {
            transport = new TFramedTransport(new TSocket(thriftServerIp, thriftServerPort));
            transport.open();
            TProtocol protocol = new TBinaryProtocol(transport);
            EchoService.Client client = new EchoService.Client(protocol);
            transport.close();
            String echo = client.echo("MESSAGE");
            return "echo = " + echo;
        } catch (TException e) {
            return "exception = " + e.getMessage();
        }
    }
    
    @RequestMapping(value = "/thrift/error/closed/async")
    @ResponseBody
    public String errorClosedAsync() throws IOException {
        final TNonblockingTransport transport = new TNonblockingSocket(thriftServerIp, thriftServerPort);
        try {
            EchoService.AsyncClient client = new EchoService.AsyncClient(new TBinaryProtocol.Factory(), new TAsyncClientManager(), transport);
            AsyncMethodCallback<EchoService.AsyncClient.echo_call> callback = new AsyncMethodCallback<EchoService.AsyncClient.echo_call>() {
                @Override
                public void onComplete(EchoService.AsyncClient.echo_call response) {
                    try {
                        response.getResult();
                    } catch (TException e) {
                        // do nothing
                    }
                }
                @Override
                public void onError(Exception exception) {
                }
            };
            transport.close();
            client.echo(MESSAGE, callback);
            return "done";
        } catch (TException e) {
            return "exception = " + e.getMessage();
        }
    }
    
    @RequestMapping(value = "/thrift/error/protocol")
    @ResponseBody
    public String errorProtocol() {
        TTransport transport = null;
        try {
            transport = new TFramedTransport(new TSocket(thriftServerIp, thriftServerPort));
            transport.open();
            TProtocol protocol = new TCompactProtocol(transport);
            EchoService.Client client = new EchoService.Client(protocol);
            String echo = client.echo("MESSAGE");
            return "echo = " + echo;
        } catch (TException e) {
            return "exception = " + e.getMessage();
        } finally {
            if (transport != null) {
                transport.close();
            }
        }
    }
    
    @RequestMapping(value = "/thrift/error/protocol/async")
    @ResponseBody
    public String errorProtocolAsync() throws IOException {
        final TNonblockingTransport transport = new TNonblockingSocket(thriftServerIp, thriftServerPort);
        try {
            EchoService.AsyncClient client = new EchoService.AsyncClient(new TCompactProtocol.Factory(), new TAsyncClientManager(), transport);
            AsyncMethodCallback<EchoService.AsyncClient.echo_call> callback = new AsyncMethodCallback<EchoService.AsyncClient.echo_call>() {
                @Override
                public void onComplete(EchoService.AsyncClient.echo_call response) {
                    try {
                        response.getResult();
                    } catch (TException e) {
                        // do nothing
                    } finally {
                        if (transport != null) {
                            transport.close();
                        }
                    }
                }
                @Override
                public void onError(Exception exception) {
                    transport.close();
                }
            };
            client.echo(MESSAGE, callback);
            return "done";
        } catch (TException e) {
            return "exception = " + e.getMessage();
        }
    }
    
    @RequestMapping(value = "/thrift/error/callback/async")
    @ResponseBody
    public String errorCallbackAsync() throws IOException {
        final TNonblockingTransport transport = new TNonblockingSocket(thriftServerIp, thriftServerPort);
        try {
            EchoService.AsyncClient client = new EchoService.AsyncClient(new TBinaryProtocol.Factory(), new TAsyncClientManager(), transport);
            AsyncMethodCallback<EchoService.AsyncClient.echo_call> callback = new AsyncMethodCallback<EchoService.AsyncClient.echo_call>() {
                @Override
                public void onComplete(EchoService.AsyncClient.echo_call response) {
                    try {
                        response.getResult();
                    } catch (TException e) {
                        // do nothing
                    } finally {
                        if (transport != null) {
                            transport.close();
                        }
                    }
                    throw new RuntimeException("test callback error");
                }
                @Override
                public void onError(Exception exception) {
                    transport.close();
                }
            };
            client.echo(MESSAGE, callback);
            return "done";
        } catch (TException e) {
            return "exception = " + e.getMessage();
        }
    }
    
    @RequestMapping(value = "/thrift/multipleCalls")
    @ResponseBody
    public List<String> multipleCalls() {
        final int loopCnt = 5;
        List<String> result = new ArrayList<String>(loopCnt);
        TTransport transport = null;
        try {
            transport = new TFramedTransport(new TSocket(thriftServerIp, thriftServerPort));
            transport.open();
            TProtocol protocol = new TBinaryProtocol(transport);
            EchoService.Client client = new EchoService.Client(protocol);
            for (int i = 0; i < loopCnt; i++) {
              try {
                  if ((i+1)%5 == 0) {
                      result.add("unexisting = " + client.unexisting(MESSAGE));
                  } else {
                      result.add("echo = " + client.echo(MESSAGE));
                  }
              } catch (TException e) {
                  result.add("exception = " + e.getMessage());
              }
            }
            return result;
        } catch (TException e) {
            return Arrays.asList("exception = " + e.getMessage());
        } finally {
            if (transport != null) {
                transport.close();
            }
        }
    }
    
    @RequestMapping(value = "/thrift/multipleCalls/async")
    @ResponseBody
    public String multipleCallsAsync() throws IOException, InterruptedException {
        final int loopCnt = 5;
        final TNonblockingTransport transport = new TNonblockingSocket(thriftServerIp, thriftServerPort);
        try {
            EchoService.AsyncClient client = new EchoService.AsyncClient(new TBinaryProtocol.Factory(), new TAsyncClientManager(), transport);
            class EchoAsyncMethodCallback implements AsyncMethodCallback<EchoService.AsyncClient.echo_call> {
                private final CountDownLatch latch;
                
                private EchoAsyncMethodCallback(CountDownLatch latch) {
                    this.latch = latch;
                }
                
                @Override
                public void onComplete(EchoService.AsyncClient.echo_call response) {
                    try {
                        response.getResult();
                    } catch (TException e) {
                        // do nothing
                    } finally {
                        latch.countDown();
                    }
                }

                @Override
                public void onError(Exception exception) {
                    latch.countDown();
                }
                
            }
            class UnexistingAsyncMethodCallback implements AsyncMethodCallback<EchoService.AsyncClient.unexisting_call> {
                private final CountDownLatch latch;
                
                private UnexistingAsyncMethodCallback(CountDownLatch latch) {
                    this.latch = latch;
                }
                
                @Override
                public void onComplete(EchoService.AsyncClient.unexisting_call response) {
                    try {
                        response.getResult();
                    } catch (TException e) {
                        // do nothing
                    } finally {
                        latch.countDown();
                    }
                }

                @Override
                public void onError(Exception exception) {
                    latch.countDown();
                }
            }
            for (int i = 0; i < loopCnt; i++) {
                CountDownLatch latch = new CountDownLatch(1);
                if ((i+1)%5 == 0) {
                    UnexistingAsyncMethodCallback unexistingCallback = new UnexistingAsyncMethodCallback(latch);
                    client.unexisting(MESSAGE, unexistingCallback);
                } else {
                    EchoAsyncMethodCallback echoCallback = new EchoAsyncMethodCallback(latch);
                    client.echo(MESSAGE, echoCallback);
                }
                latch.await();
            }
            return "done";
        } catch (TException e) {
            return "exception = " + e.getMessage();
        } finally {
            if (transport != null) {
                transport.close();
            }
        }
    }
    
}
