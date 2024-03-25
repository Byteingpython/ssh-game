package de.byteingpython.sshGame.database.surreal;

import java.security.PublicKey;

public class User {
    String passwordHash;
    String name;
    String publicKey;

    public User(String name, String passwordHash) {
        this.name = name;
        this.passwordHash = passwordHash;
    }

    //TODO: Implement this!
    public User(String name, PublicKey publicKey) {
        this.name = name;
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

    //TODO: Implement this!
    public String getPublicKey() {
        return null;
    }

    public void setPublicKey(PublicKey publicKey) {

    }
}
