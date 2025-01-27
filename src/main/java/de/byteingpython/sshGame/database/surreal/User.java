package de.byteingpython.sshGame.database.surreal;

import com.sshtools.common.publickey.SshKeyUtils;
import com.sshtools.common.ssh.components.SshPublicKey;

import java.io.IOException;
import java.security.PublicKey;
import java.util.Optional;

public class User {
    String passwordHash;
    String name;
    String publicKey;
    String locale;

    public User(String name, String passwordHash) {
        this.name = name;
        this.passwordHash = passwordHash;
    }

    public User(String name, SshPublicKey publicKey) {
        this.name = name;
        try {
            this.publicKey = SshKeyUtils.getFormattedKey(publicKey, "");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getLocale() {
        return locale;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getName() {
        return name;
    }

    public void addFriend(User user) {
    }

    public void removeFriend(User user) {
    }

    public Optional<SshPublicKey> getPublicKey() {
        if (publicKey == null) {
            return Optional.empty();
        }
        try {
            SshPublicKey key = SshKeyUtils.getPublicKey(publicKey);
            return Optional.of(key);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void setPublicKey(PublicKey publicKey) {

    }
}
