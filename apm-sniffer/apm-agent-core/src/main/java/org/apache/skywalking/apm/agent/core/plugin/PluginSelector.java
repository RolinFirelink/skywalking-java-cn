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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.skywalking.apm.agent.core.conf.Config;

import static org.apache.skywalking.apm.agent.core.conf.Config.Plugin.EXCLUDE_PLUGINS;

/**
 * 从已激活的插件中选择部分插件
 */
public class PluginSelector {
    /**
     * 排除已激活的插件
     *
     * @param pluginDefines 插件定义列表，从activations目录或plugins目录加载
     * @return 实际激活的插件列表
     * @see Config.Plugin#EXCLUDE_PLUGINS
     */
    public List<PluginDefine> select(List<PluginDefine> pluginDefines) {
        if (!EXCLUDE_PLUGINS.isEmpty()) {
            List<String> excludes = Arrays.asList(EXCLUDE_PLUGINS.toLowerCase().split(","));
            return pluginDefines.stream()
                                .filter(item -> !excludes.contains(item.getName().toLowerCase()))
                                .collect(Collectors.toList());
        }
        return pluginDefines;
    }
}
