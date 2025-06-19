package org.dnttr.zephyr.network.communication.core.channel;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.netty.channel.Channel;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.dnttr.zephyr.network.bridge.Security;
import org.dnttr.zephyr.network.communication.core.Consumer;
import org.dnttr.zephyr.network.protocol.Packet;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.time.Duration;

import static org.dnttr.zephyr.network.bridge.Security.EncryptionMode.NONE;

/**
 * @author dnttr
 */

@Getter
@Setter
public final class ChannelContext {

    private final AsyncCache<ByteBuffer, Boolean> nonces;

    private final Channel channel;
    private final Consumer consumer;

    private final long uuid;

    private Security.EncryptionMode encryptionType;

    @Setter(AccessLevel.NONE)
    private boolean restricted;
    private boolean ready;
    private boolean hash;

    private String restrictionReason;

    public ChannelContext(@NotNull Channel channel) {
        this.channel = channel;

        this.restrictionReason = "Not restricted";
        this.encryptionType = NONE;

        this.uuid = Security.createSession();
        this.nonces = Caffeine
                .newBuilder()
                .expireAfterWrite(Duration.ofSeconds(10))
                .buildAsync();

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

    public void restrict(@NotNull String reason) {
        System.out.println(reason);
        this.restricted = true;
        this.restrictionReason = reason;

        this.channel.disconnect();
    }

    @Override
    public String toString() {
        return "ChannelContext{" +
                "channel=" + channel +
                ", consumer=" + consumer +
                ", uuid=" + uuid +
                ", encryptionType=" + encryptionType +
                ", restricted=" + restricted +
                ", ready=" + ready +
                ", hash=" + hash +
                '}';
    }
}
