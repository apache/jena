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

package org.apache.jena.sdb.engine;

import org.apache.jena.query.ARQ ;
import org.apache.jena.query.Query ;
import org.apache.jena.sdb.SDB ;
import org.apache.jena.sdb.SDBException ;
import org.apache.jena.sdb.Store ;
import org.apache.jena.sdb.compiler.OpSQL ;
import org.apache.jena.sdb.compiler.SDBCompile ;
import org.apache.jena.sdb.core.SDBRequest ;
import org.apache.jena.sdb.store.DatasetGraphSDB ;
import org.apache.jena.sparql.ARQConstants ;
import org.apache.jena.sparql.algebra.Algebra ;
import org.apache.jena.sparql.algebra.Op ;
import org.apache.jena.sparql.algebra.Transformer ;
import org.apache.jena.sparql.algebra.optimize.TransformFilterEquality ;
import org.apache.jena.sparql.algebra.optimize.TransformPropertyFunction ;
import org.apache.jena.sparql.core.DatasetDescription;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.DynamicDatasets;
import org.apache.jena.sparql.engine.* ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.engine.binding.BindingRoot ;
import org.apache.jena.sparql.engine.iterator.QueryIterRoot ;
import org.apache.jena.sparql.engine.iterator.QueryIterSingleton ;
import org.apache.jena.sparql.engine.iterator.QueryIteratorCheck ;
import org.apache.jena.sparql.engine.main.QC ;
import org.apache.jena.sparql.util.Context ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class QueryEngineSDB extends QueryEngineBase
{
    private static Logger log = LoggerFactory.getLogger(QueryEngineSDB.class) ; 
    private Store store ;
    private SDBRequest request = null ;
    private Op originalOp = null ;

    public QueryEngineSDB(Store store, Query q)
    {
        this(new DatasetGraphSDB(store, SDB.getContext().copy()), q, BindingRoot.create(), SDB.getContext().copy()) ;
    }
    
    public QueryEngineSDB(DatasetGraphSDB dsg, Query query, Binding initialBinding, Context context)
    {
        super(query, dsg, initialBinding, context) ;
        init(dsg, query, initialBinding, context) ;
    }

    public QueryEngineSDB(DatasetGraphSDB dsg, Op op, Binding initialBinding, Context context)
    {
        super(op, dsg, initialBinding, context) ;
        init(dsg, null, initialBinding, context) ;
    }
    
//    @Override
//    protected Op modifyOp(Op op)
//    { 
//        return op ;
//    }
    
    private void init(DatasetGraphSDB dsg, Query query, Binding initialBinding, Context context)
    {
        if ( context == null )
            context = ARQ.getContext().copy() ;

        DatasetDescription dsDesc = DatasetDescription.create(query, context) ;
        if ( dsDesc != null )
            super.dataset = DynamicDatasets.dynamicDataset(dsDesc, dataset, context.isTrue(SDB.unionDefaultGraph) ) ;

        if ( context.isDefined(ARQConstants.sysDatasetDescription) )
            throw new SDBException("Queries with dataset descriptions set in the context not supported" ) ;
        
        this.store = dsg.getStore() ;
        this.request = new SDBRequest(store, query, context) ;
        this.originalOp = getOp() ;
        // Enable transformations : use own choice of transformations.
        // e.g. don't place filters
        // Op op = Algebra.optimize(originalOp, context) ;
        Op op = originalOp ;
        
        // Do property functions.
        op = Transformer.transform(new TransformPropertyFunction(context), op) ;
        op = Transformer.transform(new TransformFilterEquality(), op) ;

        // Quad it now so it can be passed to Compile.compile
        // Only apply if not a rewritten DynamicDataset
        if ( dsDesc == null )
            op = Algebra.toQuadForm(op) ;
        
        // Compile to SQL / extract parts to execute as SQL.
        op = SDBCompile.compile(store, op, initialBinding, context, request) ;
        
        setOp(op) ;
    }
    
    public SDBRequest getRequest()      { return request ; }
    
    @Override
    public void close()
    { 
        super.close();
    }

    @Override
    public QueryIterator eval(Op op, DatasetGraph dsg, Binding binding, Context context)
    {
        ExecutionContext execCxt = new ExecutionContext(context, dsg.getDefaultGraph(), dsg, QC.getFactory(context)) ;

        // This pattern is common to QueryEngineMain - find a sharing pattern
        if ( ! ( op instanceof OpSQL ) )
        {
            // Not top - invoke the main query engine as a framework to
            // put all the sub-opSQL parts together.
            QueryIterator input = QueryIterSingleton.create(binding, execCxt) ;
            QueryIterator qIter = QC.execute(op, input, execCxt) ;  // OpExecutor from main query engine.
            qIter = QueryIteratorCheck.check(qIter, execCxt) ;
            return qIter ;
        }
        // Direct.
        OpSQL opSQL = (OpSQL)op ;
        QueryIterator qIter ;
        if ( opSQL.getSqlNode() == null )
        {
            // Empty BGP, nothing else.
            // Just return the answer.
            if ( binding != null && binding.size() != 0 )
                qIter = QueryIterSingleton.create(binding, execCxt) ;
            else
                qIter = QueryIterRoot.create(execCxt) ;
        }
        else
            qIter = opSQL.exec(binding, execCxt) ;
        qIter = QueryIteratorCheck.check(qIter, execCxt) ;
        return qIter ;
    }
    
    // -------- Factory
    
    static private QueryEngineFactory factory = new QueryEngineFactorySDB() ;
    static public QueryEngineFactory getFactory()   { return factory ; } 
    static public void register()       { QueryEngineRegistry.addFactory(factory) ; }
    static public void unregister()     { QueryEngineRegistry.removeFactory(factory) ; }
    
    
    private static class QueryEngineFactorySDB implements QueryEngineFactory
    {
        @Override
        public boolean accept(Query query, DatasetGraph dataset, Context context)
        {
            if ( dataset instanceof DatasetGraphSDB )
                return true ;
            return false ;
        }

        @Override
        public Plan create(Query query, DatasetGraph dataset, Binding inputBinding, Context context)
        {
            QueryEngineSDB qe = new QueryEngineSDB((DatasetGraphSDB)dataset , query, inputBinding, context) ;
            return qe.getPlan() ;
        }

        @Override
        public boolean accept(Op op, DatasetGraph dataset, Context context)
        {
            if ( dataset instanceof DatasetGraphSDB )
                return true ;
            return false ;
        }

        @Override
        public Plan create(Op op, DatasetGraph dataset, Binding inputBinding, Context context)
        {
            if ( inputBinding == null )
                inputBinding = BindingRoot.create();
            QueryEngineSDB qe = new QueryEngineSDB((DatasetGraphSDB)dataset, op, inputBinding, context) ;
            return qe.getPlan() ;
        }

    }
}
