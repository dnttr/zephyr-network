package org.dnttr.zephyr.network.loader.api.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.SneakyThrows;
import org.dnttr.zephyr.event.EventBus;
import org.dnttr.zephyr.network.communication.api.Parent;
import org.dnttr.zephyr.network.communication.api.controllers.ClientChannelController;
import org.dnttr.zephyr.network.communication.core.managers.ObserverManager;
import org.dnttr.zephyr.network.communication.core.packet.processor.Transformer;
import org.dnttr.zephyr.network.loader.core.Worker;
import org.dnttr.zephyr.network.communication.core.channel.ChannelHandler;
import org.dnttr.zephyr.network.management.client.Child;

import java.net.InetSocketAddress;

public class Client extends Worker {

    public Client(EventBus eventBus, InetSocketAddress socketAddress) {
        super(eventBus, socketAddress, new Child());

        this.environment.execute();
    }

    @SneakyThrows
    @Override
    protected void construct(Parent session) {
        Bootstrap bootstrap = new Bootstrap();

        ClientChannelController clientChannelController = new ClientChannelController(session, eventBus, new ObserverManager(this.eventBus), new Transformer());

        bootstrap.
                group(this.boss).
                channel(NioSocketChannel.class).
                option(ChannelOption.SO_KEEPALIVE, true).
                handler(new ChannelHandler(clientChannelController, this.eventBus));

        ChannelFuture channelFuture = bootstrap.connect(getAddress()).sync();
        channelFuture.channel().closeFuture().sync();
    }

    @Override
    protected void destroy() {
        this.boss.shutdownGracefully();
    }
}