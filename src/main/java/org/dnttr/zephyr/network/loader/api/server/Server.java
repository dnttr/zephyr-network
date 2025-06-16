package org.dnttr.zephyr.network.loader.api.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.SneakyThrows;
import org.dnttr.zephyr.event.EventBus;
import org.dnttr.zephyr.network.communication.api.Parent;
import org.dnttr.zephyr.network.communication.api.controllers.ServerChannelController;
import org.dnttr.zephyr.network.communication.core.channel.ChannelHandler;
import org.dnttr.zephyr.network.communication.core.managers.ObserverManager;
import org.dnttr.zephyr.network.communication.core.packet.processor.Transformer;
import org.dnttr.zephyr.network.loader.core.Worker;
import org.dnttr.zephyr.network.management.server.Child;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;

public final class Server extends Worker {

    private final MultiThreadIoEventLoopGroup child;

    public Server(@NotNull EventBus eventBus, @NotNull InetSocketAddress socketAddress) {
        super(eventBus, socketAddress, new Child());

        this.child = new MultiThreadIoEventLoopGroup(0, NioIoHandler.newFactory());
        this.environment.execute();
    }

    @SneakyThrows
    @Override
    protected void construct(Parent session) {
        ServerBootstrap bootstrap = new ServerBootstrap();
        ServerChannelController controller = new ServerChannelController(session, this.eventBus, new ObserverManager(this.eventBus), new Transformer());
        ChannelHandler handler = new ChannelHandler(controller, this.eventBus);

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