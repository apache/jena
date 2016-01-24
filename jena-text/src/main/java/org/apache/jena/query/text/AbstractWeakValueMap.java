/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
