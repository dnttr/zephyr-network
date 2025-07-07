package org.dnttr.zephyr.network.communication.api.server.flow;

import com.google.gson.Gson;
import org.dnttr.zephyr.event.EventSubscriber;
import org.dnttr.zephyr.network.communication.api.Endpoint;
import org.dnttr.zephyr.network.communication.api.server.relay.AvailableRelayCandidates;
import org.dnttr.zephyr.network.communication.api.server.relay.Candidate;
import org.dnttr.zephyr.network.communication.core.flow.Observer;
import org.dnttr.zephyr.network.communication.core.flow.Relay;
import org.dnttr.zephyr.network.communication.core.flow.events.internal.channel.ConnectionTerminatedEvent;
import org.dnttr.zephyr.network.communication.core.flow.events.packet.PacketInboundEvent;
import org.dnttr.zephyr.network.communication.core.flow.events.session.SessionEstablishedEvent;
import org.dnttr.zephyr.network.communication.core.managers.ObserverManager;
import org.dnttr.zephyr.network.communication.core.packet.Direction;
import org.dnttr.zephyr.network.protocol.packets.internal.*;
import org.dnttr.zephyr.network.protocol.packets.internal.relay.ConnectionRelayAnswer;
import org.dnttr.zephyr.network.protocol.packets.internal.relay.ConnectionRelayRequest;
import org.dnttr.zephyr.network.protocol.packets.internal.relay.ConnectionRelayResponse;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ServerSessionEndpoint extends Endpoint {

    private final AvailableRelayCandidates candidates = new AvailableRelayCandidates();
    private final Gson gson = new Gson();

    public ServerSessionEndpoint(ObserverManager observerManager) {
        super(observerManager);
    }

    @EventSubscriber
    public void onSessionEstablished(final SessionEstablishedEvent event) {
        System.err.printf("[SERVER-INFO] Session established for consumer UUID: %s. Awaiting identification.%n", event.getConsumer().getUuid());
    }

    @EventSubscriber
    public void onPacketReceived(final PacketInboundEvent event) {
        long consumerUuid = event.getConsumer().getUuid();

        if (event.getPacket() instanceof ConnectionIdentifierPacket packet) {
            handleConnectionIdentifier(event, packet);
            return;
        }

        if (this.candidates.getCandidate(consumerUuid) == null) {
            System.err.printf("[SERVER-WARN] Received packet %s from unidentified consumer %s. Ignoring.%n", event.getPacket().getClass().getSimpleName(), consumerUuid);
            return;
        }

        if (event.getPacket() instanceof ConnectionGetUserListPacket) {
            handleGetUserList(event);
            return;
        }

        if (event.getConsumer().isFree()) {
            if (event.getPacket() instanceof ConnectionRelayRequest packet) {
                handleRelayRequest(event, packet);
            }
        } else {
            Relay relay = this.candidates.findRelay(consumerUuid);
            if (relay != null) {
                relay.exchange(consumerUuid, event.getPacket());
            } else {
                System.err.printf("[SERVER-WARN] Consumer UUID %s is NOT free but no active relay found. Dropping packet %s.%n", consumerUuid, event.getPacket().getClass().getSimpleName());
            }
        }
    }

    private void handleGetUserList(PacketInboundEvent event) {
        List<Map<String, Object>> userList = this.candidates.getAllCandidates().stream()
                .map(candidate -> {
                    Map<String, Object> userMap = new java.util.HashMap<>();
                    userMap.put("name", candidate.getName());
                    userMap.put("status", candidate.getConsumer().isFree() ? 1 : 2);
                    return userMap;
                })
                .collect(Collectors.toList());

        String jsonPayload = gson.toJson(userList);
        event.getConsumer().send(new ConnectionUserListPacket(jsonPayload));
    }

    private void handleConnectionIdentifier(PacketInboundEvent event, ConnectionIdentifierPacket packet) {
        String name = packet.getName();
        long consumerUuid = event.getConsumer().getUuid();

        if (name == null || name.isEmpty() || name.length() > 32 || !name.matches("^[a-zA-Z0-9_.-]+$")) {
            System.err.printf("[SERVER-WARN] Invalid name '%s' from UUID %s. Refusing.%n", name, consumerUuid);
            event.getConsumer().send(new ConnectionIdentifierRefusedPacket("Invalid name."));
            return;
        }

        if (this.candidates.containsCandidate(name)) {
            event.getConsumer().send(new ConnectionIdentifierRefusedPacket("Name is already taken."));
            return;
        }

        this.candidates.addCandidate(consumerUuid, new Candidate(name, event.getConsumer()));
        event.getConsumer().setFree(true);
        event.getConsumer().send(new ConnectionIdentifierSuccessPacket());

        broadcastUserListUpdate();
    }

    private void handleRelayRequest(PacketInboundEvent event, ConnectionRelayRequest packet) {
        long senderUuid = event.getConsumer().getUuid();
        String targetName = packet.getName();
        Candidate sender = this.candidates.getCandidate(senderUuid);

        Candidate target = this.candidates.getCandidate(targetName);

        if (target == null) {
            System.err.printf("[SERVER-WARN] Target Candidate '%s' not found. Sending REFUSED to '%s'.%n", targetName, sender.getName());
            sender.getConsumer().send(new ConnectionRelayResponse(ConnectionRelayResponse.State.REFUSED.getValue()));
            return;
        }

        if (sender.equals(target)) {
            System.err.printf("[SERVER-WARN] Sender '%s' attempting to relay to self. Sending REFUSED.%n", sender.getName());
            sender.getConsumer().send(new ConnectionRelayResponse(ConnectionRelayResponse.State.REFUSED.getValue()));
            return;
        }

        if (!target.getConsumer().isFree()) {
            System.err.printf("[SERVER-WARN] Target '%s' is not free. Sending REFUSED to '%s'.%n", target.getName(), sender.getName());
            sender.getConsumer().send(new ConnectionRelayResponse(ConnectionRelayResponse.State.REFUSED.getValue()));
            return;
        }

        try {
            target.getConsumer().send(new ConnectionRelayRequest(sender.getName()));
        } catch (Exception e) {
            System.err.printf("[SERVER-ERROR] Failed to send ConnectionRelayRequest to target '%s': %s%n", target.getName(), e.getMessage());
            sender.getConsumer().send(new ConnectionRelayResponse(ConnectionRelayResponse.State.REFUSED.getValue()));
            return;
        }

        Observer observer = this.getObserverManager().observe(ConnectionRelayAnswer.class, Direction.INBOUND, target.getConsumer().getContext());

        observer.orTimeout(30, TimeUnit.SECONDS).thenAccept(msg -> {
            ConnectionRelayAnswer answer = (ConnectionRelayAnswer) msg;

            if (answer.getState() == ConnectionRelayAnswer.Answer.ACCEPT.getValue()) {
                Relay newRelay = new Relay(sender, target);
                this.candidates.addRelay(newRelay);
                sender.getConsumer().send(new ConnectionRelayResponse(ConnectionRelayResponse.State.SUCCESS.getValue()));
                target.getConsumer().send(new ConnectionRelayResponse(ConnectionRelayResponse.State.SUCCESS.getValue()));
            } else {
                sender.getConsumer().send(new ConnectionRelayResponse(ConnectionRelayResponse.State.REFUSED.getValue()));
            }
        }).exceptionally(ex -> {
            System.err.printf("[SERVER-ERROR] Exception or timeout waiting for relay answer from '%s': %s%n", target.getName(), ex.getMessage());
            if (ex instanceof CompletionException && ex.getCause() instanceof java.util.concurrent.TimeoutException) {
                System.err.printf("[SERVER-WARN] Relay request to '%s' timed out.%n", target.getName());
            }
            sender.getConsumer().send(new ConnectionRelayResponse(ConnectionRelayResponse.State.REFUSED.getValue()));
            return null;
        });
    }

    @EventSubscriber
    public void onSessionTermination(final ConnectionTerminatedEvent event) {
        long terminatedUuid = event.getContext().getConsumer().getUuid();
        Candidate terminatedCandidate = this.candidates.getCandidate(terminatedUuid);
        String terminatedName = (terminatedCandidate != null) ? terminatedCandidate.getName() : "UNKNOWN";

        System.err.printf("[SERVER-INFO] Session terminated for '%s' (UUID %s). Cleaning up.%n", terminatedName, terminatedUuid);

        Relay relay = this.candidates.findRelay(terminatedUuid);
        if (relay != null) {
            System.err.printf("[SERVER-INFO] Terminating active relay for '%s'.%n", terminatedName);
            relay.terminate(terminatedUuid);
            this.candidates.removeRelay(relay);
        }

        if (terminatedCandidate != null) {
            this.candidates.removeCandidate(terminatedUuid);
            System.err.printf("[SERVER-INFO] Candidate '%s' removed.%n", terminatedName);

            broadcastUserListUpdate();
        }
    }

    private void broadcastUserListUpdate() {
        List<Map<String, Object>> userList = this.candidates.getAllCandidates().stream()
                .map(candidate -> {
                    Map<String, Object> userMap = new java.util.HashMap<>();
                    userMap.put("name", candidate.getName());
                    userMap.put("status", candidate.getConsumer().isFree() ? 1 : 2);
                    return userMap;
                })
                .collect(Collectors.toList());

        String jsonPayload = gson.toJson(userList);
        ConnectionUserListPacket packet = new ConnectionUserListPacket(jsonPayload);

        System.err.println("[SERVER-INFO] Broadcasting updated user list to all clients.");

        this.candidates.getAllCandidates().forEach(candidate -> {
            try {
                candidate.getConsumer().send(packet);
            } catch (Exception e) {
                System.err.printf("[SERVER-ERROR] Failed to send user list update to '%s': %s%n", candidate.getName(), e.getMessage());
            }
        });
    }
}