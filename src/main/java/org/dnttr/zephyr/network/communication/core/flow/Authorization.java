package org.dnttr.zephyr.network.communication.core.flow;

import lombok.AccessLevel;
import lombok.Getter;
import org.dnttr.zephyr.event.EventBus;
import org.dnttr.zephyr.network.bridge.Security;
import org.dnttr.zephyr.network.communication.core.channel.ChannelContext;
import org.dnttr.zephyr.network.communication.core.managers.ObserverManager;

/**
 * @author dnttr
 */

public abstract class Authorization {

    @Getter(AccessLevel.PROTECTED)
    private final ObserverManager observerManager;

    @Getter(AccessLevel.PROTECTED)
    private final EventBus bus;

    public Authorization(final EventBus bus, final ObserverManager manager) {
        this.bus = bus;
        this.observerManager = manager;
    }

    protected byte[] getPublicKeyForAuth(final ChannelContext context) {
        Security.generateKeys(context.getUuid(), Security.EncryptionMode.ASYMMETRIC);
        var authPubKey = Security.getKeyPair(context.getUuid(), Security.KeyType.PUBLIC);

        if (authPubKey.isEmpty()) {
            context.restrict("Unable to get public key for auth.");

            return null;
        }

        return authPubKey.get();
    }
}
