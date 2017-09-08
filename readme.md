# Postman Plugin for ReadyAPI

This repository contains source code files for the Postman Plugin for ReadyAPI. You can use this plugin to import your Postman collections.

## Plugin Info

- Author: SmartBear Software (http://smartbear.com)
- Plugin version: 1.0

## Requirements

The plugin requires ReadyAPI version 1.7 or later.

## Working With the Plugin

### Installing the Plugin

To install the plugin:

- Open the Plugin Manager in ReadyAPI.
- Select **Postman Plugin** and click **Install/Upgrade Plugin**.
- Confirm that you want to download and install the plugin.

### Importing the Collection

To import the collection:

- Select **File | Import Postman Collection**.
- In the **Import Postman Collection** dialog, click Browse and select the Postman collection to import.

### Building the plugin

If you want to build the plugin yourself all you need to do is clone this repository locally and run

```
mvn clean install
```

which will create the plugin jar in the target folder

## Additional Information

You can find more information about importing the plugin and conversion from Postman collection to ReadyAPI project in the [documentation](http://readyapi.smartbear.com/features/postman/start).
