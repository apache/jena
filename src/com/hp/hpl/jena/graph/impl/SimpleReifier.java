/*
  (c) Copyright 2002, 2003, Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: SimpleReifier.java,v 1.14 2003-08-27 13:00:58 andy_seaborne Exp $
*/

package com.hp.hpl.jena.graph.impl;

/**
	@author kers
<p>
    A base-level implementation of Reifier, intended to be straightforward
    and obvious. It fails this test nowadays ...
*/

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.util.iterator.*;
import java.util.*;

import com.hp.hpl.jena.vocabulary.RDF;

public class SimpleReifier implements Reifier
    {
    private Graph parent;
    private boolean intercepting = false;
    private boolean concealing = false;
    private Style style = null;
    private FragmentMap nodeMap;
    
    /** 
        construct a simple reifier that is bound to the parent graph .
        
        @param parent the Graph which we're reifiying for
        @param style the reification style to use
    */
    public SimpleReifier( Graph parent, Style style )
        {
        this.parent = parent;
        this.nodeMap = new FragmentMap();
        this.intercepting = style.intercepts();
        this.concealing = style.conceals();
        this.style = style;
        }
        
    public Style getStyle()
        { return style; }
            
    /** return the parent graph we are bound to */
    public Graph getParentGraph()
        { return parent; }
        
    /** return the triple bound to _n_ */
    public Triple getTriple( Node n )        
        {
        Object partial = nodeMap.get( n );
        return
            partial == null ? null
            : partial instanceof Triple ? (Triple) partial
            : getTriple( n, (Fragments) partial )
            ;
        }
        
    private Triple getTriple( Node n, Fragments f )
        { // if (f.isComplete()) System.err.println( ">> this is not supposed to happen" );
            return f.isComplete() ? nodeMap.putTriple( n, f.asTriple() ) : null; }
        
    /** true iff there is a triple bound to _n_ */
    public boolean hasTriple( Node n )
    	{ return getTriple( n ) != null; }
        
    /** */
    public ExtendedIterator allNodes()
        {
        return WrappedIterator.create( nodeMap.keySet().iterator() ) .filterKeep ( completeFragment );
        }
        
    public ExtendedIterator allNodes( Triple t )
        { return allNodes() .filterKeep( matching( t ) );  }
        
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
        Object x = nodeMap.get( n );
        return x instanceof Triple || ((Fragments) x) .isComplete();
        }
        
    /** 
        reifiy a triple _t_ with tag _tag_. If a different triple is already
        reified under _tag_, throw an AlreadyReifiedException.
    */
    public Node reifyAs( Node tag, Triple t )
    	{
        Object partial = nodeMap.get( tag );
        if (partial instanceof Triple)
            { if (!t.equals( partial )) throw new AlreadyReifiedException( tag ); }
        else if (partial == null)
            nodeMap.putTriple( tag, t );
        else
            {
            FragmentMap.graphAddQuad( parent, tag, t );
            Triple t2 = getTriple( tag );
            if (t2 == null) throw new CannotReifyException( tag );
            }
        if (concealing == false) FragmentMap.graphAddQuad( parent, tag, t );
        return tag; 
    	}
        
    /** unbind _n_ */    	
    public void remove( Node n, Triple t )
        {
        Object x = nodeMap.get( n );
        if (x instanceof Triple)
            { if (x.equals( t )) nodeMap.remove( n ); }
        else
            parentRemoveQuad( n, t );
        }
        
    public boolean hasTriple( Triple t )
        { return allNodes( t ).hasNext(); }
          
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
                if (fs.isComplete()) nodeMap.putTriple( t.getSubject(), fs.asTriple() );
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
                if (fs.isComplete()) nodeMap.putTriple( t.getSubject(), fs.asTriple() );
                return concealing;
                }
            }
        else
            return false;
        }
                  
    private Fragments getFragment( Triple t )
        {
        Node s = t.getSubject();
        Object x = nodeMap.get( s );
        return
            x instanceof Triple ? explode( s, (Triple) x )
            : x == null ? nodeMap.putFragments( s, new Fragments( s ) )
            : (Fragments) x;
        }
        
    private Fragments explode( Node s, Triple t )
        { return nodeMap.putFragments( s, new Fragments( s, t ) ); }

    public void remove( Triple t )
        {     
        // horrid code. we don't likes it, my precious.
        Set nodes = new HashSet();
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
        { return nodeMap.asGraph(); }
    
    /**
        remove from the parent all of the triples that correspond to a reification
        of t on tag.
    */
    private void parentRemoveQuad( Node n, Triple t )
        {
        parent.delete( new Triple( n, RDF.Nodes.type, RDF.Nodes.Statement ) );
        parent.delete( new Triple( n, RDF.Nodes.subject, t.getSubject() ) );
        parent.delete( new Triple( n, RDF.Nodes.predicate, t.getPredicate() ) );
        parent.delete( new Triple( n, RDF.Nodes.object, t.getObject() ) ); 
        }        
              
    /**
        our string representation is <R ...> wrapped round the string representation
        of our node map.
    */
    public String toString()
        { return "<R " + nodeMap + ">"; }
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
