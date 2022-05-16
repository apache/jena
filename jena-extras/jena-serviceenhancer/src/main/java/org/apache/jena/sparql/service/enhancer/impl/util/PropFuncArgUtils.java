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

package org.apache.jena.sparql.service.enhancer.impl.util;

import java.util.Collections;
import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.pfunction.PropFuncArg;
import org.apache.jena.vocabulary.RDF;

public class PropFuncArgUtils {

    /** If the argument is neither null nor rdf:nil then the result is a singleton list containing it.
     *  Otherwise an empty list is returned. */
    public static List<Node> nodeToList(Node node) {
        List<Node> result = node == null || RDF.Nodes.nil.equals(node)
                ? Collections.emptyList()
                : Collections.singletonList(node);
        return result;
    }

    /** Return a list also if the given argument holds a single Node */
    public static List<Node> getAsList(PropFuncArg arg) {
        List<Node> result = arg.isNode()
                ? nodeToList(arg.getArg())
                : arg.getArgList();
        return result;
    }
}
