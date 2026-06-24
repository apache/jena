/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.sparql.vocabulary;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;

public enum SPARQLVersions {
/*

PREFIX sparql:  <http://www.w3.org/ns/sparql#>

## URIs for versions of SPARQL
## The rdfs:label corresponds to the constant defined in SPARQL Query Language.


sparql:version-1.2 a rdfs:Resource ;
    rdfs:label "1.2" ;
    rdfs:comment "Version 1.2." ;
    rdfs:isDefinedBy <https://www.w3.org/ns/sparql> ;
    rdfs:seeAlso <http://www.w3.org/TR/sparql12-query/> ;
    .

sparql:version-1.2-basic a rdfs:Resource ;
    rdfs:label "1.2-basic" ;
    rdfs:comment "Version 1.2-basic" ;
    rdfs:isDefinedBy <https://www.w3.org/ns/sparql> ;
    rdfs:seeAlso <http://www.w3.org/TR/sparql12-query/> ;
    .

sparql:version-1.1 a rdfs:Resource ;
    rdfs:label "1.1" ;
    rdfs:comment "Version 1.1" ;
    rdfs:isDefinedBy <https://www.w3.org/ns/sparql> ;
    dc:conformsTo <http://www.w3.org/TR/sparql11-query/> ;
    .

sparql:version-1.0 a rdfs:Resource ;
    rdfs:label "1.0" ;
    rdfs:comment "Version 1.0" ;
    rdfs:isDefinedBy <https://www.w3.org/ns/sparql> ;
    dc:conformsTo <http://www.w3.org/TR/sparql10-query/> ;
    .
 */

    SPARQL_10(SPARQL.NS+"version-1.0", "1.0"),
    SPARQL_11(SPARQL.NS+"version-1.1", "1.1"),
    SPARQL_12_basic(SPARQL.NS+"version-1.2-basic", "1.2-basic"),
    SPARQL_12(SPARQL.NS+"version-1.2", "1.2");

    public final String uri;
    public final Node term;
    public final String label;

    SPARQLVersions(String uri, String label) {
        this.uri = uri;
        this.term = NodeFactory.createURI(uri);
        this.label = label;
    }
}
