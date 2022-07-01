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

package org.apache.jena.shacl.vocabulary;

import java.util.Objects;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;

/** Vocabulary for Jena additions to SHACL */
public class SHJ {

    /** The namespace of the vocabulary as a string */
    public static final String NS = "http://jena.apache.org/shacl#";

    /** The namespace of the vocabulary as a string*/
    public static String getURI() {return NS;}
    
    /** Namespace */
    public String ns() { return NS; }
        
    private static String uri(String ns, String local) {
        Objects.requireNonNull(ns);
        Objects.requireNonNull(local);
        return ns+local;
    }

    private static Node createResource(String ns, String localName) { return NodeFactory.createURI(uri(ns, localName)); }
    private static Node createProperty(String ns, String localName) { return NodeFactory.createURI(uri(ns, localName)); }

    public static final Node LogConstraintComponent         = createResource(NS, "LogConstraintComponent");
    public static final Node logConstraint                  = createProperty(NS, "log");

    public static final Node ViolationConstraintComponent   = createResource(NS, "ViolationConstraintComponent");
    public static final Node violation                      = createProperty(NS, "violation");
}
