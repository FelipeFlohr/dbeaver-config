package dev.felipeflohr.dbeaverconfig.exception;

import lombok.Getter;
import org.jspecify.annotations.NullMarked;

import java.nio.file.Path;

@NullMarked
@Getter
public class DBeaverCredentialsConfigFileDoesNotExistException extends DBeaverConfigException {
    public final Path configFile;

    public DBeaverCredentialsConfigFileDoesNotExistException(Path configFile) {
        super("The credentials config file does not exist at \"%s\"".formatted(configFile.toAbsolutePath()));
        this.configFile = configFile;
    }
}
