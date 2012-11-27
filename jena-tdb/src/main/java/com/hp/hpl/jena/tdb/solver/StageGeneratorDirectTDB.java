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

package com.hp.hpl.jena.tdb.solver;

import org.apache.jena.atlas.iterator.Filter ;
import org.apache.jena.atlas.lib.Tuple ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.sparql.core.BasicPattern ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.main.StageGenerator ;
import com.hp.hpl.jena.tdb.store.GraphTDB ;
import com.hp.hpl.jena.tdb.store.NodeId ;

/** Execute TDB requests directly -- no reordering
 *  Using OpExecutor is preferred.
 */ 
public class StageGeneratorDirectTDB implements StageGenerator
{
    // Using OpExecutor is preferred.
    StageGenerator above = null ;
    
    public StageGeneratorDirectTDB(StageGenerator original)
    {
        above = original ;
    }
    
    @Override
    public QueryIterator execute(BasicPattern pattern, QueryIterator input, ExecutionContext execCxt)
    {
        // --- In case this isn't for TDB
        Graph g = execCxt.getActiveGraph() ;
        
        if ( ! ( g instanceof GraphTDB ) )
            // Not us - bounce up the StageGenerator chain
            return above.execute(pattern, input, execCxt) ;
        GraphTDB graph = (GraphTDB)g ;
        Filter<Tuple<NodeId>> filter = QC2.getFilter(execCxt.getContext()) ;
        return SolverLib.execute(graph, pattern, input, filter, execCxt) ;
    }
}
