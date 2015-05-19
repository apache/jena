/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.seaborne.dboe.engine.extra_tdb;

import org.seaborne.dboe.engine.general.OpExecutorBlockFilter ;

import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.Node ;
import org.apache.jena.sparql.core.BasicPattern ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.engine.ExecutionContext ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.expr.ExprList ;
import org.seaborne.tdb2.store.DatasetGraphTDB ;
import org.seaborne.tdb2.store.GraphTDB ;

/** Framework for writing an OpExecutor for TDB based on OpExecutorBlockFilter. */
public abstract class OpExecutorTDBBase extends OpExecutorBlockFilter
{
    protected final DatasetGraphTDB dsgtdb ;
    protected OpExecutorTDBBase(ExecutionContext execCxt) {
        super(execCxt) ;
        if ( isForThisExecutor ) {
            GraphTDB g = (GraphTDB)execCxt.getActiveGraph() ;
            dsgtdb = g.getDSG() ;
        } else {
            this.dsgtdb = null ;
        }
    }
    
    @Override
    protected boolean isForThisExecutor(DatasetGraph dsg, Graph activeGraph, ExecutionContext execCxt) {
        return ( execCxt.getActiveGraph() instanceof GraphTDB ) ;
    }

    @Override
    protected QueryIterator evaluateBlockFilter(Node graphNode, BasicPattern bgp, ExprList exprs, QueryIterator input) {
        return evaluateBlockFilter(dsgtdb, graphNode, bgp, exprs, input) ;
    }

    /** As for {@link OpExecutorBlockFilter#executeBlockFilter} but with the DatasetGraphTDB included. */
    protected abstract QueryIterator evaluateBlockFilter(DatasetGraphTDB dsgtdb, 
                                                         Node graphNode, BasicPattern bgp, 
                                                         ExprList exprs, 
                                                         QueryIterator input) ;
}
