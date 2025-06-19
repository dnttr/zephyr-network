package org.dnttr.zephyr.network.communication.api.server.flow;

import org.dnttr.zephyr.event.EventSubscriber;
import org.dnttr.zephyr.network.communication.api.Endpoint;
import org.dnttr.zephyr.network.communication.core.flow.events.session.SessionEstablishedEvent;

public class ServerSessionEndpoint extends Endpoint {

    @EventSubscriber
    public void onSessionEstablished(final SessionEstablishedEvent event) {
    }
}