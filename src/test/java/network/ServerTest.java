package network;

import org.dnttr.zephyr.network.loader.api.server.Server;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.InetSocketAddress;

public class ServerTest {

    @BeforeAll
    public static void setUp() {
        String path = System.getenv("EXTERNAL_LIBS");

        if (path == null || path.isEmpty()) {
            System.err.println("Environment variable EXTERNAL_LIBS is not set or empty.");
            System.exit(1337);
        }

        File file = new File(path + "libze.dylib");
        System.load(file.getPath());
    }

    @Test
    public void init() {
        new Server(new InetSocketAddress(2137));
    }
}