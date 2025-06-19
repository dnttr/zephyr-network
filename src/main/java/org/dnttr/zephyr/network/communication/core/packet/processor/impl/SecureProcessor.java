package org.dnttr.zephyr.network.communication.core.packet.processor.impl;

import org.dnttr.zephyr.network.bridge.Security;
import org.dnttr.zephyr.network.communication.core.channel.ChannelContext;
import org.dnttr.zephyr.network.communication.core.packet.processor.IProcessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * @author dnttr
 */

public final class SecureProcessor implements IProcessor {

    @Override
    public byte @Nullable [] processInbound(@NotNull ChannelContext context, byte @NotNull [] content) {
        Objects.requireNonNull(context);
        Objects.requireNonNull(content);

        var type = context.getEncryptionType();
        byte[] decryptedContent;

        switch (type) {
            case ASYMMETRIC, SYMMETRIC -> {
                var message = Security.decrypt(context.getUuid(), type, content, null);

                if (message.isEmpty()) {
                    throw new SecurityException("Cannot decrypt message");
                }

                decryptedContent = message.get();
            }

            default -> throw new IllegalArgumentException("(Inbound) Unsupported encryption type: " + type);
        }

        return decryptedContent;
    }

    @Override
    public byte @Nullable [] processOutbound(@NotNull ChannelContext context, byte @NotNull [] content) {
        Objects.requireNonNull(context);
        Objects.requireNonNull(content);

        var type = context.getEncryptionType();
        byte[] encryptedContent;

        switch (type) {
            case ASYMMETRIC, SYMMETRIC -> {
                var message = Security.encrypt(context.getUuid(), type, content, null);
                
                if (message.isEmpty()) {
                    throw new SecurityException("Cannot encrypt message");
                }

                encryptedContent = message.get();
            }

            default -> throw new IllegalArgumentException("(Outbound) Unsupported encryption type: " + type);
        }

        return encryptedContent;
    }
}
