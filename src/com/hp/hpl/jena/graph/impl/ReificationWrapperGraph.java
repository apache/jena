/*
 	(c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 	All rights reserved.
 	$Id: ReificationWrapperGraph.java,v 1.2 2008-12-28 19:31:53 andy_seaborne Exp $
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

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
