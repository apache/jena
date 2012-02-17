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

package com.hp.hpl.jena.sparql.path.eval;

import java.util.Iterator ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.path.Path ;
import com.hp.hpl.jena.sparql.path.PathFactory ;

/** Path evaluation - public interface */

public class PathEval
{

    /** Evaluate a path */ 
    static public Iterator<Node> eval(Graph graph, Node node, Path path)
    {
        return PathEvaluator.eval(graph, node, path, new PathEngineN(graph, true)) ;
    }

    /** Evaluate a path : unique results */ 
    static public Iterator<Node> eval1(Graph graph, Node node, Path path)
    {
        path = PathFactory.pathDistinct(path) ;
        return PathEvaluator.eval(graph, node, path, new PathEngineN(graph, true)) ;
    }

    /** Evaluate a path */ 
    static public Iterator<Node> evalReverse(Graph graph, Node node, Path path)
    {
        return PathEvaluator.eval(graph, node, path, new PathEngineN(graph, false)) ;
    }

    /** Evaluate a path : unique results */ 
    static public Iterator<Node> evalReverse1(Graph graph, Node node, Path path)
    {
        path = PathFactory.pathDistinct(path) ;
        return PathEvaluator.eval(graph, node, path, new PathEngineN(graph, false)) ;
    }
}
