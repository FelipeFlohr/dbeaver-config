package dev.felipeflohr.dbeaverconfig;

import dev.felipeflohr.dbeaverconfig.data.config.DBeaverDriverConfig;
import dev.felipeflohr.dbeaverconfig.data.driver.DBeaverDrivers;
import dev.felipeflohr.dbeaverconfig.exception.DBeaverConfigException;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface DBeaverDriver {
    DBeaverDrivers getDrivers(DBeaverDriverConfig config) throws DBeaverConfigException;
    DBeaverDrivers getDrivers() throws DBeaverConfigException;
}
