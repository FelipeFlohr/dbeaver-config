package dev.felipeflohr.dbeaverconfig;

import dev.felipeflohr.dbeaverconfig.data.config.DBeaverDriverConfig;
import dev.felipeflohr.dbeaverconfig.data.driver.DBeaverDriverDefinition;
import dev.felipeflohr.dbeaverconfig.data.driver.DBeaverDriverLibrary;
import dev.felipeflohr.dbeaverconfig.data.driver.DBeaverDriverProvider;
import dev.felipeflohr.dbeaverconfig.data.driver.DBeaverDrivers;
import dev.felipeflohr.dbeaverconfig.exception.DBeaverConfigException;
import dev.felipeflohr.dbeaverconfig.exception.DBeaverFailedToReadDriversFromXmlException;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@NullMarked
class DBeaverDriverImplTest {
    private static final String FIXTURE_RESOURCE = "/drivers.xml";
    private static final String POSTGRES_PROVIDER = "postgresql";
    private static final String ORACLE_PROVIDER = "oracle";

    @TempDir
    Path tempDir;

    private DBeaverDriverImpl driverReader;
    private Path driversFilePath;
    private Path driversHomePath;
    private Path dbeaverHomePath;

    @BeforeEach
    void beforeEach() throws IOException {
        driverReader = new DBeaverDriverImpl();
        driversFilePath = copyFixtureTo(tempDir.resolve("drivers.xml"));
        driversHomePath = tempDir.resolve("drivers");
        dbeaverHomePath = tempDir.resolve("dbeaver");
    }

    @Test
    void parsesEveryProviderAndDriver() throws DBeaverConfigException {
        DBeaverDrivers drivers = driverReader.getDrivers(configWithoutDBeaverHome());

        assertEquals(List.of(POSTGRES_PROVIDER, ORACLE_PROVIDER), List.copyOf(drivers.getProviders().keySet()));
        assertTrue(drivers.findProvider(POSTGRES_PROVIDER).isPresent());
        assertTrue(drivers.findProvider("mysql").isEmpty());
        assertFalse(drivers.isEmpty());
    }

    @Test
    void parsesDriverAttributes() throws DBeaverConfigException {
        DBeaverDriverDefinition driver = postgresDriver(driverReader.getDrivers(configWithoutDBeaverHome()));

        assertEquals("postgres-jdbc", driver.getId());
        assertEquals("PostgreSQL", driver.getName());
        assertEquals("org.postgresql.Driver", driver.getDriverClassName());
        assertEquals("jdbc:postgresql://{host}[:{port}]/[{database}]", driver.getSampleUrl());
        assertEquals("5432", driver.getPort());
        assertEquals("postgres", driver.getDefaultDatabase());
        assertEquals("postgres", driver.getDefaultUser());
        assertEquals("PostgreSQL standard driver", driver.getDescription());
        assertFalse(driver.isCustom());
    }

    @Test
    void resolvesDriversHomePlaceholderAgainstConfiguredDriversHome() throws DBeaverConfigException {
        DBeaverDriverDefinition driver = postgresDriver(driverReader.getDrivers(configWithoutDBeaverHome()));

        assertEquals(
                List.of(
                        mavenJar("org.postgresql", "postgresql-42.7.13.jar"),
                        mavenJar("org.checkerframework", "checker-qual-3.55.1.jar"),
                        mavenJar("net.postgis", "postgis-jdbc-2.5.0.jar")
                ),
                driver.getResolvedJarPaths()
        );
    }

    @Test
    void excludesNonJarLibrariesFromResolvedJarPaths() throws DBeaverConfigException {
        DBeaverDriverDefinition driver = postgresDriver(driverReader.getDrivers(configWithoutDBeaverHome()));

        assertEquals(3, driver.getLibraries().size());
        assertTrue(driver.getResolvedJarPaths().stream().noneMatch(path -> path.toString().endsWith("pg.txt")));
    }

    @Test
    void parsesLibraryFlags() throws DBeaverConfigException {
        List<DBeaverDriverLibrary> libraries = postgresDriver(driverReader.getDrivers(configWithoutDBeaverHome()))
                .getLibraries();

        assertFalse(libraries.get(0).isIgnoreDependencies());
        assertTrue(libraries.get(1).isIgnoreDependencies());
        assertEquals("2.5.0", libraries.get(1).getVersion());
        assertFalse(libraries.get(2).isJar());
    }

    @Test
    void parsesFileMetadata() throws DBeaverConfigException {
        var file = postgresDriver(driverReader.getDrivers(configWithoutDBeaverHome()))
                .getLibraries().get(0).getFiles().get(0);

        assertEquals("org.postgresql:postgresql:RELEASE", file.getId());
        assertEquals("42.7.13", file.getVersion());
        assertEquals("5099f19d", file.getCrc());
        assertEquals(
                "${drivers_home}/maven/maven-central/org.postgresql/postgresql-42.7.13.jar",
                file.getRawPath()
        );
    }

    @Test
    void leavesDBeaverHomePlaceholderUnresolvedWhenNotConfigured() throws DBeaverConfigException {
        DBeaverDriverDefinition driver = oracleDriver(driverReader.getDrivers(configWithoutDBeaverHome()));

        assertTrue(driver.getResolvedJarPaths().isEmpty());
        assertEquals(
                List.of(
                        "${dbeaver_home}/drivers/oracle/ojdbc11.jar",
                        "${unknown_home}/ojdbc11.jar"
                ),
                driver.getUnresolvedJarRawPaths()
        );
    }

    @Test
    void resolvesDBeaverHomePlaceholderWhenConfigured() throws DBeaverConfigException {
        DBeaverDriverDefinition driver = oracleDriver(driverReader.getDrivers(configWithDBeaverHome()));

        assertEquals(
                List.of(dbeaverHomePath.resolve("drivers").resolve("oracle").resolve("ojdbc11.jar")),
                driver.getResolvedJarPaths()
        );
    }

    @Test
    void ignoresUnknownPlaceholders() throws DBeaverConfigException {
        DBeaverDriverDefinition driver = oracleDriver(driverReader.getDrivers(configWithDBeaverHome()));

        assertEquals(List.of("${unknown_home}/ojdbc11.jar"), driver.getUnresolvedJarRawPaths());
    }

    @Test
    void resolvesAbsolutePathsWithoutPlaceholder() throws DBeaverConfigException, IOException {
        Path localJar = tempDir.resolve("local-driver.jar").toAbsolutePath();
        Path customDriversFilePath = writeDriversFile(tempDir.resolve("custom-drivers.xml"), localJar.toString());
        DBeaverDriverConfig config = new DBeaverDriverConfig(customDriversFilePath, driversHomePath);

        DBeaverDriverDefinition driver = driverReader.getDrivers(config)
                .findProvider("custom").orElseThrow()
                .findDriver("custom-driver").orElseThrow();

        assertEquals(List.of(localJar), driver.getResolvedJarPaths());
    }

    @Test
    void ignoresRelativePathsWithoutPlaceholder() throws DBeaverConfigException, IOException {
        Path customDriversFilePath = writeDriversFile(tempDir.resolve("relative-drivers.xml"), "lib/local-driver.jar");
        DBeaverDriverConfig config = new DBeaverDriverConfig(customDriversFilePath, driversHomePath);

        DBeaverDriverDefinition driver = driverReader.getDrivers(config)
                .findProvider("custom").orElseThrow()
                .findDriver("custom-driver").orElseThrow();

        assertTrue(driver.getResolvedJarPaths().isEmpty());
        assertEquals(List.of("lib/local-driver.jar"), driver.getUnresolvedJarRawPaths());
    }

    @Test
    void skipsDriversWithoutAnId() throws DBeaverConfigException {
        DBeaverDriverProvider provider = driverReader.getDrivers(configWithoutDBeaverHome())
                .findProvider(ORACLE_PROVIDER).orElseThrow();

        assertEquals(List.of("oracle_thin"), List.copyOf(provider.getDrivers().keySet()));
    }

    @Test
    void findsFirstDriverWithJars() throws DBeaverConfigException {
        DBeaverDriverProvider provider = driverReader.getDrivers(configWithoutDBeaverHome())
                .findProvider(POSTGRES_PROVIDER).orElseThrow();

        assertEquals("postgres-jdbc", provider.findFirstDriverWithJars().orElseThrow().getId());
    }

    @Test
    void returnsNoDriversWhenFileDoesNotExist() throws DBeaverConfigException {
        DBeaverDriverConfig config = new DBeaverDriverConfig(tempDir.resolve("missing.xml"), driversHomePath);

        DBeaverDrivers drivers = driverReader.getDrivers(config);

        assertTrue(drivers.isEmpty());
        assertTrue(drivers.findProvider(POSTGRES_PROVIDER).isEmpty());
    }

    @Test
    void throwsWhenXmlIsMalformed() throws IOException {
        Path malformedFilePath = tempDir.resolve("malformed.xml");
        Files.writeString(malformedFilePath, "<drivers><provider id=\"postgresql\"></drivers>", StandardCharsets.UTF_8);
        DBeaverDriverConfig config = new DBeaverDriverConfig(malformedFilePath, driversHomePath);

        DBeaverFailedToReadDriversFromXmlException exception = assertThrows(
                DBeaverFailedToReadDriversFromXmlException.class,
                () -> driverReader.getDrivers(config));

        assertEquals(malformedFilePath, exception.getDriversPath());
    }

    @Test
    void doesNotResolveExternalEntities() throws IOException {
        Path secretFilePath = tempDir.resolve("secret.txt");
        Files.writeString(secretFilePath, "top-secret", StandardCharsets.UTF_8);
        Path xxeFilePath = tempDir.resolve("xxe.xml");
        Files.writeString(xxeFilePath, """
                <?xml version="1.0" encoding="UTF-8"?>
                <!DOCTYPE drivers [<!ENTITY secret SYSTEM "%s">]>
                <drivers>
                    <provider id="&secret;"/>
                </drivers>
                """.formatted(secretFilePath.toUri()), StandardCharsets.UTF_8);
        DBeaverDriverConfig config = new DBeaverDriverConfig(xxeFilePath, driversHomePath);

        assertThrows(DBeaverFailedToReadDriversFromXmlException.class, () -> driverReader.getDrivers(config));
    }

    @Test
    void usesRawPathWhenPathIsNotResolvable() throws DBeaverConfigException {
        var file = oracleDriver(driverReader.getDrivers(configWithoutDBeaverHome()))
                .getLibraries().get(0).getFiles().get(1);

        assertNull(file.getPath());
        assertEquals("${unknown_home}/ojdbc11.jar", file.getRawPath());
    }

    private DBeaverDriverConfig configWithoutDBeaverHome() {
        return new DBeaverDriverConfig(driversFilePath, driversHomePath);
    }

    private DBeaverDriverConfig configWithDBeaverHome() {
        return new DBeaverDriverConfig(driversFilePath, driversHomePath, dbeaverHomePath);
    }

    private DBeaverDriverDefinition postgresDriver(DBeaverDrivers drivers) {
        return drivers.findProvider(POSTGRES_PROVIDER).orElseThrow()
                .findDriver("postgres-jdbc").orElseThrow();
    }

    private DBeaverDriverDefinition oracleDriver(DBeaverDrivers drivers) {
        return drivers.findProvider(ORACLE_PROVIDER).orElseThrow()
                .findDriver("oracle_thin").orElseThrow();
    }

    private Path mavenJar(String groupId, String jarName) {
        return driversHomePath.resolve("maven").resolve("maven-central").resolve(groupId).resolve(jarName);
    }

    private Path writeDriversFile(Path target, String libraryFilePath) throws IOException {
        Files.writeString(target, """
                <?xml version="1.0" encoding="UTF-8"?>
                <drivers>
                    <provider id="custom">
                        <driver id="custom-driver" name="Custom" class="com.example.Driver" custom="true">
                            <library type="jar" path="custom" custom="true">
                                <file id="custom-file" path="%s"/>
                            </library>
                        </driver>
                    </provider>
                </drivers>
                """.formatted(libraryFilePath), StandardCharsets.UTF_8);
        return target;
    }

    private Path copyFixtureTo(Path target) throws IOException {
        try (InputStream fixture = getClass().getResourceAsStream(FIXTURE_RESOURCE)) {
            if (fixture == null) {
                throw new IOException("Missing test fixture " + FIXTURE_RESOURCE);
            }
            Files.copy(fixture, target, StandardCopyOption.REPLACE_EXISTING);
        }
        return target;
    }
}
