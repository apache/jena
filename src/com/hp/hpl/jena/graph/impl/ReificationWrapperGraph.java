/*
 	(c) Copyright 2008 Hewlett-Packard Development Company, LP
 	All rights reserved.
 	$Id: ReificationWrapperGraph.java,v 1.1 2008-11-21 15:28:18 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.impl;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.shared.ReificationStyle;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
    <p>
      A ReificationWrapperGraph wraps an existing graph and provides a
      reification API on that graph that stores the reification triples as 
      triples in that graph. Unlike Simple(ha!)Reifier, it does not maintain
      independent state about the reification fragments.
    </p>
    
     <p>
       One use (the provoking use) is to wrap TDB and SDB graphs to give
       a reification API that respects persistent data.
     </p>
     
 	@author kers
*/
public final class ReificationWrapperGraph extends WrappedGraph
    {
    protected final ReificationStyle style;
    
    /**
        Initialise this wrapper with the base graph and the preferred
        reification style. <i>Only <b>Standard</b> is officially supported</i>.
        
     	@param base
     	@param style
     */
    public ReificationWrapperGraph( Graph base, ReificationStyle style )
        {
        super( base );
        this.style = style;  
        this.reifier = new ReificationWrapper( this, style );
        }
    
    /**
        Answer the wrapped graph.
    */
    public Graph getBase()
        { return base; }
    
    public ExtendedIterator find( TripleMatch tm )
        { return find( tm.asTriple() ); }
    
    public ExtendedIterator find( Node s, Node p, Node o )
        { return find( Triple.create( s, p, o ) ); }
    
    private ExtendedIterator find( Triple t )
        { 
        ExtendedIterator found = base.find( t );
        ExtendedIterator result = reifier.getStyle().conceals() ? found.filterDrop( ReificationWrapper.isReificationTriple ) : found;
        return SimpleEventManager.notifyingRemove( this, result );
        }
    
    /**
        Answer the number of unconcealed triples in this graph, which is the
        number of triples in the base graph minus the number of concealed
        triples (and concealed triples are not supported).
        
     	@see com.hp.hpl.jena.graph.impl.WrappedGraph#size()
    */
    public int size()  
        { 
        ReificationWrapper br = (ReificationWrapper) reifier;
        return base.size() - br.countConcealed();
        }
    
    /**
        An identifying string. For diagnostic messages only.
    */
    public String toString()
        { return "<rwg " + index + ">"; }
    
    private static int count = 0;
    private int index = count++;
    }
