package de.byteingpython.sshGame.utils;

public class EscapeCodeUtils {
    public static final String CLEAR_SCREEN = "\033[H\033[2J";
    public static final String SWITCH_TO_ALTERNATE_SCREEN = "\033[?1049h";
    public static final String SWITCH_TO_MAIN_SCREEN = "\033[?1049l";
    public static final String HIDE_CURSOR = "\033[?25l";
    public static final String SHOW_CURSOR = "\033[?25h";
}