/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.apache.jena.dboe.storage.migrate;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Quad;

/** Ways to match triples. quads */
public class M {

//    public static Stream<Triple> match(Collection<Triple> triples, Node s, Node p, Node o) {
//        return triples.stream()
//            .filter(t-> match(t, s,p,o));
//    }
//
//    public static Stream<Quad> match(Collection<Quad> quads, Node g, Node s, Node p, Node o) {
//        return quads.stream()
//            .filter(q-> match(q, g,s,p,o));
//    }

    public static boolean match(Quad quad, Node g, Node s, Node p, Node o) {
        return
            match(quad.getGraph(), g) &&
            match(quad.getSubject(), s) &&
            match(quad.getPredicate(), p) &&
            match(quad.getObject(), o);
    }

    public static boolean match(Triple triple, Node s, Node p, Node o) {
        return
            match(triple.getSubject(), s) &&
            match(triple.getPredicate(), p) &&
            match(triple.getObject(), o);
    }

    public static boolean match(Node node, Node pattern) {
        return pattern == null || pattern == Node.ANY || pattern.equals(node);
    }

//    public static void checkConcrete(Node...nodes) {
//        for ( Node n : nodes )
//            checkConcrete(n);
//    }
//
//    public static void checkConcrete(Node n) {
//        if ( ! n.isConcrete() )
//            throw new IllegalArgumentException("Not concrete: "+n);
//    }
}
