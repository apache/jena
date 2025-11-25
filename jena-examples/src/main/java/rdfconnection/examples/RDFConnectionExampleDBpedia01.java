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

package rdfconnection.examples;

import java.util.List;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionRemote;
import org.apache.jena.riot.WebContent;
import org.apache.jena.sparql.resultset.ResultsFormat;

/**
 * Example that showcases querying DBpedia with disabled parse check and
 * different accept headers.
 * <p>
 *
 * The expected output should be similar to this snippet:
 * <pre>
 * Trying to query with content type: application/sparql-results+thrift
 * Request failed: Not Acceptable
 *
 * Trying to query with content type: application/sparql-results+json
 * -------------------------
 * | l                     |
 * =========================
 * | "Jena (Framework)"@de |
 * -------------------------
 * </pre>
 */
public class RDFConnectionExampleDBpedia01 {
    public static void main(String[] args) {
        List<String> acceptHeaders = List.of(WebContent.contentTypeResultsThrift, WebContent.contentTypeResultsJSON);

        // Note that the query string uses the prefix 'rdfs:' without declaring it.
        // The DBpedia server can parse such a query against its configured default prefixes.
        String queryString = "SELECT * { <http://dbpedia.org/resource/Apache_Jena> rdfs:label ?l } ORDER BY ?l LIMIT 1";

        for (String acceptHeader : acceptHeaders) {
            System.out.println("Trying to query with content type: " + acceptHeader);
            try (RDFConnection conn = RDFConnectionRemote.service("http://dbpedia.org/sparql")
                    .acceptHeaderSelectQuery(acceptHeader).parseCheckSPARQL(false).build()) {
                try (QueryExecution qe = conn.query(queryString)) {
                    ResultSet rs = qe.execSelect();
                    ResultSetFormatter.output(System.out, rs, ResultsFormat.TEXT);
                } catch (Exception e) {
                    System.out.println("Request failed: " + e.getMessage());
                }
            }
            System.out.println();
        }

        System.out.println("Done.");
    }
}
