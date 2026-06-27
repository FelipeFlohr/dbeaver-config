package dev.felipeflohr.dbeaverconfig.exception;

import lombok.Getter;
import org.jspecify.annotations.NullMarked;

import java.nio.file.Path;

@NullMarked
@Getter
public class DBeaverCredentialsFileFailedToReadException extends DBeaverConfigException {
    private final Path credentialsFile;

    public DBeaverCredentialsFileFailedToReadException(Path credentialsFile, Throwable cause) {
        super("Failed to read credentials file: \"%s\"".formatted(credentialsFile.toAbsolutePath()), cause);
        this.credentialsFile = credentialsFile;
    }
}
