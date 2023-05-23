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
package org.apache.jena.mem.graph.helper;

import org.apache.jena.JenaVersion;

/**
 * Context for the benchmarks which contains the graph implementation and the Jena version.
 */
public class Context {

    public GraphClass getGraphClass() {
        return graphClass;
    }

    public JenaVersion getJenaVersion() {
        return jenaVersion;
    }

    public enum GraphClass {
        GraphMem
    }

    private final GraphClass graphClass;
    private final JenaVersion jenaVersion;


    public Context(String graphImplementation) {
        switch (graphImplementation) {
            case "GraphMem (current)":
                this.graphClass = GraphClass.GraphMem;
                this.jenaVersion = JenaVersion.CURRENT;
                break;
            case "GraphMem (Jena 4.8.0)":
                this.graphClass = GraphClass.GraphMem;
                this.jenaVersion = JenaVersion.JENA_4_8_0;
                break;
            default:
                throw new IllegalArgumentException("Unknown graph implementation: " + graphImplementation);
        }
    }


}
