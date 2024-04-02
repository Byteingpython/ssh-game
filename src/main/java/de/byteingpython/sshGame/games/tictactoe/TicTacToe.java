package de.byteingpython.sshGame.games.tictactoe;

import de.byteingpython.sshGame.games.Game;
import de.byteingpython.sshGame.games.Lobby;
import de.byteingpython.sshGame.games.Player;
import de.byteingpython.sshGame.utils.RandomBoolean;

import java.io.IOException;
import java.util.List;

public class TicTacToe implements Game {

    private Runnable endCallback;

    @Override
    public String getName() {
        return "Tic Tac Toe";
    }

    @Override
    public String getDescription() {
        return "A simple game of Tic Tac Toe against players in your lobby.";
    }

    @Override
    public String getId() {
        return "tic_tac_toe";
    }

    @Override
    public int getMaxLobbySize() {
        return 2;
    }

    @Override
    public int getMinLobbySize() {
        return 2;
    }

    @Override
    public int getMinLobbyCount() {
        return 1;
    }

    @Override
    public int getMaxLobbyCount() {
        return 1;
    }

    @Override
    public void startGame(List<Lobby> lobbies) {
        lobbies.get(0).setPlaying(true);
        new Thread(() -> {
            List<Player> players = lobbies.get(0).getPlayers();
            Board board;
            if (RandomBoolean.getRandomBoolean()) {
                board = new Board(players.get(0), players.get(1));
            } else {
                board = new Board(players.get(1), players.get(0));
            }
            while (true) {
                for (Player player : players) {
                    try {
                        player.getOutputStream().write("\033[H\033[2J".getBytes());
                        player.getOutputStream().flush();
                        player.getOutputStream().write(board.render(player).getBytes());
                        player.getOutputStream().flush();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                try {
                    int input = board.getCurrentPlayer().getInputStream().read();
                    if (input == 3) {
                        endCallback.run();
                        return;
                    }
                    if (input < 31 || input > 39) {
                        continue;
                    }
                    board.setField(board.getCurrentPlayer(), input - 30);
                    if (board.checkWin(input - 30)) {
                        for (Player player : players) {
                            player.getOutputStream().write("You won!\n".getBytes());
                            player.getOutputStream().flush();
                        }
                        endCallback.run();
                        return;
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    @Override
    public void setEndCallback(Runnable callback) {
        this.endCallback = callback;
    }
}
