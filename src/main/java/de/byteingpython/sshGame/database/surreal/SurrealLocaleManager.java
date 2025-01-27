package de.byteingpython.sshGame.database.surreal;

import com.surrealdb.driver.SyncSurrealDriver;
import de.byteingpython.sshGame.player.LocaleManager;
import de.byteingpython.sshGame.player.Player;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SurrealLocaleManager implements LocaleManager {
    private final SyncSurrealDriver driver;

    public SurrealLocaleManager(SyncSurrealDriver driver) {
        this.driver = driver;
    }

    @Override
    public Locale getLocale(Player player) {
        List<User> results = driver.query("SELECT locale FROM user WHERE name=$player", Map.of("player", player.getName()), User.class).get(0).getResult();
        if (!results.isEmpty()) {
            if (results.get(0).getLocale() == null) return getDefaultLocale();
            return Locale.forLanguageTag(results.get(0).getLocale());
        }
        return getDefaultLocale();
    }

    @Override
    public List<Locale> getAvailableLocales() {
        return List.of(Locale.ENGLISH, Locale.GERMAN);
    }

    @Override
    public void setLocale(Player player, Locale locale) {
        driver.query("UPDATE user SET locale=$locale WHERE name=$name", Map.of("locale", locale.toLanguageTag(), "name", player.getName()), Object.class);
    }

    @Override
    public Locale getDefaultLocale() {
        return Locale.ENGLISH;
    }
}
