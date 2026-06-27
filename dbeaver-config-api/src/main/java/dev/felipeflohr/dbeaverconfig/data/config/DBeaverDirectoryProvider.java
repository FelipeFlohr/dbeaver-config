package dev.felipeflohr.dbeaverconfig.data.config;

import java.nio.file.Path;

class DBeaverDirectoryProvider {
    private DBeaverDirectoryProvider() {}

    static Path getDefaultDBeaverDirectory() {
        Path base = switch (OSProvider.getOS()) {
            case WINDOWS -> Path.of(System.getenv("APPDATA"));
            case MAC -> Path.of(System.getProperty("user.home"), "Library", "DBeaverData");
            case LINUX -> Path.of(System.getProperty("user.home"), ".local", "share");
        };
        final Path relativePath = Path.of("DBeaverData", "workspace6", "General", ".dbeaver");
        return base.resolve(relativePath);
    }
}
