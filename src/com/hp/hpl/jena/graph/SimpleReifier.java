/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: SimpleReifier.java,v 1.8 2003-04-04 13:59:51 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph;

/**
	@author kers
<p>
    A base-level implementation of Reifier, intended to be straightforward
    and obvious. It fails this test nowadays ...
*/

import com.hp.hpl.jena.mem.*;
import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.vocabulary.*;

import java.util.*;

public class SimpleReifier implements Reifier
    {
    private Graph parent;
    private Graph triples;    
    private boolean passing = false;
          
    public FragmentMap nodeMap;
    
    /* construct a simple reifier that is bound to the parent graph */
    public SimpleReifier( Graph parent )
        {
        this.parent = parent;
        this.triples = new GraphMem();
        this.nodeMap = new FragmentMap();
        }
            
    public Graph getHiddenTriples()
        { // TODO: turn into a dynamic graph
        Graph result = new GraphMem();
        ((SimpleReifier) result.getReifier()).passing = true;
        Iterator it = nodeMap.keySet().iterator();
        while (it.hasNext()) include( result, (Node) it.next() );
        return result;
        }
        
    // include into g the reification triples of f
    private void include( Graph g, Node node )
        {
        Object f = nodeMap.get( node );
        if (f instanceof Triple)
            { includeInto( g, node, (Triple) f ); }
        else
            { ((Fragments) f).includeInto( g ); }
        }
        
    private void includeInto( Graph g, Node node, Triple t )
        {
        g.add( new Triple( node, Reifier.subject, t.getSubject() ) );
        g.add( new Triple( node, Reifier.predicate, t.getPredicate() ) );
        g.add( new Triple( node, Reifier.object, t.getObject() ) );
        g.add( new Triple( node, Reifier.type, Reifier.Statement ) );
        }
        
    /** return the parent graph we are bound to */
    public Graph getParentGraph()
        { return parent; }
        
    /** return the triple bound to _n_ */
    public Triple getTriple( Node n )        
        {
        Object partial = nodeMap.get( n );
        // System.err.println( "SR::getTriple( " + n + " = " + partial  + ")" );
        if (partial == null)
            return null;
        else if (partial instanceof Triple) 
            return (Triple) partial;
        else
            return getTriple( n, (Fragments) partial );
        }
        
    private Triple getTriple( Node n, Fragments w )
        { 
            // System.err.println( "| getTriple: " + w.isComplete() );
            return w.isComplete() ? nodeMap.putTriple( n, w.asTriple() ) : null; }
        
    /** true iff there is a triple bound to _n_ */
    public boolean hasTriple( Node n )
    	{ return getTriple( n ) != null; }
        
    /** */
    public ExtendedIterator allNodes()
        {
        return WrappedIterator.create( nodeMap.keySet().iterator() ) .filterKeep ( completeWomble );
        }
        
    private Filter completeWomble = new Filter()
        { public boolean accept( Object x ) { return isComplete( (Node) x ); } };
        
    private boolean isComplete( Node n )
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
        Triple already = getTriple( tag );
        if (already != null && !already.equals( t ))
            throw new Reifier.AlreadyReifiedException( tag );
        Object mapped = nodeMap.get( tag );
        if (mapped == null)
            {
            nodeMap.put( tag, t );
            }
        else if (mapped.equals( t ))
            { /* that's easy */
            }
        else
            {
            parent.add( new Triple( tag, type, Statement ) );
            parent.add( new Triple( tag, subject, t.getSubject() ) );
            parent.add( new Triple( tag, predicate, t.getPredicate() ) );
            parent.add( new Triple( tag, object, t.getObject() ) ); 
            }
        triples.add( t );
        return tag; 
    	}
        
    /** unbind _n_ */    	
    public void remove( Node n, Triple t )
        {
        Object x = nodeMap.get( n );
        if (x instanceof Triple)
            { if (x.equals( t )) nodeMap.remove( n ); }
        else
            {
            parent.delete( new Triple( n, type, Statement ) );
            parent.delete( new Triple( n, subject, t.getSubject() ) );
            parent.delete( new Triple( n, predicate, t.getPredicate() ) );
            parent.delete( new Triple( n, object, t.getObject() ) ); 
            }
        }
        
    public boolean hasTriple( Triple t )
        { 
        Iterator it = allNodes();
        while (it.hasNext())
            if (getTriple( (Node) it.next() ) .equals( t )) return true;
        return false;
        }
          
    public boolean handledAdd( Triple t )
        {
        int s = Fragments.getFragmentSelector( t );       
         if (passing || s < 0)
            return false;
        else
            {
            getFragment( t ).add( s, t.getObject() );
            return true;
            }
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
        { return nodeMap.putFragments( s, new Fragments( s ) .addTriple( t ) ); }

    public boolean handledRemove( Triple t )
        {
        int s = Fragments.getFragmentSelector( t );
        if (passing || s < 0)
            return false;
        else
            {
            getFragment( t ).remove( s, t.getObject() );
            return true;
            }
        }

           
    public void remove( Triple t )
        {
        triples.delete( t );        
    /* */
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
        
    public String toString()
        {
        return "<R " + nodeMap + ">";
        }
    }
    
/*
    (c) Copyright Hewlett-Packard Company 200, 2003
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
