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
public class DBeaverAuthConnectionData {
    @JsonProperty("#connection")
    private DBeaverAuthConnection connection;

    @Nullable
    @JsonProperty("network/ssh_tunnel")
    private DBeaverAuthSSHTunnel sshTunnel;
}
