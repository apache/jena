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

package org.seaborne.jena.engine.general;

import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.sparql.algebra.Algebra ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.optimize.TransformScopeRename ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.engine.Plan ;
import com.hp.hpl.jena.sparql.engine.QueryEngineFactory ;
import com.hp.hpl.jena.sparql.engine.QueryEngineRegistry ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.main.QC ;
import com.hp.hpl.jena.sparql.engine.main.QueryEngineMain ;
import com.hp.hpl.jena.sparql.util.Context ;

/** Query engine using substitution joins only - a test of the SubstitutionJoin code */ 
public class QueryEngineMain2 extends QueryEngineMain {

    // ---- Wiring
    static public QueryEngineFactory getFactory()   { return factory2 ; } 
    static public void register()                   { QueryEngineRegistry.addFactory(factory2) ; }
    static public void unregister()                 { QueryEngineRegistry.removeFactory(factory2) ; }
    
    protected QueryEngineMain2(Op op, DatasetGraph dataset, Binding input, Context context) {
        super(op, dataset, input, context) ;
    }

    protected QueryEngineMain2(Query query, DatasetGraph dataset, Binding input, Context cxt) {
        super(query, dataset, input, cxt) ;
    }

    @Override
    protected Op modifyOp(Op op)
    { 
        if ( context.isFalse(ARQ.optimization) )
            return minimalModifyOp(op) ;
        return Algebra.optimize(op, super.context) ;
    }
    
    @Override
    protected Op minimalModifyOp(Op op)
    {
        // Must always do this for QueryEngineMain.
        // The optimizer does do this.
        return TransformScopeRename.transform(op) ;
    }
    
    protected static QueryEngineFactory factory2 = new QueryEngineSubstitFactory() ;
    
    protected static class QueryEngineSubstitFactory implements QueryEngineFactory
    {
        @Override
        public boolean accept(Query query, DatasetGraph dataset, Context context) 
        { return true ; }

        @Override
        public Plan create(Query query, DatasetGraph dataset, Binding input, Context context)
        {
            QueryEngineMain engine = new QueryEngineMain2(query, dataset, input, context) ;
            QC.setFactory(context, OpExecutorRowsMain.factoryRowsMain) ;
            return engine.getPlan() ;
        }
        
        @Override
        public boolean accept(Op op, DatasetGraph dataset, Context context) 
        { return true ; }

        @Override
        public Plan create(Op op, DatasetGraph dataset, Binding binding, Context context)
        {
            QueryEngineMain engine = new QueryEngineMain2(op, dataset, binding, context) ;
            QC.setFactory(context, OpExecutorRowsMain.factoryRowsMain) ;
            return engine.getPlan() ;
        }
    }
}
