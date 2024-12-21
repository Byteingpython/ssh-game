package de.byteingpython.sshGame.player;

import de.byteingpython.sshGame.event.EventHandler;
import de.byteingpython.sshGame.event.InputEventHandler;
import de.byteingpython.sshGame.games.StreamHolder;
import de.byteingpython.sshGame.lobby.Lobby;
import de.byteingpython.sshGame.ssh.shell.WindowSize;

import java.util.Locale;
import java.util.ResourceBundle;

public interface Player extends StreamHolder {
    String getName();

    Lobby getLobby();

    void setLobby(Lobby lobby);

    Runnable getEndCallback();

    InputEventHandler getInputEventHandler();

    EventHandler getEventHandler();

    WindowSize getWindowSize();

    ResourceBundle getLocale();

    void setLocale(Locale locale);
}
