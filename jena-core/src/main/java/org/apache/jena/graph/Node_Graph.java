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

package org.apache.jena.graph;

/** RDF Graphs as RDF terms.
 * <p>
 * Beware that equality and hashCode are defined by
 * object identity, not graph same-triples nor isomorphism.
 * <p>
 * For experimentation.
 * Otherwise, unsupported.
 */
public class Node_Graph extends Node_Ext<Graph>{

    public Node_Graph(Graph graph) {
        super(graph);
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(get())*31;
    }

    @Override
    public boolean equals(Object o) {
        if ( o == this )
            return true;
        if ( !(o instanceof Node_Graph) )
            return false;
        Node_Graph other = (Node_Graph)o;
        return this.get() == other.get();
    }
}
