package com.nhn.pinpoint.collector.handler;

import org.apache.thrift.TBase;

import java.net.DatagramPacket;

public interface Handler {
    void handler(TBase<?, ?> tbase, byte[] packet, int offset, int length);
}
