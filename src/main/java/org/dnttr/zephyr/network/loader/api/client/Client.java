package org.dnttr.zephyr.network.loader.api.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.dnttr.zephyr.event.EventBus;
import org.dnttr.zephyr.network.communication.api.client.ClientChannelController;
import org.dnttr.zephyr.network.communication.api.client.flow.ClientSessionEndpoint;
import org.dnttr.zephyr.network.communication.core.channel.ChannelHandler;
import org.dnttr.zephyr.network.communication.core.managers.ObserverManager;
import org.dnttr.zephyr.network.communication.core.packet.transformer.TransformerFacade;
import org.dnttr.zephyr.network.loader.core.Worker;

import java.net.InetSocketAddress;

public class Client extends Worker<Bootstrap> {

    public Client(InetSocketAddress socketAddress) {
        super(socketAddress, new EventBus(), new Bootstrap(), new ObserverManager(), new ClientSessionEndpoint());

        this.environment.execute();
    }

    @Override
    protected void construct(Bootstrap bootstrap) {
        var facade = new TransformerFacade();
        var controller = new ClientChannelController(this.eventBus, this.observerManager, facade);

        bootstrap.
                group(this.boss).
                channel(NioSocketChannel.class).
                option(ChannelOption.SO_KEEPALIVE, true).
                handler(new ChannelHandler(controller, this.eventBus));
    }

    @Override
    protected void execute(Bootstrap bootstrap) {
        try{
            ChannelFuture future = bootstrap.connect(getAddress()).addListener(f -> {
                if (f.isSuccess()) {
                    System.out.println("Client connected to " + getAddress());
                }
            }).sync();

            future.channel().closeFuture().addListener(f -> {
                destroy();
            });
        } catch (Exception _) {
            System.out.println("Unable to connect to " + getAddress());
        } finally {
            destroy();
        }
    }

    @Override
    public void destroy() {
        this.boss.shutdownGracefully();
    }
}