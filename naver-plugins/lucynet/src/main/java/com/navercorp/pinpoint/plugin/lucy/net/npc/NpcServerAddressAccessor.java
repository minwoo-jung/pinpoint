package com.navercorp.pinpoint.plugin.lucy.net.npc;

import java.net.InetSocketAddress;

/**
 * @author Taejin Koo
 */
public interface NpcServerAddressAccessor {

    void _$PINPOINT$_setNpcServerAddress(InetSocketAddress serverAddress);
    InetSocketAddress _$PINPOINT$_getNpcServerAddress();

}
