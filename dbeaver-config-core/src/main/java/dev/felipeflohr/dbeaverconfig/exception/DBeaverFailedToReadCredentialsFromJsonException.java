package dev.felipeflohr.dbeaverconfig.exception;

import org.jspecify.annotations.NullMarked;

@NullMarked
public class DBeaverFailedToReadCredentialsFromJsonException extends DBeaverConfigException {
    public DBeaverFailedToReadCredentialsFromJsonException(Throwable cause) {
        super("Failed to read credentials from JSON: %s".formatted(cause.getMessage()), cause);
    }
}
