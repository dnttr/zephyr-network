package org.dnttr.zephyr.network.communication.core.packet.processor.impl;

import org.dnttr.zephyr.network.communication.core.channel.ChannelContext;
import org.dnttr.zephyr.network.communication.core.packet.processor.IProcessor;
import org.dnttr.zephyr.network.protocol.Packet;

/**
 * @author dnttr
 */

public class StandardProcessor implements IProcessor {

    @Override
    public byte[] processInbound(ChannelContext context, byte[] content) {
        if (content == null || content.length == 0) {
            return null;
        }

        return content;
    }

    @Override
    public byte[] processOutbound(Packet message, ChannelContext context, byte[] content) {
        if (content == null || content.length == 0) {
            return null;
        }

        return content;
    }
}
