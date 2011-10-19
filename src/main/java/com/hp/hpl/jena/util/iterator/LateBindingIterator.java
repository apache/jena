/*
 *  (c) Copyright 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
 * $Id: LateBindingIterator.java,v 1.1 2009-06-29 08:55:49 castagna Exp $
 *
 *
 * LateBindingIterator.java
 *
 * Created on June 16, 2001, 8:19 PM
 */

package com.hp.hpl.jena.util.iterator;
import java.util.Iterator ;
/** An Iterator that is created lazily.
 * The sequence to be defined is defined by
 * the subclass's definition of create().
 * This is only called on the first call to
 * <CODE>hasNext()</CODE> or <CODE>next()</CODE>.
 * This allows an Iterator to be passed to some other
 * code, while delaying the evaluation of what actually
 * is going to be iterated over.
 * @author jjc
 * @version Release='$Name: not supported by cvs2svn $' Revision='$Revision: 1.1 $' Date='$Date: 2009-06-29 08:55:49 $'
 */
abstract public class LateBindingIterator<T> implements Iterator<T> {

    private Iterator<? extends T> it;
    
    /** An Iterator that is created lazily. 
     * The sequence to be defined is defined by 
     * a subclass's instantiation of create().
     * This is only called on the first call to
     * <CODE>hasNext()</CODE> or <CODE>next()</CODE>.
 */
    public LateBindingIterator() {
    }

    @Override
    public boolean hasNext() {
        lazy();
        return it.hasNext();
    }
    
    @Override
    public T next() {
        lazy();
        return it.next();
    }
    
    @Override
    public void remove() {
        lazy();
        it.remove();
    }
    
    private void lazy() {
        if ( it == null )
            it = create();
    }
/** The subclass must define this to return
 * the Iterator to invoke. This method will be
 * called at most once, on the first call to
 * <CODE>next()</CODE> or <CODE>hasNext()</CODE>.
 * From then on, all calls to this will be passed
 * through to the returned Iterator.
 * @return The parent iterator defining the sequence.
 */    
    public abstract Iterator<? extends T> create();
    
}
