/*
 	(c) Copyright 2005 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: NodeToTriplesMapBase.java,v 1.1 2005-08-26 12:48:45 chris-dollin Exp $
*/

package com.hp.hpl.jena.mem;

import java.util.*;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.Triple.Field;
import com.hp.hpl.jena.util.CollectionFactory;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
    A base class for the "normal" nad "faster" NodeToTriplesMaps.
    
    @author kers
*/
public abstract class NodeToTriplesMapBase
    {

    /**
         The map from nodes to Set(Triple).
    */
    protected Map map = CollectionFactory.createHashedMap();

    /**
         The number of triples held in this NTM, maintained incrementally 
         (because it's a pain to compute from scratch).
    */
    protected int size = 0;

    protected final Field indexField;
    protected final Field f2;
    protected final Field f3;
    
    public NodeToTriplesMapBase( Field indexField, Field f2, Field f3 )
        { this.indexField = indexField; this.f2 = f2; this.f3 = f3; }
    
    /**
     Add <code>t</code> to this NTM; the node <code>o</code> <i>must</i>
     be the index node of the triple. Answer <code>true</code> iff the triple
     was not previously in the set, ie, it really truly has been added. 
     */
    public abstract boolean add( Triple t );

    /**
     Remove <code>t</code> from this NTM. Answer <code>true</code> iff the 
     triple was previously in the set, ie, it really truly has been removed. 
     */
    public abstract boolean remove( Triple t );

    /**
     Answer an iterator over all the triples in this NTM which have index node
     <code>o</code>.
     */
    // public abstract Iterator iterator( Node o );

    public abstract Iterator iterator( Object o );


    /**
     Answer true iff this NTM contains the concrete triple <code>t</code>.
     */
    public abstract boolean contains( Triple t );

    /**
     Answer an iterator over all the triples in this NTM which match
     <code>pattern</code>. The index field of this NTM is guaranteed
     concrete in the pattern.
     */
    // public abstract ExtendedIterator iterator( Triple pattern );

    /**
     Answer an iterator over all the triples in this NTM which are 
     accepted by <code>pattern</code>.
     */
    // public abstract ExtendedIterator iterateAll( Triple pattern );

    /**
     Answer an iterator over all the triples in this NTM.
     */
    public abstract ExtendedIterator iterator();


    /**
        The nodes which appear in the index position of the stored triples; useful
        for eg listSubjects().
    */
    public final Iterator domain()
        { return map.keySet().iterator(); }

    protected final Object getIndexField( Triple t )
        { return indexField.getField( t ).getIndexingValue(); }

    /**
        Clear this NTM; it will contain no triples.
    */
    public void clear()
        { map.clear(); size = 0; }

    public int size()
        { return size; }

    public void removedOneViaIterator()
        { size -= 1; }

    public boolean isEmpty()
        { return size == 0; }

    // public abstract Set get( Object y );

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