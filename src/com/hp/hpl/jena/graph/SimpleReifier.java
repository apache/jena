/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: SimpleReifier.java,v 1.5 2003-03-27 15:23:01 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph;

/**
	@author kers
<br>
    A base-level implementation of Reifier, intended to be straightforward
    and obvious.
*/

import com.hp.hpl.jena.mem.*;
import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.vocabulary.*;

import java.util.*;

public class SimpleReifier implements Reifier
    {
    private Graph parent;
    private Graph triples;    
        
    private HashMap nodeMap;
    
    static final Node type = RDF.type.asNode();
    static final Node subject = RDF.subject.asNode();
    static final Node predicate = RDF.predicate.asNode();
    static final Node object = RDF.object.asNode();
    static final Node Statement = RDF.Statement.asNode();
          
    /* construct a simple reifier that is bound to the parent graph */
    public SimpleReifier( Graph parent )
        {
        this.parent = parent;
        this.triples = new GraphMem();
        this.nodeMap = new HashMap();
        }
        
    static class Womble
        {
        Set types;
        Set subjects;
        Set predicates;
        Set objects;

        Womble() 
            {
            types = new HashSet();
            subjects = new HashSet();
            predicates = new HashSet();
            objects = new HashSet();
            }
        }
        
    /** return the parent graph we are bound to */
    public Graph getParentGraph()
        { return parent; }
        
    /** return the triple bound to _n_ */
    public Triple getTriple( Node n )        
        { return findTriple( n ); }

    private Triple findTriple( Node n )
        {
        Object map = nodeMap.get( n );
        if (map instanceof Triple) return (Triple) map;
        Womble w = (Womble) map;
        if (w == null) return null;
        if (w.types.size() != 1 || w.subjects.size() != 1 || w.predicates.size() != 1 || w.objects.size() != 1) return null;
        Triple t = new Triple( only( w.subjects ), only( w.predicates ), only( w.objects ) );
        nodeMap.put( n, t );
        return t;
        }
        
    private Node only( Set s )
        { return (Node) s.iterator().next(); }
        
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
        if (x instanceof Triple) return true;
        Womble w = (Womble) nodeMap.get( n );
        return w.types.size() == 1 && w.subjects.size() == 1 && w.predicates.size() == 1 && w.objects.size() == 1;
        }
        
    /** return the graph of reified triples. */
    public Graph getReifiedTriples()
        { return triples; }
        
    /** reify a triple _t_, allocating a new blank node to represent it */
    public Node reify( Triple t )
        { return reifyAs( Node.createAnon(), t ); }        
        
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
            {
            if (x.equals( t )) nodeMap.remove( n );
            }
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
        // return nodeToTriple.containsValue( t );
        }
        
    public boolean handledAdd( Triple t )
        {
        if (special( t ))
            {
            burble( t );
            return true;
            }
        else
            return false;
        }
        
    private void burble( Triple t )
        {
        Node s = t.getSubject();
        Node p = t.getPredicate();
        Node o = t.getObject();
        Womble w = (Womble) nodeMap.get( s );
        if (w == null) { w = new Womble(); nodeMap.put( s, w ); }
        if (p.equals( subject )) 
            {
            w.subjects.add( o );
            }
        if (p.equals( predicate )) 
            {
            w.predicates.add( o );
            }
        if (p.equals( object )) 
            {
            w.objects.add( o );
            }
        if (p.equals( type ))            
            {
            w.types.add( o );
            }
        }

    private void antiBurble( Triple t )
        {
        Node s = t.getSubject();
        Node p = t.getPredicate();
        Node o = t.getObject();
        Object x = nodeMap.get( s );
        if (t.equals( x )) { nodeMap.remove( s ); return; }
        if (x instanceof Triple)
            {
            Triple tt = (Triple) x;
            Womble ww = new Womble();
            if (p.equals( subject )) ww.subjects.add( tt.getSubject() );
            if (p.equals( predicate )) ww.predicates.add( tt.getPredicate() );
            if (p.equals( object )) ww.objects.add( tt.getObject() );
            if (p.equals( type )) ww.types.add( Statement );
            nodeMap.put( s, ww );
            x = ww;
            }
        Womble w = (Womble) x;
        if (w == null) return;
        if (p.equals( subject )) 
            {
            w.subjects.remove( o );
            }
        if (p.equals( predicate )) 
            {
            w.predicates.remove( o );
            }
        if (p.equals( object )) 
            {
            w.objects.remove( o );
            }
        if (p.equals( type ))            
            {
            w.types.remove( o );
            }
        if (w.subjects.size() == 0 && w.predicates.size() == 0 && w.objects.size() == 0 && w.types.size() == 0)
            nodeMap.remove( s );
        }
    
    public boolean handledRemove( Triple t )
        {
        if (special( t ))
            {
            antiBurble( t );
            return true;
            }
        else
            return false;
        }

    private boolean special( Triple t )
        {
        Node p = t.getPredicate();
        return p == subject || p == predicate || p == object || p == type && t.getObject().equals( Statement );
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
    }
    
/*
    (c) Copyright Hewlett-Packard Company 2002
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
