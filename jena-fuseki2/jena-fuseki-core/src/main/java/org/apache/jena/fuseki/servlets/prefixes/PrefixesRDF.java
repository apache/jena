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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.jena.query.*;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Transactional;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.exec.UpdateExec;
import org.apache.jena.tdb2.DatabaseMgr;

/**
 * The prefix-URI mappings are represented in blank nodes of type Prefix, with prefixName and prefixURI attributes.
 * The prefixName and prefixURI of the same node are a distinct prefix-URI mapping.
 *
 * PREFIX prefixes: <http://jena.apache.org/prefixes#>
 * PREFIX rdf:      <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
 * PREFIX xsd:      <http://www.w3.org/2001/XMLSchema#>
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
 */

public class PrefixesRDF implements PrefixesAccess {

    DatasetGraph dataset = DatabaseMgr.createDatasetGraph();

    public Transactional transactional() { return dataset; }

    @Override
    public Optional<String> fetchURI(String prefix) {

        String query = "PREFIX prefixes: <http://jena.apache.org/prefixes#> " +
                "SELECT ?prefixURI WHERE { " +
                "?X prefixes:prefixName \"" + prefix + "\" . " +
                "?X prefixes:prefixURI ?prefixURI " +
                "}";

        try (QueryExec qExec = QueryExec.dataset(dataset).query(query).build()) {

            RowSet rowSet = qExec.select();
            List<String> x = new ArrayList<String>();

            rowSet.forEachRemaining(row -> {
                String prefixNameLiteral = row.get("prefixURI").getLiteralLexicalForm();
                if (prefixNameLiteral != null) {
                    x.add(prefixNameLiteral);
                }
            });
            if (x.isEmpty())
                return Optional.empty();
            //originally x.getFirst()
            return Optional.ofNullable(x.get(0));
        }
    }

    @Override
    public void updatePrefix(String prefix, String uri) {

        String query =
                "PREFIX prefixes: <http://jena.apache.org/prefixes#>\n" +
                "PREFIX xsd:      <http://www.w3.org/2001/XMLSchema#>\n" +
                "ASK WHERE {  ?X prefixes:prefixName  \"" + prefix + "\" ; \n" +
                "                prefixes:prefixURI   ?prefixURI . }";
        try (QueryExec qExec = QueryExec.dataset(dataset).query(query).build()) {

            AtomicBoolean result = new AtomicBoolean(false);
            dataset.executeRead(()->{
                result.set(qExec.ask());
            });

            String update;
            if (result.get())
                update =
                        "PREFIX prefixes: <http://jena.apache.org/prefixes#>\n" +
                        "PREFIX xsd:      <http://www.w3.org/2001/XMLSchema#>\n" +
                        "DELETE { ?X  prefixes:prefixURI ?oldURI }\n" +
                        "INSERT { ?X  prefixes:prefixURI \"" + uri + "\"^^xsd:anyURI }\n" +
                        "WHERE {\n" +
                        "  ?X prefixes:prefixName  \"" + prefix + "\" ;\n" +
                        "     prefixes:prefixURI   ?oldURI \n" +
                        "}\n";
            else
                update =
                        "PREFIX prefixes: <http://jena.apache.org/prefixes#>\n" +
                        "PREFIX xsd:      <http://www.w3.org/2001/XMLSchema#>\n" +
                        "PREFIX rdf:      <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                        "INSERT DATA {\n" +
                        "[] rdf:type prefixes:Prefix ;\n" +
                        "   prefixes:prefixName  \"" + prefix + "\" ;\n" +
                        "   prefixes:prefixURI   \"" + uri + "\"^^xsd:anyURI \n" +
                        "}";
            UpdateExec.dataset(dataset).update(update).execute();
        }
        catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removePrefix(String prefixToRemove) {
        String update = "PREFIX prefixes: <http://jena.apache.org/prefixes#>\n" +
                        "DELETE WHERE {?X  prefixes:prefixName \"" + prefixToRemove + "\" }\n" +
                        "\n";
        UpdateExec.dataset(dataset).update(update).execute();
    }

    @Override
    public Map<String, String> getAll() {
        String query = "PREFIX prefixes: <http://jena.apache.org/prefixes#>\n" +
                       "SELECT ?prefixName ?prefixURI WHERE { " +
                       "?X prefixes:prefixName ?prefixName ." +
                       "?X prefixes:prefixURI ?prefixURI " +
                       "}";

        try (QueryExec qExec = QueryExec.dataset(dataset).query(query).build()) {

            RowSet rowSet = qExec.select();
            Map<String, String> allPairs = new ConcurrentHashMap<>();
            rowSet.forEachRemaining(row -> {
                String prefixNameLiteral = row.get("prefixName").getLiteralLexicalForm();
                String prefixURILiteral = row.get("prefixURI").getLiteralLexicalForm();
                if (prefixNameLiteral != null && prefixURILiteral != null) {
                    allPairs.put(prefixNameLiteral, prefixURILiteral);
                }
            });
            return allPairs;
        }
    }

    @Override
    public List<String> fetchPrefix(String uri) {
        String query = "PREFIX prefixes: <http://jena.apache.org/prefixes#>\n" +
                       "PREFIX xsd:      <http://www.w3.org/2001/XMLSchema#>\n" +
                       "SELECT ?prefixName WHERE { " +
                       "    ?X prefixes:prefixURI \"" + uri + "\"^^xsd:anyURI . \n" +
                       "    ?X prefixes:prefixName ?prefixName \n" +
                       "}";
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
        }
    }
}
