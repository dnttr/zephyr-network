package org.dnttr.zephyr.network.communication.core.flow;

import org.dnttr.zephyr.event.EventSubscriber;
import org.dnttr.zephyr.network.communication.core.channel.ChannelContext;
import org.dnttr.zephyr.network.communication.core.flow.events.packet.PacketInboundEvent;
import org.dnttr.zephyr.network.protocol.Packet;
import org.dnttr.zephyr.network.protocol.packets.shared.ChatMessagePacket;
import org.dnttr.zephyr.network.protocol.packets.shared.UserStatusPacket;
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
        this.packets = Set.of(ChatMessagePacket.class, UserStatusPacket.class);
    }

    /**
     * Links a source channel to a target channel. This is for one-to-one routing,
     * which might be less relevant for a general chat but kept for existing structure.
     *
     * @param source The source channel context.
     * @param target The target channel context.
     */
    public void link(@NotNull ChannelContext source, @NotNull ChannelContext target) {
        this.destinations.put(source.getUuid(), target);
    }

    /**
     * Unlinks a source channel.
     *
     * @param source The source channel context to unlink.
     */
    public void unlink(@NotNull ChannelContext source) {
        this.destinations.remove(source.getUuid());
    }


    /**
     * Event handler for incoming packets. Routes packets based on their type.
     *
     * @param ev The PacketInboundEvent containing the packet and consumer context.
     */
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
            if (packets.contains(packet.getClass())) {
                broadcast(context, packet);
            }
        }
    }

    /**
     * Broadcasts a packet to all active channels except the sender.
     *
     * @param packet The packet to broadcast.
     * @param context The ChannelContext of the sender, to exclude from broadcast.
     */
    private void broadcast(@NotNull ChannelContext context, @NotNull Packet packet) {
        if (this.destinations.isEmpty()) {
            return;
        }

        this.destinations.values().stream() //new style...
                .filter(destination ->
                        destination.getUuid() != context.getUuid())
                .forEach(destination ->
                        destination.getChannel().writeAndFlush(packet));
    }
}
