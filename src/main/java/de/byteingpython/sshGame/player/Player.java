package de.byteingpython.sshGame.player;

import de.byteingpython.sshGame.event.InputEventHandler;
import de.byteingpython.sshGame.games.StreamHolder;
import de.byteingpython.sshGame.lobby.Lobby;

public interface Player extends StreamHolder {
    String getName();

    Lobby getLobby();

    void setLobby(Lobby lobby);

    Runnable getEndCallback();

    InputEventHandler getEventHandler();
}
