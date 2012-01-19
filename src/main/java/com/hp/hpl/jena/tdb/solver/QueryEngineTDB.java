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

import java.util.Set ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.sparql.algebra.Algebra ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.core.DatasetDescription ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.core.Substitute ;
import com.hp.hpl.jena.sparql.engine.Plan ;
import com.hp.hpl.jena.sparql.engine.QueryEngineFactory ;
import com.hp.hpl.jena.sparql.engine.QueryEngineRegistry ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.main.QueryEngineMain ;
import com.hp.hpl.jena.sparql.mgt.Explain ;
import com.hp.hpl.jena.sparql.util.Context ;
import com.hp.hpl.jena.sparql.util.NodeUtils ;
import com.hp.hpl.jena.tdb.TDB ;
import com.hp.hpl.jena.tdb.migrate.A2 ;
import com.hp.hpl.jena.tdb.migrate.DynamicDatasets ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;
import com.hp.hpl.jena.tdb.store.GraphNamedTDB ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;
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
    protected QueryEngineTDB(Op op, DatasetGraph dataset, Binding input, Context context)
    {
        super(op, dataset, input, context) ;
        this.initialInput = input ;
    }
    
    /*
     * rewrite does not work for paths (:p*) because the path evaluator uses the active graph. 
     * Need to alter the active graph in the QueryExecutionContext which is set in
     * QueryEngineMain.
     * Possibility: pass up the preferred active graph (defaulting to the default graph).
     * TEMPORARY FIX: rewrite the dataset to be a general one with the right graphs in it.  
     */
    private static final boolean DynamicDatasetByRewrite = false ;
    private boolean doingDynamicDatasetBySpecialDataset = false ;
    
    protected QueryEngineTDB(Query query, DatasetGraph dataset, Binding input, Context cxt)
    { 
        super(query, dataset, input, cxt) ; 
        // [[DynDS]]
        // Dynamic dataset done as a special dataset.
        
        DatasetDescription dsDesc = DatasetDescription.create(query, context) ;
        
        if ( ! DynamicDatasetByRewrite && dsDesc != null )
        {
            // MIGRATION.
            // 1 - All this to ARQ.
            //     Context slot for real default graph and union graph.
            //     or DatasetGraph.getUnionGraph(), DatasetGraph.getRealDftGraph(),   
            // 2 - Modify OpExecutor.execute(OpGraph) to skip Quad.unionGraph, Quad.defaultGraphIRI
            
            doingDynamicDatasetBySpecialDataset = true ;
            DatasetGraph dsg = super.dataset; 
            dsg = DynamicDatasets.dynamicDataset(dsDesc, dsg, context.isTrue(TDB.symUnionDefaultGraph) ) ;  // Flag for default union graph?
            super.dataset = dsg ;
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
        if ( ! doingDynamicDatasetBySpecialDataset )
            // [[DynDS]]
            // We flipped to general execution-leave alone.
            op = Algebra.toQuadForm(op) ;

        // Could apply dynamic dataset transform before everything else
        // but default merged graphs works on quads. 

//        // [[DynDS]]
//        if ( doingDynamicDatasetBySpecialDataset )
//        {
//            // No action. Already done.
//        }
//        else if ( DynamicDatasetByRewrite )
//            // Do by rewrite - note issues about paths
//            op = dynamicDatasetOp(op, context) ;
        
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
            // doingDynamicDatasetBySpecialDataset => done earlier.
            op = A2.unionDefaultGraphQuads(op) ;
            // Rewrite so that any explicitly named "default graph" is union graph.
            // And set the default graph to be the union graph as well.
            DatasetGraphTDB ds = ((DatasetGraphTDB)dsg).duplicate() ;
            ds.setEffectiveDefaultGraph(new GraphNamedTDB(ds, Quad.unionGraph)) ;
            Explain.explain("REWRITE(Union default graph)", op, context) ;
            dsg = ds ;
        }
        return super.eval(op, dsg, input, context) ;
    }
    
    // Execution time (needs wiring to ARQ).
    public long getMillis() { return -1 ; }
    
    // ---- Factory
    private static QueryEngineFactory factory = new QueryEngineFactoryTDB() ;
        
    private static class QueryEngineFactoryTDB implements QueryEngineFactory
    {
        private static boolean isHandledByTDB(DatasetGraph dataset)
        {
            if (dataset instanceof DatasetGraphTDB) return true ;
            if (dataset instanceof DatasetGraphTransaction ) return true ;
            return false ;
        }
        
        @Override
        public boolean accept(Query query, DatasetGraph dataset, Context context) 
        { return isHandledByTDB(dataset) ; }

        @Override
        public Plan create(Query query, DatasetGraph dataset, Binding input, Context context)
        {
            //DatasetGraphTDB ds = (DatasetGraphTDB)dataset ;
            dynamicDatasetQE(query, context) ;
            QueryEngineTDB engine = new QueryEngineTDB(query, dataset, input, context) ;
            return engine.getPlan() ;
        }
        
        @Override
        public boolean accept(Op op, DatasetGraph dataset, Context context) 
        { return isHandledByTDB(dataset) ; }

        @Override
        public Plan create(Op op, DatasetGraph dataset, Binding binding, Context context)
        {
            QueryEngineTDB engine = new QueryEngineTDB(op, (DatasetGraphTDB)dataset, binding, context) ;
            return engine.getPlan() ;
        }
    } ;
    
    // Write the DatasetDescription into the context.
    private static void dynamicDatasetQE(Query query,  Context context)
    {
        if ( query.hasDatasetDescription() )
        {
            Set<Node> defaultGraphs = NodeUtils.convertToNodes(query.getGraphURIs()) ; 
            Set<Node> namedGraphs = NodeUtils.convertToNodes(query.getNamedGraphURIs()) ;
            
            context.set(SystemTDB.symDatasetDefaultGraphs, defaultGraphs) ;
            context.set(SystemTDB.symDatasetNamedGraphs, namedGraphs) ;
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
