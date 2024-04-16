package de.byteingpython.sshGame.games;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LocalGameMananger implements GameManager{

    private List<Game> games = new ArrayList<>();

    public LocalGameMananger(Game... games) {
        this.games = List.of(games);
    }

    @Override
    public List<Game> getGames() {
        return games;
    }

    @Override
    public Optional<Game> getGame(String id) {
        for(Game game:games){
            if(game.getId().equals(id)){
                return Optional.of(game);
            }
        }
        return Optional.empty();
    }
}
