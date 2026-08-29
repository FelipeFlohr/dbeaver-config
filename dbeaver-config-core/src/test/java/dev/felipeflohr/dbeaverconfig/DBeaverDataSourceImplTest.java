package dev.felipeflohr.dbeaverconfig;

import dev.felipeflohr.dbeaverconfig.data.config.DBeaverDataSourceConfig;
import dev.felipeflohr.dbeaverconfig.data.datasource.DBeaverConnection;
import dev.felipeflohr.dbeaverconfig.data.datasource.DBeaverConnectionConfigurationSSHTunnelProperties;
import dev.felipeflohr.dbeaverconfig.data.datasource.DBeaverConnectionType;
import dev.felipeflohr.dbeaverconfig.data.datasource.DBeaverDataSources;
import dev.felipeflohr.dbeaverconfig.data.datasource.DBeaverSSHAuthType;
import dev.felipeflohr.dbeaverconfig.exception.DBeaverFailedToReadDataSourcesFromJsonException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junitpioneer.jupiter.RestoreSystemProperties;
import tools.jackson.databind.ObjectMapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

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
        assertEquals(7, dataSources.getConnections().size());

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

        Map<String, DBeaverConnectionType> connectionTypes = dataSources.getConnectionTypes();
        assertNotNull(connectionTypes);
        assertEquals(2, connectionTypes.size());

        DBeaverConnectionType devConnectionType = connectionTypes.get("dev");
        assertNotNull(devConnectionType);
        assertEquals("Development", devConnectionType.getName());
        assertEquals("Regular development database", devConnectionType.getDescription());
        assertTrue(devConnectionType.isAutoCommit());

        DBeaverConnectionType prodConnectionType = connectionTypes.get("prod");
        assertNotNull(prodConnectionType);
        assertEquals("Production", prodConnectionType.getName());
        assertEquals("Production database", prodConnectionType.getDescription());
        assertFalse(prodConnectionType.isAutoCommit());
    }

    @Test
    void parsesSshTunnelWithPasswordAuthentication() throws Exception {
        DBeaverConnectionConfigurationSSHTunnelProperties properties =
                sshTunnelProperties("postgres-jdbc-19bc99acaa1-78b516adfdeb47d9");

        assertEquals(Optional.of(DBeaverSSHAuthType.PASSWORD), DBeaverSSHAuthType.fromValue(properties.getAuthType()));
        assertNull(properties.getKeyPath());
        assertEquals("sshj", properties.getImplementation());
        assertEquals(0, properties.getJumpServerCount());
    }

    @Test
    void parsesSshTunnelWithPublicKeyAuthentication() throws Exception {
        DBeaverConnectionConfigurationSSHTunnelProperties properties =
                sshTunnelProperties("oracle_thin-1a04e5da314-638ba8bf46a81b7c");

        assertEquals(Optional.of(DBeaverSSHAuthType.PUBLIC_KEY), DBeaverSSHAuthType.fromValue(properties.getAuthType()));
        assertEquals("/home/felipe/.ssh/id_rsa", properties.getKeyPath());
        assertEquals("sshj", properties.getImplementation());
        assertEquals("ssh.example.com", properties.getHost());
        assertEquals(22, properties.getPort());
        assertEquals(0, properties.getJumpServerCount());
    }

    @Test
    void parsesSshTunnelWithPublicKeyWithoutPassphrase() throws Exception {
        DBeaverConnectionConfigurationSSHTunnelProperties properties =
                sshTunnelProperties("mysql8-1a04e64fc31-22331740ccca195c");

        assertEquals(Optional.of(DBeaverSSHAuthType.PUBLIC_KEY), DBeaverSSHAuthType.fromValue(properties.getAuthType()));
        assertEquals("/home/felipe/.ssh/id_ed25519", properties.getKeyPath());
        assertEquals(0, properties.getJumpServerCount());
    }

    @Test
    void parsesSshTunnelWithJumpServers() throws Exception {
        DBeaverConnectionConfigurationSSHTunnelProperties properties =
                sshTunnelProperties("postgres-jdbc-1a04e7000aa-11223344556677aa");

        assertEquals(Optional.of(DBeaverSSHAuthType.AGENT), DBeaverSSHAuthType.fromValue(properties.getAuthType()));
        assertEquals(2, properties.getJumpServerCount());
        assertEquals("bastion.example.com", properties.getHost());
        assertEquals(2222, properties.getPort());
        assertNull(properties.getKeyPath());
    }

    private DBeaverConnectionConfigurationSSHTunnelProperties sshTunnelProperties(String connectionId) throws Exception {
        DBeaverConnection connection = dataSource.getDataSources(config()).getConnections().get(connectionId);
        return Objects.requireNonNull(
                Objects.requireNonNull(connection.getConfiguration().getHandlers()).getSshTunnel()).getProperties();
    }

    @RestoreSystemProperties
    @Test
    void parsesDataSourcesFromDefaultLocation(@TempDir Path home) throws Exception {
        System.setProperty("user.home", home.toString());
        Path dir = home.resolve(".local").resolve("share").resolve(DBEAVER_DIR);
        Files.createDirectories(dir);
        Files.copy(dataSourcesResource(), dir.resolve("data-sources.json"));

        assertEquals(7, dataSource.getDataSources().getConnections().size());
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
