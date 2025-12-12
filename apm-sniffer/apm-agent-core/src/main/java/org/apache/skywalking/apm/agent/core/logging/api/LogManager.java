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

package org.apache.skywalking.apm.agent.core.logging.api;

import org.apache.skywalking.apm.agent.core.logging.core.PatternLogResolver;

/**
 * LogManager 是 {@link LogResolver} 实现的管理器。通过使用 {@link LogResolver}，{@link LogManager#getLogger(Class)} 返回一个 {@link ILog} 实现。该模块使用此类作为主要入口，
 * 并屏蔽了日志组件的实现细节。在不同的模块中，如服务器或嗅探器，它将使用不同的实现。
 *
 * <p> 如果没有注册 {@link LogResolver}，则返回 {@link NoopLogger#INSTANCE} 以避免 {@link NullPointerException}。
 * 如果 {@link LogManager#setLogResolver(LogResolver)} 被调用两次，第二次将覆盖第一次，不会有任何警告或异常。
 *
 * <p> 由 xin 于 2016/11/10 创建。
 */
public class LogManager {
    private static LogResolver RESOLVER = new PatternLogResolver();

    public static void setLogResolver(LogResolver resolver) {
        LogManager.RESOLVER = resolver;
    }

    public static ILog getLogger(Class<?> clazz) {
        if (RESOLVER == null) {
            return NoopLogger.INSTANCE;
        }
        return LogManager.RESOLVER.getLogger(clazz);
    }

    public static ILog getLogger(String clazz) {
        if (RESOLVER == null) {
            return NoopLogger.INSTANCE;
        }
        return LogManager.RESOLVER.getLogger(clazz);
    }
}
