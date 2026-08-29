package dev.felipeflohr.dbeaverconfig.data.driver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@NullMarked
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DBeaverDriverDefinition {
    private String id;

    @Nullable
    private String name;

    @Nullable
    private String driverClassName;

    @Nullable
    private String sampleUrl;

    @Nullable
    private String port;

    @Nullable
    private String defaultDatabase;

    @Nullable
    private String defaultUser;

    @Nullable
    private String description;

    private boolean custom;
    private List<DBeaverDriverLibrary> libraries = new ArrayList<>();

    public List<Path> getResolvedJarPaths() {
        return libraries.stream()
                .filter(DBeaverDriverLibrary::isJar)
                .flatMap(library -> library.getFiles().stream())
                .map(DBeaverDriverFile::getPath)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    public List<String> getUnresolvedJarRawPaths() {
        return libraries.stream()
                .filter(DBeaverDriverLibrary::isJar)
                .flatMap(library -> library.getFiles().stream())
                .filter(file -> file.getPath() == null)
                .map(DBeaverDriverFile::getRawPath)
                .distinct()
                .toList();
    }
}
