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

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.ontapi.impl.GraphListenerBase;
import org.apache.jena.ontapi.model.OntClass;
import org.apache.jena.ontapi.model.OntModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDF;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class OntModelMiscTest {
    @Test
    public void testRecursionOnComplementOf() {
        // test there is no StackOverflowError
        Assertions.assertThrows(OntJenaException.Recursion.class, () -> {
            Model m = OntModelFactory.createDefaultModel().setNsPrefixes(OntModelFactory.STANDARD);
            Resource anon = m.createResource().addProperty(RDF.type, OWL2.Class);
            anon.addProperty(OWL2.complementOf, anon);
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

    @Test
    public void testWriteAll() {
        OntModel a = OntModelFactory.createModel();
        a.createOntClass("A");
        OntModel b = OntModelFactory.createModel().setID("http://ont#B").getModel();
        b.createOntClass("B");
        a.addImport(b);

        ByteArrayOutputStream res1 = new ByteArrayOutputStream();
        a.writeAll(res1, "ttl");
        Model c = ModelFactory.createDefaultModel();
        c.read(new ByteArrayInputStream(res1.toByteArray()), "http://ex1#", "ttl");

        Assertions.assertEquals(5, c.size());
        Assertions.assertTrue(c.contains(c.createResource("http://ex1/A"), RDF.type, OWL2.Class));
        Assertions.assertTrue(c.contains(c.createResource("http://ex1/B"), RDF.type, OWL2.Class));

        ByteArrayOutputStream res2 = new ByteArrayOutputStream();
        a.writeAll(res2, "ttl", "http://ex2#");
        Model d = ModelFactory.createDefaultModel();
        d.read(new ByteArrayInputStream(res2.toByteArray()), "http://ex1#", "ttl");

        Assertions.assertEquals(5, d.size());
        Assertions.assertTrue(d.contains(d.createResource("http://ex2/A"), RDF.type, OWL2.Class));
        Assertions.assertTrue(d.contains(d.createResource("http://ex2/B"), RDF.type, OWL2.Class));
    }

}
