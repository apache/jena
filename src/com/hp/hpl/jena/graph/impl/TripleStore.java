/*
 (c) Copyright 2004, Hewlett-Packard Development Company, LP, all rights reserved.
 [See end of file]
 $Id: TripleStore.java,v 1.3 2004-09-14 17:10:29 chris-dollin Exp $
 */

package com.hp.hpl.jena.graph.impl;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
     TripleStore - interface for bulk storage of triples used in composed graphs.
     @author kers
*/
public interface TripleStore
    {
    /**
         Destroy this triple store - discard the indexes.
    */
    public abstract void close();

    /**
         Add a triple to this triple store.
    */
    public abstract void add( Triple t );

    /**
         Remove a triple from this triple store.
    */
    public abstract void delete( Triple t );

    /**
         Answer the size (number of triples) of this triple store.
    */
    public abstract int size();

    /**
         Answer true iff this triple store is empty.
    */
    public abstract boolean isEmpty();

    /**
         Answer true iff this triple store contains the (concrete) triple <code>t</code>.
    */
    public abstract boolean contains( Triple t );

    /**
         Answer an setwise iterator over all the subjects of triples in this store.
    */
    public ExtendedIterator listSubjects();
    
    /**
         Answer an setwise iterator over all the objects of triples in this store.
    */    
    public ExtendedIterator listObjects();
    
    /** 
         Answer an ExtendedIterator returning all the triples from this store that
         match the pattern <code>m = (S, P, O)</code>.
    */
    public abstract ExtendedIterator find( TripleMatch tm );

    /**
        Clear this store, ie remove all triples from it.
    */
    public abstract void clear();
    }

/*
     (c) Copyright 2004, Hewlett-Packard Development Company, LP
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