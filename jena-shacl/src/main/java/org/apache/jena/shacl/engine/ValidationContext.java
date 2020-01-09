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

package org.apache.jena.shacl.engine;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.shacl.Shapes;
import org.apache.jena.shacl.ValidationReport;
import org.apache.jena.shacl.engine.exec.TripleValidator;
import org.apache.jena.shacl.parser.Constraint;
import org.apache.jena.shacl.parser.Shape;
import org.apache.jena.shacl.validation.ReportItem;
import org.apache.jena.sparql.path.Path;

public class ValidationContext {

    private final ValidationReport.Builder validationReportBuilder = ValidationReport.create();
    private boolean verbose = false;
    private final Shapes shapes;
    private final Graph dataGraph;
    private boolean strict = false;

    public ValidationContext(ValidationContext vCxt) {
        this.shapes = vCxt.shapes;
        this.dataGraph = vCxt.dataGraph;
    }

    public ValidationContext(Shapes shapes, Graph data) {
        this.shapes = shapes;
        this.dataGraph = data;
        validationReportBuilder.addPrefixes(data.getPrefixMapping());
        validationReportBuilder.addPrefixes(shapes.getGraph().getPrefixMapping());
    }

    //public ValidationReport.Builder builder() { return validationReportBuilder; }
    
    public void reportEntry(ReportItem item, Shape shape, Node focusNode, Path path, Constraint constraint) {
        reportEntry(item.getMessage(), shape, focusNode, path, item.getValue(), constraint);
    }

    public void reportEntry(String message, Shape shape, Node focusNode, Path path, Node valueNode, Constraint constraint) {
        validationReportBuilder.addReportEntry(message, shape, focusNode, path, valueNode, constraint);
    }

    public void reportEntry(ReportItem item, TripleValidator validator, Triple triple) {
        validationReportBuilder.addReportEntry(item, validator, triple);
    }

    public ValidationReport generateReport() {
        return validationReportBuilder.build();
    }

    public void setVerbose(boolean value) {
        this.verbose = value;
    }

    public boolean isVerbose() {
        return this.verbose;
    }

    public void setStrict(boolean value) {
        this.strict = value;
    }

    public boolean isStrict() {
        return strict ;
    }
    public Shapes getShapes() {
        return shapes;
    }

    public Graph getShapesGraph() {
        return shapes.getGraph();
    }

    public Graph getDataGraph() {
        return dataGraph;
    }
}
