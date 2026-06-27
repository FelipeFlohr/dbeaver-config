package dev.felipeflohr.dbeaverconfig;

import dev.felipeflohr.dbeaverconfig.data.auth.DBeaverAuthConnectionData;
import dev.felipeflohr.dbeaverconfig.data.config.DBeaverCipherConfig;
import dev.felipeflohr.dbeaverconfig.exception.DBeaverConfigException;
import org.jspecify.annotations.NullMarked;

import java.util.Map;

@NullMarked
public interface DBeaverCipher {
    Map<String, DBeaverAuthConnectionData> getConnectionsAuthentication(DBeaverCipherConfig config) throws DBeaverConfigException;
    Map<String, DBeaverAuthConnectionData> getConnectionsAuthentication() throws DBeaverConfigException;
}
