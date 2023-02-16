package cn.crane4j.core.support;

import lombok.Getter;
import org.junit.Assert;
import org.junit.Test;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * test for {@link Grouped}
 *
 * @author huangchengxing
 */
public class GroupedTest {

    @Test
    public void allMatch() {
        Assert.assertTrue(Grouped.allMatch().test(new Foo()));
        Assert.assertTrue(Grouped.allMatch().test(new Foo("1", "2")));
        Assert.assertTrue(Grouped.allMatch("1").test(new Foo("1")));
        Assert.assertTrue(Grouped.allMatch("1").test(new Foo("1", "2")));
        Assert.assertTrue(Grouped.allMatch("1", "2").test(new Foo("1", "2")));
        Assert.assertFalse(Grouped.allMatch("1", "2").test(new Foo("3")));
    }

    @Test
    public void noneMatch() {
        Assert.assertTrue(Grouped.noneMatch().test(new Foo()));
        Assert.assertTrue(Grouped.noneMatch().test(new Foo("1", "2")));
        Assert.assertFalse(Grouped.noneMatch("1", "2").test(new Foo("1", "2")));
        Assert.assertFalse(Grouped.noneMatch("1", "2").test(new Foo("1")));
        Assert.assertTrue(Grouped.noneMatch("1", "2").test(new Foo("3")));
    }

    @Test
    public void anyMatch() {
        Assert.assertFalse(Grouped.anyMatch().test(new Foo()));
        Assert.assertFalse(Grouped.anyMatch().test(new Foo("1", "2")));
        Assert.assertTrue(Grouped.anyMatch("1", "2").test(new Foo("1", "2")));
        Assert.assertTrue(Grouped.anyMatch("1", "2").test(new Foo("1")));
        Assert.assertFalse(Grouped.anyMatch("1", "2").test(new Foo("3")));
    }

    @Test
    public void getGroups() {
        Assert.assertTrue(new Empty().getGroups().isEmpty());

        String[] groups = {"123", "321"};
        Foo foo = new Foo(groups);
        Assert.assertEquals(
            Stream.of(groups).collect(Collectors.toSet()),
            foo.getGroups()
        );
    }

    @Test
    public void isBelong() {
        String[] groups = {"123", "321"};
        Foo foo = new Foo(groups);
        Assert.assertTrue(foo.isBelong("123"));
        Assert.assertTrue(foo.isBelong("321"));
        Assert.assertFalse(foo.isBelong("none"));
    }

    private static class Empty implements Grouped {

    }

    @Getter
    private static class Foo implements Grouped {
        private final Set<String> groups;
        Foo(String... args) {
            this.groups = Stream.of(args).collect(Collectors.toSet());
        }
    }
}
