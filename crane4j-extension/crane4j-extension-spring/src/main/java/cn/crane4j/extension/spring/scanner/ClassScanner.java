package cn.crane4j.extension.spring.scanner;

import cn.crane4j.core.util.ClassUtils;
import cn.crane4j.core.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>A class scanner based on {@link ResourcePatternResolver} and {@link MetadataReaderFactory}
 * that scans all classes under the specified package.
 *
 * @author huangchengxing
 */
@Slf4j
@RequiredArgsConstructor
public class ClassScanner {

    public static final ClassScanner INSTANCE = new ClassScanner();
    public static final String CLASS_SUFFIX = ".class";
    public static final String ALL = "*";
    public static final String ALL_RECURSIVE = "**";

    private final MetadataReaderFactory metadataReaderFactory;
    private final ResourcePatternResolver resourcePatternResolver;

    /**
     * Create a {@link ClassScanner} instance.
     */
    public ClassScanner() {
        this(new CachingMetadataReaderFactory(), new PathMatchingResourcePatternResolver());
    }

    /**
     * <p>Scan all classes under the specified package.
     *
     * @param basePackages the specified package
     * @return a set of classes
     */
    public Set<Class<?>> scan(String... basePackages) {
        return Stream.of(basePackages)
            .filter(StringUtils::isNotEmpty)
            .map(this::resolvePath)
            .map(this::doScan)
            .flatMap(Collection::stream)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private String resolvePath(String path) {
        // it already specifies the path of the class
        if (path.endsWith(CLASS_SUFFIX)) {
            String pathNotWithClassSuffix = path.substring(0, path.length() - CLASS_SUFFIX.length());
            return ClassUtils.packageToPath(pathNotWithClassSuffix) + CLASS_SUFFIX;
        }
        path = ClassUtils.packageToPath(path);
        if (path.endsWith(ALL_RECURSIVE)) {
            return path + "/*.class";
        }
        if (path.endsWith(ALL)) {
            return path + CLASS_SUFFIX;
        }
        return path + "/**/*.class";
    }

    private Set<Class<?>> doScan(String packageSearchPath) {
        try {
            return Stream.of(resourcePatternResolver.getResources(packageSearchPath))
                .filter(Resource::isReadable)
                .map(this::getMetadataReader)
                .filter(Objects::nonNull)
                .map(MetadataReader::getClassMetadata)
                .map(ClassMetadata::getClassName)
                .map(ClassUtils::forName)
                .collect(Collectors.toSet());
        } catch (IOException e) {
            log.error("scan path [{}] failed", packageSearchPath, e);
        }
        return Collections.emptySet();
    }

    private MetadataReader getMetadataReader(Resource resource) {
        try {
            return metadataReaderFactory.getMetadataReader(resource);
        } catch (IOException e) {
            log.error("get metadata reader from resource [{}] failed", resource, e);
        }
        return null;
    }
}
