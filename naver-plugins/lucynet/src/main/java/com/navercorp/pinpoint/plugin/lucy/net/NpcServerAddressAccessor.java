package com.navercorp.pinpoint.plugin.lucy.net;

import java.net.InetSocketAddress;

/**
 * @Author Taejin Koo
 */
public interface NpcServerAddressAccessor {

    void _$PINPOINT$_setNpcServerAddress(InetSocketAddress serverAddress);
    InetSocketAddress _$PINPOINT$_getNpcServerAddress();

}
