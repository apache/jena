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

package org.apache.jena.rdfs.engine;

import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Quad;

/** Mappers */
public class Mappers {
    private static MapperX<Node, Triple> mapperSingletonTriple = new MapperTriple();
    private static MapperX<Node, Tuple<Node>> mapperSingletonTuple = new MapperTuple();

    public static MapperX<Node, Triple> mapperTriple() {
        return mapperSingletonTriple;
    }

    public static MapperX<Node, Quad> mapperQuad(Node g) {
        return new MapperQuad(g);
    }

    private static class MapperTriple implements MapperX<Node, Triple> {
        @Override public Node fromNode(Node n)          { return n; }
        @Override public Node toNode(Node x)            { return x; }
        @Override public Node subject(Triple triple)    { return triple.getSubject(); }
        @Override public Node predicate(Triple triple)  { return triple.getPredicate(); }
        @Override public Node object(Triple triple)     { return triple.getObject(); }
    }

    public static class MapperQuad implements MapperX<Node, Quad> {
        private final Node graph;
        MapperQuad(Node g)  { this.graph = g; }
        @Override public Node fromNode(Node n)      { return n; }
        @Override public Node toNode(Node x)        { return x; }
        @Override public Node subject(Quad quad)    { return quad.getSubject(); }
        @Override public Node predicate(Quad quad)  { return quad.getPredicate(); }
        @Override public Node object(Quad quad)     { return quad.getObject(); }
    }

    private static class MapperTuple implements MapperX<Node, Tuple<Node>> {
        @Override public Node fromNode(Node n)              { return n; }
        @Override public Node toNode(Node x)                { return x; }
        @Override public Node subject(Tuple<Node> tuple)    { return offset(tuple, 0); }
        @Override public Node predicate(Tuple<Node> tuple)  { return offset(tuple, 1); }
        @Override public Node object(Tuple<Node> tuple)     { return offset(tuple, 2); }

        private static Node offset(Tuple<Node> tuple, int i) {
            int idx = ( tuple.len() == 3 ) ? i : i+1;
            return tuple.get(idx);
        }
    }
}
