package de.byteingpython.sshGame.ssh.shell;

import de.byteingpython.sshGame.config.ConfigurationProvider;
import org.apache.sshd.common.io.IoInputStream;
import org.apache.sshd.common.io.IoOutputStream;
import org.apache.sshd.common.util.buffer.ByteArrayBuffer;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ShellCommand implements Command {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ConfigurationProvider configurationProvider;
    private InputStream in;
    private OutputStream out;
    private OutputStream err;
    private IoOutputStream ioOut;
    private IoOutputStream ioErr;
    private IoInputStream ioIn;
    private ExitCallback callback;

    public ShellCommand(ConfigurationProvider configurationProvider) {
        this.configurationProvider = configurationProvider;
    }

    @Override
    public void setExitCallback(ExitCallback callback) {
        this.callback = callback;
    }

    @Override
    public void setErrorStream(OutputStream err) {
        this.err = err;
    }

    @Override
    public void setInputStream(InputStream in) {
        this.in = in;
    }

    @Override
    public void setOutputStream(OutputStream out) {
        logger.info("Setting output stream");
        this.out = out;
    }

    @Override
    public void start(ChannelSession channel, Environment env) throws IOException {
        logger.info("Starting shell");
        if (ioOut != null) {
            logger.info("Writing to iooutput stream");
            ioOut.writeBuffer(new ByteArrayBuffer("Welcome to the game\n".getBytes()));
        }
        if (out != null) {
            logger.info("Writing to output stream");
            out.write("Welcome to the game\n".getBytes());
            out.flush();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {

                    if (ioIn != null) {
                        logger.info("Reading from ioinput stream");
                        ByteArrayBuffer buffer = new ByteArrayBuffer(1024);
                        ioIn.read(buffer);
                        logger.info("Read from ioinput stream: " + new String(buffer.array()));
                    } else if (in != null) {
                        logger.info("Reading from input stream");
                        try {
                            logger.info("Read int:" + in.read());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        logger.info("No input stream");
                        break;
                    }
                }
            }

        }).start();
    }

    @Override
    public void destroy(ChannelSession channel) throws Exception {
        callback.onExit(0, "Goodbye");
    }
/*
    @Override
    public void setIoErrorStream(IoOutputStream err) {
        this.ioErr = err;
    }

    @Override
    public void setIoInputStream(IoInputStream in) {
        this.ioIn = in;
    }

    @Override
    public void setIoOutputStream(IoOutputStream out) {
        this.ioOut = out;
    }
 */
}
