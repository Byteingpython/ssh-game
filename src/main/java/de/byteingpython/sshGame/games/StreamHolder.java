package de.byteingpython.sshGame.games;

import java.io.InputStream;
import java.io.OutputStream;

public interface StreamHolder {
    OutputStream getOutputStream();

    OutputStream getErrorStream();

    InputStream getInputStream();
}
