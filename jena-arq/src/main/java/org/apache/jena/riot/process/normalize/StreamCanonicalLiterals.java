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

package org.apache.jena.riot.process.normalize;

import org.apache.jena.graph.Node;
import org.apache.jena.riot.RDFParserBuilder;
import org.apache.jena.riot.process.StreamRDFApplyObject;
import org.apache.jena.riot.system.StreamRDF;

/** Canonicalize literal lexical forms (in the object position).
 * Canoncialize literals use the same RDF term (same lexcial form) for a given value.
 * So {@code "+01"^^xsd:integer} is converted to {@code "1"^^xsd:integer}.
 * Language tags are canonicalized for case as well.
 *
 * See {@link RDFParserBuilder#canonicalValues(boolean)} and {@link RDFParserBuilder#langTagCanonical()}.
 */
public class StreamCanonicalLiterals extends StreamRDFApplyObject {
    public StreamCanonicalLiterals(StreamRDF other) {
        super(other, StreamCanonicalLiterals::canonical);
    }

    private static Node canonical(Node n) {
        if ( ! n.isLiteral() )
            return n;
        Node obj2 = CanonicalizeLiteral.canonicalValue(n);
        return obj2;
    }
}
