/**
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

package reports;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.query.DataSource ;
import com.hp.hpl.jena.query.DatasetFactory ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.update.UpdateAction ;

public class ReportOracle1
{
    public static void main(String ...argv)
    {
        DataSource ds = DatasetFactory.create();

        //ModelOracleSem model = ModelOracleSem.createOracleSemModel(oracle, szModelName);
        Model model = ModelFactory.createDefaultModel() ;
        model.getGraph().add(Triple.create(Node.createURI("http://example.org/bob"),
                                           Node.createURI("http://purl.org/dc/elements/1.1/publisher"),
                                           Node.createLiteral("Bob Hacker")));
        model.getGraph().add(Triple.create(Node.createURI("http://example.org/alice"),
                                           Node.createURI("http://purl.org/dc/elements/1.1/publisher"),
                                           Node.createLiteral("alice Hacker")));


        //ModelOracleSem model1 = ModelOracleSem.createOracleSemModel(oracle, szModelName+"1");
        Model model1 = ModelFactory.createDefaultModel() ;

        model1.getGraph().add(Triple.create(Node.createURI("urn:bob"),
                                            Node.createURI("http://xmlns.com/foaf/0.1/name"),
                                            Node.createLiteral("Bob")
        ));
        model1.getGraph().add(Triple.create(Node.createURI("urn:bob"),
                                            Node.createURI("http://xmlns.com/foaf/0.1/mbox"),
                                            Node.createURI("mailto:bob@example")
        ));

        //ModelOracleSem model2 = ModelOracleSem.createOracleSemModel(oracle, szModelName+"2");
        Model model2 = ModelFactory.createDefaultModel() ;
        model2.getGraph().add(Triple.create(Node.createURI("urn:alice"),
                                            Node.createURI("http://xmlns.com/foaf/0.1/name"),
                                            Node.createLiteral("Alice")
        ));
        model2.getGraph().add(Triple.create(Node.createURI("urn:alice"),
                                            Node.createURI("http://xmlns.com/foaf/0.1/mbox"),
                                            Node.createURI("mailto:alice@example")
        ));

        ds.setDefaultModel(model);
        //ds.addNamedModel("<http://example.org/bob>",model1);
        ds.addNamedModel("http://example.org/bob",model1);
        // ds.addNamedModel("http://example.org/alice",model2);

        String insertString =
            "INSERT DATA <http://example.org/bob> {<urn:alice> <urn:loves> <urn:apples> } ";
        UpdateAction.parseExecute(insertString, ds); 
        System.out.println("DONE") ;
    }
}
