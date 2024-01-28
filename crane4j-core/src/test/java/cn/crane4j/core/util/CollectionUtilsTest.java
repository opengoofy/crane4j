package cn.crane4j.core.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * test for {@link CollectionUtils}
 *
 * @author huangchengxing
 */
public class CollectionUtilsTest {

    @Test
    public void split() {
        Assert.assertThrows(IllegalArgumentException.class, () -> CollectionUtils.split(Collections.emptyList(), -1));
        Assert.assertEquals(Collections.emptyList(), CollectionUtils.split(null, 1));
        Assert.assertEquals(Collections.emptyList(), CollectionUtils.split(Collections.emptyList(), 1));

        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5);
        List<Collection<Integer>> splitList = CollectionUtils.split(list, 2);
        Assert.assertEquals(3, splitList.size());
        Assert.assertEquals(2, splitList.get(0).size());
        Assert.assertEquals(2, splitList.get(1).size());
        Assert.assertEquals(1, splitList.get(2).size());

        splitList = CollectionUtils.split(list, 5);
        Assert.assertEquals(1, splitList.size());
        Assert.assertEquals(list, splitList.get(0));
    }

    @Test
    public void getFirstNotNull() {
        // if target is iterator
        Assert.assertNull(CollectionUtils.getFirstNotNull((Iterator<? extends Object>)null));
        Assert.assertNull(CollectionUtils.getFirstNotNull(Collections.emptyIterator()));
        Assert.assertEquals("a", CollectionUtils.getFirstNotNull(Arrays.asList("a", "b").iterator()));
        Assert.assertNull(CollectionUtils.getFirstNotNull(Arrays.asList(null, null).iterator()));
        // if target is iterable
        Assert.assertNull(CollectionUtils.getFirstNotNull((Iterable<? extends Object>)null));
        Assert.assertNull(CollectionUtils.getFirstNotNull(Collections.emptyList()));
        Assert.assertEquals("a", CollectionUtils.getFirstNotNull(Arrays.asList("a", "b")));
        Assert.assertNull(CollectionUtils.getFirstNotNull(Arrays.asList(null, null)));
    }

    @Test
    public void reverse() {
        Assert.assertEquals(Collections.emptyMap(), CollectionUtils.reverse(null));
        Assert.assertEquals(Collections.emptyMap(), CollectionUtils.reverse(Collections.emptyMap()));
        Map<Integer, String> map = new HashMap<>();
        map.put(1, "a");
        map.put(2, "b");
        Map<String, Integer> reversedMap = CollectionUtils.reverse(map);
        Assert.assertEquals(2, reversedMap.size());
        Assert.assertEquals(1, reversedMap.get("a").intValue());
        Assert.assertEquals(2, reversedMap.get("b").intValue());
    }

    @Test
    public void defaultIfEmpty() {
        Assert.assertEquals(Collections.emptyList(), CollectionUtils.defaultIfEmpty(null, Collections.emptyList()));
        Assert.assertEquals(Collections.emptyList(), CollectionUtils.defaultIfEmpty(Collections.emptyList(), Collections.emptyList()));
        Assert.assertEquals(Collections.singletonList(1), CollectionUtils.defaultIfEmpty(Collections.singletonList(1), Collections.emptyList()));
    }

    @Test
    public void containsAny() {
        Assert.assertFalse(CollectionUtils.containsAny(null, null));
        Assert.assertFalse(CollectionUtils.containsAny(Collections.emptyList(), Collections.emptyList()));
        Assert.assertFalse(CollectionUtils.containsAny(Collections.emptyList(), Collections.singletonList(1)));
        Assert.assertFalse(CollectionUtils.containsAny(Collections.singletonList(1), Collections.emptyList()));
        Assert.assertTrue(CollectionUtils.containsAny(Collections.singletonList(1), Collections.singletonList(1)));
        Assert.assertTrue(CollectionUtils.containsAny(Arrays.asList(1, 2), Arrays.asList(1, 2)));
        Assert.assertTrue(CollectionUtils.containsAny(Arrays.asList(1, 2), Arrays.asList(1, 3)));
        Assert.assertFalse(CollectionUtils.containsAny(Arrays.asList(1, 2), Arrays.asList(3, 4)));
        Assert.assertFalse(CollectionUtils.containsAny(Arrays.asList(1, 2), Arrays.asList(3, 4, 5)));
        // any not null
        Assert.assertFalse(CollectionUtils.containsAny(null, Collections.singletonList(1)));
        Assert.assertFalse(CollectionUtils.containsAny(Collections.singletonList(1), null));
        // element siz not equal
        Assert.assertTrue(CollectionUtils.containsAny(Arrays.asList(1, 2), Arrays.asList(1, 2, 3)));
        Assert.assertTrue(CollectionUtils.containsAny(Arrays.asList(1, 2, 3), Arrays.asList(1, 2)));
    }

    @Test
    public void notContainsAny() {
        Assert.assertTrue(CollectionUtils.notContainsAny(null, null));
        Assert.assertTrue(CollectionUtils.notContainsAny(Collections.emptyList(), Collections.emptyList()));
        Assert.assertTrue(CollectionUtils.notContainsAny(Collections.emptyList(), Collections.singletonList(1)));
        Assert.assertTrue(CollectionUtils.notContainsAny(Collections.singletonList(1), Collections.emptyList()));
        Assert.assertFalse(CollectionUtils.notContainsAny(Collections.singletonList(1), Collections.singletonList(1)));
        Assert.assertFalse(CollectionUtils.notContainsAny(Arrays.asList(1, 2), Arrays.asList(1, 2)));
        Assert.assertFalse(CollectionUtils.notContainsAny(Arrays.asList(1, 2), Arrays.asList(1, 3)));
        Assert.assertTrue(CollectionUtils.notContainsAny(Arrays.asList(1, 2), Arrays.asList(3, 4)));
        // any not null
        Assert.assertTrue(CollectionUtils.notContainsAny(null, Collections.singletonList(1)));
        Assert.assertTrue(CollectionUtils.notContainsAny(Collections.singletonList(1), null));
        // element siz not equal
        Assert.assertFalse(CollectionUtils.notContainsAny(Arrays.asList(1, 2), Arrays.asList(1, 2, 3)));
        Assert.assertFalse(CollectionUtils.notContainsAny(Arrays.asList(1, 2, 3), Arrays.asList(1, 2)));
    }

    @Test
    @SuppressWarnings("all")
    public void getForIterable() {
        Assert.assertNull(CollectionUtils.get((Iterable<Object>)null, 0));
        Assert.assertNull(CollectionUtils.get((Iterable<Object>)Collections.emptyList(), 0));
        Assert.assertEquals(1, CollectionUtils.get(Collections.singletonList(1), 0).intValue());
    }

    @Test
    @SuppressWarnings("all")
    public void getForIterator() {
        Assert.assertNull(CollectionUtils.get((Iterator<Object>)null, 0));
        Assert.assertNull(CollectionUtils.get(Collections.emptyIterator(), 0));
        Assert.assertEquals(1, CollectionUtils.get(Collections.singletonList(1).iterator(), 0).intValue());
    }

    @Test
    @SuppressWarnings("all")
    public void getForCollection() {
        Assert.assertNull(CollectionUtils.get((Collection<Object>)null, 0));
        Assert.assertNull(CollectionUtils.get(Collections.emptyList(), 0));
        Assert.assertEquals(1, CollectionUtils.get(Collections.singletonList(1), 0).intValue());
        // if not list
        Assert.assertNull(CollectionUtils.get(Collections.singleton(1), 1));
    }

    @Test
    @SuppressWarnings("all")
    public void newCollection() {
        Assert.assertThrows(NullPointerException.class, () -> CollectionUtils.newCollection(null, 1));
        Assert.assertTrue(CollectionUtils.newCollection(ArrayList::new).isEmpty());
        Assert.assertEquals(1, CollectionUtils.newCollection(ArrayList::new, 1).size());
        Assert.assertEquals(2, CollectionUtils.newCollection(ArrayList::new, 1, 2).size());
        // if array is null or empty
        Assert.assertTrue(CollectionUtils.newCollection(ArrayList::new, (Object[])null).isEmpty());
        Assert.assertTrue(CollectionUtils.newCollection(ArrayList::new, new Object[0]).isEmpty());
    }

    @Test
    @SuppressWarnings("all")
    public void addAllForArray() {
        Assert.assertTrue(CollectionUtils.addAll(null).isEmpty());
        Assert.assertEquals(1, CollectionUtils.addAll(new ArrayList<>(), 1).size());
        Assert.assertEquals(2, CollectionUtils.addAll(new ArrayList<>(), 1, 2).size());
        // if array is null or empty
        Assert.assertTrue(CollectionUtils.addAll(new ArrayList<>(), (Object[])null).isEmpty());
        Assert.assertTrue(CollectionUtils.addAll(new ArrayList<>(), new Object[0]).isEmpty());
    }

    @Test
    public void addAllForCollection() {
        Assert.assertTrue(CollectionUtils.addAll(null, (Collection<Object>)null).isEmpty());
        Assert.assertTrue(CollectionUtils.addAll(new ArrayList<>(), (Collection<Object>)null).isEmpty());
        Assert.assertTrue(CollectionUtils.addAll(null, new ArrayList<>()).isEmpty());
        Assert.assertEquals(1, CollectionUtils.addAll(new ArrayList<>(), Collections.singletonList(1)).size());
        Assert.assertEquals(2, CollectionUtils.addAll(new ArrayList<>(), Arrays.asList(1, 2)).size());
    }

    @Test
    @SuppressWarnings("all")
    public void isEmptyForIterator() {
        Assert.assertTrue(CollectionUtils.isEmpty((Iterator<?>)null));
        Assert.assertTrue(CollectionUtils.isEmpty(Collections.emptyIterator()));
        Assert.assertFalse(CollectionUtils.isEmpty(Collections.singletonList(1).iterator()));
    }

    @Test
    @SuppressWarnings("all")
    public void isNotEmptyForIterator() {
        Assert.assertFalse(CollectionUtils.isNotEmpty((Iterator<?>)null));
        Assert.assertFalse(CollectionUtils.isNotEmpty(Collections.emptyIterator()));
        Assert.assertTrue(CollectionUtils.isNotEmpty(Collections.singletonList(1).iterator()));
    }

    @Test
    @SuppressWarnings("all")
    public void isEmptyForIterable() {
        Assert.assertTrue(CollectionUtils.isEmpty((Iterable<?>)null));
        Assert.assertTrue(CollectionUtils.isEmpty(Collections.emptyList()));
        Assert.assertFalse(CollectionUtils.isEmpty(Collections.singletonList(1)));
    }

    @Test
    @SuppressWarnings("all")
    public void isNotEmptyForIterable() {
        Assert.assertFalse(CollectionUtils.isNotEmpty((Iterable<?>)null));
        Assert.assertFalse(CollectionUtils.isNotEmpty(Collections.emptyList()));
        Assert.assertTrue(CollectionUtils.isNotEmpty(Collections.singletonList(1)));
    }

    @Test
    @SuppressWarnings("all")
    public void isEmptyForMap() {
        Assert.assertTrue(CollectionUtils.isEmpty((Map<?, ?>)null));
        Assert.assertTrue(CollectionUtils.isEmpty(Collections.emptyMap()));
        Assert.assertFalse(CollectionUtils.isEmpty(Collections.singletonMap(1, 1)));
    }

    @Test
    @SuppressWarnings("all")
    public void isNotEmptyForMap() {
        Assert.assertFalse(CollectionUtils.isNotEmpty((Map<?, ?>)null));
        Assert.assertFalse(CollectionUtils.isNotEmpty(Collections.emptyMap()));
        Assert.assertTrue(CollectionUtils.isNotEmpty(Collections.singletonMap(1, 1)));
    }

    @Test
    @SuppressWarnings("all")
    public void isEmptyForCollection() {
        Assert.assertTrue(CollectionUtils.isEmpty((Collection<?>)null));
        Assert.assertTrue(CollectionUtils.isEmpty(Collections.emptyList()));
        Assert.assertFalse(CollectionUtils.isEmpty(Collections.singletonList(1)));
    }

    @Test
    @SuppressWarnings("all")
    public void isNotEmptyForCollection() {
        Assert.assertFalse(CollectionUtils.isNotEmpty((Collection<?>)null));
        Assert.assertFalse(CollectionUtils.isNotEmpty(Collections.emptyList()));
        Assert.assertTrue(CollectionUtils.isNotEmpty(Collections.singletonList(1)));
    }

    @Test
    public void newWeakConcurrentMap() {
        Assert.assertNotNull(CollectionUtils.newWeakConcurrentMap());
    }

    @Test
    public void adaptObjectToCollection() {
        Assert.assertTrue(CollectionUtils.adaptObjectToCollection(null).isEmpty());
        Assert.assertTrue(CollectionUtils.adaptObjectToCollection(new Object[0]).isEmpty());
        Assert.assertTrue(CollectionUtils.adaptObjectToCollection(Collections.emptyList()).isEmpty());
        Assert.assertEquals(1, CollectionUtils.adaptObjectToCollection(Collections.emptyMap()).size());
        Assert.assertEquals(1, CollectionUtils.adaptObjectToCollection(new Object()).size());
        Assert.assertEquals(1, CollectionUtils.adaptObjectToCollection(Collections.singletonList(1).iterator()).size());
        Assert.assertEquals(1, CollectionUtils.adaptObjectToCollection((Iterable<Integer>)() -> (Iterator<Integer>)Collections.singletonList(1).iterator()).size());
    }

    @Test
    public void computeIfAbsent() {
        Map<Integer, Integer> map = new HashMap<>();
        Assert.assertEquals(1, CollectionUtils.computeIfAbsent(map, 1, k -> 1).intValue());
        Assert.assertEquals(1, CollectionUtils.computeIfAbsent(map, 1, k -> 2).intValue());
    }
}
