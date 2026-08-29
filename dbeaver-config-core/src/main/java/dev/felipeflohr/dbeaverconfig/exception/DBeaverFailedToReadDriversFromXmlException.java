package dev.felipeflohr.dbeaverconfig.exception;

import lombok.Getter;
import org.jspecify.annotations.NullMarked;

import java.nio.file.Path;

@NullMarked
@Getter
public class DBeaverFailedToReadDriversFromXmlException extends DBeaverConfigException {
    private final Path driversPath;

    public DBeaverFailedToReadDriversFromXmlException(Path driversPath, Throwable cause) {
        super("Failed to read drivers from XML: %s".formatted(cause.getMessage()), cause);
        this.driversPath = driversPath;
    }
}
