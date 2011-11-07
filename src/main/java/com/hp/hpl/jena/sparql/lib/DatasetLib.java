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

package com.hp.hpl.jena.sparql.lib;

import java.util.Iterator ;

import org.openjena.atlas.io.IndentedWriter ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.DatasetGraphFactory ;
import com.hp.hpl.jena.sparql.sse.writers.WriterGraph ;

public class DatasetLib
{
    /** Write, in SSE format (a debugging format).
     */
    public static void dump(DatasetGraph dataset)
    {
        WriterGraph.output(IndentedWriter.stdout, dataset, null) ;
        IndentedWriter.stdout.flush();
    }
    
    /**
     * Return true if the datasets are isomorphic - same names for graphs, graphs isomorphic. 
     */
    public static boolean isomorphic(Dataset dataset1, Dataset dataset2)
    {
        return isomorphic(dataset1.asDatasetGraph(), dataset2.asDatasetGraph()) ;
    }
    
    /**
     * Return true if the datasets are isomorphic - same names for graphs, graphs isomorphic. 
     */
    public static boolean isomorphic(DatasetGraph dataset1, DatasetGraph dataset2)
    {
        long x1 = dataset1.size() ;
        long x2 = dataset2.size() ;
        if ( x1 >=0 && x1 != x2 )
            return false ;
        
        boolean b = dataset1.getDefaultGraph().isIsomorphicWith(dataset2.getDefaultGraph()) ;
        if ( ! b )
            return b ;
        
        for ( Iterator<Node> iter1 = dataset1.listGraphNodes() ; iter1.hasNext() ; )
        {
            Node gn = iter1.next() ;
            Graph g1 = dataset1.getGraph(gn) ;
            Graph g2 = dataset2.getGraph(gn) ;
            if ( g2 == null )
                return false ;
            if ( ! g1.isIsomorphicWith(g2) )
                return false ;
        }
        
        return true ;
    }
    
    // A DatasetGraph that creates memory graphs on mention */
    public static DatasetGraph createDatasetGraphMem()
    {
        return DatasetGraphFactory.createMem() ;
    }
}
