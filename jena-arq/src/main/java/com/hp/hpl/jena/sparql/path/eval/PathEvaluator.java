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

import org.apache.jena.atlas.iterator.Filter ;
import org.apache.jena.atlas.iterator.Iter ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.ARQNotImplemented ;
import com.hp.hpl.jena.sparql.path.* ;
import com.hp.hpl.jena.sparql.sse.writers.WriterPath ;

final class PathEvaluator implements PathVisitor
{
    protected final Graph            graph ;
    protected final Node             node ;
    protected final Collection<Node> output ;
    private PathEngine               engine ;

    protected PathEvaluator(Graph g, Node n, Collection<Node> output, PathEngine engine) {
        this.graph = g ;
        this.node = n ;
        this.output = output ;
        this.engine = engine ;
    }

    protected final void fill(Iterator<Node> iter) {
        for (; iter.hasNext();)
            output.add(iter.next()) ;
    }

    // These operations yield the same results regardless of counting
    // (their subpaths may not).

    @Override
    public void visit(P_Link pathNode) {
        Iterator<Node> nodes = engine.doOne(node, pathNode.getNode()) ;
        fill(nodes) ;
    }

    @Override
    public void visit(P_ReverseLink pathNode) {
        engine.flipDirection() ;
        Iterator<Node> nodes = engine.doOne(node, pathNode.getNode()) ;
        fill(nodes) ;
        engine.flipDirection() ;
    }

    @Override
    public void visit(P_Inverse inversePath) {
        // boolean b = forwardMode ;
        // Flip direction and evaluate
        engine.flipDirection() ;
        engine.eval(inversePath.getSubPath(), node, output) ;
        engine.flipDirection() ;
    }

    @Override
    public void visit(P_NegPropSet pathNotOneOf) {
        engine.doNegatedPropertySet(pathNotOneOf, node, output) ;
    }

    @Override
    public void visit(P_Mod pathMod) {
        // do..Or.. need to take a visited set.

        if ( pathMod.isZeroOrMore() ) {
            // :p{0,}
            engine.doOneOrMoreN(pathMod.getSubPath(), node, output) ;
            return ;
        }
        if ( pathMod.isOneOrMore() ) {
            engine.doOneOrMoreN(pathMod.getSubPath(), node, output) ;
            return ;
        }

        if ( pathMod.isFixedLength() )
            engine.doFixedLengthPath(pathMod.getSubPath(), node, pathMod.getFixedLength(), output) ;
        else
            engine.doMultiLengthPath(pathMod.getSubPath(), node, pathMod.getMin(), pathMod.getMax(), output) ;
    }

    @Override
    public void visit(P_FixedLength pFixedLength) {
        engine.doFixedLengthPath(pFixedLength.getSubPath(), node, pFixedLength.getCount(), output) ;
    }

    @Override
    public void visit(P_ZeroOrOne path) {
        engine.doZeroOrOne(path.getSubPath(), node, output) ;
    }

    @Override
    public void visit(P_ZeroOrMore1 path) {
        engine.doZeroOrMore(path.getSubPath(), node, output) ;
    }

    @Override
    public void visit(P_ZeroOrMoreN path) {
        engine.doZeroOrMoreN(path.getSubPath(), node, output) ;
    }

    @Override
    public void visit(P_OneOrMore1 path) {
        engine.doOneOrMore(path.getSubPath(), node, output) ;
    }

    @Override
    public void visit(P_OneOrMoreN path) {
        engine.doOneOrMoreN(path.getSubPath(), node, output) ;
    }

    @Override
    public void visit(P_Alt pathAlt) {
        engine.doAlt(pathAlt.getLeft(), pathAlt.getRight(), node, output) ;
    }

    @Override
    public void visit(P_Distinct pathDistinct) {
        PathEngine engine2 = engine ;
        engine = new PathEngine1(graph, engine.direction()) ;
        engine.eval(pathDistinct.getSubPath(), node, output) ;
        engine = engine2 ;
    }

    @Override
    public void visit(P_Multi pathMulti) {
        PathEngine engine2 = engine ;
        engine = new PathEngineN(graph, engine.direction()) ;
        engine.eval(pathMulti.getSubPath(), node, output) ;
        engine = engine2 ;
    }

    @Override
    public void visit(P_Shortest path) {
        throw new ARQNotImplemented(WriterPath.asString(path)) ;
    }

    @Override
    public void visit(P_Seq pathSeq) {
        engine.doSeq(pathSeq.getLeft(), pathSeq.getRight(), node, output) ;
    }

    // Other operations can produce duplicates and so may be executed in
    // different ways depending on cardibnality requirements.

    protected static class FilterExclude implements Filter<Triple>
    {
        private Collection<Node> excludes ;

        public FilterExclude(Collection<Node> excludes) {
            this.excludes = excludes ;
        }

        @Override
        public boolean accept(Triple triple) {
            return !excludes.contains(triple.getPredicate()) ;
        }
    }

    final protected Iter<Triple> between(Node x, Node z) {
        return Iter.iter(engine.graphFind(x, Node.ANY, z)) ;
    }

    final protected void doZero(Path path, Node node, Collection<Node> output) {
        // Ignores path.
        output.add(node) ;
    }
}
