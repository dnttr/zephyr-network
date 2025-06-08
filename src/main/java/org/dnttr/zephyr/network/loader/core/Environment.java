package org.dnttr.zephyr.network.loader.core;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Environment {

    private final Worker worker;

    public void execute() {
        try {
            this.worker.construct0();
        } catch (Exception ex) {
            //add more robust error handling here, otherwise the IDE won't stop complaining
            ex.printStackTrace();
        } finally {
            this.worker.destroy();
        }
    }
}