/*
 *  (c) Copyright 2000-2002 Hewlett-Packard Development Company, LP
 *  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.

 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * $Id: ArrayIterator.java,v 1.3 2003-08-27 13:07:54 andy_seaborne Exp $
 *
 */
//ArrayIterator.java
package com.hp.hpl.jena.util.iterator;
import java.util.Iterator;
import java.lang.reflect.Array;
import java.util.NoSuchElementException ;

/** An Iterator for arrays.
 * @author Jeremy Carroll
 * @version Release='$Name: not supported by cvs2svn $' Revision='$Revision: 1.3 $' Date='$Date: 2003-08-27 13:07:54 $'
 */
public class ArrayIterator implements Iterator {
	private int i;
	private Object a;
	/** Constructs an iterator over the members of an array.
         * All arrays are supported including primitive types.
         * @param array Must be an array.
 */
	public ArrayIterator(Object array) {
		i = 0;
		a = array;
		if (!a.getClass().isArray())
			throw new ArrayStoreException();
	}
	public boolean hasNext() {
		return i<Array.getLength(a);
	}
	public Object next() throws NoSuchElementException {
		try {
			return Array.get(a,i++);
		}
		catch (IndexOutOfBoundsException e) {
			throw new NoSuchElementException();
		}
	}
/** Not supported.
 * @throws java.lang.UnsupportedOperationException Always.
 *
 */        
        public void remove() {

            throw new UnsupportedOperationException();
        }
}