/*
  (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id: TrackingTripleIterator.java,v 1.4 2005-02-21 12:03:47 andy_seaborne Exp $
*/

package com.hp.hpl.jena.mem;

import java.util.Iterator;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.util.iterator.WrappedIterator;

/**
    A WrappedIterator which remembers the last object next'ed in a
    protected instance variable, so that subclasses have access to it 
    during .remove.

	@author kers
*/
public class TrackingTripleIterator extends WrappedIterator
    {
    public TrackingTripleIterator( Iterator it ) 
        { super( it ); }    
    
    /**
        The remembered current triple. Subclass should *not* assign to this variable. 
    */
    protected Triple current;
        
    /**
        Answer the next object, remembering it in <code>current</code>. 
     	@see java.util.Iterator#next()
    */
    public Object next()
        { return current = (Triple) super.next(); }       
    }

/*
    (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
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