package org.dnttr.zephyr.network.communication.api.server.relay;

import lombok.RequiredArgsConstructor;

import java.util.HashMap;

/**
 * @author dnttr
 */

@RequiredArgsConstructor
public final class AvailableRelayCandidates {

    private final HashMap<Long, Candidate> candidates;

    public Candidate getCandidate(long candidate) {
        return this.candidates.get(candidate);
    }

    public void addCandidate(long candidate, Candidate context) {
        this.candidates.put(candidate, context);
    }

    public void removeCandidate(long candidate) {
        this.candidates.remove(candidate);
    }
}
