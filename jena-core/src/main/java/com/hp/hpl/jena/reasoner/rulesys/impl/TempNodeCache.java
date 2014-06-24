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

package com.hp.hpl.jena.reasoner.rulesys.impl;

import java.util.*;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.reasoner.InfGraph;
import com.hp.hpl.jena.util.OneToManyMap;

/**
 * In some rules we need to be able to create temporary property values 
 * which are inferred from ontology constraints but not present in the ground
 * data. This structure is used to manage a pool of such temporary nodes.
 * It is only needed in situations where the data can not be added directly
 * to a deductions graph due to the risk of concurrent access.
 */

// Implementation note: We need to map from a pair of values (instance and prop).
// The current implementation in terms on NodePair will turn over storage during
// lookup. Could replace this with a cascaded hash table.

public class TempNodeCache {

    /** Map from instance+property to value */
    protected OneToManyMap<NodePair, Node> ipMap = new OneToManyMap<>();
    
    /** Map from temp to RDF class, if any */
    protected Map<Node, Node> classMap = new HashMap<>();
    
    /**
     * Cosntructor.
     * @param infgraph Parent inference graph, used to be needed for synchronization, don't think
     * we need it any more 
     */
    public TempNodeCache(InfGraph infgraph) {
    }
    
    /**
     * Retrieve or create a bNode representing an inferred property value.
     * @param instance the base instance node to which the property applies
     * @param prop the property node whose value is being inferred
     * @param pclass the (optional, can be null) class for the inferred value.
     * @return the bNode representing the property value 
     */
    public synchronized Node getTemp(Node instance, Node prop, Node pclass) {
        NodePair ip = new NodePair(instance, prop);
        Node result = null;
        for (Iterator<Node> i = ipMap.getAll(ip); i.hasNext(); ) {
            Node t = i.next();
            if (pclass != null) {
                Object tClass = classMap.get(t);
                if (tClass != null && tClass.equals(pclass)) {
                    result = t;
                    break;
                }
            } else {
                result = t;
                break;
            }
        }
        if (result == null) {
            // No value yet, so create one
            result = NodeFactory.createAnon();
            ipMap.put(ip, result);
            if (pclass != null) {
                classMap.put(result, pclass);
            }
        }
        return result;
    }
    
    /**
     * Inner class used to hold and hash a node pair.
     */
    public static class NodePair {
        /** first node in the pair */
        protected Node first;
        
        /** second node in the pair */
        protected Node second;
        
        /** Constructor */
        public NodePair(Node first, Node second) {
            this.first = first;
            this.second = second;
        }
        
        /**
         * Return the first node in the pair.
         */
        public Node getFirst() {
            return first;
        }
        
        /**
         * Return the second node in the pair.
         */
        public Node getSecond() {
            return second;
        }
        
        /**
         * Equality of each component.
         */
        @Override
        public boolean equals(Object o) {
            return o instanceof NodePair &&
                        first.equals(((NodePair)o).first) && 
                        second.equals(((NodePair)o).second); 
        }
        /**
         * Simple combined hashcode.
         */
        @Override
        public int hashCode() {
            return first.hashCode() ^ (second.hashCode() << 1);
        }

    }
    
}
