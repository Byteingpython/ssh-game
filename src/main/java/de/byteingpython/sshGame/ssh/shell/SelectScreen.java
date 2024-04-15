package de.byteingpython.sshGame.ssh.shell;

import de.byteingpython.sshGame.games.Player;
import de.byteingpython.sshGame.utils.ColorUtils;
import de.byteingpython.sshGame.utils.EscapeCodeUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class SelectScreen<T> {
    private final LinkedHashMap<String, T> options = new LinkedHashMap<>();
    private Map.Entry<String, T> selected;

    public SelectScreen(){

    }

    public void addOption(String name, T value) {
        options.put(name, value);
    }

    public Optional<T> selectOption(Player player, String message) {
        if(options.isEmpty()) {
            throw new IllegalArgumentException("Options must not be empty");
        }
        while (true) {
            try {
                render(player, message);
                int input = player.getInputStream().read();
                // 91 is the escape character for arrow keys
                if (input == 91) {
                    input = player.getInputStream().read();
                    switch (input) {
                        case 65:
                            selected = getNextEntry();
                            break;
                        case 66:
                            selected = getPreviousEntry();
                            break;
                    }
                } else if (input==13){
                    return Optional.of(selected.getValue());
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
        player.getOutputStream().write(message.getBytes(StandardCharsets.UTF_8));
        for (Map.Entry<String, T> entry : options.entrySet()) {
            if(entry==selected) {
                player.getOutputStream().write((ColorUtils.WHITE+entry.getKey()+ColorUtils.NO_COLOR +"\n\r").getBytes(StandardCharsets.UTF_8));
            }
            else {
                player.getOutputStream().write((entry.getKey()+"\n\r").getBytes(StandardCharsets.UTF_8));
            }
        }
    }

    private Map.Entry<String, T> getNextEntry() {
        int index = indexOfEntry(options, selected).orElseThrow();
        return (Map.Entry<String, T>) options.entrySet().toArray()[(options.size()-1) % (index +1)];
    }

    private Map.Entry<String, T> getPreviousEntry() {
        int index = indexOfEntry(options, selected).orElseThrow();
        return (Map.Entry<String, T>) options.entrySet().toArray()[(options.size()-1) % (index -1)];
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
