package org.dnttr.zephyr.network.communication.core.packet.processor;

import org.dnttr.zephyr.network.communication.core.channel.ChannelContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IProcessor {

    byte @Nullable [] processInbound(@NotNull ChannelContext context, byte @NotNull [] content);

    byte @Nullable [] processOutbound(@NotNull ChannelContext context, byte @NotNull [] bytes);
}
