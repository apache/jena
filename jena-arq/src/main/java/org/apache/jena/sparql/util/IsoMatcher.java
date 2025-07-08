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

package org.apache.jena.sparql.util ;

import static org.apache.jena.atlas.lib.tuple.TupleFactory.tuple ;

import java.util.Collection ;
import java.util.Iterator ;
import java.util.List ;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.tuple.Tuple ;
import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.Quad ;
import org.apache.jena.sparql.util.iso.IsoAlgRows;
import org.apache.jena.sparql.util.iso.IsoAlgTuple;

/**
 * Simple isomorphism testing for on unordered collections.
 * This code is simple and slow.
 * This code can work on any tuples of nodes.
 *
 * See {@link Iso} for isomorphism for ordered lists.
 *
 * See {@link IsoAlgTuple} for the isomorphism algorithm.
 * See {@link IsoAlgRows} for the isomorphism algorithm for rowsets.
 */
public class IsoMatcher
{
    /** Graph isomorphism */
    public static boolean isomorphic(Graph graph1, Graph graph2) {
        List<Tuple<Node>> x1 = tuplesTriples(graph1.find());
        List<Tuple<Node>> x2 = tuplesTriples(graph2.find());
        return isomorphicTuples(x1, x2, NodeUtils.sameRdfTerm);
    }

    /** Dataset isomorphism */
    public static boolean isomorphic(DatasetGraph dsg1, DatasetGraph dsg2) {
        List<Tuple<Node>> x1 = tuplesQuads(dsg1.find());
        List<Tuple<Node>> x2 = tuplesQuads(dsg2.find());
        return isomorphicTuples(x1, x2, NodeUtils.sameRdfTerm);
    }

    /** Collection of triples isomorphism */
    public static boolean isomorphic(Collection<Triple> triples1, Collection<Triple> triples2) {
        List<Tuple<Node>> x1 = tuplesTriples(triples1.iterator());
        List<Tuple<Node>> x2 = tuplesTriples(triples2.iterator());
        return isomorphicTuples(x1, x2, NodeUtils.sameRdfTerm);
    }

    /** Collection of tuples isomorphism */
    public static boolean isomorphicTuples(Collection<Tuple<Node>> x1, Collection<Tuple<Node>> x2) {
        return isomorphicTuples(x1, x2, NodeUtils.sameRdfTerm);
    }

    /** Helper - convert to {@code List<Tuple<Node>>} */
    private static List<Tuple<Node>> tuplesQuads(Iterator<Quad> iter) {
        return Iter.iter(iter).map(q->tuple(q.getGraph(), q.getSubject(), q.getPredicate(), q.getObject())).toList();
    }

    /** Helper - convert to {@code List<Tuple<Node>>} */
    private static List<Tuple<Node>> tuplesTriples(Iterator<Triple> iter) {
        try {
            return Iter.iter(iter).map(t->tuple(t.getSubject(), t.getPredicate(), t.getObject())).toList();
        }
        finally { Iter.close(iter); }
    }

    /** Collection of tuples isomorphism, with choice of when two nodes are "equal".
     * See also {@link IsoAlgTuple#isIsomorphic(Collection, Collection, org.apache.jena.sparql.util.Iso.Mappable, EqualityTest)}
     * for isomorphisms testing for more than just blank nodes.
     */
    private static boolean isomorphicTuples(Collection<Tuple<Node>> x1, Collection<Tuple<Node>> x2, EqualityTest nodeTest) {
        return IsoAlgTuple.isIsomorphic(x1, x2, nodeTest);
    }
}
