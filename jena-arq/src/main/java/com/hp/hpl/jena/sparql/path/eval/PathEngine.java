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

import java.util.ArrayList ;
import java.util.Collection ;
import java.util.Iterator ;
import java.util.List ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.iterator.Transform ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterRoot ;
import com.hp.hpl.jena.sparql.path.P_NegPropSet ;
import com.hp.hpl.jena.sparql.path.Path ;
import com.hp.hpl.jena.sparql.path.eval.PathEvaluator.FilterExclude ;
import com.hp.hpl.jena.sparql.pfunction.* ;
import com.hp.hpl.jena.sparql.util.Context ;
import com.hp.hpl.jena.sparql.util.graph.GraphContainerUtils ;
import com.hp.hpl.jena.sparql.util.graph.GraphList ;
import com.hp.hpl.jena.sparql.vocabulary.ListPFunction ;
import com.hp.hpl.jena.vocabulary.RDFS ;

abstract public class PathEngine
{
    private final boolean doingRDFSmember ;
    private final boolean doingListMember ;
    private final Graph   graph ;
    private final Context context ;
    private final PropertyFunctionRegistry registry ; 

    protected PathEngine(Graph graph, Context context) {
        boolean doingRDFSmember$ = false ;
        boolean doingListMember$ = false ;
        PropertyFunctionRegistry registry$ = null ;

        if ( context == null || context.isTrueOrUndef(ARQ.propertyFunctions) ) {
            registry$ = PropertyFunctionRegistry.chooseRegistry(context) ;
            if ( registry$ != null ) {
                doingRDFSmember$ = ( registry$.get(RDFSmember.getURI()) != null ) ;
                doingRDFSmember$ = ( registry$.get(ListMember.getURI()) != null ) ;
            }
        }
        
        this.registry = registry$ ;
        this.doingRDFSmember = doingRDFSmember$ ;
        this.doingListMember = doingListMember$ ;
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

    private static Binding binding = BindingFactory.binding() ;
    private static Node RDFSmember = RDFS.Nodes.member ;
    private static Node ListMember = ListPFunction.nListMember ;
    
    private /*static*/ Iterator<Triple> graphFind(Graph graph, Node s, Node p, Node o, Context context) {
        // This is the only place this is called.
        // It means we can add property functions here.

        // Fast-path common cases.
        if ( doingRDFSmember && RDFSmember.equals(p) )
            return GraphContainerUtils.rdfsMember(graph, s, o) ;
        if ( doingListMember && ListMember.equals(p) )
            return GraphList.listMember(graph, s, o) ;
        // Potentially just allow the cases above.
        //return graph.find(s, p, o) ;
        return graphFind2(graph, s, p, o, context) ;
    }

    /* As general as possible property function inclusion */ 
    private Iterator<Triple> graphFind2(Graph graph, Node s, Node p, Node o, Context context) {
        // Not all property functions make sense in property path
        // For example, ones taking list arguments only make sense at
        // the start or finish, and then only in simple paths
        // (e.g. ?x .../propertyFunction ?z) 
        // which would have been packaged by the optimizer.
        if ( p != null && p.isURI() && registry != null ) {
            PropertyFunctionFactory f = registry.get(p.getURI()) ;
            if ( f != null )
                return graphFindWorker(graph, s, f, p, o, context) ;
        }

        return graph.find(s, p, o) ;
    }

    private Iterator<Triple> graphFindWorker(Graph graph, Node s, PropertyFunctionFactory f, Node p, Node o, Context context) {
        // Expensive?
        PropertyFunction pf = f.create(p.getURI()) ;
        PropFuncArg sv = arg(s, "S") ;
        PropFuncArg ov = arg(o, "O") ;
        QueryIterator r = QueryIterRoot.create(new ExecutionContext(context, graph, null, null)) ;
        QueryIterator qIter = pf.exec(r, sv, p, ov, new ExecutionContext(ARQ.getContext(), graph, null, null)) ;
        if ( ! qIter.hasNext() )
            return Iter.nullIterator() ;
        List<Triple> array = new ArrayList<>() ;
        for ( ; qIter.hasNext() ; ) {
            Binding b = qIter.next() ;
            Node st = value(sv, b) ;
            Node ot = value(ov, b) ;
            array.add(Triple.create(st, p, ot)) ;
        }
        // Materialise so the inner QueryIterators are used up. 
        return array.iterator() ; 
    }

    private static PropFuncArg arg(Node x, String name) {
        if ( x == null || Node.ANY.equals(x) ) 
        { return new PropFuncArg(Var.alloc(name)) ; }
        return new PropFuncArg(x) ;
    }

    private static Node value(PropFuncArg arg, Binding b) {
        Node x = arg.getArg() ;
        if ( !Var.isVar(x) )
            return x ;
        return b.get(Var.alloc(x)) ;
    }
}
