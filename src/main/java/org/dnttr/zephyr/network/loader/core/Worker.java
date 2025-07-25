package org.dnttr.zephyr.network.loader.core;

import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.dnttr.zephyr.event.EventBus;
import org.dnttr.zephyr.network.communication.api.Endpoint;
import org.dnttr.zephyr.network.communication.core.flow.events.internal.management.ManagerTerminationEvent;
import org.dnttr.zephyr.network.communication.core.managers.ObserverManager;

import java.net.InetSocketAddress;

@RequiredArgsConstructor
public abstract class Worker<B> {

    @Getter
    protected static Worker<?> instance;

    @Getter
    protected final EventBus eventBus;

    @Getter
    protected final ObserverManager observerManager;

    @Getter
    protected final InetSocketAddress address;
    protected final Environment environment;
    protected final MultiThreadIoEventLoopGroup boss;

    private final Endpoint endpoint;

    private B bootstrap;

    public Worker(InetSocketAddress address, EventBus eventBus, B bootstrap, ObserverManager observerManager, Endpoint endpoint) {
        instance = this;

        this.address = address;
        this.eventBus = eventBus;
        this.bootstrap = bootstrap;

        this.observerManager = observerManager;
        this.boss = new MultiThreadIoEventLoopGroup(1, NioIoHandler.newFactory());
        this.environment = new Environment(this);

        this.endpoint = endpoint;

        this.eventBus.register(this);
        this.endpoint.setParent(this);
    }

    final void construct0() {
        this.eventBus.register(this.observerManager);
        this.eventBus.register(this.endpoint);

        this.construct(this.bootstrap);
        this.execute(this.bootstrap);
    }

    protected abstract void construct(B bootstrap);

    protected abstract void execute(B bootstrap);

    public abstract void destroy();

    public void destroy0() {
        this.eventBus.call(new ManagerTerminationEvent());

        destroy();
    }
}