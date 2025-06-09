package org.dnttr.zephyr.network.management.server;

import org.dnttr.zephyr.network.communication.api.ISession;
import org.dnttr.zephyr.network.communication.core.Consumer;
import org.dnttr.zephyr.network.communication.core.packet.Packet;

public class Session implements ISession {

    @Override
    public void onRead(Consumer consumer, Packet packet) {

    }

    @Override
    public void onReadComplete(Consumer consumer) {

    }

    @Override
    public void onActive(Consumer consumer) {

    }

    @Override
    public void onInactive(Consumer consumer) {

    }

    @Override
    public void onWrite(Consumer consumer, Packet packet) {

    }

    @Override
    public void onWriteComplete(Consumer consumer, Packet packet) {

    }

    @Override
    public void onRestriction(Consumer consumer) {

    }
}