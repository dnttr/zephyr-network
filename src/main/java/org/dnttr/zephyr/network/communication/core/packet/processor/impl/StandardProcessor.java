package org.dnttr.zephyr.network.communication.core.packet.processor.impl;

import org.dnttr.zephyr.network.communication.core.channel.ChannelContext;
import org.dnttr.zephyr.network.communication.core.packet.processor.IProcessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * @author dnttr
 */

public final class StandardProcessor implements IProcessor {

    @Override
    public byte[] processInbound(@NotNull ChannelContext context, byte @NotNull [] content) {
        Objects.requireNonNull(context);
        Objects.requireNonNull(content);

        if (content.length == 0) {
            return null;
        }

        return content;
    }

    @Override
    public byte @Nullable [] processOutbound(@NotNull ChannelContext context, byte @NotNull [] content) {
        Objects.requireNonNull(context);
        Objects.requireNonNull(content);

        if (content.length == 0) {
            return null;
        }

        return content;
    }
}
