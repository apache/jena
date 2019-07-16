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
import org.apache.jena.graph.Node;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shacl.sys.ShaclSystem;

/** Public API for the SHACL Validation. */
public interface ShaclValidator {

    /** Return the current system-wide {@code ShaclValidator}. */ 
    public static ShaclValidator get() { return ShaclSystem.get();}

    /** Parse the shapes from the graph and return a AST object that has all the shapes. */
    public Shapes parse(Graph shapesGraph);

    /** Parse the shapes from a file or URL return a AST object that has all the shapes. */
    public default Shapes parse(String filenameOrURL) {
        Graph g = RDFDataMgr.loadGraph(filenameOrURL);
        return parse(g);
    }

    /** Does the data conform to the shapes?
     * This operation only checks whether the data is conformant or not - it does not genberate a complete report.
     * @see #validate(Shapes, Graph)
     */
    public boolean conforms(Shapes shapes, Graph data);

    /** Does the data conform to the shapes?
     * This operation only checks whether the data is conformant or not - it does not genberate a complete report.
     * @see #validate(Graph, Graph)
     */
    public default boolean conforms(Graph shapesGraph, Graph data) {
        return conforms(parse(shapesGraph), data);
    }

    /**
     * Does the node within the data conform to the shapes?
     * This operation only checks the data is conformant or not - it does not genberate a complete report.
     * @see #validate(Shapes, Graph, Node)
     */
    public boolean conforms(Shapes shapes, Graph data, Node node);

    /** Produce a full validation report. */
    public ValidationReport validate(Shapes shapes, Graph data);

    /** Produce a full validation report for this node in the data. */
    public ValidationReport validate(Shapes shapes, Graph data, Node node);

    /** Produce a full validation report. */
    public default ValidationReport validate(Graph shapesGraph, Graph data) {
        return validate(parse(shapesGraph), data);
    }
}
