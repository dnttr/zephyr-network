package org.dnttr.zephyr.network.communication.api.server.heartbeat;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.ReadTimeoutException;

/**
 * @author dnttr
 */
 
public class ServerTimeoutHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof ReadTimeoutException) {
            System.err.println("[CTX] Read timeout detected for " + ctx.channel().remoteAddress() + ". Closing connection.");
            ctx.close();
        } else {
            super.exceptionCaught(ctx, cause);
        }
    }
}
