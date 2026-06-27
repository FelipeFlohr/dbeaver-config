package dev.felipeflohr.dbeaver.data.config;

import dev.felipeflohr.dbeaverconfig.data.config.DBeaverDataSourceConfig;
import dev.felipeflohr.dbeaverconfig.exception.DBeaverConfigException;
import dev.felipeflohr.dbeaverconfig.exception.DBeaverDataSourceConfigFileDoesNotExistException;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;
import org.junitpioneer.jupiter.SetSystemProperty;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
@NullMarked
class DBeaverDataSourceConfigTest {
    private static final String TEST_HOME = "target/test-home";
    private static final String TEST_APPDATA = "target/test-appdata";
    private static final Path DBEAVER_RELATIVE = Path.of("DBeaverData", "workspace6", "General", ".dbeaver");

    @AfterEach
    void afterEach() throws IOException {
        deleteRecursively(Path.of(TEST_HOME));
        deleteRecursively(Path.of(TEST_APPDATA));
    }

    @Test
    @SetSystemProperty(key = "os.name", value = "Linux")
    @SetSystemProperty(key = "user.home", value = TEST_HOME)
    void usesLinuxFolderWhenOnLinux() throws DBeaverConfigException, IOException {
        Path expected = createConfigFileUnder(Path.of(TEST_HOME, ".local", "share"));

        DBeaverDataSourceConfig config = DBeaverDataSourceConfig.ofDefault();

        assertEquals(expected, config.getDataSourcesFilePath());
    }

    @Test
    @SetSystemProperty(key = "os.name", value = "Mac OS X")
    @SetSystemProperty(key = "user.home", value = TEST_HOME)
    void usesMacFolderWhenOnMac() throws DBeaverConfigException, IOException {
        Path expected = createConfigFileUnder(Path.of(TEST_HOME, "Library", "DBeaverData"));

        DBeaverDataSourceConfig config = DBeaverDataSourceConfig.ofDefault();

        assertEquals(expected, config.getDataSourcesFilePath());
    }

    @Test
    @SetSystemProperty(key = "os.name", value = "Windows 11")
    @SetEnvironmentVariable(key = "APPDATA", value = TEST_APPDATA)
    void usesWindowsFolderWhenOnWindows() throws DBeaverConfigException, IOException {
        Path expected = createConfigFileUnder(Path.of(TEST_APPDATA));

        DBeaverDataSourceConfig config = DBeaverDataSourceConfig.ofDefault();

        assertEquals(expected, config.getDataSourcesFilePath());
    }

    @Test
    @SetSystemProperty(key = "os.name", value = "Linux")
    @SetSystemProperty(key = "user.home", value = TEST_HOME)
    void throwsWhenConfigFileDoesNotExist() {
        DBeaverDataSourceConfigFileDoesNotExistException ex = assertThrows(
                DBeaverDataSourceConfigFileDoesNotExistException.class,
                DBeaverDataSourceConfig::ofDefault);

        Path expectedMissing = Path.of(TEST_HOME, ".local", "share")
                .resolve(DBEAVER_RELATIVE)
                .resolve("data-sources.json");
        assertEquals(expectedMissing, ex.getConfigFile());
    }

    private Path createConfigFileUnder(Path base) throws IOException {
        Path file = base.resolve(DBEAVER_RELATIVE).resolve("data-sources.json");
        Files.createDirectories(file.getParent());
        Files.createFile(file);
        return file;
    }

    private static void deleteRecursively(Path root) throws IOException {
        if (!Files.exists(root)) return;
        try (var paths = Files.walk(root)) {
            paths.sorted(Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.delete(p);
                } catch (IOException e) {
                    log.error("Failed to delete {}", p, e);
                }
            });
        }
    }
}
