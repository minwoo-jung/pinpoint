package com.nhn.pinpoint.rpc.client;

import com.nhn.pinpoint.rpc.DefaultFuture;
import com.nhn.pinpoint.rpc.Future;
import com.nhn.pinpoint.rpc.PinpointSocketException;
import com.nhn.pinpoint.rpc.ResponseMessage;

import java.net.SocketAddress;

/**
 *
 */
public class ReconnectStateSocketHandler implements SocketHandler {


    @Override
    public void setConnectSocketAddress(SocketAddress connectSocketAddress) {
    }

    @Override
    public void open() {
        throw new IllegalStateException();
    }

    @Override
    public void setPinpointSocket(PinpointSocket pinpointSocket) {
    }

    @Override
    public void sendSync(byte[] bytes) {
        throw newReconnectException();
    }

    @Override
    public Future sendAsync(byte[] bytes) {
        return reconnectFailureFuture();
    }

    private DefaultFuture<ResponseMessage> reconnectFailureFuture() {
        DefaultFuture<ResponseMessage> reconnect = new DefaultFuture<ResponseMessage>();
        reconnect.setFailure(newReconnectException());
        return reconnect;
    }

    @Override
    public void close() {
    }

    @Override
    public void send(byte[] bytes) {
    }

    private PinpointSocketException newReconnectException() {
        return new PinpointSocketException("reconnecting...");
    }

    @Override
    public Future<ResponseMessage> request(byte[] bytes) {
        return reconnectFailureFuture();
    }

    @Override
    public StreamChannel createStreamChannel() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendPing() {
    }
}
