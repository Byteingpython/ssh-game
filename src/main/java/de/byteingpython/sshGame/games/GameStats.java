package de.byteingpython.sshGame.games;

import java.util.Date;

public record GameStats(Date time, Game game, String player, String opponent, GameOutcome outcome) {
}

