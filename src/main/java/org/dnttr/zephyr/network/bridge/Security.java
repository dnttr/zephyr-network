package org.dnttr.zephyr.network.bridge;

import org.dnttr.zephyr.network.bridge.internal.ZEKit;

/**
 * @author dnttr
 */

public final class Security {

    public static long createSession() {
        long uuid = ZEKit.ffi_ze_create_session();

        if (uuid <= 0) {
            throw new IllegalStateException("Create session failed, invalid uuid has been returned");
        }

        return uuid;
    }

    public static boolean deleteSession(long uuid) {
        if (uuid <= 0) {
            throw new IllegalArgumentException("Delete session failed due to illegal uuid provided");
        }

        int code = ZEKit.ffi_ze_delete_session(uuid);

        return code == 0;
    }
}
