package org.dnttr.zephyr.network.communication.core.packet.processor;

import org.dnttr.zephyr.network.communication.core.channel.ChannelContext;

public interface IProcessor {

    byte[] processInbound(ChannelContext context, byte[] content);

    byte[] processOutbound(ChannelContext context, byte[] bytes);
}
