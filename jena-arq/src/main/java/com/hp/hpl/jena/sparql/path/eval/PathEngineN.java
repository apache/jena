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

import java.util.* ;

import org.apache.jena.atlas.iterator.Iter ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.path.P_FixedLength ;
import com.hp.hpl.jena.sparql.path.P_Mod ;
import com.hp.hpl.jena.sparql.path.P_NegPropSet ;
import com.hp.hpl.jena.sparql.path.Path ;

/** Path evaluator that produces duplicates.
 * This is NOT SPARQL semantics.
 * This class exists for experimentation.
 * It is written to get the right results - not necessarily with maximum efficiency.
 */

final class PathEngineN extends PathEngine
{
    private boolean forwardMode ;

    public PathEngineN(Graph graph, boolean forward) {
        super(graph, null) ;
        this.forwardMode = forward ;
    }

    @Override
    protected Collection<Node> collector() {
        return new ArrayList<>() ;
    }

    @Override
    protected void flipDirection() {
        forwardMode = !forwardMode ;
    }

    @Override
    protected boolean direction() {
        return forwardMode ;
    }

    @Override
    protected void doNegatedPropertySet(P_NegPropSet pathNotOneOf, Node node, Collection<Node> output) {
        // X !(:a|:b|^:c|^:d) Y = { X !(:a|:b) Y } UNION { Y !(:c|:d) X }
        if ( pathNotOneOf.getFwdNodes().size() > 0 ) {
            Iterator<Node> nodes1 = stepExcludeForwards(node, pathNotOneOf.getFwdNodes()) ;
            fill(nodes1, output) ;
        }
        if ( pathNotOneOf.getBwdNodes().size() > 0 ) {
            Iterator<Node> nodes2 = stepExcludeBackwards(node, pathNotOneOf.getBwdNodes()) ;
            fill(nodes2, output) ;
        }
    }

    @Override
    protected void doAlt(Path pathStepLeft, Path pathStepRight, Node node, Collection<Node> output) {
        // Try both sizes, accumulate into output.
        Iterator<Node> iter = eval(pathStepLeft, node) ;
        fill(iter, output) ;
        iter = eval(pathStepRight, node) ;
        fill(iter, output) ;
    }

    @Override
    protected void doSeq(Path pathStepLeft, Path pathStepRight, Node node, Collection<Node> output) {
        Path part1 = forwardMode ? pathStepLeft : pathStepRight ;
        Path part2 = forwardMode ? pathStepRight : pathStepLeft ;

        // Feed one side into the other
        Iter<Node> iter = eval(part1, node) ;
        for (Node n : iter)
            eval(part2, n, output) ;
    }

    @Override
    protected void doMultiLengthPath(Path pathStep, Node node, long min1, long max1, Collection<Node> output) {
        // Why not always reduce {N,M} to {N} and {0,M-N}
        // Why not iterate, not recurse, for {N,}
        // -- optimizer wil have expanded this so only in unoptimized mode.

        if ( min1 == P_Mod.UNSET )
            // {,N}
            min1 = 0 ;

        // ----------------
        // This code is for p{n,m} and :p{,n} inc :p{0,n}
        // and for :p{N,}

        // if ( max1 == P_Mod.UNSET ) max1 = 0 ;

        if ( min1 == 0 )
            output.add(node) ;

        if ( max1 == 0 )
            return ;

        // The next step
        long min2 = dec(min1) ;
        long max2 = dec(max1) ;

        // TODO Rewrite
        Path p1 = pathStep ;
        Path p2 = new P_Mod(pathStep, min2, max2) ;

        if ( !forwardMode ) {
            // Reverse order. Do the second bit first.
            Path tmp = p1 ;
            p1 = p2 ;
            p2 = tmp ;
            // This forces execution to be in the order that it's written, when
            // working backwards.
            // {N,*} is {*} then {N} backwards != do {N} then do {*} as
            // cardinality of the
            // two operations is different.
        }
        // ****

        // One step.
        Iterator<Node> iter = eval(p1, node) ;

        // Moved on one step
        for (; iter.hasNext();) {
            Node n2 = iter.next() ;
            Iterator<Node> iter2 = eval(p2, n2) ;
            fill(iter2, output) ;
        }

        // If no matches, will not call eval and we drop out.
    }

    @Override
    protected void doFixedLengthPath(Path pathStep, Node node, long fixedLength, Collection<Node> output) {
        if ( fixedLength == 0 ) {
            output.add(node) ;
            return ;
        }
        // P_Mod(path, count, count)
        // One step.
        Iterator<Node> iter = eval(pathStep, node) ;
        // Build a path for all remaining steps.
        long count2 = dec(fixedLength) ;
        P_FixedLength nextPath = new P_FixedLength(pathStep, count2) ;
        // For each element in the first step, do remaining step
        // Accumulate across everything from first step.
        for (; iter.hasNext();) {
            Node n2 = iter.next() ;
            Iterator<Node> iter2 = eval(nextPath, n2) ;
            fill(iter2, output) ;
        }
    }

    @Override
    protected void doZeroOrOne(Path pathStep, Node node, Collection<Node> output) {
        doZero(pathStep, node, output) ;
        doOne(pathStep, node, output) ;
    }

    private void doOne(Path path, Node node, Collection<Node> output) {
        Iterator<Node> iter = eval(path, node) ;
        fill(iter, output) ;
    }

    @Override
    protected void doZero(Path path, Node node, Collection<Node> output) {
        output.add(node) ;
    }

    @Override
    protected void doZeroOrMore(Path path, Node node, Collection<Node> output) {
        Set<Node> visited = new HashSet<>() ;
        ALP(node, path, visited, output) ;
    }

    @Override
    protected void doOneOrMore(Path path, Node node, Collection<Node> output) {
        Set<Node> visited = new HashSet<>() ;
        // Do one step without including.
        Iterator<Node> iter1 = eval(path, node) ;
        for (; iter1.hasNext();) {
            Node n1 = iter1.next() ;
            ALP(n1, path, visited, output) ;
        }
    }

    // This is the worker function for path*
    private void ALP(Node node, Path path, Collection<Node> visited, Collection<Node> output) {
        if ( visited.contains(node) )
            return ;

        // If output is a set, then no point going on if node has been added to
        // the results.
        // If output includes duplicates, more solutions are generated
        // "visited" is nodes on this path (see the matching .remove).
        if ( !output.add(node) )
            return ;

        visited.add(node) ;

        Iterator<Node> iter1 = eval(path, node) ;
        // For each step, add to results and recurse.
        for (; iter1.hasNext();) {
            Node n1 = iter1.next() ;
            ALP(n1, path, visited, output) ;
        }
        visited.remove(node) ;
    }
}
