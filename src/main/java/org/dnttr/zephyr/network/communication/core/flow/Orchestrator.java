package org.dnttr.zephyr.network.communication.core.flow;

import lombok.AccessLevel;
import lombok.Getter;
import org.dnttr.zephyr.event.EventBus;
import org.dnttr.zephyr.bridge.Security;
import org.dnttr.zephyr.network.communication.core.channel.ChannelContext;
import org.dnttr.zephyr.network.communication.core.managers.ObserverManager;
import org.jetbrains.annotations.NotNull;

/**
 * @author dnttr
 */

public abstract class Orchestrator {

    @Getter(AccessLevel.PROTECTED)
    private final ObserverManager observerManager;

    @Getter(AccessLevel.PROTECTED)
    private final EventBus bus;

    public Orchestrator(final @NotNull EventBus bus, final @NotNull ObserverManager manager) {
        this.bus = bus;
        this.observerManager = manager;
    }

    protected byte[] getPublicKeyForAuth(@NotNull final ChannelContext context) {
        Security.generateKeys(context.getUuid(), Security.EncryptionMode.ASYMMETRIC);
        var authPubKey = Security.getKeyPair(context.getUuid(), Security.KeyType.PUBLIC);

        if (authPubKey.isEmpty()) {
            context.restrict("Unable to get public key for auth.");

            return null;
        }

        return authPubKey.get();
    }
}
