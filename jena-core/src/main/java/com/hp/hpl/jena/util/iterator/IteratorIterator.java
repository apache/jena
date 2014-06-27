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

import java.util.Iterator;
import java.util.NoSuchElementException;

/** Given an Iterator that returns Iterator's, this creates an
 * Iterator over the next level values.
 * Similar to list splicing in lisp.
 * @deprecated use {@linkplain com.hp.hpl.jena.util.iterator.WrappedIterator#create}
 */
@Deprecated
public class IteratorIterator<T> implements Iterator<T>
{
	private Iterator<Iterator<T>> top;
	private Iterator<T> currentMember;

/** The first element of this Iterator is the first element of the
 * first non-empty element of <code>e</code>.
 * @param e An Iterator all of whose members are themselves Iterator's.
 */        
	public IteratorIterator(Iterator<Iterator<T>> e) {
		top = e;
		currentMember = null;
	}

/** Is there another element in one of the Iterator's
 * still to consider.
 */        
	@Override
    public boolean hasNext() {
		while ( currentMember == null || !currentMember.hasNext() ) {
			if (!top.hasNext())
				return false;
			currentMember = top.next();
		}
		return true;
	}

	@Override
    public T next() {
		hasNext();
		if (currentMember == null)
			throw new NoSuchElementException();
		return currentMember.next();
	}
/** remove's the element from the underlying Iterator
 * in which it is a member.
 */        
        @Override
        public void remove() {
	  if (currentMember == null)
			throw new IllegalStateException();
          currentMember.remove();
        }
}
