package de.byteingpython.sshGame.database.surreal;

import com.sshtools.common.publickey.SshKeyUtils;
import com.sshtools.common.ssh.components.SshPublicKey;
import com.surrealdb.driver.SyncSurrealDriver;
import com.surrealdb.driver.model.QueryResult;
import de.byteingpython.sshGame.config.ConfigurationProvider;
import de.byteingpython.sshGame.ssh.auth.CredentialAuthProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.ConfigurationException;
import java.io.IOException;
import java.security.PublicKey;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SurrealCredentialProvider extends CredentialAuthProvider {
    private final SyncSurrealDriver driver;
    private final Logger logger = LoggerFactory.getLogger(SurrealCredentialProvider.class);

    public SurrealCredentialProvider(ConfigurationProvider config) throws ConfigurationException {
        driver = new ConfigSurrealDriver(config);
    }


    @Override
    public Optional<String> getHashedPassword(String username) {
        List<QueryResult<User>> passwordQueryResult = driver.query("SELECT * FROM user WHERE name=$name", Map.of("name", username), User.class);
        if (!passwordQueryResult.get(0).getResult().isEmpty()) {
            return Optional.ofNullable(passwordQueryResult.get(0).getResult().get(0).getPasswordHash());
        }
        return Optional.empty();
    }

    @Override
    public Optional<SshPublicKey> getPublicKey(String username) {
        List<QueryResult<User>> keyQueryResult = driver.query("SELECT * FROM user WHERE name=$name", Map.of("name", username), User.class);
        if (!keyQueryResult.get(0).getResult().isEmpty()) {
            return keyQueryResult.get(0).getResult().get(0).getPublicKey();
        }
        return Optional.empty();
    }

    @Override
    public boolean doesUserExist(String username) {
        List<QueryResult<User>> userQueryResult = driver.query("SELECT * FROM user WHERE name=$name", Map.of("name", username), User.class);
        return !userQueryResult.get(0).getResult().isEmpty();
    }

    @Override
    public void createUserWithHashedPassword(String username, String passwordHash) {
        if (doesUserExist(username)) {
            throw new IllegalArgumentException("User already exists");
        }
        User user = new User(username, passwordHash);
        driver.create("user", user);
    }

    @Override
    public void createUser(String username, SshPublicKey publicKey) {
        if (doesUserExist(username)) {
            throw new IllegalArgumentException("User already exists");
        }
        User user = new User(username, publicKey);
        driver.create("user", user);
    }

    @Override
    public void updatePasswordHash(String username, String passwordHash) {
        if (!doesUserExist(username)) {
            throw new IllegalArgumentException("User does not exist");
        }
        driver.query("UPDATE user SET passwordHash=$password WHERE name=$name", Map.of("password", passwordHash, "name", username), User.class);
    }

    @Override
    public void updateUserKey(String username, SshPublicKey publicKey) {
        if (!doesUserExist(username)) {
            throw new IllegalArgumentException("User does not exist");
        }
        try {
            driver.query("UPDATE user SET publicKey=$publicKey WHERE name=$name", Map.of("name", username, "publicKey", SshKeyUtils.getFormattedKey(publicKey, "")), User.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //TODO: Implement this
    }
}
