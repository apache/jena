/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.path;

import java.util.Collection ;
import java.util.Iterator ;
import java.util.LinkedHashSet ;
import java.util.List ;
import java.util.Set ;

import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.NodeIterator ;
import com.hp.hpl.jena.rdf.model.RDFNode ;
import com.hp.hpl.jena.rdf.model.impl.NodeIteratorImpl ;
import com.hp.hpl.jena.sparql.ARQException ;
import com.hp.hpl.jena.sparql.lib.iterator.Filter ;
import com.hp.hpl.jena.sparql.lib.iterator.Iter ;
import com.hp.hpl.jena.sparql.lib.iterator.Transform ;
import com.hp.hpl.jena.sparql.util.ALog ;
import com.hp.hpl.jena.sparql.util.ModelUtils ;

public class PathEval
{
    static private Logger log = LoggerFactory.getLogger(PathEval.class) ; 
    
    // Graph to Model.
    static NodeIterator convertGraphNodeToRDFNode(final Model model, Iterator<Node> iter)
    {
        Transform<Node, RDFNode> conv = new Transform<Node, RDFNode>(){
            public RDFNode convert(Node obj)
            {
                return ModelUtils.convertGraphNodeToRDFNode(obj, model) ;
            }
        } ;
        Iterator<RDFNode> iterRDF = Iter.map(iter, conv) ;
        return new NodeIteratorImpl(iterRDF, null) ;
    }
    
    // Possible API usages.
    static public NodeIterator walkForwards(final Model model, RDFNode rdfNode, Path path)
    {
        Iterator<Node> iter = eval(model.getGraph(), rdfNode.asNode(), path) ;
        return convertGraphNodeToRDFNode(model, iter) ;
    }
    
    static public NodeIterator walkBackwards(final Model model, RDFNode rdfNode, Path path)
    {
        Iterator<Node> iter = evalInverse(model.getGraph(), rdfNode.asNode(), path) ;
        return convertGraphNodeToRDFNode(model, iter) ;
    }
    
    // LinkedHashSet for predictable order - remove later??
    
    /** Evaluate a path in the forward direction */ 
    static public Iterator<Node> eval(Graph graph, Node node, Path path)
    { 
        if ( node == null  )
            ALog.fatal(PathEval.class, "PathEval.eval applied to a null node") ;
        if ( node.isVariable() )
            ALog.warn(PathEval.class, "PathEval.eval applied to a variable: "+node) ;
        return eval(graph, node, path, true) ;
    }
    
    /** Evaluate a path starting at the end of the path */ 
    static public Iterator<Node> evalInverse(Graph g, Node node, Path path) 
    { return eval(g, node, path, false) ; }

    static private Iterator<Node> eval(Graph graph, Node node, Path path, boolean forward)
    {
        Set<Node> acc = new LinkedHashSet<Node>() ;
        eval(graph, node, path, forward, acc);
        return acc.iterator() ;
    }
    
    static private Iterator<Node> eval(Graph graph, Iterator<Node> input, Path path, boolean forward) 
    {
        Set<Node> acc = new LinkedHashSet<Node>() ;
        
        for ( ; input.hasNext() ; )
        {
            Node node = input.next() ;
            eval(graph, node, path, forward, acc) ;
        }
        return acc.iterator() ;
    }
    
    // ---- Worker ??
    static private void eval(Graph graph, Node node, Path p, boolean forward, Collection<Node> acc)
    {
        PathEvaluator evaluator = new PathEvaluator(graph, node, acc, forward) ;
        p.visit(evaluator) ;
    }
    // ----
    
    private static class PathEvaluator implements PathVisitor
    {

        private final Graph graph ;
        private final Node node ;
        private final Collection<Node> output ;
        private boolean forwardMode ; 

        public PathEvaluator(Graph g, Node n, Collection<Node> output, boolean forward)
        {
            this.graph = g ; 
            this.node = n ;
            this.output = output ;
            this.forwardMode = forward ;
        }

        //@Override
        public void visit(P_Link pathNode)
        {
            Iterator<Node> nodes = doOne(pathNode.getNode()) ;
            fill(nodes) ;
        }
        
        public void visit(P_ReverseLink pathNode)
        {
            forwardMode = ! forwardMode ;
            Iterator<Node> nodes = doOne(pathNode.getNode()) ;
            forwardMode = ! forwardMode ;
            fill(nodes) ;
        }

        //@Override
        public void visit(P_NegPropClass pathNotOneOf)
        {
            List<Node> props = pathNotOneOf.getExcludedNodes()  ;
            if ( props.size() == 0 )
                throw new ARQException("Bad path element: Negative property class found with no elements") ;
            //Iterator<Node> nodes = doOneExclude(pathNotOneOf.getFwdNodes(), pathNotOneOf.getBwdNodes()) ;
            Iterator<Node> nodes = doOneExclude(pathNotOneOf.getExcludedNodes()) ;
            fill(nodes) ;
        }
        
        //@Override
        public void visit(P_Inverse inversePath)
        {
            //boolean b = forwardMode ;
            // Flip direction and evaluate
            forwardMode = ! forwardMode ;
            inversePath.getSubPath().visit(this) ;
            forwardMode = ! forwardMode ;
        }

        //@Override
        public void visit(P_Alt pathAlt)
        {
            // Try both sizes, accumulate into output.
            Iterator<Node> iter = eval(graph, node, pathAlt.getLeft(), forwardMode) ;
            fill(iter) ;
            iter = eval(graph, node, pathAlt.getRight(), forwardMode) ;
            fill(iter) ;
        }

        //@Override
        public void visit(P_Seq pathSeq)
        {
            Path part1 = forwardMode ? pathSeq.getLeft() : pathSeq.getRight() ;
            Path part2 = forwardMode ? pathSeq.getRight() : pathSeq.getLeft() ;
            
            // Feed one side into the other
            Iterator<Node> iter = eval(graph, node, part1, forwardMode) ;
            iter = eval(graph, iter, part2, forwardMode) ;
            fill(iter) ;
        }

        //@Override
        public void visit(P_Mod pathMod)
        {
            if ( pathMod.isZeroOrMore() )
            {
                doZeroOrMore(pathMod.getSubPath()) ;
                return ;
            }
            
            if ( pathMod.isOneOrMore() )
            {
                doOneOrMore(pathMod.getSubPath()) ;
                return ;
            }
            
            if ( pathMod.getMin() == 0 )
                output.add(node) ;

            if ( pathMod.getMax() == 0 )
                return ;
            
            // One step.
            Iterator<Node> iter = eval(graph, node, pathMod.getSubPath(), forwardMode) ;

            // The next step
            long min2 = dec(pathMod.getMin()) ;
            long max2 = dec(pathMod.getMax()) ;
            P_Mod nextPath = new P_Mod(pathMod.getSubPath(), min2, max2) ;
            
//            // Debug.
//            Listx = Iter.toList(iter) ;
//            System.out.println(x) ;
//            iter = x.iterator() ;
            
            // Moved on one step - now go and do it again on a new path
            
            for ( ; iter.hasNext() ; )
            {
                Node n2 = iter.next() ;
                Iterator<Node> iter2 = eval(graph, n2, nextPath, forwardMode) ;
                fill(iter2) ;
            }
            // If no matches, will not call eval and we drop out.
        }
        
        private void fill(Iterator<Node> iter)
        {
            for ( ; iter.hasNext() ; )
                output.add(iter.next()) ;
        }

        private static Transform<Triple, Node> selectSubject = new Transform<Triple, Node>()
        {
            public Node convert(Triple triple)
            { return triple.getSubject() ; }
        } ;

        private static Transform<Triple, Node> selectPredicate = new Transform<Triple, Node>()
        {
            public Node convert(Triple triple)
            { return triple.getPredicate() ; }
        } ;

        private static Transform<Triple, Node> selectObject = new Transform<Triple, Node>()
        {
            public Node convert(Triple triple)
            { return triple.getObject() ; }
        } ;
        
        // --- Where we touch the graph
        private final Iterator<Node> doOne(Node property)
        {
            Iterator<Node> iter2 = null ;
            if ( forwardMode )
            {
                Iter<Triple> iter1 = Iter.iter(graph.find(node, property, Node.ANY)) ;
                iter2 = iter1.map(selectObject) ;
            }
            else
            {
                Iter<Triple> iter1 = Iter.iter(graph.find(Node.ANY, property, node)) ;
                iter2 = iter1.map(selectSubject) ;
            }
            
            return iter2 ;
        }

        private static class FilterExclude implements Filter<Triple>
        {
            private Collection<Node> excludes ;
            public FilterExclude(Collection <Node> excludes) { this.excludes = excludes ; }
            public boolean accept(Triple triple)
            {
                return ! excludes.contains(triple.getPredicate()) ;
            }
        }
        
        private final Iterator<Node> doOneExclude(List<Node> excludedNodes)
        {
            // Forward mode only
            Iter<Triple> iter1 = forwardLinks(node, excludedNodes) ;

            if ( false )
            {
                System.out.println("Node: "+node) ;
                List<Triple> x = iter1.toList() ;
                for ( Triple _t : x )
                    System.out.println("    "+_t) ;
                iter1 = Iter.iter(x) ;
            }

            Iter<Node> r1 = iter1.map(selectObject) ;
            return r1 ;
        }
        
//        private final Iterator<Node> doOneExclude(List<Node> fwdNodes, List<Node> bwdNodes)
//        {
////            if ( forwardMode )
////            { }
////            else
////            {}
//            
//            // FORWARD MODE
//            Iter<Triple> iter1 = forwardLinks(node, fwdNodes) ;
//
//            if ( false )
//            {
//                System.out.println("Node: "+node) ;
//                List<Triple> x = iter1.toList() ;
//                for ( Triple _t : x )
//                    System.out.println("    "+_t) ;
//                iter1 = Iter.iter(x) ;
//            }
//
//            Iter<Node> r1 = iter1.map(selectObject) ; 
//            
//            if ( bwdNodes.size() == 0 )
//                return r1 ;
//            
//            if ( true )
//            {
//                if ( bwdNodes.size() > 0 )
//                    throw new ARQNotImplemented() ;
//            }
//            
//            Iter<Triple> iter2 = backwardLinks(node, bwdNodes) ;
//            Iter<Node> r2 = iter1.map(selectSubject) ;
//            
//            return Iter.concat(r1, r2) ;
//        }
    
        private boolean testConnected(Node x, Node z, List<Node> excludeProperties)
        {
            Iter<Triple> iter1 = Iter.iter(graph.find(x, Node.ANY, z)) ;
            if ( excludeProperties != null )
                iter1 = iter1.filter(new FilterExclude(excludeProperties)) ;
            return iter1.hasNext() ;
        }

        private Iter<Triple> between(Node x, Node z)
        {
            Iter<Triple> iter1 = Iter.iter(graph.find(x, Node.ANY, z)) ;
            return iter1 ;
        }
        
        private Iter<Triple> forwardLinks(Node x, Collection<Node> excludeProperties)
        {
            Iter<Triple> iter1 = Iter.iter(graph.find(x, Node.ANY, Node.ANY)) ;
            if ( excludeProperties != null )
                iter1 = iter1.filter(new FilterExclude(excludeProperties)) ;
            return iter1 ;
        }

        private Iter<Triple> backwardLinks(Node x, Collection<Node> excludeProperties)
        {
            Iter<Triple> iter1 = Iter.iter(graph.find(Node.ANY, Node.ANY, x)) ;
            if ( excludeProperties != null )
                iter1 = iter1.filter(new FilterExclude(excludeProperties)) ;
            return iter1 ;
        }

        private static long dec(long x) { return (x<=0) ? x : x-1 ; }

        private void doOneOrMore(Path path)
        {
            // Do one, then do zero or more for each result.
            Iterator<Node> iter1 = eval(graph, node, path, forwardMode) ;  // ORDER
            // Do zero or more.
            Set<Node> visited = new LinkedHashSet<Node>() ;
            for ( ; iter1.hasNext() ; )
            {
                Node n1 = iter1.next();
                closure(graph, n1, path, visited, forwardMode) ;
            }
            output.addAll(visited) ;
        }

        private void doZeroOrMore(Path path)
        {
            Set<Node> visited = new LinkedHashSet<Node>() ;
            closure(graph, node, path, visited, forwardMode) ;
            output.addAll(visited) ;
        }

        private static void closure(Graph graph, Node node, Path path, Collection<Node> visited, boolean forward)
        {
            if ( visited.contains(node) ) return ;
            visited.add(node) ;
            Iterator<Node> iter = eval(graph, node, path, forward) ;
            for ( ; iter.hasNext() ; )
            {
                Node n2 = iter.next() ;
                closure(graph, n2, path, visited, forward) ;
            }
        }
    }
}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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