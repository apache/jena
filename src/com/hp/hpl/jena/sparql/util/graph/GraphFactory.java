/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.util.graph;

import com.hp.hpl.jena.graph.Factory ;
import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.sparql.util.PlainGraphMem ;
import com.hp.hpl.jena.sparql.util.RefBoolean ;

/** Ways to make graphs and models */
public class GraphFactory
{
    private static RefBoolean usePlainGraph = new RefBoolean(ARQ.strictGraph) ;
    
    public static Graph createGraph()
    {
        return Factory.createGraphMem() ;
    }

    /** Create a graph - ARQ-wide default type */
    public static Graph createDefaultGraph()
    {
        return usePlainGraph.getValue() ? createPlainGraph() : createJenaDefaultGraph() ;
    }

    /** Create a graph - always the Jena default graph type */
    public static Graph createJenaDefaultGraph()
    {
        return Factory.createDefaultGraph() ;
    }
    
    /** Very simple graph that uses same-term for find() (small-scale use only) */
    public static Graph createPlainGraph()
    {
        return new PlainGraphMem() ;
    }

    public static Graph sinkGraph()
    {
        return new GraphSink() ;
    }

    /** Guaranteed call-through to Jena's ModelFactory operation */
    public static Model makeJenaDefaultModel() { return ModelFactory.createDefaultModel() ; }
    
    /** Create a model over a default graph (ARQ-wide for degault graph type) */ 
    public static Model makeDefaultModel()
    {
        return ModelFactory.createModelForGraph(createDefaultGraph()) ;
    }

    /** Create a model over a plain graph (small-scale use only) */ 
    public static Model makePlainModel()
    {
        return ModelFactory.createModelForGraph(createPlainGraph()) ;
    }
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