package de.byteingpython.sshGame.screen;

import de.byteingpython.sshGame.event.EventListener;
import de.byteingpython.sshGame.event.InputEvent;
import de.byteingpython.sshGame.player.Player;
import de.byteingpython.sshGame.ssh.shell.WindowChangeEvent;
import de.byteingpython.sshGame.utils.EscapeCodeUtils;
import de.byteingpython.sshGame.utils.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;

public class TextDisplayScreen {
    private final String text;
    private Runnable endCallback;
    private Player player;
    TextDisplayScreen(String text) {
        this.text = text;
    }

    public static TextDisplayScreen fromTranslation(String key, ResourceBundle locale) {
        return new TextDisplayScreen(locale.getString(key));
    }

    public void show(Player player, Runnable endCallback) {
        this.player = player;
        this.endCallback = endCallback;
        player.getEventHandler().registerListeners(this);
        render();
    }

    private void render() {
        try {
            player.getOutputStream().write(EscapeCodeUtils.CLEAR_SCREEN.getBytes(StandardCharsets.UTF_8));
            player.getOutputStream().write(StringUtils.centerInTerminal(StringUtils.fillOutSpaces(text), player.getWindowSize()).getBytes(StandardCharsets.UTF_8));
            player.getOutputStream().flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @EventListener
    public void onInput(InputEvent event) {
        switch(event.getInput()) {
            case 'q':
                player.getEventHandler().unregisterListeners(this);
                endCallback.run();
                return;
        }
    }

    @EventListener
    public void onResize(WindowChangeEvent event) {
        render();
    }

}
