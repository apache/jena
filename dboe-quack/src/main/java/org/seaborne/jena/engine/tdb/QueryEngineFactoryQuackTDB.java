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

package org.seaborne.jena.engine.tdb;

import org.apache.jena.atlas.lib.Lib ;
import org.seaborne.jena.engine.Quack ;

import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.engine.Plan ;
import com.hp.hpl.jena.sparql.engine.QueryEngineFactory ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.main.OpExecutorFactory ;
import com.hp.hpl.jena.sparql.engine.main.QC ;
import com.hp.hpl.jena.sparql.util.Context ;
import com.hp.hpl.jena.tdb.TDBException ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;
import com.hp.hpl.jena.tdb.transaction.DatasetGraphTransaction ;

public class QueryEngineFactoryQuackTDB implements QueryEngineFactory
{
    @Override
    public boolean accept(Query query, DatasetGraph dataset, Context context) {
        return isHandledByTDB(dataset) ;        }

    @Override
    public Plan create(Query query, DatasetGraph dataset, Binding input, Context context)
    {
        // This is the usual route.
        DatasetGraphTDB dsgtdb = dsgToQuery(dataset) ;
        setup(dsgtdb, context) ;
        QueryEngineQuackTDB engine = new QueryEngineQuackTDB(query, dsgtdb, input, context) ;
        return engine.getPlan() ;
    }
    
    private void setup(DatasetGraphTDB dataset, Context context) {
        OpExecutorFactory opExecfactory = Quack.getOpExecutorFactory(context) ;
        if ( opExecfactory != null )
            QC.setFactory(context, opExecfactory) ;
    }

    @Override
    public boolean accept(Op op, DatasetGraph dataset, Context context) {
        return isHandledByTDB(dataset) ;
    }

    @Override
    public Plan create(Op op, DatasetGraph dataset, Binding input, Context context)
    {
        if ( context == null )
            context = ARQ.getContext().copy() ;
        DatasetGraphTDB dsgtdb = dsgToQuery(dataset) ;
        setup(dsgtdb, context) ;
        // This is the route for op execution, not from a Query.
        QueryEngineQuackTDB engine = new QueryEngineQuackTDB(op, dsgtdb, input, context) ;
        return engine.getPlan() ;
    }

    /* From QueryEngineFactoryTDB */
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
}