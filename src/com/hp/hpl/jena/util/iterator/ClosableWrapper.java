/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: ClosableWrapper.java,v 1.1 2003-03-26 11:44:26 chris-dollin Exp $
*/

package com.hp.hpl.jena.util.iterator;

import java.util.*;
import com.hp.hpl.jena.rdf.model.*;

/**
    an abstract base class for iterators that wrap other iterators, typically
    to provide type-constrained nextFoo methods; it provides an implementation
    of _close_ that accounts for non-closable iterators.
*/

public abstract class ClosableWrapper extends NiceIterator implements Iterator
    {
    /** the underlying iterator we wrap */
    protected Iterator iterator;

    /** construction: remember the base iterator */
    public ClosableWrapper( Iterator iterator )
        { this.iterator = iterator; }

    public boolean hasNext()
        { return mustBeUnclosed().hasNext(); }

    public void remove()
        { mustBeUnclosed().remove(); }

    public Object next()
        { return mustBeUnclosed().next();  }

    protected Iterator mustBeUnclosed()
        {
        if (iterator == null) throw new RDFException( RDFException.ITERATORCLOSED );
        return iterator;
        }

    /** close the base iterator (if possible), and forget it */
    public void close()
        {
        if (iterator != null) WrappedIterator.close( iterator );
        iterator = null;
        }
    }

/*
    (c) Copyright Hewlett-Packard Company 2003
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
