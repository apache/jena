/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: NiceIterator.java,v 1.1.1.1 2002-12-19 19:21:19 bwm Exp $
*/

package com.hp.hpl.jena.util.iterator;

/**
	@author kers
<br>
    A NiceIterator is an abstract class implementing ClosableIterator, which is
    about to become more complicated ...
*/

public class NiceIterator implements ClosableIterator
    {
    public NiceIterator()
        { super(); }

    /**
        default close: don't need to do anything.
    */
    public void close()
        { }

    /**
        default hasNext: we have no bananas.
    */
    public boolean hasNext()
        {  return false; }

    /**
        default next: we *said* we had no bananas.
    */
    public Object next()
        {
        throw new UnsupportedOperationException( "no objects in this iterator" );
        }

    /**
        we have no bananas, so we can't remove any.
    */
    public void remove()
        { 
        throw new UnsupportedOperationException( "remove not supported for this iterator" ); 
        }
        
    /**
        concatenate two closable iterators.
    */
    
    public static ClosableIterator andThen( final ClosableIterator a, final ClosableIterator b )
        {
        return new NiceIterator()
            {
            private boolean walkingA = true;
            
            public boolean hasNext()
                { return (walkingA = a.hasNext()) || b.hasNext(); }
                
            public Object next()
                { return (walkingA = a.hasNext()) ? a.next() : b.next(); }
                
            public void close()
                {
                a.close();
                b.close();
                }
                
            public void remove()
                { (walkingA ? a : b).remove(); }
            };
        }
        
    /**
        make a new iterator, which is us then the other chap.
    */
    
    public ClosableIterator andThen( ClosableIterator other )
        { return andThen( this, other ); }
        
    /**
        make a new iterator, which is our elements that pass the filter
    */
    public ClosableIterator filterKeep( Filter f )
        { return new FilterIterator( f, this ); }
        
    public ClosableIterator filterDrop( final Filter f )
        { 
        Filter notF = new Filter() { public boolean accept( Object x ) { return !f.accept( x ); } };
        return new FilterIterator( notF, this ); 
        }
        
    public ClosableIterator mapWith( Map1 map1 )
        { return new Map1Iterator( map1, this ); }
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
