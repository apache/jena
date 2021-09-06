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

package org.apache.jena.sparql.engine.main.iterator;

import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.query.QueryExecException ;
import org.apache.jena.riot.out.NodeFmtLib;
import org.apache.jena.sparql.algebra.op.OpService ;
import org.apache.jena.sparql.engine.ExecutionContext ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.engine.http.Service ;
import org.apache.jena.sparql.engine.iterator.QueryIter;
import org.apache.jena.sparql.engine.iterator.QueryIterCommonParent;
import org.apache.jena.sparql.engine.iterator.QueryIterRepeatApply;
import org.apache.jena.sparql.engine.iterator.QueryIterSingleton;
import org.apache.jena.sparql.engine.main.QC ;
import org.apache.jena.sparql.service.ServiceExecution;
import org.apache.jena.sparql.service.ServiceExecutorFactory;
import org.apache.jena.sparql.service.ServiceExecutorRegistry;
import org.apache.jena.sparql.util.Context;


public class QueryIterService extends QueryIterRepeatApply
{
    protected OpService opService ;

    public QueryIterService(QueryIterator input, OpService opService, ExecutionContext context)
    {
        super(input, context) ;
        if ( context.getContext().isFalse(Service.serviceAllowed) )
            throw new QueryExecException("SERVICE not allowed") ;
        this.opService = opService ;
    }


    @Override
    protected QueryIterator nextStage(Binding outerBinding) {
        boolean silent = opService.getSilent();
        ExecutionContext execCxt = getExecContext();
        Context cxt = execCxt.getContext();
        ServiceExecutorRegistry registry = ServiceExecutorRegistry.get(cxt);
        ServiceExecution svcExec = null;
        OpService substitutedOp = (OpService)QC.substitute(opService, outerBinding);

        try {
            // ---- Find handler
            if ( registry != null ) {
                for ( ServiceExecutorFactory factory : registry.getFactories() ) {
                    // Internal consistency check
                    if ( factory == null ) {
                        Log.warn(this, "SERVICE <" + opService.getService().toString() + ">: Null item in custom ServiceExecutionRegistry");
                        continue;
                    }

                    svcExec = factory.createExecutor(substitutedOp, opService, outerBinding, execCxt);
                    if ( svcExec != null )
                        break;
                }
            }

            // ---- Execute
            if ( svcExec == null )
                throw new QueryExecException("No SERVICE handler");
            QueryIterator qIter = svcExec.exec();
            qIter = QueryIter.makeTracked(qIter, getExecContext());
            // Need to put the outerBinding as parent to every binding of the service call.
            // There should be no variables in common because of the OpSubstitute.substitute
            return new QueryIterCommonParent(qIter, outerBinding, getExecContext());
        } catch (RuntimeException ex) {
            if ( silent ) {
                Log.warn(this, "SERVICE " + NodeFmtLib.str(substitutedOp.getService()) + " : " + ex.getMessage());
                // Return the input
                return QueryIterSingleton.create(outerBinding, getExecContext());
            }
            throw ex;
        }
    }
}
