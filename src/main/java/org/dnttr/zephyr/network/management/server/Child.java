package org.dnttr.zephyr.network.management.server;

import org.dnttr.zephyr.event.EventSubscriber;
import org.dnttr.zephyr.network.communication.api.Parent;
import org.dnttr.zephyr.network.communication.core.flow.events.session.SessionEstablishedEvent;

public class Child extends Parent {

    @EventSubscriber
    public void onSessionEstablished(final SessionEstablishedEvent event) {
    }
}