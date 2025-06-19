package org.dnttr.zephyr.network.communication.core.packet;

import org.jetbrains.annotations.NotNull;

/**
 * @author dnttr
 */

public record Carrier(int version, int identity, int hashSize, int contentSize, long timestamp, byte[] hash, byte[] content) {

    @Override
    public @NotNull String toString() {
        return "Carrier{" +
                "version=" + version +
                ", identity=" + identity +
                ", hashSize=" + hashSize +
                ", contentSize=" + contentSize +
                ", timestamp=" + timestamp +
                '}';
    }
}
