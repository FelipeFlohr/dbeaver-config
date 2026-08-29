package dev.felipeflohr.dbeaverconfig.data.config;

import lombok.Data;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;

@NullMarked
@Data
public class DBeaverDriverConfig {
    private Path driversFilePath;
    private Path driversHomePath;

    @Nullable
    private Path dbeaverHomePath;

    public DBeaverDriverConfig(Path driversFilePath, Path driversHomePath) {
        this(driversFilePath, driversHomePath, null);
    }

    public DBeaverDriverConfig(Path driversFilePath, Path driversHomePath, @Nullable Path dbeaverHomePath) {
        this.driversFilePath = driversFilePath;
        this.driversHomePath = driversHomePath;
        this.dbeaverHomePath = dbeaverHomePath;
    }

    public static DBeaverDriverConfig ofDefault() {
        return new DBeaverDriverConfig(
                DBeaverDirectoryProvider.getDefaultDriversFilePath(),
                DBeaverDirectoryProvider.getDefaultDriversHomeDirectory()
        );
    }
}
