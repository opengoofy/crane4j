package cn.crane4j.core.support;

import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.Containers;
import cn.crane4j.core.container.LambdaContainer;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * test for {@link DefaultContainerAdapterRegister}
 *
 * @author huangchengxing
 */
public class DefaultContainerAdapterRegisterTest {

    private DefaultContainerAdapterRegister register = new DefaultContainerAdapterRegister();

    @Test
    public void testRegisterAdapter() {
        DefaultContainerAdapterRegister register = new DefaultContainerAdapterRegister();
        Assert.assertNull(register.getAdapter(Void.TYPE));
        ContainerAdapterRegister.Adapter adapter = (ns, t) -> Containers.empty();
        register.registerAdapter(Void.TYPE, adapter);
        Assert.assertSame(adapter, register.getAdapter(Void.TYPE));
    }

    @Test
    public void testAdaptFunction() {
        ContainerAdapterRegister.Adapter functionAdapter = register.getAdapter(DataProvider.class);
        Assert.assertNotNull(functionAdapter);
        DataProvider<Object, Object> dp = ids -> Collections.emptyMap();
        Assert.assertEquals(functionAdapter, register.getAdapter(dp.getClass()));
        Container<Object> functionContainer = register.wrapIfPossible("test", dp);
        Assert.assertNotNull(functionContainer);
        Assert.assertEquals("test", functionContainer.getNamespace());
    }
    @Test
    public void testAdaptContainer() {
        ContainerAdapterRegister.Adapter containerAdapter = register.getAdapter(LambdaContainer.class);
        Assert.assertNotNull(containerAdapter);
        Assert.assertEquals(containerAdapter, register.getAdapter(Container.class));
        Container<Object> container = Containers.empty();
        Assert.assertSame(container, register.wrapIfPossible(Container.EMPTY_CONTAINER_NAMESPACE, container));
    }

    @Test
    public void testAdaptMap() {
        ContainerAdapterRegister.Adapter mapAdapter = register.getAdapter(LinkedHashMap.class);
        Assert.assertNotNull(mapAdapter);
        Assert.assertSame(mapAdapter, register.getAdapter(Map.class));
        Map<String, Object> map = new HashMap<>();
        Assert.assertEquals(map, register.wrapIfPossible("test", map).get(Collections.emptyList()));
    }
}
