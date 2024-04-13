package de.byteingpython.sshGame.games;

import java.util.List;

public interface GameManager {
    public List<Game> getGames();
    public Game getGame(String id);
}
