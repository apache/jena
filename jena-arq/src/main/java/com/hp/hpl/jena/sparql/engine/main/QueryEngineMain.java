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

import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.sparql.algebra.Algebra ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.optimize.TransformScopeRename ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.engine.* ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterRoot ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIteratorCheck ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIteratorTiming ;
import com.hp.hpl.jena.sparql.util.Context ;

public class QueryEngineMain extends QueryEngineBase
{
    static public QueryEngineFactory getFactory() { return factory ; } 
    static public void register()       { QueryEngineRegistry.addFactory(factory) ; }
    static public void unregister()     { QueryEngineRegistry.removeFactory(factory) ; }

    public QueryEngineMain(Op op, DatasetGraph dataset, Binding input, Context context)
    { super(op, dataset, input, context) ; }
    
    public QueryEngineMain(Query query, DatasetGraph dataset, Binding input, Context context)
    { 
        super(query, dataset, input, context) ;
    }

    @Override
    public QueryIterator eval(Op op, DatasetGraph dsg, Binding input, Context context)
    {
        ExecutionContext execCxt = new ExecutionContext(context, dsg.getDefaultGraph(), dsg, QC.getFactory(context)) ;
        QueryIterator qIter1 = QueryIterRoot.create(input, execCxt) ;
        QueryIterator qIter = QC.execute(op, qIter1, execCxt) ;
        // Wrap with something to check for closed iterators.
        qIter = QueryIteratorCheck.check(qIter, execCxt) ;
        // Need call back.
        if ( context.isTrue(ARQ.enableExecutionTimeLogging) )
            qIter = QueryIteratorTiming.time(qIter) ;
        return qIter ;
    }
    
    @Override
    protected Op modifyOp(Op op)
    { 
        if ( context.isFalse(ARQ.optimization) )
            return minimalModifyOp(op) ;
        return Algebra.optimize(op, super.context) ;
    }
    
    protected Op minimalModifyOp(Op op)
    {
        // Must always do this for QueryEngineMain.
        // The optimizer does do this.
        return TransformScopeRename.transform(op) ;
    }
    
    // -------- Factory
    
    private static QueryEngineFactory factory = new QueryEngineMainFactory() ;
    
    protected static class QueryEngineMainFactory implements QueryEngineFactory
    {
        @Override
        public boolean accept(Query query, DatasetGraph dataset, Context context) 
        { return true ; }

        @Override
        public Plan create(Query query, DatasetGraph dataset, Binding input, Context context)
        {
            QueryEngineMain engine = new QueryEngineMain(query, dataset, input, context) ;
            return engine.getPlan() ;
        }
        
        @Override
        public boolean accept(Op op, DatasetGraph dataset, Context context) 
        { return true ; }

        @Override
        public Plan create(Op op, DatasetGraph dataset, Binding binding, Context context)
        {
            QueryEngineMain engine = new QueryEngineMain(op, dataset, binding, context) ;
            return engine.getPlan() ;
        }
    }
}
