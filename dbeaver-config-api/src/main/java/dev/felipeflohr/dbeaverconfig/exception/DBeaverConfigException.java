package dev.felipeflohr.dbeaverconfig.exception;

import org.jspecify.annotations.NullMarked;

@NullMarked
public abstract class DBeaverConfigException extends Exception {
    public DBeaverConfigException(String message) {
        super(message);
    }

    public DBeaverConfigException(String message, Throwable cause) {
        super(message, cause);
    }
}
