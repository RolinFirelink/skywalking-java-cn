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

package org.apache.skywalking.apm.agent.core.conf;

public class Constants {
    public static String PATH_SEPARATOR = System.getProperty("file.separator", "/");

    public static String LINE_SEPARATOR = System.getProperty("line.separator", "\n");

    public static String EMPTY_STRING = "";

    public static char SERVICE_NAME_PART_CONNECTOR = '|';

    // 表示代理安装服务的层名称，
    // 定义在 https://github.com/apache/skywalking/blob/85ce1645be53e46286f36c0ea206c60db2d1a716/oap-server/server-core/src/main/java/org/apache/skywalking/oap/server/core/analysis/Layer.java#L30
    public static String EVENT_LAYER_NAME = "GENERAL";

    public static int NULL_VALUE = 0;

    /**
     * 用于ByteBuddy生成的辅助类型名称、字段名称和方法名称的命名特征。
     * 这是一个常量字符串值 "$sw$"，用作SkyWalking代理在使用ByteBuddy进行字节码增强时生成的各种元素的命名前缀或标识符
     */
    public static final String NAME_TRAIT = "$sw$";

}
