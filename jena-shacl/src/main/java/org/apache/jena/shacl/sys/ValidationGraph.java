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

package org.apache.jena.shacl.sys;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.TransactionHandler;
import org.apache.jena.graph.impl.TransactionHandlerBase;
import org.apache.jena.shacl.*;
import org.apache.jena.shacl.validation.ValidationProc;
import org.apache.jena.sparql.graph.GraphWrapper;

/** A graph that performs SHACL validation on a graph during transaction "commit".
 *  (It does not validate on direct addition of date to the graph outside a transaction).
 *  Usage:
 *  <pre>
 *      try {
 *          graph.getTransactionHandler().execute(()->{
 *             ... application code ...
 *             });
 *     } catch (ShaclValidationException ex) {
 *           ShLib.printReport(ex.getReport());
 *     }
 *  </pre>
 *  If validation fails, the transaction is aborted.
 *
 *  <em>Experimental</em>
 */
public class ValidationGraph extends GraphWrapper {
    private final Shapes shapes;
    private final TransactionHandlerValidate transactionHandler;

    public ValidationGraph(Graph graph, Shapes shapes) {
        super(graph);
        this.shapes = shapes;
        this.transactionHandler = new TransactionHandlerValidate(this, get().getTransactionHandler());
    }

    @Override
    public TransactionHandler getTransactionHandler() {
        return transactionHandler;
    }

    /**
     * Update the graph and return a validation report. This operation aborts the
     * update if there are any validation results from shapes (any severity).
     * To determine if the update resulted in a graph that validates,
     * call {@code ValidationReport.conforms}.
     */
    public ValidationReport updateValidateReport(Runnable action) {
        return GraphValidation.updateAndReport(shapes, get(), action);
    }

    /**
     * Update the graph. This operation aborts and throws a
     * {@link ShaclValidationException} if there are any validation results from
     * shapes (of any severity). If the update results in a valid graph, return
     * without exception (with a {@link ValidationReport} with
     * {@code ValidationReport.conforms} returning true).
     */
    public ValidationReport updateValidate(Runnable action) throws ShaclValidationException {
        return GraphValidation.update(shapes, get(), action);
    }

    /**
     * Update the graph and return a validation report.
     * This operation only reports, it does cancel the update
     * if there are any validation results from shapes.
     */
    public ValidationReport updateAndReport(Runnable action) {
        TransactionHandler superTH = get().getTransactionHandler();
        if ( superTH.transactionsSupported() ) {
            superTH.execute(action);
        } else {
            action.run();
        }
        return ShaclValidator.get().validate(shapes, get());
    }

    private static class TransactionHandlerValidate extends TransactionHandlerBase {

        private final TransactionHandler other;
        private ValidationGraph graphValidate;

        TransactionHandlerValidate(ValidationGraph graphValidate, TransactionHandler other) {
            this.graphValidate = graphValidate;
            this.other = other ;
        }

        @Override
        public boolean transactionsSupported() {
            return other.transactionsSupported();
        }

        @Override
        public void begin() {
            other.begin();
        }

        @Override
        public void abort() {
            other.abort();
        }

        @Override
        public void commit() {
            ValidationReport report = validateCommit();
            throw new ShaclValidationException(report) {
                @Override
                public Throwable fillInStackTrace() { return this; }
            };
        }

        // Execute. If there are any validation results of any severity, abort, and return the ValidationReport.
        private ValidationReport validateCommit() {
            ValidationReport report = ValidationProc.simpleValidation(graphValidate.shapes, graphValidate.get(), false);
            if ( report.conforms() ) {
                other.commit();
                return null;
            }
            abort();
            return report;
        }
    }
}

