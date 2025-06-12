package org.dnttr.zephyr.network.loader.core;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Environment {

    private final Worker worker;

    public void execute() {
        try {
            this.worker.construct0();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            this.worker.destroy();
        }
    }
}