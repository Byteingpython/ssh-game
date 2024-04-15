package de.byteingpython.sshGame.ssh.shell;


import de.byteingpython.sshGame.games.*;
import de.byteingpython.sshGame.games.matchmaking.Matchmaker;
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

public class LobbyScreen implements Command {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private InputStream in;
    private OutputStream out;
    private OutputStream err;
    private ExitCallback callback;
    private final LobbyManager lobbyManager;
    private final GameManager gameManager;

    private final Matchmaker matchmaker;

    private String message = "Welcome to the game";

    private Player player;
    public LobbyScreen(LobbyManager lobbyManager, GameManager gameManager, Matchmaker matchmaker) {
        this.lobbyManager = lobbyManager;
        this.gameManager = gameManager;
        this.matchmaker = matchmaker;
        this.player = player;
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
        this.player = new LocalPlayer(channel.getSession().getUsername(), out, err, in, this::loopUntilGameStarts);
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
        loopUntilGameStarts();
    }

    private void loopUntilGameStarts() {
        while (!player.getLobby().isPlaying()) {
            try {
                render();
                int read = in.read();
                logger.info("Received input: " + read);
                if (read == 19) {
                    out.write("-> Settings".getBytes());
                    out.flush();
                }
                if (read == 6) {
                    out.write("-> Friends".getBytes());
                    out.flush();
                }
                if (read == 1) {
                    out.write("-> Add".getBytes());
                    out.flush();
                }
                if (read == 17) {
                    try {
                        matchmaker.matchmake(player.getLobby());
                    }catch (IllegalArgumentException e) {
                        showMessage(e.getMessage(), 3000);
                    }
                }
                if (read == 13) {
                    SelectScreen<Game> selectScreen = new SelectScreen<>();
                    gameManager.getGames().forEach(game -> selectScreen.addOption(game.getName(), game));
                    selectScreen.selectOption(player).ifPresent(game -> {
                        player.getLobby().setGame(game);
                    });
                }

                if (read == 3) {
                    out.write("\nGoodbye\n".getBytes());
                    out.flush();
                    callback.onExit(0, "Goodbye");
                    break;
                }

            } catch (IOException e) {
                logger.error(e.toString());
                break;
            }
        }
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
    public void destroy(ChannelSession channel) throws Exception {
        callback.onExit(0, "Goodbye");
    }

}