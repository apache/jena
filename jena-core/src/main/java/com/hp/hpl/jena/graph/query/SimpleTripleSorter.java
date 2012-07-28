/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
    
 */
public class SimpleTripleSorter implements TripleSorter
    {
    private Triple [] result;
    private int putIndex;
    private Set<Node> bound;
    private List<Triple> remaining;
        
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
    @Override
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
        this.bound = new HashSet<Node>();
        this.result = new Triple[triples.length]; 
        this.remaining = new ArrayList<Triple>( Arrays.asList( triples ) );       
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
    protected List<Triple> findLightest( List<Triple> candidates )
        {
        List<Triple> lightest = new ArrayList<Triple>();
        int minWeight = 100;
        for (int i = 0; i < candidates.size(); i +=1)
            {
            Triple t = candidates.get(i);
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
    protected Triple findMostBinding( List<Triple> candidates )
        {
        int maxBinding = -1;
        Triple mostBinding = null;
        for (int i = 0; i < candidates.size(); i += 1)
            {
            Triple t = candidates.get(i);
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
            Triple other = remaining.get(i);    
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
