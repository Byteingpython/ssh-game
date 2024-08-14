package de.byteingpython.sshGame.screen;


import de.byteingpython.sshGame.event.InputListener;
import de.byteingpython.sshGame.games.*;
import de.byteingpython.sshGame.matchmaking.Matchmaker;
import de.byteingpython.sshGame.lobby.Lobby;
import de.byteingpython.sshGame.lobby.LobbyManager;
import de.byteingpython.sshGame.player.LocalPlayer;
import de.byteingpython.sshGame.player.Player;
import de.byteingpython.sshGame.player.PlayerManager;
import de.byteingpython.sshGame.utils.EscapeCodeUtils;
import de.byteingpython.sshGame.utils.StringUtils;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class LobbyScreen implements Command, InputListener {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private InputStream in;
    private OutputStream out;
    private OutputStream err;
    private ExitCallback callback;
    private final LobbyManager lobbyManager;
    private final GameManager gameManager;
    private final PlayerManager playerManager;

    private final Matchmaker matchmaker;

    private String message = "Welcome to the game";

    private Player player;

    public LobbyScreen(LobbyManager lobbyManager, GameManager gameManager, Matchmaker matchmaker, PlayerManager playerManager) {
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
        this.player = new LocalPlayer(channel.getSession().getUsername(), out, err, in, this::render);
        try {
            playerManager.registerPlayer(player);
        } catch (IllegalArgumentException e) {
            player.getOutputStream().write(("Unable to register Player. " + e.getMessage() + "\n\r").getBytes(StandardCharsets.UTF_8));
            player.getOutputStream().flush();
            callback.onExit(0);
            return;
        }
        Lobby lobby = lobbyManager.createLobby();
        lobby.addPlayer(player);

        List<Game> games = gameManager.getGames();
        if(!games.isEmpty()) {
            Game game = games.get(0);
            lobby.setGame(game);
        }
        try {
            player.getOutputStream().write(EscapeCodeUtils.SWITCH_TO_ALTERNATE_SCREEN.getBytes(StandardCharsets.UTF_8));
            player.getOutputStream().flush();
        } catch (IOException e) {
            logger.error(e.toString());
            channel.close();
            return;
        }
        logger.trace("Starting lobby");
        player.getEventHandler().registerListener(this);
        render();
    }

    /**
     * Shows a message for a certain duration.
     * There can only be one message at a time. If a new message is shown, the old one is overwritten.
     * @param s the message to be shown
     * @param duration the duration in milliseconds
     */
    private void showMessage(String s, long duration) {
        message = s;
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (message.equals(s)) {
                    message = "";
                }
            }
        }, duration);
        render();
    }

    private void render() {
        try {
            player.getOutputStream().write(EscapeCodeUtils.CLEAR_SCREEN.getBytes(StandardCharsets.UTF_8));
            player.getOutputStream().flush();

            String playerNameRow = StringUtils.centerText(player.getName(), 25) + "║\n\r";

            player.getOutputStream().write(("╔════════════════════════════════════════════╗\n\r" +
                    "║ Settings ^s                     ^f Friends ║\n\r" +
                    "║               ╭───╮    ╭───╮               ║\n\r" +
                    "║             ^a│ + │    │ @ │               ║\n\r" +
                    "║               ╰───╯    ╰───╯               ║\n\r" +
                    "║                Add" + playerNameRow+
                    "║"+ StringUtils.centerText(message, 44)+"║\n\r" +
                    "║ ┏╺╺╺╺╺┓                      ┏╺╺╺╺╺╺╺╺╺╺╺┓ ║\n\r" +
                    "║ ╏queue╏^q                  ^m╏"+player.getLobby().getGame().getName() + "╏ ║\n\r" +
                    "║ ┗╺╺╺╺╺┛                      ┗╺╺╺╺╺╺╺╺╺╺╺┛ ║\n\r" +
                    "╚════════════════════════════════════════════╝").getBytes(StandardCharsets.UTF_8));
            player.getOutputStream().flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void destroy(ChannelSession channel) {
        callback.onExit(0, "Goodbye");
    }

    @Override
    public void onInput(int input) {
        if (!player.getLobby().isPlaying()) {
            try {

                logger.info("Received input: " + input);
                if (input == 19) {
                    out.write("-> Settings".getBytes());
                    out.flush();
                }
                if (input == 6) {
                    out.write("-> Friends".getBytes());
                    out.flush();
                }
                if (input == 1) {
                    out.write("-> Add".getBytes());
                    out.flush();
                }
                if (input == 17) {
                    try {
                        matchmaker.matchmake(player.getLobby());
                        if (!player.getLobby().isPlaying()) {
                            showMessage("Matchmaking started", 2999);
                        }
                    } catch (IllegalArgumentException e) {
                        showMessage(e.getMessage(), 2999);
                        throw e;
                    }
                }
                if (input == 12) {
                    SelectScreen<Game> selectScreen = new SelectScreen<>();
                    gameManager.getGames().forEach(game -> selectScreen.addOption(game.getName(), game));
                    selectScreen.selectOption(player).ifPresent(game -> {
                        player.getLobby().setGame(game);
                    });
                }

                if (input == 3) {
                    out.write("\nGoodbye\n".getBytes());
                    out.flush();
                    callback.onExit(-1, "Goodbye");
                }

            } catch (IOException e) {
                logger.error(e.toString());
            }
        }
        if(!player.getLobby().isPlaying()) {
            render();
        }
    }
}