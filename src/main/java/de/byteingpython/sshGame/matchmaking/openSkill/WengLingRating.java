package de.byteingpython.sshGame.matchmaking.openSkill;

import java.util.Objects;

public final class WengLingRating {
    private float rating;
    private float uncertainty;

    public WengLingRating(float rating, float uncertainty) {
        this.rating = rating;
        this.uncertainty = uncertainty;
    }

    public float rating() {
        return rating;
    }

    public float uncertainty() {
        return uncertainty;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (WengLingRating) obj;
        return Float.floatToIntBits(this.rating) == Float.floatToIntBits(that.rating) &&
                Float.floatToIntBits(this.uncertainty) == Float.floatToIntBits(that.uncertainty);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rating, uncertainty);
    }

    @Override
    public String toString() {
        return "WengLingRating[" +
                "rating=" + rating + ", " +
                "uncertainty=" + uncertainty + ']';
    }

}
