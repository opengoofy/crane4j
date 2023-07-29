package cn.crane4j.core.support.reflect;

import cn.crane4j.core.support.MethodInvoker;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

/**
 * test for {@link ReflectiveFieldAdapterMethodInvoker}
 */
public class ReflectiveFieldAdapterMethodInvokerTest {

    private static class MyClass {
        private int value;

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }
    }

    @Test
    public void testCreateGetter() throws NoSuchFieldException {
        MyClass obj = new MyClass();
        obj.setValue(42);

        Field field = MyClass.class.getDeclaredField("value");
        ReflectiveFieldAdapterMethodInvoker invoker = ReflectiveFieldAdapterMethodInvoker.createGetter(field);

        assertEquals(42, invoker.invoke(obj));
    }

    @Test
    public void testCreateSetter() throws NoSuchFieldException {
        MyClass obj = new MyClass();

        Field field = MyClass.class.getDeclaredField("value");
        ReflectiveFieldAdapterMethodInvoker invoker = ReflectiveFieldAdapterMethodInvoker.createSetter(field);

        invoker.invoke(obj, 42);

        assertEquals(42, obj.getValue());
    }

    @Test
    public void testCreateGetterWithInaccessibleField() throws NoSuchFieldException {
        MyClass obj = new MyClass();
        Field field = MyClass.class.getDeclaredField("value");
        MethodInvoker getter = ReflectiveFieldAdapterMethodInvoker.createGetter(field);
        field.setAccessible(false);
        assertThrows(IllegalStateException.class, () -> getter.invoke(obj));
    }

    @Test
    public void testCreateSetterWithInaccessibleField() throws NoSuchFieldException {
        MyClass obj = new MyClass();
        Field field = MyClass.class.getDeclaredField("value");
        MethodInvoker setter = ReflectiveFieldAdapterMethodInvoker.createSetter(field);
        field.setAccessible(false);
        assertThrows(IllegalStateException.class, () -> setter.invoke(obj, 42));
    }
}