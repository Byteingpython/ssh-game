package de.byteingpython.sshGame.ssh.shell;

import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.Command;

import java.beans.EventHandler;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class EventHandlerShell implements Command {
    @Override
    public void setExitCallback(ExitCallback exitCallback) {

    }

    @Override
    public void setErrorStream(OutputStream outputStream) {

    }

    @Override
    public void setInputStream(InputStream inputStream) {

    }

    @Override
    public void setOutputStream(OutputStream outputStream) {

    }

    @Override
    public void start(ChannelSession channelSession, Environment environment) throws IOException {

    }

    @Override
    public void destroy(ChannelSession channelSession) throws Exception {

    }
}
