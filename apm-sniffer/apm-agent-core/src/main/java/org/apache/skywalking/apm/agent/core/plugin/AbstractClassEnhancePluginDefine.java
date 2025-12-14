/*
 * 根据一个或多个贡献者许可协议授权给 Apache Software Foundation (ASF)。
 * 有关版权所有权的其他信息，请参阅随此作品分发的 NOTICE 文件。
 * ASF 根据 Apache License, Version 2.0（"许可证"）向您授权此文件；
 * 除非符合许可证，否则您不得使用此文件。您可以在以下位置获取许可证的副本：
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * 除非适用法律要求或书面同意，否则根据许可证分发的软件
 * 按"原样"基础分发，不提供任何明示或暗示的担保或条件。
 * 有关许可证管理权限和限制的具体语言，请参阅许可证。
 *
 */

package org.apache.skywalking.apm.agent.core.plugin;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import org.apache.skywalking.apm.agent.core.logging.api.ILog;
import org.apache.skywalking.apm.agent.core.logging.api.LogManager;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.ConstructorInterceptPoint;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.InstanceMethodsInterceptPoint;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.StaticMethodsInterceptPoint;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.ClassEnhancePluginDefine;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.v2.InstanceMethodsInterceptV2Point;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.v2.StaticMethodsInterceptV2Point;
import org.apache.skywalking.apm.agent.core.plugin.match.ClassMatch;
import org.apache.skywalking.apm.agent.core.util.CollectionUtil;
import org.apache.skywalking.apm.util.StringUtil;

import java.util.List;

/**
 * 所有 sky-walking 自动插桩插件的基础抽象类。
 * <p>
 * 它提供了增强目标类的框架。如果你想了解更多关于增强的信息，你应该查看
 * {@link ClassEnhancePluginDefine}
 */
public abstract class AbstractClassEnhancePluginDefine {
    private static final ILog LOGGER = LogManager.getLogger(AbstractClassEnhancePluginDefine.class);

    /**
     * 在 skywalking-plugin.def 中定义的插件名称
     */
    private String pluginName;
    /**
     * 新字段名称。
     */
    public static final String CONTEXT_ATTR_NAME = "_$EnhancedClassField_ws";
    /**
     * Getter 方法名称。
     */
    public static final String CONTEXT_GETTER_NAME = "getSkyWalkingDynamicField";
    /**
     * Setter 方法名称。
     */
    public static final String CONTEXT_SETTER_NAME = "setSkyWalkingDynamicField";

    /**
     * 增强类的主入口。
     *
     * @param typeDescription 目标类描述。
     * @param builder         byte-buddy 的构建器，用于操作目标类的字节码。
     * @param classLoader     加载给定的 transformClass
     * @return 新的构建器，如果未被增强则返回 <code>null</code>。
     * @throws PluginException 当设置构建器失败时。
     */
    public DynamicType.Builder<?> define(TypeDescription typeDescription, DynamicType.Builder<?> builder,
        ClassLoader classLoader, EnhanceContext context) throws PluginException {
        String interceptorDefineClassName = this.getClass().getName();
        String transformClassName = typeDescription.getTypeName();
        if (StringUtil.isEmpty(transformClassName)) {
            LOGGER.warn("classname of being intercepted is not defined by {}.", interceptorDefineClassName);
            return null;
        }

        LOGGER.debug("prepare to enhance class {} by {}.", transformClassName, interceptorDefineClassName);
        WitnessFinder finder = WitnessFinder.INSTANCE;
        /**
         * 查找用于增强类的见证类
         */
        String[] witnessClasses = witnessClasses();
        if (witnessClasses != null) {
            for (String witnessClass : witnessClasses) {
                if (!finder.exist(witnessClass, classLoader)) {
                    LOGGER.warn("enhance class {} by plugin {} is not activated. Witness class {} does not exist.", transformClassName, interceptorDefineClassName, witnessClass);
                    return null;
                }
            }
        }
        List<WitnessMethod> witnessMethods = witnessMethods();
        if (!CollectionUtil.isEmpty(witnessMethods)) {
            for (WitnessMethod witnessMethod : witnessMethods) {
                if (!finder.exist(witnessMethod, classLoader)) {
                    LOGGER.warn("enhance class {} by plugin {} is not activated. Witness method {} does not exist.", transformClassName, interceptorDefineClassName, witnessMethod);
                    return null;
                }
            }
        }

        /**
         * 查找拦截器的原始类源代码
         */
        DynamicType.Builder<?> newClassBuilder = this.enhance(typeDescription, builder, classLoader, context);

        context.initializationStageCompleted();
        LOGGER.debug("enhance class {} by {} completely.", transformClassName, interceptorDefineClassName);

        return newClassBuilder;
    }

    /**
     * 开始定义如何增强类。调用此方法后，仅表示定义已完成。
     *
     * @param typeDescription 目标类描述
     * @param newClassBuilder byte-buddy 的构建器，用于操作类字节码。
     * @return 新的 byte-buddy 构建器，用于进一步操作。
     */
    protected DynamicType.Builder<?> enhance(TypeDescription typeDescription, DynamicType.Builder<?> newClassBuilder,
                                             ClassLoader classLoader, EnhanceContext context) throws PluginException {
        newClassBuilder = this.enhanceClass(typeDescription, newClassBuilder, classLoader);

        newClassBuilder = this.enhanceInstance(typeDescription, newClassBuilder, classLoader, context);

        return newClassBuilder;
    }

    /**
     * 增强类以拦截构造函数和类实例方法。
     *
     * @param typeDescription 目标类描述
     * @param newClassBuilder byte-buddy 的构建器，用于操作类字节码。
     * @return 新的 byte-buddy 构建器，用于进一步操作。
     */
    protected abstract DynamicType.Builder<?> enhanceInstance(TypeDescription typeDescription,
                                                     DynamicType.Builder<?> newClassBuilder, ClassLoader classLoader,
                                                     EnhanceContext context) throws PluginException;

    /**
     * 增强类以拦截类静态方法。
     *
     * @param typeDescription 目标类描述
     * @param newClassBuilder byte-buddy 的构建器，用于操作类字节码。
     * @return 新的 byte-buddy 构建器，用于进一步操作。
     */
    protected abstract DynamicType.Builder<?> enhanceClass(TypeDescription typeDescription, DynamicType.Builder<?> newClassBuilder,
                                                  ClassLoader classLoader) throws PluginException;

    /**
     * 定义用于过滤类的 {@link ClassMatch}。
     *
     * @return {@link ClassMatch}
     */
    protected abstract ClassMatch enhanceClass();

    /**
     * 见证类名列表。为什么需要见证类名？让我们这样看：一个库存在两个发布版本
     *（如 1.0、2.0），它们包含相同的目标类，但由于版本迭代，它们可能有相同的
     * 名称，但方法不同，或方法参数列表不同。所以，如果我想针对特定版本
     *（比如 1.0），版本号显然不是一个选项，这时你需要"见证类"。
     * 你可以添加仅在此特定发布版本中存在的任何类（比如类
     * com.company.1.x.A，仅在 1.0 中存在），这样你就可以达到目标。
     */
    protected String[] witnessClasses() {
        return new String[] {};
    }

    protected List<WitnessMethod> witnessMethods() {
        return null;
    }

    public boolean isBootstrapInstrumentation() {
        return false;
    }

    /**
     * 构造函数方法拦截点。参见 {@link ConstructorInterceptPoint}
     *
     * @return {@link ConstructorInterceptPoint} 的集合
     */
    public abstract ConstructorInterceptPoint[] getConstructorsInterceptPoints();

    /**
     * 实例方法拦截点。参见 {@link InstanceMethodsInterceptPoint}
     *
     * @return {@link InstanceMethodsInterceptPoint} 的集合
     */
    public abstract InstanceMethodsInterceptPoint[] getInstanceMethodsInterceptPoints();

    /**
     * 实例方法拦截 v2 点。参见 {@link InstanceMethodsInterceptV2Point}
     *
     * @return {@link InstanceMethodsInterceptV2Point} 的集合
     */
    public abstract InstanceMethodsInterceptV2Point[] getInstanceMethodsInterceptV2Points();

    /**
     * 静态方法拦截点。参见 {@link StaticMethodsInterceptPoint}
     *
     * @return {@link StaticMethodsInterceptPoint} 的集合
     */
    public abstract StaticMethodsInterceptPoint[] getStaticMethodsInterceptPoints();

    /**
     * 静态方法拦截 v2 点。参见 {@link StaticMethodsInterceptV2Point}
     *
     * @return {@link StaticMethodsInterceptV2Point} 的集合
     */
    public abstract StaticMethodsInterceptV2Point[] getStaticMethodsInterceptV2Points();

    /**
     * 插件名称应在创建 PluginDefine 实例后设置
     *
     * @param pluginName 在 skywalking-plugin.def 中定义的键
     */
    protected void setPluginName(final String pluginName) {
        this.pluginName = pluginName;
    }

    public String getPluginName() {
        return pluginName;
    }
}
