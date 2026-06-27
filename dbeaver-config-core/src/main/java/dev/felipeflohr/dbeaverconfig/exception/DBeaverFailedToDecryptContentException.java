package dev.felipeflohr.dbeaverconfig.exception;

public class DBeaverFailedToDecryptContentException extends DBeaverConfigException {
    public DBeaverFailedToDecryptContentException(Throwable cause) {
        super("Failed to decrypt content: %s".formatted(cause.getMessage()), cause);
    }
}
