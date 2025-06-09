package org.dnttr.zephyr.network.communication.core.packet.processor;

import io.netty.buffer.ByteBuf;
import org.dnttr.zephyr.network.communication.core.channel.ChannelContext;
import org.dnttr.zephyr.network.communication.core.packet.Carrier;
import org.dnttr.zephyr.network.protocol.Packet;

public interface IProcessor {

    ByteBuf processInbound(Carrier message, ChannelContext context, byte[] content);

    ByteBuf processOutbound(Packet message, ChannelContext context, byte[] bytes);
}
