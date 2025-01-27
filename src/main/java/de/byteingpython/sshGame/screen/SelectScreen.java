package de.byteingpython.sshGame.screen;

import de.byteingpython.sshGame.event.InputListener;
import de.byteingpython.sshGame.player.Player;
import de.byteingpython.sshGame.utils.EscapeCodeUtils;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class SelectScreen<T> implements InputListener {
    private final LinkedHashMap<String, T> options = new LinkedHashMap<>();
    private final Player player;
    private Optional<Map.Entry<String, T>> selected = Optional.empty();
    private Runnable endRunnable;
    private String message;
    private boolean escaped = false;
    private boolean running = false;

    public SelectScreen(Player player) {
        this.player = player;
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

    public void addOption(String name, T value) {
        options.put(name, value);
        if (running) {
            try {
                selected = Optional.of(options.entrySet().iterator().next());
                render();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void selectOption(String message, Runnable endRunnable) {
        player.getInputEventHandler().registerListener(this);
        this.message = message;
        this.endRunnable = endRunnable;
        running = true;
        if (options.isEmpty()) {
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
            } else {
                player.getOutputStream().write((entry.getKey() + "\n\r").getBytes(StandardCharsets.UTF_8));
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

    @Override
    public void onInput(int input) {
        LoggerFactory.getLogger(this.getClass()).info("Select input: " + input);
        if (options.isEmpty()) {
            return;
        }
        if (escaped) {
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
        } else if (input == 13) {
            running = false;
            player.getInputEventHandler().unregisterListener(this);
            endRunnable.run();
            return;
        } else if (input == 3 || input == 'q') {
            selected = Optional.empty();
            running = false;
            player.getInputEventHandler().unregisterListener(this);
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

    public void clearOptions() {
        options.clear();
        selected = Optional.empty();
        if (running) {
            try {
                render();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    protected Player getPlayer() {
        return player;
    }

    public void removeOption(T value) {
        if (selected.isPresent()) {
            if (selected.get().getValue().equals(value)) {
                selected = Optional.ofNullable(getPreviousEntry());
            }
        }
        for (Map.Entry<String, T> entry : new HashSet<>((options.entrySet()))) {
            if (entry.getValue().equals(value)) {
                options.remove(entry.getKey());
            }
        }
        if (running) {
            try {
                render();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
