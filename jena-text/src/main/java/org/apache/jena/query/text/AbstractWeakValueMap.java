package org.apache.jena.query.text;

import java.lang.ref.WeakReference;
import java.util.Map;

/**
 * An abstract map with weak references to values in the map.
 * 
 */
public abstract class AbstractWeakValueMap<Key, Value> {
	 
     protected abstract Map<Key, WeakReference<Value>> map();
	 
     public synchronized Value get(Key key) {
       	 WeakReference<Value> ref = map().get(key);
       	 if (ref == null)
       		 return null;
         Value result = ref.get();
         if (result == null) 
        	 remove(key);
         return result;
     }
	 
     public synchronized void put(Key key, Value value) {
         map().put(key, new WeakReference<>(value));
     }
	 
     public synchronized void remove(Key key) {
         map().remove(key);
     }
}
