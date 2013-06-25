/**
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

package com.hp.hpl.jena.sparql.engine;

import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.DatasetGraphWrapper ;
import com.hp.hpl.jena.sparql.engine.Plan ;
import com.hp.hpl.jena.sparql.engine.QueryEngineFactory ;
import com.hp.hpl.jena.sparql.engine.QueryEngineRegistry ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.util.Context ;

/** Default processing for a DatasetGraphWrapper - unwrap and repeat */ 
public class QueryEngineFactoryWrapper implements QueryEngineFactory
{
    private static QueryEngineFactory instance = new QueryEngineFactoryWrapper() ;
    public static QueryEngineFactory get() { return instance ; }
    
    @Override
    public boolean accept(Query query, DatasetGraph dsg, Context context) {
        if ( !(  dsg instanceof DatasetGraphWrapper ) )
            return false ;    
        DatasetGraph dsg2 = ((DatasetGraphWrapper)dsg).getWrapped() ;
        return QueryEngineRegistry.findFactory(query, dsg2, context).accept(query, dsg2, context) ;
    }

    @Override
    public Plan create(Query query, DatasetGraph dsg, Binding inputBinding, Context context) {
        if ( !(  dsg instanceof DatasetGraphWrapper ) )
            return null ;    
        DatasetGraph dsg2 = ((DatasetGraphWrapper)dsg).getWrapped() ;
        return QueryEngineRegistry.findFactory(query, dsg2, context).create(query, dsg2, inputBinding, context) ;
    }

    @Override
    public boolean accept(Op op, DatasetGraph dsg, Context context) {
        if ( !(  dsg instanceof DatasetGraphWrapper ) )
            return false ;    
        DatasetGraph dsg2 = ((DatasetGraphWrapper)dsg).getWrapped() ;
        return QueryEngineRegistry.findFactory(op, dsg2, context).accept(op, dsg2, context) ;
    }

    @Override
    public Plan create(Op op, DatasetGraph dsg, Binding inputBinding, Context context) {
        if ( !(  dsg instanceof DatasetGraphWrapper ) )
            return null ;    
        DatasetGraph dsg2 = ((DatasetGraphWrapper)dsg).getWrapped() ;
        return QueryEngineRegistry.findFactory(op, dsg2, context).create(op, dsg2, inputBinding, context) ;
    }
}

