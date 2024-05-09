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

package org.apache.jena.ontapi.impl.factories;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.enhanced.EnhNode;
import org.apache.jena.enhanced.Implementation;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.LiteralRequiredException;
import org.apache.jena.rdf.model.ResourceRequiredException;
import org.apache.jena.rdf.model.impl.AltImpl;
import org.apache.jena.rdf.model.impl.BagImpl;
import org.apache.jena.rdf.model.impl.LiteralImpl;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.rdf.model.impl.RDFListImpl;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.rdf.model.impl.SeqImpl;
import org.apache.jena.shared.JenaException;
import org.apache.jena.vocabulary.RDF;

public class STDObjectFactories {

    public static final Implementation NODE = new Implementation() {
        @Override
        public boolean canWrap(Node n, EnhGraph eg) {
            return true;
        }

        @Override
        public EnhNode wrap(Node n, EnhGraph eg) {
            if (n.isURI() || n.isBlank())
                return new ResourceImpl(n, eg);
            if (n.isLiteral())
                return new LiteralImpl(n, eg);
            return null;
        }
    };

    public static final Implementation RESOURCE = new Implementation() {
        @Override
        public boolean canWrap(Node n, EnhGraph eg) {
            return !n.isLiteral();
        }

        @Override
        public EnhNode wrap(Node n, EnhGraph eg) {
            if (n.isLiteral()) {
                throw new ResourceRequiredException(n);
            }
            return new ResourceImpl(n, eg);
        }
    };

    public static final Implementation PROPERTY = new Implementation() {
        @Override
        public boolean canWrap(Node n, EnhGraph eg) {
            return n.isURI();
        }

        @Override
        public EnhNode wrap(Node n, EnhGraph eg) {
            return new PropertyImpl(n, eg);
        }
    };

    public static final Implementation LITERAL = new Implementation() {
        @Override
        public boolean canWrap(Node n, EnhGraph eg) {
            return n.isLiteral();
        }

        @Override
        public EnhNode wrap(Node n, EnhGraph eg) {
            if (!n.isLiteral()) throw new LiteralRequiredException(n);
            return new LiteralImpl(n, eg);
        }
    };

    public static final Implementation ALT = new Implementation() {
        @Override
        public boolean canWrap(Node n, EnhGraph eg) {
            return true;
        }

        @Override
        public EnhNode wrap(Node n, EnhGraph eg) {
            return new AltImpl(n, eg);
        }
    };

    public static final Implementation BAG = new Implementation() {
        @Override
        public boolean canWrap(Node n, EnhGraph eg) {
            return true;
        }

        @Override
        public EnhNode wrap(Node n, EnhGraph eg) {
            return new BagImpl(n, eg);
        }
    };

    public static final Implementation SEQ = new Implementation() {
        @Override
        public boolean canWrap(Node n, EnhGraph eg) {
            return true;
        }

        @Override
        public EnhNode wrap(Node n, EnhGraph eg) {
            return new SeqImpl(n, eg);
        }
    };

    public static final Implementation RDF_LIST = new Implementation() {
        @Override
        public EnhNode wrap(Node n, EnhGraph eg) {
            if (canWrap(n, eg)) {
                return new RDFListImpl(n, eg);
            } else {
                throw new JenaException(String.format("Cannot convert node %s to RDFList", n));
            }
        }

        @Override
        public boolean canWrap(Node node, EnhGraph eg) {
            Graph g = eg.asGraph();
            return RDF.nil.asNode().equals(node) ||
                    g.contains(node, RDF.first.asNode(), Node.ANY) ||
                    g.contains(node, RDF.rest.asNode(), Node.ANY) ||
                    g.contains(node, RDF.type.asNode(), RDF.List.asNode());
        }
    };

}
