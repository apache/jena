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

import java.util.ArrayList ;
import java.util.Collection ;
import java.util.Iterator ;

import org.apache.jena.atlas.iterator.Iter ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.path.Path ;
import com.hp.hpl.jena.sparql.util.Context ;

/** Path evaluation - public interface */

public class PathEval
{
    /** Evaluate a path : SPARQL semantics */
    static public Iterator<Node> eval(Graph graph, Node node, Path path, Context context) {
        return eval$(graph, node, path, new PathEngineSPARQL(graph, true, context)) ;
        // return eval$(graph, node, path, new PathEngineN(graph, true)) ;
    }

    /** Evaluate a path */
    static public Iterator<Node> evalReverse(Graph graph, Node node, Path path, Context context) {
        return eval$(graph, node, path, new PathEngineSPARQL(graph, false, context)) ;
        // return eval$(graph, node, path, new PathEngineN(graph, false)) ;
    }

    /** Evaluate a path : counting semantics */
    static public Iterator<Node> evalN(Graph graph, Node node, Path path) {
        return eval$(graph, node, path, new PathEngineN(graph, true)) ;
    }

    /** Evaluate a path : counting semantics */
    static public Iterator<Node> evalReverseN(Graph graph, Node node, Path path) {
        return eval$(graph, node, path, new PathEngineN(graph, false)) ;
    }

    /** Evaluate a path : unique results */
    static public Iterator<Node> eval1(Graph graph, Node node, Path path) {
        return eval$(graph, node, path, new PathEngine1(graph, true)) ;
    }

    /** Evaluate a path : unique results */
    static public Iterator<Node> evalReverse1(Graph graph, Node node, Path path) {
        return eval$(graph, node, path, new PathEngine1(graph, false)) ;
    }

    /** Evaluate a path */
    static void eval$(Graph graph, Node node, Path path, PathEngine engine, Collection<Node> acc) {
        PathEvaluator evaluator = new PathEvaluator(graph, node, acc, engine) ;
        path.visit(evaluator) ;
    }

    /** Evaluate a path */
    static Iter<Node> eval$(Graph graph, Node node, Path path, PathEngine engine) {
        Collection<Node> acc = new ArrayList<>() ;
        PathEvaluator evaluator = new PathEvaluator(graph, node, acc, engine) ;
        path.visit(evaluator) ;
        return Iter.iter(acc) ;
    }
}
