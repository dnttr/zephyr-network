package org.dnttr.zephyr.network.core.launch;

import io.netty.channel.nio.NioEventLoopGroup;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.dnttr.zephyr.event.EventBus;
import org.dnttr.zephyr.network.api.ISession;

import java.net.InetSocketAddress;

@RequiredArgsConstructor
public abstract class Worker {

    @Getter
    protected static Worker instance;

    @Getter
    protected final EventBus eventBus;

    @Getter
    protected final InetSocketAddress address;
    protected final Environment environment;
    protected final NioEventLoopGroup boss;

    private final ISession session;

    public Worker(EventBus eventBus, InetSocketAddress address, ISession session) {
        instance = this;

        this.eventBus = eventBus;
        this.address = address;

        this.boss = new NioEventLoopGroup();
        this.environment = new Environment(this);

        this.session = session;
    }

    final void construct0() {
        this.eventBus.register(this.session);
        this.construct(this.session);
    }

    protected abstract void construct(ISession session);

    protected abstract void destroy();
}