package cn.crane4j.core.support.auto;

import cn.crane4j.annotation.AutoOperate;
import cn.crane4j.core.util.Asserts;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.AnnotatedElement;
import java.util.List;
import java.util.Objects;

/**
 * An implementation of {@link AutoOperateAnnotatedElementResolver} that composes multiple resolvers.
 *
 * @author huangchengxing
 * @since 2.3.0
 */
@RequiredArgsConstructor
public class ComposableAutoOperateAnnotatedElementResolver implements AutoOperateAnnotatedElementResolver {

    @Getter
    private final List<AutoOperateAnnotatedElementResolver> resolvers;

    /**
     * Add a {@link AutoOperateAnnotatedElementResolver} to the end of the resolver list.
     *
     * @param resolver resolver
     */
    public void addResolver(AutoOperateAnnotatedElementResolver resolver) {
        if (resolver == this) {
            return;
        }
        Asserts.isNotNull(resolver, "resolver must not be null");
        resolvers.remove(resolver);
        resolvers.add(resolver);
    }

    /**
     * Remove a {@link AutoOperateAnnotatedElementResolver} from the resolver list.
     *
     * @param resolver resolver
     */
    public void removeResolver(AutoOperateAnnotatedElementResolver resolver) {
        resolvers.remove(resolver);
    }

    /**
     * Whether the resolver supports the element.
     *
     * @param element    element
     * @param annotation annotation
     * @return true if supports, otherwise false
     */
    @Override
    public boolean support(AnnotatedElement element, @Nullable AutoOperate annotation) {
        return resolvers.stream()
            .anyMatch(resolver -> resolver.support(element, annotation));
    }

    /**
     * Resolve the {@link AutoOperate} annotation on the element
     * and build {@link AutoOperateAnnotatedElement} for it according to its configuration.
     *
     * @param element    element
     * @param annotation annotation
     * @return {@link AutoOperateAnnotatedElement}
     */
    @Nullable
    @Override
    public AutoOperateAnnotatedElement resolve(
        AnnotatedElement element, @Nullable AutoOperate annotation) {
        return resolvers.stream()
            .filter(resolver -> resolver.support(element, annotation))
            .map(resolver -> resolver.resolve(element, annotation))
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);
    }
}
