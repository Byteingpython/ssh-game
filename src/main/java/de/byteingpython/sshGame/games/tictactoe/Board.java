package de.byteingpython.sshGame.games.tictactoe;

import de.byteingpython.sshGame.games.Player;

import java.util.HashMap;
import java.util.Map;

public class Board {
    private final int[] board = new int[9];
    private final Map<Player, Sign> players = new HashMap<>();
    private Player currentPlayer;
    private Player otherPlayer;
    public Board(Player player1, Player player2) {
        players.put(player1, Sign.X);
        players.put(player2, Sign.O);
        currentPlayer = player1;
        otherPlayer = player2;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
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

    public boolean isSet(int field) {
        return board[field] != 0;
    }


    public String render(Player player) {
        StringBuilder sb = new StringBuilder();
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
                if (i < 2) {
                    sb.append("│");
                } else {
                    sb.append("\n\r");
                }
            }
            if (i < 2) {
                sb.append("─────┼─────┼─────\n\r");
            }
        }
        return sb.toString();
    }
}
