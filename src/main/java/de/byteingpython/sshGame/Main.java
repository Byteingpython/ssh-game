package de.byteingpython.sshGame;

import com.surrealdb.connection.SurrealWebSocketConnection;
import com.surrealdb.driver.SyncSurrealDriver;
import de.byteingpython.sshGame.ssh.PasswordAuthenticator;
import de.byteingpython.sshGame.ssh.ShellFactory;
import io.github.cdimascio.dotenv.Dotenv;
import org.apache.sshd.common.cipher.BuiltinCiphers;
import org.apache.sshd.common.keyprovider.KeyPairProvider;
import org.apache.sshd.common.session.SessionContext;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.keyboard.DefaultKeyboardInteractiveAuthenticator;
import org.apache.sshd.server.config.keys.DefaultAuthorizedKeysAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.shell.InteractiveProcessShellFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;

public class Main {

    public SshServer getSshd() {
        return sshd;
    }

    private SshServer sshd;

    public Logger getLogger() {
        return logger;
    }

    private Logger logger= LoggerFactory.getLogger(Main.class);

    public SyncSurrealDriver getDatabaseDriver() {
        return databaseDriver;
    }

    SyncSurrealDriver databaseDriver;


    public Main() {
        Dotenv dotenv = Dotenv.load();
        SurrealWebSocketConnection conn = new SurrealWebSocketConnection(Objects.requireNonNull(dotenv.get("SURREALDB_URL")), Integer.parseInt(Objects.requireNonNullElse(dotenv.get("SURREALDB_PORT"), "8000")), Boolean.parseBoolean(Objects.requireNonNullElse(dotenv.get("SURREALDB_TLS"), "false")));
        conn.connect(5);
        databaseDriver = new SyncSurrealDriver(conn);
        databaseDriver.signIn(Objects.requireNonNull(dotenv.get("SURREALDB_USER")), Objects.requireNonNull(dotenv.get("SURREALDB_PASSWORD")));
        databaseDriver.use(Objects.requireNonNull(dotenv.get("SURREALDB_NAMESPACE")), Objects.requireNonNull(dotenv.get("SURREALDB_DATABASE")));
        try {
            setupSshServer(dotenv);
            logger.info("SSH Server started on port "+sshd.getPort());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    private void setupSshServer(Dotenv dotenv) throws IOException {
        sshd = SshServer.setUpDefaultServer();
        sshd.setPort(Integer.parseInt(Objects.requireNonNullElse(dotenv.get("SSH_PORT"), "22")));
        sshd.setPasswordAuthenticator(new PasswordAuthenticator(this));
        sshd.setShellFactory(new InteractiveProcessShellFactory());
        sshd.setKeyPairProvider(new KeyPairProvider() {
            @Override
            public Iterable<KeyPair> loadKeys(SessionContext session) throws IOException, GeneralSecurityException {
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(Objects.requireNonNull(dotenv.get("SSH_PUBLIC_KEY"))));
                KeyPair keyPair = new;
            }
        });
        sshd.setPublickeyAuthenticator(new DefaultAuthorizedKeysAuthenticator(true));
        sshd.setShellFactory(new ShellFactory(this));
        sshd.setKeyboardInteractiveAuthenticator(new DefaultKeyboardInteractiveAuthenticator());
        sshd.setCipherFactories(Arrays.asList(BuiltinCiphers.aes256ctr, BuiltinCiphers.aes192ctr, BuiltinCiphers.aes128ctr));
        sshd.start();
    }

    public static void main(String[] args) {
        new Main();
    }
}
