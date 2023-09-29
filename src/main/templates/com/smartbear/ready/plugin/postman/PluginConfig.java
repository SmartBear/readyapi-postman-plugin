/**
 *  Copyright 2016 SmartBear Software, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.smartbear.ready.plugin.postman;

import com.eviware.soapui.plugins.PluginAdapter;
import com.eviware.soapui.plugins.PluginConfiguration;
import com.eviware.soapui.support.UISupport;
import com.smartbear.ready.core.ApplicationEnvironment;
import com.eviware.soapui.SoapUICore;
import com.eviware.soapui.impl.actions.ImportMethodFactory;

@PluginConfiguration(groupId = "${project.groupId}",
    name = "${project.name}",
    version = "${project.version}",
    autoDetect = true,
    description = "${project.description}",
    infoUrl = "${project.url}",
    minimumReadyApiVersion = "${ready-api-version}")
public class PluginConfig extends PluginAdapter {

    public PluginConfig() {
        super();
        ApplicationEnvironment.getSoapUICore().getFactoryRegistry().addFactory(ImportMethodFactory.class,
                new PostmanImportMethodFactory());
    }
}