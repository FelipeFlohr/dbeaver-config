package dev.felipeflohr.dbeaverconfig.exception;

import lombok.Getter;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Getter
public class DBeaverDecryptedContentTooShortException extends DBeaverConfigException {
    private final int minLength;
    private final int actualLength;
    private final byte[] content;

    public DBeaverDecryptedContentTooShortException(int minLength, int actualLength, byte[] content) {
        super("Decrypted content is too short. Minimum expected: %s. Actual: %s".formatted(minLength, actualLength));
        this.minLength = minLength;
        this.actualLength = actualLength;
        this.content = content;
    }
}
