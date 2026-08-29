package dev.felipeflohr.dbeaverconfig;

import dev.felipeflohr.dbeaverconfig.data.config.DBeaverDriverConfig;
import dev.felipeflohr.dbeaverconfig.data.driver.DBeaverDriverDefinition;
import dev.felipeflohr.dbeaverconfig.data.driver.DBeaverDriverFile;
import dev.felipeflohr.dbeaverconfig.data.driver.DBeaverDriverLibrary;
import dev.felipeflohr.dbeaverconfig.data.driver.DBeaverDriverProvider;
import dev.felipeflohr.dbeaverconfig.data.driver.DBeaverDrivers;
import dev.felipeflohr.dbeaverconfig.exception.DBeaverConfigException;
import dev.felipeflohr.dbeaverconfig.exception.DBeaverFailedToReadDriversFromXmlException;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@NullMarked
public class DBeaverDriverImpl implements DBeaverDriver {
    private static final String PROVIDER_ELEMENT = "provider";
    private static final String DRIVER_ELEMENT = "driver";
    private static final String LIBRARY_ELEMENT = "library";
    private static final String FILE_ELEMENT = "file";

    private static final String ID_ATTRIBUTE = "id";
    private static final String NAME_ATTRIBUTE = "name";
    private static final String CLASS_ATTRIBUTE = "class";
    private static final String URL_ATTRIBUTE = "url";
    private static final String PORT_ATTRIBUTE = "port";
    private static final String DEFAULT_DATABASE_ATTRIBUTE = "defaultDatabase";
    private static final String DEFAULT_USER_ATTRIBUTE = "defaultUser";
    private static final String DESCRIPTION_ATTRIBUTE = "description";
    private static final String CUSTOM_ATTRIBUTE = "custom";
    private static final String TYPE_ATTRIBUTE = "type";
    private static final String PATH_ATTRIBUTE = "path";
    private static final String VERSION_ATTRIBUTE = "version";
    private static final String CRC_ATTRIBUTE = "crc";
    private static final String IGNORE_DEPENDENCIES_ATTRIBUTE = "ignore-dependencies";

    private static final String DRIVERS_HOME_PLACEHOLDER = "${drivers_home}";
    private static final String DBEAVER_HOME_PLACEHOLDER = "${dbeaver_home}";
    private static final String PLACEHOLDER_PREFIX = "${";
    private static final String PATH_SEPARATOR = "/";
    private static final String TRUE_VALUE = "true";

    @Override
    public DBeaverDrivers getDrivers(DBeaverDriverConfig config) throws DBeaverConfigException {
        Path driversFilePath = config.getDriversFilePath();
        if (!Files.isRegularFile(driversFilePath)) {
            log.debug("Drivers file {} does not exist. Returning no drivers.", driversFilePath);
            return new DBeaverDrivers(new LinkedHashMap<>());
        }

        XMLStreamReader reader = null;
        try (InputStream inputStream = Files.newInputStream(driversFilePath)) {
            reader = createReader(inputStream);
            return read(reader, config);
        } catch (IOException | XMLStreamException e) {
            throw new DBeaverFailedToReadDriversFromXmlException(driversFilePath, e);
        } finally {
            closeQuietly(reader);
        }
    }

    @Override
    public DBeaverDrivers getDrivers() throws DBeaverConfigException {
        return getDrivers(DBeaverDriverConfig.ofDefault());
    }

    private XMLStreamReader createReader(InputStream inputStream) throws XMLStreamException {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        return factory.createXMLStreamReader(inputStream);
    }

    private DBeaverDrivers read(XMLStreamReader reader, DBeaverDriverConfig config) throws XMLStreamException {
        Map<String, DBeaverDriverProvider> providers = new LinkedHashMap<>();
        DBeaverDriverProvider currentProvider = null;
        DBeaverDriverDefinition currentDriver = null;
        DBeaverDriverLibrary currentLibrary = null;

        while (reader.hasNext()) {
            if (reader.next() != XMLStreamConstants.START_ELEMENT) {
                continue;
            }

            switch (reader.getLocalName()) {
                case PROVIDER_ELEMENT -> {
                    currentDriver = null;
                    currentLibrary = null;
                    currentProvider = readProvider(reader);
                    if (currentProvider != null) {
                        providers.put(currentProvider.getId(), currentProvider);
                    }
                }
                case DRIVER_ELEMENT -> {
                    currentLibrary = null;
                    currentDriver = readDriver(reader);
                    if (currentProvider != null && currentDriver != null) {
                        currentProvider.getDrivers().put(currentDriver.getId(), currentDriver);
                    }
                }
                case LIBRARY_ELEMENT -> {
                    currentLibrary = readLibrary(reader);
                    if (currentDriver != null) {
                        currentDriver.getLibraries().add(currentLibrary);
                    }
                }
                case FILE_ELEMENT -> {
                    DBeaverDriverFile file = readFile(reader, config);
                    if (currentLibrary != null && file != null) {
                        currentLibrary.getFiles().add(file);
                    }
                }
            }
        }

        return new DBeaverDrivers(providers);
    }

    @Nullable
    private DBeaverDriverProvider readProvider(XMLStreamReader reader) {
        String id = attribute(reader, ID_ATTRIBUTE);
        if (id == null) {
            return null;
        }
        return new DBeaverDriverProvider(id, new LinkedHashMap<>());
    }

    @Nullable
    private DBeaverDriverDefinition readDriver(XMLStreamReader reader) {
        String id = attribute(reader, ID_ATTRIBUTE);
        if (id == null) {
            return null;
        }

        DBeaverDriverDefinition driver = new DBeaverDriverDefinition();
        driver.setId(id);
        driver.setName(attribute(reader, NAME_ATTRIBUTE));
        driver.setDriverClassName(attribute(reader, CLASS_ATTRIBUTE));
        driver.setSampleUrl(attribute(reader, URL_ATTRIBUTE));
        driver.setPort(attribute(reader, PORT_ATTRIBUTE));
        driver.setDefaultDatabase(attribute(reader, DEFAULT_DATABASE_ATTRIBUTE));
        driver.setDefaultUser(attribute(reader, DEFAULT_USER_ATTRIBUTE));
        driver.setDescription(attribute(reader, DESCRIPTION_ATTRIBUTE));
        driver.setCustom(booleanAttribute(reader, CUSTOM_ATTRIBUTE));
        return driver;
    }

    private DBeaverDriverLibrary readLibrary(XMLStreamReader reader) {
        DBeaverDriverLibrary library = new DBeaverDriverLibrary();
        library.setType(attribute(reader, TYPE_ATTRIBUTE));
        library.setPath(attribute(reader, PATH_ATTRIBUTE));
        library.setVersion(attribute(reader, VERSION_ATTRIBUTE));
        library.setCustom(booleanAttribute(reader, CUSTOM_ATTRIBUTE));
        library.setIgnoreDependencies(booleanAttribute(reader, IGNORE_DEPENDENCIES_ATTRIBUTE));
        return library;
    }

    @Nullable
    private DBeaverDriverFile readFile(XMLStreamReader reader, DBeaverDriverConfig config) {
        String rawPath = attribute(reader, PATH_ATTRIBUTE);
        if (rawPath == null) {
            return null;
        }

        DBeaverDriverFile file = new DBeaverDriverFile();
        file.setId(attribute(reader, ID_ATTRIBUTE));
        file.setVersion(attribute(reader, VERSION_ATTRIBUTE));
        file.setRawPath(rawPath);
        file.setPath(resolvePath(rawPath, config));
        file.setCrc(attribute(reader, CRC_ATTRIBUTE));
        return file;
    }

    @Nullable
    private Path resolvePath(String rawPath, DBeaverDriverConfig config) {
        if (rawPath.startsWith(DRIVERS_HOME_PLACEHOLDER)) {
            return resolveAgainst(config.getDriversHomePath(), rawPath.substring(DRIVERS_HOME_PLACEHOLDER.length()));
        }
        if (rawPath.startsWith(DBEAVER_HOME_PLACEHOLDER)) {
            Path dbeaverHomePath = config.getDbeaverHomePath();
            if (dbeaverHomePath == null) {
                return null;
            }
            return resolveAgainst(dbeaverHomePath, rawPath.substring(DBEAVER_HOME_PLACEHOLDER.length()));
        }
        if (rawPath.contains(PLACEHOLDER_PREFIX)) {
            return null;
        }
        return toAbsolutePathOrNull(rawPath);
    }

    private Path resolveAgainst(Path base, String relativePath) {
        Path resolved = base;
        for (String segment : relativePath.split(PATH_SEPARATOR)) {
            if (!segment.isEmpty()) {
                resolved = resolved.resolve(segment);
            }
        }
        return resolved;
    }

    @Nullable
    private Path toAbsolutePathOrNull(String rawPath) {
        try {
            Path path = Path.of(rawPath);
            return path.isAbsolute() ? path : null;
        } catch (InvalidPathException e) {
            log.warn("Ignoring invalid driver library path {}", rawPath);
            return null;
        }
    }

    @Nullable
    private String attribute(XMLStreamReader reader, String attributeName) {
        return reader.getAttributeValue(null, attributeName);
    }

    private boolean booleanAttribute(XMLStreamReader reader, String attributeName) {
        return TRUE_VALUE.equalsIgnoreCase(attribute(reader, attributeName));
    }

    private void closeQuietly(@Nullable XMLStreamReader reader) {
        if (reader == null) {
            return;
        }
        try {
            reader.close();
        } catch (XMLStreamException e) {
            log.warn("Failed to close the drivers XML reader", e);
        }
    }
}
