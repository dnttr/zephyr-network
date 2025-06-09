package org.dnttr.zephyr.network.communication.core.managers;

import org.dnttr.zephyr.event.EventBus;
import org.dnttr.zephyr.event.EventSubscriber;
import org.dnttr.zephyr.network.communication.core.channel.ChannelContext;
import org.dnttr.zephyr.network.communication.core.flow.Observer;
import org.dnttr.zephyr.network.communication.core.flow.events.packet.PacketReceivedEvent;
import org.dnttr.zephyr.network.communication.core.flow.events.packet.PacketSentEvent;
import org.dnttr.zephyr.network.protocol.Packet;
import org.dnttr.zephyr.network.communication.core.packet.processor.Direction;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author dnttr
 */

public class ObserverManager {

    private final Map<Class<? extends Packet>, List<Observer>> observers;

    public ObserverManager(EventBus bus) {
        this.observers = new ConcurrentHashMap<>();

        bus.register(this);
    }

    public <T extends Packet> Observer observe(Class<T> packetClass, Direction direction, ChannelContext context)
    {
        Observer observer = new Observer(packetClass, direction, context);
        observers.computeIfAbsent(packetClass, k -> new CopyOnWriteArrayList<>()).add(observer);

        return observer;
    }

    @EventSubscriber
    public void onPacketReceived(PacketReceivedEvent ev) {
        this.process(ev.getPacket(), ev.getChannelContext(), Direction.INBOUND);
    }

    @EventSubscriber
    public void onPacketSent(PacketSentEvent ev) {
        this.process(ev.getPacket(), ev.getChannelContext(), Direction.OUTBOUND);
    }

    private void process(Packet packet, ChannelContext context, Direction direction) {
        Class<? extends Packet> klass = packet.getClass();
        List<Observer> observersList = observers.get(klass);

        if (observersList == null) {
            return;
        }

        for (Observer observer : observersList) {
            if (observer.getDirection() == direction && (observer.getContext() == null || observer.getContext().equals(context))) {
                observer.complete(packet);
                observersList.remove(observer);
            }
        }
    }
}
