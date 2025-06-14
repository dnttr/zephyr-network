package org.dnttr.zephyr.network.communication.core.packet.processor.impl;

import org.dnttr.zephyr.network.bridge.ZEKit;
import org.dnttr.zephyr.network.communication.core.channel.ChannelContext;
import org.dnttr.zephyr.network.communication.core.packet.processor.IProcessor;
import org.dnttr.zephyr.network.protocol.Packet;

import static org.dnttr.zephyr.network.bridge.ZEKit.Type.ASYMMETRIC;
import static org.dnttr.zephyr.network.bridge.ZEKit.Type.SYMMETRIC;

/**
 * @author dnttr
 */

public class SecureProcessor implements IProcessor {

    @Override
    public byte[] processInbound(ChannelContext context, byte[] content) {
        if (context.getNonce() == null) {
            return null;
        }

        byte[] decryptedContent;

        switch (context.getEncryptionType()) {
            case ASYMMETRIC -> {
                ZEKit.ffi_ze_set_nonce(context.getUuid(), ASYMMETRIC.getValue(), context.getNonce());
                decryptedContent = ZEKit.ffi_ze_decrypt_asymmetric(context.getUuid(), content);
            }

            case SYMMETRIC -> {
                ZEKit.ffi_ze_set_nonce(context.getUuid(), SYMMETRIC.getValue(), context.getNonce());
                decryptedContent = ZEKit.ffi_ze_decrypt_symmetric(context.getUuid(), content, null);
            }

            default -> throw new IllegalArgumentException("(Inbound) Unsupported encryption type: " + context.getEncryptionType());
        }

        return decryptedContent;
    }

    @Override
    public byte[] processOutbound(Packet message, ChannelContext context, byte[] bytes) {
        byte[] encryptedContent;

        switch (context.getEncryptionType()) {
            case ASYMMETRIC -> {
                ZEKit.ffi_ze_nonce(context.getUuid(), ASYMMETRIC.getValue());
                encryptedContent = ZEKit.ffi_ze_encrypt_asymmetric(context.getUuid(), bytes);
            }

            case SYMMETRIC -> {
                ZEKit.ffi_ze_nonce(context.getUuid(), SYMMETRIC.getValue());
                encryptedContent = ZEKit.ffi_ze_encrypt_symmetric(context.getUuid(), bytes, null);
            }

            default -> throw new IllegalArgumentException("(Outbound) Unsupported encryption type: " + context.getEncryptionType());
        }

        return encryptedContent;
    }
}
