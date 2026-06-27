package dev.felipeflohr.dbeaverconfig.data.config;

import dev.felipeflohr.dbeaverconfig.exception.DBeaverConfigException;
import dev.felipeflohr.dbeaverconfig.exception.DBeaverCredentialsConfigFileDoesNotExistException;
import lombok.Data;
import org.jspecify.annotations.NullMarked;

import java.nio.file.Path;

@NullMarked
@Data
public class DBeaverCipherConfig {
    public static final String DEFAULT_KEY = "babb4a9f774ab853c96c2d653dfe544a";
    public static final String DEFAULT_IV_KEY = "00000000000000000000000000000000";

    private String key;
    private String iv;
    private Path credentialsConfigFilePath;

    public DBeaverCipherConfig(String key, String iv, Path credentialsConfigFilePath) throws DBeaverCredentialsConfigFileDoesNotExistException {
        this.key = key;
        this.iv = iv;
        this.credentialsConfigFilePath = credentialsConfigFilePath;

        if (!credentialsConfigFilePath.toFile().exists()) {
            throw new DBeaverCredentialsConfigFileDoesNotExistException(credentialsConfigFilePath);
        }
    }

    public static DBeaverCipherConfig ofDefault() throws DBeaverConfigException {
        return new DBeaverCipherConfig(
                DEFAULT_KEY,
                DEFAULT_IV_KEY,
                getDefaultCredentialsConfigFilePath()
        );
    }

    private static Path getDefaultCredentialsConfigFilePath() {
        return DBeaverDirectoryProvider.getDefaultDBeaverDirectory().resolve("credentials-config.json");
    }
}
