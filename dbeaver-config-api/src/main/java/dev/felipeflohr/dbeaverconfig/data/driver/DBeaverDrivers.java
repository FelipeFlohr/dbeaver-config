package dev.felipeflohr.dbeaverconfig.data.driver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NullMarked;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@NullMarked
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DBeaverDrivers {
    private Map<String, DBeaverDriverProvider> providers = new LinkedHashMap<>();

    public Optional<DBeaverDriverProvider> findProvider(String providerId) {
        return Optional.ofNullable(providers.get(providerId));
    }

    public boolean isEmpty() {
        return providers.isEmpty();
    }
}
