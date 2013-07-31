package com.nhn.pinpoint.rpc.server;

import com.nhn.pinpoint.rpc.packet.RequestPacket;
import com.nhn.pinpoint.rpc.packet.SendPacket;
import com.nhn.pinpoint.rpc.packet.StreamPacket;

/**
 *
 */
public interface ServerMessageListener {

    void handleSend(SendPacket sendPacket, SocketChannel channel);
    // 외부 노출 Channel은 별도의 Tcp Channel로 감싸는걸로 변경할 것.
    void handleRequest(RequestPacket requestPacket, SocketChannel channel);

    void handleStream(StreamPacket streamPacket, ServerStreamChannel streamChannel);

}
