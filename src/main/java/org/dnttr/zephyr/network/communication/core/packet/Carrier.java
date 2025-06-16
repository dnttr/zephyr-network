package org.dnttr.zephyr.network.communication.core.packet;

import java.util.Arrays;

/**
 * @author dnttr
 */

public record Carrier(int version, int identity, int hashSize, int contentSize, long timestamp, byte[] hash, byte[] content) {

    @Override
    public String toString() {
        return "Carrier{" +
                "version=" + version +
                ", identity=" + identity +
                ", hashSize=" + hashSize +
                ", contentSize=" + contentSize +
                ", timestamp=" + timestamp +
                ", hash=" + Arrays.toString(hash) +
                ", content=" + Arrays.toString(content) +
                '}';
    }
}
