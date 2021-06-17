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

package org.apache.jena.shex;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.shex.sys.SysShex;

public interface ShexValidator {

    /** Return the current system-wide {@code ShexValidator}. */
    public static ShexValidator get() { return SysShex.get();}

    /** Validate data using a collection of shapes and a shape map */
    public ShexReport validate(Graph graph, ShexSchema shapes, ShexMap shapeMap);

    /** Validate a specific node (the focus), with a specific shape in a set of shapes. */
    public ShexReport validate(Graph graphData, ShexSchema shapes, Node shapeRef, Node focus);

    /** Validate a specific node (the focus), against a shape. */
    public ShexReport validate(Graph graphData, ShexSchema shapes, ShexShape shape, Node focus);

    /** Validate a specific node using the shape map to determine which shapes to use. */
    public ShexReport validate(Graph dataGraph, ShexSchema shapes, ShexMap shapeMap, Node dataNode);
}
