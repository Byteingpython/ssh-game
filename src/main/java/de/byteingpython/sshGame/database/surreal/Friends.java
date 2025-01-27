package de.byteingpython.sshGame.database.surreal;

import java.util.ArrayList;
import java.util.List;

public class Friends {
    private final List<String> name;

    public Friends() {
        name = new ArrayList<>();
    }

    public List<String> getNames() {
        return name;
    }
}
