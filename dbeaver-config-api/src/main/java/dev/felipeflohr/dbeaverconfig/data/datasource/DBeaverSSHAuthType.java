package dev.felipeflohr.dbeaverconfig.data.datasource;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Optional;

@NullMarked
public enum DBeaverSSHAuthType {
    PASSWORD,
    PUBLIC_KEY,
    AGENT;

    public static Optional<DBeaverSSHAuthType> fromValue(@Nullable String value) {
        if (value == null) {
            return Optional.empty();
        }
        return Arrays.stream(values())
                .filter(authType -> authType.name().equalsIgnoreCase(value.trim()))
                .findFirst();
    }
}
