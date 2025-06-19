package org.dnttr.zephyr.network.communication.api.client.flow;

import org.dnttr.zephyr.event.EventSubscriber;
import org.dnttr.zephyr.network.communication.api.Endpoint;
import org.dnttr.zephyr.network.communication.core.flow.events.session.SessionEstablishedEvent;
import org.dnttr.zephyr.network.protocol.packets.client.ClientAvailabilityPacket;

public class ClientSessionEndpoint extends Endpoint {

    @EventSubscriber
    public void onSessionEstablished(final SessionEstablishedEvent event) {
        event.getConsumer().send(new ClientAvailabilityPacket(true));
    }
}