package de.byteingpython.sshGame.utils;

public class Pair<A, B> {
    public final A first;
    public final B second;

    public Pair(A first, B second) {
        this.first = first;
        this.second = second;
    }

    public B getSecond() {
        return second;
    }

    public A getFirst() {
        return first;
    }
}
