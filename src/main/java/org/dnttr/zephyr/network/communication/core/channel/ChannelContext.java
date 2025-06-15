package org.dnttr.zephyr.network.communication.core.channel;

import io.netty.channel.Channel;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.dnttr.zephyr.network.bridge.Security;
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

    private Security.EncryptionMode encryptionType;

    @Setter(AccessLevel.NONE)
    private boolean restricted;
    private boolean ready;
    private boolean hash;

    public ChannelContext(Channel channel) {
        this.channel = channel;

        this.encryptionType = Security.EncryptionMode.NONE;
        this.uuid = Security.createSession();

        Security.generateSigningKeyPair(this.uuid);

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
