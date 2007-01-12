/*
 	(c) Copyright 2006 Hewlett-Packard Development Company, LP
 	All rights reserved.
 	$Id: BasicFBReifier.java,v 1.4 2007-01-12 14:26:58 chris-dollin Exp $
*/

package com.hp.hpl.jena.reasoner.rulesys;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

public class BasicFBReifier implements Reifier
    {
    protected final GetReifier deductions;
    protected final Graph parent;
    protected final Reifier base;
    
    public BasicFBReifier( BasicForwardRuleInfGraph parent, Reifier base, GetReifier deductions, ReificationStyle style )
        {
        this.deductions = deductions;
        this.parent = parent;
        this.base = base;
        }
    
    interface GetReifier
        { Reifier getReifier(); }

    public ExtendedIterator allNodes()
        { return base.allNodes().andThen( deductions.getReifier().allNodes() ); }

    public ExtendedIterator allNodes( Triple t )
        { return base.allNodes( t ).andThen( deductions.getReifier().allNodes() );  }

    public void close()
        { base.close(); }

    public ExtendedIterator find( TripleMatch m )
        { return base.find( m ).andThen( deductions.getReifier().find( m ) ); }

    public ExtendedIterator findEither( TripleMatch m, boolean showHidden )
        { 
        return 
            base.findEither(  m, showHidden )
            .andThen( deductions.getReifier().findEither(  m, showHidden ) ); 
        }

    public ExtendedIterator findExposed( TripleMatch m )
        { return base.findExposed( m ).andThen( deductions.getReifier().findExposed( m ) );  }

    public Graph getParentGraph()
        { return parent; }

    public ReificationStyle getStyle()
        { return base.getStyle(); }

    public boolean handledAdd( Triple t )
        { return base.handledAdd( t ); }

    public boolean handledRemove( Triple t )
        { return base.handledRemove( t ); }

    public boolean hasTriple( Node n )
        { return base.hasTriple( n ) || deductions.getReifier().hasTriple( n ); }

    public boolean hasTriple( Triple t )
        { return base.hasTriple( t ) || deductions.getReifier().hasTriple( t ); }

    public Node reifyAs( Node n, Triple t )
        { return base.reifyAs( n, t ); }

    public void remove( Node n, Triple t )
        { base.remove( n, t ); }

    public void remove( Triple t )
        { base.remove(  t  ); }

    public int size()
        { return deductions.getReifier().size(); }

    public Triple getTriple( Node n )
        {
        Triple a = base.getTriple( n );
        Triple b = deductions.getReifier().getTriple( n );
        if (a != null && b != null) throw new JenaException( "TODO: have multiple answers for getTrple, viz " + a + " and " + b );
        return a == null ? b : a;
        }
    }

