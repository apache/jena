/*
  (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: SimpleTripleSorter.java,v 1.9 2005-02-21 11:52:25 andy_seaborne Exp $
*/

package com.hp.hpl.jena.graph.query;

import com.hp.hpl.jena.graph.*;

import java.util.*;

/**
    A TripleSorter for "optimising" queries. The triples of the query are permuted by
    moving the "lightest" triples to earlier positions. Within each region of the same
    lightness, triples the bind the most variables to their right are preferred. Otherwise
    the order is preserved.
<p>
    The notion of "lightness" makes more concrete triples lighter than less concrete ones,
    and variables lighter than ANY. Variables that have been bound by the time their
    containing triple is processed weigh just a little.
<p>
    The notion of "bind the most" is just the sum of occurances of the variables in the
    triple in the other triples.
<p>
    No weighting is applied to predicate position, and no knowledge about the graph 
    being queried is required.
    
 	@author kers
*/
public class SimpleTripleSorter implements TripleSorter
    {
    private Triple [] result;
    private int putIndex;
    private Set bound;
    private List remaining;
        
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
        The algorithm just repeatedly looks for a lightest triple, moves it into the result
        array, and re-weighs triples in the light of the new bindings that makes. Of several
        lightest triples, the first is picked [mostly so that it's easier to write the tests].
    */
    protected Triple [] sort() 
        {
        while (remaining.size() > 0) accept( findMostBinding( findLightest( remaining ) ) );    
        return result;
        }
        
    /**
        Accept a triple as the next element in the result array, note that all its variables are
        now bound, and remove it from the list of remaining triples.
    */
    protected void accept( Triple t )
        {
        result[putIndex++] = t;
        bind( t );
        remaining.remove( t );  
        }
                
    /**
        Answer a list of the lightest triples in the candidate list; takes one pass over the
        candidates.
        
        @param candidates the list of triples to select from
        @return the light of lightest triples [by <code>weight</code>], preserving order
    */
    protected List findLightest( List candidates )
        {
        List lightest = new ArrayList();
        int minWeight = 100;
        for (int i = 0; i < candidates.size(); i +=1)
            {
            Triple t = (Triple) candidates.get(i);
            int w = weight( t );
            if (w < minWeight) 
                { 
                lightest.clear(); 
                lightest.add( t ); 
                minWeight = w; 
                }
            else if (w == minWeight)
                lightest.add( t );      
            }
        return lightest;
        }
        
    /**
        Answer the first most-binding triple in the list of candidates.
    */
    protected Triple findMostBinding( List candidates )
        {
        int maxBinding = -1;
        Triple mostBinding = null;
        for (int i = 0; i < candidates.size(); i += 1)
            {
            Triple t = (Triple) candidates.get(i);
            int count = bindingCount( t );
            if (count > maxBinding) { mostBinding = t; maxBinding = count; }    
            }  
        return mostBinding;
        }
        
    /**
        The binding count of a triple is the number of instances of variables in other triples 
        it would capture if it were to be bound.
        
        @param t the triple to compute the binding count for
        @return the total binding count of t with respect to all the triples in remaining
    */
    protected int bindingCount( Triple t )
        {
        int count = 0;
        for (int i = 0; i < remaining.size(); i += 1)
            {
            Triple other = (Triple) remaining.get(i);    
            if (other != t) count += bindingCount( t, other );
            }
        return count;    
        }
        
    /**
        Answer the binding count of t with respect to some other triple
    */
    protected int bindingCount( Triple t, Triple other )
        {
        return 
            bindingCount( t.getSubject(), other ) 
            + bindingCount( t.getPredicate(), other ) 
            + bindingCount( t.getObject(), other ) ; 
        }
        
    protected int bindingCount( Node n, Triple o )
        {
        return n.isVariable()   
            ? bc( n, o.getSubject() ) + bc( n, o.getPredicate() ) + bc( n, o.getObject() )
            : 0;
        }
        
    /**
        Answer 1 if nodes are .equals, 0 otherwise.
    */
    protected int bc( Node n, Node other )
        { return n.equals( other ) ? 1 : 0; }

    /**
        Bind a triple by binding each of its nodes.
    */
    protected void bind( Triple t )
        {
        bind( t.getSubject() );
        bind( t.getPredicate() );
        bind( t.getObject() );    
        }
        
    protected void bind( Node n )
        { if (n.isVariable()) bound.add( n ); }
        
    /**
        In this simple sorter, the weight of a triple is the sum of the weights of its nodes.
        None of the positions get weighted differently. One might choose to weigh 
        positions that were more search-intensive more heavily.
        
        @param t the triple to be weighed [with respect to the bound variables]
        @return the weight of the triple, rising as the triple is more variable
    */
    protected int weight( Triple t )
        {
        return weight( t.getSubject() ) + weight( t.getPredicate() ) + weight( t.getObject() );    
        }
        
    /**
        In this simple sorter, concrete nodes weigh nothing. [This is, after all, computing
        rather than building.] ANYs cost the most, because they cannot be bound, and
        variable nodes cost a little if they are bound and a lot if they are not. 
    <p>
        The rules are
    <ul>
        <li>any concrete node weighs nothing
        <li>a bound variable node weighs something, but a triple which is three bound
            variables must weigh less than a triple with an unbound variable
        <li>an ANY node weighs more than an unbound variable node but less than
            two unbound variable nodes
    </ul>
    
        @param n the node to be weighed [with respect to the bound variables]
        @return the weight of the node
    */
    protected int weight( Node n )
        { return n.isConcrete() ? 0 : n.equals( Node.ANY ) ? 5 : bound.contains( n ) ? 1 : 4; }
    }


/*
    (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
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