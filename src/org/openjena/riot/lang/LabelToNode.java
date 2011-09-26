/**
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

package org.openjena.riot.lang;
import static java.lang.String.format ;

import java.util.HashMap ;
import java.util.Map ;

import org.openjena.riot.SysRIOT ;
import org.openjena.riot.out.NodeFmtLib ;
import org.openjena.riot.out.NodeToLabel ;
import org.openjena.riot.system.MapWithScope ;
import org.openjena.riot.system.SyntaxLabels ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.rdf.model.AnonId ;

/** Allocation Nodes (Bnodes usually) based on the graph and label 
 * Various different policies.
 * See {@link SyntaxLabels#createLabelToNode} for getting a default setup.
 */  

public class LabelToNode extends MapWithScope<String, Node, Node>
{
    /** Allocation from a single scope; just the label matters. */
    public static LabelToNode createScopeByDocument()
    { return new LabelToNode(new SingleScopePolicy(), nodeMaker) ; }

    /** Allocation scoped by graph and label. */
    public static LabelToNode createScopeByGraph()
    { return new LabelToNode(new GraphScopePolicy(), nodeMaker) ; }

    /** Allocation using syntax label; output is unsafe for reading (use 
     * {@link #createUseLabelEncoded()} for output-input).
     * The reverse operation is provided by {@link NodeToLabel#createBNodeByLabelAsGiven()} */
    public static LabelToNode createUseLabelAsGiven()
    { return new LabelToNode(new SingleScopePolicy(), nodeMakerByLabel) ; }
    
    /** Allocation using an encoded syntax label 
     * (i.e. _:B&lt;encoded&gt; format from {@link NodeFmtLib#encodeBNodeLabel}).
     * The reverse operation is provided by {@link NodeToLabel#createBNodeByLabelEncoded()}
     */
    public static LabelToNode createUseLabelEncoded()
    { return new LabelToNode(new SingleScopePolicy(), nodeMakerByLabelEncoded) ; }

    /** Allocation, globally scoped, that uses a incrementing field to create new nodes */  
    public static LabelToNode createIncremental()
    { return new LabelToNode(new SingleScopePolicy(), nodeMakerDeterministic) ; } 
    
    public LabelToNode(ScopePolicy<String, Node, Node> scopePolicy, Allocator<String, Node> allocator)
    {
        super(scopePolicy, allocator) ;
    }

    // ======== Scope Policies
    
    /** Single scope */
    private static class SingleScopePolicy implements ScopePolicy<String, Node, Node>
    { 
        private Map<String, Node> map = new HashMap<String, Node>() ;
        public Map<String, Node> getScope(Node scope) { return map ; }
        public void clear() { map.clear(); }
    }
    
    /** One scope for labels per graph */
    private static class GraphScopePolicy  implements ScopePolicy<String, Node, Node>
    { 
        private Map<String, Node> dftMap = new HashMap<String, Node>() ;
        private Map<Node, Map<String, Node>> map = new HashMap<Node, Map<String, Node>>() ;
        public Map<String, Node> getScope(Node scope)
        {
            if ( scope == null )
                return dftMap ;
            
            Map<String, Node> x = map.get(scope) ;
            if ( x == null )
            {
                x = new HashMap<String, Node>() ;
                map.put(scope, x) ;
            }
            return x ;
        }
        public void clear() { map.clear(); }
    }

    // ======== Node Allocators 
    
    private static Allocator<String, Node> nodeMaker = new Allocator<String, Node>()
    {
        public Node create(String label)
        { return Node.createAnon() ; }

        public void reset()     {}
    } ;

    private static Allocator<String, Node> nodeMakerDeterministic = new Allocator<String, Node>()
    {
        private long counter = 0 ;

        public Node create(String label)
        {
            String $ = format("B0x%04X", ++counter) ;
            return Node.createAnon(new AnonId($)) ;
        }

        public void reset()     {}
    } ;
    
    private static Allocator<String, Node> nodeMakerByLabel = new Allocator<String, Node>()
    {
        private long counter = 0 ;
        
        public Node create(String label)
        {
            if ( label == null )
                label = SysRIOT.BNodeGenIdPrefix+counter++ ;
            return Node.createAnon(new AnonId(label)) ;
        }

        public void reset()     {}
    } ;
    
    private static Allocator<String, Node> nodeMakerByLabelEncoded = new Allocator<String, Node>()
    {
        private long counter = 0 ;
        
        public Node create(String label)
        {
            if ( label == null )
                label = SysRIOT.BNodeGenIdPrefix+counter++ ;
            return Node.createAnon(new AnonId(NodeFmtLib.decodeBNodeLabel(label))) ;
        }

        public void reset()     {}
    } ;
}
