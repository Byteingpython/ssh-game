package de.byteingpython.sshGame.player;

import de.byteingpython.sshGame.event.EventHandler;
import de.byteingpython.sshGame.event.InputEventHandler;
import de.byteingpython.sshGame.event.StreamReaderInputHandler;
import de.byteingpython.sshGame.lobby.Lobby;
import de.byteingpython.sshGame.ssh.shell.WindowChangeEvent;
import de.byteingpython.sshGame.ssh.shell.WindowSize;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.Signal;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

public class LocalPlayer implements Player {
    private final String name;
    private final OutputStream outputStream;
    private final OutputStream errorStream;
    private final InputStream inputStream;
    private final Runnable endCallback;
    private final InputEventHandler inputEventHandler;
    private final EventHandler eventHandler;
    private final Environment environment;
    private Lobby lobby;

    public LocalPlayer(String name, OutputStream outputStream, OutputStream errorStream, InputStream inputStream, Runnable endCallback, Environment environment) {
        this.name = name;
        this.outputStream = outputStream;
        this.errorStream = errorStream;
        this.inputStream = inputStream;
        this.endCallback = endCallback;
        this.environment = environment;
        this.eventHandler = new EventHandler();
        this.inputEventHandler = new StreamReaderInputHandler(this);
        // Handle terminal size changes
        environment.addSignalListener((channel, signal) -> {
            if(signal!= Signal.WINCH) return;
            Map<String, String> env = environment.getEnv();
            eventHandler.handle(new WindowChangeEvent(getWindowSize()));
        });
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
    public InputEventHandler getInputEventHandler() {
        return inputEventHandler;
    }

    @Override
    public EventHandler getEventHandler() {
        return eventHandler;
    }

    @Override
    public WindowSize getWindowSize() {
        Map<String, String> env = environment.getEnv();
        int width = Integer.parseInt(env.get(Environment.ENV_COLUMNS));
        int height = Integer.parseInt(env.get(Environment.ENV_LINES));
        return new WindowSize(width, height);
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
