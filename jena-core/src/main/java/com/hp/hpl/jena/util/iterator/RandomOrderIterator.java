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

package com.hp.hpl.jena.util.iterator;

import java.util.*;
/**
 * RandomOrderIterator - Reorders the elements returned by an Iterator.
 */
public class RandomOrderIterator<T> extends WrappedIterator<T> {
	private Random rnd = new Random();
    private Object buffer[];
    // one more than the index of the last non-null element.
    int top;
	/**
	 * Wrap the base iterator, randomizing with a buffer of length sz.
	 */
	public RandomOrderIterator(int sz, Iterator<T> base) {
		super(base);
		buffer = new Object[sz];
		top = 0;
		fill();
	}
	
	@Override
    public boolean hasNext() {
		return top > 0;
	}
    @Override
    public T next() {
		int ix = rnd.nextInt(top);
		Object rslt = buffer[ix];
		top--;
		buffer[ix] = buffer[top];
		fill();
	    @SuppressWarnings("unchecked")
	    T obj = (T)rslt;
	    return obj ;
	}
	
	@Override
    public void remove() {
		throw new UnsupportedOperationException("randomizing does not allow modification");
	}
	
	private void fill() {
	   while ( top < buffer.length && super.hasNext() ) {
	   	 buffer[top++] = super.next();
	   }
	}

}
