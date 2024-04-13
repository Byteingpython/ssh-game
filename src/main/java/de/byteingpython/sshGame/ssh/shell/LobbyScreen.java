package de.byteingpython.sshGame.ssh.shell;


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
import java.nio.charset.StandardCharsets;

public class LobbyScreen implements Command {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private InputStream in;
    private OutputStream out;
    private OutputStream err;
    private IoOutputStream ioOut;
    private IoOutputStream ioErr;
    private IoInputStream ioIn;
    private ExitCallback callback;
    private final String PlayerName = "guenther";

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

        logger.info("Starting lobby");
        if (ioOut != null) {
            logger.info("Writing to iooutput stream");
            ioOut.writeBuffer(new ByteArrayBuffer("Welcome to the lobby\n".getBytes()));
        }
        if (out != null) {
            logger.info("Writing to output stream");
            out.write("Welcome to the lobby\n\r".getBytes());
            out.flush();
        }
        try {
            String playerNameRow = centerText(PlayerName, 25) + "║\n\r";

            out.write(("╔════════════════════════════════════════════╗\n\r" +
                    "║ Settings ^s                     ^f Friends ║\n\r" +
                    "║               ╭───╮    ╭───╮               ║\n\r" +
                    "║             ^a│ + │    │ @ │               ║\n\r" +
                    "║               ╰───╯    ╰───╯               ║\n\r" +
                    "║                Add" + playerNameRow+
                    "║                                            ║\n\r" +
                    "║ ┏╺╺╺╺╺┓                      ┏╺╺╺╺╺╺╺╺╺╺╺┓ ║\n\r" +
                    "║ ╏queue╏^q                  ^m╏Tic-Tac-Toe╏ ║\n\r" +
                    "║ ┗╺╺╺╺╺┛                      ┗╺╺╺╺╺╺╺╺╺╺╺┛ ║\n\r" +
                    "╚════════════════════════════════════════════╝").getBytes(StandardCharsets.UTF_8));
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        while (true) {

            try {
                int read = in.read();
                logger.info("Received input: " + read);
                if (read == 127) {
                    out.write("\b \b".getBytes());
                    out.flush();
                    continue;
                }
                //out.write(read);
                //out.flush();
                if (read == 19) {
                    out.write("-> Settings".getBytes());
                    out.flush();
                    continue;
                }
                if (read == 6) {
                    out.write("-> Friends".getBytes());
                    out.flush();
                    continue;
                }
                if (read == 1) {
                    out.write("-> Add".getBytes());
                    out.flush();
                    continue;
                }
                if (read == 17) {
                    out.write("-> Queue".getBytes());
                    out.flush();
                    continue;
                }
                if (read == 13) {
                    out.write("-> Menu".getBytes());
                    out.flush();
                    continue;
                }

                if (read == 3) {
                    out.write("\nGoodbye\n".getBytes());
                    out.flush();
                    callback.onExit(0, "Goodbye");
                    break;
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    @Override
    public void destroy(ChannelSession channel) throws Exception {
        callback.onExit(0, "Goodbye");
    }

    public static String centerText(String text, int totalWidth) {
        int spacesNeeded = totalWidth - text.length();
        int leftSpaces = spacesNeeded / 2;

        if (spacesNeeded % 2 != 0) {
            leftSpaces++;
        }

        int rightSpaces = spacesNeeded - leftSpaces;

        return " ".repeat(5) + text + " ".repeat(rightSpaces+leftSpaces-5);
    }
}