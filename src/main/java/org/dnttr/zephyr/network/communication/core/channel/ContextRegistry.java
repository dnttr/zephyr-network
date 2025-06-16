package org.dnttr.zephyr.network.communication.core.channel;

import io.netty.channel.Channel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

/**
 * @author dnttr
 */

public final class ContextRegistry {

    private final HashMap<String, ChannelContext> contexts = new HashMap<>();

    public @Nullable ChannelContext get(@NotNull String identity) {
        return this.contexts.get(identity);
    }

    public @Nullable ChannelContext get(@NotNull Channel channel) {
        return this.contexts.get(channel.id().asShortText());
    }

    public @Nullable ChannelContext register(@NotNull Channel channel) {
        ChannelContext context = new ChannelContext(channel);
        this.contexts.put(channel.id().asShortText(), context);

        return context;
    }

    public void unregister(@NotNull Channel channel) {
        this.contexts.remove(channel.id().asShortText());
    }
}
