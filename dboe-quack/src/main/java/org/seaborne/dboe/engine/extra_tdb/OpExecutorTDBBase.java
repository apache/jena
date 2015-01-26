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

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.core.BasicPattern ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.expr.ExprList ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;
import com.hp.hpl.jena.tdb.store.GraphTDB ;

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
