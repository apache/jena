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

package com.hp.hpl.jena.sparql.path.eval ;

import java.util.Collection ;
import java.util.Iterator ;
import java.util.List ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.iterator.Transform ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory ;
import com.hp.hpl.jena.sparql.path.P_NegPropSet ;
import com.hp.hpl.jena.sparql.path.Path ;
import com.hp.hpl.jena.sparql.path.eval.PathEvaluator.FilterExclude ;
import com.hp.hpl.jena.sparql.util.Context ;
import com.hp.hpl.jena.sparql.util.graph.GraphContainerUtils ;
import com.hp.hpl.jena.sparql.util.graph.GraphList ;
import com.hp.hpl.jena.sparql.vocabulary.ListPFunction ;
import com.hp.hpl.jena.vocabulary.RDFS ;

abstract public class PathEngine
{
    private final Graph   graph ;
    private final Context context ;

    protected PathEngine(Graph graph, Context context) {
        this.graph = graph ;
        this.context = context ;
    }

    protected final Iter<Node> eval(Path path, Node node) {
        return PathEval.eval$(graph, node, path, this) ;
    }

    protected final void eval(Path path, Node node, Collection<Node> output) {
        PathEval.eval$(graph, node, path, this, output) ;
    }

    // protected final void eval(Path path, Node node, Collection<Node> output)

    protected abstract void flipDirection() ;

    protected abstract boolean direction() ;

    protected abstract Collection<Node> collector() ;

    // protected abstract void doZero(Path pathStep, Node node, Collection<Node>
    // output) ;
    // protected abstract void doOne(Path pathStep, Node node, Collection<Node>
    // output) ;

    // --- Where we touch the graph
    // Because for SP? or ?PO, no duplicates occur, so works for both strategies.
    protected final Iterator<Node> doOne(Node node, Node property) {
        Iterator<Node> iter2 = null ;
        if ( direction() ) {
            Iter<Triple> iter1 = Iter.iter(graphFind(node, property, Node.ANY)) ;
            iter2 = iter1.map(PathEngine.selectObject) ;
        } else {
            Iter<Triple> iter1 = Iter.iter(graphFind(Node.ANY, property, node)) ;
            iter2 = iter1.map(PathEngine.selectSubject) ;
        }

        return iter2 ;
    }

    protected abstract void doSeq(Path pathStepLeft, Path pathStepRight, Node node, Collection<Node> output) ;

    protected abstract void doAlt(Path pathStepLeft, Path pathStepRight, Node node, Collection<Node> output) ;

    // path*
    protected abstract void doZeroOrMore(Path pathStep, Node node, Collection<Node> output) ;

    // path+
    protected abstract void doOneOrMore(Path pathStep, Node node, Collection<Node> output) ;

    // path?
    protected abstract void doZeroOrOne(Path pathStep, Node node, Collection<Node> output) ;

    protected abstract void doNegatedPropertySet(P_NegPropSet pathNotOneOf, Node node, Collection<Node> output) ;

    // path{*} : default implementation
    protected void doZeroOrMoreN(Path pathStep, Node node, Collection<Node> output) {
        doZeroOrMore(pathStep, node, output) ;
    }

    // path{+} : default implementation
    protected void doOneOrMoreN(Path pathStep, Node node, Collection<Node> output) {
        doOneOrMore(pathStep, node, output) ;
    }

    protected abstract void doZero(Path path, Node node, Collection<Node> output) ;

    // {N,M} and variations

    protected abstract void doFixedLengthPath(Path pathStep, Node node, long fixedLength, Collection<Node> output) ;

    protected abstract void doMultiLengthPath(Path pathStep, Node node, long min, long max, Collection<Node> output) ;

    protected final void fill(Iterator<Node> iter, Collection<Node> output) {
        for (; iter.hasNext();)
            output.add(iter.next()) ;
    }

    protected static long dec(long x) {
        return (x <= 0) ? x : x - 1 ;
    }

    protected static Transform<Triple, Node> selectSubject = new Transform<Triple, Node>() {
        @Override
        public Node convert(Triple triple) { return triple.getSubject() ; }
    } ;
    
    protected static Transform<Triple, Node> selectPredicate = new Transform<Triple, Node>() {
        @Override
        public Node convert(Triple triple) { return triple.getPredicate() ; }
    } ;
    
    protected static Transform<Triple, Node> selectObject = new Transform<Triple, Node>() {
        @Override
        public Node convert(Triple triple) { return triple.getObject() ; }
    } ;

    protected Iterator<Node> stepExcludeForwards(Node node, List<Node> excludedNodes) {
        Iter<Triple> iter1 = forwardLinks(node, excludedNodes) ;
        Iter<Node> r1 = iter1.map(selectObject) ;
        return r1 ;
    }

    protected Iterator<Node> stepExcludeBackwards(Node node, List<Node> excludedNodes) {
        Iter<Triple> iter1 = backwardLinks(node, excludedNodes) ;
        Iter<Node> r1 = iter1.map(selectSubject) ;
        return r1 ;
    }

    protected Iter<Triple> forwardLinks(Node x, Collection<Node> excludeProperties) {
        Iter<Triple> iter1 = Iter.iter(graphFind(x, Node.ANY, Node.ANY)) ;
        if ( excludeProperties != null )
            iter1 = iter1.filter(new FilterExclude(excludeProperties)) ;
        return iter1 ;
    }

    protected Iter<Triple> backwardLinks(Node x, Collection<Node> excludeProperties) {
        Iter<Triple> iter1 = Iter.iter(graphFind(Node.ANY, Node.ANY, x)) ;
        if ( excludeProperties != null )
            iter1 = iter1.filter(new FilterExclude(excludeProperties)) ;
        return iter1 ;
    }

    protected Iterator<Triple> graphFind(Node s, Node p, Node o) {
        return graphFind(graph, s, p, o, context) ;
    }

    static Binding binding = BindingFactory.binding() ;
    static Node RDFSmember = RDFS.Nodes.member ;
    
    
    static Node ListMember = ListPFunction.member.asNode() ;
    
    
    private/* package */static Iterator<Triple> graphFind(Graph graph, Node s, Node p, Node o, Context context) {
        // This is the only place this is called.
        // It means we can add property functions here.
        if ( RDFSmember.equals(p) )
            return GraphContainerUtils.rdfsMember(graph, s, o) ;
        if ( ListMember.equals(p) )
            return GraphList.listMember(graph, s, o) ;
        return graph.find(s, p, o) ;
    }
    
    // Not all property functions make sense in property path
    // For example, ones taking list arguments only make sense at
    // the start or finish, and then only in simple paths
    // (e.g. ?x .../propertyFunction ?z) 
    // which would have been packaged by the optimizer.
    
//        if ( p != null && p.isURI() ) {
//            // XXX This is heavy weight
//            PropertyFunctionRegistry reg = PropertyFunctionRegistry.chooseRegistry(context) ;
//            PropertyFunctionFactory f = reg.get(p.getURI()) ;
//            if ( f != null ) { 
//                // Expensive.
//                PropertyFunction pf = f.create(p.getURI()) ;
//                // Must be a PFuncSimple -- no list arguments to the property function.
//                if ( pf instanceof PFuncSimple) {
//                    PFuncSimple pfs = (PFuncSimple)pf ;
//                    Node sv = arg(s, "S") ;
//                    Node ov = arg(o, "O") ;
//                    QueryIterator qIter = pfs.execEvaluated(binding, sv, p, ov, new ExecutionContext(ARQ.getContext(), graph, null, null)) ;
//                    if ( ! qIter.hasNext() )
//                        return Iter.nullIterator() ;
//                    List<Triple> array = new ArrayList<Triple>() ;
//                    for ( ; qIter.hasNext() ; ) {
//                        Binding b = qIter.next() ;
//                        Node st = value(sv, b) ;
//                        Node ot = value(ov, b) ;
//                        array.add(Triple.create(st, p, ot)) ;
//                    }
//                    return array.iterator() ; 
//                }
//            }
//        }
//
//        return graph.find(s, p, o) ;
//    }
//
//    private static Node arg(Node x, String name) {
//        if ( x == null || Node.ANY.equals(x) ) { return Var.alloc(name) ; }
//        return x ;
//    }
//
//    private static Node value(Node x, Binding b) {
//        if ( !Var.isVar(x) )
//            return x ;
//        return b.get(Var.alloc(x)) ;
//    }
}
