/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.riot.out;

import java.util.HashMap ;
import java.util.Map ;

import com.hp.hpl.jena.graph.Node ;

/** Map nodes to string (usually, blank nodes to labels) */ 

public class NodeToLabel
    // extends X2Y<Node, String>
{
    /** Allocation from a single scope; just the label matters. */
    static public NodeToLabel createScopeByDocument()
    { return new NodeToLabel(new SingleScopePolicy(), new AllocatorIncremental()) ; }

    /** Allocation scoped by graph and label. */
    public static NodeToLabel createScopeByGraph() 
    { return new NodeToLabel(new GraphScopePolicy(), new AllocatorIncremental()) ; }

    /** Allocation as per internal label */
    public static NodeToLabel createScopeByLabel() 
    { return new NodeToLabel(new SingleScopePolicy(), new AllocatorBNode()) ; }

    private static NodeToLabel _internal = createScopeByLabel() ;
    public static NodeToLabel labelByInternal() { return _internal ; }  
    
    // ======== Interfaces
    
    private interface ScopePolicy
    {
        Map<Node, String> getScope(Node scope) ;
        void clear() ;
    }
    private interface Allocator<T>
    {
        public T create(Node node) ;
        public void reset() ;
    }
    
    // ======== The Object

    private final ScopePolicy scopePolicy ;
    private final Allocator<String> allocator ;

    private NodeToLabel(ScopePolicy scopePolicy, Allocator<String> allocator)
    {
        this.scopePolicy = scopePolicy ;
        this.allocator = allocator ;
    }
    
    /** Get a node for a label, given the node (for the graph) as scope */
    public String get(Node scope, Node node)
    {
        if ( scope == null )
            ;
        Map<Node, String> map = scopePolicy.getScope(scope) ;
        String str = map.get(node) ;
        if ( str == null )
        {
            str = allocator.create(node) ;
            map.put(node, str) ;
        }
        return str ;
    }
    
    /** Create a label that is guaranteed to be fresh */ 
    public String create() { return allocator.create(null) ; }
    
    public void clear() { scopePolicy.clear() ; allocator.reset() ; }
    
    // ======== Scope Policies
    
    /** Single scope */
    private static class SingleScopePolicy implements ScopePolicy
    { 
        private Map<Node, String> map = new HashMap<Node, String>() ;
        public Map<Node, String> getScope(Node scope) { return map ; }
        public void clear() { map.clear(); }
    }
    
    /** One scope for labels per graph */
    private static class GraphScopePolicy implements ScopePolicy
    { 
        private Map<Node, String> dftMap = new HashMap<Node, String>() ;
        private Map<Node, Map<Node, String>> map = new HashMap<Node, Map<Node, String>>() ;
        public Map<Node, String> getScope(Node scope)
        {
            if ( scope == null )
                return dftMap ;
            
            Map<Node, String> x = map.get(scope) ;
            if ( x == null )
            {
                x = new HashMap<Node, String>() ;
                map.put(scope, x) ;
            }
            return x ;
        }
        public void clear() { map.clear(); }
    }
    
    // ======== Allocators 

//    private static Allocator<String> nodeMaker = new Allocator<String>()
//    {
//        public String create(Node node)
//        { return Node.createAnon() ; }
//
//        public void reset()     {}
//    } ;

    private static class AllocatorIncremental implements Allocator<String>
    {
        private int counter = 0 ;
//        private StringBuilder sb = new StringBuilder(20) ; 

        public String create(Node node)
        {
            return Integer.toString(counter++) ;
//            sb.setLength(0) ;
//            NumberUtils.formatInt(sb, counter) ;
//            ++counter ;
//            return sb.toString() ;
        }

        public void reset()     {}
    } ;
    
    /** Allocate that emits the bNode label (encoded) */
    private static class AllocatorBNode implements Allocator<String>
    {
        private int counter = 0 ;
//        private StringBuilder sb = new StringBuilder(20) ; 

        public String create(Node node)
        {
            if ( node.isBlank() )
                return NodeFmtLib.safeBNodeLabel(node.getBlankNodeLabel()) ;
            
            return Integer.toString(counter++) ;
        }

        public void reset()     {}
    } ;

//    private static class AllocatorDeterministic implements Allocator<String>
//    {
//        public Node create(String label)
//        }
//
//        public void reset()     {}
//    } ;
}

/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */