package dev.felipeflohr.dbeaverconfig;

import dev.felipeflohr.dbeaverconfig.data.auth.DBeaverAuthConnectionData;
import dev.felipeflohr.dbeaverconfig.data.config.DBeaverCipherConfig;
import dev.felipeflohr.dbeaverconfig.exception.DBeaverCredentialsFileFailedToReadException;
import dev.felipeflohr.dbeaverconfig.exception.DBeaverDecryptedContentTooShortException;
import dev.felipeflohr.dbeaverconfig.exception.DBeaverFailedToDecryptContentException;
import dev.felipeflohr.dbeaverconfig.exception.DBeaverFailedToReadCredentialsFromJsonException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junitpioneer.jupiter.RestoreSystemProperties;
import tools.jackson.databind.ObjectMapper;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class DBeaverCipherImplTest {
    private static final Path DBEAVER_DIR = Path.of("DBeaverData", "workspace6", "General", ".dbeaver");

    private final DBeaverCipher cipher = new DBeaverCipherImpl(new ObjectMapper());

    @Test
    void decryptsConnectionsAuthentication() throws Exception {
        Map<String, DBeaverAuthConnectionData> connections = cipher.getConnectionsAuthentication(config(credentialsResource()));

        DBeaverAuthConnectionData postgres = connections.get("postgres-jdbc-196079609f2-53d190edd595caaa");
        assertEquals("admin", postgres.getConnection().getUser());
        assertEquals("admin", postgres.getConnection().getPassword());
        assertNull(postgres.getConnection().getOracleLogonAs());
        assertNull(postgres.getSshTunnel());

        DBeaverAuthConnectionData postgresSsh = connections.get("postgres-jdbc-19bc99acaa1-78b516adfdeb47d9");
        assertEquals("admin", postgresSsh.getConnection().getUser());
        assertNull(postgresSsh.getConnection().getPassword());
        assertNull(postgresSsh.getConnection().getOracleLogonAs());
        assertNotNull(postgresSsh.getSshTunnel());
        assertEquals("username", postgresSsh.getSshTunnel().getUser());
        assertEquals("password", postgresSsh.getSshTunnel().getPassword());

        DBeaverAuthConnectionData oracle = connections.get("oracle_thin-19c2146e52e-70ea308a7473e0a9");
        assertEquals("username", oracle.getConnection().getUser());
        assertEquals("password", oracle.getConnection().getPassword());
        assertNull(oracle.getConnection().getOracleLogonAs());

        DBeaverAuthConnectionData oracleSysdba = connections.get("oracle_thin-19c2147fa39-6d55b3e69d72ab41");
        assertEquals("system", oracleSysdba.getConnection().getUser());
        assertEquals("passwordaqui", oracleSysdba.getConnection().getPassword());
        assertEquals("sysdba", oracleSysdba.getConnection().getOracleLogonAs());
    }

    @RestoreSystemProperties
    @Test
    void decryptsConnectionsAuthenticationFromDefaultLocation(@TempDir Path home) throws Exception {
        System.setProperty("user.home", home.toString());
        Path dir = home.resolve(".local").resolve("share").resolve(DBEAVER_DIR);
        Files.createDirectories(dir);
        Files.copy(credentialsResource(), dir.resolve("data-sources.json"));

        assertEquals(4, cipher.getConnectionsAuthentication().size());
    }

    @Test
    void throwsWhenCredentialsFileCannotBeRead(@TempDir Path directory) throws Exception {
        DBeaverCipherConfig config = config(directory);
        assertThrows(DBeaverCredentialsFileFailedToReadException.class, () -> cipher.getConnectionsAuthentication(config));
    }

    @Test
    void throwsWhenContentCannotBeDecrypted(@TempDir Path tempDir) throws Exception {
        Path file = Files.writeString(tempDir.resolve("invalid.bin"), "this-is-not-aes-encrypted");
        DBeaverCipherConfig config = config(file);
        assertThrows(DBeaverFailedToDecryptContentException.class, () -> cipher.getConnectionsAuthentication(config));
    }

    @Test
    void throwsWhenDecryptedContentIsTooShort(@TempDir Path tempDir) throws Exception {
        DBeaverCipherConfig config = config(encryptedFile(tempDir, "short"));
        assertThrows(DBeaverDecryptedContentTooShortException.class, () -> cipher.getConnectionsAuthentication(config));
    }

    @Test
    void throwsWhenDecryptedContentIsNotValidJson(@TempDir Path tempDir) throws Exception {
        DBeaverCipherConfig config = config(encryptedFile(tempDir, "0123456789abcdefnotvalidjson"));
        assertThrows(DBeaverFailedToReadCredentialsFromJsonException.class, () -> cipher.getConnectionsAuthentication(config));
    }

    private DBeaverCipherConfig config(Path path) throws Exception {
        return new DBeaverCipherConfig(DBeaverCipherConfig.DEFAULT_KEY, DBeaverCipherConfig.DEFAULT_IV_KEY, path);
    }

    private Path credentialsResource() throws Exception {
        return Paths.get(Objects.requireNonNull(getClass().getResource("/credentials-config.json")).toURI());
    }

    private Path encryptedFile(Path dir, String plaintext) throws Exception {
        Cipher aes = Cipher.getInstance("AES/CBC/PKCS5Padding");
        aes.init(Cipher.ENCRYPT_MODE,
                new SecretKeySpec(hexToBytes(DBeaverCipherConfig.DEFAULT_KEY), "AES"),
                new IvParameterSpec(hexToBytes(DBeaverCipherConfig.DEFAULT_IV_KEY)));
        return Files.write(dir.resolve("encrypted.bin"), aes.doFinal(plaintext.getBytes(StandardCharsets.UTF_8)));
    }

    private byte[] hexToBytes(String hex) {
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
        }
        return bytes;
    }
}
