package org.dnttr.zephyr.network.communication.core.channel;

import io.netty.channel.Channel;

import java.util.HashMap;

/**
 * @author dnttr
 */

public final class ContextRegistry {

    private final HashMap<String, ChannelContext> contexts = new HashMap<>();

    public ChannelContext get(String identity) {
        return this.contexts.get(identity);
    }

    public ChannelContext get(Channel channel) {
        return this.contexts.get(channel.id().asShortText());
    }

    public ChannelContext register(Channel channel) {
        ChannelContext context = new ChannelContext(channel);
        this.contexts.put(channel.id().asShortText(), context);

        return context;
    }

    public void unregister(String identity) {
        this.contexts.remove(identity);
    }

    public void unregister(Channel channel) {
        this.contexts.remove(channel.id().asShortText());
    }
}
