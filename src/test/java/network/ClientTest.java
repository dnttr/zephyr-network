package network;

import org.dnttr.zephyr.event.EventBus;
import org.dnttr.zephyr.network.communication.core.managers.ObserverManager;
import org.dnttr.zephyr.network.loader.api.client.Client;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.io.File;
import java.net.InetSocketAddress;

@EnabledIfEnvironmentVariable(named = "EXTERNAL_LIBS", matches = ".+")
public class ClientTest {

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
        new Client(new InetSocketAddress(12345), new EventBus(), new ObserverManager());
    }
}