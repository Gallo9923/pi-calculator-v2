package datastructures;

import java.math.BigInteger;

public interface BalancedBSTree<K extends Comparable<K>, V> {
	void add(K key, V value);
	BigInteger search(K key);
	void delete(K key);
	boolean contains(K key);
	// List<V> autoComplete(String key, int maxSize);
}
