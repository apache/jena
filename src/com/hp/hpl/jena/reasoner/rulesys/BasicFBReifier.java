/*
 	(c) Copyright 2006 Hewlett-Packard Development Company, LP
 	All rights reserved.
 	$Id: BasicFBReifier.java,v 1.2 2007-01-12 10:42:30 chris-dollin Exp $
*/

package com.hp.hpl.jena.reasoner.rulesys;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.impl.SimpleReifier;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

public class BasicFBReifier implements Reifier
    {
    protected final GetReifier other;
    protected final Graph parent;
    protected final Reifier self;
    
    public BasicFBReifier( BasicForwardRuleInfGraph parent, GetReifier other, ReificationStyle style )
        {
        this.other = other;
        this.parent = parent;
        this.self = new SimpleReifier( parent, style );
        }
    
    interface GetReifier
        { Reifier getReifier(); }

    public ExtendedIterator allNodes()
        { return self.allNodes().andThen( other.getReifier().allNodes() ); }

    public ExtendedIterator allNodes( Triple t )
        {
        throw new JenaException( "not implemented" );
        }

    public void close()
        { self.close(); }

    public ExtendedIterator find( TripleMatch m )
        { return self.find( m ).andThen( other.getReifier().find( m ) ); }

    public ExtendedIterator findEither( TripleMatch m, boolean showHidden )
        { 
        return 
            self.findEither(  m, showHidden )
            .andThen( other.getReifier().findEither(  m, showHidden ) ); 
        }

    public ExtendedIterator findExposed( TripleMatch m )
        { return self.findExposed( m ).andThen( other.getReifier().findExposed( m ) );  }

    public Graph getParentGraph()
        { return parent; }

    public ReificationStyle getStyle()
        { return self.getStyle(); }

    public boolean handledAdd( Triple t )
        { return self.handledAdd( t ); }

    public boolean handledRemove( Triple t )
        {
        throw new JenaException( "not implemented" );
        }

    public boolean hasTriple( Node n )
        { return self.hasTriple( n ) || other.getReifier().hasTriple( n ); }

    public boolean hasTriple( Triple t )
        { return self.hasTriple( t ) || other.getReifier().hasTriple( t ); }

    public Node reifyAs( Node n, Triple t )
        { return self.reifyAs( n, t ); }

    public void remove( Node n, Triple t )
        { 
        self.remove( n, t );
        other.getReifier().remove( n, t );
        }

    public void remove( Triple t )
        {
        throw new JenaException( "not implemented" );
        }

    public int size()
        { return self.size() + other.getReifier().size(); }

    public Triple getTriple( Node n )
        {
        Triple a = self.getTriple( n );
        Triple b = other.getReifier().getTriple( n );
        if (a != null && b != null) throw new JenaException( "TODO: have multiple answers for getTrple, viz " + a + " and " + b );
        return a == null ? b : a;
        }

    }

