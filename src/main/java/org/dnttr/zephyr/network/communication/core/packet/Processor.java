package org.dnttr.zephyr.network.communication.core.packet;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import lombok.Getter;
import org.dnttr.zephyr.network.bridge.ZEKit;
import org.dnttr.zephyr.network.communication.core.channel.ChannelContext;
import org.dnttr.zephyr.network.communication.core.security.JSecurity;
import org.dnttr.zephyr.network.communication.core.utilities.PacketUtils;
import org.dnttr.zephyr.protocol.packet.Packet;
import org.dnttr.zephyr.serializer.Serializer;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;

/**
 * @author dnttr
 */
public class Processor {

    @Getter
    private final IdentityHashMap<Integer, Class<?>> packets;
    private final JSecurity security = new JSecurity();

    public Processor() {
        this.packets = new IdentityHashMap<>();
    }

    @Nullable
    public Object process(Mode mode, ChannelContext context, Object message) throws Exception {
        final boolean isProviderAvailable = this.security.isIntegrityProviderAvailable(context.getSecret());

        switch (mode) {
            case DECRYPT -> {
                Carrier carrier = (Carrier) message;
                ByteBuf content = carrier.buffer();

                if (isProviderAvailable) {
                    var buffer = PacketUtils.decompose(carrier, carrier.hashSize());
                    if (buffer == null) {
                        return null;
                    }

                    if (!this.security.isIntegrityPreserved(context.getSecret(), buffer)) {
                        return null;
                    }

                    content = buffer.value();
                } else if (carrier.hashSize() > 0) {
                    return null;
                }

                if (content == null) {
                    context.restrict();
                    return null;
                }

                byte[] contentBytes = ByteBufUtil.getBytes(content);
                byte[] decrypted;

                //set nonce

                switch (context.getEncryptionType()) {
                    case ZEKit.Type.NONE -> {
                        decrypted = contentBytes;
                        break;
                    }
                    case ASYMMETRIC -> {
                        decrypted = ZEKit.ffi_ze_decrypt_asymmetric(context.getUuid(), contentBytes);
                        break;
                    }
                    case SYMMETRIC -> {
                        decrypted = ZEKit.ffi_ze_decrypt_symmetric(context.getUuid(), contentBytes, new byte[0]);
                        break;
                    }
                    default -> {
                        throw new Exception("Unrecognized encryption type");
                    }
                }

                ByteBuf decryptedContent = Unpooled.wrappedBuffer(decrypted);

                return this.instate(carrier.identity(), decryptedContent);
            }

            case ENCRYPT -> {
                ByteBuf buffer = Unpooled.buffer();
                Packet packet = (Packet) message;

                ByteBuf content = Serializer.serializeToBuffer(packet.getClass(), packet);
                byte[] contentBytes = ByteBufUtil.getBytes(content);

                byte[] encrypted;

                // set nonce

                switch (context.getEncryptionType()) {
                    case ZEKit.Type.NONE -> {
                        encrypted = contentBytes;
                        break;
                    }
                    case ZEKit.Type.ASYMMETRIC -> {
                        encrypted = ZEKit.ffi_ze_encrypt_asymmetric(context.getUuid(), contentBytes);
                        break;
                    }
                    case ZEKit.Type.SYMMETRIC -> {
                        encrypted = ZEKit.ffi_ze_encrypt_symmetric(context.getUuid(), contentBytes, new byte[0]);
                        break;
                    }
                    default -> throw new Exception("Unrecognized encryption type");
                }

                ByteBuf encryptedContent = Unpooled.wrappedBuffer(encrypted);

                int hSize = 0, cSize = encryptedContent.readableBytes();

                if (isProviderAvailable) {
                    byte[] mark = this.security.getMark(context.getSecret(), encryptedContent);
                    buffer.writeBytes(mark);
                    hSize = mark.length;
                }

                buffer.writeBytes(encryptedContent);
                content.release();

                int versionId = packet.getData().version();
                int packetId = packet.getData().identity();

                return new Carrier(versionId, packetId, hSize, cSize, buffer);
            }

            case null, default -> throw new RuntimeException("Unsupported operation mode " + mode);
        }
    }

    @Nullable
    private Packet instate(int identifier, ByteBuf content) throws Exception {
        var klass = this.packets.get(identifier);

        if (klass == null) {
            return null;
        }

        return (Packet) Serializer.deserializeUsingBuffer(klass, content);
    }

    public enum Mode {
        ENCRYPT,
        DECRYPT
    }
}