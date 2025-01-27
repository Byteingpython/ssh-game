package de.byteingpython.sshGame.games;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LocalGameManager implements GameManager {

    private final List<Game> games;

    public LocalGameManager(Game... games) {
        this.games = new ArrayList<>(List.of(games));
    }

    public void add(Game game) {
        games.add(game);
    }

    @Override
    public List<Game> getGames() {
        return games;
    }

    @Override
    public Optional<Game> getGame(String id) {
        for (Game game : games) {
            if (game.getId().equals(id)) {
                return Optional.of(game);
            }
        }
        return Optional.empty();
    }
}
