package dev.felipeflohr.dbeaverconfig;

import dev.felipeflohr.dbeaverconfig.data.config.DBeaverDataSourceConfig;
import dev.felipeflohr.dbeaverconfig.data.datasource.DBeaverConnection;
import dev.felipeflohr.dbeaverconfig.data.datasource.DBeaverDataSources;
import dev.felipeflohr.dbeaverconfig.exception.DBeaverFailedToReadDataSourcesFromJsonException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junitpioneer.jupiter.RestoreSystemProperties;
import tools.jackson.databind.ObjectMapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class DBeaverDataSourceImplTest {
    private static final Path DBEAVER_DIR = Path.of("DBeaverData", "workspace6", "General", ".dbeaver");

    private final DBeaverDataSource dataSource = new DBeaverDataSourceImpl(new ObjectMapper());

    private DBeaverDataSourceConfig config() throws Exception {
        return new DBeaverDataSourceConfig(dataSourcesResource());
    }

    private Path dataSourcesResource() throws Exception {
        return Paths.get(Objects.requireNonNull(getClass().getResource("/data-sources.json")).toURI());
    }

    @Test
    void parsesDataSources() throws Exception {
        DBeaverDataSources dataSources = dataSource.getDataSources(config());
        assertEquals(4, dataSources.getConnections().size());

        DBeaverConnection postgres = dataSources.getConnections().get("postgres-jdbc-196079609f2-53d190edd595caaa");
        assertEquals("postgresql", postgres.getProvider());
        assertEquals("postgres-jdbc", postgres.getDriver());
        assertEquals("Local Postgres", postgres.getName());
        assertEquals("jdbc:postgresql://localhost:5432/postgres", postgres.getConfiguration().getUrl());
        assertNull(postgres.getConfiguration().getHandlers());

        DBeaverConnection nvr = dataSources.getConnections().get("postgres-jdbc-19bc99acaa1-78b516adfdeb47d9");
        assertEquals("NVR - Server", nvr.getName());
        assertEquals("jdbc:postgresql://localhost:5432/nvr", nvr.getConfiguration().getUrl());
        assertNotNull(nvr.getConfiguration().getHandlers());
        assertNotNull(nvr.getConfiguration().getHandlers().getSshTunnel());
        assertTrue(nvr.getConfiguration().getHandlers().getSshTunnel().isEnabled());
        assertEquals("192.168.1.20", nvr.getConfiguration().getHandlers().getSshTunnel().getProperties().getHost());
        assertEquals(22, nvr.getConfiguration().getHandlers().getSshTunnel().getProperties().getPort());
        assertEquals("PASSWORD", nvr.getConfiguration().getHandlers().getSshTunnel().getProperties().getAuthType());

        DBeaverConnection oracle = dataSources.getConnections().get("oracle_thin-19c2146e52e-70ea308a7473e0a9");
        assertEquals("oracle", oracle.getProvider());
        assertEquals("oracle_thin", oracle.getDriver());
        assertEquals("Oracle Normal", oracle.getName());
        assertEquals("jdbc:oracle:thin:@//localhost:1521/ORCL", oracle.getConfiguration().getUrl());
        assertNotNull(oracle.getConfiguration().getHandlers());
        assertNull(oracle.getConfiguration().getHandlers().getSshTunnel());

        DBeaverConnection oracleSysdba = dataSources.getConnections().get("oracle_thin-19c2147fa39-6d55b3e69d72ab41");
        assertEquals("Oracle as SYSDBA", oracleSysdba.getName());
        assertEquals("jdbc:oracle:thin:@localhost:1521:ORCLSIDASSYSDBA", oracleSysdba.getConfiguration().getUrl());
        assertNull(oracleSysdba.getConfiguration().getHandlers());
    }

    @RestoreSystemProperties
    @Test
    void parsesDataSourcesFromDefaultLocation(@TempDir Path home) throws Exception {
        System.setProperty("user.home", home.toString());
        Path dir = home.resolve(".local").resolve("share").resolve(DBEAVER_DIR);
        Files.createDirectories(dir);
        Files.copy(dataSourcesResource(), dir.resolve("data-sources.json"));

        assertEquals(4, dataSource.getDataSources().getConnections().size());
    }

    @Test
    void throwsWhenDataSourcesCannotBeRead(@TempDir Path tempDir) throws Exception {
        Path file = Files.writeString(tempDir.resolve("invalid.json"), "this is not json");
        DBeaverDataSourceConfig config = new DBeaverDataSourceConfig(file);
        DBeaverFailedToReadDataSourcesFromJsonException exception =
                assertThrows(DBeaverFailedToReadDataSourcesFromJsonException.class, () -> dataSource.getDataSources(config));
        assertEquals(file, exception.getDataSourcesPath());
    }
}
