package dev.felipeflohr.dbeaverconfig.data.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
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
public class DBeaverAuthConnection {
    private String user;

    @Nullable
    private String password;

    @Nullable
    @JsonProperty("oracle.logon-as")
    private String oracleLogonAs;
}
