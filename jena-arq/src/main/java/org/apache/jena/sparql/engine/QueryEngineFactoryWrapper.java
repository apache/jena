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

package org.apache.jena.sparql.engine;

import org.apache.jena.query.Query ;
import org.apache.jena.sparql.algebra.Op ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.DatasetGraphWrapper ;
import org.apache.jena.sparql.core.DatasetGraphWrapperView;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.util.Context ;

/** Default processing for a DatasetGraphWrapper - unwrap and repeat */
public class QueryEngineFactoryWrapper implements QueryEngineFactory
{
    private static QueryEngineFactory instance = new QueryEngineFactoryWrapper() ;
    public static QueryEngineFactory get() { return instance ; }

    @Override
    public boolean accept(Query query, DatasetGraph dsg, Context context) {
        // DatasetGraphFilteredView changes the seen contents so we can't unwrap it for query.
        if ( !( dsg instanceof DatasetGraphWrapper dsgw) || dsg instanceof DatasetGraphWrapperView )
            return false ;
        DatasetGraph dsg2 = dsgw.getWrapped() ;
        return QueryEngineRegistry.findFactory(query, dsg2, context).accept(query, dsg2, context) ;
    }

    @Override
    public Plan create(Query query, DatasetGraph dsg, Binding inputBinding, Context context) {
        if ( !( dsg instanceof DatasetGraphWrapper dsgw) || dsg instanceof DatasetGraphWrapperView )
            return null ;
        DatasetGraph dsg2 = dsgw.getWrapped() ;
        return QueryEngineRegistry.findFactory(query, dsg2, context).create(query, dsg2, inputBinding, context) ;
    }

    @Override
    public boolean accept(Op op, DatasetGraph dsg, Context context) {
        if ( !( dsg instanceof DatasetGraphWrapper ) || dsg instanceof DatasetGraphWrapperView )
            return false ;
        DatasetGraph dsg2 = ((DatasetGraphWrapper)dsg).getWrapped() ;
        return QueryEngineRegistry.findFactory(op, dsg2, context).accept(op, dsg2, context) ;
    }

    @Override
    public Plan create(Op op, DatasetGraph dsg, Binding inputBinding, Context context) {
        if ( !( dsg instanceof DatasetGraphWrapper ) || dsg instanceof DatasetGraphWrapperView )
            return null ;
        DatasetGraph dsg2 = ((DatasetGraphWrapper)dsg).getWrapped() ;
        return QueryEngineRegistry.findFactory(op, dsg2, context).create(op, dsg2, inputBinding, context) ;
    }
}

