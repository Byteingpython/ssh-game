package de.byteingpython.sshGame.player;

import java.util.List;
import java.util.Locale;

public interface LocaleManager {
    Locale getLocale(Player player);

    List<Locale> getAvailableLocales();

    void setLocale(Player player, Locale locale);

    Locale getDefaultLocale();
}
