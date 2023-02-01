package cn.createsequence.crane4j.core.support;

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
