package de.byteingpython.sshGame.database.surreal;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

public class User {
    String passwordHash;
    String name;
    String publicKey;
    List<String> friends;

    public User(String name, String passwordHash) {
        this.name = name;
        this.passwordHash = passwordHash;
        friends = new ArrayList<>();
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

    public void addFriend(User user) {
    }

    public void removeFriend(User user) {
    }

    public List<String> getFriends() {
        return friends;
    }

    //TODO: Implement this!
    public String getPublicKey() {
        return null;
    }

    public void setPublicKey(PublicKey publicKey) {

    }
}
