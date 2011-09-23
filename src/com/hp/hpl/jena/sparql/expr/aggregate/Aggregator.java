/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Systems Ltd
 * (c) Copyright 2010 Epimorphics Ltd
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.expr.aggregate;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.expr.Expr ;
import com.hp.hpl.jena.sparql.graph.NodeTransform ;

/** An Aggregator is the processor for the whole result stream.
 *  BindingKeys identify which section of a group we're in. */ 

public interface Aggregator
{
    //-- Aggregator - per query (strictly, one per SELECT level), unique even if mentioned several times.
    //-- Accumulator - per group per key section processors (from AggregatorBase)

    /** Create an accumulator for this aggregator */ 
    public Accumulator createAccumulator() ;
    
    /** Value if there are no elements in any group : return null for no result */
    public Node getValueEmpty() ;
    
    public String toPrefixString()  ;
    // Key to identify an aggregator as syntax for duplicate use in a query.
    public String key() ;           
    
    /** Get the expression - may be null (e.g COUNT(*)) ; */ 
    public Expr getExpr() ;
    public Aggregator copy(Expr expr) ;
    public Aggregator copyTransform(NodeTransform transform) ;
    
    public int hashCode() ;
    public boolean equals(Object other) ;
}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Systems Ltd
 * (c) Copyright 2010 Epimorphics Ltd
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