package de.byteingpython.sshGame.screen;

import com.sshtools.common.publickey.SshKeyUtils;
import com.sshtools.common.ssh.components.SshPublicKey;
import de.byteingpython.sshGame.player.LocaleManager;
import de.byteingpython.sshGame.player.Player;
import de.byteingpython.sshGame.ssh.auth.CredentialAuthProvider;
import de.byteingpython.sshGame.utils.Message;

import java.io.IOException;
import java.util.Locale;

public class SettingsScreen {
    private final Player player;
    private final CredentialAuthProvider credentialAuthProvider;
    private final LocaleManager localeManager;
    private TextInputScreen textInputScreen;
    private String confirmPassword;

    public SettingsScreen(Player player, CredentialAuthProvider credentialAuthProvider, LocaleManager localeManager) {
        this.player = player;
        this.credentialAuthProvider = credentialAuthProvider;
        this.localeManager = localeManager;
    }

    public void show(Runnable runnable) {
        showMainSettingsScreen(runnable);
    }

    private void showMainSettingsScreen(Runnable runnable) {
        SelectScreen<String> selectScreen = new SelectScreen<>(player);
        selectScreen.addOption(player.getLocale().getString("change_password"), "password");
        selectScreen.addOption(player.getLocale().getString("change_publickey"), "publickey");
        selectScreen.addOption(player.getLocale().getString("change_locale"), "locale");
        selectScreen.selectOption(player.getLocale().getString("settings"), () -> {
            if (selectScreen.getSelected().isEmpty()) {
                runnable.run();
                return;
            }
            switch (selectScreen.getSelected().get()) {
                case "password":
                    showPasswordEditScreen(runnable, false);
                    break;
                case "publickey":
                    showPublicKeyEditScreen(runnable);
                    break;
                case "locale":
                    showLocaleEditScreen(runnable);
                    break;
            }
        });

    }

    private void showPasswordEditScreen(Runnable runnable, boolean confirm) {
        String message;
        if (confirm) {
            message = player.getLocale().getString("password_confirm");
        } else {
            message = player.getLocale().getString("new_password");
        }
        try {
            textInputScreen = new TextInputScreen(() -> {
                String input = textInputScreen.getInput();
                if (input.isEmpty()) {
                    showMainSettingsScreen(runnable);
                    return;
                }
                if (!confirm) {
                    confirmPassword = input;
                    showPasswordEditScreen(runnable, true);
                } else {
                    if (confirmPassword.equals(input)) {
                        credentialAuthProvider.updatePassword(player.getName(), input);
                        player.getEventHandler().handle(new LobbyScreenMessageEvent(new Message(player.getLocale().getString("password_changed"), 3000)));
                        runnable.run();
                    } else {
                        player.getEventHandler().handle(new LobbyScreenMessageEvent(new Message(player.getLocale().getString("password_not_match"), 3000)));
                        runnable.run();
                    }
                }
            }, player, message);
            textInputScreen.setPasswordInput(true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void showPublicKeyEditScreen(Runnable runnable) {
        try {
            textInputScreen = new TextInputScreen(() -> {
                String input = textInputScreen.getInput();
                if (input.isEmpty()) {
                    showMainSettingsScreen(runnable);
                    return;
                }
                try {
                    SshPublicKey publicKey = SshKeyUtils.getPublicKey(input);
                    credentialAuthProvider.updateUserKey(player.getName(), publicKey);
                    player.getEventHandler().handle(new LobbyScreenMessageEvent(new Message(player.getLocale().getString("public_key_changed"), 3000)));
                } catch (IOException e) {
                    player.getEventHandler().handle(new LobbyScreenMessageEvent(new Message(player.getLocale().getString("public_key_invalid_format"), 3000)));
                }
                runnable.run();
            }, player, player.getLocale().getString("public_key_input_title"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void showLocaleEditScreen(Runnable runnable) {
        SelectScreen<String> selectScreen = new SelectScreen<>(player);
        for (Locale locale : localeManager.getAvailableLocales()) {
            selectScreen.addOption(locale.getDisplayLanguage(player.getLocale().getLocale()), locale.toLanguageTag());
        }
        selectScreen.selectOption(player.getLocale().getString("select_locale"), () -> {
            if (selectScreen.getSelected().isEmpty()) {
                showMainSettingsScreen(runnable);
                return;
            }
            localeManager.setLocale(player, Locale.forLanguageTag(selectScreen.getSelected().get()));
            player.setLocale(Locale.forLanguageTag(selectScreen.getSelected().get()));
            player.getEventHandler().handle(new LobbyScreenMessageEvent(new Message(player.getLocale().getString("locale_changed"), 3000)));
            runnable.run();
        });
    }
}
