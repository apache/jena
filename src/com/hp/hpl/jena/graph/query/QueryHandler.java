/*
  (c) Copyright 2002, 2003, 2004 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: QueryHandler.java,v 1.15 2004-12-06 13:50:13 andy_seaborne Exp $
*/

package com.hp.hpl.jena.graph.query;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.util.iterator.*;

/**
    a QueryHandler handles queries on behalf of a graph. It's primary purpose
    is to isolate changes to the query interface away from the Graph; multiple
    different Graph implementations can use the same QueryHandler class, such
    as the built-in SimpleQueryHandler.

	@author kers
*/

public interface QueryHandler
    {
    /**
        prepare a plan for generating bindings given the query _q_ and the result
        variables _variables_.
    */
    public BindingQueryPlan prepareBindings( Query q, Node [] variables );

    /**
        produce a single Stage which will probe the underlying graph for triples
        matching p and inject all the resulting bindings into the processing stream
        (see Stage for details)
    <p>
        _map_ is the variable binding map to use and update. _constraints_ is
        the current constraint expression: if this Stage can absorb some of the 
        ANDed constraints, it may do so, and remove them from the ExpressionSet.
    */
    public Stage patternStage( Mapping map, ExpressionSet constraints, Triple [] p );
    
    /**
    	deliver a plan for executing the tree-match query defined by _pattern_.
    */
    public TreeQueryPlan prepareTree( Graph pattern );
    
    /**
    	deliver an iterator over all the objects _o_ such that _(s, p, o)_ is in the
    	underlying graph; nulls count as wildcards.  .remove() is not defined 
        on this iterator.
    */
    public ExtendedIterator objectsFor( Node s, Node p );

    /**
    	deliver an iterator over all the subjects _s_ such that _(s, p, o)_ is in the
    	underlying graph; nulls count as wildcards.  .remove() is not defined 
        on this iterator.
    */
    public ExtendedIterator subjectsFor( Node p, Node o );

    /**
         Answer an iterator over all the predicates <code>p</code> such that
         <code>(s, p, o)</code> is in the underlying graph.  .remove() is not 
         defined on this iterator.
    */
    public ExtendedIterator predicatesFor( Node s, Node o );
    
    /**
        true iff the graph contains a triple in which n appears somewhere.
        if n is a fluid node, it is not defined whether true or false is returned,
        so don't do that.
    */
    public boolean containsNode( Node n );

    }

/*
    (c) Copyright 2002, 2003, 2004 Hewlett-Packard Development Company, LP
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
