/*
  (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: SimpleTripleSorter.java,v 1.4 2003-08-12 14:32:56 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.query;

import com.hp.hpl.jena.graph.*;

import java.util.*;

/**
 	@author kers
*/
public class SimpleTripleSorter implements TripleSorter
    {
    private Triple [] result;
    private int putIndex;
    private Set bound;
    private List remaining;
    private int currentWeight;
        
    /**
        A public SimpleTripleSorter needs no arguments (we imagine more sophisticated
        ones might).
    */
    public SimpleTripleSorter()
        {}
        
    /**
        Sort the triple array so that more-bound triples come before less-bound triples.
        Preserve the order of the elements unless they <i>have<i> to move. Return 
        a new permuted copy of the original array. The work is done by a new instance
        of SimpleTripleSorter specialised to this triple array (and with helpful state). 
    */
    public Triple [] sort( Triple[] ts )
        { return new SimpleTripleSorter( ts ) .sort(); }        
        
    /**
        Initialise a working SimpleTripleSorter from the triple array to sort. The working 
        copy has an empty set of bound variables and a mutable (and mutated) list of the
        original triple array, in the same order. 
    */
    protected SimpleTripleSorter( Triple [] triples )
        {
        this(); 
        this.bound = new HashSet();
        this.result = new Triple[triples.length]; 
        this.remaining = new ArrayList( Arrays.asList( triples ) );       
        }

    /**
        Sort the triple array so that more-bound triples come before less-bound triples.
        Preserve the order of the elements unless they <i>have<i> to move. 
    <p>      
        The algorithm just repeatedly looks for the lightest triples, moves them into the result
        array, and re-weighs triples in the light of the new bindings that makes.
    */
    protected Triple [] sort() 
        {
        while (remaining.size() > 0)
            {
                
                
                
            for (int i = 0; i < remaining.size(); i += 1) consider( (Triple) remaining.get(i) );
            currentWeight += 1;
            }
        return result;
        }

    protected void consider( Triple t )
        { if (weight( t ) <= currentWeight) accept( t ); }
        
    protected void accept( Triple t )
        {
        result[putIndex++] = t;
        bind( t );
        remaining.remove( t );  
        }
        
    protected void bind( Triple t )
        {
        bound.add( t.getSubject() );
        bound.add( t.getPredicate() );
        bound.add( t.getObject() );    
        }
        
    /**
        In this simple sorter, the weight of a triple is the sum of the weights of its nodes.
        None of the positions get weighted differently. One might choose to weigh 
        positions that were more search-intensive more heavily.
    */
    protected int weight( Triple t )
        {
        return weight( t.getSubject() ) + weight( t.getPredicate() ) + weight( t.getObject() );    
        }
        
    /**
        In this simple sorter, concrete nodes weigh nothing. [This is, after all, computing
        rather than building.] ANYs cost the most, because they cannot be bound, and
        variable nodes cost a little if they are bound and a lot if they are not. 
    */
    protected int weight( Node n )
        { return n.isConcrete() ? 0 : n.equals( Node.ANY ) ? 5 : bound.contains( n ) ? 1 : 4; }
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