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

package org.seaborne.dboe.engine.tdb ;

import org.apache.jena.query.Query ;
import org.apache.jena.sparql.algebra.Op ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.engine.QueryEngineFactory ;
import org.apache.jena.sparql.engine.QueryEngineRegistry ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.util.Context ;
import org.apache.jena.tdb.solver.QueryEngineTDB ;
import org.apache.jena.tdb.store.DatasetGraphTDB ;

/**
 * Quack query engine - convenient to have quack specific
 * so that the exact choice of executor can change for the same dataset. 
 */ 
public class QueryEngineQuackTDB extends QueryEngineTDB {
    // Using QueryEngineTDB machinary is convenience.
    // ---- Wiring
    static public QueryEngineFactory getFactory()   { return queryEngineFactory ; } 
    static public void register()                   { QueryEngineRegistry.addFactory(queryEngineFactory) ; }
    static public void unregister()                 { QueryEngineRegistry.removeFactory(queryEngineFactory) ; }
    
    protected QueryEngineQuackTDB(Op op, DatasetGraphTDB dataset, Binding input, Context context) {
        super(op, dataset, input, context) ;
    }

    protected QueryEngineQuackTDB(Query query, DatasetGraphTDB dataset, Binding input, Context cxt) {
        super(query, dataset, input, cxt) ;
    }
    
    @Override
    protected Op modifyOp(Op op) {
        return super.modifyOp(op) ;
    }

    @Override
    public QueryIterator eval(Op op, DatasetGraph dsg, Binding input, Context context) {
        return super.eval(op, dsg, input, context) ;
    }

    protected static QueryEngineFactory queryEngineFactory = new QueryEngineFactoryQuackTDB() ;

}
