package de.byteingpython.sshGame.games;

import de.byteingpython.sshGame.matchmaking.openSkill.RatingAlgo;
import de.byteingpython.sshGame.player.Player;

import java.util.List;

public interface StatisticsManager<T> {
    void registerWin(Player winner, Player loser, Game game);

    void registerDraw(Player player1, Player player2, Game game);

    List<GameStats> getGameStats(Player player);

    List<GameStats> getGameStats(Player player, Game game);

    RatingAlgo<T> getRatingAlgo();

    T getRating(Player player, Game game);
}
