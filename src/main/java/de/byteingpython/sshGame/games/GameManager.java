package de.byteingpython.sshGame.games;

import java.util.List;
import java.util.Optional;

public interface GameManager {
    public List<Game> getGames();
    public Optional<Game> getGame(String id);
}
