/*
    (c) Copyright 2003, 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
    [See end of file]
    $Id: FilterIterator.java,v 1.10 2007-01-02 11:49:41 andy_seaborne Exp $
*/

package com.hp.hpl.jena.util.iterator;

import java.util.Iterator;
import java.util.NoSuchElementException;

/** 
     Creates a sub-Iterator by filtering. This class should not be used
     directly any more; the subclasses FilterKeepIterator and FilterDropIterator
     should be used instead. 
     @author jjc, mods [clarity & speedup] by kers
 */
public class FilterIterator extends WrappedIterator
    {
	protected final Filter f;
	protected Object current;
    protected boolean canRemove;
    protected boolean hasCurrent;

    /** 
        Initialises a FilterIterator with its filter and base.
        @param fl An object is included if it is accepted by this Filter.
        @param e The base Iterator.
    */        
	public FilterIterator( Filter fl, Iterator e ) 
        {
		super( e );
		f = fl;
        }

    /** 
        Answer true iff there is at least one more acceptable object.
        [Stores reference into <code>current</code>, sets <code>canRemove</code>
        false; answer preserved in `hasCurrent`]
    */        
	synchronized public boolean hasNext() 
        {
	    while (!hasCurrent && super.hasNext())
            hasCurrent = accept( current = super.next() );
        canRemove = false;
        return hasCurrent;
        }

    /**
        Overridden in Drop/Keep as appropriate. Answer true if the object is
        to be kept in the output, false if it is to be dropped.
    */
    protected boolean accept( Object x )
        { return f.accept( x ); }
    
    /** 
         Remove the current member from the underlying iterator. Legal only
         after a .next() but before any subsequent .hasNext(), because that
         may advance the underlying iterator.
    */        
    synchronized public void remove() 
        {
        if (!canRemove ) throw new IllegalStateException
            ( "FilterIterators do not permit calls to hasNext between calls to next and remove." );
        super.remove();
        }
        
    /** 
        Answer the next acceptable object from the base iterator. The redundant
        test of `hasCurrent` appears to make a detectable speed difference.
        Crazy.
    */        
	synchronized public Object next() 
        {
		if (hasCurrent || hasNext()) 
            {
            canRemove = true;
            hasCurrent = false;
            return current;
            }
		throw new NoSuchElementException();
        }
    }

/*
 *  (c) Copyright 2000, 2001, 2002, 2003, 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
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
 * $Id: FilterIterator.java,v 1.10 2007-01-02 11:49:41 andy_seaborne Exp $
 *
*/
