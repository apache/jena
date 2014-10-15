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

import org.apache.jena.atlas.lib.Lib ;

import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.sparql.algebra.Algebra ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.core.DatasetDescription ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.DynamicDatasets ;
import com.hp.hpl.jena.sparql.core.Substitute ;
import com.hp.hpl.jena.sparql.engine.Plan ;
import com.hp.hpl.jena.sparql.engine.QueryEngineFactory ;
import com.hp.hpl.jena.sparql.engine.QueryEngineRegistry ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIteratorWrapper ;
import com.hp.hpl.jena.sparql.engine.main.QueryEngineMain ;
import com.hp.hpl.jena.sparql.mgt.Explain ;
import com.hp.hpl.jena.sparql.util.Context ;
import com.hp.hpl.jena.tdb.TDB ;
import com.hp.hpl.jena.tdb.TDBException ;
import com.hp.hpl.jena.tdb.migrate.A2 ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;
import com.hp.hpl.jena.tdb.transaction.DatasetGraphTransaction ;

// This exists to intercept the query execution setup.
//  e.g choose the transformation optimizations
// then to make the quad form.
// TDB also uses a custom OpExecutor to intercept certain part 
// of the Op evaluations

public class QueryEngineTDB extends QueryEngineMain
{
    // ---- Wiring
    static public QueryEngineFactory getFactory() { return factory ; } 
    static public void register()       { QueryEngineRegistry.addFactory(factory) ; }
    static public void unregister()     { QueryEngineRegistry.removeFactory(factory) ; }
    
    private Binding initialInput ;

    // ---- Object
    protected QueryEngineTDB(Op op, DatasetGraphTDB dataset, Binding input, Context context)
    {
        super(op, dataset, input, context) ;
        this.initialInput = input ;
    }
    
    private boolean doingDynamicDatasetBySpecialDataset = false ;
    
    protected QueryEngineTDB(Query query, DatasetGraphTDB dataset, Binding input, Context cxt)
    { 
        super(query, dataset, input, cxt) ; 
        DatasetDescription dsDesc = DatasetDescription.create(query, context) ;
        
        if ( dsDesc != null )
        {
            doingDynamicDatasetBySpecialDataset = true ;
            super.dataset = DynamicDatasets.dynamicDataset(dsDesc, dataset, cxt.isTrue(TDB.symUnionDefaultGraph) ) ;
        }
        this.initialInput = input ; 
    }
    
    // Choose the algebra-level optimizations to invoke. 
    @Override
    protected Op modifyOp(Op op)
    {
        op = Substitute.substitute(op, initialInput) ;
        // Optimize (high-level)
        op = super.modifyOp(op) ;

        // Quadification
        // Only apply if not a rewritten DynamicDataset
        if ( ! doingDynamicDatasetBySpecialDataset )
            op = Algebra.toQuadForm(op) ;
        
        // Record it.
        setOp(op) ;
        return op ;
    }

    @Override
    public QueryIterator eval(Op op, DatasetGraph dsg, Binding input, Context context)
    {
        // Top of execution of a query.
        // Op is quad'ed by now but there still may be some (graph ....) forms e.g. paths
        
        // Fix DatasetGraph for global union.
        if ( context.isTrue(TDB.symUnionDefaultGraph) && ! doingDynamicDatasetBySpecialDataset ) 
        {
            op = A2.unionDefaultGraphQuads(op) ;
            Explain.explain("REWRITE(Union default graph)", op, context) ;
        }
        QueryIterator results = super.eval(op, dsg, input, context) ;
        results = new QueryIteratorMaterializeBinding(results) ;
        return results ; 
    }
    
    /** Copy from any TDB internal BindingTDB to a Binding that
     *  does not have any connection to the database.   
     */
    static class QueryIteratorMaterializeBinding extends QueryIteratorWrapper
    {
        public QueryIteratorMaterializeBinding(QueryIterator qIter)
        {
            super(qIter) ;
        }
        
        @Override
        protected Binding moveToNextBinding()
        { 
            Binding b = super.moveToNextBinding() ;
            b = BindingFactory.materialize(b) ;
            return b ;
        }
    }
    
    // Execution time (needs wiring to ARQ).
    public long getMillis() { return -1 ; }
    
    // ---- Factory
    protected static QueryEngineFactory factory = new QueryEngineFactoryTDB() ;
        
    protected static class QueryEngineFactoryTDB implements QueryEngineFactory
    {
        // If a DatasetGraphTransaction is passed in, we are outside a transaction.
        
        private static boolean isHandledByTDB(DatasetGraph dataset)
        {
            if (dataset instanceof DatasetGraphTDB) return true ;
            if (dataset instanceof DatasetGraphTransaction ) return true ;
            return false ;
        }
        
        protected DatasetGraphTDB dsgToQuery(DatasetGraph dataset)
        {
            if (dataset instanceof DatasetGraphTDB) return (DatasetGraphTDB)dataset ;
            if (dataset instanceof DatasetGraphTransaction) 
                return ((DatasetGraphTransaction)dataset).getDatasetGraphToQuery() ;
            throw new TDBException("Internal inconsistency: trying to execute query on unrecognized kind of DatasetGraph: "+Lib.className(dataset)) ;
        }
        
        @Override
        public boolean accept(Query query, DatasetGraph dataset, Context context) 
        { return isHandledByTDB(dataset) ; }

        @Override
        public Plan create(Query query, DatasetGraph dataset, Binding input, Context context)
        {
            QueryEngineTDB engine = new QueryEngineTDB(query, dsgToQuery(dataset), input, context) ;
            return engine.getPlan() ;
        }
        
        @Override
        public boolean accept(Op op, DatasetGraph dataset, Context context) 
        { return isHandledByTDB(dataset) ; }

        @Override
        public Plan create(Op op, DatasetGraph dataset, Binding binding, Context context)
        {
            QueryEngineTDB engine = new QueryEngineTDB(op, dsgToQuery(dataset), binding, context) ;
            return engine.getPlan() ;
        }
    }
    
//    // By rewrite, not using a general purpose dataset with the right graphs in.
//    private static Op dynamicDatasetOp(Op op,  Context context)
//    {
//        Transform transform = null ;
//    
//        try {
//            @SuppressWarnings("unchecked")
//            Set<Node> defaultGraphs = (Set<Node>)(context.get(SystemTDB.symDatasetDefaultGraphs)) ;
//            @SuppressWarnings("unchecked")
//            Set<Node> namedGraphs = (Set<Node>)(context.get(SystemTDB.symDatasetNamedGraphs)) ;
//            if ( defaultGraphs != null || namedGraphs != null )
//                transform = new TransformDynamicDataset(defaultGraphs, 
//                                                        namedGraphs, 
//                                                        context.isTrue(TDB.symUnionDefaultGraph)) ;
//        } catch (ClassCastException ex)
//        {
//            Log.warn(QueryEngineTDB.class, "Bad dynamic dataset description (ClassCastException)", ex) ;
//            transform = null ;
//            return op ;
//        }
//
//        // Apply dynamic dataset modifications.
//        if ( transform != null )
//            op = Transformer.transform(transform, op) ;
//        return op ;
//    }        
//    
}
