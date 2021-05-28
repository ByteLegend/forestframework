package io.forestframework.core.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.annotations.VisibleForTesting;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServerOptions;
import org.apache.commons.io.IOUtils;
import org.apiguardian.api.API;

import javax.inject.Singleton;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * Provides a "Vertx-domain-specific" configuration value instance based on the configuration key for the Forest
 * application. A configuration value instance can be normal String, Integer, Long, or very complicated instances.
 * <p>
 * You can get such a configuration instance via two ways:
 * <p>
 * 1. Call {@link ConfigProvider#getInstance(String, Class)}, e.g.
 * {@code configProvider.getInstance("http.port", Integer.class)}.
 * <p>
 * 2. Inject the configuration instance via Guice DI, e.g.
 * <pre>
 * public class MyService {
 *     {@literal @}Inject
 *     public MyService(@literal @Config("http.port") int port) {
 *         ...
 *     }
 * }
 * </pre>
 * We make it deeply integrated into Guice DI (in a hacky way, don't want to talk too much about it).
 * <p>
 * When the configuration value is a complicated instance (usually Vert.x option classes), it's constructed by three steps:
 * <ol>
 *     <li>Get the that configuration default value instance, by calling default constructor.</li>
 *     <li>Overwrite some properties of the instance by reading the configuration file.</li>
 *     <li>Overwrite some properties of the instance by reading the system properties or environment variables.</li>
 * </ol>
 * <p>
 * This means, you can get a fully configured instance partially overwritten by the external configuration or command line args.
 * For example, given configuration file:
 *
 * <pre>
 *     http:
 *       port: 8081
 *       compressionSupported: false
 *       initialSettings:
 *         headerTableSize: 4096
 * </pre>
 * <p>
 * You can get the port via {@code configProvider.getInstance("http.port", Integer.class)}, or get the
 * fully configured {@link HttpServerOptions} instance, with the corresponding properties in the config file overwritten
 * via {@code configProvider.getInstance("http", HttpServerOptions.class)}.
 */
@API(status = API.Status.EXPERIMENTAL, since = "0.1")
@Singleton
public class ConfigProvider {
    private static final Pattern SYSTEM_PROPERTY_CONFIG_PATTERN = Pattern.compile("forest(\\.[\\w\\d$])+");
    private static final Pattern ENV_VARIABLE_CONFIG_PATTERN = Pattern.compile("FOREST(_[\\w\\d$])+");
    private static final ObjectMapper YAML_PARSER = new ObjectMapper(new YAMLFactory());
    private static final ObjectMapper JSON_PARSER = new ObjectMapper();
    private final Map<String, Supplier<?>> defaultOptions = new ConcurrentHashMap<>();
    /**
     * The config data in config file. It's created by JSON or Yaml parser directly.
     */
    private final Map<String, Object> configFileModel;
    /**
     * The config data in environment. By default, system properties with prefix "forest." go into this model, with "forest." prefix
     * removed as key. For example, the application starts with {@code -Dforest.http.port=8080 -Dforest.pg.connect.port=5432} will
     * have this model @literal {http.port=8080, pg.connect.port=5432}.
     * <p>
     * Note that you can pass a JSON string like this: @literal -Dforest.http='{"port":8080,compressionSupported:false}'
     */
    private final Map<String, Object> environmentModel;

    {
        defaultOptions.put("http", HttpServerOptions::new);
        defaultOptions.put("vertx", VertxOptions::new);
        defaultOptions.put("deploy", DeploymentOptions::new);
        defaultOptions.put("environment", () -> "dev");
    }

    private ConfigProvider() {
        this.configFileModel = new HashMap<>();
        this.environmentModel = new HashMap<>();
    }

    @VisibleForTesting
    ConfigProvider(Map<String, Object> configFileModel, Map<String, Object> environmentModel) {
        this.configFileModel = configFileModel;
        this.environmentModel = environmentModel;
    }

    /**
     * Creates an empty config provider.
     *
     * @return the empty config provider.
     */
    public static ConfigProvider empty() {
        return new ConfigProvider(new HashMap<>(), new HashMap<>());
    }

    /**
     * Load all configuration from current environment for further query. It searches the environment in the following orders:
     * <ul>
     *   <li>Config file (the latter one supersedes the former one):</li>
     *     <li>The file specified by system property "-Dforest.config.file={filePath}".</li>
     *     <li>The first resource file named "/forest.json" in classpath.</li>
     *     <li>The first resource file named "/forest.yml" in classpath.</li>
     *   <li>System properties (this overwrites the corresponding value in config file):</li>
     *       <li>All system properties starting with "FOREST_a_b_c" will be read as "a.b.c". </li>
     *       <li>All system properties starting with "forest.", if the value isn't JSON string (starting with '{' or '['),
     *       store the value as it is.</li>
     *       <li>All system properties starting with "forest.", if the value is JSON string (starting with '{' or '['),
     *       let JSON parser parse the value then store the deserialized result.</li>
     * </ul>
     * <p>
     * See {@link ConfigProvider#SYSTEM_PROPERTY_CONFIG_PATTERN}
     * See {@link ConfigProvider#ENV_VARIABLE_CONFIG_PATTERN}
     */
    public static ConfigProvider load() {
        try {
            return new ConfigProvider(loadConfigFile(), loadEnvironmentConfig());
        } catch (Throwable e) {
            throw new RuntimeException("Can't load config", e);
        }
    }

    /**
     * Load from specified config file and environment.
     *
     * @param configFile the specified config file.
     * @return Loaded config provider.
     */
    public static ConfigProvider loadFromFileAndEnvironment(File configFile) {
        try {
            return new ConfigProvider(loadModel(configFile), loadEnvironmentConfig());
        } catch (Throwable e) {
            throw new RuntimeException("Can't load config", e);
        }
    }

    public void addDefaultOptions(String key, Supplier<?> supplier) {
        defaultOptions.put(key, supplier);
    }

    public void addConfig(String key, String value) {
        addConfigTo(key, value, environmentModel);
    }

    @SuppressWarnings("unchecked")
    public <T> T getInstance(String key, Class<T> klass) {
        String[] paths = key.split("\\.");
        ConfigObject current = new ConfigObject();

        for (String path : paths) {
            current = current.getObject(path);
        }

        return (T) current.getResult(klass);
    }

    private static Map<String, Object> loadEnvironmentConfig() {
        Map<String, Object> resultMap = new HashMap<>();
        for (Map.Entry<Object, Object> entry : System.getProperties().entrySet()) {
            String key = entry.getKey().toString();
            String value = entry.getValue().toString();
            if (SYSTEM_PROPERTY_CONFIG_PATTERN.matcher(key).find()) {
                addConfigTo(key.substring(7), value, resultMap);
            }
        }

        for (Map.Entry<String, String> entry : System.getenv().entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (ENV_VARIABLE_CONFIG_PATTERN.matcher(key).find()) {
                // TODO escape _ with __
                addConfigTo(key.substring(7).replace('_', '.'), value, resultMap);
            }
        }
        return resultMap;
    }

    @SuppressWarnings("unchecked")
    private static void addConfigTo(String key, String value, Map<String, Object> resultMap) {
        String[] path = key.split("\\.");

        Map<String, Object> current = resultMap;
        for (int i = 0; i < path.length - 1; ++i) {
            current = (Map<String, Object>) current.computeIfAbsent(path[i], __ -> new HashMap<>());
        }

        if (isJson(value)) {
            try {
                current.put(path[path.length - 1], JSON_PARSER.readValue(value, Object.class));
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException(String.format("Can't deserialize %s=%s", key, value), e);
            }
        } else {
            current.put(path[path.length - 1], value);
        }
    }

    private static Map<String, Object> loadConfigFile() throws IOException {
        if (System.getenv("FOREST_config_file") != null) {
            return loadModel(new FileInputStream(System.getenv("FOREST_config_file")));
        } else if (System.getProperty("forest.config.file") != null) {
            return loadModel(new FileInputStream(System.getProperty("forest.config.file")));
        } else {
            InputStream yml = ConfigProvider.class.getResourceAsStream("/forest.yml");
            if (yml != null) {
                return loadModel(yml);
            }
            InputStream yaml = ConfigProvider.class.getResourceAsStream("/forest.yaml");
            if (yaml != null) {
                return loadModel(yaml);
            }
            InputStream json = ConfigProvider.class.getResourceAsStream("/forest.json");
            if (json != null) {
                return loadModel(json);
            }
            return Collections.emptyMap();
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> loadModel(File file) throws IOException {
        return loadModel(new FileInputStream(file));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> loadModel(InputStream i) throws IOException {
        try (InputStream is = i) {
            String content = IOUtils.toString(is, StandardCharsets.UTF_8);
            if (isJson(content)) {
                return JSON_PARSER.readValue(content, Map.class);
            } else {
                return YAML_PARSER.readValue(content, Map.class);
            }
        }
    }

    private static boolean isJson(String content) {
        for (int i = 0; i < content.length(); ++i) {
            if (!Character.isWhitespace(content.charAt(i))) {
                return content.charAt(i) == '{' || content.charAt(i) == '[';
            }
        }
        return false;
    }

    private class ConfigObject {
        private final String path;
        private Object defaultValue;
        private final Object configValue;
        private final Object environmentValue;

        public ConfigObject() {
            this("", null, configFileModel, environmentModel);
        }

        public ConfigObject(String path, Object defaultValue, Object configValue, Object environmentValue) {
            this.path = path;
            this.defaultValue = defaultValue;
            this.configValue = configValue;
            this.environmentValue = environmentValue;
        }

        public ConfigObject getObject(String key) {
            String childPath = path.isEmpty() ? key : path + "." + key;
            Supplier<?> defaultOptionsSupplier = defaultOptions.get(childPath);
            Object childDefaultValue = defaultOptionsSupplier == null ? PropertyUtils.getProperty(defaultValue, key) : defaultOptionsSupplier.get();
            Object childConfigValue = PropertyUtils.getProperty(configValue, key);
            Object childEnvironmentValue = PropertyUtils.getProperty(environmentValue, key);
            return new ConfigObject(childPath, childDefaultValue, childConfigValue, childEnvironmentValue);
        }

        public Object getResult(Class<?> klass) {
            Object rawResult = getRawResult();
            if (rawResult == null) {
                return null;
            } else if (isNonNestedPropertyType(klass)) {
                return DefaultConverter.INSTANCE.convert(rawResult, rawResult.getClass(), klass);
            } else {
                // Java bean
                mergeConfigValue();
                return DefaultConverter.INSTANCE.convert(defaultValue, defaultValue.getClass(), klass);
            }
        }

        // BeanUtils.copyProperties() is shallow copy, so we filter out non-nested properties, invoke copyProperties,
        // then manually copy nested fields recursively
        @SuppressWarnings("unchecked")
        private void mergeConfigValueToDefault(Object destBean, Map<String, Object> srcMap) {
            if (defaultValue == null) {
                defaultValue = srcMap;
                return;
            }
            if (srcMap != null) {
                srcMap.forEach((String key, Object value) -> {
                    if (value == null || isNonNestedPropertyType(value.getClass())) {
                        PropertyUtils.setProperty(destBean, key, value);
                    } else {
                        Object destBeanProperty = PropertyUtils.getProperty(destBean, key);
                        if (destBeanProperty == null) {
                            PropertyUtils.setProperty(destBean, key, value);
                        } else {
                            mergeConfigValueToDefault(PropertyUtils.getProperty(destBean, key), (Map<String, Object>) value);
                        }
                    }
                });
            }
        }


        private boolean isNonNestedPropertyType(Class<?> klass) {
            return klass.isPrimitive()
                || klass == Boolean.class
                || Number.class.isAssignableFrom(klass)
                || klass == String.class
                || klass.isEnum()
                || List.class.isAssignableFrom(klass);
        }

        @SuppressWarnings("unchecked")
        private Object mergeConfigValue() {
            mergeConfigValueToDefault(defaultValue, (Map<String, Object>) configValue);
            mergeConfigValueToDefault(defaultValue, (Map<String, Object>) environmentValue);
            return defaultValue;
        }

        private Object getRawResult() {
            if (environmentValue != null) {
                return environmentValue;
            } else if (configValue != null) {
                return configValue;
            } else {
                return defaultValue;
            }
        }
    }

}
