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

package org.apache.jena.shacl;

import org.apache.jena.graph.Graph;

/** Operations to work on graph with SHACL.
 *
 * Example: update with possible exception:
 * <pre>
 *      Shapes shapes = ...
 *      try {
 *        GraphValidation.updateEx(shapes, graph, ()->{
 *            ... application code ...
 *        });
 *     } catch (ShaclValidationException ex) {
 *           ShLib.printReport(ex.getReport());
 *     }
 * </pre>
 * If validation fails, the transaction is aborted.
 *
 * Example: update with report:
 * <pre>
 *     Shapes shapes = ...
 *     ValidationReport report = GraphValidation.updateEx(shapes, graph, ()->{
 *         ... application code ...
 *     });
 *     if ( ! report.conforms() ) {
 *        // Update aborted.
 *        ShLib.printReport(report);
 *     }
 * </pre>
 * Example: update regardless and then report:
 * <pre>
 *     Shapes shapes = ...
 *     ... graph changes ...
 *     ValidationReport report = ShaclValidation.validate(shapes, graph);
 *     if ( ! report.conforms() ) {
 *        // Changes happened.
 *        ShLib.printReport(report);
 *     }
 * </pre>
 */
public class GraphValidation {
    /**
     * Update the graph. This operation aborts and throws a
     * {@link ShaclValidationException} if there are any validation results from
     * shapes (of any severity). If the update results in a valid graph, return
     * without exception (with a {@link ValidationReport} with
     * {@code ValidationReport.conforms} returning true).
     */
    public static ValidationReport updateAndReport(Shapes shapes, Graph data, Runnable update) {
        try {
            return update(shapes, data, update);
        } catch (ShaclValidationException ex) {
            return ex.getReport();
        }
    }

    /**
     * Update the graph. This operation aborts and throws a
     * {@link ShaclValidationException} if there are any validation results from
     * shapes (of any severity). If the update results in a valid graph, return
     * without exception (with a {@link ValidationReport} with
     * {@code ValidationReport.conforms} returning true).
     */
    public static ValidationReport update(Shapes shapes, Graph data, Runnable update) throws ShaclValidationException {
        return
            data.getTransactionHandler().calculate(()->{
                update.run();
                ValidationReport report = ShaclValidator.get().validate(shapes, data);
                if ( report.conforms() )
                    return report;
                // Causes abort.
                throw new ShaclValidationException(report);
            });
    }
}
