package dev.felipeflohr.dbeaverconfig.data.datasource;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DBeaverConnectionType {
    private String name;

    private String description;

    @JsonProperty("auto-commit")
    private boolean autoCommit;
}
