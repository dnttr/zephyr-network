package org.dnttr.zephyr.network.communication.core.channel;

import io.netty.channel.Channel;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

/**
 * @author dnttr
 */

public final class ContextRegistry {

    @Getter
    private static final HashMap<String, ChannelContext> contexts = new HashMap<>();

    public @Nullable ChannelContext get(@NotNull String identity) {
        return contexts.get(identity);
    }

    public @Nullable ChannelContext get(@NotNull Channel channel) {
        return contexts.get(channel.id().asShortText());
    }

    public @NotNull ChannelContext register(@NotNull Channel channel) {
        ChannelContext context = new ChannelContext(channel);
        contexts.put(channel.id().asShortText(), context);

        return context;
    }

    public void unregister(@NotNull Channel channel) {
        contexts.remove(channel.id().asShortText());
    }
}
