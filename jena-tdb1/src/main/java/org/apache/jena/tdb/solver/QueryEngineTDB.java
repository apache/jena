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

package org.apache.jena.tdb.solver;

import org.apache.jena.atlas.lib.Lib ;
import org.apache.jena.query.Query ;
import org.apache.jena.sparql.algebra.Algebra ;
import org.apache.jena.sparql.algebra.Op ;
import org.apache.jena.sparql.algebra.OpLib;
import org.apache.jena.sparql.core.DatasetDescription ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.DynamicDatasets ;
import org.apache.jena.sparql.core.Substitute ;
import org.apache.jena.sparql.engine.Plan ;
import org.apache.jena.sparql.engine.QueryEngineFactory ;
import org.apache.jena.sparql.engine.QueryEngineRegistry ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.engine.main.QueryEngineMain ;
import org.apache.jena.sparql.mgt.Explain ;
import org.apache.jena.sparql.util.Context ;
import org.apache.jena.tdb.TDB ;
import org.apache.jena.tdb.TDBException ;
import org.apache.jena.tdb.store.DatasetGraphTDB ;
import org.apache.jena.tdb.transaction.DatasetGraphTransaction ;

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

    // ---- Object

    protected QueryEngineTDB(Op op, DatasetGraphTDB dataset, Binding input, Context context) {
        super(op, dataset, input, context);
    }

    protected QueryEngineTDB(Query query, DatasetGraphTDB dataset, Binding input, Context cxt) {
        super(query, dataset, input, cxt);
    }

    @Override
    protected DatasetGraph dynamicDataset(DatasetDescription dsDesc, DatasetGraph dataset, boolean unionDftGraph) {
        boolean union = unionDftGraph || context.isTrue(TDB.symUnionDefaultGraph);
        return DynamicDatasets.dynamicDataset(dsDesc, dataset, union ) ;
    }

    // Choose the algebra-level optimizations to invoke.
    @Override
    protected Op modifyOp(Op op)
    {
        op = Substitute.substitute(op, getStartBinding()) ;
        // Optimize (high-level)
        op = super.modifyOp(op) ;

        // Quadification
        // Only apply if not a rewritten DynamicDataset
        if ( ! isDynamicDataset() )
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
        if ( context.isTrue(TDB.symUnionDefaultGraph) && ! isDynamicDataset() )
        {
            op = OpLib.unionDefaultGraphQuads(op) ;
            Explain.explain("REWRITE(Union default graph)", op, context) ;
        }
        QueryIterator results = super.eval(op, dsg, input, context) ;
        return results ;
    }

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
            if (dataset instanceof DatasetGraphTransaction dsgtxn )
                return dsgtxn.getDatasetGraphToQuery() ;
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
}
