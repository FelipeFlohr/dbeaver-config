package dev.felipeflohr.dbeaverconfig.data.datasource;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DBeaverConnectionConfigurationSSHTunnelHandler {
    private boolean enabled;
    private DBeaverConnectionConfigurationSSHTunnelProperties properties;
}
