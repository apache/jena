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

package org.apache.jena.shacl.validation;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.shacl.ShaclValidator;
import org.apache.jena.shacl.Shapes;
import org.apache.jena.shacl.ValidationReport;

/** A basic implementation of SHACL Validator. */
public class ShaclPlainValidator implements ShaclValidator {

    public ShaclPlainValidator() {}

    /**
     * Parse the shapes from the graph and return a AST object that has all the
     * shapes.
     */
    @Override
    public Shapes parse(Graph shapesGraph) {
        return Shapes.parse(shapesGraph);
    }

    /**
     * Does the data conform to the shapes? This operation only checks whether the
     * data is conformant or not - it does not generate a complete report.
     *
     * @see #validate(Shapes, Graph)
     */
    @Override
    public boolean conforms(Shapes shapes, Graph data) {
        // XXX Fast version of "conforms"
        return validate(shapes, data).conforms();
    }

    /**
     * Does the data conform to the shapes? This operation only checks whether the
     * data is conformant or not - it does not generate a complete report.
     *
     * @see #validate(Graph, Graph)
     */
    @Override
    public boolean conforms(Graph shapesGraph, Graph data) {
        return conforms(parse(shapesGraph), data);
    }

    /**
     * Does the node within the data conform to the shapes? This operation only
     * checks the data is conformant or not - it does not generate a complete
     * report.
     *
     * @see #validate(Shapes, Graph, Node)
     */
    @Override
    public boolean conforms(Shapes shapes, Graph data, Node node) {
        return validate(shapes, data, node).conforms();
    }

    /** Produce a full validation report. */
    @Override
    public ValidationReport validate(Shapes shapes, Graph data) {
        return ValidationProc.plainValidation(shapes, data);
    }

    /** Produce a full validation report for this node in the data. */
    @Override
    public ValidationReport validate(Shapes shapes, Graph data, Node node) {
        return ValidationProc.plainValidationNode(shapes, data, node);
    }

    /** Produce a full validation report. */
    @Override
    public ValidationReport validate(Graph shapesGraph, Graph data) {
        return validate(parse(shapesGraph), data);
    }
}
