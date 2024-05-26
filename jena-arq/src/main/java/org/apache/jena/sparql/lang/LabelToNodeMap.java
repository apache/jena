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

package org.apache.jena.sparql.lang;

import java.util.HashMap ;
import java.util.Map ;
import java.util.Set ;

import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.riot.lang.LabelToNode;
import org.apache.jena.sparql.ARQConstants ;
import org.apache.jena.sparql.core.VarAlloc ;


/** Map from _:* form to bNodes or variables.
 * Used in SPARQL parsing.
 * See also {@link LabelToNode} in RIOT for language parser label to node mapping.
 */
public class LabelToNodeMap
{

    private Map<String, Node> bNodeLabels = new HashMap<>() ;

    // Variables or bNodes?
    // True means variables (query pattern)
    // False means blank node (construct template)
    boolean generateVars = false ;
    VarAlloc allocator = null ;

    /**
     * Create blank nodes, with the same blank node returned for the same label.
     *
     * @return LabelToNodeMap
     * @deprecated Internal use only. Use {@link LabelToNode#createUseLabelAsGiven}
     */
    @Deprecated
    public static LabelToNodeMap createBNodeMap()
    { return new LabelToNodeMap(false, null) ; }

    /**
     * Create variables (Var), starting from zero each time This means that parsing a
     * query string will generate the same variable names for bNode variables each
     * time, making Query.equals and Query.hashCode work.
     *
     * @return LabelToNodeMap
     */
    public static LabelToNodeMap createVarMap()
    { return new LabelToNodeMap(true, new VarAlloc(ARQConstants.allocParserAnonVars) ) ; }

    private LabelToNodeMap(boolean genVars, VarAlloc allocator) {
        generateVars = genVars ;
        this.allocator = allocator ;
    }

    public Set<String> getLabels()  { return bNodeLabels.keySet() ; }

    public Node asNode(String label) {
        Node n = bNodeLabels.get(label) ;
        if ( n != null )
            return n ;
        n = allocNode() ;
        bNodeLabels.put(label, n) ;
        return n ;
    }

    public Node allocNode() {
        if ( generateVars )
            return allocAnonVariable() ;
        return NodeFactory.createBlankNode() ;
    }

    private Node allocAnonVariable() {
        return allocator.allocVar() ;
    }

    public void clear() {
        bNodeLabels.clear();
    }
}
