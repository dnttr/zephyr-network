package org.dnttr.zephyr.network.api;

import org.dnttr.zephyr.network.bridge.ZEKit;

import java.io.File;

/**
 * @author dnttr
 */

public class Launcher {

    static int x(byte[] lol) {
        return 0;
    }

    public static void main(String[] args) throws InterruptedException {
        File file = new File("/Users/damian/Development/Zephyr/zephyr-network/external/libze.dylib");

        System.load(file.getPath());
        Thread.sleep(1000);
        ZEKit.ffi_ze_close();
    }
}
