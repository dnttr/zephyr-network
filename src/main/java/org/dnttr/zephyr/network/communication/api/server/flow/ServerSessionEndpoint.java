package org.dnttr.zephyr.network.communication.api.server.flow;

import org.dnttr.zephyr.event.EventSubscriber;
import org.dnttr.zephyr.network.communication.api.Parent;
import org.dnttr.zephyr.network.communication.core.flow.events.session.SessionEstablishedEvent;

public class ServerSessionEndpoint extends Parent {

    @EventSubscriber
    public void onSessionEstablished(final SessionEstablishedEvent event) {
    }
}