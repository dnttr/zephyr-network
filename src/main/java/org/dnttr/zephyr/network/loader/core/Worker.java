package org.dnttr.zephyr.network.loader.core;

import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.dnttr.zephyr.event.EventBus;
import org.dnttr.zephyr.network.communication.api.Parent;
import org.dnttr.zephyr.network.communication.core.managers.ObserverManager;

import java.net.InetSocketAddress;

@RequiredArgsConstructor
public abstract class Worker {

    @Getter
    protected static Worker instance;

    @Getter
    protected final EventBus eventBus;

    @Getter
    protected final ObserverManager observerManager;

    @Getter
    protected final InetSocketAddress address;
    protected final Environment environment;
    protected final MultiThreadIoEventLoopGroup boss;

    private final Parent session;

    public Worker(EventBus eventBus, InetSocketAddress address, ObserverManager observerManager, Parent session) {
        instance = this;

        this.eventBus = eventBus;
        this.address = address;

        this.observerManager = observerManager;
        this.boss = new MultiThreadIoEventLoopGroup(1, NioIoHandler.newFactory());
        this.environment = new Environment(this);

        this.session = session;
    }

    final void construct0() {
        this.eventBus.register(this.observerManager);
        this.eventBus.register(this.session);
        this.construct();
    }

    protected abstract void construct();

    protected abstract void destroy();
}