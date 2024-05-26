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

import java.util.Optional;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.util.graph.GraphUtils;

public class GraphUtilsExtra {
    public static Number getAsNumber(Resource resource, Property property) {
        Number result = null;
        RDFNode rdfNode = GraphUtils.getAsRDFNode(resource, property);
        if (rdfNode != null) {
            Node node = rdfNode.asNode();
            result = NodeUtilsExtra.getNumberOrNull(node);
        }
        return result;
    }

    public static int getAsInt(Resource resource, Property property, int fallback) {
        return Optional.ofNullable(getAsNumber(resource, property)).map(Number::intValue).orElse(fallback);
    }

    public static long getAsLong(Resource resource, Property property, long fallback) {
        return Optional.ofNullable(getAsNumber(resource, property)).map(Number::longValue).orElse(fallback);
    }
}
