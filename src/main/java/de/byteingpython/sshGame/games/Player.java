package de.byteingpython.sshGame.games;

import de.byteingpython.sshGame.event.InputEventHandler;

public interface Player extends StreamHolder {
    String getName();

    Lobby getLobby();

    void setLobby(Lobby lobby);

    Runnable getEndCallback();

    InputEventHandler getEventHandler();
}
