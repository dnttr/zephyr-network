package org.dnttr.zephyr.network.communication.core.channel;

import io.netty.channel.*;
import io.netty.channel.ChannelHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.dnttr.zephyr.network.communication.api.server.heartbeat.ServerTimeoutHandler;
import org.dnttr.zephyr.network.communication.core.codec.PacketDecoder;
import org.dnttr.zephyr.network.communication.core.codec.PacketEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author dnttr
 */

@RequiredArgsConstructor
public abstract class ChannelAdapter<I, O> extends ChannelInitializer<SocketChannel> {

    private final boolean timeout;

    @Getter(AccessLevel.PRIVATE)
    private final ContextRegistry registry = new ContextRegistry();

    @Override
    protected void initChannel(SocketChannel socketChannel) {
        List<ChannelHandler> handlers = new ArrayList<>();

        if (this.timeout) {
            handlers.add(new ReadTimeoutHandler(40, TimeUnit.SECONDS));
            handlers.add(new ServerTimeoutHandler());
        }

        handlers.add(new PacketEncoder());
        handlers.add(new PacketDecoder());
        handlers.add(new InboundAdapter(this));
        handlers.add(new OutboundAdapter(this));

        socketChannel.pipeline().addLast(handlers.toArray(new ChannelHandler[0]));
    }

    protected abstract void channelRead(ChannelContext context, O input) throws Exception;

    protected abstract void channelReadComplete(ChannelContext context) throws Exception;

    protected abstract void channelActive(ChannelContext context) throws Exception;

    protected abstract void channelInactive(ChannelContext context) throws Exception;

    protected abstract O write(ChannelContext context, I input) throws Exception;

    protected abstract void onWriteComplete(ChannelContext context, I input, O output);

    @RequiredArgsConstructor
    private class InboundAdapter extends ChannelInboundHandlerAdapter {

        public final ChannelAdapter<I, O> adapter;

        @Override
        @SuppressWarnings("unchecked")
        public final void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            this.adapter.channelRead(this.adapter.getRegistry().get(ctx.channel()), (O) msg);

            super.channelRead(ctx, msg);
        }

        @Override
        public final void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            this.adapter.channelReadComplete(this.adapter.getRegistry().get(ctx.channel()));

            super.channelReadComplete(ctx);
        }

        @Override
        public final void channelActive(ChannelHandlerContext ctx) throws Exception {
            var context = this.adapter.getRegistry().register(ctx.channel());

            this.adapter.channelActive(context);

            super.channelActive(ctx);
        }

        @Override
        public final void channelInactive(ChannelHandlerContext ctx) throws Exception {
            var registry = this.adapter.getRegistry();

            ChannelContext context = registry.get(ctx.channel());

            if (context == null) {
                return;
            }

            this.adapter.channelInactive(context);
            registry.unregister(ctx.channel());

            super.channelInactive(ctx);
        }
    }

    @RequiredArgsConstructor
    private class OutboundAdapter extends ChannelOutboundHandlerAdapter {

        public final ChannelAdapter<I, O> adapter;

        @Override
        @SuppressWarnings("unchecked")
        public final void write(ChannelHandlerContext ctx, Object input, ChannelPromise promise) throws Exception {
            var context = this.adapter.getRegistry().get(ctx.channel());

            I in = (I) input;
            O out = this.adapter.write(context, in);
            promise.addListener((ChannelFutureListener) _ -> this.adapter.onWriteComplete(context, in, out));

            super.write(ctx, out, promise);
        }
    }
}
