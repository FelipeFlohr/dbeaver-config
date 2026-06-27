package dev.felipeflohr.dbeaverconfig.data.datasource;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DBeaverConnectionConfiguration {
    private String url;

    @Nullable
    private DBeaverConnectionConfigurationHandlers handlers;
}
