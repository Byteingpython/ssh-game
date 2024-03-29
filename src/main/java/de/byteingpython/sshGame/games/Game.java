package de.byteingpython.sshGame.games;

import java.util.List;

public interface Game {
    String getName();

    String getDescription();

    String getId();

    int getMaxLobbySize();

    int getMinLobbySize();

    int getMinLobbyCount();

    int getMaxLobbyCount();

    void startGame(List<Lobby> lobbies);

    void setEndCallback(Runnable callback);
}
