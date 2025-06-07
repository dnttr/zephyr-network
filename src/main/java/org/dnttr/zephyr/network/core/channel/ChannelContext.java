package org.dnttr.zephyr.network.core.channel;

import io.netty.channel.Channel;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.dnttr.zephyr.network.core.Consumer;
import org.dnttr.zephyr.protocol.packet.Packet;

/**
 * @author dnttr
 */

@Getter
@Setter
public final class ChannelContext {

    private final Channel channel;
    private final Consumer consumer;

    @Setter(AccessLevel.NONE)
    private boolean restricted;
    private boolean ready;

    public ChannelContext(Channel channel) {
        this.channel = channel;

        this.consumer = new Consumer() {
            @Override
            public void send(Packet packet) {
                if (!ready) {
                    return;
                }

                channel.writeAndFlush(packet);
            }
        };
    }

    public void restrict() {
        this.restricted = true;

        this.channel.disconnect();
    }

    @Override
    public String toString() {
        return "ChannelContext{" +
                ", restricted=" + restricted +
                ", ready=" + ready +
                '}';
    }
}
