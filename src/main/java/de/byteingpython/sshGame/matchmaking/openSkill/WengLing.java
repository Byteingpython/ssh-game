package de.byteingpython.sshGame.matchmaking.openSkill;

import de.byteingpython.sshGame.games.GameOutcome;
import de.byteingpython.sshGame.utils.Pair;

import java.util.List;

public class WengLing implements RatingAlgo<WengLingRating> {
    private final float beta;
    private final float uncertaintyTolerance;

    public WengLing(float beta, float uncertaintyTolerance) {
        this.beta = beta;
        this.uncertaintyTolerance = uncertaintyTolerance;
    }


    @Override
    public WengLingRating generateRating() {
        return new WengLingRating(25f, 25f / 3f);
    }

    //TODO: Implement this
    @Override
    public List<Team<WengLingRating>> rate(Team<WengLingRating>... teams) {
        return List.of();
    }

    @Override
    public Pair<WengLingRating, WengLingRating> rate(GameOutcome outcome, WengLingRating t1, WengLingRating t2) {
        float c = (float) Math.sqrt((2.0f * this.beta * this.beta) + t1.uncertainty() * t1.uncertainty() + t2.uncertainty() * t2.uncertainty());
        Pair<Float, Float> p = pValue(t1.rating(), t2.rating(), c);
        float floatOutcome = outcomeToFloat(outcome);
        WengLingRating firstRating = new WengLingRating(newRating(t1, c, p.getFirst(), floatOutcome), newUncertainty(t1, c, p.getFirst()));
        WengLingRating secondRating = new WengLingRating(newRating(t2, c, p.getSecond(), 1f - floatOutcome), newUncertainty(t2, c, p.getSecond()));
        return new Pair<>(firstRating, secondRating);
    }

    //TODO: Implement this
    @Override
    public List<Team<WengLingRating>> rateTie(Team<WengLingRating>... teams) {
        return List.of();
    }

    //TODO: Implement this
    @Override
    public float predict_win(Team<WengLingRating>... teams) {
        return 0;
    }

    //TODO: Implement this
    @Override
    public float predict_draw(Team<WengLingRating>... teams) {
        return 0;
    }

    //TODO: Implement this
    @Override
    public float predict_rank(Team<WengLingRating>... teams) {
        return 0;
    }

    @Override
    public float predict(WengLingRating t1, WengLingRating t2) {
        float c = 2f * this.beta * this.beta + t1.uncertainty() * t1.uncertainty() + t2.uncertainty() * t2.uncertainty();
        return pValue(t1.rating(), t2.rating(), c).first;
    }


    private Pair<Float, Float> pValue(float ratingOne, float ratingTwo, float c) {
        float e1 = (float) Math.exp(ratingOne / c);
        float e2 = (float) Math.exp(ratingTwo / c);
        float exp1 = e1 / (e1 + e2);
        float exp2 = 1f - exp1;
        return new Pair(exp1, exp2);
    }

    private float outcomeToFloat(GameOutcome outcome) {
        return switch (outcome) {
            case WIN -> 1.0f;
            case LOOSE -> 0f;
            case DRAW -> 0.5f;
        };
    }

    private float newRating(WengLingRating rating, float c, float p, float score) {
        return (rating.uncertainty() * rating.uncertainty() / c) * (score - p) + rating.rating();
    }

    private float newUncertainty(WengLingRating rating, float c, float p) {
        float pc = rating.uncertainty() / c;
        float eta = pc * pc * pc * p * (1f - p);
        return (float) Math.sqrt(rating.uncertainty() * rating.uncertainty() * Math.max(1f - eta, uncertaintyTolerance));
    }
}
