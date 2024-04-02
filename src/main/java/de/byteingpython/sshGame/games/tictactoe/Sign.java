package de.byteingpython.sshGame.games.tictactoe;

public enum Sign {
    X, O;

    public int getIntRepresentation() {
        return this == Sign.X ? 1 : 2;
    }
}
