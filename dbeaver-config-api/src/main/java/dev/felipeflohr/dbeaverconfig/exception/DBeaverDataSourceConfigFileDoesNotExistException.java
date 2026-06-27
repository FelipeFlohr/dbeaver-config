package dev.felipeflohr.dbeaverconfig.exception;

import lombok.Getter;
import org.jspecify.annotations.NullMarked;

import java.nio.file.Path;

@NullMarked
@Getter
public class DBeaverDataSourceConfigFileDoesNotExistException extends DBeaverConfigException {
    private final Path configFile;

    public DBeaverDataSourceConfigFileDoesNotExistException(Path configFile) {
        super("The credentials config file does not exist at \"%s\"".formatted(configFile.toAbsolutePath()));
        this.configFile = configFile;
    }
}
