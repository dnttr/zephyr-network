package org.dnttr.zephyr.network.loader.api.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.dnttr.zephyr.event.EventBus;
import org.dnttr.zephyr.network.communication.api.server.ServerChannelController;
import org.dnttr.zephyr.network.communication.api.server.flow.ServerSessionEndpoint;
import org.dnttr.zephyr.network.communication.core.channel.ChannelHandler;
import org.dnttr.zephyr.network.communication.core.managers.ObserverManager;
import org.dnttr.zephyr.network.communication.core.packet.transformer.TransformerFacade;
import org.dnttr.zephyr.network.loader.core.Worker;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;

public final class Server extends Worker {

    private final MultiThreadIoEventLoopGroup child;

    public Server(@NotNull InetSocketAddress socketAddress) {
        super(new EventBus(), socketAddress, new ObserverManager(), new ServerSessionEndpoint());

        this.child = new MultiThreadIoEventLoopGroup(0, NioIoHandler.newFactory());
        this.environment.execute();
    }

    @Override
    protected void construct() {
        final ServerBootstrap bootstrap = new ServerBootstrap();
        final TransformerFacade facade = new TransformerFacade();
        final ServerChannelController controller = new ServerChannelController(this.eventBus, this.observerManager, facade);

        ChannelHandler handler = new ChannelHandler(controller, this.eventBus);

        bootstrap.
                group(this.boss, child).
                option(ChannelOption.SO_BACKLOG, 1024).
                childOption(ChannelOption.SO_KEEPALIVE, true).
                childOption(ChannelOption.TCP_NODELAY, true).
                channel(NioServerSocketChannel.class).
                childHandler(handler);

        try {
            ChannelFuture future = bootstrap.bind(getAddress()).sync();
            future.addListener(f -> {
                if (f.isSuccess()) {
                    System.out.println("Server has been successfully started.");
                }
            });

            future.channel().closeFuture().sync();
        } catch (Exception _) {
            System.out.println("Unable to bind server channel to " + getAddress());
        } finally {
            destroy();
        }
    }

    @Override
    protected void destroy() {
        this.boss.shutdownGracefully();
        this.child.shutdownGracefully();
    }
}