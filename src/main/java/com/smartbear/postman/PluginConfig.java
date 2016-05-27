package com.smartbear.postman;

import com.eviware.soapui.plugins.PluginAdapter;
import com.eviware.soapui.plugins.PluginConfiguration;

@PluginConfiguration(groupId = "com.smartbear.soapui.plugins", name = "Postman Plugin", version = "1.0",
        autoDetect = true, description = "Imports Postman collesctions",
        infoUrl = "https://github.com/antone-sb/soapui-swagger-plugin")
public class PluginConfig extends PluginAdapter {
}
