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

package com.hp.hpl.jena.reasoner.test;

import java.util.* ;

import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.shared.JenaException ;

public class Matcher
{
    private static Logger log = LoggerFactory.getLogger("Matcher") ;
    static boolean logging = false ;
    private static Allocator anyizer = new AllocatorAny() ;

    /**
     * Subgraph isomorphism.
     * Like a BGP match except you can only bind to bNodes.
     */
    public static boolean subgraphIsomorphism(Graph subgraph, Graph graph) {
        // BNode subgraph isomorphism.  For small graphs.
        List<Triple> pattern = bnodes2vars(subgraph) ;
        return match(pattern, graph, false).hasNext();
    }
    
    /**
     * Subgraph inferred by .
     * Like a BGP match except you can only bind to bNodes.
     */
    public static boolean subgraphInferred(Graph subgraph, Graph graph) {
        // BNode subgraph isomorphism.  For small graphs.
        List<Triple> pattern = bnodes2vars(subgraph) ;
        return match(pattern, graph, true).hasNext();
    }
    
    /*package*/ static List<Triple> bnodes2vars(Graph graph)
    {
        Map<Node, Node> bnodeMapping = new HashMap<>();
        Allocator allocator = new AllocatorBlankVar() ;
        List<Triple> pattern = remap(bnodeMapping, graph, allocator) ;
        return pattern ;
    }

    private static Iterator<Map<Node, Node>> match(List<Triple> pattern, Graph graph, boolean bindAny) {
        List<Map<Node, Node>> solutions = new ArrayList<>() ;
        solutions.add(new HashMap<Node, Node>()) ;  // Root binding.
        return solve(solutions, pattern, graph, bindAny) ; 
    }
    
    private static Iterator<Map<Node, Node>> solve(List<Map<Node, Node>> solutions , List<Triple> pattern, Graph graph, boolean bindAny)
    {
        log("Solve: %s", pattern) ;
        
        if ( pattern.size() == 0 )
        {
            log("Solve: Result: %s", solutions) ;
            return solutions.iterator() ;
        }
        
        Triple step  = pattern.get(0) ;
        List<Map<Node, Node>> solutions2 = new ArrayList<>() ;
        
        for ( Map<Node, Node> binding : solutions )
        {
            Triple gStep = remap(binding, step, anyizer) ;
            log("Solve: %s => %s", step, gStep) ;
            Iterator<Triple> iter = graph.find(gStep) ;
            while ( iter.hasNext() )
            {
                Triple t = iter.next() ;
                log("Solve: %s -> %s", step, t) ;
                Map<Node, Node> newBinding = bind(step, t, binding, bindAny) ;
                if ( newBinding == null )
                    continue ;
                log("Solve: soln: %s", newBinding) ;
                solutions2.add(newBinding) ;
            }
        }
        
        List<Triple> nextPattern = pattern.subList(1, pattern.size()) ;
        return solve(solutions2, nextPattern, graph, bindAny) ;
    }
    
    private static Map<Node, Node> bind(Triple step, Triple t, Map<Node, Node> bindings, boolean bindAny)
    {
        log("Bind: %s :: %s",step,t) ;
        HashMap<Node, Node> newBinding = new HashMap<>() ;
        newBinding.putAll(bindings) ;
        if ( ! process(newBinding, t.getSubject(), step.getSubject(), bindAny ))
            return null ;
        if ( ! process(newBinding, t.getPredicate(), step.getPredicate(), bindAny ))
            return null ;
        if ( ! process(newBinding, t.getObject(), step.getObject(), bindAny ))
            return null ;
        log("Bind: %s",newBinding) ;
        return newBinding ;
    }

    private static boolean process(Map<Node, Node> results, Node dataNode, Node varNode, boolean bindAny)
    {
        if ( ! varNode.isVariable() )
        {
            if ( ! dataNode.sameValueAs(varNode) )
                throw new JenaException("Internal error in Matcher") ;
            return true ;
        }
        Node x = results.get(varNode) ;
        if ( x != null )
            // Bound already.  Match?
            return dataNode.equals(x) ;
        
        // Isomorphism - must be a bNode.
        // Infered, can be anything.
        if ( !bindAny && ! dataNode.isBlank() )
            return false ;
        
        results.put(varNode, dataNode) ;
        return true ;
    }

    private static List<Triple> remap(Map<Node, Node> bnodeMapping, Graph g, Allocator alloc)
    {
        List<Triple> triples = g.find(Node.ANY,  Node.ANY, Node.ANY).toList() ;
        return remap(bnodeMapping, triples, alloc) ;
    }

    private static List<Triple> remap(Map<Node, Node> bnodeMapping, List<Triple>triples, Allocator alloc)
    {
        List<Triple> pattern = new ArrayList<>() ;
        for ( Triple t : triples )
        {
            Triple t2 = remap(bnodeMapping, t, alloc) ;
            pattern.add(t2) ;
        }
        return pattern ; 
        
    }
    
    private static Triple remap(Map<Node, Node> bnodeMapping, Triple t, Allocator alloc)
    {
        Node s = t.getSubject() ;
        Node p = t.getPredicate() ;
        Node o = t.getObject() ;
        return new Triple(remap(bnodeMapping,s,alloc), remap(bnodeMapping,p,alloc), remap(bnodeMapping,o,alloc) ) ;
    }

    private static Node remap(Map<Node, Node> mapping, Node n, Allocator alloc)
    {
        // caution
        if ( ! n.isBlank() && ! n.isVariable() )
            return n ;
        if ( mapping.containsKey(n) )
            return mapping.get(n) ;
        Node n2 = alloc.allocate() ;
        alloc.update(mapping, n, n2) ;
        return n2 ;
    }
    
    private static void log(String fmt, Object... args)
    {
        if ( logging && log.isInfoEnabled() )
        {
            String x = String.format(fmt, args) ;
            log.info(x) ;
        }
    }
    
    private interface Allocator
    {
        boolean test(Node n) ;
        Node allocate() ;
        void update(Map<Node, Node> mapping, Node inNode, Node allocNode) ;
    }
    
    private static class AllocatorBlankVar implements Allocator {
        int counter = 0 ; 
        @Override
        public Node allocate()
        { return NodeFactory.createVariable("v"+(counter++)) ; }
        @Override
        public boolean test(Node n) { return n.isBlank() ; }
        @Override
        public void update(Map<Node, Node> mapping, Node inNode, Node allocNode) { mapping.put(inNode, allocNode) ; }
    }
    
    private static class AllocatorAny implements Allocator {
        int counter = 0 ; 
        @Override
        public Node allocate()
        { return Node.ANY ; }
        @Override
        public boolean test(Node n) { return n.isVariable() ; }
        @Override
        public void update(Map<Node, Node> mapping, Node inNode, Node allocNode) { }
        
    }
}
