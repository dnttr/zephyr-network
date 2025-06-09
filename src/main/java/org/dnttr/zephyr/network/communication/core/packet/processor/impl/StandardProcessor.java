package org.dnttr.zephyr.network.communication.core.packet.processor.impl;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.dnttr.zephyr.network.communication.core.channel.ChannelContext;
import org.dnttr.zephyr.network.communication.core.packet.Carrier;
import org.dnttr.zephyr.network.protocol.Packet;
import org.dnttr.zephyr.network.communication.core.packet.processor.IProcessor;

/**
 * @author dnttr
 */

public class StandardProcessor implements IProcessor {

    @Override
    public ByteBuf processInbound(Carrier message, ChannelContext context, byte[] content) {
        if (content == null || content.length == 0) {
            return null;
        }

        return message.buffer();
    }

    @Override
    public ByteBuf processOutbound(Packet message, ChannelContext context, byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        return Unpooled.wrappedBuffer(bytes);
    }
}
