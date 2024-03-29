package de.byteingpython.sshGame.games;

public interface Player extends StreamHolder {
    String getName();

    Lobby getLobby();

    void setLobby(Lobby lobby);
}
