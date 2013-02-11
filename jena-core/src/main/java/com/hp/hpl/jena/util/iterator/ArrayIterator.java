/*
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

//ArrayIterator.java
package com.hp.hpl.jena.util.iterator;
import java.util.Iterator;
import java.lang.reflect.Array;
import java.util.NoSuchElementException ;

/** An Iterator for arrays  
 * @deprecated Use <code> Arrays.asList( array ).iterator();</code>
 */
@Deprecated
public class ArrayIterator<T> implements Iterator<T> {
	private int i;
	private T[] a;
	/** Constructs an iterator over the members of an array.
         * All arrays are supported including primitive types.
         * @param array Must be an array.
 */
	public ArrayIterator(T[] array) {
		i = 0;
		a = array;
		if (!a.getClass().isArray())
			throw new ArrayStoreException();
	}
	@Override
    public boolean hasNext() {
		return i<Array.getLength(a);
	}
	@Override
    public T next() throws NoSuchElementException {
		try {
			return a[i++]; // Array.get(a,i++);
		}
		catch (IndexOutOfBoundsException e) {
			throw new NoSuchElementException();
		}
	}
/** Not supported.
 * @throws java.lang.UnsupportedOperationException Always.
 */        
        @Override
        public void remove() {

            throw new UnsupportedOperationException();
        }
}
