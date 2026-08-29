package dev.felipeflohr.dbeaver.data.config;

import dev.felipeflohr.dbeaverconfig.data.config.DBeaverDriverConfig;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;
import org.junitpioneer.jupiter.SetSystemProperty;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@NullMarked
class DBeaverDriverConfigTest {
    private static final String TEST_HOME = "target/test-home";
    private static final String TEST_APPDATA = "target/test-appdata";
    private static final Path DRIVERS_FILE_RELATIVE =
            Path.of("DBeaverData", "workspace6", ".metadata", ".config", "drivers.xml");
    private static final Path DRIVERS_HOME_RELATIVE = Path.of("DBeaverData", "drivers");

    @Test
    @SetSystemProperty(key = "os.name", value = "Linux")
    @SetSystemProperty(key = "user.home", value = TEST_HOME)
    void usesLinuxFolderWhenOnLinux() {
        Path base = Path.of(TEST_HOME, ".local", "share");

        DBeaverDriverConfig config = DBeaverDriverConfig.ofDefault();

        assertEquals(base.resolve(DRIVERS_FILE_RELATIVE), config.getDriversFilePath());
        assertEquals(base.resolve(DRIVERS_HOME_RELATIVE), config.getDriversHomePath());
    }

    @Test
    @SetSystemProperty(key = "os.name", value = "Mac OS X")
    @SetSystemProperty(key = "user.home", value = TEST_HOME)
    void usesMacFolderWhenOnMac() {
        Path base = Path.of(TEST_HOME, "Library");

        DBeaverDriverConfig config = DBeaverDriverConfig.ofDefault();

        assertEquals(base.resolve(DRIVERS_FILE_RELATIVE), config.getDriversFilePath());
        assertEquals(base.resolve(DRIVERS_HOME_RELATIVE), config.getDriversHomePath());
    }

    @Test
    @SetSystemProperty(key = "os.name", value = "Windows 11")
    @SetEnvironmentVariable(key = "APPDATA", value = TEST_APPDATA)
    void usesWindowsFolderWhenOnWindows() {
        Path base = Path.of(TEST_APPDATA);

        DBeaverDriverConfig config = DBeaverDriverConfig.ofDefault();

        assertEquals(base.resolve(DRIVERS_FILE_RELATIVE), config.getDriversFilePath());
        assertEquals(base.resolve(DRIVERS_HOME_RELATIVE), config.getDriversHomePath());
    }

    @Test
    @SetSystemProperty(key = "os.name", value = "Linux")
    @SetSystemProperty(key = "user.home", value = TEST_HOME)
    void doesNotThrowWhenDriversFileDoesNotExist() {
        assertDoesNotThrow(DBeaverDriverConfig::ofDefault);
    }

    @Test
    @SetSystemProperty(key = "os.name", value = "Linux")
    @SetSystemProperty(key = "user.home", value = TEST_HOME)
    void hasNoDBeaverHomeByDefault() {
        DBeaverDriverConfig config = DBeaverDriverConfig.ofDefault();

        assertNull(config.getDbeaverHomePath());
    }

    @Test
    void keepsExplicitlyProvidedPaths() {
        Path driversFilePath = Path.of("custom", "drivers.xml");
        Path driversHomePath = Path.of("custom", "drivers");
        Path dbeaverHomePath = Path.of("opt", "dbeaver");

        DBeaverDriverConfig config = new DBeaverDriverConfig(driversFilePath, driversHomePath, dbeaverHomePath);

        assertEquals(driversFilePath, config.getDriversFilePath());
        assertEquals(driversHomePath, config.getDriversHomePath());
        assertEquals(dbeaverHomePath, config.getDbeaverHomePath());
    }
}
