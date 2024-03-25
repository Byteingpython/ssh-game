package de.byteingpython.sshGame.ssh.keys;

import de.byteingpython.sshGame.config.ConfigurationProvider;
import org.apache.sshd.common.keyprovider.AbstractKeyPairProvider;
import org.apache.sshd.common.session.SessionContext;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Collections;
import java.util.Optional;

public class ConfigKeyPairProvider extends AbstractKeyPairProvider {
    private final ConfigurationProvider configurationProvider;

    public ConfigKeyPairProvider(ConfigurationProvider configurationProvider) {
        this.configurationProvider = configurationProvider;
    }

    @Override
    public Iterable<KeyPair> loadKeys(SessionContext session) throws IOException, GeneralSecurityException {
        Optional<String> publicKeyString = configurationProvider.getString("SSH_PUBLIC_KEY");
        Optional<String> privateKeyString = configurationProvider.getString("SSH_PRIVATE_KEY");
        if (publicKeyString.isEmpty()) {
            throw new IOException("No public key configured");
        }
        if (privateKeyString.isEmpty()) {
            throw new IOException("No private key configured");
        }
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyString.get().getBytes(StandardCharsets.UTF_8)));
        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyString.get().getBytes(StandardCharsets.UTF_8)));
        return Collections.singleton(new KeyPair(keyFactory.generatePublic(publicKeySpec), keyFactory.generatePrivate(privateKeySpec)));
    }
}
