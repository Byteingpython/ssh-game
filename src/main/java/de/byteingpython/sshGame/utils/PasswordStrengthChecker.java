package de.byteingpython.sshGame.utils;

public class PasswordStrengthChecker {
    public static boolean isPasswordStrong(String password) {
        return password.matches("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$");
    }
}
