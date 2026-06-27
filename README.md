# dbeaver-config

A small Java module that reads DBeaver's local configuration - connection data sources and decrypted credentials - from the files DBeaver stores on disk.

It is part of the [DBeaver MCP](https://github.com/FelipeFlohr/dbeaver-mcp) project, where it is used to load existing DBeaver connections.

## Modules

- **dbeaver-config-api**: public interfaces and data models (`DBeaverDataSource`, `DBeaverCipher`).
- **dbeaver-config-core**: implementations that parse and decrypt the configuration files.

## Requirements

`dbeaver-config-core` depends on Jackson (`tools.jackson.core:jackson-databind`) to parse the configuration files. This dependency is declared with `provided` scope, so it is **not** bundled with the module - the consuming application must provide Jackson on its runtime classpath for the core implementation to work.
