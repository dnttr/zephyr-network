package org.dnttr.zephyr.network.management.client;

import org.dnttr.zephyr.event.EventSubscriber;
import org.dnttr.zephyr.network.communication.api.ISession;
import org.dnttr.zephyr.network.communication.core.flow.events.session.SessionEstablishedEvent;

public class Session implements ISession {

    @EventSubscriber
    public void onSessionEstablished(final SessionEstablishedEvent event) {
        System.out.println("Session Established");
    }
}