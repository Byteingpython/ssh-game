package de.byteingpython.sshGame.games;

import java.io.InputStream;
import java.io.OutputStream;

public class LocalPlayer implements Player {
    private final String name;
    private final OutputStream outputStream;
    private final OutputStream errorStream;
    private final InputStream inputStream;
    private final Runnable endCallback;
    private Lobby lobby;

    public LocalPlayer(String name, OutputStream outputStream, OutputStream errorStream, InputStream inputStream, Runnable endCallback) {
        this.name = name;
        this.outputStream = outputStream;
        this.errorStream = errorStream;
        this.inputStream = inputStream;
        this.endCallback = endCallback;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Lobby getLobby() {
        return lobby;
    }

    @Override
    public void setLobby(Lobby lobby) {
        this.lobby = lobby;
    }

    @Override
    public Runnable getEndCallback() {
        return endCallback;
    }

    @Override
    public OutputStream getOutputStream() {
        return outputStream;
    }

    @Override
    public OutputStream getErrorStream() {
        return errorStream;
    }

    @Override
    public InputStream getInputStream() {
        return inputStream;
    }
}
