package cn.crane4j.annotation;

import lombok.RequiredArgsConstructor;

/**
 * An enumeration that defines the strategy for handling duplicate keys.
 *
 * @author huangchengxing
 * @since 2.2.0
 */
@RequiredArgsConstructor
public enum DuplicateStrategy {

    /**
     * Throws an exception when the key already exists.
     *
     * @see IllegalArgumentException
     */
    ALERT(new Selector() {
        @Override
        public <K, V> V choose(K key, V oldVal, V newVal) {
            throw new IllegalArgumentException(
                "Duplicate key [" + key + "] has been associated with value [" + oldVal + "],"
                    + " can no longer be associated with [" + newVal +"]"
            );
        }
    }),

    /**
     * When the key already exists, discard the old key value.
     */
    DISCARD_NEW(new Selector() {
        @Override
        public <K, V> V choose(K key, V oldVal, V newVal) {
            return oldVal;
        }
    }),

    /**
     * When the keys are the same, discard the new key value.
     */
    DISCARD_OLD(new Selector() {
        @Override
        public <K, V> V choose(K key, V oldVal, V newVal) {
            return newVal;
        }
    }),

    /**
     * When the keys are the same, discard the new value and old value, return null.
     */
    DISCARD(new Selector() {
        @Override
        public <K, V> V choose(K key, V oldVal, V newVal) {
            return null;
        }
    })

    ;

    /**
     * selector
     */
    private final Selector selector;

    /**
     * Choose to return one of the old and new values.
     *
     * @param key key
     * @param oldVal old val
     * @param newVal new val
     * @return val
     */
    public <K, V> V choose(K key, V oldVal, V newVal) {
        return selector.choose(key, oldVal, newVal);
    }

    /**
     * Internal interface, used to select the value to be returned when the key is duplicated.
     *
     * @author huangchengxing
     */
    @FunctionalInterface
    private interface Selector {
        
        /**
         * Choose to return one of the old and new values.
         *
         * @param key key
         * @param oldVal old val
         * @param newVal new val
         * @return val
         */
        <K, V> V choose(K key, V oldVal, V newVal);
    }
}
