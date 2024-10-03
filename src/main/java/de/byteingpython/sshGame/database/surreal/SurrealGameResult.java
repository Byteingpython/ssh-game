package de.byteingpython.sshGame.database.surreal;

import java.util.Date;

public class SurrealGameResult {
    Date time;
    String game;
    String player;
    String opponent;

    public SurrealGameResult(Date time, String game, String player, String opponent) {
        this.time = time;
        this.game = game;
        this.player = player;
        this.opponent = opponent;
    }
}
