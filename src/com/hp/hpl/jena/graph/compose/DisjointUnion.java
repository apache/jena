/*
  (c) Copyright 2004, Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id: DisjointUnion.java,v 1.3 2004-11-01 16:38:26 chris-dollin Exp $
*/
package com.hp.hpl.jena.graph.compose;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.TripleMatch;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
     DisjointUnion - a version of Union that assumes the graphs are disjoint, and
     hence that <code>find</code> need not do duplicate-removal. Adding things
     to the graph adds them to the left component, and does <i>not</i> add
     triples that are already in the right component.
     
     @author kers
*/
public class DisjointUnion extends Dyadic
    {
    public DisjointUnion( Graph L, Graph R )
        { super( L, R ); }

    public ExtendedIterator graphBaseFind( TripleMatch m )
        { return L.find( m ) .andThen( R.find( m ) ); }
    
    public boolean contains( Triple t )
        { return L.contains( t ) || R.contains( t ); }
    
    public void performDelete( Triple t )
        { L.delete( t ); R.delete( t ); }
    
    public void performAdd( Triple t )
        { if (!R.contains( t )) L.add( t ); }
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