/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.hpl.jena.sparql.graph;

import org.openjena.atlas.data.ThresholdPolicy ;

import com.hp.hpl.jena.graph.Factory ;
import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.sparql.util.RefBoolean ;
import com.hp.hpl.jena.sparql.util.graph.GraphSink ;

/** Ways to make graphs and models */
public class GraphFactory
{
    private static RefBoolean usePlainGraph = new RefBoolean(ARQ.strictGraph) ;
    
    /** Create a graph that is a Jena memory graph 
     * @see #createDefaultGraph
     */
    public static Graph createGraphMem()
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
        return new GraphMemSimple2() ; // new PlainGraphMem() ;
    }

    public static Graph sinkGraph()
    {
        return new GraphSink() ;
    }
    
    /** A graph backed by a DistinctDataBag&lt;Triple&gt;. */
    public static Graph createDataBagGraph(ThresholdPolicy<Triple> thresholdPolicy)
    {
        return new GraphDistinctDataBag(thresholdPolicy) ;
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
    
    /** Create a model over a DataBag graph (will spill to disk when it get large) */
    public static Model makeDataBagModel(ThresholdPolicy<Triple> thresholdPolicy)
    {
        return ModelFactory.createModelForGraph(createDataBagGraph(thresholdPolicy)) ;
    }
}
