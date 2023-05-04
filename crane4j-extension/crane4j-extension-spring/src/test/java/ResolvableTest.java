import org.junit.Test;
import org.springframework.core.ResolvableType;

/**
 * @author huangchengxing
 */
public class ResolvableTest {

    @Test
    public void test() {
        ResolvableType t = ResolvableType.forClass(Interface.class, Grandson.class);
        System.out.println(t.getGeneric(0));
    }


    private interface Interface<T> {}
    private static class Super<T> implements Interface<T> {}
    private static class Child<T> extends Super<Integer> {}
    private static class Grandson extends Child<Boolean> {}
}
