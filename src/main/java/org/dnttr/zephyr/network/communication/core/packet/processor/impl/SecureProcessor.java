package org.dnttr.zephyr.network.communication.core.packet.processor.impl;

import org.dnttr.zephyr.network.bridge.internal.ZEKit;
import org.dnttr.zephyr.network.communication.core.channel.ChannelContext;
import org.dnttr.zephyr.network.communication.core.packet.processor.IProcessor;
import org.dnttr.zephyr.network.protocol.Packet;

/**
 * @author dnttr
 */

public class SecureProcessor implements IProcessor {

    @Override
    public byte[] processInbound(ChannelContext context, byte[] content) {
        byte[] decryptedContent;

        switch (context.getEncryptionType()) {
            case ASYMMETRIC -> decryptedContent = ZEKit.ffi_ze_decrypt_asymmetric(context.getUuid(), content);
            case SYMMETRIC -> decryptedContent = ZEKit.ffi_ze_decrypt_symmetric(context.getUuid(), content, null);
            default -> throw new IllegalArgumentException("(Inbound) Unsupported encryption type: " + context.getEncryptionType());
        }

        return decryptedContent;
    }

    @Override
    public byte[] processOutbound(Packet message, ChannelContext context, byte[] bytes) {
        byte[] encryptedContent;

        switch (context.getEncryptionType()) {
            case ASYMMETRIC -> encryptedContent = ZEKit.ffi_ze_encrypt_asymmetric(context.getUuid(), bytes);
            case SYMMETRIC -> encryptedContent = ZEKit.ffi_ze_encrypt_symmetric(context.getUuid(), bytes, null);
            default -> throw new IllegalArgumentException("(Outbound) Unsupported encryption type: " + context.getEncryptionType());
        }

        return encryptedContent;
    }
}
