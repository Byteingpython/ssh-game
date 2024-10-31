package de.byteingpython.sshGame.screen;

import com.sshtools.common.publickey.SshKeyUtils;
import com.sshtools.common.ssh.components.SshPublicKey;
import de.byteingpython.sshGame.player.Player;
import de.byteingpython.sshGame.ssh.auth.CredentialAuthProvider;
import de.byteingpython.sshGame.ssh.auth.CredentialProvider;
import de.byteingpython.sshGame.utils.Message;

import java.io.IOException;

public class SettingsScreen {
    private final Player player;
    private final CredentialAuthProvider credentialAuthProvider;
    private TextInputScreen textInputScreen;
    private String confirmPassword;

    public SettingsScreen(Player player, CredentialAuthProvider credentialAuthProvider) {
        this.player = player;
        this.credentialAuthProvider = credentialAuthProvider;
    }

    public void show(Runnable runnable) {
        showMainSettingsScreen(runnable);
    }

    private void showMainSettingsScreen(Runnable runnable) {
        SelectScreen<String> selectScreen = new SelectScreen<>(player);
        selectScreen.addOption("Change Password", "password");
        selectScreen.addOption("Change Public Key", "publickey");
        selectScreen.selectOption("Settings", ()-> {
            if(selectScreen.getSelected().isEmpty()){
                runnable.run();
            }
            switch (selectScreen.getSelected().get()){
                case "password":
                    showPasswordEditScreen(runnable, false);
                    break;
                case "publickey":
                    showPublicKeyEditScreen(runnable);
                    break;
            }
        });

    }

    private void showPasswordEditScreen(Runnable runnable, boolean confirm) {
        String message;
        if(confirm){
            message="Confirm new password";
        } else {
            message="New password";
        }
        try {
            textInputScreen = new TextInputScreen(()->{
                String input = textInputScreen.getInput();
                if(input.isEmpty()){
                    showMainSettingsScreen(runnable);
                }
                if(!confirm) {
                    confirmPassword = input;
                    showPasswordEditScreen(runnable, true);
                } else {
                    if(confirmPassword.equals(input)){
                        credentialAuthProvider.updatePassword(player.getName(), input);
                        player.getEventHandler().handle(new LobbyScreenMessageEvent(new Message("Password changed!", 3000)));
                        runnable.run();
                    } else {
                        player.getEventHandler().handle(new LobbyScreenMessageEvent(new Message("Passwords do not match!", 3000)));
                        runnable.run();
                    }
                }
            }, player, message);
            textInputScreen.setPasswordInput(true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void showPublicKeyEditScreen(Runnable runnable) {
        try {
            textInputScreen = new TextInputScreen(()->{
                String input = textInputScreen.getInput();
                if(input.isEmpty()){
                    showMainSettingsScreen(runnable);
                }
                try {
                    SshPublicKey publicKey = SshKeyUtils.getPublicKey(input);
                    credentialAuthProvider.updateUserKey(player.getName(), publicKey);
                    player.getEventHandler().handle(new LobbyScreenMessageEvent(new Message("Public key changed!", 3000)));
                } catch (IOException e) {
                    player.getEventHandler().handle(new LobbyScreenMessageEvent(new Message("Public Key is not in a recognized Format!", 3000)));
                }
                runnable.run();
            }, player, "Enter Public Key");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
