/*
  (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: SimpleTripleSorter.java,v 1.3 2003-08-12 12:52:48 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.query;

import com.hp.hpl.jena.graph.*;

import java.util.*;

/**
 	@author kers
*/
public class SimpleTripleSorter implements TripleSorter
    {

    private Triple [] triples;
    private Triple [] copy;
    private int here;
    private Set bound;
    private Set remaining;
        
    public SimpleTripleSorter()
        {}
        
    protected SimpleTripleSorter( Triple [] triples )
        {
        this(); 
        this.triples = triples;
        this.bound = new HashSet();
        this.copy = new Triple[triples.length]; 
        this.remaining = new HashSet( Arrays.asList( triples ) );       
        }

    /**
        Sort the triple array so that more-bound triples come before less-bound triples.
        Preserve the order of the elements unless they <i>have<i> to move. 
    */
    public Triple [] sort( Triple[] ts )
        { return new SimpleTripleSorter( ts ) .sort(); }        
        
    protected Triple [] sort() 
        {
        int limit = 1;
        bound.clear();
        here = 0;
        while (remaining.size() > 0)
            {
            for (int i = 0; i < triples.length; i += 1) consider( triples[i], limit );
            limit += 1;
            }
        return copy;
        }

    protected void consider( Triple t, int limit )
        {     
        if (remaining.contains( t ) && weight( t ) < limit) accept( t );
        }
        
    protected void accept( Triple t )
        {
        copy[here++] = t;
        bind( t );
        remaining.remove( t );  
        }
        
    protected void bind( Triple t )
        {
        bound.add( t.getSubject() );
        bound.add( t.getPredicate() );
        bound.add( t.getObject() );    
        }
        
    protected int weight( Triple t )
        {
        return weight( t.getSubject() ) + weight( t.getPredicate() ) + weight( t.getObject() );    
        }
        
    protected int weight( Node n )
        { return n.isConcrete() ? 0 : bound.contains( n ) ? 1 : 4; }
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