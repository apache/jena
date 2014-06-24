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

package org.apache.jena.riot.out;

import java.util.HashMap ;
import java.util.Map ;

import org.apache.jena.atlas.lib.InternalErrorException ;
import org.apache.jena.riot.system.MapWithScope ;
import org.apache.jena.riot.system.SyntaxLabels ;

import com.hp.hpl.jena.graph.Node ;

/** Map nodes to string (usually, blank nodes to labels).
 * See {@link SyntaxLabels#createNodeToLabel} for getting a default setup.
 */

public class NodeToLabel extends MapWithScope<Node, String, Node>
{
    /** Allocation from a single scope; just the label matters. */
    static public NodeToLabel createScopeByDocument()
    { return new NodeToLabel(new SingleScopePolicy(), new AllocatorIncLabel()) ; }

//    /** Allocation scoped by graph and label. */
//    public static NodeToLabel createScopeByGraph() 
//    { return new NodeToLabel(new GraphScopePolicy(), new AllocatorIncLabel()) ; }

    /** Allocation as per internal label, with an encoded safe label. */
    public static NodeToLabel createBNodeByLabelEncoded() 
    { return new NodeToLabel(new SingleScopePolicy(), new AllocatorInternalSafe()) ; }

    /** Allocation as per internal label */
    public static NodeToLabel createBNodeByLabelAsGiven() 
    { return new NodeToLabel(new SingleScopePolicy(), new AllocatorInternalRaw()) ; }

    /** Allocation as per internal label */
    public static NodeToLabel createBNodeByIRI() 
    { return new NodeToLabel(new SingleScopePolicy(), new AllocatorBNodeAsIRI()) ; }

    private static final NodeToLabel _internal = createBNodeByLabelEncoded() ;
    public static NodeToLabel labelByInternal() { return _internal ; }  

    private NodeToLabel(ScopePolicy<Node, String, Node> scopePolicy, Allocator<Node, String, Node> allocator)
    {
        super(scopePolicy, allocator) ;
    }
    // ======== Scope Policies
    
    /** Single scope */
    private static class SingleScopePolicy implements ScopePolicy<Node, String, Node>
    { 
        private Map<Node, String> map = new HashMap<>() ;
        @Override
        public Map<Node, String> getScope(Node scope) { return map ; }
        @Override
        public void clear() { map.clear(); }
    }
    
    /** One scope for labels per graph */
    private static class GraphScopePolicy implements ScopePolicy<Node, String, Node>
    { 
        private Map<Node, String> dftMap = new HashMap<>() ;
        private Map<Node, Map<Node, String>> map = new HashMap<>() ;
        @Override
        public Map<Node, String> getScope(Node scope)
        {
            if ( scope == null )
                return dftMap ;
            
            Map<Node, String> x = map.get(scope) ;
            if ( x == null )
            {
                x = new HashMap<>() ;
                map.put(scope, x) ;
            }
            return x ;
        }
        @Override
        public void clear() { map.clear(); }
    }
    
    // ======== Allocators 

    /** Allocator and some default policies. */
    private abstract static class AllocatorBase implements Allocator<Node, String, Node>
    {
        // abstract to make you think about the policy!
        private long counter = 0 ;
        
        @Override
        public final String alloc(Node scope, Node node)
        {
            if ( node.isURI() )         return labelForURI(node) ;
            if ( node.isLiteral() )     return labelForLiteral(node) ;
            if ( node.isBlank() )       return labelForBlank(node) ;
            if ( node.isVariable() )    return labelForVar(node) ;
            throw new InternalErrorException("Node type not supported: "+node) ; 
        }
        
        @Override
        public final String create()    { return labelCreate() ; }

        // Just return a fresh label.
        protected String labelCreate()
        {
            return Long.toString(counter++) ;
        }

        protected String labelForURI(Node node)
        {
            return "<"+node.getURI()+">" ;
        }

        protected abstract String labelForBlank(Node node) ;

        protected String labelForLiteral(Node node)
        {
            return NodeFmtLib.str(node) ;
        }

        protected String labelForVar(Node node)
        {
            return "?"+node.getName() ;
        }
        @Override
        public void reset()     {}
    }
    
    private static class AllocatorInternalRaw extends AllocatorBase
    {
        @Override
        protected String labelForBlank(Node node)
        {
            return "_:"+node.getBlankNodeLabel() ;
        }
    }
    
    private static class AllocatorInternalSafe extends AllocatorBase
    {
        @Override
        protected String labelForBlank(Node node)
        {
            // NodeFmtLib.safeBNodeLabel adds a "B"
            return "_:"+NodeFmtLib.encodeBNodeLabel(node.getBlankNodeLabel()) ;
        }
    }
    
    private static class AllocatorIncLabel extends AllocatorBase
    {
        private int X = 0 ;
        
        AllocatorIncLabel() {}

        @Override
        protected String labelForBlank(Node node)
        {
            return "_:b"+Integer.toString(X++) ;
        }
    }
    
    private static class AllocatorBNodeAsIRI extends AllocatorBase
    {
        @Override
        protected String labelForBlank(Node node)
        {
            // Needs to be safe?
            //String str = NodeFmtLib.safeBNodeLabel(node.getBlankNodeLabel()) ;
            String str = node.getBlankNodeLabel() ;
            return "<_:"+str+">" ;
        }
    }

}
