package org.dnttr.zephyr.network.loader.api.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.SneakyThrows;
import org.dnttr.zephyr.event.EventBus;
import org.dnttr.zephyr.network.communication.api.controllers.ClientChannelController;
import org.dnttr.zephyr.network.loader.core.Worker;
import org.dnttr.zephyr.network.communication.api.ISession;
import org.dnttr.zephyr.network.communication.core.channel.ChannelHandler;
import org.dnttr.zephyr.network.management.client.Session;

import java.net.InetSocketAddress;

public class Client extends Worker {

    public Client(EventBus eventBus, InetSocketAddress socketAddress) {
        super(eventBus, socketAddress, new Session());

        this.environment.execute();
    }

    @SneakyThrows
    @Override
    protected void construct(ISession session) {
        Bootstrap bootstrap = new Bootstrap();

        bootstrap.
                group(this.boss).
                channel(NioSocketChannel.class).
                option(ChannelOption.SO_KEEPALIVE, true).
                handler(new ChannelHandler(new ClientChannelController(session, eventBus)));

        ChannelFuture channelFuture = bootstrap.connect(getAddress()).sync();
        channelFuture.channel().closeFuture().sync();
    }

    @Override
    protected void destroy() {
        this.boss.shutdownGracefully();
    }
}