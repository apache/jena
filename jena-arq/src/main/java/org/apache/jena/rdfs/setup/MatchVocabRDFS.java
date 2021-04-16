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

package org.apache.jena.rdfs.setup;


import static org.apache.jena.rdfs.engine.ConstRDFS.rdfsDomain;
import static org.apache.jena.rdfs.engine.ConstRDFS.rdfsRange;
import static org.apache.jena.rdfs.engine.ConstRDFS.rdfsSubClassOf;
import static org.apache.jena.rdfs.engine.ConstRDFS.rdfsSubPropertyOf;

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdfs.engine.Match;

/**
 *  The vocabulary, with transitive closure of subClass and subProperty, as a {@link Match}.
 */
public class MatchVocabRDFS implements Match<Node, Triple>{

    private final ConfigRDFS<Node> setup;

    public MatchVocabRDFS(ConfigRDFS<Node> setup) {
        this.setup = setup;
    }

    private static Node nullToAny(Node n) { return (n == null) ? Node.ANY : n; }

    @Override
    public Stream<Triple> match(Node s, Node p , Node o) {
        Node sx = nullToAny(s);
        Node px = nullToAny(p);
        Node ox = nullToAny(o);
        return match2(nullToAny(s), nullToAny(p), nullToAny(o));
    }

    private Stream<Triple> match2(Node s, Node p , Node o) {

        if ( p == Node.ANY ) {
            // (?? ANY ??)
            Stream<Triple> stream = match(s,rdfsSubClassOf,o);
            stream = Stream.concat(stream, match(s,rdfsSubPropertyOf,o));
            stream = Stream.concat(stream, match(s,rdfsDomain,o));
            stream = Stream.concat(stream, match(s,rdfsRange,o));
            return stream;
        }

        if ( o.isConcrete() && ! s.isConcrete() ) {
            // (ANY p o)
            Set<Node> set;
            if ( p.equals(rdfsSubClassOf) )
                set = setup.getSubClassesInc(o);
            else if ( p.equals(rdfsSubPropertyOf) )
                set = setup.getSubPropertiesInc(o);
            else if ( p.equals(rdfsDomain) )
                set = setup.getPropertiesByDomain(o);
            else if ( p.equals(rdfsRange) )
                set = setup.getPropertiesByRange(o);
            else {
                return Stream.empty();
            }
            // s unbound
            return set.stream().map(x->Triple.create(x,p,o));
        }

        // p is defined, o is not, s maybe
        // (?? p ANY)
        Map<Node, Set<Node>> map;
        if ( p.equals(rdfsSubClassOf) )
            map = setup.getSubClassHierarchy();
        else if ( p.equals(rdfsSubPropertyOf) )
            map = setup.getSubPropertyHierarchy();
        else if ( p.equals(rdfsDomain) )
            map = setup.getPropertyDomains();
        else if ( p.equals(rdfsRange) )
            map = setup.getPropertyRanges();
        else {
            return Stream.empty();
        }

        if ( s.isConcrete() ) {
            // (s p ANY)
            Set<Node> x = map.get(s);
            if ( x == null )
                return Stream.empty();
            if ( o.isConcrete() ) {
                return ( x.contains(o) ) ? Stream.of(Triple.create(s,p,o)) : Stream.empty();
            } else {
                return x.stream().map(ox->Triple.create(s,p,ox));
            }
        }

        // (ANY p ANY)
        return map.entrySet()
                .stream()
                .flatMap( e->e.getValue().stream().map(obj->Triple.create(e.getKey(), p, obj)) );
    }
}
