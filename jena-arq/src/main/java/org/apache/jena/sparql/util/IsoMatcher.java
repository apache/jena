/**
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

/**
 * Simple isomorphism testing for on unordered collections. 
 * This code is simple and slow.
 * For graphs, the Graph isomorphism code in Jena is much better (better tested, better performance)
 * This code can work on any tuples of nodes.
 * 
 * See {@link Iso} for isomorphism for ordered lists.
 * 
 * See {@link IsoAlg} for the isomorphism algorithm.
 */
public class IsoMatcher
{
    /** Graph isomorphism */
    public static boolean isomorphic(Graph g1, Graph g2) {
        List<Tuple<Node>> x1 = tuplesTriples(g1.find());
        List<Tuple<Node>> x2 = tuplesTriples(g2.find());
        return isomorphic(x1, x2, NodeUtils.sameRdfTerm);
    }

    /** Dataset isomorphism */
    public static boolean isomorphic(DatasetGraph dsg1, DatasetGraph dsg2) {
        List<Tuple<Node>> x1 = tuplesQuads(dsg1.find());
        List<Tuple<Node>> x2 = tuplesQuads(dsg2.find());
        return isomorphic(x1, x2, NodeUtils.sameRdfTerm);
    }

    /** Collection of tuples isomorphism */
    public static boolean isomorphic(Collection<Tuple<Node>> x1, Collection<Tuple<Node>> x2) {
        return isomorphic(x1, x2, NodeUtils.sameRdfTerm);
    }

    /** Helper - convert to {@code List<Tuple<Node>>} */
    public static List<Tuple<Node>> tuplesTriples(Iterator<Triple> iter) {
        return Iter.iter(iter).map(t->tuple(t.getSubject(), t.getPredicate(), t.getObject())).toList();
    }

    /** Helper - convert to {@code List<Tuple<Node>>} */
    public static List<Tuple<Node>> tuplesQuads(Iterator<Quad> iter) {
        return Iter.iter(iter).map(q->tuple(q.getGraph(), q.getSubject(), q.getPredicate(), q.getObject())).toList();
    }

    /** Collection of tuples isomorphism, with choice of when two nodes are "equal".
     * See also {@link IsoAlg#isIsomorphic(Collection, Collection, org.apache.jena.sparql.util.Iso.Mappable, EqualityTest)}
     * for isomorphisms testing for more than just blank nodes.
     */
    public static boolean isomorphic(Collection<Tuple<Node>> x1, Collection<Tuple<Node>> x2, EqualityTest nodeTest) {
        return IsoAlg.isIsomorphic(x1, x2, nodeTest);
    }
}
