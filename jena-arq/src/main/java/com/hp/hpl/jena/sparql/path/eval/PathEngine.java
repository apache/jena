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

package com.hp.hpl.jena.sparql.path.eval;

import java.util.Collection ;
import java.util.Iterator ;
import java.util.List ;

import org.openjena.atlas.iterator.Iter ;
import org.openjena.atlas.iterator.Transform ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.path.P_NegPropSet ;
import com.hp.hpl.jena.sparql.path.Path ;
import com.hp.hpl.jena.sparql.path.eval.PathEvaluator.FilterExclude ;

abstract public class PathEngine {

    protected final Iter<Node> eval(Graph graph, Path path, Node node)
    {
        return PathEval.eval$(graph, node, path, this) ;
    }
    
    protected final void eval(Graph graph, Path path, Node node, Collection<Node> output)
    {
        PathEval.eval$(graph, node, path, this, output) ;
    }
    
    protected abstract void flipDirection() ;
    protected abstract boolean direction() ;
    
    protected abstract Collection<Node> collector() ;
    
    //    protected abstract void doZero(Path pathStep, Node node, Collection<Node> output) ;
    //    protected abstract void doOne(Path pathStep, Node node, Collection<Node> output) ;
    
    // --- Where we touch the graph
    // Because it SP? or ?PO, no duplicates occur, so works for both strategies.
    protected final Iterator<Node> doOne(Graph graph, Node node, Node property)
    {
        Iterator<Node> iter2 = null ;
        if ( direction() )
        {
            Iter<Triple> iter1 = Iter.iter(graph.find(node, property, Node.ANY)) ;
            iter2 = iter1.map(PathEngine.selectObject) ;
        }
        else
        {
            Iter<Triple> iter1 = Iter.iter(graph.find(Node.ANY, property, node)) ;
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
    protected void doZeroOrMoreN(Path pathStep, Node node, Collection<Node> output)
    { doZeroOrMore(pathStep, node, output) ; } 

    // path{+} : default implementation
    protected void doOneOrMoreN(Path pathStep, Node node, Collection<Node> output)
    { doOneOrMore(pathStep, node, output) ; } 

    protected abstract void doZero(Path path, Node node, Collection<Node> output) ;
    
    // {N,M} and variations
    
    protected abstract void doFixedLengthPath(Path pathStep, Node node, long fixedLength, Collection<Node> output) ;

    protected abstract void doMultiLengthPath(Path pathStep, Node node, long min, long max, Collection<Node> output) ;
    
    protected final void fill(Iterator<Node> iter, Collection<Node> output)
    {
        for ( ; iter.hasNext() ; )
            output.add(iter.next()) ;
    }
    
    protected static long dec(long x) { return (x<=0) ? x : x-1 ; }
    
    protected static Transform<Triple, Node> selectSubject   = new Transform<Triple, Node>() {
        @Override
        public Node convert(Triple triple)
        {
            return triple.getSubject() ;
        }
    } ;
    protected static Transform<Triple, Node> selectPredicate = new Transform<Triple, Node>() {
        @Override
        public Node convert(Triple triple)
        {
            return triple.getPredicate() ;
        }
    } ;
    protected static Transform<Triple, Node> selectObject    = new Transform<Triple, Node>() {
        @Override
        public Node convert(Triple triple)
        {
            return triple.getObject() ;
        }
    } ;


    protected static Iterator<Node> stepExcludeForwards(Graph graph , Node node , List<Node> excludedNodes )
    {
        Iter<Triple> iter1 = forwardLinks(graph, node, excludedNodes) ;
        Iter<Node> r1 = iter1.map(selectObject) ;
        return r1 ;
    }

    protected static Iterator<Node> stepExcludeBackwards(Graph graph , Node node , List<Node> excludedNodes )
    {
        Iter<Triple> iter1 = backwardLinks(graph, node, excludedNodes) ;
        Iter<Node> r1 = iter1.map(selectSubject) ;
        return r1 ;
    }

    protected static  Iter<Triple> forwardLinks(Graph graph, Node x, Collection<Node> excludeProperties)
    {
        Iter<Triple> iter1 = Iter.iter(graph.find(x, Node.ANY, Node.ANY)) ;
        if ( excludeProperties != null )
            iter1 = iter1.filter(new FilterExclude(excludeProperties)) ;
        return iter1 ;
    }

    protected static  Iter<Triple> backwardLinks(Graph graph, Node x, Collection<Node> excludeProperties)
    {
        Iter<Triple> iter1 = Iter.iter(graph.find(Node.ANY, Node.ANY, x)) ;
        if ( excludeProperties != null )
            iter1 = iter1.filter(new FilterExclude(excludeProperties)) ;
        return iter1 ;
    }
}
    

