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

import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.sparql.algebra.Algebra ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.engine.Plan ;
import com.hp.hpl.jena.sparql.engine.QueryEngineFactory ;
import com.hp.hpl.jena.sparql.engine.QueryEngineRegistry ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.util.Context ;

/** Same as QueryEngineMain except tranform to quads */
public class QueryEngineMainQuad extends QueryEngineMain
{
    public QueryEngineMainQuad(Op op, DatasetGraph dataset, Binding input, Context context)
    { super(op, dataset, input, context) ; }
    
    public QueryEngineMainQuad(Query query, DatasetGraph dataset, Binding input, Context context)
    { super(query, dataset, input, context) ; }
    
    @Override
    protected Op modifyOp(Op op)
    { 
        op = super.modifyOp(op) ;
        op = Algebra.toQuadForm(op) ;
        return op ;
    }
    
    // -------- Factory
    
    static public QueryEngineFactory getFactory() { return factory ; } 
    static public void register()       { QueryEngineRegistry.addFactory(factory) ; }
    static public void unregister()     { QueryEngineRegistry.removeFactory(factory) ; }

    private static QueryEngineFactory factory = new QueryEngineFactory()
    {
        @Override
        public boolean accept(Query query, DatasetGraph dataset, Context context) 
        { return true ; }

        @Override
        public Plan create(Query query, DatasetGraph dataset, Binding input, Context context)
        {
            QueryEngineMainQuad engine = new QueryEngineMainQuad(query, dataset, input, context) ;
            return engine.getPlan() ;
        }
        
        @Override
        public boolean accept(Op op, DatasetGraph dataset, Context context) 
        { return true ; }

        @Override
        public Plan create(Op op, DatasetGraph dataset, Binding binding, Context context)
        {
            QueryEngineMainQuad engine = new QueryEngineMainQuad(op, dataset, binding, context) ;
            return engine.getPlan() ;
        }
    } ;

}
