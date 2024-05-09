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

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.ontapi.model.OntClass;
import org.apache.jena.ontapi.model.OntID;
import org.apache.jena.ontapi.model.OntModel;
import org.apache.jena.ontapi.model.OntObject;
import org.apache.jena.ontapi.model.OntStatement;
import org.apache.jena.ontapi.testutils.ModelTestUtils;
import org.apache.jena.ontapi.utils.Iterators;
import org.apache.jena.ontapi.utils.OntModels;
import org.apache.jena.ontapi.utils.StdModels;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * To test {@link StdModels} and {@link OntModels} utilities.
 */
public class ModelUtilsTest {
    @SuppressWarnings("SameParameterValue")
    private static void testStatementsComparator(Model m, Comparator<Statement> comp) {
        List<Statement> first = Iterators.asStream(m.listStatements()).sorted(comp).collect(Collectors.toList());
        List<Statement> tmp;
        Collections.shuffle(tmp = m.listStatements().toList());
        List<Statement> second = tmp.stream().sorted(comp).collect(Collectors.toList());
        Assertions.assertEquals(first, second);
    }

    @Test
    public void testDeleteResources() {
        OntModel m = OntModelFactory.createModel()
                .read(ModelUtilsTest.class.getResourceAsStream("/with-recursion.ttl"), null, "ttl");
        String ns = m.getID().getURI() + "#";
        OntObject d = m.createDisjointClasses(
                m.createOntClass(ns + "CL1"),
                m.createOntClass(ns + "CL2"),
                m.createObjectUnionOf(Arrays.asList(
                        m.createOntClass(ns + "CL4"),
                        m.createOntClass(ns + "CL5"),
                        m.createOntClass(ns + "CL6"))),
                m.createOntClass(ns + "CL3"));


        Assertions.assertEquals(40, m.localStatements().count());

        Resource r = m.statements(null, RDFS.subClassOf, null)
                .map(Statement::getObject)
                .filter(RDFNode::isAnon)
                .map(RDFNode::asResource)
                .filter(s -> s.hasProperty(OWL2.someValuesFrom))
                .findFirst()
                .orElseThrow(IllegalStateException::new);

        ModelTestUtils.deleteAll(r);
        ModelTestUtils.deleteAll(d);
        List<OntClass> classes = m.classes()
                .filter(s -> s.getLocalName().contains("CL"))
                .collect(Collectors.toList());
        classes.forEach(ModelTestUtils::deleteAll);

        Assertions.assertEquals(10, m.statements().count());
    }

    @Test
    public void testListLangValues() {
        OntModel m = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD);
        OntID id = m.getID()
                .addVersionInfo("lab1")
                .addVersionInfo("lab1", "e1")
                .addVersionInfo("lab2", "e2")
                .addVersionInfo("lab3", "language3")
                .addVersionInfo("lab4", "e2")
                .addVersionInfo("lab5", "e2")
                .addVersionInfo("lab5");

        Property p = OWL2.versionInfo;
        Assertions.assertEquals(2, ModelTestUtils.langValues(id, p, null).count());
        Assertions.assertEquals(3, ModelTestUtils.langValues(id, p, "e2").count());
        Assertions.assertEquals(1, ModelTestUtils.langValues(id, p, "language3").count());
        Assertions.assertEquals(7, m.listObjectsOfProperty(id, p).toSet().size());
    }

    @Test
    public void testMiscModelsFunctionality() {
        OntModel m = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD);
        OntClass a = m.createOntClass("A");
        OntClass b = m.createOntClass("B");
        Resource t = m.getResource("type");
        RDFList list = StdModels.createTypedList(m, t, Arrays.asList(a, b));
        Assertions.assertNotNull(list);

        Assertions.assertEquals(8, m.size());
        Assertions.assertEquals(2, m.listStatements(null, RDF.type, t).toList().size());

        Assertions.assertTrue(StdModels.isInList(m, a));
        Assertions.assertTrue(StdModels.isInList(m, b));

        Assertions.assertEquals(6, Iterators.peek(ModelTestUtils.listDescendingStatements(list),
                s -> Assertions.assertTrue(RDF.type.equals(s.getPredicate()) || StdModels.isInList(s))).toList().size());

        Assertions.assertEquals(2, ModelTestUtils.subjects(t).count());
        Assertions.assertEquals(2, ModelTestUtils.listAscendingStatements(RDF.nil.inModel(m)).toList().size());
    }

    @Test
    public void testStatementsComparators() {
        OntModel m = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD);
        OntClass a = m.createOntClass("A");
        OntClass b = m.createOntClass("B");
        m.createObjectSomeValuesFrom(m.createObjectProperty("P"), m.createObjectComplementOf(m.createObjectUnionOf(a, b)));

        testStatementsComparator(m, StdModels.STATEMENT_COMPARATOR);
    }

    @Test
    public void testTripleToStatement() {
        OntModel m = OntModelFactory.createModel();
        m.createDisjointClasses(m.createOntClass("X1").addComment("A1"))
                .addComment("A2")
                .addComment("A3");

        Triple t1 = Iterators.findFirst(m.getBaseGraph().find(Node.ANY, RDF.type.asNode(), OWL2.AllDisjointClasses.asNode()))
                .orElseThrow(AssertionError::new);
        OntStatement s1 = OntModels.toOntStatement(t1, m);
        Assertions.assertEquals(2, s1.annotations().count());

        Triple t2 = Iterators.findFirst(m.getBaseGraph().find(Node.ANY, RDF.type.asNode(), OWL2.Class.asNode()))
                .orElseThrow(AssertionError::new);
        OntStatement s2 = OntModels.toOntStatement(t2, m);
        Assertions.assertEquals(1, s2.annotations().count());
    }
}
