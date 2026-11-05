/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
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
import org.apache.jena.sparql.engine.iterator.QueryIter1;
import org.apache.jena.sparql.engine.iterator.QueryIterCommonParent;
import org.apache.jena.sparql.engine.iterator.QueryIterSingleton;
import org.apache.jena.sparql.exec.http.Service;

/** The default HTTP service executor implementation */
public class ServiceExecutorHttp
    implements ServiceExecutor
{
    @Override
    public QueryIterator createExecution(OpService opExecute, OpService opOriginal,
                                         Binding binding, ExecutionContext execCxt) {
        boolean silent = opExecute.getSilent();

        try {
            QueryIterator qIter = Service.exec(opExecute, execCxt);

            // ---- Execute
            if ( qIter == null )
                throw new QueryExecException("No SERVICE handler");

            qIter = QueryIter.makeTracked(qIter, execCxt);
            // Need to put the outerBinding as parent to every binding of the service call.
            // There should be no variables in common because of the OpSubstitute.substitute
            qIter = new QueryIterCommonParent(qIter, binding, execCxt);
            return silent ? new QueryIterServiceSilent(qIter, binding, opExecute, execCxt) : qIter;
        } catch (RuntimeException ex) {
            if ( silent ) {
                Log.warn(this, "SERVICE " + NodeFmtLib.strTTL(opExecute.getService()) + " : " + ex.getMessage());
                // Return the input
                return QueryIterSingleton.create(binding, execCxt);

            }
            throw ex;
        }
    }

    private static class QueryIterServiceSilent extends QueryIter1 {
        private final Binding fallback;
        private final OpService service;
        private boolean fallbackPending = false;
        private boolean fallbackReturned = false;

        QueryIterServiceSilent(QueryIterator input, Binding fallback, OpService service, ExecutionContext execCxt) {
            super(input, execCxt);
            this.fallback = fallback;
            this.service = service;
        }

        @Override
        protected boolean hasNextBinding() {
            if ( fallbackPending )
                return true;
            if ( fallbackReturned )
                return false;
            try {
                return getInput().hasNext();
            } catch (RuntimeException ex) {
                handleFailure(ex);
                return true;
            }
        }

        @Override
        protected Binding moveToNextBinding() {
            if ( fallbackPending ) {
                fallbackPending = false;
                fallbackReturned = true;
                return fallback;
            }
            try {
                return getInput().next();
            } catch (RuntimeException ex) {
                handleFailure(ex);
                fallbackPending = false;
                fallbackReturned = true;
                return fallback;
            }
        }

        private void handleFailure(RuntimeException ex) {
            Log.warn(this, "SERVICE " + NodeFmtLib.strTTL(service.getService()) + " : " + ex.getMessage());
            try {
                getInput().close();
            } catch (RuntimeException closeException) {
                ex.addSuppressed(closeException);
            }
            fallbackPending = true;
        }

        @Override
        protected void requestSubCancel() {}

        @Override
        protected void closeSubIterator() {}
    }
}
