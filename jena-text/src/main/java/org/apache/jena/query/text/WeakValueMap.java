package org.apache.jena.query.text;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * A map with strong references to the keys and weak references to the values.
 *
 */
public class WeakValueMap<Key,Value> extends AbstractWeakValueMap<Key,Value> {
	private HashMap<Key,WeakReference<Value>> map = new HashMap<Key,WeakReference<Value>>();

	@Override
	protected Map<Key, WeakReference<Value>> map() {
		return map;
	}
}
