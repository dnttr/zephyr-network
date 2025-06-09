package org.dnttr.zephyr.network.communication.core.channel;

import io.netty.channel.Channel;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.dnttr.zephyr.network.bridge.ZEKit;
import org.dnttr.zephyr.network.communication.core.Consumer;
import org.dnttr.zephyr.network.protocol.Packet;

/**
 * @author dnttr
 */

@Getter
@Setter
public final class ChannelContext {

    private final Channel channel;
    private final Consumer consumer;

    private final long uuid;

    private byte[] secret;
    private byte[] nonce; //It is public so nobody cares about it

    private ZEKit.Type encryptionType;

    @Setter(AccessLevel.NONE)
    private boolean restricted;
    private boolean ready;

    public ChannelContext(Channel channel) {
        this.channel = channel;

        this.encryptionType = ZEKit.Type.NONE;
        this.uuid = ZEKit.ffi_zm_open_session();

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
