package de.byteingpython.sshGame.games.tictactoe;

import de.byteingpython.sshGame.games.Game;
import de.byteingpython.sshGame.games.Lobby;
import de.byteingpython.sshGame.games.Player;
import de.byteingpython.sshGame.utils.RandomBoolean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TicTacToe implements Game {
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

    private static void endGame(List<Lobby> lobbies) {
        for (Lobby lobby : lobbies) {
            lobby.setPlaying(false);
        }
        for (Lobby lobby : lobbies) {
            new Thread(() -> {
                lobby.getEndCallback().run();
            }).start();
        }
    }

    @Override
    public int getMinLobbyCount() {
        return 1;
    }

    @Override
    public int getMinLobbySize() {
        return 1;
    }

    @Override
    public int getMinTotalPlayers() {
        return 2;
    }

    @Override
    public int getMaxTotalPlayers() {
        return 2;
    }

    @Override
    public int getMaxLobbyCount() {
        return 2;
    }

    @Override
    public void startGame(List<Lobby> lobbies) {
        List<Player> players = new ArrayList<>();
        for (Lobby lobby : lobbies) {
            players.addAll(lobby.getPlayers());
            lobby.setPlaying(true);
        }
        new Thread(() -> {
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
                        endGame(lobbies);
                        return;
                    }
                    if (input < 49 || input > 57) {
                        continue;
                    }
                    board.setField(board.getCurrentPlayer(), input - 49);
                    if (board.checkWin(input - 49)) {
                        endGame(lobbies);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

}
