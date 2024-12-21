package de.byteingpython.sshGame.utils;

import de.byteingpython.sshGame.ssh.shell.WindowSize;

public class StringUtils {
    /**
     * Centers the given text in a string with the given total width
     *
     * @param text       The text to center
     * @param totalWidth the total width of the string in characters that the text should be centered in.
     * @return The rendered String
     */
    public static String centerText(String text, int totalWidth) {
        int spacesNeeded = totalWidth - text.length();
        if (spacesNeeded < 0) {
            text = text.substring(0, Math.max(totalWidth - 3, 0));
            text += "...".substring(0, Math.min(3, totalWidth));
        }
        int leftSpaces = spacesNeeded / 2;

        if (spacesNeeded % 2 != 0) {
            leftSpaces++;
        }

        int rightSpaces = spacesNeeded - leftSpaces;

        return " ".repeat(leftSpaces) + text + " ".repeat(rightSpaces);
    }

    public static String centerInTerminal(String text, WindowSize windowSize) {
        StringBuilder builder = new StringBuilder();
        int linesToAdd = windowSize.height() - text.split("\n").length;
        builder.append((" ".repeat(windowSize.width()) + "\n\r").repeat(Math.max(0, linesToAdd / 2)));
        for (String line : text.split("\n")) {
            String clearedLine = line.replace("\r", "");
            int columnsToAdd = windowSize.width() - clearedLine.length();
            builder.append(" ".repeat(Math.max(0, columnsToAdd / 2)))
                    .append(clearedLine)
                    .append(" ".repeat(Math.max(0, Math.round((float) columnsToAdd / 2))))
                    .append("\n\r");
        }
        builder.append((" ".repeat(windowSize.width()) + "\n\r").repeat(Math.max(0, Math.round((float) linesToAdd / 2))));
        builder.replace(builder.length() - 2, builder.length(), "");
        return builder.toString();
    }

    public static String fillOutSpaces(String text) {
        int maxLength = 0;
        for (String line : text.split("\n")) {
            int length = line.replace("\r", "").length();
            if (length > maxLength) {
                maxLength = length;
            }
        }

        StringBuilder builder = new StringBuilder();

        for (String line : text.split("\n")) {
            int length = line.replace("\r", "").length();
            builder.append(line.replace("\r", ""));
            builder.append(" ".repeat(maxLength - length));
            builder.append("\n\r");
        }
        return builder.toString();
    }

    public static String alignToSides(String left, String right, int length) {
        int spacesNeeded = length - left.length() - right.length();
        return left + " ".repeat(Math.max(0, spacesNeeded)) + right;
    }
}
