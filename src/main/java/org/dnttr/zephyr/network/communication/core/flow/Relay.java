package org.dnttr.zephyr.network.communication.core.flow;

import org.dnttr.zephyr.network.communication.api.server.relay.Candidate;
import org.dnttr.zephyr.network.protocol.Packet;
import org.dnttr.zephyr.network.protocol.packets.internal.relay.ConnectionRelayTerminatePacket;
import org.dnttr.zephyr.network.protocol.packets.shared.ChatMessagePacket;
import org.dnttr.zephyr.network.protocol.packets.shared.UserDescriptionPacket;
import org.dnttr.zephyr.network.protocol.packets.shared.UserStatusPacket;

import java.util.Set;

/**
 * @author dnttr
 */
public class Relay {

    private final Set<Class<? extends Packet>> relayedPackets;

    private final Candidate candidate1;
    private final Candidate candidate2;

    public Relay(Candidate candidate1, Candidate candidate2) {
        this.candidate1 = candidate1;
        this.candidate2 = candidate2;

        this.relayedPackets = Set.of(
                ChatMessagePacket.class,
                UserStatusPacket.class,
                UserDescriptionPacket.class
        );

        this.candidate1.getConsumer().setFree(false);
        this.candidate2.getConsumer().setFree(false);

        System.err.printf("[RELAY-INFO] New relay established between %s (%s) and %s (%s). Consumers set to not free.%n",
                candidate1.getName(), candidate1.getConsumer().getUuid(),
                candidate2.getName(), candidate2.getConsumer().getUuid());
    }

    /**
     * Forwards a packet from a sender to the other participant if it's an allowed type.
     * @param senderUuid The UUID of the packet sender.
     * @param packet The packet to be relayed.
     */
    public void exchange(long senderUuid, Packet packet) {
        System.err.printf("[RELAY-DEBUG] Exchange attempt from sender UUID %s with packet type %s%n",
                senderUuid, packet.getClass().getSimpleName());

        if (!relayedPackets.contains(packet.getClass())) {
            System.err.printf("[RELAY-WARN] Packet type %s is not allowed for relay. Dropping packet.%n",
                    packet.getClass().getSimpleName());
            return;
        }

        try {
            if (this.candidate1.getConsumer().getUuid() == senderUuid) {
                this.candidate2.getConsumer().send(packet);
            } else if (this.candidate2.getConsumer().getUuid() == senderUuid) {
                this.candidate1.getConsumer().send(packet);
            } else {
                System.err.printf("[RELAY-WARN] Packet from unknown sender UUID %s. Not part of this relay (%s, %s). Dropping.%n",
                        senderUuid, candidate1.getConsumer().getUuid(), candidate2.getConsumer().getUuid());
            }
        } catch (Exception e) {
            System.err.printf("[RELAY-ERROR] Error during packet exchange for sender UUID %s, packet %s: %s%n",
                    senderUuid, packet.getClass().getSimpleName(), e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Checks if a given consumer UUID is part of this relay.
     */
    public boolean contains(long uuid) {
        return this.candidate1.getConsumer().getUuid() == uuid || this.candidate2.getConsumer().getUuid() == uuid;
    }

    /**
     * Terminates the relay, frees both participants, and notifies the other party.
     * @param initiatorUuid The UUID of the client that initiated the termination.
     */
    public void terminate(long initiatorUuid) {
        System.err.printf("[RELAY-INFO] Termination initiated by UUID %s for relay between %s and %s.%n",
                initiatorUuid, candidate1.getName(), candidate2.getName());

        this.candidate1.getConsumer().setFree(true);
        this.candidate2.getConsumer().setFree(true);

        try {
            Candidate recipient = candidate1.getConsumer().getUuid() == initiatorUuid ? candidate2 : candidate1;
            recipient.getConsumer().send(new ConnectionRelayTerminatePacket("The other user disconnected."));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}