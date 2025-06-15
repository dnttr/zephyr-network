package org.dnttr.zephyr.network.communication.core.packet.processor.impl;

import org.dnttr.zephyr.network.bridge.Security;
import org.dnttr.zephyr.network.communication.core.channel.ChannelContext;
import org.dnttr.zephyr.network.communication.core.packet.processor.IProcessor;
import org.dnttr.zephyr.network.protocol.Packet;

import java.util.Optional;

/**
 * @author dnttr
 */

public class SecureProcessor implements IProcessor {

    @Override
    public byte[] processInbound(ChannelContext context, byte[] content) {
        Security.EncryptionMode type = context.getEncryptionType();
        byte[] decryptedContent;

        switch (type) {
            case ASYMMETRIC, SYMMETRIC -> {
                Optional<byte[]> message = Security.decrypt(context.getUuid(), type, content, null);

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
    public byte[] processOutbound(Packet packet, ChannelContext context, byte[] content) {
        Security.EncryptionMode type = context.getEncryptionType();
        byte[] encryptedContent;

        switch (type) {
            case ASYMMETRIC, SYMMETRIC -> {
                Optional<byte[]> message = Security.encrypt(context.getUuid(), type, content, null);
                
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
