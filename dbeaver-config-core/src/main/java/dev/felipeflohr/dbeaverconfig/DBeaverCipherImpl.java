package dev.felipeflohr.dbeaverconfig;

import dev.felipeflohr.dbeaverconfig.data.auth.DBeaverAuthConnectionData;
import dev.felipeflohr.dbeaverconfig.data.config.DBeaverCipherConfig;
import dev.felipeflohr.dbeaverconfig.exception.*;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

@NullMarked
@RequiredArgsConstructor
public class DBeaverCipherImpl implements DBeaverCipher {
    private final ObjectMapper objectMapper;

    @Override
    public Map<String, DBeaverAuthConnectionData> getConnectionsAuthentication(DBeaverCipherConfig config) throws DBeaverConfigException {
        byte[] credentialsFileContent = readCredentialsFileAsBytes(config.getCredentialsConfigFilePath());
        var keySpec = new SecretKeySpec(hexToBytes(config.getKey()), getSecretKeyAlgorithm());
        var ivSpec = new IvParameterSpec(hexToBytes(config.getIv()));

        String credentialsJson;
        try {
            Cipher cipher = Cipher.getInstance(getCipherTransformation());
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            byte[] decryptedBytes = cipher.doFinal(credentialsFileContent);

            final int skipAfterDecrypt = 16;
            if (decryptedBytes.length <= skipAfterDecrypt) {
                throw new DBeaverDecryptedContentTooShortException(skipAfterDecrypt, decryptedBytes.length, decryptedBytes);
            }

            credentialsJson = new String(
                    decryptedBytes,
                    skipAfterDecrypt,
                    decryptedBytes.length - skipAfterDecrypt,
                    StandardCharsets.UTF_8
            ).trim();
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException |
                 InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            throw new DBeaverFailedToDecryptContentException(e);
        }

        try {
            var type = new TypeReference<Map<String, DBeaverAuthConnectionData>>() {};
            return objectMapper.readValue(credentialsJson, type);
        } catch (JacksonException e) {
            throw new DBeaverFailedToReadCredentialsFromJsonException(e);
        }
    }

    @Override
    public Map<String, DBeaverAuthConnectionData> getConnectionsAuthentication() throws DBeaverConfigException {
        return getConnectionsAuthentication(DBeaverCipherConfig.ofDefault());
    }

    protected String getSecretKeyAlgorithm() {
        return "AES";
    }

    protected String getCipherTransformation() {
        return "AES/CBC/PKCS5Padding";
    }

    private byte[] readCredentialsFileAsBytes(Path credentialsFile) throws DBeaverConfigException {
        try {
            return Files.readAllBytes(credentialsFile);
        } catch (IOException e) {
            throw new DBeaverCredentialsFileFailedToReadException(credentialsFile, e);
        }
    }

    private byte[] hexToBytes(String hexString) {
        int len = hexString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i+1), 16));
        }
        return data;
    }
}
