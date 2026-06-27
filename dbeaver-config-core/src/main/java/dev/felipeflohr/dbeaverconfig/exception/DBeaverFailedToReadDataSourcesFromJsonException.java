package dev.felipeflohr.dbeaverconfig.exception;

import lombok.Getter;
import org.jspecify.annotations.NullMarked;

import java.nio.file.Path;

@NullMarked
@Getter
public class DBeaverFailedToReadDataSourcesFromJsonException extends DBeaverConfigException {
    private final Path dataSourcesPath;

    public DBeaverFailedToReadDataSourcesFromJsonException(Path dataSourcesPath, Throwable cause) {
        super("Failed to read data sources from JSON: %s".formatted(cause.getMessage()), cause);
        this.dataSourcesPath = dataSourcesPath;
    }
}
