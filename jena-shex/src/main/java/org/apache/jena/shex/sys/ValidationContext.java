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

package org.apache.jena.shex.sys;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import org.apache.jena.atlas.lib.InternalErrorException;
import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.shex.*;
import org.apache.jena.shex.expressions.TripleExpression;

/** Context for a validation and collector of the results. */
public class ValidationContext {
    private final ShexSchema shapes;
    private final Graph data;
    private boolean verbose = false;
    private boolean seenReportEntry = false;
    // <data node, shape>
    private Deque<Pair<Node, ShexShape>> inProgress = new ArrayDeque<>();

    private final ShexReport.Builder reportBuilder = ShexReport.create();


    public static ValidationContext create(ValidationContext vCxt) {
        // Fresh ShexReport.Builder
        return new ValidationContext(vCxt.data, vCxt.shapes, vCxt.inProgress);
    }

    public ValidationContext(Graph data, ShexSchema shapes) {
        this(data, shapes, null);
    }

    private ValidationContext(Graph data, ShexSchema shapes ,Deque<Pair<Node, ShexShape>> progress) {
        this.data = data;
        this.shapes = shapes;
        if ( progress != null )
            this.inProgress.addAll(progress);
    }

    public TripleExpression getTripleExpression(Node label) {
        return shapes.getTripleExpression(label);
    }

    public ShexSchema getShapes() {
        return shapes;
    }

    public ShexShape getShape(Node label) {
        return shapes.get(label);
    }

    public Graph getData() {
        return data;
    }

    public void startValidate(ShexShape shape, Node data) {
        inProgress.push(Pair.create(data, shape));
    }

    // Return true if done or in-progress (i.e. don't walk further)
    public boolean cycle(ShexShape shape, Node data) {
        return inProgress.stream().anyMatch(p->p.equalElts(data, shape));
    }

    public void finishValidate(ShexShape shape, Node data) {
        Pair<Node, ShexShape> x = inProgress.pop();
        if ( x.equalElts(data, shape) )
            return;
        throw new InternalErrorException("Eval stack error");
    }

    // In ShEx "satisfies" returns a boolean.
    //    public boolean conforms() { return validationReportBuilder.isEmpty(); }

    public boolean hasEntries() { return reportBuilder.hasEntries(); }

    /** Update other with "this" state */
    public void copyInto(ValidationContext other) {
        reportBuilder.getItems().forEach(item->other.reportEntry(item));
        reportBuilder.getReports().forEach(reportLine->other.shexReport(reportLine));
    }

    private void shexReport(ShexRecord reportLine) {
        reportBuilder.shexReport(reportLine);
    }

    /** Current state. */
    public List<ReportItem> getReportItems() {
        return reportBuilder.getItems();
    }

    /** Current state. */
    public List<ShexRecord> getShexReportItems() {
        return reportBuilder.getReports();
    }

    public void reportEntry(ReportItem item) {
        reportBuilder.addReportItem(item);
    }

    public void shexReport(ShexRecord entry, Node focusNode, ShexStatus result, String reason) {
        reportBuilder.shexReport(entry, focusNode, result, reason);
    }

    public ShexReport generateReport() {
        return reportBuilder.build();
    }
}
