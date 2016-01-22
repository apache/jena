package org.apache.jena.query.text;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * A map with with weak references to both keys and values.
 */

public class WeakWeakValueMap<Key,Value> extends AbstractWeakValueMap<Key,Value> {
	private WeakHashMap<Key,WeakReference<Value>> map = new WeakHashMap<Key,WeakReference<Value>>();

	@Override
	protected Map<Key, WeakReference<Value>> map() {
		return map;
	}

}
