package org.dnttr.zephyr.network.communication.api.server.relay;

import org.dnttr.zephyr.network.communication.core.flow.Relay;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;


public final class AvailableRelayCandidates {

    private final Map<Long, Candidate> candidatesByUUID = new ConcurrentHashMap<>();
    private final Map<String, Candidate> candidatesByName = new ConcurrentHashMap<>();
    private final List<Relay> activeRelays = new CopyOnWriteArrayList<>();

    public @Nullable Candidate getCandidate(long uuid) {
        return this.candidatesByUUID.get(uuid);
    }

    public @Nullable Candidate getCandidate(String name) {
        return this.candidatesByName.get(name);
    }

    public Collection<Candidate> getAllCandidates() {
        return new ArrayList<>(this.candidatesByUUID.values());
    }

    public void addCandidate(long uuid, @NotNull Candidate candidate) {
        this.candidatesByUUID.put(uuid, candidate);
        this.candidatesByName.put(candidate.getName(), candidate);
    }

    public boolean containsCandidate(String name) {
        return this.candidatesByName.containsKey(name);
    }

    public void removeCandidate(long candidate) {
        Candidate toRemove = this.candidatesByUUID.get(candidate);
        if (toRemove != null) {
            this.candidatesByName.remove(toRemove.getName());
        }
        this.candidatesByUUID.remove(candidate);
    }

    public void addRelay(Relay relay) {
        this.activeRelays.add(relay);
    }

    public Relay findRelay(long uuid) {
        return this.activeRelays.stream()
                .filter(relay -> relay.contains(uuid))
                .findFirst()
                .orElse(null);
    }

    public void removeRelay(Relay relay) {
        this.activeRelays.remove(relay);
    }
}