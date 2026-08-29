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
public class DBeaverDriverProvider {
    private String id;
    private Map<String, DBeaverDriverDefinition> drivers = new LinkedHashMap<>();

    public Optional<DBeaverDriverDefinition> findDriver(String driverId) {
        return Optional.ofNullable(drivers.get(driverId));
    }

    public Optional<DBeaverDriverDefinition> findFirstDriverWithJars() {
        return drivers.values().stream()
                .filter(driver -> !driver.getResolvedJarPaths().isEmpty())
                .findFirst();
    }
}
