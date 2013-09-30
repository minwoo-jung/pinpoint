package com.nhn.pinpoint.web.util;

import java.util.HashMap;

/**
 * 
 * @author netspider
 * 
 * @param <K>
 * @param <V>
 */
public class MergeableHashMap<K, V extends Mergeable<V>> extends HashMap<K, V> implements MergeableMap<K, V> {

	private static final long serialVersionUID = -8474027588052874209L;

	public V putOrMerge(K key, V value) {
		V v = get(key);
		if (v == null) {
			put(key, v = value);
		} else {
			put(key, v.mergeWith(value));
		}
		return v;
	}
}
