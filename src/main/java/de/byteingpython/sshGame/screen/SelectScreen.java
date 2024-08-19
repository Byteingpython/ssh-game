package de.byteingpython.sshGame.screen;

import de.byteingpython.sshGame.player.Player;
import de.byteingpython.sshGame.utils.EscapeCodeUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class SelectScreen<T> {
    private final LinkedHashMap<String, T> options = new LinkedHashMap<>();
    private final Map<Player, Map.Entry<String, T>> selected = new HashMap<>();

    public SelectScreen(){

    }

    public void addOption(String name, T value) {
        options.put(name, value);
    }

    public Optional<T> selectOption(Player player, String message) {
        if(options.isEmpty()) {
            throw new IllegalArgumentException("Options must not be empty");
        }
        selected.put(player, options.entrySet().iterator().next());
        while (true) {
            try {
                render(player, message);
                int input = player.getInputStream().read();
                // 91 is the escape character for arrow keys
                if (input == 91) {
                    input = player.getInputStream().read();
                    switch (input) {
                        case 66:
                            selected.put(player, getNextEntry(player));
                            break;
                        case 65:
                            selected.put(player, getPreviousEntry(player));
                            break;
                    }
                } else if (input==13){
                    return Optional.of(selected.get(player).getValue());
                } else if (input == 3 || input==27) {
                    return Optional.empty();
                }

            } catch (IOException e) {
                return Optional.empty();
            }
        }
    }

    public Optional<T> selectOption(Player player) {
        return selectOption(player, "Select an option");
    }

    private void render(Player player, String message) throws IOException {
        player.getOutputStream().write(EscapeCodeUtils.CLEAR_SCREEN.getBytes(StandardCharsets.UTF_8));
        player.getOutputStream().flush();
        player.getOutputStream().write((message + "\n\r").getBytes(StandardCharsets.UTF_8));
        for (Map.Entry<String, T> entry : options.entrySet()) {
            if (entry == selected.get(player)) {
                player.getOutputStream().write(("> " + entry.getKey()).getBytes(StandardCharsets.UTF_8));
                player.getOutputStream().write("\n\r".getBytes(StandardCharsets.UTF_8));
            }
            else {
                player.getOutputStream().write((entry.getKey()+"\n\r").getBytes(StandardCharsets.UTF_8));
            }
        }
        player.getOutputStream().flush();
    }

    private Map.Entry<String, T> getNextEntry(Player player) {
        int index = indexOfEntry(options, selected.get(player)).orElseThrow();
        return (Map.Entry<String, T>) options.entrySet().toArray()[(index + 1) % (options.size())];
    }

    private Map.Entry<String, T> getPreviousEntry(Player player) {
        int index = indexOfEntry(options, selected.get(player)).orElseThrow();
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
}
