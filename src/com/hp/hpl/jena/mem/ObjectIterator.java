/*
 	(c) Copyright 2005 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: ObjectIterator.java,v 1.1 2005-08-12 13:23:08 chris-dollin Exp $
*/

package com.hp.hpl.jena.mem;

import java.util.*;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.util.CollectionFactory;
import com.hp.hpl.jena.util.iterator.NiceIterator;

/**
    Helper class for listObjects. Because literal indexing means that the
    domain of the object map is not a node, but an indexing value (shared by
    a bunch of different literal nodes), getting the list of objects requires
    mapping that indexing value to all the triples that use it, and then
    filtering those triples for their objects, removing duplicates.
    
    @author kers
*/
public abstract class ObjectIterator extends NiceIterator
    {
    public ObjectIterator( Iterator domain )
        { this.domain = domain; }

    protected abstract Iterator iteratorFor( Object y );

    final Iterator domain;
    
    final Set seen = CollectionFactory.createHashedSet();
    
    final List pending = new ArrayList();
    
    public boolean hasNext()
        {
        while (pending.isEmpty() && domain.hasNext()) refillPending();
        return !pending.isEmpty();                
        }
    
    public Object next()
        {
        if (!hasNext()) throw new NoSuchElementException
            ( "FasterTripleStore listObjects next()" );
        return pending.remove( pending.size() - 1 );
        }
    
    protected void refillPending()
        {
        Object y = domain.next();
        if (y instanceof Node)
            pending.add( y );
        else
            {
            Iterator z = iteratorFor( y );
            while (z.hasNext())
                {
                Node object = ((Triple) z.next()).getObject();
                if (seen.add( object )) pending.add( object );
                }
            }
        }
    
    public void remove()
        { throw new UnsupportedOperationException( "listObjects remove()" ); }
    }

/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP
 * All rights reserved.
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
 *
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
*/