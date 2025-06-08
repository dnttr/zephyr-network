package org.dnttr.zephyr.network.loader.api.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.SneakyThrows;
import org.dnttr.zephyr.event.EventBus;
import org.dnttr.zephyr.network.communication.api.controllers.ServerChannelController;
import org.dnttr.zephyr.network.loader.core.Worker;
import org.dnttr.zephyr.network.communication.api.ISession;
import org.dnttr.zephyr.network.communication.core.channel.ChannelHandler;
import org.dnttr.zephyr.network.management.server.Session;

import java.net.InetSocketAddress;

public final class Server extends Worker {

    private final NioEventLoopGroup child; //deprecated? but why the hell. LOL

    public Server(EventBus eventBus, InetSocketAddress socketAddress) {
        super(eventBus, socketAddress, new Session());

        this.child = new NioEventLoopGroup();
        this.environment.execute();
    }

    @SneakyThrows
    @Override
    protected void construct(ISession session) {
        ServerBootstrap bootstrap = new ServerBootstrap();
        ServerChannelController controller = new ServerChannelController(session);
        ChannelHandler handler = new ChannelHandler(controller);

        bootstrap.
                group(this.boss, child).
                option(ChannelOption.SO_BACKLOG, 1024).
                childOption(ChannelOption.SO_KEEPALIVE, true).
                childOption(ChannelOption.TCP_NODELAY, true).
                channel(NioServerSocketChannel.class).
                childHandler(handler);

        ChannelFuture channelFuture = bootstrap.bind(getAddress()).sync();
        channelFuture.channel().closeFuture().sync();
    }

    @Override
    protected void destroy() {
        this.boss.shutdownGracefully();
        this.child.shutdownGracefully();
    }
}