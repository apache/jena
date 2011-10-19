/*
 *  (c) Copyright 2000, 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
 * $Id: IteratorIterator.java,v 1.1 2009-06-29 08:55:49 castagna Exp $
 *
 */

package com.hp.hpl.jena.util.iterator;

import java.util.Iterator;
import java.util.NoSuchElementException;

/** Given an Iterator that returns Iterator's, this creates an
 * Iterator over the next level values.
 * Similar to list splicing in lisp.
 * @author jjc
 * @version Release='$Name: not supported by cvs2svn $' Revision='$Revision: 1.1 $' Date='$Date: 2009-06-29 08:55:49 $'
 */
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
