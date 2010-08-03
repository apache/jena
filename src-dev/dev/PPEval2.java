/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package dev;

import java.util.Collection ;
import java.util.Iterator ;
import java.util.LinkedHashSet ;
import java.util.Set ;

import org.openjena.atlas.iterator.Iter ;
import org.openjena.atlas.iterator.Transform ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.ARQNotImplemented ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.path.* ;

public class PPEval2
{
    static public QueryIterator eval(Graph graph, Node start, Path path, Node finish)
    {
        throw new ARQNotImplemented() ;
        
    }
    
    // To replace PathEval
    // Follows the SPARQL 1.1 algorithm directly.
    
//    // A struct.
//    private class PathEvalState
//    {
//        final Graph graph ;
//        final Node node ;
//        final Collection<Node> output ;
//        boolean forwardMode ;
//        
//        PathEvalState(Graph g, Node n, Collection<Node> output, boolean forward)
//        {
//            this.graph = g ; 
//            this.node = n ;
//            this.output = output ;
//            this.forwardMode = forward ;
//        }
//    }
    
    
    // Recursion
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
    
    static private void eval(Graph graph, Node node, Path p, boolean forward, Collection<Node> acc)
    {
        // Create another evaluator for the next step.
        PathEvaluator evaluator = new PathEvaluator(graph, acc, node, forward) ;
        p.visit(evaluator) ;
    }
    
    // This is the dispatcher
    // It can evaluate in forward or backwards mode but not with both end grounded.
    private static class PathEvaluator implements PathVisitor
    {
        
        private final Graph graph ;                 // Fixed input.
        private boolean forwardMode ;               // Process state
        private final Collection<Node> output ;     // Result accumulator
        private Node node ;
        
        public PathEvaluator(Graph g, Collection<Node> output, Node startNode, boolean forwardMode)
        {
            this.graph = g ; 
            this.output = output ;
            this.forwardMode = forwardMode ;
            this.node = startNode ;
        }
        
        public void visit(P_Link pathNode)
        {
            Iterator<Node> nodes = doOne(graph, node, pathNode.getNode(), forwardMode) ;
            fill(nodes) ;
        }
        

        // Reverse link in P_???
        // Not !(...)
        public void visit(P_ReverseLink pathNode)
        {
            forwardMode = ! forwardMode ;
            Iterator<Node> nodes = null ; 
            forwardMode = ! forwardMode ;
            fill(nodes) ;
        }
        
        public void visit(P_NegPropSet pathNotOneOf)
        {}
        
        public void visit(P_Alt pathAlt)
        {}
        
        public void visit(P_Seq pathSeq)
        {
            Path part1 = forwardMode ? pathSeq.getLeft() : pathSeq.getRight() ;
            Path part2 = forwardMode ? pathSeq.getRight() : pathSeq.getLeft() ;
            
            // Feed one side into the other
            Iterator<Node> iter = null ; //eval(node, part1, forwardMode) ;
            iter = eval(graph, iter, part2, forwardMode) ;
            fill(iter) ;
        }
        
        public void visit(P_Mod pathMod)
        {}
        
        public void visit(P_Inverse inversePath)
        {}
        
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
        
        private void fill(Iterator<Node> iter)
        {
            for ( ; iter.hasNext() ; )
                output.add(iter.next()) ;
        }
        
        // --- This is where we touch the graph
        // Contains test.
        
        private static final boolean reaches(Graph graph, Node node, Node property, Node endNode, boolean forwardMode)
        {
            throw new ARQNotImplemented("new PathEval : reaches") ;
        }
        
        private static final Iterator<Node> doOne(Graph graph, Node n, Node property, boolean forwardMode)
        {
            Iterator<Node> iter2 = null ;
            if ( forwardMode )
            {
                Iter<Triple> iter1 = Iter.iter(graph.find(n, property, Node.ANY)) ;
                iter2 = iter1.map(selectObject) ;
            }
            else
            {
                Iter<Triple> iter1 = Iter.iter(graph.find(Node.ANY, property, n)) ;
                iter2 = iter1.map(selectSubject) ;
            }
            
            return iter2 ;
        }
        
        // Calculate ?x path{0} ?y 
        private static void evalZeroLengthPath(Collection<Node> acc, Node s, Path path, Node o)
        {
            /*
             * zeropath(?x (path){0} ?y, G) = { μ | μ(?x->iri) and μ(?y->iri) for all IRIs
             *    which are subject or objects of a triple in G }
             *
             * zeropath(iri (path){0} ?z) = { μ | μ(?z->iri) }
             * 
             * zeropath(iri1 (path){0} iri2) matches if iri1 = iri2.             
             */
        }
        
        // Calculate ?x path+ ?y 
        private static void evalArbitraryLengthPath(Collection<Node> acc, Node s, Path path, Node o)
        {
            /*
             * ArbitraryLengthPath(X (path)+ Y) = eval(X, path, Y, {})
             * where
             *   X - a set of nodes, or a variable
             *   Y - a set of nodes, or a variable
             *   S - a set of nodes traversed
             *   R - the set of bindings that are solutions
             * 
             * eval(X:Variable, path, Y, S) =
             *   R = {}
             *   for each subject x in G
             *     R = R + { (X,x) } UNION eval(x, path Y, S) 
             *   result is R
             * 
             * eval(x:RDFTerm, path, Y, S) =
             *     S := S + {x}
             *     T = evalPath({z} path Y)
             *     for solution μ in T:
             *        R := R + {(Y, y)} if Y is a variable
             *        fi
             *     end
             *     S := S \ {x}
             *   result is R
             */
        }
    }
}

/*
 * (c) Copyright 2010 Talis Systems Ltd.
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