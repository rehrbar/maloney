package ch.hsr.maloney.util;

/**
 * @author oniet
 *
 * Simple Tuple for saving of simple data structures
 *
 */

/**
 * Creates a tuple for easier storage in e.g. queues or lists.
 *
 * @param <K> Type of left value
 * @param <V> Type of right value
 */
public class Tuple<K,V>{
    final private K left;
    final private V right;

    public Tuple(K left, V right) {
        this.left = left;
        this.right = right;
    }
    public K getLeft() {
        return left;
    }

    public V getRight() {
        return right;
    }

}
