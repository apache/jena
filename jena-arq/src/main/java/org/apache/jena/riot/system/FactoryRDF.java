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

package org.apache.jena.riot.system;

import org.apache.jena.datatypes.RDFDatatype ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.sparql.core.Quad ;

/**
 * Create core RDF objects: {@link Node}s, {@link Triple}s, {@link Quad}s,
 * which are system-wide.
 */
public interface FactoryRDF {
    public Triple createTriple(Node subject, Node predicate, Node object);

    public Quad createQuad(Node graph, Node subject, Node predicate, Node object);

    public Node createURI(String uriStr);

    public Node createTypedLiteral(String lexical, RDFDatatype datatype);

    public Node createLangLiteral(String lexical, String langTag);

    public Node createStringLiteral(String lexical);

    /** Create a blank node which is completely new and used nowhere else. */
    public Node createBlankNode() ;

    /** Create a blank node with the given string as internal system id */
    public Node createBlankNode(String label) ;

    /** Create a blank with the internal system id taken from 128 bit number provided.
     * This must be compatible with {@link java.util.UUID} so the variant and version
     * bits must agree with <a href="http://www.ietf.org/rfc/rfc4122.txt">RFC 4122</a>.
     *
     *  @see java.util.UUID
     *  @see <a href="http://www.ietf.org/rfc/rfc4122.txt" ><i>RFC&nbsp;4122: A Universally Unique IDentifier (UUID) URN Namespace</i></a>
     */
    public Node createBlankNode(long mostSigBits, long leastSigBits) ;

    /** Reset any internal state that should not be carried across parse runs (e.g. blank node labels). */
    public void reset();
}
