package de.byteingpython.sshGame.games;

import java.util.List;
import java.util.UUID;

public interface Lobby {
    void addPlayer(Player player);

    void removePlayer(Player player);

    List<Player> getPlayers();

    UUID getId();

    Game getGame();

    void setGame(Game game);

    boolean isPlaying();

    void setPlaying(boolean playing);
}
