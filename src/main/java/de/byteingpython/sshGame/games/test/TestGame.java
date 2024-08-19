package de.byteingpython.sshGame.games.test;

import de.byteingpython.sshGame.games.Game;
import de.byteingpython.sshGame.lobby.Lobby;

import java.util.List;

public class TestGame implements Game {
    @Override
    public String getName() {
        return "Test Game";
    }

    @Override
    public String getDescription() {
        return "Just a friggin test";
    }

    @Override
    public String getId() {
        return "test";
    }

    @Override
    public int getMaxLobbySize() {
        return 5;
    }

    @Override
    public int getMinLobbySize() {
        return 1;
    }

    @Override
    public int getMinLobbyCount() {
        return 1;
    }

    @Override
    public int getMinTotalPlayers() {
        return 2;
    }

    @Override
    public int getMaxTotalPlayers() {
        return 20;
    }

    @Override
    public int getMaxLobbyCount() {
        return 20;
    }

    @Override
    public void startGame(List<Lobby> lobbies) {
        for(Lobby lobby:lobbies){
            lobby.getEndCallback().run();
        }
    }
}
