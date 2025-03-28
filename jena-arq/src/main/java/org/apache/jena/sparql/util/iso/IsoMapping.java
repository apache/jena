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

package org.apache.jena.sparql.util.iso;

import org.apache.jena.graph.Node;

/** Record the mapping of a node. This is a one-way linked list. */
/*package*/ record IsoMapping(IsoMapping parent, Node node1, Node node2) {

    static final IsoMapping rootMapping = new IsoMapping(null, null, null);

    boolean mapped(Node node) {
        return map(node) != null;
    }

    Node map(Node node) {
        IsoMapping mapping = this;
        while (mapping != rootMapping) {
            if ( mapping.node1.equals(node) )
                return mapping.node2;
            mapping = mapping.parent;
        }
        return null;
    }

    boolean reverseMapped(Node node) {
        return reverseMap(node) != null;
    }

    Node reverseMap(Node node) {
        IsoMapping mapping = this;
        while (mapping != rootMapping) {
            if ( mapping.node2.equals(node) )
                return mapping.node1;
            mapping = mapping.parent;
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder sbuff = new StringBuilder();
        IsoMapping mapping = this;
        while (mapping != rootMapping) {
            sbuff.append("{" + mapping.node1 + " => " + mapping.node2 + "}");
            mapping = mapping.parent;
        }
        sbuff.append("{}");
        return sbuff.toString();
    }
}