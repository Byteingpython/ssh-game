package de.byteingpython.sshGame.lobby;

import de.byteingpython.sshGame.games.Game;
import de.byteingpython.sshGame.player.Player;

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

    Runnable getEndCallback();
}
