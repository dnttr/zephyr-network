package org.dnttr.zephyr.network.communication.core.packet.processor;

import org.dnttr.zephyr.network.communication.core.channel.ChannelContext;
import org.dnttr.zephyr.network.protocol.Packet;

public interface IProcessor {

    byte[] processInbound(ChannelContext context, byte[] content);

    byte[] processOutbound(Packet message, ChannelContext context, byte[] bytes);
}
