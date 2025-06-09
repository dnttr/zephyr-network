package org.dnttr.zephyr.network.communication.core.flow;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.dnttr.zephyr.network.communication.core.channel.ChannelContext;
import org.dnttr.zephyr.network.protocol.Packet;
import org.dnttr.zephyr.network.communication.core.packet.processor.Direction;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author dnttr
 */

@RequiredArgsConstructor
public class Observer extends CompletableFuture<Packet> {

    private final Class<? extends Packet> klass;

    @Getter
    private final Direction direction;

    @Getter
    private final ChannelContext context;

    @NotNull
    public Observer accept(Consumer<? super Packet> action) {
        return this.accept(this.klass, action);
    }

    @NotNull
    public <U> Observer compose(Function<? super Packet, ? extends CompletionStage<U>> fn) {
        super.thenComposeAsync(fn);
        return this;
    }

    @NotNull
    public Observer accept(Class<? extends Packet> klass, Consumer<? super Packet> action) {
        super.thenAcceptAsync(msg -> {
            if (klass.isInstance(msg)) {
                action.accept(klass.cast(msg));
            }
        });

        return this;
    }
}
