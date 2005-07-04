/*
 * (c) Copyright 2000, 2001, 2002, 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: Filter.java,v 1.7 2005-07-04 13:18:04 chris-dollin Exp $
 */

package com.hp.hpl.jena.util.iterator;

import java.util.Iterator;

/** 
    boolean functions wrapped to be used in filtering iterators.
    
    @author jjc, kers
*/
public abstract class Filter
    {
    /**
        Answer true iff the object <code>o</code> is acceptable. This method
        may also throw an exception if the argument is of a wrong type; it
        is not required to return <code>false</code> in such a case.
    */
	public abstract boolean accept( Object o );
    
    public ExtendedIterator filterKeep( Iterator it )
        { return new FilterKeepIterator( this, it ); }
    
    public Filter and( final Filter other )
        { return other == any ? this : new Filter()
            { public boolean accept( Object x ) 
                { return Filter.this.accept( x ) && other.accept( x ); } 
            };
        }
    
    /** 
        A Filter that accepts everything it's offered.
    */
    public static final Filter any = new Filter()
        { 
        public final boolean accept( Object o ) { return true; } 
        
        public Filter and( Filter other ) { return other; }
        
        public ExtendedIterator filterKeep( Iterator it )
            { return WrappedIterator.create( it ); }
        };
        
    }

/*
 *  (c) Copyright 2000, 2001, 2002, 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
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
 * $Id: Filter.java,v 1.7 2005-07-04 13:18:04 chris-dollin Exp $
 *
 */
