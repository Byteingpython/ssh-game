package de.byteingpython.sshGame.ssh.shell;

import de.byteingpython.sshGame.config.ConfigurationProvider;
import de.byteingpython.sshGame.games.GameManager;
import de.byteingpython.sshGame.lobby.LobbyManager;
import de.byteingpython.sshGame.player.PlayerManager;
import de.byteingpython.sshGame.matchmaking.Matchmaker;
import de.byteingpython.sshGame.screen.LobbyScreen;
import org.apache.sshd.common.io.IoInputStream;
import org.apache.sshd.common.io.IoOutputStream;
import org.apache.sshd.common.util.buffer.ByteArrayBuffer;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ShellCommand implements Command {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ConfigurationProvider configurationProvider;
    private InputStream in;
    private OutputStream out;
    private OutputStream err;
    private IoOutputStream ioOut;
    private IoOutputStream ioErr;
    private IoInputStream ioIn;
    private ExitCallback callback;
    private final LobbyManager lobbyManager;
    private final GameManager gameManager;
    private final Matchmaker matchmaker;
    private final PlayerManager playerManager;

    public ShellCommand(ConfigurationProvider configurationProvider, LobbyManager lobbyManager, PlayerManager playerManager, GameManager gameManager, Matchmaker matchmaker) {
        this.configurationProvider = configurationProvider;
        this.lobbyManager = lobbyManager;
        this.gameManager = gameManager;
        this.matchmaker = matchmaker;
        this.playerManager = playerManager;
    }

    @Override
    public void setExitCallback(ExitCallback callback) {
        this.callback = callback;
    }

    @Override
    public void setErrorStream(OutputStream err) {
        this.err = err;
    }

    @Override
    public void setInputStream(InputStream in) {
        this.in = in;
    }

    @Override
    public void setOutputStream(OutputStream out) {
        logger.info("Setting output stream");
        this.out = out;
    }

    @Override
    public void start(ChannelSession channel, Environment env) throws IOException {
        logger.info("Starting shell");
        if (ioOut != null) {
            logger.info("Writing to iooutput stream");
            ioOut.writeBuffer(new ByteArrayBuffer("Welcome to the game\n".getBytes()));
        }
        if (out != null) {
            logger.info("Writing to output stream");
            out.write("Welcome to the game\n".getBytes());
            out.flush();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    out.write("Please enter sth: ".getBytes());
                    out.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                    try {
                        out.write("\033[H\033[2J".getBytes());
                        out.flush();
                        LobbyScreen lobby = new LobbyScreen(lobbyManager, gameManager, matchmaker, playerManager);
                        lobby.setInputStream(in);
                        lobby.setOutputStream(out);
                        lobby.setExitCallback(callback);
                        lobby.setErrorStream(err);
                        lobby.start(channel, env);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
            }

        }).start();
    }

    @Override
    public void destroy(ChannelSession channel) {
        callback.onExit(0, "Goodbye");
    }
/*
    @Override
    public void setIoErrorStream(IoOutputStream err) {
        this.ioErr = err;
    }

    @Override
    public void setIoInputStream(IoInputStream in) {
        this.ioIn = in;
    }

    @Override
    public void setIoOutputStream(IoOutputStream out) {
        this.ioOut = out;
    }
 */
}
