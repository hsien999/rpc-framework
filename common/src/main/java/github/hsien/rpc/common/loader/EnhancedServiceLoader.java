package github.hsien.rpc.common.loader;

import github.hsien.rpc.common.loader.annotion.LoadLevel;
import github.hsien.rpc.common.loader.annotion.SPI;
import github.hsien.rpc.common.loader.exception.InitializeException;
import github.hsien.rpc.common.loader.exception.LoadLevelException;
import github.hsien.rpc.common.singleton.SingletonHolder;
import github.hsien.rpc.common.util.CollectionUtils;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Extension service loader for SPI
 *
 * @author hsien
 */
public final class EnhancedServiceLoader {

    /**
     * @see #load(Class, String, Class[], Object[], ClassLoader)
     */
    public static <S> S load(Class<S> type) {
        return InnerServiceLoader.getServiceLoader(type).load(defaultClassLoader());
    }

    /**
     * @see #load(Class, String, Class[], Object[], ClassLoader)
     */
    public static <S> S load(Class<S> type, ClassLoader loader) {
        return InnerServiceLoader.getServiceLoader(type).load(loader);
    }

    /**
     * @see #load(Class, String, Class[], Object[], ClassLoader)
     */
    public static <S> S load(Class<S> type, String name) {
        return InnerServiceLoader.getServiceLoader(type).load(name, defaultClassLoader());
    }

    /**
     * @see #load(Class, String, Class[], Object[], ClassLoader)
     */
    public static <S> S load(Class<S> type, String name, ClassLoader loader) {
        return InnerServiceLoader.getServiceLoader(type).load(name, loader);
    }

    /**
     * @see #load(Class, String, Class[], Object[], ClassLoader)
     */
    public static <S> S load(Class<S> type, String name, Object... args) {
        return InnerServiceLoader.getServiceLoader(type).load(name, args, defaultClassLoader());
    }

    /**
     * @see #load(Class, String, Class[], Object[], ClassLoader)
     */
    public static <S> S load(Class<S> type, String name, Object[] args, ClassLoader loader) {
        return InnerServiceLoader.getServiceLoader(type).load(name, args, loader);
    }

    /**
     * @see #load(Class, String, Class[], Object[], ClassLoader)
     */
    public static <S> S load(Class<S> type, String name, Class<?>[] argsType, Object... args) {
        return InnerServiceLoader.getServiceLoader(type).load(name, argsType, args, defaultClassLoader());
    }

    /**
     * Load an instance from SPI Loader
     *
     * @param type     type of service instance to be loaded
     * @param name     name of service instance to be loaded
     * @param argsType argument types passed in the constructor of service class
     * @param args     argument values passed in the constructor of service class
     * @param loader   class loader
     * @param <S>      type parameter of service class
     * @return service instance
     */
    public static <S> S load(Class<S> type, String name, Class<?>[] argsType, Object[] args, ClassLoader loader) {
        return InnerServiceLoader.getServiceLoader(type).load(name, argsType, args, loader);
    }

    /**
     * @see #loadAll(Class, Class[], Object[], ClassLoader)
     */
    public static <S> List<S> loadAll(Class<S> type) {
        return InnerServiceLoader.getServiceLoader(type).loadAll(defaultClassLoader());
    }

    /**
     * @see #loadAll(Class, Class[], Object[], ClassLoader)
     */
    public static <S> List<S> loadAll(Class<S> type, ClassLoader loader) {
        return InnerServiceLoader.getServiceLoader(type).loadAll(loader);
    }

    /**
     * @see #loadAll(Class, Class[], Object[], ClassLoader)
     */
    public static <S> List<S> loadAll(Class<S> type, Class<?>[] argsType, Object... args) {
        return InnerServiceLoader.getServiceLoader(type).loadAll(argsType, args, defaultClassLoader());
    }

    /**
     * Load all instances from SPI Loader
     *
     * @param type     type of service instance to be loaded
     * @param argsType argument types passed in the constructor of service class
     * @param args     argument values passed in the constructor of service class
     * @param loader   class loader
     * @param <S>      type parameter of service class
     * @return service instance list
     */
    public static <S> List<S> loadAll(Class<S> type, Class<?>[] argsType, Object[] args, ClassLoader loader) {
        return InnerServiceLoader.getServiceLoader(type).loadAll(argsType, args, loader);
    }

    private static ClassLoader defaultClassLoader() {
        return EnhancedServiceLoader.class.getClassLoader();
    }

    /**
     * Inner service loader for specified interface
     */
    private static final class InnerServiceLoader<S> {
        private static final Logger LOGGER = LoggerFactory.getLogger(InnerServiceLoader.class);
        private static final ConcurrentHashMap<Class<?>, InnerServiceLoader<?>> SERVICE_LOADER = new ConcurrentHashMap<>();
        private static final String SERVICE_DIRECTORY = "META-INF/services/";
        private static final String SERVICE_FILE_COMMENT = "#";

        private final Class<S> type;
        private final SingletonHolder<List<ExtensionDefinition<S>>> extensionsHolder = new SingletonHolder<>();
        private final ConcurrentHashMap<String, List<ExtensionDefinition<S>>> nameToExtensionsMap = new ConcurrentHashMap<>();
        private final ConcurrentHashMap<ExtensionDefinition<S>, SingletonHolder<S>> extensionToInstanceMap = new ConcurrentHashMap<>();

        private InnerServiceLoader(Class<S> type) {
            this.type = type;
        }

        @SuppressWarnings("unchecked")
        private static <S> InnerServiceLoader<S> getServiceLoader(Class<S> type) {
            if (type == null) {
                throw new IllegalArgumentException("Service type should not be null");
            }
            if (!type.isAnnotationPresent(SPI.class)) {
                throw new IllegalArgumentException("Service type should be annotated by " + SPI.class.getName());
            }
            return (InnerServiceLoader<S>) SERVICE_LOADER.computeIfAbsent(type, k -> new InnerServiceLoader<>(type));
        }

        private S load(ClassLoader loader) {
            return loadExtInstance(null, null, null, loader);
        }

        private S load(String name, ClassLoader loader) {
            return loadExtInstance(name, null, null, loader);
        }

        private S load(String name, Object[] args, ClassLoader loader) {
            int len = args.length;
            Class<?>[] argsType = new Class[len];
            for (int i = 0; i < len; i++) {
                argsType[i] = args[i].getClass();
            }
            return loadExtInstance(name, argsType, args, loader);
        }

        private S load(String name, Class<?>[] argsType, Object[] args, ClassLoader loader) {
            return loadExtInstance(name, argsType, args, loader);
        }

        private List<S> loadAll(ClassLoader loader) {
            return loadAll(null, null, loader);
        }

        private List<S> loadAll(Class<?>[] argsType, Object[] args, ClassLoader loader) {
            return loadAllExtInstances(argsType, args, loader);
        }

        private S loadExtInstance(String name, Class<?>[] argTypes, Object[] args, ClassLoader loader) {
            if (name != null && name.isEmpty()) {
                throw new IllegalArgumentException("Got an empty extension name for service: " + type.getName());
            }
            try {
                // load all extensions into extensionsList and nameToExtensionsMap in thread-safe
                extensionsHolder.setIfAbsent(() -> getAllExtensions(loader));
                // get extension with max priority in thread-safe
                ExtensionDefinition<S> extension = (name == null ? CollectionUtils.lastElementInSafe(extensionsHolder.get())
                    : CollectionUtils.lastElementInSafe(nameToExtensionsMap.get(name.toLowerCase())));
                if (extension == null) {
                    throw new ServiceLoaderException("Failed to find a compatible extension for service: " + type.getName());
                }
                // get instance by extensionToInstanceMap in thread-safe
                S instance = getExtensionInstance(extension, argTypes, args);
                initializeInstance(instance);
                return instance;
            } catch (Throwable t) {
                throw new ServiceLoaderException("Failed to load extension for: " + type.getName(), t);
            }
        }

        private List<S> loadAllExtInstances(Class<?>[] argsType, Object[] args, ClassLoader loader) {
            List<S> instances = new ArrayList<>();
            List<ExtensionDefinition<S>> extensions = getAllExtensions(loader);
            if (CollectionUtils.isEmpty(extensions)) {
                return instances;
            }
            try {
                for (ExtensionDefinition<S> extension : extensions) {
                    instances.add(getExtensionInstance(extension, argsType, args));
                }
                return instances;
            } catch (Throwable t) {
                throw new ServiceLoaderException("Failed to load extension for: " + type.getName(), t);
            }
        }

        private List<ExtensionDefinition<S>> getAllExtensions(ClassLoader loader) {
            String filePath = SERVICE_DIRECTORY + type.getName();
            try {
                // load all extensions into extensionsList and nameToExtensionsMap
                List<ExtensionDefinition<S>> extensionsList = loadExtensionsFromFile(filePath, loader);
                if (extensionsList.size() == 0) {
                    SERVICE_LOADER.remove(type);
                    LOGGER.warn("No available config is found for service [{}] in {}", type.getName(), SERVICE_LOADER);
                }
                extensionsList.forEach(extension -> {
                    if (extension.getName() != null) {
                        nameToExtensionsMap.computeIfAbsent(extension.getName(), k -> new ArrayList<>()).add(extension);
                    }
                });
                // sort extensions by order
                extensionsList.sort(Comparator.comparingInt(ExtensionDefinition::getPriority));
                nameToExtensionsMap.forEach((name, extensions) -> extensions.sort(Comparator.comparingInt(ExtensionDefinition::getPriority)));
                return extensionsList;
            } catch (IOException e) {
                throw new IllegalStateException("Failed to load extensions in " + filePath + " for service: " + type.getName(), e);
            }
        }

        private S getExtensionInstance(@NonNull ExtensionDefinition<S> extension, Class<?>[] argTypes, Object[] args) {
            switch (extension.getScope()) {
                case SINGLETON:
                    SingletonHolder<S> holder = extensionToInstanceMap.computeIfAbsent(extension, k -> new SingletonHolder<>());
                    return holder.setIfAbsent(() -> createNewInstance(extension.getServiceClass(), argTypes, args));
                case PROTOTYPE:
                    return createNewInstance(extension.getServiceClass(), argTypes, args);
                default:
                    throw new IllegalStateException("Unknown scope type: " + extension.getScope());
            }
        }

        private List<ExtensionDefinition<S>> loadExtensionsFromFile(@NonNull String filePath, ClassLoader loader) throws IOException {
            List<ExtensionDefinition<S>> extensionsList = new ArrayList<>();
            Set<String> clazzSet = new HashSet<>();
            Enumeration<URL> urls = loader != null ? loader.getResources(filePath)
                : ClassLoader.getSystemResources(filePath);
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(),
                    StandardCharsets.UTF_8))) {
                    String line, className;
                    while ((line = reader.readLine()) != null) {
                        // read line with both of suffixed comment and bilateral whitespace removed
                        final int ci = line.indexOf(SERVICE_FILE_COMMENT);
                        if (ci > -1) {
                            line = line.substring(0, ci);
                        }
                        className = line.trim();
                        if (className.length() == 0) {
                            continue;
                        }
                        try {
                            Class<?> clazz = Class.forName(className, true, loader);
                            // identify extension definition by class meta(class name & class loader)
                            if (!clazzSet.contains(clazz.toString())) {
                                ExtensionDefinition<S> extension = loadExtensionFromClass(clazz);
                                clazzSet.add(clazz.toString());
                                extensionsList.add(extension);
                            } else {
                                LOGGER.warn("Duplicated classes [{}] found in {}", clazz, filePath);
                            }
                        } catch (LinkageError | ClassNotFoundException | LoadLevelException e) {
                            LOGGER.warn("Failed to load extension class [{}] in {}", className, filePath, e);
                        } catch (ClassCastException e) {
                            LOGGER.warn("Failed to load extension class [{}] in {}"
                                    + "Make sure that class is assignment-compatible with the type: {}",
                                className, filePath, type.getName(), e);
                        }
                    }
                }
            }
            return extensionsList;
        }

        @SuppressWarnings("unchecked")
        private ExtensionDefinition<S> loadExtensionFromClass(@NonNull Class<?> clazz) {
            if (!type.isAssignableFrom(clazz)) {
                throw new ClassCastException("Can not cast " + clazz.getName() + " to " + type.getName());
            }
            Class<S> serviceClass = (Class<S>) clazz;
            LoadLevel loadLevel = serviceClass.getAnnotation(LoadLevel.class);
            if (loadLevel == null) {
                throw new LoadLevelException("Service class " + clazz.getName() + " should be annotated by "
                    + LoadLevel.class.getName());
            }
            String name = loadLevel.name();
            if (name != null) {
                name = name.trim().toLowerCase();
                if (name.isEmpty()) {
                    throw new LoadLevelException("Service name should not be empty in " + clazz.getName());
                }
            }
            return new ExtensionDefinition<>(name, loadLevel.priority(), loadLevel.scope(), serviceClass);
        }

        private S createNewInstance(@NonNull Class<S> clazz, Class<?>[] argTypes, Object[] args) {
            S instance;
            try {
                if (argTypes != null && args != null) {
                    Constructor<S> constructor = clazz.getDeclaredConstructor(argTypes);
                    instance = constructor.newInstance(args);
                } else {
                    instance = clazz.newInstance();
                }
            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException
                     | IllegalAccessException e) {
                throw new IllegalStateException("Failed to create a new instance for " + clazz.getName(), e);
            }
            return instance;
        }

        private void initializeInstance(S instance) {
            try {
                if (instance instanceof Initializable) {
                    ((Initializable) instance).initialize();
                }
            } catch (Exception e) {
                throw new InitializeException("Failed to initialize instance", e);
            }
        }
    }
}
