/*
  (c) Copyright 2003, Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: RandomOrderIterator.java,v 1.1 2003-12-04 17:26:44 jeremy_carroll Exp $
*/

package com.hp.hpl.jena.util.iterator;

import java.util.*;
/**
 * 
 * RandomOrderIterator - Reorders the elements returned by an Iterator.
 * 
 * @author jjc
 *
 */
public class RandomOrderIterator extends WrappedIterator {
	private Random rnd = new Random();
    private Object buffer[];
    // one more than the index of the last non-null element.
    int top;
	/**
	 * Wrap the base iterator, randomizing with a buffer of length sz.
	 */
	public RandomOrderIterator(int sz, Iterator base) {
		super(base);
		buffer = new Object[sz];
		top = 0;
		fill();
	}
	
	public boolean hasNext() {
		return top > 0;
	}
	public Object next() {
		int ix = rnd.nextInt(top);
		Object rslt = buffer[ix];
		top--;
		buffer[ix] = buffer[top];
		fill();
		return rslt;
	}
	
	public void remove() {
		throw new UnsupportedOperationException("randomizing does not allow modification");
	}
	
	private void fill() {
	   while ( top < buffer.length && super.hasNext() ) {
	   	 buffer[top++] = super.next();
	   }
	}

}
/*
 (c) Copyright 2003 Hewlett-Packard Development Company, LP
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:

 1. Redistributions of source code must retain the above copyright
 notice, this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright
 notice, this list of conditions and the following disclaimer in the
 documentation and/or other materials provided with the distribution.

 3. The name of the author may not be used to endorse or promote products
 derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */