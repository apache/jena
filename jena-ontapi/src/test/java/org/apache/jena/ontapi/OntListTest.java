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

import org.apache.jena.graph.GraphMemFactory;
import org.apache.jena.ontapi.impl.OntGraphModelImpl;
import org.apache.jena.ontapi.model.OntAnnotationProperty;
import org.apache.jena.ontapi.model.OntClass;
import org.apache.jena.ontapi.model.OntDataProperty;
import org.apache.jena.ontapi.model.OntDisjoint;
import org.apache.jena.ontapi.model.OntList;
import org.apache.jena.ontapi.model.OntModel;
import org.apache.jena.ontapi.model.OntObject;
import org.apache.jena.ontapi.model.OntObjectProperty;
import org.apache.jena.ontapi.model.OntProperty;
import org.apache.jena.ontapi.model.OntRelationalProperty;
import org.apache.jena.ontapi.model.OntStatement;
import org.apache.jena.ontapi.testutils.ModelTestUtils;
import org.apache.jena.ontapi.utils.Iterators;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.util.iterator.NullIterator;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class OntListTest {

    private static OntStatement getSingleAnnotation(OntList<?> list) {
        return getSingleAnnotation(list.getMainStatement());
    }

    private static OntStatement getSingleAnnotation(OntStatement s) {
        List<OntStatement> res = s.annotations().toList();
        Assertions.assertEquals(1, res.size());
        return res.get(0);
    }

    private static void check(OntModel m, int numLists, Class<? extends RDFNode> type) {
        Assertions.assertFalse(m.contains(null, RDF.type, RDF.List));
        Assertions.assertEquals(numLists, m.statements(null, null, RDF.nil).count());
        m.statements(null, RDF.first, null).map(Statement::getObject).forEach(n -> Assertions.assertTrue(n.canAs(type)));
        m.statements(null, RDF.rest, null)
                .map(Statement::getObject)
                .forEach(n -> Assertions.assertTrue(RDF.nil.equals(n) ||
                        (n.isAnon() && m.statements().map(OntStatement::getSubject).anyMatch(n::equals))));
    }

    @Test
    public void testCommonFunctionality1() {
        OntModel m = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD);
        OntObjectProperty p1 = m.createObjectProperty("p1");
        OntObjectProperty p2 = m.createObjectProperty("p2");
        OntObjectProperty p3 = m.createObjectProperty("p3");
        p1.addPropertyChainAxiomStatement();
        check(m, 1, OntObjectProperty.Named.class);

        OntList<OntObjectProperty> list = p2.createPropertyChain(Collections.emptyList());
        Assertions.assertTrue(list.canAs(RDFList.class));
        Assertions.assertEquals(3, list.add(p3).add(p3).add(p1).members().count());
        Assertions.assertEquals(3, list.members().count());
        Assertions.assertEquals(3, list.as(RDFList.class).size());
        Assertions.assertEquals(1, p2.propertyChains().count());
        Assertions.assertEquals(0, p3.propertyChains().count());
        Assertions.assertEquals(2, m.objectProperties().flatMap(OntObjectProperty::propertyChains).count());
        Assertions.assertTrue(list.contains(p3));
        Assertions.assertFalse(list.contains(p2));
        check(m, 2, OntObjectProperty.Named.class);

        list.remove();
        Assertions.assertEquals(2, list.members().count());
        Assertions.assertEquals(2, list.as(RDFList.class).size());
        Assertions.assertFalse(list.isNil());
        Assertions.assertFalse(list.members().anyMatch(p -> p.equals(p1)));
        Assertions.assertFalse(list.contains(p1));
        Assertions.assertEquals(p3, list.last().orElseThrow(AssertionError::new));
        Assertions.assertEquals(p3, list.first().orElseThrow(AssertionError::new));
        check(m, 2, OntObjectProperty.Named.class);

        Assertions.assertEquals(1, (list = list.remove()).members().count());
        Assertions.assertEquals(1, list.as(RDFList.class).size());
        Assertions.assertFalse(list.isNil());
        check(m, 2, OntObjectProperty.class);

        list = list.remove();
        Assertions.assertEquals(0, list.members().count());
        Assertions.assertEquals(0, list.as(RDFList.class).size());
        Assertions.assertTrue(list.isNil());
        Assertions.assertFalse(list.contains(p1));
        Assertions.assertFalse(list.contains(p2));
        Assertions.assertFalse(list.contains(p3));
        check(m, 2, OntObjectProperty.class);
    }

    @Test
    public void testCommonFunctionality2() {
        OntModel m = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD);
        OntObjectProperty p1 = m.createObjectProperty("p1");
        OntObjectProperty p2 = m.createObjectProperty("p2");
        OntObjectProperty p3 = m.createObjectProperty("p3");
        OntObjectProperty p4 = m.createObjectProperty("p4");
        p1.createPropertyChain(Collections.singletonList(p2)).add(p3);
        check(m, 1, OntObjectProperty.class);

        Assertions.assertEquals(1, p1.propertyChains().count());
        OntList<OntObjectProperty> list = p1.propertyChains().findFirst().orElseThrow(AssertionError::new);
        Assertions.assertEquals(3, list.addFirst(p4).members().count());
        Assertions.assertTrue(list.first().filter(p4::equals).isPresent());
        Assertions.assertTrue(list.last().filter(p3::equals).isPresent());
        check(m, 1, OntObjectProperty.class);

        Assertions.assertEquals(1, p1.propertyChains().count());
        Assertions.assertEquals(2, list.removeFirst().members().count());
        Assertions.assertTrue(list.first().filter(p2::equals).isPresent());
        Assertions.assertTrue(list.last().filter(p3::equals).isPresent());
        check(m, 1, OntObjectProperty.Named.class);

        Assertions.assertTrue(list.removeFirst().removeFirst().isNil());
        check(m, 1, OntProperty.class);
        Assertions.assertEquals(1, list.addFirst(p4).members().count());
        Assertions.assertTrue(list.first().filter(p4::equals).isPresent());
        Assertions.assertEquals(1, p1.propertyChains().count());
        list = p1.propertyChains().findFirst().orElseThrow(AssertionError::new);
        Assertions.assertEquals(1, list.members().count());
        Assertions.assertTrue(list.last().filter(p4::equals).isPresent());
        check(m, 1, OntObjectProperty.class);

        Assertions.assertEquals(3, p1.propertyChains().findFirst().orElseThrow(AssertionError::new).addLast(p3).addFirst(p2).size());
        check(m, 1, OntObjectProperty.Named.class);
        list = p1.propertyChains().findFirst().orElseThrow(AssertionError::new);
        Assertions.assertEquals(3, list.size());
        list.removeLast().removeLast();
        Assertions.assertEquals(1, p1.propertyChains().findFirst().orElseThrow(AssertionError::new).size());
        Assertions.assertEquals(1, list.members().count());

        list.clear();
        Assertions.assertEquals(0, list.members().count());
        Assertions.assertTrue(p1.propertyChains().findFirst().orElseThrow(AssertionError::new).isNil());
        Assertions.assertEquals(0, list.members().count());
        Assertions.assertEquals(3, list.addLast(p2).addFirst(p4).addFirst(p3).size());
        Assertions.assertEquals(Arrays.asList(p3, p4, p2), list.as(RDFList.class).asJavaList());
    }

    @Test
    public void testGetAndClear1() {
        OntModel m = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD);
        OntObjectProperty p1 = m.createObjectProperty("p1");
        OntObjectProperty p2 = m.createObjectProperty("p2");
        OntObjectProperty p3 = m.createObjectProperty("p3");
        OntObjectProperty p4 = m.createObjectProperty("p4");

        OntList<OntObjectProperty> list = p1.createPropertyChain(Arrays.asList(p2, p3)).add(p4);
        check(m, 1, OntObjectProperty.class);

        Assertions.assertEquals(3, list.get(0).size());
        Assertions.assertEquals(2, list.get(1).size());
        Assertions.assertEquals(1, list.get(2).size());
        Assertions.assertEquals(0, list.get(3).size());
        try {
            OntList<OntObjectProperty> n = list.get(4);
            Assertions.fail("Found out of bound list: " + n);
        } catch (OntJenaException.IllegalArgument j) {
            // expected
        }

        Assertions.assertTrue(list.get(2).clear().isNil());
        check(m, 1, OntObjectProperty.class);
        Assertions.assertEquals(2, list.size());
    }

    @Test
    public void testGetAndClear2() {
        OntModel m = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD);
        OntObjectProperty p1 = m.createObjectProperty("p1");
        OntObjectProperty p2 = m.createObjectProperty("p2");
        OntObjectProperty p3 = m.createObjectProperty("p3");
        OntObjectProperty p4 = m.createObjectProperty("p4");

        OntList<OntObjectProperty> list = p1.createPropertyChain(Collections.emptyList()).add(p2).add(p3).add(p4);
        check(m, 1, OntObjectProperty.class);
        Assertions.assertEquals(2, list.get(2).addFirst(p2).get(1).addLast(p2).size());
        check(m, 1, OntObjectProperty.class);
        // p2, p3, p2, p4, p2
        Assertions.assertEquals(Arrays.asList(p2, p3, p2, p4, p2), list.as(RDFList.class).asJavaList());
        // link expired:
        p1.propertyChains().findFirst().orElseThrow(AssertionError::new).clear();
        try {
            list.size();
            Assertions.fail("Possible to work with expired ont-list instance");
        } catch (OntJenaException.IllegalState j) {
            // expected
        }
    }

    @Test
    public void testMixedList() {
        OntModel m = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD);
        OntObjectProperty p1 = m.createObjectProperty("p1");
        OntObjectProperty p2 = m.createObjectProperty("p2");
        OntObjectProperty p3 = m.createObjectProperty("p3");
        OntObjectProperty p4 = m.createObjectProperty("p4");
        OntList<OntObjectProperty> list = p1.createPropertyChain(Arrays.asList(p4, p3, p2));
        list.get(1).as(RDFList.class).replace(0, m.createTypedLiteral("Not a property"));
        check(m, 1, RDFNode.class);
        Assertions.assertEquals(3, list.size());
        try {
            long c = list.members().count();
            Assertions.fail("Possible to get members count for expired ont-list: " + c);
        } catch (OntJenaException j) {
            // expected
        }
        list = p1.propertyChains().findFirst().orElseThrow(AssertionError::new);
        Assertions.assertEquals(3, list.size());
        Assertions.assertEquals(2, list.members().count());
        Assertions.assertEquals(3, list.addFirst(p3).members().count());
        Assertions.assertTrue(list.contains(p3));
        Assertions.assertEquals(4, list.size());
        Assertions.assertEquals(2, list.get(1).members().count());
        Assertions.assertEquals(p3, list.first().orElseThrow(AssertionError::new));
        Assertions.assertEquals(p2, list.last().orElseThrow(AssertionError::new));
    }

    @Test
    public void testListAnnotations() {
        OntModel m = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD);
        OntObjectProperty p1 = m.createObjectProperty("p1");
        OntObjectProperty p2 = m.createObjectProperty("p2");
        OntObjectProperty p3 = m.createObjectProperty("p3");
        OntObjectProperty p4 = m.createObjectProperty("p4");
        OntAnnotationProperty p5 = m.createAnnotationProperty("p5");
        Literal literal_x = m.createLiteral("x");
        Literal literal_y = m.createLiteral("y", "y");
        Literal literal_z = m.createTypedLiteral(2.2);
        Assertions.assertEquals(XSD.xdouble.getURI(), literal_z.getDatatypeURI());

        p1.addPropertyChainAxiomStatement(p4, p4, p3, p2).annotate(m.getRDFSLabel(), literal_x);
        OntList<OntObjectProperty> list = p1.propertyChains().findFirst().orElseThrow(AssertionError::new);
        Assertions.assertEquals(literal_x, getSingleAnnotation(list).getLiteral());
        list.clear();
        Assertions.assertEquals(literal_x, getSingleAnnotation(list).getLiteral());
        Assertions.assertEquals(0, list.size());
        list.addLast(p2).addFirst(p3);
        list.last().filter(p2::equals).orElseThrow(AssertionError::new);
        list.first().filter(p3::equals).orElseThrow(AssertionError::new);
        Assertions.assertEquals(literal_x, getSingleAnnotation(list).getLiteral());
        Assertions.assertEquals(2, list.size());
        try {
            list.get(1).getMainStatement().annotate(m.getRDFSLabel(), literal_z);
            Assertions.fail("Possible to annotate sub-lists");
        } catch (OntJenaException.IllegalCall j) {
            // expected
        }

        getSingleAnnotation(list).annotate(m.getRDFSLabel(), literal_y);
        list.removeFirst();
        Assertions.assertEquals(1, list.size());
        Assertions.assertEquals(literal_x, getSingleAnnotation(list).getLiteral());
        Assertions.assertEquals(literal_y, getSingleAnnotation(getSingleAnnotation(list)).getLiteral());
        list.getMainStatement().annotate(p5, literal_z);
        Assertions.assertEquals(2, list.getMainStatement().annotations().count());
        list.remove();
        Assertions.assertEquals(2, list.getMainStatement().annotations().count());
        list.getMainStatement().annotations()
                .filter(s -> p5.equals(s.getPredicate()) && literal_z.equals(s.getLiteral()))
                .findFirst().orElseThrow(AssertionError::new);
        list.getMainStatement().annotations()
                .filter(s -> RDFS.label.equals(s.getPredicate()) && literal_x.equals(s.getLiteral()))
                .findFirst().orElseThrow(AssertionError::new);
        Assertions.assertTrue(list.isNil());
        Assertions.assertNotNull(list.getMainStatement().clearAnnotations());
        Assertions.assertEquals(0, list.getMainStatement().annotations().count());
        Assertions.assertEquals(6, m.statements().count());
    }

    @Test
    public void testListSpec() {
        OntModel m = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD);
        OntObjectProperty p1 = m.createObjectProperty("p1");
        OntObjectProperty p2 = m.createObjectProperty("p2");
        OntObjectProperty p3 = m.createObjectProperty("p3");
        OntObjectProperty p4 = m.createObjectProperty("p4");
        OntList<OntObjectProperty> list = p1.createPropertyChain(Collections.emptyList());
        Assertions.assertEquals(0, list.spec().count());

        list.add(p2).add(p3).add(p4);
        Assertions.assertEquals(6, list.spec().count());
        Set<Statement> set = ModelTestUtils.getAssociatedStatements(m.listStatements(null, OWL2.propertyChainAxiom, (RDFNode) null)
                .mapWith(Statement::getObject).mapWith(RDFNode::asResource).toList().get(0));
        Assertions.assertEquals(set, list.spec().collect(Collectors.toSet()));

        list.getMainStatement().addAnnotation(m.getRDFSComment(), "The list", "xx").addAnnotation(m.getRDFSLabel(), "test");
        Assertions.assertEquals(6, list.spec().count());

        // check that spec elements cannot be annotated
        try {
            list.spec().skip(3).limit(1)
                    .findFirst().orElseThrow(AssertionError::new).addAnnotation(m.getRDFSComment(), "Is it possible?");
            Assertions.fail("Possible to annotate some rdf:List statement");
        } catch (OntJenaException j) {
            // expected
        }

        list.clear();
        Assertions.assertEquals(0, list.spec().count());
    }

    @Test
    public void testTypedList() {
        OntGraphModelImpl m = new OntGraphModelImpl(
                OntModelFactory.createUnionGraph(GraphMemFactory.createGraphMem()),
                OntSpecification.OWL2_DL_MEM_BUILTIN_RDFS_INF.getPersonality()
        );
        m.setNsPrefixes(OntModelFactory.STANDARD);
        Resource a = m.createResource("A");
        Resource b = m.createResource("B");
        Literal c = m.createLiteral("C");
        Resource d = m.createResource("D");
        Literal e = m.createLiteral("E");

        OntObject s = m.createResource("list").as(OntObject.class);
        Property p = m.createProperty("of");
        OntList<RDFNode> list = m.createOntList(s, p, RDF.List, RDFNode.class, Iterators.of(a, b, c, d, e));

        Assertions.assertEquals(RDF.List, list.type().orElseThrow(AssertionError::new));
        Assertions.assertEquals(16, m.size());
        Assertions.assertEquals(15, list.spec().count());
        Assertions.assertEquals(16, list.content().count());
        Assertions.assertEquals(5, list.members().count());

        OntList<Resource> tmp1 = m.asOntList(list.as(RDFList.class), s, p, false, RDF.List, Resource.class);
        Assertions.assertEquals(RDF.List, tmp1.type().orElseThrow(AssertionError::new));
        Assertions.assertEquals(15, tmp1.spec().count());
        Assertions.assertEquals(3, tmp1.members().count());
        OntList<Literal> tmp2 = m.asOntList(list.as(RDFList.class), s, p, false, RDF.List, Literal.class);
        Assertions.assertEquals(RDF.List, tmp2.type().orElseThrow(AssertionError::new));
        Assertions.assertEquals(15, tmp2.spec().count());
        Assertions.assertEquals(2, tmp2.members().count());

        list.removeLast().removeFirst();

        Assertions.assertEquals(10, m.size());
        Assertions.assertEquals(9, list.spec().count());

        list.addLast(m.createResource("X")).addFirst(m.createLiteral("Y"));

        Assertions.assertEquals(15, list.spec().count());
        Assertions.assertEquals(Arrays.asList("Y", "B", "C", "D", "X"),
                list.members().map(String::valueOf).collect(Collectors.toList()));
        Assertions.assertTrue(list.contains(m.createResource("X")));
        Assertions.assertFalse(list.contains(m.createLiteral("X")));
        Assertions.assertFalse(list.contains(m.createResource("Y")));
        Assertions.assertTrue(list.contains(m.createLiteral("Y")));

        Assertions.assertEquals(2, list.get(2).removeFirst()
                .addFirst(m.createResource("Z")).get(1)
                .removeFirst().addLast(m.createLiteral("F")).members().count());

        Assertions.assertEquals(Arrays.asList("Y", "B", "Z", "X", "F"), list.members().map(String::valueOf).collect(Collectors.toList()));
        Assertions.assertEquals(16, m.size());
        Assertions.assertEquals(15, list.spec().count());
        Assertions.assertTrue(list.contains(m.createResource("Z")));

        list.clear();
        Assertions.assertTrue(list.isNil());
        Assertions.assertEquals(0, list.size());
        Assertions.assertEquals(1, m.size());


        OntList<Resource> empty = m.createOntList(m.createResource("empty").as(OntObject.class), p, RDF.List,
                Resource.class,
                NullIterator.instance());
        Assertions.assertTrue(empty.isNil());
        Assertions.assertEquals(0, empty.size());
        Assertions.assertEquals(2, m.size());


    }

    @Test
    public void testPropertyChain() {
        OntModel m = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD);
        OntObjectProperty p1 = m.createObjectProperty("p1");
        OntObjectProperty p2 = m.createObjectProperty("p2");
        OntObjectProperty p3 = m.createObjectProperty("p3");
        OntObjectProperty p4 = m.createObjectProperty("p4");
        p1.addPropertyChain(p2, p3).addPropertyChain(p3, p3, p4).addPropertyChain(p4, p4);
        Assertions.assertEquals(3, p1.fromPropertyChain().count());
        Assertions.assertEquals(3, m.objectProperties().flatMap(OntObjectProperty::propertyChains).count());
        OntList<OntObjectProperty> p334 = p1.propertyChains()
                .filter(c -> c.first().filter(p3::equals).isPresent())
                .findFirst()
                .orElseThrow(AssertionError::new);
        Assertions.assertEquals(Arrays.asList(p3, p3, p4), p334.members().collect(Collectors.toList()));
        OntList<OntObjectProperty> p23 = p1.propertyChains()
                .filter(c -> c.last().filter(p3::equals).isPresent())
                .findFirst()
                .orElseThrow(AssertionError::new);
        Assertions.assertEquals(Arrays.asList(p2, p3), p23.members().collect(Collectors.toList()));
        p334.getMainStatement().annotate(m.getRDFSComment(), m.createLiteral("p3, p3, p4"));
        p23.getMainStatement().annotate(m.getRDFSComment(), m.createLiteral("p2, p3"));
        Assertions.assertEquals(2, m.statements(null, RDF.type, OWL2.Axiom).count());
        Assertions.assertSame(p1, p1.removePropertyChain(p334));
        Assertions.assertEquals(2, m.objectProperties().flatMap(OntObjectProperty::propertyChains).count());
        Assertions.assertEquals(1, m.statements(null, RDF.type, OWL2.Axiom).count());
        Assertions.assertSame(p1, p1.clearPropertyChains());
        Assertions.assertEquals(4, m.size());
    }

    @Test
    public void testDisjointUnion() {
        OntModel m = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD);
        OntClass clazz = m.createOntClass("c");
        OntClass ce1, ce3, ce4;
        OntClass ce2 = m.createObjectComplementOf(ce1 = m.createOntClass("c1"));
        OntClass ce5 = m.createObjectUnionOf(ce3 = m.createOntClass("c3"), ce4 = m.createOntClass("c4"));
        Assertions.assertEquals(2, clazz.asNamed().addDisjointUnionOfStatement(ce2, ce3).getObject(RDFList.class).size());
        Assertions.assertEquals(2, clazz.asNamed().addDisjointUnionOfStatement(ce3, ce3, ce4).getObject(RDFList.class).size());
        Assertions.assertEquals(3, clazz.asNamed().addDisjointUnionOfStatement(ce4, ce4, ce5, ce1, ce1)
                .getObject(RDFList.class).size());
        Assertions.assertEquals(3, clazz.asNamed().disjointUnions().count());
        Assertions.assertEquals(3, m.classes().flatMap(OntClass.Named::disjointUnions).count());

        OntList<OntClass> d23 = clazz.asNamed().disjointUnions()
                .filter(c -> c.first().filter(ce2::equals).isPresent())
                .findFirst()
                .orElseThrow(AssertionError::new);
        OntList<OntClass> d34 = clazz.asNamed().disjointUnions()
                .filter(c -> c.last().filter(ce4::equals).isPresent())
                .findFirst()
                .orElseThrow(AssertionError::new);
        OntList<OntClass> d451 = clazz.asNamed().disjointUnions()
                .filter(c -> c.last().filter(ce1::equals).isPresent())
                .findFirst()
                .orElseThrow(AssertionError::new);
        Assertions.assertEquals(Arrays.asList(ce2, ce3), d23.members().collect(Collectors.toList()));
        Assertions.assertEquals(Arrays.asList(ce3, ce4), d34.members().collect(Collectors.toList()));
        Assertions.assertEquals(Arrays.asList(ce4, ce5, ce1), d451.members().collect(Collectors.toList()));

        d451.getMainStatement().addAnnotation(m.getRDFSLabel(), "ce4, ce5, ce1");
        d23.getMainStatement().addAnnotation(m.getRDFSLabel(), "ce2, ce3");
        Assertions.assertEquals(2, m.statements(null, RDF.type, OWL2.Axiom).count());
        clazz.asNamed().removeDisjointUnion(d451);

        Assertions.assertEquals(2, m.classes().flatMap(OntClass.Named::disjointUnions).count());
        Assertions.assertEquals(1, m.statements(null, RDF.type, OWL2.Axiom).count());
        clazz.asNamed().clearDisjointUnions();
        Assertions.assertEquals(12, m.size());
    }

    @Test
    public void testHasKey() {
        OntModel m = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD);
        OntClass clazz = m.createOntClass("c");
        OntObjectProperty p1 = m.createObjectProperty("p1");
        OntObjectProperty p2 = m.createObjectProperty("p2");
        OntDataProperty p3 = m.createDataProperty("p3");
        OntDataProperty p4 = m.createDataProperty("p4");
        OntObjectProperty p5 = m.createObjectProperty("p5").createInverse();

        Assertions.assertEquals(2, clazz.addHasKeyStatement(p2, p3).getObject(RDFList.class).size());
        Assertions.assertEquals(2, clazz.addHasKeyStatement(p3, p3, p4).getObject(RDFList.class).size());
        Assertions.assertEquals(3, clazz.addHasKeyStatement(p4, p4, p5, p1, p1).getObject(RDFList.class).size());
        Assertions.assertEquals(3, clazz.hasKeys().count());
        Assertions.assertEquals(3, m.classes().flatMap(OntClass::hasKeys).count());

        OntList<OntRelationalProperty> h23 = clazz.hasKeys()
                .filter(c -> c.first().filter(p2::equals).isPresent())
                .findFirst()
                .orElseThrow(AssertionError::new);
        OntList<OntRelationalProperty> h34 = clazz.hasKeys()
                .filter(c -> c.last().filter(p4::equals).isPresent())
                .findFirst()
                .orElseThrow(AssertionError::new);
        OntList<OntRelationalProperty> h451 = clazz.hasKeys()
                .filter(c -> c.last().filter(p1::equals).isPresent())
                .findFirst()
                .orElseThrow(AssertionError::new);
        Assertions.assertEquals(List.of(p2, p3), h23.members().collect(Collectors.toList()));
        Assertions.assertEquals(List.of(p3, p4), h34.members().collect(Collectors.toList()));
        Assertions.assertEquals(List.of(p4, p5, p1), h451.members().collect(Collectors.toList()));

        h451.getMainStatement().addAnnotation(m.getRDFSComment(), "p4, p5, p1");
        h23.getMainStatement().addAnnotation(m.getRDFSComment(), "p2, p3");
        Assertions.assertEquals(2, m.statements(null, RDF.type, OWL2.Axiom).count());
        Assertions.assertSame(clazz, clazz.removeHasKey(h451));

        Assertions.assertEquals(2, m.classes().flatMap(OntClass::hasKeys).count());
        Assertions.assertEquals(1, m.statements(null, RDF.type, OWL2.Axiom).count());
        Assertions.assertSame(clazz, clazz.clearHasKeys());
        Assertions.assertEquals(7, m.size());
    }

    @Test
    public void testDisjointPropertiesOntList() {
        OntModel m = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD);
        OntObjectProperty p1 = m.createObjectProperty("p1");
        OntObjectProperty p2 = m.createObjectProperty("p2");
        OntObjectProperty p3 = m.createObjectProperty("p3");
        OntObjectProperty p4 = m.createObjectProperty("p4");

        OntDataProperty p5 = m.createDataProperty("p5");
        OntDataProperty p6 = m.createDataProperty("p6");
        OntDataProperty p7 = m.createDataProperty("p7");

        OntDisjoint.ObjectProperties d1 = m.createDisjointObjectProperties(p1, p2);
        OntDisjoint.DataProperties d2 = m.createDisjointDataProperties(p5, p7);

        Assertions.assertEquals(2, m.ontObjects(OntDisjoint.class).count());
        d1.getList().addFirst(p3).addFirst(p4);
        d2.getList().get(1).addFirst(p6);


        Assertions.assertEquals(Arrays.asList(p4, p3, p1, p2), d1.members().collect(Collectors.toList()));
        Assertions.assertEquals(Arrays.asList(p5, p6, p7), m.ontObjects(OntDisjoint.DataProperties.class)
                .findFirst().orElseThrow(AssertionError::new).members().collect(Collectors.toList()));
    }

    @Test
    public void testDisjointClassIndividualsOntList() {
        OntModel m = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD);

        OntClass ce1 = m.createOntClass("c1");
        OntClass ce3 = m.createHasSelf(m.createObjectProperty("p1"));
        OntClass ce2 = m.createDataHasValue(m.createDataProperty("p2"), m.createLiteral("2"));

        OntDisjoint.Classes d1 = m.createDisjointClasses(m.getOWLNothing(), ce1, ce3);
        OntDisjoint.Individuals d2 = m.createDifferentIndividuals(ce2.createIndividual(), ce3.createIndividual("I"));


        Assertions.assertEquals(2, m.statements(null, OWL2.members, null).count());
        Assertions.assertEquals(2, m.ontObjects(OntDisjoint.class).count());

        Assertions.assertEquals(1, d1.getList().get(1).removeFirst().members().count());
        Assertions.assertEquals(2, d1.members().count());
        Assertions.assertEquals(1, d2.getList().removeFirst().members().count());

        Assertions.assertEquals(1, m.ontObjects(OntDisjoint.Individuals.class)
                .findFirst().orElseThrow(AssertionError::new).members().count());
    }

    @Test
    public void testOntListWithIncompatibleTypes() {
        OntModel m = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD);
        OntList<OntClass> list = m.createObjectUnionOf(m.createOntClass("C1"), m.getOWLThing(), m.createOntClass("C2")).getList();
        Assertions.assertFalse(list.isEmpty());
        Assertions.assertFalse(list.isNil());
        Assertions.assertEquals(3, list.size());
        Assertions.assertEquals(3, list.members().count());

        OntStatement s = m.statements(null, null, OWL2.Thing).findFirst().orElseThrow(AssertionError::new);
        m.remove(s).add(s.getSubject(), s.getPredicate(), m.createTypedLiteral(0));

        Assertions.assertFalse(list.isEmpty());
        Assertions.assertFalse(list.isNil());
        Assertions.assertEquals(3, list.size());
        Assertions.assertEquals(2, list.members().count());
        list.removeFirst().removeLast();

        Assertions.assertTrue(list.isEmpty());
        Assertions.assertFalse(list.isNil());
        Assertions.assertEquals(1, list.size());
        Assertions.assertEquals(0, list.members().count());
    }
}
