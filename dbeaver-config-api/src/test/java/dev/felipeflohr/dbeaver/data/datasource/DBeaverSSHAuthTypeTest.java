package dev.felipeflohr.dbeaver.data.datasource;

import dev.felipeflohr.dbeaverconfig.data.datasource.DBeaverSSHAuthType;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@NullMarked
class DBeaverSSHAuthTypeTest {
    @Test
    void resolvesKnownValues() {
        assertEquals(Optional.of(DBeaverSSHAuthType.PASSWORD), DBeaverSSHAuthType.fromValue("PASSWORD"));
        assertEquals(Optional.of(DBeaverSSHAuthType.PUBLIC_KEY), DBeaverSSHAuthType.fromValue("PUBLIC_KEY"));
        assertEquals(Optional.of(DBeaverSSHAuthType.AGENT), DBeaverSSHAuthType.fromValue("AGENT"));
    }

    @Test
    void ignoresCaseAndSurroundingWhitespace() {
        assertEquals(Optional.of(DBeaverSSHAuthType.PUBLIC_KEY), DBeaverSSHAuthType.fromValue("public_key"));
        assertEquals(Optional.of(DBeaverSSHAuthType.PUBLIC_KEY), DBeaverSSHAuthType.fromValue("  PUBLIC_KEY  "));
    }

    @Test
    void returnsEmptyForUnknownValue() {
        assertEquals(Optional.empty(), DBeaverSSHAuthType.fromValue("KERBEROS"));
        assertEquals(Optional.empty(), DBeaverSSHAuthType.fromValue(""));
    }

    @Test
    void returnsEmptyForNull() {
        assertEquals(Optional.empty(), DBeaverSSHAuthType.fromValue(null));
    }
}
