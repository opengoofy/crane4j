package cn.crane4j.core.support;

/**
 * A component with a name.
 *
 * @author huangchengxing
 * @see Crane4jGlobalConfiguration
 */
public interface NamedComponent {

    /**
     * Get the component name.
     *
     * @return String
     */
    default String getName() {
        return this.getClass().getSimpleName();
    }
}
