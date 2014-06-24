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
import com.hp.hpl.jena.sparql.path.P_Mod ;
import com.hp.hpl.jena.sparql.path.P_NegPropSet ;
import com.hp.hpl.jena.sparql.path.Path ;

/** Path evaluation visitor that provide distinct nodes visited only.
 * This is NOT SPARQL semantics.
 * This class exists for experimentation.
 * It is written to get the right results - not necessarily with maximum efficiency.
 */

final class PathEngine1 extends PathEngine
{
    private boolean forwardMode ;

    public PathEngine1(Graph graph, boolean forward) {
        super(graph, null) ;
        this.forwardMode = forward ;
    }

    // Choose the underlying impl - different choice for debugging.
    @Override
    protected Collection<Node> collector() {
        return new ArrayList<>() ;
        // { return new HashSet<Node>() ; }
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
    protected void doAlt(Path pathStepLeft, Path pathStepRight, Node node, Collection<Node> output) {
        // Must be duplicate supressing.
        Collection<Node> nodes = new HashSet<>() ;
        // Insert directly.
        eval(pathStepLeft, node, nodes) ;
        // Need to reduce/check other side.
        eval(pathStepRight, node, nodes) ;
        output.addAll(nodes) ;
    }

    @Override
    protected void doSeq(Path pathStepLeft, Path pathStepRight, Node node, Collection<Node> output) {
        Path part1 = forwardMode ? pathStepLeft : pathStepRight ;
        Path part2 = forwardMode ? pathStepRight : pathStepLeft ;

        Collection<Node> nodes = collector() ;
        eval(part1, node, nodes) ;
        Collection<Node> nodes2 = new HashSet<>() ;
        for (Node n : nodes)
            eval(part2, n, nodes2) ;
        output.addAll(nodes2) ;
    }

    // Can use .addAll if collector is set-like.
    private static void fillUnique(Iterator<Node> nodes, Collection<Node> acc) {
        for (; nodes.hasNext();) {
            Node n = nodes.next() ;
            if ( !acc.contains(n) )
                acc.add(n) ;
        }
    }

    @Override
    protected void doMultiLengthPath(Path pathStep, Node node, long min1, long max1, Collection<Node> output) {
        // This algrothim can be used for counting {n,m}
        // abstract ALP(=>rename?) , doFixedLength

        if ( min1 == P_Mod.UNSET )
            // {,N}
            min1 = 0 ;

        // do 0-min1 steps, not collecting.
        Collection<Node> collectStartPoints = collector() ;

        if ( min1 > 0 )
            doFixedLengthPath(pathStep, node, min1, collectStartPoints) ;
        else
            collectStartPoints.add(node) ;

        // System.out.println("Start points: "+collectStartPoints) ;

        // {N,M} = {N} then {0,M-N}
        int length = (int)(max1 - min1) ;

        Collection<Node> visited = collector() ;

        for (Node n : collectStartPoints)
            doMultiLengthPath(pathStep, n, length, visited, output) ;
    }

    // {0,length}
    protected void doMultiLengthPath(Path pathStep, Node node, long length, Collection<Node> visited, Collection<Node> output) {
        if ( visited.contains(node) )
            return ;
        visited.add(node) ;
        output.add(node) ;

        if ( length == 0 )
            return ;

        // One step.
        Iterator<Node> iter = eval(pathStep, node) ;
        for (; iter.hasNext();) {
            Node m = iter.next() ;
            if ( visited.contains(m) )
                continue ;
            doMultiLengthPath(pathStep, m, length - 1, visited, output) ;
        }
    }

//    // Do {0,length}
//    private void doFixedLengthPath(Path path, Node node, int length, Collection<Node> visited) {
//        System.out.printf("doModPath (%d) %s\n", length, node) ;
//        ALP1(forwardMode, 0, length, node, path, visited) ;
//    }

    @Override
    protected void doFixedLengthPath(Path pathStep, Node node, long fixedLength, Collection<Node> output) {
        // Special for small?
        // if ( fixedLength < 3 )
        // {}
        Collection<Node> visited = collector() ;

        if ( fixedLength == 0 ) {
            doZero(pathStep, node, output) ;
            return ;
        }
        if ( fixedLength == 1 ) {
            Iter<Node> iter = eval(pathStep, node) ;
            for (Node n : iter) {
                if ( !output.contains(n) )
                    output.add(n) ;
            }
            return ;
        }
        // Loop, not recurse.
        Iter<Node> iter = eval(pathStep, node) ;
        for (Node n : iter)
            doFixedLengthPath(pathStep, n, fixedLength - 1, output) ;
        return ;
    }

    @Override
    protected void doZeroOrMore(Path path, Node node, Collection<Node> output) {
        // Reuse "output"
        Collection<Node> visited = new LinkedList<>() ; // new
                                                            // HashSet<Node>() ;
        ALP1(forwardMode, 0, -1, node, path, visited) ;
        output.addAll(visited) ;
    }

    @Override
    protected void doOneOrMore(Path path, Node node, Collection<Node> output) {
        // Reuse "output"
        Collection<Node> visited = new LinkedList<>() ; // new
                                                            // HashSet<Node>() ;
        // Do one step without including.
        Iter<Node> iter1 = eval(path, node) ;
        for (; iter1.hasNext();) {
            Node n1 = iter1.next() ;
            ALP1(forwardMode, 0, -1, n1, path, visited) ;
        }
        output.addAll(visited) ;
    }

    private void ALP1(boolean forwardMode, int stepCount, int maxStepCount, Node node, Path path, Collection<Node> visited) {
        if ( false )
            System.out.printf("ALP1 node=%s\n   visited=%s\n   output=%s\n", node, visited) ;
        if ( maxStepCount >= 0 && stepCount > maxStepCount )
            return ;
        if ( visited.contains(node) )
            return ;

        if ( !visited.add(node) )
            return ;

        Iter<Node> iter1 = eval(path, node) ;
        // For each step, add to results and recurse.
        for (; iter1.hasNext();) {
            Node n1 = iter1.next() ;
            ALP1(forwardMode, stepCount + 1, maxStepCount, n1, path, visited) ;
        }
        // Different from ALP-counting.
        // visited.remove(node) ;
    }

    @Override
    protected void doNegatedPropertySet(P_NegPropSet pathNotOneOf, Node node, Collection<Node> output) {
        // X !(:a|:b|^:c|^:d) Y = { X !(:a|:b) Y } UNION { Y !(:c|:d) X }
        if ( pathNotOneOf.getFwdNodes().size() > 0 ) {
            Iterator<Node> nodes1 = stepExcludeForwards(node, pathNotOneOf.getFwdNodes()) ;
            fillUnique(nodes1, output) ;
        }
        if ( pathNotOneOf.getBwdNodes().size() > 0 ) {
            Iterator<Node> nodes2 = stepExcludeBackwards(node, pathNotOneOf.getBwdNodes()) ;
            fillUnique(nodes2, output) ;
        }
    }

    @Override
    protected void doZeroOrOne(Path pathStep, Node node, Collection<Node> output) {
        eval(pathStep, node, output) ;
        if ( !output.contains(node) )
            output.add(node) ;
    }

    @Override
    protected void doZero(Path path, Node node, Collection<Node> output) {
        if ( !output.contains(node) )
            output.add(node) ;
    }
}
