package org.dnttr.zephyr.network.communication.core.security;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import lombok.SneakyThrows;
import org.dnttr.zephyr.toolset.Pair;
import org.jetbrains.annotations.NotNull;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Arrays;
import java.util.Objects;

/**
 * @author dnttr
 */

public class JSecurity {

    @SneakyThrows
    private byte[] instateIntegrity(byte[] key, byte[] message) {
        String crypt = "HmacSHA256";
        Mac hmac = Mac.getInstance(crypt);
        hmac.init(new SecretKeySpec(key, crypt));
        return hmac.doFinal(message);
    }

    private boolean verifyIntegrity(byte[] key, byte[] hmac, byte[] message) {
        byte[] localHMAC = instateIntegrity(key, message);

        return Arrays.equals(localHMAC, hmac);
    }

    public byte[] getMark(byte[] secret, ByteBuf content) {
        return this.instateIntegrity(secret, ByteBufUtil.getBytes(content));
    }

    public boolean isIntegrityPreserved(byte[] secret, @NotNull Pair<@NotNull ByteBuf, @NotNull ByteBuf> buffers) {
        byte[] hash = ByteBufUtil.getBytes(buffers.key());
        byte[] content = ByteBufUtil.getBytes(Objects.requireNonNull(buffers.value()));

        return this.verifyIntegrity(secret, hash, content);
    }

    public boolean isIntegrityProviderAvailable(byte[] secret) {
        return secret != null && secret.length > 0;
    }
}
