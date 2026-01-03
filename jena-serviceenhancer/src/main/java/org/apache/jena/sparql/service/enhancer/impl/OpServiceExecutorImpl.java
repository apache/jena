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

package org.apache.jena.sparql.service.enhancer.impl;

import org.apache.jena.atlas.logging.Log;
import org.apache.jena.query.QueryExecException;
import org.apache.jena.riot.out.NodeFmtLib;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.iterator.QueryIter;
import org.apache.jena.sparql.engine.iterator.QueryIterSingleton;
import org.apache.jena.sparql.service.bulk.ServiceExecutorBulk;

/** Helper class to simplify executing concrete OpService instances */
public class OpServiceExecutorImpl
    implements OpServiceExecutor
{
    protected OpService originalOp;
    protected ExecutionContext execCxt;
    protected ServiceExecutorBulk delegate;

    public OpServiceExecutorImpl(OpService opService, ExecutionContext execCxt, ServiceExecutorBulk delegate) {
        this.originalOp = opService;
        this.execCxt = execCxt;
        this.delegate = delegate;
    }

    public ExecutionContext getExecCxt() {
        return execCxt;
    }
    
    @Override
    public QueryIterator exec(OpService substitutedOp) {
        QueryIterator result;
        Binding input = BindingFactory.binding();
        boolean silent = originalOp.getSilent();

        try {
            QueryIterator singleton = QueryIterSingleton.create(BindingFactory.root(), execCxt);
            result = delegate.createExecution(substitutedOp, singleton, execCxt);

            // ---- Execute
            if (result == null) {
                throw new QueryExecException("No SERVICE handler");
            }

            result = QueryIter.makeTracked(result, execCxt);
            // Need to put the outerBinding as parent to every binding of the service call.
            // There should be no variables in common because of the OpSubstitute.substitute
            // return new QueryIterCommonParent(qIter, outerBinding, getExecContext());
        } catch (RuntimeException ex) {
            if ( silent ) {
                Log.warn(this, "SERVICE " + NodeFmtLib.strTTL(substitutedOp.getService()) + " : " + ex.getMessage());
                // Return the input
                result = QueryIterSingleton.create(input, execCxt);

            }
            throw ex;
        }

        return result;
    }
}
