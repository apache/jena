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

package org.apache.jena.shex.eval;

import java.util.Set;
import java.util.stream.Collectors;

import org.apache.jena.atlas.lib.StreamOps;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.other.G;
import org.apache.jena.shex.expressions.ShapeExpression;
import org.apache.jena.shex.expressions.TripleConstraint;
import org.apache.jena.shex.sys.ReportItem;
import org.apache.jena.shex.sys.ValidationContext;

/*package*/ class ShapeEvalTripleConstraint {

    /** Triple Constraint, with cardinality */
    static boolean matchesCardinalityTC(ValidationContext vCxt, Set<Triple> matchables, Node node,
                                        TripleConstraint tripleConstraint, Set<Node> extras) {
        Node predicate = tripleConstraint.getPredicate();
        if ( tripleConstraint.reverse() ) {
            // [shex] A bit of a fudge.
            matchables = G.find(vCxt.getData(), null, predicate, node).toSet();
        } else {
            if ( ! matchables.stream().allMatch(t->predicate.equals(t.getPredicate())) ) {
                // Other predicates present.
                return false;
            }
        }
        // Find same predicate.
        Set<Triple> triples = StreamOps.toSet(matchables.stream().filter(t->predicate.equals(t.getPredicate())));
        int min = tripleConstraint.min();
        int max = tripleConstraint.max();
        ShapeExpression shExpr = tripleConstraint.getShapeExpression();

        Set<Triple> positive = triples.stream().filter(t->{
            Node v = tripleConstraint.reverse() ? t.getSubject() : t.getObject();
            return shExpr.satisfies(vCxt, v);
        }).collect(Collectors.toSet());

        int N = positive.size();
        if ( min >= 0 && N < min ) {
            vCxt.reportEntry(new ReportItem("Cardinality violation (min="+min+"): "+N, null));
            return false;
        }
        // Remove extras.
        if ( extras == null || ! extras.contains(predicate) ) {
            if ( positive.size() != triples.size() )
                // Something did not match.
                return false;
        }

        if ( max >= 0 && N > max ) {
            vCxt.reportEntry(new ReportItem("Cardinality violation (max="+max+"): "+N, null));
            return false;
        }
        return true;
    }
}
