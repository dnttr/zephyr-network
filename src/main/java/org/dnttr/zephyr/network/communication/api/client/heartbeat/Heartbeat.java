package org.dnttr.zephyr.network.communication.api.client.heartbeat;

import org.dnttr.zephyr.network.communication.core.channel.ChannelContext;
import org.dnttr.zephyr.network.protocol.packets.internal.ConnectionKeepAlivePacket;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author dnttr
 */

public class Heartbeat {

    private final ScheduledExecutorService keepAliveExecutor = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> keepAliveTask;

    public void startHeartbeat(final ChannelContext context) {
        if (this.keepAliveTask != null && !this.keepAliveTask.isDone()) {
            this.keepAliveTask.cancel(false);
        }

        var beat = this.getBeat(context);

        this.keepAliveTask = this.keepAliveExecutor.scheduleAtFixedRate(beat, 20, 20, TimeUnit.SECONDS);
    }

    public void stopHeartbeat() {
        if (this.keepAliveTask != null && !this.keepAliveTask.isDone()) {
            this.keepAliveTask.cancel(true);
        }
    }

    private Runnable getBeat(ChannelContext context) {
        return () -> {
            if (context.getChannel().isActive()) {
                context.getChannel().writeAndFlush(new ConnectionKeepAlivePacket());
            } else {
                stopHeartbeat();
            }
        };
    }
}
