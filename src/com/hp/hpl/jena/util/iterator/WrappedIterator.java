/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: WrappedIterator.java,v 1.1 2003-01-30 10:25:17 chris-dollin Exp $
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
        factory method for creating a wrapper arounf _it_. We reserve
        the right to deliver the argument if it's already an extended iterator.
    */
    public static WrappedIterator create( Iterator it )
        { return new WrappedIterator( it ); }
      
    /** the base iterator that we wrap */  
    private Iterator base;
    
    /** private constructor: remember the base iterator */
    private WrappedIterator( Iterator base )
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
        { if (base instanceof ClosableIterator) ((ClosableIterator) base).close(); }
    }

/*
    (c) Copyright Hewlett-Packard Company 2002
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
