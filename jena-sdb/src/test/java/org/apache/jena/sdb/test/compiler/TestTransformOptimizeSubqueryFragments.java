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

package org.apache.jena.sdb.test.compiler;

import org.apache.jena.query.QueryFactory;
import org.apache.jena.sdb.compiler.TransformOptimizeSubqueryFragments;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.sse.SSE;
import org.junit.Assert;
import org.junit.Test;

public class TestTransformOptimizeSubqueryFragments {
    @Test
    public void testSimpleOptional() {
        testTransform(
                // SPARQL to Test
                "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "        \n" +
                "        SELECT ?sublect ?label\n" +
                "        WHERE {\n" +
                "            ?subject a  foaf:Person.\n" +
                "            OPTIONAL {\n" +
                "                ?subject rdfs:label ?label .\n" +
                "            }\n" +
                "        } \n",

                // Target Op
                "(project (?sublect ?label)\n" +
                "  (leftjoin\n" +
                "    (bgp (triple ?subject <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person>))\n" +
                "    (bgp\n" +
                "      (triple ?subject <http://www.w3.org/2000/01/rdf-schema#label> ?label)\n" +
                "      (triple ?subject <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person>)\n" +
                "    )))\n"
        );
    }

    @Test
    public void testSimpleMinus() {
        testTransform(
                // SPARQL to Test
                "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                        "        \n" +
                        "        SELECT ?sublect ?label\n" +
                        "        WHERE {\n" +
                        "            ?subject a  foaf:Person.\n" +
                        "            MINUS {\n" +
                        "                ?subject rdfs:label ?label .\n" +
                        "            }\n" +
                        "        } \n",

                // Target Op
                "(project (?sublect ?label)\n" +
                        "  (minus\n" +
                        "    (bgp (triple ?subject <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person>))\n" +
                        "    (bgp\n" +
                        "      (triple ?subject <http://www.w3.org/2000/01/rdf-schema#label> ?label)\n" +
                        "      (triple ?subject <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person>)\n" +
                        "    )))\n"
        );
    }

    @Test
    public void testOptionalNotBound() {
        testTransform(
                // SPARQL to Test
                "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                        "        \n" +
                        "        SELECT ?sublect ?label\n" +
                        "        WHERE {\n" +
                        "            ?subject a  foaf:Person.\n" +
                        "            OPTIONAL {\n" +
                        "                ?unbound rdfs:label ?label .\n" +
                        "            }\n" +
                        "        } \n",

                // Target Op
                "(project (?sublect ?label)\n" +
                        "  (leftjoin\n" +
                        "    (bgp (triple ?subject <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person>))\n" +
                        "    (bgp (triple ?unbound <http://www.w3.org/2000/01/rdf-schema#label> ?label))))\n"
        );
    }

    @Test
    public void testUnion() {
        testTransform(
                // SPARQL to Test
                "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                        "        \n" +
                        "        SELECT ?sublect ?label\n" +
                        "        WHERE {\n" +
                        "            ?subject a  foaf:Person.\n" +
                        "            {\n" +
                        "                ?subject rdfs:label ?label .\n" +
                        "            } UNION {\n" +
                        "                ?subject <http://example.com/hasVcard> ?vcard .\n" +
                        "                ?vcard rdfs:label ?label .\n" +
                        "            }\n" +
                        "        } \n",

                // Target Op
                "(project (?sublect ?label)\n" +
                        "  (join\n" +
                        "    (bgp (triple ?subject <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person>))\n" +
                        "    (union\n" +
                        "      (bgp\n" +
                        "        (triple ?subject <http://www.w3.org/2000/01/rdf-schema#label> ?label)\n" +
                        "        (triple ?subject <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person>)\n" +
                        "      )\n" +
                        "      (bgp\n" +
                        "        (triple ?subject <http://example.com/hasVcard> ?vcard)\n" +
                        "        (triple ?vcard <http://www.w3.org/2000/01/rdf-schema#label> ?label)\n" +
                        "        (triple ?subject <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person>)\n" +
                        "      ))))\n"
        );
    }


    @Test
    public void testUnionWithOptional() {
        testTransform(
                // SPARQL to Test
                "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                        "        \n" +
                        "        SELECT ?sublect ?label\n" +
                        "        WHERE {\n" +
                        "            ?subject a  foaf:Person.\n" +
                        "            {\n" +
                        "                ?subject rdfs:label ?label .\n" +
                        "            } UNION {\n" +
                        "                ?subject <http://example.com/hasVcard> ?vcard .\n" +
                        "                OPTIONAL {\n" +
                        "                   ?vcard rdfs:label ?label .\n" +
                        "                }\n" +
                        "            }\n" +
                        "        } \n",

                // Target Op
                "(project (?sublect ?label)\n" +
                        "  (join\n" +
                        "    (bgp (triple ?subject <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person>))\n" +
                        "    (union\n" +
                        "      (bgp\n" +
                        "        (triple ?subject <http://www.w3.org/2000/01/rdf-schema#label> ?label)\n" +
                        "        (triple ?subject <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person>)\n" +
                        "      )\n" +
                        "      (leftjoin\n" +
                        "        (bgp\n" +
                        "          (triple ?subject <http://example.com/hasVcard> ?vcard)\n" +
                        "          (triple ?subject <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person>)\n" +
                        "        )\n" +
                        "        (bgp\n" +
                        "          (triple ?vcard <http://www.w3.org/2000/01/rdf-schema#label> ?label)\n" +
                        "          (triple ?subject <http://example.com/hasVcard> ?vcard)\n" +
                        "          (triple ?subject <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person>)\n" +
                        "        )))))\n"
        );
    }

    @Test
    public void testUnionRestriction() {
        testTransform(
                // SPARQL to Test
                "CONSTRUCT {\n" +
                        "  ?publication a ?type .\n" +
                        "  ?publication <http://localhost/access> ?access .\n" +
                        "} WHERE {\n" +
                        "  ?publication a ?type .\n" +
                        "  OPTIONAL { ?publication <http://localhost/access> ?access . }\n" +
                        "  ?publication <http://localhost/dateTimeValue>  ?dateTimeObj .\n" +
                        "  ?dateTimeObj  <http://localhost/dateTime> ?dateTime .\n" +
                        "    {\n" +
                        "\t\t ?publication a <http://localhost/Report>  .\n" +
                        "\t }\n" +
                        "\t UNION\n" +
                        "\t  {\n" +
                        "\t\t ?publication a <http://localhost/AcademicArticle>  .\n" +
                        "\t }\n" +
                        "}\n",

                // Target Op
                "(join\n" +
                        "  (join\n" +
                        "    (leftjoin\n" +
                        "      (bgp\n" +
                        "        (triple ?publication <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?type)\n" +
                        "      )\n" +
                        "      (bgp\n" +
                        "        (triple ?publication <http://localhost/access> ?access)\n" +
                        "      ))\n" +
                        "    (bgp\n" +
                        "      (triple ?publication <http://localhost/dateTimeValue> ?dateTimeObj)\n" +
                        "      (triple ?dateTimeObj <http://localhost/dateTime> ?dateTime)\n" +
                        "    ))\n" +
                        "  (union\n" +
                        "    (bgp (triple ?publication <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://localhost/Report>))\n" +
                        "    (bgp (triple ?publication <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://localhost/AcademicArticle>))\n" +
                        "    ))\n\n"
        );
    }


    private void testTransform(String sparql, String targetOp) {
        // Generate Op
        Op op = Algebra.compile(QueryFactory.create(sparql));

        // Transofrm to optimize subquery
        op = TransformOptimizeSubqueryFragments.transform(op);

        // Parse target Op structure and compate
        Op target = SSE.parseOp(targetOp);
        Assert.assertTrue(target.equalTo(op, null));
    }
}
