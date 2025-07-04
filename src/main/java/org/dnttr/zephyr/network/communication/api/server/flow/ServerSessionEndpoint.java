package org.dnttr.zephyr.network.communication.api.server.flow;

import lombok.RequiredArgsConstructor;
import org.dnttr.zephyr.event.EventSubscriber;
import org.dnttr.zephyr.network.communication.api.Endpoint;
import org.dnttr.zephyr.network.communication.api.server.relay.AvailableRelayCandidates;
import org.dnttr.zephyr.network.communication.core.flow.events.packet.PacketInboundEvent;
import org.dnttr.zephyr.network.communication.core.flow.events.session.SessionEstablishedEvent;
import org.dnttr.zephyr.network.communication.core.flow.events.session.SessionTerminatedEvent;
import org.dnttr.zephyr.network.protocol.packets.internal.ConnectionIdentifierPacket;

@RequiredArgsConstructor
public class ServerSessionEndpoint extends Endpoint {

    private final AvailableRelayCandidates candidates;

    @EventSubscriber
    public void onSessionEstablished(final SessionEstablishedEvent event) {
    }

    @EventSubscriber
    public void onPacketReceived(final PacketInboundEvent event) {
        if (event.getPacket() instanceof ConnectionIdentifierPacket packet) {
            String name = packet.getName();

            if (name.isEmpty() || name.length() > 32) {
                return;
            }

            var isOkay = name.matches("^[a-zA-Z0-9]+$");

            if (!isOkay) {
                return;
            }

            this.candidates.addCandidate(name, event.getConsumer());
        }
    }

    @EventSubscriber
    public void onSessionTermination(final SessionTerminatedEvent event) {
    }
}