package com.navercorp.pinpoint.plugin.lucy.net;

import java.net.InetSocketAddress;

/**
 * @Author Taejin Koo
 */
public interface NpcServerAddressAccessor {

    public void _$PINPOINT$_setNpcServerAddress(InetSocketAddress serverAddress);
    public InetSocketAddress _$PINPOINT$_getNpcServerAddress();

}
