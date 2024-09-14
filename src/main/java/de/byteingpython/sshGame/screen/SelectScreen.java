package de.byteingpython.sshGame.screen;

import de.byteingpython.sshGame.event.InputListener;
import de.byteingpython.sshGame.player.Player;
import de.byteingpython.sshGame.utils.EscapeCodeUtils;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class SelectScreen<T> implements InputListener {
    private final LinkedHashMap<String, T> options = new LinkedHashMap<>();
    private Optional<Map.Entry<String, T>> selected = Optional.empty();
    private Runnable endRunnable;
    private final Player player;
    private String message;
    private boolean escaped = false;

    public SelectScreen(Player player) {
        this.player = player;
    }

    public void addOption(String name, T value) {
        options.put(name, value);
    }

    public void selectOption(String message, Runnable endRunnable) {
        player.getEventHandler().registerListener(this);
        this.message = message;
        this.endRunnable = endRunnable;
        if(options.isEmpty()) {
            throw new IllegalArgumentException("Options must not be empty");
        }
        selected = Optional.of(options.entrySet().iterator().next());
        try {
            render();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void selectOption(Runnable endRunnable) {
        selectOption("Select an option", endRunnable);
    }

    private void render() throws IOException {
        player.getOutputStream().write(EscapeCodeUtils.CLEAR_SCREEN.getBytes(StandardCharsets.UTF_8));
        player.getOutputStream().flush();
        player.getOutputStream().write((message + "\n\r").getBytes(StandardCharsets.UTF_8));
        for (Map.Entry<String, T> entry : options.entrySet()) {
            if (entry == selected.orElse(null)) {
                player.getOutputStream().write(("> " + entry.getKey()).getBytes(StandardCharsets.UTF_8));
                player.getOutputStream().write("\n\r".getBytes(StandardCharsets.UTF_8));
            }
            else {
                player.getOutputStream().write((entry.getKey()+"\n\r").getBytes(StandardCharsets.UTF_8));
            }
        }
        player.getOutputStream().flush();
    }

    private Map.Entry<String, T> getNextEntry() {
        int index = indexOfEntry(options, selected.orElseThrow()).orElseThrow();
        return (Map.Entry<String, T>) options.entrySet().toArray()[(index + 1) % (options.size())];
    }

    private Map.Entry<String, T> getPreviousEntry() {
        int index = indexOfEntry(options, selected.orElseThrow()).orElseThrow();
        return (Map.Entry<String, T>) options.entrySet().toArray()[Math.floorMod(index - 1, options.size())];
    }
    private static <T, H> Optional<Integer> indexOfEntry(LinkedHashMap<H, T> options, Map.Entry<H, T> entry) {
        int index = 0;
        for (Map.Entry<H, T> current : options.entrySet()) {
            if (entry.equals(current)) {
                return Optional.of(index);
            }
            index++;
        }
        return Optional.empty();
    }

    @Override
    public void onInput(int input) {
        LoggerFactory.getLogger(this.getClass()).info("Select input: " + input);
        if(escaped) {
            escaped = false;
            switch (input) {
                case 66:
                    selected = Optional.ofNullable(getNextEntry());
                    break;
                case 65:
                    selected = Optional.ofNullable(getPreviousEntry());
                    break;
            }
        }
        // 91 is the escape character for arrow keys
        if (input == 91) {
            escaped = true;
        } else if (input==13){
            player.getEventHandler().unregisterListener(this);
            endRunnable.run();
            return;
        } else if (input == 3) {
            selected = Optional.empty();
            player.getEventHandler().unregisterListener(this);
            endRunnable.run();
            return;
        }
        try {
            render();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<T> getSelected() {
        return selected.map(Map.Entry::getValue);
    }
}
