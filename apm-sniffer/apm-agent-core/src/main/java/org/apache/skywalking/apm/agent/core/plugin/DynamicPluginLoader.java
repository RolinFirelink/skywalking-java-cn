/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.skywalking.apm.agent.core.plugin;

import org.apache.skywalking.apm.agent.core.plugin.loader.AgentClassLoader;
import org.apache.skywalking.apm.agent.core.plugin.loader.InstrumentationLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * 通过实现此SPI接口并返回PluginDefine列表，插件可以被插入到内核中
 */

public enum DynamicPluginLoader {

    INSTANCE;

    /**
     * 通过ServiceLoader机制加载所有InstrumentationLoader服务实例，然后调用每个加载器的load方法获取插件定义列表，最终将所有插件合并返回。
     * 具体步骤：
     * 1. 创建空的结果列表all
     * 2. 使用ServiceLoader.load()查找并加载InstrumentationLoader的实现类
     * 3. 遍历每个加载器，调用其load方法获取插件列表
     * 4. 将非空插件列表添加到结果中
     * 5. 返回合并后的完整插件列表
     */
    public List<AbstractClassEnhancePluginDefine> load(AgentClassLoader classLoader) {
        List<AbstractClassEnhancePluginDefine> all = new ArrayList<AbstractClassEnhancePluginDefine>();
        for (InstrumentationLoader instrumentationLoader : ServiceLoader.load(InstrumentationLoader.class, classLoader)) {
            List<AbstractClassEnhancePluginDefine> plugins = instrumentationLoader.load(classLoader);
            if (plugins != null && !plugins.isEmpty()) {
                all.addAll(plugins);
            }
        }
        return all;
    }
}
