package org.dnttr.zephyr.network.communication.core.flow;

import org.dnttr.zephyr.event.EventSubscriber;
import org.dnttr.zephyr.network.communication.core.channel.ChannelContext;
import org.dnttr.zephyr.network.communication.core.flow.events.packet.PacketInboundEvent;
import org.dnttr.zephyr.network.protocol.Packet;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author dnttr
 */

public final class Relay {

    private final Map<Long, ChannelContext> destinations = new ConcurrentHashMap<>();
    private final Set<Class<? extends Packet>> packets;

    public Relay() {
        this.packets = Set.of();
    }

    public void link(@NotNull ChannelContext source, @NotNull ChannelContext target) {
        this.destinations.put(source.getUuid(), target);
    }

    public void unlink(@NotNull ChannelContext source) {
        this.destinations.remove(source.getUuid());
    }
    
    @EventSubscriber
    public void onPacketReceived(final PacketInboundEvent ev) {
        if (this.destinations.isEmpty() || this.packets.isEmpty()) {
            return;
        }

        var packet = ev.getPacket();
        var consumer = ev.getConsumer();

        if (!this.packets.contains(packet.getClass())) {
            return;
        }

        var context = this.destinations.get(consumer.getUuid());

        if (context != null) {
            context.getChannel().writeAndFlush(packet);
        }
    }
}
