package de.byteingpython.sshGame.utils;

public class StringUtils {
    /**
     * Centers the given text in a string with the given total width
     * @param text The text to center
     * @param totalWidth the total width of the string in characters that the text should be centered in.
     * @return The rendered String
     */
    public static String centerText(String text, int totalWidth) {
        int spacesNeeded = totalWidth - text.length();
        if(spacesNeeded <= 0) {
            text = text.substring(0, Math.max(totalWidth-3, 0));
            text += "...".substring(0, Math.min(3, totalWidth));
        }
        int leftSpaces = spacesNeeded / 2;

        if (spacesNeeded % 2 != 0) {
            leftSpaces++;
        }

        int rightSpaces = spacesNeeded - leftSpaces;

        return " ".repeat(leftSpaces) + text + " ".repeat(rightSpaces);
    }
}
