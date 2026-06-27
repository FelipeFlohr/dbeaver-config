package dev.felipeflohr.dbeaverconfig;

import dev.felipeflohr.dbeaverconfig.data.config.DBeaverDataSourceConfig;
import dev.felipeflohr.dbeaverconfig.data.datasource.DBeaverDataSources;
import dev.felipeflohr.dbeaverconfig.exception.DBeaverConfigException;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface DBeaverDataSource {
    DBeaverDataSources getDataSources(DBeaverDataSourceConfig config) throws DBeaverConfigException;
    DBeaverDataSources getDataSources() throws DBeaverConfigException;
}
