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

public final class Server extends Worker<ServerBootstrap> {

    private final MultiThreadIoEventLoopGroup child;

    public Server(@NotNull InetSocketAddress socketAddress) {
        super(socketAddress, new EventBus(), new ServerBootstrap(), new ObserverManager(), new ServerSessionEndpoint());

        this.child = new MultiThreadIoEventLoopGroup(0, NioIoHandler.newFactory());
        this.environment.execute();
    }

    @Override
    protected void construct(ServerBootstrap bootstrap) {
        var facade = new TransformerFacade();
        var controller = new ServerChannelController(this.eventBus, this.observerManager, facade);

        bootstrap.
                group(this.boss, child).
                option(ChannelOption.SO_BACKLOG, 1024).
                childOption(ChannelOption.SO_KEEPALIVE, true).
                childOption(ChannelOption.TCP_NODELAY, true).
                channel(NioServerSocketChannel.class).
                childHandler(new ChannelHandler(controller, this.eventBus));
    }

    @Override
    protected void execute(ServerBootstrap bootstrap) {
        try {
            ChannelFuture future = bootstrap.bind(getAddress()).addListener(f -> {
                if (f.isSuccess()) {
                    System.out.println("Server bound to " + getAddress());
                }
            }).sync();

            future.channel().closeFuture().sync();
        } catch (Exception _) {
            System.out.println("Unable to bind server channel to " + getAddress());
        } finally {
            destroy();
        }
    }

    @Override
    public void destroy() {
        this.boss.shutdownGracefully();
        this.child.shutdownGracefully();
    }
}