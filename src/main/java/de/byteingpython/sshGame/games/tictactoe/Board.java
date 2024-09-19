package de.byteingpython.sshGame.games.tictactoe;

import de.byteingpython.sshGame.event.InputListener;
import de.byteingpython.sshGame.lobby.Lobby;
import de.byteingpython.sshGame.player.Player;
import de.byteingpython.sshGame.utils.EscapeCodeUtils;
import de.byteingpython.sshGame.utils.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Board implements InputListener {
    private final int[] board = new int[9];
    private final Map<Player, Sign> players = new HashMap<>();
    private Player currentPlayer;
    private Player otherPlayer;
    public Board(Player player1, Player player2) {
        players.put(player1, Sign.X);
        players.put(player2, Sign.O);
        currentPlayer = player1;
        otherPlayer = player2;
        currentPlayer.getInputEventHandler().registerListener(this);
        renderAll();
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public Player getOtherPlayer() {
        return otherPlayer;
    }

    public void setField(Player player, int field) {
        if (field < 0 || field > 8) {
            throw new IllegalArgumentException("Field must be between 0 and 8");
        }
        if (isSet(field)) {
            throw new IllegalArgumentException("Field is already set");
        }
        if (player != currentPlayer) {
            throw new IllegalArgumentException("It's not your turn");
        }
        board[field] = players.get(player).getIntRepresentation();
        currentPlayer = otherPlayer;
        otherPlayer = player;
    }


    public boolean checkWin(int sourceField) {
        int sourceValue = board[sourceField];
        if (sourceValue == 0) {
            return false;
        }
        int row = sourceField / 3;
        int col = sourceField % 3;
        if (board[row * 3] == sourceValue && board[row * 3 + 1] == sourceValue && board[row * 3 + 2] == sourceValue) {
            return true;
        }
        if (board[col] == sourceValue && board[col + 3] == sourceValue && board[col + 6] == sourceValue) {
            return true;
        }
        if (row == col && board[0] == sourceValue && board[4] == sourceValue && board[8] == sourceValue) {
            return true;
        }
        return row + col == 2 && board[2] == sourceValue && board[4] == sourceValue && board[6] == sourceValue;
    }

    private boolean isDraw() {
        for(int value:board){
            if(value == 0) {
                return false;
            }
        }
        return true;
    }

    public boolean isSet(int field) {
        return board[field] != 0;
    }


    public void render(Player player) {
        StringBuilder sb = new StringBuilder();
        if(currentPlayer==player) {
            sb.append(StringUtils.centerText("Your Turn", 17));
        } else {
            sb.append(StringUtils.centerText(otherPlayer.getName()+"'s Turn", 17));
        }
        sb.append("\n\r");
        for (int i = 0; i < 3; i++) {
            sb.append("     │     │     \n\r");
            for (int j = 0; j < 3; j++) {
                sb.append("  ");
                if (board[i * 3 + j] == 0) {
                    if (player == currentPlayer) {
                        sb.append(i * 3 + j + 1);
                    } else {
                        sb.append(" ");
                    }
                } else if (board[i * 3 + j] == 1) {
                    sb.append("X");
                } else {
                    sb.append("O");
                }
                sb.append("  ");
                if (j < 2) {
                    sb.append("│");
                } else {
                    sb.append("\n\r");
                }
            }
            if (i < 2) {
                sb.append("─────┼─────┼─────\n\r");
            }
        }
        try {
            player.getOutputStream().write(EscapeCodeUtils.CLEAR_SCREEN.getBytes());
            player.getOutputStream().write(sb.toString().getBytes(StandardCharsets.UTF_8));
            player.getOutputStream().flush();
        } catch (IOException ignored) {}
    }

    private void renderAll() {
        render(currentPlayer);
        render(otherPlayer);
    }

    @Override
    public void onInput(int input) {
        if (input == 3) {
            endGame();
            return;
        }
        if (input < 49 || input > 57) {
            return;
        }
        if (this.isSet(input - 49)) {
            return;
        }
        this.setField(this.getCurrentPlayer(), input - 49);

        if (this.checkWin(input - 49)||this.isDraw()) {
            getCurrentPlayer().getInputEventHandler().unregisterListener(this);
            if (isDraw()) {
                try {
                    getCurrentPlayer().getOutputStream().write(StringUtils.centerText("Its a tie", 17).getBytes(StandardCharsets.UTF_8));
                    getCurrentPlayer().getOutputStream().flush();
                    getOtherPlayer().getOutputStream().write(StringUtils.centerText("Its a tie", 17).getBytes(StandardCharsets.UTF_8));
                    getOtherPlayer().getOutputStream().flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                try {
                    getCurrentPlayer().getOutputStream().write(StringUtils.centerText("You won!", 17).getBytes(StandardCharsets.UTF_8));
                    getCurrentPlayer().getOutputStream().flush();
                    getOtherPlayer().getOutputStream().write(StringUtils.centerText("You lost!", 17).getBytes(StandardCharsets.UTF_8));
                    getOtherPlayer().getOutputStream().flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            new Thread(() -> {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                endGame();
            }).start();
            return;
        }
        this.getCurrentPlayer().getInputEventHandler().registerListener(this);
        this.getOtherPlayer().getInputEventHandler().unregisterListener(this);
        renderAll();
    }

    private void endGame() {
        Lobby lobby = getCurrentPlayer().getLobby();
        lobby.getEndCallback().run();
        if (getOtherPlayer().getLobby() != lobby) {
            getOtherPlayer().getLobby().getEndCallback().run();
        }
    }
}
