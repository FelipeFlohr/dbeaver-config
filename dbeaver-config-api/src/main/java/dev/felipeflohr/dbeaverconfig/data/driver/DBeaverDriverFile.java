package dev.felipeflohr.dbeaverconfig.data.driver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;

@NullMarked
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DBeaverDriverFile {
    @Nullable
    private String id;

    @Nullable
    private String version;

    private String rawPath;

    @Nullable
    private Path path;

    @Nullable
    private String crc;
}
