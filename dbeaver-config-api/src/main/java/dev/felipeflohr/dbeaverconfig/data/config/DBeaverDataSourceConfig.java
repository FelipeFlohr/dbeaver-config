package dev.felipeflohr.dbeaverconfig.data.config;

import dev.felipeflohr.dbeaverconfig.exception.DBeaverDataSourceConfigFileDoesNotExistException;
import lombok.Data;
import org.jspecify.annotations.NullMarked;

import java.nio.file.Path;

@NullMarked
@Data
public class DBeaverDataSourceConfig {
    private Path dataSourcesFilePath;

    public DBeaverDataSourceConfig(Path dataSourcesFilePath) throws DBeaverDataSourceConfigFileDoesNotExistException {
        this.dataSourcesFilePath = dataSourcesFilePath;

        if (!dataSourcesFilePath.toFile().exists()) {
            throw new DBeaverDataSourceConfigFileDoesNotExistException(dataSourcesFilePath);
        }
    }

    public static DBeaverDataSourceConfig ofDefault() throws DBeaverDataSourceConfigFileDoesNotExistException {
        Path dataSourcesPath = DBeaverDirectoryProvider.getDefaultDBeaverDirectory().resolve("data-sources.json");
        return new DBeaverDataSourceConfig(dataSourcesPath);
    }
}
