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

package org.apache.skywalking.apm.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * 用于处理包含占位符值的字符串的工具类。占位符采用 {@code ${name}} 的形式。
 * 使用 {@code PropertyPlaceholderHelper} 可以将这些占位符替换为用户提供的值。
 * <p>
 * 替换值可以通过 {@link Properties} 实例或使用 {@link PlaceholderResolver} 来提供。
 */
public enum PropertyPlaceholderHelper {

    // 单例对象,传参是为了调用构造方法,默认无视解析不成功的占位符
    INSTANCE(
        PlaceholderConfigurerSupport.DEFAULT_PLACEHOLDER_PREFIX,
        PlaceholderConfigurerSupport.DEFAULT_PLACEHOLDER_SUFFIX,
        PlaceholderConfigurerSupport.DEFAULT_VALUE_SEPARATOR,
        true
    );

    private final String placeholderPrefix;

    private final String placeholderSuffix;

    private final String simplePrefix;

    private final String valueSeparator;

    private final boolean ignoreUnresolvablePlaceholders;

    /**
     * 创建一个新的 {@code PropertyPlaceholderHelper}，使用指定的前缀和后缀。
     *
     * @param placeholderPrefix              表示占位符开始的前缀
     * @param placeholderSuffix              表示占位符结束的后缀
     * @param valueSeparator                 占位符变量与关联默认值之间的分隔字符（如果有）
     * @param ignoreUnresolvablePlaceholders 指示是否应忽略无法解析的占位符（{@code true}）还是抛出异常（{@code false}）
     */
    PropertyPlaceholderHelper(String placeholderPrefix, String placeholderSuffix, String valueSeparator,
                              boolean ignoreUnresolvablePlaceholders) {
        if (StringUtil.isEmpty(placeholderPrefix) || StringUtil.isEmpty(placeholderSuffix)) {
            throw new UnsupportedOperationException("'placeholderPrefix or placeholderSuffix' must not be null");
        }

        final Map<String, String> wellKnownSimplePrefixes = new HashMap<String, String>(4);

        wellKnownSimplePrefixes.put("}", "{");
        wellKnownSimplePrefixes.put("]", "[");
        wellKnownSimplePrefixes.put(")", "(");

        this.placeholderPrefix = placeholderPrefix;
        this.placeholderSuffix = placeholderSuffix;
        String simplePrefixForSuffix = wellKnownSimplePrefixes.get(this.placeholderSuffix);
        if (simplePrefixForSuffix != null && this.placeholderPrefix.endsWith(simplePrefixForSuffix)) {
            this.simplePrefix = simplePrefixForSuffix;
        } else {
            this.simplePrefix = this.placeholderPrefix;
        }
        this.valueSeparator = valueSeparator;
        this.ignoreUnresolvablePlaceholders = ignoreUnresolvablePlaceholders;
    }

    /**
     * 将格式为 {@code ${name}} 的所有占位符替换为提供的 {@link Properties} 中的相应属性。
     *
     * @param value      包含待替换占位符的值
     * @param properties 用于替换的 {@code Properties} 对象
     * @return 占位符被内联替换后的值
     */
    public String replacePlaceholders(String value, final Properties properties) {
        return replacePlaceholders(value, new PlaceholderResolver() {
            @Override
            public String resolvePlaceholder(String placeholderName) {
                return getConfigValue(placeholderName, properties);
            }
        });
    }

    /**
     * 获取配置值
     * 首先从系统属性(System Properties)中查找指定key的值
     * 如果找不到，则从环境变量(Environment Variables)中查找
     * 如果仍然找不到，则从传入的Properties对象中查找
     * 返回找到的值，都找不到则返回null
     */
    private String getConfigValue(String key, final Properties properties) {
        String value = System.getProperty(key);
        if (value == null) {
            value = System.getenv(key);
        }
        if (value == null) {
            value = properties.getProperty(key);
        }
        return value;
    }

    /**
     * 将格式为 {@code ${name}} 的所有占位符替换为提供的 {@link PlaceholderResolver} 返回的值。
     *
     * @param value               包含待替换占位符的值
     * @param placeholderResolver 用于替换的 {@code PlaceholderResolver} 对象
     * @return 占位符被内联替换后的值
     */
    public String replacePlaceholders(String value, PlaceholderResolver placeholderResolver) {
        return parseStringValue(value, placeholderResolver, new HashSet<String>());
    }

    /**
     * 该方法用于解析字符串中的占位符。主要功能包括：
     * 1.查找并替换占位符为实际值；
     * 2.支持递归解析嵌套占位符；
     * 3.检测循环引用，防止死循环；
     * 4.支持默认值（通过分隔符区分）；
     * 5.可配置是否忽略无法解析的占位符。
     * 核心是通过递归和字符串替换实现多层占位符解析。
     */
    protected String parseStringValue(String value, PlaceholderResolver placeholderResolver,
                                      Set<String> visitedPlaceholders) {

        StringBuilder result = new StringBuilder(value);

        int startIndex = value.indexOf(this.placeholderPrefix);
        while (startIndex != -1) {
            int endIndex = findPlaceholderEndIndex(result, startIndex);
            if (endIndex != -1) {
                String placeholder = result.substring(startIndex + this.placeholderPrefix.length(), endIndex);
                String originalPlaceholder = placeholder;
                if (!visitedPlaceholders.add(originalPlaceholder)) {
                    throw new IllegalArgumentException(
                        "Circular placeholder reference '" + originalPlaceholder + "' in property definitions");
                }
                // Recursive invocation, parsing placeholders contained in the placeholder key.
                placeholder = parseStringValue(placeholder, placeholderResolver, visitedPlaceholders);
                // Now obtain the value for the fully resolved key...
                String propVal = placeholderResolver.resolvePlaceholder(placeholder);
                if (propVal == null && this.valueSeparator != null) {
                    int separatorIndex = placeholder.indexOf(this.valueSeparator);
                    if (separatorIndex != -1) {
                        String actualPlaceholder = placeholder.substring(0, separatorIndex);
                        String defaultValue = placeholder.substring(separatorIndex + this.valueSeparator.length());
                        propVal = placeholderResolver.resolvePlaceholder(actualPlaceholder);
                        if (propVal == null) {
                            propVal = defaultValue;
                        }
                    }
                }
                if (propVal != null) {
                    // Recursive invocation, parsing placeholders contained in the
                    // previously resolved placeholder value.
                    propVal = parseStringValue(propVal, placeholderResolver, visitedPlaceholders);
                    result.replace(startIndex, endIndex + this.placeholderSuffix.length(), propVal);
                    startIndex = result.indexOf(this.placeholderPrefix, startIndex + propVal.length());
                } else if (this.ignoreUnresolvablePlaceholders) {
                    // Proceed with unprocessed value.
                    startIndex = result.indexOf(this.placeholderPrefix, endIndex + this.placeholderSuffix.length());
                } else {
                    throw new IllegalArgumentException(
                        "Could not resolve placeholder '" + placeholder + "'" + " in value \"" + value + "\"");
                }
                visitedPlaceholders.remove(originalPlaceholder);
            } else {
                startIndex = -1;
            }
        }
        return result.toString();
    }

    private int findPlaceholderEndIndex(CharSequence buf, int startIndex) {
        int index = startIndex + this.placeholderPrefix.length();
        int withinNestedPlaceholder = 0;
        while (index < buf.length()) {
            if (StringUtil.substringMatch(buf, index, this.placeholderSuffix)) {
                if (withinNestedPlaceholder > 0) {
                    withinNestedPlaceholder--;
                    index = index + this.placeholderSuffix.length();
                } else {
                    return index;
                }
            } else if (StringUtil.substringMatch(buf, index, this.simplePrefix)) {
                withinNestedPlaceholder++;
                index = index + this.simplePrefix.length();
            } else {
                index++;
            }
        }
        return -1;
    }

    /**
     * Strategy interface used to resolve replacement values for placeholders contained in Strings.
     */
    public interface PlaceholderResolver {

        /**
         * Resolve the supplied placeholder name to the replacement value.
         *
         * @param placeholderName the name of the placeholder to resolve
         * @return the replacement value, or {@code null} if no replacement is to be made
         */
        String resolvePlaceholder(String placeholderName);
    }
}
