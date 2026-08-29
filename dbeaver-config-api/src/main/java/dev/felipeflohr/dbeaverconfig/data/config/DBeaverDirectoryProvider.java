package dev.felipeflohr.dbeaverconfig.data.config;

import java.nio.file.Path;

class DBeaverDirectoryProvider {
    private static final String DBEAVER_DATA_DIRECTORY_NAME = "DBeaverData";
    private static final String WORKSPACE_DIRECTORY_NAME = "workspace6";
    private static final String DRIVERS_DIRECTORY_NAME = "drivers";
    private static final String DRIVERS_FILE_NAME = "drivers.xml";

    private DBeaverDirectoryProvider() {}

    static Path getDefaultDBeaverDataDirectory() {
        Path base = switch (OSProvider.getOS()) {
            case WINDOWS -> Path.of(System.getenv("APPDATA"));
            case MAC -> Path.of(System.getProperty("user.home"), "Library");
            case LINUX -> Path.of(System.getProperty("user.home"), ".local", "share");
        };
        return base.resolve(DBEAVER_DATA_DIRECTORY_NAME);
    }

    static Path getDefaultDBeaverDirectory() {
        return getDefaultDBeaverDataDirectory()
                .resolve(Path.of(WORKSPACE_DIRECTORY_NAME, "General", ".dbeaver"));
    }

    static Path getDefaultDriversFilePath() {
        return getDefaultDBeaverDataDirectory()
                .resolve(Path.of(WORKSPACE_DIRECTORY_NAME, ".metadata", ".config", DRIVERS_FILE_NAME));
    }

    static Path getDefaultDriversHomeDirectory() {
        return getDefaultDBeaverDataDirectory().resolve(DRIVERS_DIRECTORY_NAME);
    }
}
