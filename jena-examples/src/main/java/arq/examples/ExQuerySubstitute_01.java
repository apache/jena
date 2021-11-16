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

package arq.examples;

import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.update.UpdateExecution;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;

public class ExQuerySubstitute_01 {

    private static String prefixes = StrUtils.strjoinNL
            ("PREFIX rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
            ,"PREFIX rdfs:    <http://www.w3.org/2000/01/rdf-schema#>"
            ,"PREFIX xsd:     <http://www.w3.org/2001/XMLSchema#>"
            ,"PREFIX foaf:    <http://xmlns.com/foaf/0.1/>"
            ,""
            );


    public static void main(String...args) {
        Dataset dataset = DatasetFactory.createTxnMem();
        dataset.getPrefixMapping().setNsPrefix("foaf", "http://xmlns.com/foaf/0.1/");

        addPerson(dataset, "http://example/carl_segan", "Carl Segan");
        addPerson(dataset, "http://example/richard_feyman", "Richard Feynman");

        System.out.println("-- Data");
        RDFDataMgr.write(System.out, dataset, Lang.TRIG);
        System.out.println("--");

        Literal name1 = ResourceFactory.createPlainLiteral("Carl Segan");
        Literal name2 = ResourceFactory.createPlainLiteral("Richard Feynman");

        ResultSet resultSet1 = QueryExecution.dataset(dataset)
                .query(prefixes+"SELECT * { ?person foaf:name ?name }")
                .substitution("name", name1)
                .select();
        ResultSetFormatter.out(resultSet1);

        // To return the values used for variable substitution, include the variable in the SELECT clause.
        ResultSet resultSet2 = QueryExecution.dataset(dataset)
                .query(prefixes+"SELECT ?person ?name { ?person foaf:name ?name }")
                .substitution("name", name2)
                .select();
        ResultSetFormatter.out(resultSet2);
    }

    private static void addPerson(Dataset dataset, String uri, String name) {
        UpdateRequest update = UpdateFactory.create(prefixes+"INSERT { ?person foaf:name ?name } WHERE {}");
        Resource person = ResourceFactory.createResource(uri);
        Literal literal = ResourceFactory.createPlainLiteral(name);

        UpdateExecution.dataset(dataset)
            .update(update)
            .substitution("person", person)
            .substitution("name", literal)
            .execute();
    }
}
