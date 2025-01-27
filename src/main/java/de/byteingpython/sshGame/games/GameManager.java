package de.byteingpython.sshGame.games;

import java.util.List;
import java.util.Optional;

public interface GameManager {
    List<Game> getGames();

    Optional<Game> getGame(String id);
}
