package de.byteingpython.sshGame.database.surreal;

import com.surrealdb.driver.SyncSurrealDriver;
import com.surrealdb.driver.model.QueryResult;
import de.byteingpython.sshGame.games.*;
import de.byteingpython.sshGame.matchmaking.openSkill.RatingAlgo;
import de.byteingpython.sshGame.matchmaking.openSkill.WengLing;
import de.byteingpython.sshGame.matchmaking.openSkill.WengLingRating;
import de.byteingpython.sshGame.player.Player;
import de.byteingpython.sshGame.utils.Pair;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SurrealStatisticsManager implements StatisticsManager<WengLingRating> {
    private final SyncSurrealDriver driver;
    private final GameManager gameManager;

    public SurrealStatisticsManager(SyncSurrealDriver driver, GameManager gameManager) {
        this.driver = driver;
        this.gameManager = gameManager;
        try {
            String sql = """
                    DEFINE TABLE win TYPE RELATION IN user OUT user;
                    DEFINE TABLE draw TYPE RELATION IN user OUT user;
                    DEFINE FIELD time ON TABLE win TYPE datetime DEFAULT time::now();
                    DEFINE FIELD game ON TABLE win TYPE string;
                    DEFINE FIELD time ON TABLE draw TYPE datetime DEFAULT time::now();
                    DEFINE FIELD game ON TABLE draw TYPE string;""";
            driver.query(sql, Map.of(), Object.class);
        } catch (Exception ignored) {
            LoggerFactory.getLogger(this.getClass()).info("Unable to set Statistics scheme");
        }
    }


    @Override
    public void registerWin(Player winner, Player loser, Game game) {
        String sql = "RELATE (SELECT VALUE id FROM user WHERE name=$winnerName)->win->(SELECT VALUE id FROM user WHERE name=$loserName) SET game=$game;";
        this.driver.query(sql, Map.of("winnerName", winner.getName(), "loserName", loser.getName(), "game", game.getId()), Object.class);
        WengLingRating winnerRating = getRating(winner, game);
        WengLingRating loserRating = getRating(loser, game);
        WengLing wengLing = (WengLing) getRatingAlgo();
        Pair<WengLingRating, WengLingRating> newRatings = wengLing.rate(GameOutcome.WIN, winnerRating, loserRating);
        updateRating(winner, newRatings.first, game);
        updateRating(loser, newRatings.second, game);
    }

    @Override
    public void registerDraw(Player player1, Player player2, Game game) {
        String sql = "RELATE (SELECT VALUE id FROM user WHERE name=$player1)->draw->(SELECT VALUE id FROM user WHERE name=$player2) SET game=$game;";
        this.driver.query(sql, Map.of("player1", player1.getName(), "player2", player2.getName(), "game", game.getId()), Object.class);
        WengLingRating player1Rating = getRating(player1, game);
        WengLingRating player2Rating = getRating(player2, game);
        WengLing wengLing = (WengLing) getRatingAlgo();
        Pair<WengLingRating, WengLingRating> newRatings = wengLing.rate(GameOutcome.DRAW, player1Rating, player2Rating);
        updateRating(player1, newRatings.first, game);
        updateRating(player2, newRatings.second, game);
    }

    @Override
    public List<GameStats> getGameStats(Player player) {
        String sql = """
                SELECT in.name AS player, out.name AS opponent, game, time FROM win WHERE in.name=$player;
                SELECT out.name AS player, in.name AS opponent, game, time FROM win WHERE out.name=$player;
                SELECT in.name AS player, out.name AS opponent, game, time FROM draw WHERE in.name=$player;
                SELECT out.name AS player, in.name AS opponent, game, time FROM draw WHERE out.name=$player;""";
        List<QueryResult<SurrealGameResult>> results = this.driver.query(sql, Map.of("player", player.getName()), SurrealGameResult.class);
        return convertResultToStats(results);
    }

    @Override
    public List<GameStats> getGameStats(Player player, Game game) {
        String sql = """
                SELECT in.name AS player, out.name AS opponent, game, time FROM win WHERE in.name=$player && game=$game;
                SELECT out.name AS player, in.name AS opponent, game, time FROM win WHERE out.name=$player && game=$game;
                SELECT in.name AS player, out.name AS opponent, game, time FROM draw WHERE in.name=$player && game=$game;
                SELECT out.name AS player, in.name AS opponent, game, time FROM draw WHERE out.name=$player && game=$game;""";
        List<QueryResult<SurrealGameResult>> results = this.driver.query(sql, Map.of("player", player.getName(), "game", game.getId()), SurrealGameResult.class);
        return convertResultToStats(results);
    }

    @Override
    public RatingAlgo<WengLingRating> getRatingAlgo() {
        return new WengLing(25f / 3, 0.000001f);
    }

    @Override
    public WengLingRating getRating(Player player, Game game) {
        List<QueryResult<WengLingRating>> results = this.driver.query("SELECT rating, uncertainty FROM rating WHERE game=$game && user.name=$player", Map.of("game", game.getId(), "player", player.getName()), WengLingRating.class);
        if (results.get(0).getResult().isEmpty()) {
            createRating(player, game);
            return getRatingAlgo().generateRating();
        }
        return results.get(0).getResult().get(0);
    }

    public void updateRating(Player player, WengLingRating rating, Game game) {
        this.driver.query("UPDATE rating SET rating = <float> $rating, uncertainty = <float> $uncertainty WHERE user.name=$player && game=$game", Map.of("rating", String.valueOf(rating.rating()), "uncertainty", String.valueOf(rating.uncertainty()), "game", game.getId(), "player", player.getName()), Object.class);
    }

    private List<GameStats> convertToGameStats(QueryResult<SurrealGameResult> result, GameOutcome gameOutcome) {
        List<GameStats> stats = new ArrayList<>();
        for (SurrealGameResult win : result.getResult()) {
            Optional<Game> game = gameManager.getGame(win.game);
            if (game.isEmpty()) continue;
            stats.add(new GameStats(win.time, game.get(), win.player, win.opponent, gameOutcome));
        }
        return stats;
    }

    private List<GameStats> convertResultToStats(List<QueryResult<SurrealGameResult>> results) {
        List<GameStats> stats = convertToGameStats(results.get(0), GameOutcome.WIN);
        stats.addAll(convertToGameStats(results.get(1), GameOutcome.LOOSE));
        stats.addAll(convertToGameStats(results.get(2), GameOutcome.DRAW));
        stats.addAll(convertToGameStats(results.get(3), GameOutcome.DRAW));
        stats.sort(new GameStatsComparator());
        return stats;
    }

    private void createRating(Player player, Game game) {
        WengLingRating rating = getRatingAlgo().generateRating();
        this.driver.query("CREATE rating SET rating = <float> $rating, uncertainty = <float> $uncertainty, game=$game, user=array::at((SELECT VALUE id FROM user WHERE name=$player), 0);", Map.of("rating", String.valueOf(rating.rating()), "uncertainty", String.valueOf(rating.uncertainty()), "game", game.getId(), "player", player.getName()), Object.class);
    }

}
