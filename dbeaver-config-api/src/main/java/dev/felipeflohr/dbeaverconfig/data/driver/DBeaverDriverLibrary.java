package dev.felipeflohr.dbeaverconfig.data.driver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@NullMarked
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DBeaverDriverLibrary {
    private static final String JAR_TYPE = "jar";

    @Nullable
    private String type;

    @Nullable
    private String path;

    @Nullable
    private String version;

    private boolean custom;
    private boolean ignoreDependencies;
    private List<DBeaverDriverFile> files = new ArrayList<>();

    public boolean isJar() {
        return JAR_TYPE.equalsIgnoreCase(type);
    }
}
