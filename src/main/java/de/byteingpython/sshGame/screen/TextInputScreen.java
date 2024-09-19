package de.byteingpython.sshGame.screen;

import de.byteingpython.sshGame.event.InputListener;
import de.byteingpython.sshGame.player.Player;
import de.byteingpython.sshGame.utils.EscapeCodeUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class TextInputScreen implements InputListener {
    private final Runnable endCallback;
    private final Player player;
    private String inputText = "";

    public TextInputScreen(Runnable endCallback, Player player, String message) throws IOException {
        this.endCallback = endCallback;
        this.player = player;
        player.getInputEventHandler().registerListener(this);
        player.getOutputStream().write(EscapeCodeUtils.CLEAR_SCREEN.getBytes(StandardCharsets.UTF_8));
        player.getOutputStream().write(EscapeCodeUtils.SHOW_CURSOR.getBytes(StandardCharsets.UTF_8));
        player.getOutputStream().flush();
        player.getOutputStream().write((message + "\n\r" + ">").getBytes(StandardCharsets.UTF_8));
        player.getOutputStream().flush();
    }

    @Override
    public void onInput(int input) {
        if (input == 127) {
            if (!inputText.isEmpty()) {
                inputText = inputText.substring(0, inputText.length() - 1);
                try {
                    player.getOutputStream().write("\b".getBytes(StandardCharsets.UTF_8));
                    player.getOutputStream().write(127);
                    player.getOutputStream().write("\b".getBytes(StandardCharsets.UTF_8));
                    player.getOutputStream().flush();
                    return;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                try {
                    player.getOutputStream().write(7);
                    player.getOutputStream().flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return;
            }
        }
        if (input == 13||input == 3) {
            player.getInputEventHandler().unregisterListener(this);
            try {
                player.getOutputStream().write(EscapeCodeUtils.HIDE_CURSOR.getBytes(StandardCharsets.UTF_8));
                player.getOutputStream().flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            endCallback.run();
            return;
        }
        if(input< 21) {
            return;
        }
        if(input>126){
            return;
        }
        inputText += (char) input;
        try {
            player.getOutputStream().write(input);
            player.getOutputStream().flush();
        } catch (IOException ignored) {
        }
    }

    public String getInput() {
        return inputText;
    }

    public void setInput(String input) {
        this.inputText = input;
    }
}
