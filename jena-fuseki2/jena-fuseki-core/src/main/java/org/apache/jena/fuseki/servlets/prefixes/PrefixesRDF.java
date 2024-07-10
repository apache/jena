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

package org.apache.jena.fuseki.servlets.prefixes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Transactional;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.exec.UpdateExec;
import org.apache.jena.tdb2.DatabaseMgr;

/**
 * {@link PrefixesAccess} implementation using a dedicated {@link DatasetGraph} the
 * record the prefixes in an RDF format.
 * <p>
 * The prefix-URI mappings are represented in blank nodes of type Prefix, with
 * prefixName and prefixURI attributes.
 *
 * <pre>
 * PREFIX prefixes: &lt;http://jena.apache.org/prefixes#&gt;
 * PREFIX rdf:      &lt;http://www.w3.org/1999/02/22-rdf-syntax-ns##&gt;
 * PREFIX xsd:      &lt;http://www.w3.org/2001/XMLSchema##&gt;
 *
 * [] rdf:type prefixes:Prefix ;
 *    prefixes:prefixName  "test" ;
 *    prefixes:prefixURI   "http://example/testPrefix#"^^xsd:anyURI
 *    .
 *
 * [ rdf:type prefixes:Prefix ;
 *   prefixes:prefixName   "ns" ;
 *   prefixes:prefixURI    "http://example/namespace#"^^xsd:anyURI
 * ]
 * </pre>
 */

public class PrefixesRDF implements PrefixesAccess {

    DatasetGraph dataset = DatabaseMgr.createDatasetGraph();

    @Override
    public Transactional transactional() { return dataset; }

    @Override
    public Optional<String> fetchURI(String prefix) {
        ParameterizedSparqlString pss = new ParameterizedSparqlString(
                """
                PREFIX prefixes: <http://jena.apache.org/prefixes#>
                SELECT ?prefixURI WHERE {
                    ?X prefixes:prefixName ?prefixName .
                    ?X prefixes:prefixURI ?prefixURI
                }
                """
        );
        pss.setLiteral("prefixName", prefix);
        String query = pss.toString();
        return dataset.calculateRead(() -> {
            try (QueryExec qExec = QueryExec.dataset(dataset).query(query).build()) {
                List<String> x = new ArrayList<String>();
                RowSet rowSet = qExec.select();
                rowSet.forEachRemaining(row -> {
                    String prefixNameLiteral = row.get("prefixURI").getLiteralLexicalForm();
                    if ( prefixNameLiteral != null ) {
                        x.add(prefixNameLiteral);
                    }
                });
                if (x.isEmpty())
                    return Optional.empty();
                return Optional.ofNullable(x.get(0));
            }
        });
    }

    @Override
    public void updatePrefix(String prefix, String uri) {

        ParameterizedSparqlString pss = new ParameterizedSparqlString(
                """
                PREFIX prefixes: <http://jena.apache.org/prefixes#>
                PREFIX xsd:      <http://www.w3.org/2001/XMLSchema#>
                ASK WHERE {  ?X prefixes:prefixName  ?prefixName ;
                                prefixes:prefixURI   ?prefixURI .
                }
                """);
        pss.setLiteral("prefixName", prefix);
        String query = pss.toString();

        dataset.executeWrite(()->{
            AtomicBoolean result = new AtomicBoolean(false);
            try (QueryExec qExec = QueryExec.dataset(dataset).query(query).build()) {
                result.set(qExec.ask());
            }

            String update;
            ParameterizedSparqlString pssUpdate;
            if (result.get()) {
                pssUpdate = new ParameterizedSparqlString(
                        """
                                PREFIX prefixes: <http://jena.apache.org/prefixes#>
                                PREFIX xsd:      <http://www.w3.org/2001/XMLSchema#>
                                DELETE { ?X prefixes:prefixURI ?oldURI }
                                INSERT { ?X prefixes:prefixURI ?newURI }
                                WHERE {
                                    ?X prefixes:prefixName ?prefixName ;
                                        prefixes:prefixURI ?oldURI
                                }
                        """);
                pssUpdate.setLiteral("prefixName", prefix);
                pssUpdate.setLiteral("newURI", uri, XSDDatatype.XSDanyURI);
                update = pssUpdate.toString();
            }
            else {
                pssUpdate = new ParameterizedSparqlString(
                        """
                               PREFIX prefixes: <http://jena.apache.org/prefixes#>
                               PREFIX xsd:      <http://www.w3.org/2001/XMLSchema#>
                               PREFIX rdf:      <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
                               INSERT DATA {
                                   [] rdf:type prefixes:Prefix ;
                                       prefixes:prefixName ?prefixName ;
                                       prefixes:prefixURI ?newURI
                               }
                        """);
                pssUpdate.setLiteral("prefixName", prefix);
                pssUpdate.setLiteral("newURI", uri, XSDDatatype.XSDanyURI);
                update = pssUpdate.toString();
            }
            UpdateExec.dataset(dataset).update(update).execute();
        });
    }

    @Override
    public void removePrefix(String prefixToRemove) {
        ParameterizedSparqlString pss = new ParameterizedSparqlString(
                """
                PREFIX prefixes: <http://jena.apache.org/prefixes#>
                DELETE WHERE { ?X prefixes:prefixName ?prefixName }
                """
        );
        pss.setLiteral("prefixName", prefixToRemove);
        String update = pss.toString();
        dataset.executeWrite(()->{
            UpdateExec.dataset(dataset).update(update).execute();
        });
    }

    @Override
    public Map<String, String> getAll() {
        String query =
                """
                PREFIX prefixes: <http://jena.apache.org/prefixes#>
                SELECT ?prefixName ?prefixURI WHERE {
                    ?X prefixes:prefixName ?prefixName .
                    ?X prefixes:prefixURI ?prefixURI
                }
                """;
        return dataset.calculateRead(()->{
            Map<String, String> allPairs = new ConcurrentHashMap<>();
            try (QueryExec qExec = QueryExec.dataset(dataset).query(query).build()) {
                RowSet rowSet = qExec.select();
                rowSet.forEachRemaining(row -> {
                    String prefixNameLiteral = row.get("prefixName").getLiteralLexicalForm();
                    String prefixURILiteral = row.get("prefixURI").getLiteralLexicalForm();
                    if (prefixNameLiteral != null && prefixURILiteral != null)
                        allPairs.put(prefixNameLiteral, prefixURILiteral);
                });
                return allPairs;
            }
        });
    }

    @Override
    public List<String> fetchPrefix(String uri) {
        ParameterizedSparqlString pss = new ParameterizedSparqlString(
                """
                PREFIX prefixes: <http://jena.apache.org/prefixes#>
                PREFIX xsd:      <http://www.w3.org/2001/XMLSchema#>
                SELECT ?prefixName WHERE {
                    ?X prefixes:prefixURI ?uriName .
                    ?X prefixes:prefixName ?prefixName
                }
                """);
        pss.setLiteral("uriName", uri, XSDDatatype.XSDanyURI);
        String query = pss.toString();
        return dataset.calculateRead(()->{
            try (QueryExec qExec = QueryExec.dataset(dataset).query(query).build()) {

                RowSet rowSet = qExec.select();
                List<String> prefixes = new ArrayList<String>();

                rowSet.forEachRemaining(row -> {
                    String prefixNameLiteral = row.get("prefixName").getLiteralLexicalForm();
                    if (prefixNameLiteral != null) {
                        prefixes.add(prefixNameLiteral);
                    }
                });

                return prefixes;
            }});
    }
}
