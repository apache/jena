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

import java.util.Iterator ;
import java.util.List ;

import org.apache.jena.atlas.iterator.Iter ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.Quad ;

// Combine with Jena GraphUtils.
public class GraphOps
{
    
    public static boolean containsGraph(DatasetGraph dsg, Node gn)
    {
        // [[DynDS]]
        if ( Quad.isDefaultGraph(gn))
            return true ;
        if ( Quad.isUnionGraph(gn))
            return true ;
        return dsg.containsGraph(gn) ;
    }
    
    public static Graph getGraph(DatasetGraph dsg, Node gn)
    {
        // [[DynDS]]
        if ( gn == null )
            return dsg.getDefaultGraph() ;
        if ( Quad.isDefaultGraph(gn) )
            // Explicit or generated.
            return dsg.getDefaultGraph() ;
        if ( Quad.isUnionGraph(gn))
            return unionGraph(dsg) ;
        return dsg.getGraph(gn) ;
    }
    
    public static Graph unionGraph(DatasetGraph dsg)
    {
        List<Node> x = Iter.toList(dsg.listGraphNodes()) ;
        return new GraphUnionRead(dsg, x) ;
    }

    public static void addAll(Graph g, Iterator<Triple> iter)
    {
        while(iter.hasNext())
            g.add(iter.next()) ;
        Iter.close(iter) ;
    }

    public static void addAll(Graph g, Iterable<Triple> iter)
    {
        addAll(g, iter.iterator()) ;
    }

    public static void deleteAll(Graph g, Iterator<Triple> iter)
    {
        while(iter.hasNext())
            g.delete(iter.next()) ;
        Iter.close(iter) ;
    }

    public static void deleteAll(Graph g, Iterable<Triple> iter)
    {
        deleteAll(g, iter.iterator()) ;
    }

}
