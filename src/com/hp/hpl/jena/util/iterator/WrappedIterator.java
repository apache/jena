/*
  (c) Copyright 2003, Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: WrappedIterator.java,v 1.4 2003-08-27 13:07:54 andy_seaborne Exp $
*/

package com.hp.hpl.jena.util.iterator;

import java.util.*;

/**
    a WrappedIterator is an ExtendedIterator wrapping around a plain (or
    presented as plain) Iterator. The wrapping allows the usual extended
    operations (filtering, concatenating) to be done on an Iterator derived
    from some other source.
<br>
    @author kers
*/

public class WrappedIterator extends NiceIterator
    {
    /**
        factory method for creating a wrapper around _it_. We reserve
        the right to deliver the argument if it's already an extended iterator.
    */
    public static WrappedIterator create( Iterator it )
        { return new WrappedIterator( it ); }
      
    /** the base iterator that we wrap */  
    private Iterator base;
    
    /** private constructor: remember the base iterator */
    protected WrappedIterator( Iterator base )
        { this.base = base; }
        
    /** hasNext: defer to the base iterator */
    public boolean hasNext()
        { return base.hasNext(); }
        
    /** next: defer to the base iterator */
    public Object next()
        { return base.next(); }
        
    /** remove: defer to the base iterator */
    public void remove()
        { base.remove(); }
        
    /** close: defer to the base, iff it is closable */
    public void close()
        { close( base ); }

    /**
        if _it_ is a Closableiterator, close it. Abstracts away from
        tests [that were] scattered through the code.
    */
    public static void close( Iterator it )
        { if (it instanceof ClosableIterator) ((ClosableIterator) it).close(); }
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
