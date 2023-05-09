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

package org.apache.jena.ontapi;

import org.apache.jena.ontapi.impl.GraphListenerBase;
import org.apache.jena.ontapi.model.OntClass;
import org.apache.jena.ontapi.model.OntModel;
import org.apache.jena.ontapi.vocabulary.OWL;
import org.apache.jena.ontapi.vocabulary.RDF;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class OntModelMiscTest {
    @Test
    public void testRecursionOnComplementOf() {
        // test there is no StackOverflowError
        Assertions.assertThrows(OntJenaException.Recursion.class, () -> {
            Model m = OntModelFactory.createDefaultModel().setNsPrefixes(OntModelFactory.STANDARD);
            Resource anon = m.createResource().addProperty(RDF.type, OWL.Class);
            anon.addProperty(OWL.complementOf, anon);
            OntModel ont = OntModelFactory.createModel(m.getGraph());
            List<OntClass> ces = ont.ontObjects(OntClass.class).toList();
            Assertions.assertEquals(0, ces.size());
        });
    }

    @Test
    public void testCheckCreate() {
        OntModel m = OntModelFactory.createModel();
        List<Triple> triples = new ArrayList<>();
        m.getGraph().getEventManager().register(new GraphListenerBase() {
            @Override
            protected void addTripleEvent(Graph g, Triple t) {
                triples.add(t);
            }

            @Override
            protected void deleteTripleEvent(Graph g, Triple t) {
                Assertions.fail();
            }
        });
        m.createObjectUnionOf(m.getOWLThing());
        Assertions.assertEquals(4, triples.size());

        UnionGraph ug = (UnionGraph) m.getGraph();
        Assertions.assertEquals(0L, ug.superGraphs().count());
    }
}
