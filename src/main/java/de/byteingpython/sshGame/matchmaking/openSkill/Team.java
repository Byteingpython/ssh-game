package de.byteingpython.sshGame.matchmaking.openSkill;

import java.util.*;

public class Team<T> {
    private final Optional<Float> score;
    private final List<Weighted<T>> weightedPlayers = new ArrayList<>();

    public Team(T... players){
        this.score = Optional.empty();
        for (T player : players) {
            this.weightedPlayers.add(new Weighted<>(player, 1f));
        }
    }

    public Team(float score, T... players) {
        this.score = Optional.of(score);
        for (T player : players) {
            this.weightedPlayers.add(new Weighted<>(player, 1f));
        }
    }

    public Team(Weighted<T>... players) {
        this.weightedPlayers.addAll(Arrays.asList(players));
        this.score = Optional.empty();
    }

    public Team(float score, Weighted<T>... players) {
        this.score = Optional.of(score);
        float max=0;
        float min=Float.MAX_VALUE;
        for(Weighted<T> player : players) {
            if(player.weight() > max) max = player.weight();
            if(player.weight() < min) min = player.weight();
        }
        float source_range = max - min;
        if(source_range ==0) source_range = 1f;
        for(Weighted<T> player : players) {
            // Normalize to a weight between 1 and 2
            weightedPlayers.add(new Weighted<>(player.value(), ((player.weight()-min)/source_range)*1+1));
        }
    }


    public List<Weighted<T>> getPlayers() {
        return weightedPlayers;
    }

    public Optional<Float> getScore() {
        return score;
    }
}
