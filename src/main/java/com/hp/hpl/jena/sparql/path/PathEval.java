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

package com.hp.hpl.jena.sparql.path;

import java.util.ArrayList ;
import java.util.Collection ;
import java.util.HashSet ;
import java.util.Iterator ;
import java.util.List ;
import java.util.Set ;

import org.openjena.atlas.io.IndentedWriter ;
import org.openjena.atlas.iterator.Filter ;
import org.openjena.atlas.iterator.Iter ;
import org.openjena.atlas.iterator.Transform ;
import org.openjena.atlas.lib.NotImplemented ;
import org.openjena.atlas.logging.Log ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.NodeIterator ;
import com.hp.hpl.jena.rdf.model.RDFNode ;
import com.hp.hpl.jena.rdf.model.impl.NodeIteratorImpl ;
import com.hp.hpl.jena.sparql.util.ModelUtils ;

public class PathEval
{
    static private Logger log = LoggerFactory.getLogger(PathEval.class) ; 
    
    // Graph to Model.
    static NodeIterator convertGraphNodeToRDFNode(final Model model, Iterator<Node> iter)
    {
        Transform<Node, RDFNode> conv = new Transform<Node, RDFNode>(){
            @Override
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
            Log.fatal(PathEval.class, "PathEval.eval applied to a null node") ;
        if ( node.isVariable() )
            Log.warn(PathEval.class, "PathEval.eval applied to a variable: "+node) ;
        return eval(graph, node, path, true) ;
    }
    
    /** Evaluate a path starting at the end of the path */ 
    static public Iterator<Node> evalInverse(Graph g, Node node, Path path) 
    { 
        if ( node == null  )
            Log.fatal(PathEval.class, "PathEval.eval applied to a null node") ;
        if ( node.isVariable() )
            Log.warn(PathEval.class, "PathEval.eval applied to a variable: "+node) ;
        return eval(g, node, path, false) ; 
    }

    static private Iterator<Node> eval(Graph graph, Node node, Path path, boolean forward)
    {
        Collection<Node> acc = new ArrayList<Node>() ;
        eval$(graph, node, path, forward, acc);
        return acc.iterator() ;
    }
    
    static private Iterator<Node> eval(Graph graph, Iterator<Node> input, Path path, boolean forward) 
    {
        Collection<Node> acc = new ArrayList<Node>() ;
        
        for ( ; input.hasNext() ; )
        {
            Node node = input.next() ;
            eval$(graph, node, path, forward, acc) ;
        }
        return acc.iterator() ;
    }
    
    // ---- Worker ??
    static private void eval$(Graph graph, Node node, Path p, boolean forward, Collection<Node> acc)
    {
        PathEvaluatorN evaluator = new PathEvaluatorN(graph, node, acc, forward) ;
        p.visit(evaluator) ;
    }
    // ----
    /** Path evaluator that produces duplicates.
     *  This is the algorithm in the SPARQL 1.1 spec.  
     * 
     */
    private static class PathEvaluatorN implements PathVisitor
    {
        private final Graph graph ;
        private final Node node ;
        private final Collection<Node> output ;
        private boolean forwardMode ; 

        public PathEvaluatorN(Graph g, Node n, Collection<Node> output, boolean forward)
        {
            this.graph = g ; 
            this.node = n ;
            this.output = output ;
            this.forwardMode = forward ;
        }

        @Override
        public void visit(P_Link pathNode)
        {
            Iterator<Node> nodes = doOne(pathNode.getNode()) ;
            fill(nodes) ;
        }
        
        @Override
        public void visit(P_ReverseLink pathNode)
        {
            forwardMode = ! forwardMode ;
            Iterator<Node> nodes = doOne(pathNode.getNode()) ;
            forwardMode = ! forwardMode ;
            fill(nodes) ;
        }

        @Override
        public void visit(P_NegPropSet pathNotOneOf)
        {
            // X !(:a|:b|^:c|^:d) Y = { X !(:a|:b) Y } UNION { Y !(:c|:d) X }
            if ( pathNotOneOf.getFwdNodes().size() > 0 )
            {
                Iterator<Node> nodes1 = doOneExcludeForwards(pathNotOneOf.getFwdNodes()) ;
                fill(nodes1) ;
            }
            if ( pathNotOneOf.getBwdNodes().size() > 0 )
            {
                Iterator<Node> nodes2 = doOneExcludeBackwards(pathNotOneOf.getBwdNodes()) ;
                fill(nodes2) ;
            }
        }
        
        @Override
        public void visit(P_Inverse inversePath)
        {
            //boolean b = forwardMode ;
            // Flip direction and evaluate
            forwardMode = ! forwardMode ;
            inversePath.getSubPath().visit(this) ;
            forwardMode = ! forwardMode ;
        }

        @Override
        public void visit(P_Alt pathAlt)
        {
            // Try both sizes, accumulate into output.
            Iterator<Node> iter = eval(graph, node, pathAlt.getLeft(), forwardMode) ;
            fill(iter) ;
            iter = eval(graph, node, pathAlt.getRight(), forwardMode) ;
            fill(iter) ;
        }

        @Override
        public void visit(P_Seq pathSeq)
        {
            Path part1 = forwardMode ? pathSeq.getLeft() : pathSeq.getRight() ;
            Path part2 = forwardMode ? pathSeq.getRight() : pathSeq.getLeft() ;
            
            // Feed one side into the other
            Iterator<Node> iter = eval(graph, node, part1, forwardMode) ;
            iter = eval(graph, iter, part2, forwardMode) ;
            fill(iter) ;
        }

        static boolean DEBUG = false ;
        
        @Override
        public void visit(P_Mod pathMod)
        {
            if ( DEBUG ) IndentedWriter.stdout.println("Eval: "+pathMod+" "+node+"("+(forwardMode?"fwd":"bkd")+")") ;
            
            // :p{0,} Y is :p*
            // :p{n,} Y where n > 0  is :p{N}/:p*
            
            // This is the main line code here:
            // :p{,n} is :p{0,n}
            // :p{n,m} is the iteration count down on n and m. 
            
            if ( pathMod.isZeroOrMore() )
            {
                if ( DEBUG ) IndentedWriter.stdout.println("ZeroOrMore") ;
                if ( DEBUG ) IndentedWriter.stdout.println("ZeroOrMore: "+output) ;
                // :p{0,}
                doZeroOrMore(pathMod.getSubPath()) ;
                if ( DEBUG ) IndentedWriter.stdout.println("ZeroOrMore: "+output) ;
                return ;
            }

            long min1 = pathMod.getMin() ;
            long max1 = pathMod.getMax() ;

            // Why not always reduce {N,M} to {N} and {0,M-N}
            // Why not iterate, not recurse, for {N,} 
            // -- optimizer wil have expanded this so only in unoptimized mode.
            
            if ( min1 == P_Mod.UNSET )
                // {,N}
                min1 = 0 ;
            
            // ----------------
            // This code is for p{n,m} and :p{,n} inc :p{0,n}
            // and for :p{N,}
            
            //if ( max1 == P_Mod.UNSET ) max1 = 0 ;
            
            if ( min1 == 0 )
                output.add(node) ;

            if ( max1 == 0 )
                return ;
            
            // The next step
            long min2 = dec(min1) ;
            long max2 = dec(max1) ;

            Path p1 = pathMod.getSubPath() ;   
            Path p2 = new P_Mod(pathMod.getSubPath(), min2, max2) ;
            
            if ( !forwardMode )
            {
                // Reverse order.  Do the second bit first.
                Path tmp = p1 ; 
                p1 = p2 ; p2 = tmp ;
                // This forces execution to be in the order that it's written, when working backwards.
                // {N,*} is  {*} then {N} backwards != do {N} then do {*} as cardinality of the 
                // two operations is different.
            }
            // ****

            // One step.
            Iterator<Node> iter = eval(graph, node, p1, forwardMode) ;

            if ( DEBUG )
            {
                // Debug.
                List<Node> x = Iter.toList(iter) ;
                IndentedWriter.stdout.println("** One step: "+pathMod+" "+node+"("+(forwardMode?"fwd":"bkd")+") ==> "+x) ;
                iter = x.iterator() ;
            }
            
            // Moved on one step
            for ( ; iter.hasNext() ; )
            {
                Node n2 = iter.next() ;
                if ( DEBUG ) IndentedWriter.stdout.incIndent(4) ;
                Iterator<Node> iter2 = eval(graph, n2, p2, forwardMode) ;
                if ( DEBUG ) IndentedWriter.stdout.decIndent(4) ;
                if ( DEBUG )
                {
                    List<Node> x = Iter.toList(iter2) ;
                    IndentedWriter.stdout.println("** Recursive step: "+n2+" "+pathMod+" => "+x) ;
                    iter2 = x.iterator() ;
                }
                fill(iter2) ;
            }
            
            if ( DEBUG ) IndentedWriter.stdout.println("** Output: "+pathMod+" => "+output) ;
            // If no matches, will not call eval and we drop out.
        }
        
        @Override
        public void visit(P_FixedLength pFixedLength)
        {
            if ( pFixedLength.getCount() == 0 )
            {
                output.add(node) ;
                return ;
            }
            // P_Mod(path, count, count)
            // One step.
            Iterator<Node> iter = eval(graph, node, pFixedLength.getSubPath(), forwardMode) ;
            // Build a path for all remaining steps.
            long count2 = dec(pFixedLength.getCount()) ;
            P_FixedLength nextPath = new P_FixedLength(pFixedLength.getSubPath(), count2) ;
            // For each element in the first step, do remaining step
            // Accumulate across everything from first step.  
            for ( ; iter.hasNext() ; )
            {
                Node n2 = iter.next() ;
                Iterator<Node> iter2 = eval(graph, n2, nextPath, forwardMode) ;
                fill(iter2) ;
            }
        }

        @Override
        public void visit(P_Distinct pathDistinct)
        {
            // CRUDE - No optimization.
            Iterator<Node> iter = eval(graph, node, pathDistinct.getSubPath(), forwardMode) ;
            fill(Iter.distinct(iter)) ; 
        }

        @Override
        public void visit(P_Multi pathMulti)
        {
            throw new NotImplemented() ; 
        }

        @Override
        public void visit(P_ZeroOrOne path)
        { 
            doZero(path.getSubPath()) ;
            doOne(path.getSubPath()) ;
        }

        @Override
        public void visit(P_ZeroOrMore path)
        { 
            doZeroOrMore(path.getSubPath()) ;
        }

        @Override
        public void visit(P_OneOrMore path)
        { 
            doOneOrMore(path.getSubPath()) ;
        }

        private void doZero(Path path)
        {
            // Ignores path.
            output.add(node) ;
        }

        private void doOne(Path path)
        {
            Iterator<Node> iter = eval(graph, node, path, forwardMode) ;
            fill(iter) ;
        }

        private void fill(Iterator<Node> iter)
        {
            if ( false )
            {
                // Debug.
                List<Node> x = Iter.toList(iter) ;
                System.out.println("Fill: ==> "+x) ;
                iter = x.iterator() ;
            }
            
            for ( ; iter.hasNext() ; )
                output.add(iter.next()) ;
            
            if ( false )
            {
                // Debug.
                System.out.println("Output: ==> "+output) ;
            }
        }

        private static Transform<Triple, Node> selectSubject = new Transform<Triple, Node>()
        {
            @Override
            public Node convert(Triple triple)
            { return triple.getSubject() ; }
        } ;

        private static Transform<Triple, Node> selectPredicate = new Transform<Triple, Node>()
        {
            @Override
            public Node convert(Triple triple)
            { return triple.getPredicate() ; }
        } ;

        private static Transform<Triple, Node> selectObject = new Transform<Triple, Node>()
        {
            @Override
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
            @Override
            public boolean accept(Triple triple)
            {
                return ! excludes.contains(triple.getPredicate()) ;
            }
        }
        
        private final Iterator<Node> doOneExcludeForwards(List<Node> excludedNodes)
        {
            Iter<Triple> iter1 = forwardLinks(node, excludedNodes) ;
            Iter<Node> r1 = iter1.map(selectObject) ;
            return r1 ;
        }
        
        private final Iterator<Node> doOneExcludeBackwards(List<Node> excludedNodes)
        {
            Iter<Triple> iter1 = backwardLinks(node, excludedNodes) ;
            Iter<Node> r1 = iter1.map(selectSubject) ;
            return r1 ;
        }
        
//        private boolean testConnected(Node x, Node z, List<Node> excludeProperties)
//        {
//            Iter<Triple> iter1 = Iter.iter(graph.find(x, Node.ANY, z)) ;
//            if ( excludeProperties != null )
//                iter1 = iter1.filter(new FilterExclude(excludeProperties)) ;
//            return iter1.hasNext() ;
//        }

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

//        // OLD
//        private void doOneOrMore_OLD(Path path)
//        {
//            // This is the visited node collection - a set is OK
//            Set<Node> visited = new HashSet<Node>() ;
//            doOneOrMore(node, path, visited) ;
//        }
//
//        private void doOneOrMore(Node node, Path path, Set<Node> visited)
//        {
//            if ( visited.contains(node) ) return ;
//            
//            visited.add(node) ;
//            // Do one step.
//            Iterator<Node> iter1 = eval(graph, node, path, forwardMode) ;
//            
//            // For each step, add to results and recurse.
//            for ( ; iter1.hasNext() ; )
//            {
//                Node n1 = iter1.next();
//                output.add(n1) ;
////System.out.println("Add : "+n1+ " (" + output.size()+")") ; System.out.flush() ;                
//                
//                doOneOrMore(n1, path, visited) ;
//            }
//            visited.remove(node) ;
//            
//        }
//        
//        private void doZeroOrMore_OLD(Path path)
//        {
//            doZero(path) ;
//            doOneOrMore(path) ;
//        }
//        // OLD
        
        // NEW
        
        static final boolean trace = false ;
        
        private void doZeroOrMore(Path path)
        {
            if ( trace ) System.out.printf("\nZeroOrMore: %s\n", node) ;
            //Deque<Node> visited = new ArrayDeque<Node>() ;
            Set<Node> visited = new HashSet<Node>() ;
            ALP(node, path, visited) ;
        }
        
        private void doOneOrMore(Path path)
        {
            if ( trace ) System.out.printf("\nOneOrMore: %s\n", node) ;
            //Deque<Node> visited = new ArrayDeque<Node>() ;
            
            Set<Node> visited = new HashSet<Node>() ;
            // Do one step without including.
            Iterator<Node> iter1 = eval(graph, node, path, forwardMode) ;
            for ( ; iter1.hasNext() ; )
            {
                Node n1 = iter1.next();
                if ( trace ) System.out.printf("One from %s\n   visited=%s\n   output=%s\n", n1, visited, output) ;
                ALP(n1, path, visited) ;
            }
        }
        
        // This is the worker function for path*
        private void ALP(Node node, Path path, Collection<Node> visited)
        {
            if ( trace ) System.out.printf("ALP node=%s\n   visited=%s\n   output=%s\n", node, visited, output) ;
            if ( visited.contains(node) ) return ;
            
            // If output is a set, then no point going on if node has been added to the results.
            // If output includes duplicates, more solutions are generated
            // "visited" is nodes on this path (see the matching .remove).
            if ( ! output.add(node) )
                return ;
            
            visited.add(node) ;
            
            Iterator<Node> iter1 = eval(graph, node, path, forwardMode) ;
            // For each step, add to results and recurse.
            for ( ; iter1.hasNext() ; )
            {
                Node n1 = iter1.next();
                ALP(n1, path, visited) ;
            }
            visited.remove(node) ;
        }
    }
}
