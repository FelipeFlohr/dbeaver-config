package dev.felipeflohr.dbeaverconfig.data.config;

import org.jspecify.annotations.NullMarked;

@NullMarked
class OSProvider {
    static OS getOS() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) return OS.WINDOWS;
        if (os.contains("mac")) return OS.MAC;
        return OS.LINUX;
    }
}
