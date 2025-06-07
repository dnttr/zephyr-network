package org.dnttr.zephyr.network.api;

import org.dnttr.zephyr.network.core.Consumer;
import org.dnttr.zephyr.protocol.packet.Packet;

/**
 * @author dnttr
 */

public interface ISession {

    void onRead(Consumer consumer, Packet msg);

    void onReadComplete(Consumer consumer);

    void onActive(Consumer consumer);

    void onInactive(Consumer consumer);

    void onWrite(Consumer consumer, Packet msg);

    void onWriteComplete(Consumer consumer, Packet msg);

    void onRestriction(Consumer consumer);
}
