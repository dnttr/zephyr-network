package org.dnttr.zephyr.network.communication.core.managers;

import org.dnttr.zephyr.event.EventBus;
import org.dnttr.zephyr.event.EventSubscriber;
import org.dnttr.zephyr.network.communication.core.channel.ChannelContext;
import org.dnttr.zephyr.network.communication.core.flow.Observer;
import org.dnttr.zephyr.network.communication.core.flow.events.packet.PacketReceivedEvent;
import org.dnttr.zephyr.network.communication.core.flow.events.packet.PacketSentEvent;
import org.dnttr.zephyr.network.protocol.Packet;
import org.dnttr.zephyr.network.communication.core.packet.processor.Direction;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author dnttr
 */

public final class ObserverManager {

    private final Map<Class<? extends Packet>, List<Observer>> observers;

    public ObserverManager(@NotNull EventBus bus) {
        this.observers = new ConcurrentHashMap<>();

        bus.register(this);
    }

    public <T extends Packet> Observer observe(@NotNull Class<T> packetClass, @NotNull Direction direction, @NotNull ChannelContext context)
    {
        var observer = new Observer(packetClass, direction, context);

        this.observers.computeIfAbsent(packetClass, _
                -> new CopyOnWriteArrayList<>()).add(observer);

        return observer;
    }

    @EventSubscriber
    public void onPacketReceived(PacketReceivedEvent ev) {
        this.handle(ev.getPacket(), ev.getChannelContext(), Direction.INBOUND);
    }

    @EventSubscriber
    public void onPacketSent(PacketSentEvent ev) {
        this.handle(ev.getPacket(), ev.getChannelContext(), Direction.OUTBOUND);
    }

    private void handle(Packet packet, ChannelContext context, Direction direction) {
        var klass = packet.getClass();
        var observersList = observers.get(klass);

        if (observersList == null) {
            return;
        }

        observersList.stream()
                .filter(observer -> observer.getDirection() == direction && (observer.getContext() == null || observer.getContext().equals(context)))
                .forEach(observer -> {
                    observer.complete(packet);
                    observersList.remove(observer);
        });
    }
}
