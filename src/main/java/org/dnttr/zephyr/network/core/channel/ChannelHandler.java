package org.dnttr.zephyr.network.core.channel;

import lombok.RequiredArgsConstructor;
import org.dnttr.zephyr.protocol.packet.Carrier;
import org.dnttr.zephyr.protocol.packet.Packet;

/**
 * @author dnttr
 */

@RequiredArgsConstructor
public class ChannelHandler extends ChannelAdapter<Packet, Carrier> {

    private final ChannelController controller;

    @Override
    protected void channelRead(ChannelContext context, Carrier input) throws Exception {
       // controller.fireRead(context, );
    }

    @Override
    protected void channelReadComplete(ChannelContext context) throws Exception {

    }

    @Override
    protected void channelActive(ChannelContext context) throws Exception {

    }

    @Override
    protected void channelInactive(ChannelContext context) throws Exception {

    }

    @Override
    protected Carrier write(ChannelContext context, Packet input) throws Exception {
        return null;
    }

    @Override
    protected void onWriteComplete(ChannelContext context, Packet input, Carrier output) {

    }
}
