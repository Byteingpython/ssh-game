package de.byteingpython.sshGame.games;

import java.util.List;

public interface Game {
    String getName();

    String getDescription();

    String getId();

    int getMaxLobbySize();

    int getMinLobbySize();

    int getMinLobbyCount();

    int getMinTotalPlayers();

    int getMaxTotalPlayers();

    int getMaxLobbyCount();

    void startGame(List<Lobby> lobbies);
}
