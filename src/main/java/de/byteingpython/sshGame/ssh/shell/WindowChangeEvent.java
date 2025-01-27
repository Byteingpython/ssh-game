package de.byteingpython.sshGame.ssh.shell;

import de.byteingpython.sshGame.event.Event;

public record WindowChangeEvent(WindowSize size) implements Event {
}
