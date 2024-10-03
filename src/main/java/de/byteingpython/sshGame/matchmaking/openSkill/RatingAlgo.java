package de.byteingpython.sshGame.matchmaking.openSkill;

import de.byteingpython.sshGame.games.GameOutcome;
import de.byteingpython.sshGame.utils.Pair;

import java.util.List;

public interface RatingAlgo<T> {
    T generateRating();
    List<Team<T>> rate(Team<T>... teams);
    Pair<T, T> rate(GameOutcome outcome, T t1, T t2);
    List<Team<T>> rateTie(Team<T>... teams);
    float predict_win(Team<T>... teams);
    float predict_draw(Team<T>... teams);
    float predict_rank(Team<T>... teams);
    float predict(T t1, T t2);
}


