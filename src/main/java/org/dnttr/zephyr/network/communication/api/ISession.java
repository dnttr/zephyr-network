package org.dnttr.zephyr.network.communication.api;

import org.dnttr.zephyr.network.communication.core.Consumer;
import org.dnttr.zephyr.network.protocol.Packet;

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
