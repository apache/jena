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

package com.hp.hpl.jena.sparql.engine.main;

import org.apache.jena.atlas.logging.Log ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.sparql.core.BasicPattern ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterBlockTriples ;
import com.hp.hpl.jena.sparql.engine.optimizer.reorder.ReorderLib ;
import com.hp.hpl.jena.sparql.engine.optimizer.reorder.ReorderTransformation ;
import com.hp.hpl.jena.sparql.mgt.Explain ;
import com.hp.hpl.jena.sparql.util.Utils ;

/** Generic - always works - StageGenerator */
public class StageGeneratorGeneric implements StageGenerator {
    public StageGeneratorGeneric() {}
    private static final ReorderTransformation reorderFixed = ReorderLib.fixed() ;
    
    @Override
    public QueryIterator execute(BasicPattern pattern, QueryIterator input, ExecutionContext execCxt) {
        if ( input == null )
            Log.fatal(this, "Null input to " + Utils.classShortName(this.getClass())) ;

        Graph graph = execCxt.getActiveGraph() ;

        // Choose reorder transformation and execution strategy.

        ReorderTransformation reorder = reorderFixed ;
        StageGenerator executor = StageBuilder.executeInline ;

        return execute(pattern, reorder, executor, input, execCxt) ;
    }

    protected QueryIterator execute(BasicPattern pattern, ReorderTransformation reorder, StageGenerator execution,
                                    QueryIterator input, ExecutionContext execCxt)
    {
        Explain.explain(pattern, execCxt.getContext()) ;
        if ( reorder != null ) {
            pattern = reorder.reorder(pattern) ;
            Explain.explain("Reorder", pattern, execCxt.getContext()) ;
        }
        return QueryIterBlockTriples.create(input, pattern, execCxt) ;
    }
}
