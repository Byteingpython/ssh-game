package de.byteingpython.sshGame.matchmaking.openSkill;

import java.util.ArrayList;
import java.util.List;

public class Match<T> {
    private final List<Team<T>> teams;
    private boolean tie =false;
    private boolean scored =false;


    private Match(Team<T>... teams) {
        if(teams == null || teams.length < 2) {
            throw new IllegalArgumentException("Teams must contain at least 2 teams");
        }
        this.teams = new ArrayList<>(List.of(teams));
    }

    public static <T> Match<T> tie(Team<T>... teams) {
        Match<T> match =new Match<>(teams);
        match.tie = true;
        return match;
    }

    public static <T> Match<T> scored(Team<T>... teams) {
        for(Team<T> team : teams) {
            if(team.getScore().isEmpty()){
                throw new IllegalArgumentException("All teams have to have a score");
            }
        }
        Match<T> match = new Match<>(teams);
        match.scored = true;
        return match;
    }

    public boolean isTie() {
        return tie;
    }

    public boolean isScored() {
        return scored;
    }

}
