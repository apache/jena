/*
  (c) Copyright 2002, 2003, Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: SimpleReifier.java,v 1.23 2004-09-06 12:54:11 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.impl;

/**
	@author kers
<p>
    A base-level implementation of Reifier, intended to be straightforward
    and obvious. It fails this test nowadays ...
*/

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.compose.Union;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.util.HashUtils;
import com.hp.hpl.jena.util.iterator.*;
import java.util.*;

import com.hp.hpl.jena.vocabulary.RDF;

public class SimpleReifier implements Reifier
    {
    private GraphBase parent;
    private boolean intercepting = false;
    private boolean concealing = false;
    private ReificationStyle style;
    
    static class TMap extends FragmentMap
        {
        }
    
    static class FMap extends FragmentMap
        {
        }
    
    private FragmentMap nodeMap;
    private FragmentMap tripleMap;
    
    private Graph reificationTriples;
    
    /** 
        construct a simple reifier that is bound to the parent graph .
        
        @param parent the Graph which we're reifiying for
        @param style the reification style to use
    */
    public SimpleReifier( GraphBase parent, ReificationStyle style )
        {
        this.parent = parent;
        this.nodeMap = new FMap();
        this.tripleMap = new TMap();
        this.intercepting = style.intercepts();
        this.concealing = style.conceals();
        this.style = style;
        }
        
    public ReificationStyle getStyle()
        { return style; }
            
    /** return the parent graph we are bound to */
    public Graph getParentGraph()
        { return parent; }
        
    /** return the triple bound to _n_ */
    public Triple getTriple( Node n )        
        { 
        return (Triple) tripleMap.get( n );
//        Object partial = nodeMap.get( n );
//        return
//            partial == null ? null
//            : partial instanceof Triple ? (Triple) partial
//            : getTriple( n, (Fragments) partial )
//            ;
        }
        
//    private Triple getTriple( Node n, Fragments f )
//        { // if (f.isComplete()) System.err.println( ">> this is not supposed to happen" );
//            return f.isComplete() ? nodeMap.putTriple( n, f.asTriple() ) : null; }
        
    /** true iff there is a triple bound to _n_ */
    public boolean hasTriple( Node n )
    	{ return getTriple( n ) != null; }
        
    /** */
    public ExtendedIterator allNodes()
        {
        return WrappedIterator.create( tripleMap.tagIterator() );
        }
        
    public ExtendedIterator allNodes( Triple t )
        { 
        Set s = (Set) tripleMap.inverseMap.get( t );
        if (s == null)
            return NullIterator.instance;
        else
            return WrappedIterator.create( s.iterator() );
        }
        
    /**
        Answer a filter that only accepts nodes that are bound to the given triple.
        @param t the triple that the node must be bound to
        @return a filter that accepts only those nodes
    */        
    public Filter matching( final Triple t )
        {
        return new Filter()
            {
            public boolean accept( Object o ) { return t.equals( getTriple( (Node) o ) ); }
            };
        }
                
    private Filter completeFragment = new Filter()
        { public boolean accept( Object x ) { return isComplete( (Node) x ); } };
        
    protected boolean isComplete( Node n )
        {
        return tripleMap.get( n ) != null;
//        Object x = nodeMap.get( n );
//        return x instanceof Triple || ((Fragments) x) .isComplete();
        }
        
    /** 
        reifiy a triple _t_ with tag _tag_. If a different triple is already
        reified under _tag_, throw an AlreadyReifiedException.
    */
    public Node reifyAs( Node tag, Triple t )
    	{
        Triple existing = (Triple) tripleMap.get( tag );
        Object partial = nodeMap.get( tag );
        if (existing != null)
            { if (!t.equals( existing )) throw new AlreadyReifiedException( tag ); }
        else if (partial == null)
            tripleMap.putTriple( tag, t );
        else
            { // TODO
            FragmentMap.graphAddQuad( parent, tag, t );
            Triple t2 = getTriple( tag );
            if (t2 == null) throw new CannotReifyException( tag );
            }
        if (concealing == false) FragmentMap.graphAddQuad( parent, tag, t );
        return tag; 
    	}
        
    /**
        If n is bound to the triple t, remove that triple. If we're not concealing reification 
        quadlets, we need to remove them from the parent graph too.
    */    	
    public void remove( Node n, Triple t )
        {
        Triple x = (Triple) tripleMap.get( n );
        if (t.equals( x )) 
            { tripleMap.removeTriple( n, t ); 
            if (!concealing) parentRemoveQuad( n, t ); }
        }
        
    public boolean hasTriple( Triple t )
        { return tripleMap.hasTriple( t ); }
          
    public boolean handledAdd( Triple t )
        {
        if (intercepting)
            {
            int s = Fragments.getFragmentSelector( t );  
            if (s < 0)
                return false;
            else     
                {
                Fragments fs = getFragment( t );
                fs.add( s, t.getObject() );
                if (fs.isComplete()) 
                    {
                    tripleMap.putTriple( t.getSubject(), fs.asTriple() );
                    nodeMap.removeFragments( t.getSubject() );
                    }
                return concealing;
                }
            }
        else
            return false;
        }
        
    public boolean handledRemove( Triple t )
        {
        if (intercepting)
            {
            int s = Fragments.getFragmentSelector( t );  
            if (s < 0)
                return false;
            else     
                {
                Fragments fs = getFragment( t );
                fs.remove( s, t.getObject() );
                if (fs.isComplete()) 
                    {
                    tripleMap.putTriple( t.getSubject(), fs.asTriple() );
                    nodeMap.removeFragments( t.getSubject() );
                    }
                else 
                    {
                    tripleMap.removeTriple( t.getSubject() );
                    if (fs.isEmpty()) nodeMap.removeFragments( t.getSubject() );
                    }
                return concealing;
                }
            }
        else
            return false;
        }
                  
    private Fragments getFragment( Triple t )
        {
        Node s = t.getSubject();
        Triple already = (Triple) tripleMap.get( s );
        Object partial = nodeMap.get( s );
        return
            already != null ? explode( s, already )
            : partial == null ? nodeMap.putFragments( s, new Fragments( s ) )
            : (Fragments) partial;
        }
        
    private Fragments explode( Node s, Triple t )
        { return nodeMap.putFragments( s, new Fragments( s, t ) ); }

    public void remove( Triple t )
        {     
        // horrid code. we don't likes it, my precious.
        Set nodes = HashUtils.createSet();
        Iterator it = allNodes();
        while (it.hasNext())
            {
            Node n = (Node) it.next();
            if (t.equals( getTriple( n ))) nodes.add( n );
            }
        Iterator them = nodes.iterator();
        while (them.hasNext()) remove( (Node) them.next(), t );
        }
            
    public Graph getHiddenTriples()
        { return style == ReificationStyle.Standard ? Graph.emptyGraph : getReificationTriples(); }
    
    public Graph getReificationTriples() // TODO use DisjointUnion
        { if (reificationTriples == null) reificationTriples = new Union( tripleMap.asGraph(), nodeMap.asGraph() ); 
        return reificationTriples; }
        
    /**
        remove from the parent all of the triples that correspond to a reification
        of t on tag.
    */
    private void parentRemoveQuad( Node n, Triple t )
        {
        parent.delete( Triple.create( n, RDF.Nodes.type, RDF.Nodes.Statement ) );
        parent.delete( Triple.create( n, RDF.Nodes.subject, t.getSubject() ) );
        parent.delete( Triple.create( n, RDF.Nodes.predicate, t.getPredicate() ) );
        parent.delete( Triple.create( n, RDF.Nodes.object, t.getObject() ) ); 
        }        
              
    /**
        our string representation is <R ...> wrapped round the string representation
        of our node map.
    */
    public String toString()
        { return "<R " + nodeMap + "|" + tripleMap + ">"; }
    }
    
/*
    (c) Copyright 2002, 2003 Hewlett-Packard Development Company, LP
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
