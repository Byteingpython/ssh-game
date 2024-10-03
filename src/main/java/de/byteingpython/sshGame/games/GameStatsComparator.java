package de.byteingpython.sshGame.games;

import java.util.Comparator;

public class GameStatsComparator implements Comparator<GameStats> {
    @Override
    public int compare(GameStats o1, GameStats o2) {
        return o1.time().compareTo(o2.time());
    }
}
