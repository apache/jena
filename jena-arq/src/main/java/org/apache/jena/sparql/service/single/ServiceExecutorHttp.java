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

package org.apache.jena.sparql.service.single;

import org.apache.jena.atlas.logging.Log;
import org.apache.jena.query.QueryExecException;
import org.apache.jena.riot.out.NodeFmtLib;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.iterator.QueryIter;
import org.apache.jena.sparql.engine.iterator.QueryIterCommonParent;
import org.apache.jena.sparql.engine.iterator.QueryIterSingleton;
import org.apache.jena.sparql.exec.http.Service;
import org.apache.jena.sparql.util.Context;

/** The default HTTP service executor implementation */
public class ServiceExecutorHttp
    implements ServiceExecutor
{
    @Override
    public QueryIterator createExecution(OpService opExecute, OpService opOriginal, Binding binding,
            ExecutionContext execCxt) {

        Context context = execCxt.getContext();
        if ( context.isFalse(Service.httpServiceAllowed) )
            throw new QueryExecException("SERVICE not allowed") ;
        // Old name.
        if ( context.isFalse(Service.serviceAllowed) )
            throw new QueryExecException("SERVICE not allowed") ;

        boolean silent = opExecute.getSilent();

        try {
            QueryIterator qIter = Service.exec(opExecute, context);

            // ---- Execute
            if ( qIter == null )
                throw new QueryExecException("No SERVICE handler");

            qIter = QueryIter.makeTracked(qIter, execCxt);
            // Need to put the outerBinding as parent to every binding of the service call.
            // There should be no variables in common because of the OpSubstitute.substitute
            return new QueryIterCommonParent(qIter, binding, execCxt);
        } catch (RuntimeException ex) {
            if ( silent ) {
                Log.warn(this, "SERVICE " + NodeFmtLib.strTTL(opExecute.getService()) + " : " + ex.getMessage());
                // Return the input
                return QueryIterSingleton.create(binding, execCxt);

            }
            throw ex;
        }
    }
}
