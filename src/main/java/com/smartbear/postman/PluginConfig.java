package com.smartbear.postman;

import com.eviware.soapui.plugins.PluginAdapter;
import com.eviware.soapui.plugins.PluginConfiguration;

@PluginConfiguration(groupId = "com.smartbear.soapui.plugins", name = "Postman Plugin", version = "1.0",
        autoDetect = true, description = "Creates Ready! API projects and tests based on Postman collections",
        infoUrl = "https://github.com/SmartBear/readyapi-postman-plugin")
public class PluginConfig extends PluginAdapter {
}
