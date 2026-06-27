package dev.felipeflohr.dbeaverconfig;

import dev.felipeflohr.dbeaverconfig.data.config.DBeaverDataSourceConfig;
import dev.felipeflohr.dbeaverconfig.data.datasource.DBeaverDataSources;
import dev.felipeflohr.dbeaverconfig.exception.DBeaverConfigException;
import dev.felipeflohr.dbeaverconfig.exception.DBeaverFailedToReadDataSourcesFromJsonException;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@NullMarked
@RequiredArgsConstructor
public class DBeaverDataSourceImpl implements DBeaverDataSource {
    private final ObjectMapper objectMapper;

    @Override
    public DBeaverDataSources getDataSources(DBeaverDataSourceConfig config) throws DBeaverConfigException {
        try {
            return objectMapper.readValue(config.getDataSourcesFilePath(), DBeaverDataSources.class);
        } catch (JacksonException e) {
            throw new DBeaverFailedToReadDataSourcesFromJsonException(config.getDataSourcesFilePath(), e);
        }
    }

    @Override
    public DBeaverDataSources getDataSources() throws DBeaverConfigException {
        return getDataSources(DBeaverDataSourceConfig.ofDefault());
    }
}
