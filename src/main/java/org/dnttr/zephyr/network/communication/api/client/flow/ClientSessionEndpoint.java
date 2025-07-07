package org.dnttr.zephyr.network.communication.api.client.flow;

import org.dnttr.zephyr.event.EventBus;
import org.dnttr.zephyr.event.EventSubscriber;
import org.dnttr.zephyr.network.communication.api.Endpoint;
import org.dnttr.zephyr.network.communication.core.Consumer;
import org.dnttr.zephyr.network.communication.core.flow.events.internal.channel.ConnectionFatalEvent;
import org.dnttr.zephyr.network.communication.core.flow.events.ipc.recv.*;
import org.dnttr.zephyr.network.communication.core.flow.events.ipc.send.*;
import org.dnttr.zephyr.network.communication.core.flow.events.packet.PacketInboundEvent;
import org.dnttr.zephyr.network.communication.core.flow.events.session.SessionEstablishedEvent;
import org.dnttr.zephyr.network.communication.core.managers.ObserverManager;
import org.dnttr.zephyr.network.protocol.packets.internal.*;
import org.dnttr.zephyr.network.protocol.packets.internal.relay.ConnectionRelayAnswer;
import org.dnttr.zephyr.network.protocol.packets.internal.relay.ConnectionRelayRequest;
import org.dnttr.zephyr.network.protocol.packets.internal.relay.ConnectionRelayResponse;
import org.dnttr.zephyr.network.protocol.packets.internal.relay.ConnectionRelayTerminatePacket;
import org.dnttr.zephyr.network.protocol.packets.shared.ChatMessagePacket;

public class ClientSessionEndpoint extends Endpoint {

    private final EventBus eventBus;
    private volatile Consumer consumer;

    public ClientSessionEndpoint(ObserverManager observerManager, EventBus eventBus) {
        super(observerManager);
        this.eventBus = eventBus;
    }

    @EventSubscriber
    public void onSessionEstablished(final SessionEstablishedEvent event) {
        this.consumer = event.getConsumer();
        long consumerUuid = consumer.getUuid();

        this.eventBus.call(new ReadyForIdentificationEvent());
    }

    @EventSubscriber
    public void onIdentifyCommand(final IdentifyCommand command) {
        if (this.consumer == null) {
            this.eventBus.call(new IdentificationFailureEvent("No active network connection to identify."));
            this.eventBus.call(new ConnectionFatalEvent("No active network connection to identify. Terminating session."));

            return;
        }

        try {
            this.consumer.send(new ConnectionIdentifierPacket(command.getName()));
        } catch (Exception e) {
            System.err.printf("[CLIENT-ERROR] Failed to send ConnectionIdentifierPacket: %s%n", e.getMessage());

            this.eventBus.call(new ConnectionFatalEvent("Failed to send ConnectionIdentifierPacket. Terminating session."));
        }
    }

    @EventSubscriber
    public void onGetUserListCommand(final GetUserListCommand command) {
        if (this.consumer != null) {
            this.consumer.send(new ConnectionGetUserListPacket());
        }
    }

    @EventSubscriber
    public void onRequestRelayCommand(final RequestRelayCommand command) {
        if (this.consumer == null) {
            eventBus.call(new RelayRefusedEvent());
            return;
        }

        this.consumer.send(new ConnectionRelayRequest(command.getTargetName()));
    }

    @EventSubscriber
    public void onAnswerRelayCommand(final AnswerRelayCommand command) {
        if (this.consumer == null) {
            return;
        }

        int answerState = command.isAccepted() ? ConnectionRelayAnswer.Answer.ACCEPT.getValue() : ConnectionRelayAnswer.Answer.REFUSE.getValue();

        this.consumer.send(new ConnectionRelayAnswer(answerState));
    }

    @EventSubscriber
    public void onSendChatMessageCommand(final SendChatMessageCommand command) {
        if (this.consumer != null) {
            this.consumer.send(new ChatMessagePacket(command.getMessage()));
        }
    }

    @EventSubscriber
    public void onPacketReceived(final PacketInboundEvent event) {
        switch (event.getPacket()) {
            case ChatMessagePacket packet ->
                    eventBus.call(new IncomingChatMessageEvent(packet.getMessage()));

            case ConnectionIdentifierRefusedPacket packet -> {
                    eventBus.call(new IdentificationFailureEvent(packet.getReason()));
                    eventBus.call(new ConnectionFatalEvent("ConnectionIdentifierRefusedPacket. Terminating session."));
            }

            case ConnectionRelayRequest packet ->
                    eventBus.call(new IncomingRelayRequestEvent(packet.getName()));

            case ConnectionUserListPacket packet ->
                    eventBus.call(new IncomingUserListEvent(packet.getPayload()));

            case ConnectionIdentifierSuccessPacket _ -> {
                event.getConsumer().setFree(true);
                eventBus.call(new IdentificationSuccessEvent());
            }
            case ConnectionRelayTerminatePacket packet -> {
                event.getConsumer().setFree(true);
                eventBus.call(new RelayTerminatedEvent(packet.getReason()));
                eventBus.call(new ConnectionFatalEvent("Relay terminated. Terminating session."));
            }

            case ConnectionRelayResponse packet -> {
                if (packet.getState() == ConnectionRelayResponse.State.SUCCESS.getValue()) {
                    event.getConsumer().setFree(false);
                    eventBus.call(new RelayEstablishedEvent());
                } else {
                    event.getConsumer().setFree(true);
                    eventBus.call(new RelayRefusedEvent());
                    eventBus.call(new ConnectionFatalEvent("Relay refused. Terminating session."));
                }
            }

            default -> {
            }
        }
    }
}