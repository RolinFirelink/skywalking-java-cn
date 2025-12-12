/*
 * 根据一个或多个贡献者许可协议，授权给 Apache 软件基金会 (ASF)。
 * 有关版权所有权的其他信息，请参阅随本作品分发的 NOTICE 文件。
 * ASF 根据 Apache 许可证 2.0 版（"许可证"）向您授权此文件；
 * 除非遵守许可证，否则您不得使用此文件。您可以在以下位置获取许可证副本：
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * 除非适用法律要求或书面同意，否则根据许可证分发的软件按"原样"基础分发，
 * 不提供任何明示或暗示的担保或条件。有关权限和限制的具体语言，请参阅许可证。
 *
 */

package org.apache.skywalking.apm.agent.core.conf;

import java.util.Arrays;
import java.util.List;
import org.apache.skywalking.apm.agent.core.context.trace.TraceSegment;
import org.apache.skywalking.apm.agent.core.logging.core.LogLevel;
import org.apache.skywalking.apm.agent.core.logging.core.LogOutput;
import org.apache.skywalking.apm.agent.core.logging.core.ResolverType;
import org.apache.skywalking.apm.agent.core.logging.core.WriterFactory;
import org.apache.skywalking.apm.util.Length;

/**
 * 这是嗅探器代理中的核心配置。
 */
public class Config {

    public static class Agent {
        /**
         * 命名空间表示一个子网，例如 kubernetes 命名空间，或 172.10.*.*。
         *
         * @since 8.10.0 命名空间将作为 {@link #SERVICE_NAME} 的后缀添加。
         *
         * 在跨进程传播中移除了命名空间隔离头。HEADER 名称是 `HeaderName:Namespace`。
         */
        @Length(20)
        public static String NAMESPACE = "";

        /**
         * 服务名称显示在 UI 上。建议：为每个服务设置一个唯一的名称，服务实例节点共享相同的代码。
         *
         * @since 8.10.0 ${service name} = [${group name}::]${logic name}|${NAMESPACE}|${CLUSTER}
         *
         * 组名、命名空间和集群是可选的。一旦它们都为空，服务名称将是最终名称。
         */
        @Length(50)
        public static String SERVICE_NAME = "";

        /**
         * 集群定义数据中心或同一网段中的物理集群。在一个集群中，IP 地址应该是唯一标识。
         *
         * 集群名称将：
         *
         * 1. 作为 {@link #SERVICE_NAME} 的后缀添加。
         *
         * 2. 作为退出跨度的对等节点添加，${CLUSTER} / 原始对等节点
         *
         * 3. 跨进程传播头的值 addressUsedAtClient[index=8]（在客户端使用的此请求的目标地址）。
         *
         * @since 8.10.0
         */
        @Length(20)
        public static String CLUSTER = "";

        /**
         * 身份验证激活基于后端设置，更多详细信息请参阅 application.yml。对于大多数场景，
         * 这需要后端扩展，默认实现中仅提供基本匹配身份验证。
         */
        public static String AUTHENTICATION = "";

        /**
         * 负数或零表示关闭，默认情况下。{@code #SAMPLE_N_PER_3_SECS} 表示在最多 3 秒内采样 N 个 {@link TraceSegment}。
         */
        public static int SAMPLE_N_PER_3_SECS = -1;

        /**
         * 如果第一个跨度的操作名称包含在此集合中，则应忽略此段。多个值应用 `,` 分隔。
         */
        public static String IGNORE_SUFFIX = ".jpg,.jpeg,.js,.css,.png,.bmp,.gif,.ico,.mp3,.mp4,.html,.svg";

        /**
         * 单个跨度中 TraceSegmentRef 的最大数量，以保持内存成本可估算。
         */
        public static int TRACE_SEGMENT_REF_LIMIT_PER_SPAN = 500;

        /**
         * 单个跨度中日志的最大数量，以保持内存成本可估算。
         */
        public static int LOG_LIMIT_PER_SPAN = 300;

        /**
         * 单个段中跨度的最大数量。通过此配置项，SkyWalking 可以估算您的应用程序内存成本。
         */
        public static int SPAN_LIMIT_PER_SEGMENT = 300;

        /**
         * 如果为 true，SkyWalking 代理将在 `/debugging` 文件夹中保存所有已插桩的类文件。SkyWalking 团队可能会要求这些文件以解决兼容性问题。
         */
        public static boolean IS_OPEN_DEBUGGING_CLASS = false;

        /**
         * 实例的标识符
         */
        @Length(50)
        public volatile static String INSTANCE_NAME = "";

        /**
         * 服务实例属性，采用 json 格式。例如：agent.instance_properties_json = {"org": "apache-skywalking"}
         */
        public static String INSTANCE_PROPERTIES_JSON = "";

        /**
         * 当日志导致异常时，代理的深度。
         */
        public static int CAUSE_EXCEPTION_DEPTH = 5;

        /**
         * grpc 的强制重连周期，基于 grpc_channel_check_interval。如果检查 grpc 通道状态的次数超过此数字，
         * 通道检查将调用 channel.getState(true) 来请求连接。
         */
        public static long FORCE_RECONNECTION_PERIOD = 1;

        /**
         * 限制 operationName 的长度，以防止存储中的超长问题。
         *
         * <p>注意</p>
         * 在当前实践中，我们不建议长度超过 190。
         */
        public static int OPERATION_NAME_THRESHOLD = 150;

        /**
         * 即使后端不可用也保持追踪。
         */
        public static boolean KEEP_TRACING = false;

        /**
         * 如果为 true，则强制为 gRPC 通道启用 TLS。
         */
        public static boolean FORCE_TLS = false;

        /**
         * SSL 受信任的 CA 文件。如果存在，将为 gRPC 通道启用 TLS。
         */
        public static String SSL_TRUSTED_CA_PATH = "ca" + Constants.PATH_SEPARATOR + "ca.crt";

        /**
         * 密钥证书链文件。如果 ssl_cert_chain 和 ssl_key 存在，将为 gRPC 通道启用 mTLS。
         */
        public static String SSL_CERT_CHAIN_PATH;

        /**
         * 私钥文件。如果 ssl_cert_chain 和 ssl_key 存在，将为 gRPC 通道启用 mTLS。
         */
        public static String SSL_KEY_PATH;

        /**
         * 代理版本。这是由代理内核通过读取 skywalking-agent.jar 中的 MANIFEST.MF 文件设置的。
         */
        public static String VERSION = "UNKNOWN";

        /**
         * 启用代理内核服务和插桩。
         */
        public static boolean ENABLE = true;
    }

    public static class OsInfo {
        /**
         * 限制 ipv4 列表大小的长度。
         */
        public static int IPV4_LIST_SIZE = 10;
    }

    public static class Collector {
        /**
         * grpc 通道状态检查间隔
         */
        public static long GRPC_CHANNEL_CHECK_INTERVAL = 30;
        /**
         * 代理向后端报告心跳的周期。
         */
        public static long HEARTBEAT_PERIOD = 30;
        /**
         * 代理每 `collector.heartbeat_period * collector.properties_report_period_factor` 秒向后端发送一次实例属性
         */
        public static int PROPERTIES_REPORT_PERIOD_FACTOR = 10;
        /**
         * 收集器 SkyWalking 追踪接收器服务地址。
         */
        public static String BACKEND_SERVICE = "";
        /**
         * grpc 客户端向上游发送数据的超时时间。
         */
        public static int GRPC_UPSTREAM_TIMEOUT = 30;
        /**
         * 获取性能分析任务列表的间隔
         */
        public static int GET_PROFILE_TASK_INTERVAL = 20;
        /**
         * 获取代理动态配置的间隔
         */
        public static int GET_AGENT_DYNAMIC_CONFIG_INTERVAL = 20;
        /**
         * 如果为 true，SkyWalking 代理将启用定期解析 DNS 以更新接收器服务地址。
         */
        public static boolean IS_RESOLVE_DNS_PERIODICALLY = false;
    }

    public static class Profile {
        /**
         * 如果为 true，当用户创建新的性能分析任务时，SkyWalking 代理将启用性能分析。否则禁用性能分析。
         */
        public static boolean ACTIVE = true;

        /**
         * 并行监控端点的线程数
         */
        public static int MAX_PARALLEL = 5;

        /**
         * 单个端点访问的最大监控子任务数
         */
        public static int MAX_ACCEPT_SUB_PARALLEL = 5;

        /**
         * 最大监控段时间（分钟），如果当前段监控时间超出限制，则停止它。
         */
        public static int MAX_DURATION = 10;

        /**
         * 最大转储线程栈深度
         */
        public static int DUMP_MAX_STACK_DEPTH = 500;

        /**
         * 快照传输到后端的缓冲区大小
         */
        public static int SNAPSHOT_TRANSPORT_BUFFER_SIZE = 500;
    }

    public static class AsyncProfiler {
        /**
         * 如果为 true，当用户创建新的异步性能分析器任务时，将启用异步性能分析器。
         * 如果为 false，它将被禁用。
         * 默认值为 true。
         */
        public static boolean ACTIVE = true;

        /**
         * 异步性能分析器的最大执行时间（秒）。即使指定了更长时间，任务也会停止。
         * 默认 20 分钟。
         */
        public static int MAX_DURATION = 1200;

        /**
         * 来自异步性能分析器的 JFR 输出路径。
         * 如果参数不为空，文件将在指定目录中创建，
         * 否则将使用 Files.createTemp 方法创建文件。
         */
        public static String OUTPUT_PATH = "";

        /**
         * 上传 jfr 时的块大小
         */
        public static final int DATA_CHUNK_SIZE = 1024 * 1024;
    }

    public static class Meter {
        /**
         * 如果为 true，SkyWalking 代理将启用发送指标。否则禁用指标报告。
         */
        public static boolean ACTIVE = true;

        /**
         * 报告指标的间隔
         */
        public static Integer REPORT_INTERVAL = 20;

        /**
         * 指标计数的最大大小，使用 {@link org.apache.skywalking.apm.agent.core.meter.MeterId} 作为标识
         */
        public static Integer MAX_METER_SIZE = 500;
    }

    public static class Jvm {
        /**
         * 收集的 JVM 信息的缓冲区大小。
         */
        public static int BUFFER_SIZE = 60 * 10;
        /**
         * JVM 指标收集的周期（秒）。
         */
        public static int METRICS_COLLECT_PERIOD = 1;
    }

    public static class Log {
        /**
         * 发送到服务器的消息的最大大小。默认为 10 MB。
         */
        public static int MAX_MESSAGE_SIZE = 10 * 1024 * 1024;
    }

    public static class Buffer {
        public static int CHANNEL_SIZE = 5;

        public static int BUFFER_SIZE = 300;
    }

    public static class Logging {
        /**
         * 日志文件名。
         */
        public static String FILE_NAME = "skywalking-api.log";

        /**
         * 日志文件目录。默认为空字符串，表示使用 "{theSkywalkingAgentJarDir}/logs  " 来输出日志。
         * {theSkywalkingAgentJarDir} 是 SkyWalking 代理 jar 文件所在的目录。
         * <p>
         * 参考 {@link WriterFactory#getLogWriter()}
         */
        public static String DIR = "";

        /**
         * 日志文件的最大大小。如果大小超过此值，将归档当前文件，并写入新文件。
         */
        public static int MAX_FILE_SIZE = 300 * 1024 * 1024;

        /**
         * 最大历史日志文件数。当发生滚动时，如果日志文件超过此数量，则将删除最旧的文件。负数或零表示关闭，默认情况下。
         */
        public static int MAX_HISTORY_FILES = -1;

        /**
         * 日志级别。默认为 debug。
         */
        public static LogLevel LEVEL = LogLevel.DEBUG;

        /**
         * 日志输出。默认为 FILE。
         */
        public static LogOutput OUTPUT = LogOutput.FILE;

        /**
         * 日志解析器类型。默认为 PATTERN，稍后将创建 PatternLogResolver。
         */
        public static ResolverType RESOLVER = ResolverType.PATTERN;

        /**
         * 日志模式。默认为 "%level %timestamp %thread %class : %msg %throwable"。每个转换说明符
         * 以百分号 '%' 开头，后跟转换词。有一些默认的转换说明符：
         * %thread = 线程名称
         * %level = 日志级别 {@link LogLevel}
         * %timestamp = 当前时间，格式为 'yyyy-MM-dd HH:mm:ss:SSS'
         * %class = 目标类的简单名称
         * %msg = 用户输入的消息
         * %throwable = 用户输入的异常
         * %agent_name = 代理的服务名称 {@link Agent#SERVICE_NAME}
         *
         * @see org.apache.skywalking.apm.agent.core.logging.core.PatternLogger#DEFAULT_CONVERTER_MAP
         */
        public static String PATTERN = "%level %timestamp %thread %class : %msg %throwable";
    }

    public static class StatusCheck {
        /**
         * 列出的异常不会被视为错误。因为在某些代码中，异常被用作控制业务流的方式。
         */
        public static String IGNORED_EXCEPTIONS = "";

        /**
         * 检查代理追踪的异常时的最大递归深度。通常，我们不建议将此值设置为超过 10，这可能会导致性能问题。
         * 负值和 0 将被忽略，这意味着所有异常都会使跨度标记为错误状态。
         */
        public static Integer MAX_RECURSIVE_DEPTH = 1;
    }

    public static class Plugin {
        /**
         * 控制对等字段的长度。
         */
        public static int PEER_MAX_LENGTH = 200;

        /**
         * 排除已激活的插件
         */
        public static String EXCLUDE_PLUGINS = "";

        /**
         * 挂载插件的文件夹。文件夹路径相对于 agent.jar。
         */
        public static List<String> MOUNT = Arrays.asList("plugins", "activations");
    }

    public static class Correlation {
        /**
         * 关联上下文中的最大元素数。
         */
        public static int ELEMENT_MAX_NUMBER = 3;

        /**
         * 每个元素的最大值长度。
         */
        public static int VALUE_MAX_LENGTH = 128;

        /**
         * 当此处列出的键存在时，使用关联上下文中的键/值标记跨度。
         */
        public static String AUTO_TAG_KEYS = "";
    }
}
