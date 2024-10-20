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

import org.apache.jena.ontapi.model.OntClass;
import org.apache.jena.ontapi.model.OntDataProperty;
import org.apache.jena.ontapi.model.OntDataRange;
import org.apache.jena.ontapi.model.OntDisjoint;
import org.apache.jena.ontapi.model.OntList;
import org.apache.jena.ontapi.model.OntModel;
import org.apache.jena.ontapi.model.OntObject;
import org.apache.jena.ontapi.model.OntObjectProperty;
import org.apache.jena.ontapi.model.OntRelationalProperty;
import org.apache.jena.ontapi.testutils.RDFIOTestUtils;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.Lang;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.jena.ontapi.OntModelOWLSpecsTest.testListObjects;

public class OntModelOWL2RLSpecTest {

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_RL_MEM",
            "OWL2_RL_MEM_RDFS_INF",
            "OWL2_RL_MEM_TRANS_INF",
    })
    public void testPizzaObjects1d(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(
                RDFIOTestUtils.loadResourceAsModel("/pizza.ttl", Lang.TURTLE).getGraph(), spec.inst);

        Map<Class<? extends OntObject>, Integer> expected = new HashMap<>();
        expected.put(OntClass.ObjectSomeValuesFrom.class, 155);
        expected.put(OntClass.DataSomeValuesFrom.class, 0);
        expected.put(OntClass.ObjectAllValuesFrom.class, 3);
        expected.put(OntClass.DataAllValuesFrom.class, 0);
        expected.put(OntClass.ObjectHasValue.class, 6);
        expected.put(OntClass.DataHasValue.class, 0);
        expected.put(OntClass.ObjectMinCardinality.class, 0);
        expected.put(OntClass.DataMinCardinality.class, 0);
        expected.put(OntClass.ObjectMaxCardinality.class, 0);
        expected.put(OntClass.DataMaxCardinality.class, 0);
        expected.put(OntClass.ObjectCardinality.class, 0);
        expected.put(OntClass.DataCardinality.class, 0);
        expected.put(OntClass.HasSelf.class, 0);
        expected.put(OntClass.UnionOf.class, 25);
        expected.put(OntClass.OneOf.class, 1);
        expected.put(OntClass.IntersectionOf.class, 13);
        expected.put(OntClass.ComplementOf.class, 3);
        expected.put(OntClass.NaryDataAllValuesFrom.class, 0);
        expected.put(OntClass.NaryDataSomeValuesFrom.class, 0);
        expected.put(OntClass.LogicalExpression.class, 42);
        expected.put(OntClass.CollectionOf.class, 39);
        expected.put(OntClass.ValueRestriction.class, 164); // 187
        expected.put(OntClass.CardinalityRestriction.class, 0);
        expected.put(OntClass.ComponentRestriction.class, 164);
        expected.put(OntClass.UnaryRestriction.class, 164);
        expected.put(OntClass.Restriction.class, 164);
        expected.put(OntClass.class, 306);

        testListObjects(m, expected);

        List<OntClass.Named> classes = m.ontObjects(OntClass.Named.class).toList();
        int expectedClassesCount = m.listStatements(null, RDF.type, OWL2.Class)
                .mapWith(Statement::getSubject).filterKeep(RDFNode::isURIResource).toSet().size();
        int actualClassesCount = classes.size();
        Assertions.assertEquals(expectedClassesCount, actualClassesCount);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_RL_MEM",
    })
    public void testSubObjectSomeValuesFrom(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst);
        OntObjectProperty p = m.createObjectProperty("p");
        OntClass c0 = m.createOntClass("c0");
        OntClass c1 = m.createOntClass("c1");
        OntClass c2 = m.createOntClass("c2");
        OntClass c3 = m.createObjectSomeValuesFrom(p, c0);

        c1.addProperty(RDFS.subClassOf, c3);

        Assertions.assertEquals(0, c1.superClasses().count());
        Assertions.assertEquals(List.of(), c3.subClasses().collect(Collectors.toList()));

        c3.addProperty(RDFS.subClassOf, c2);
        Assertions.assertEquals(List.of(c2), c3.superClasses().collect(Collectors.toList()));
        Assertions.assertEquals(List.of(c3), c2.subClasses().collect(Collectors.toList()));
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_RL_MEM",
            "OWL2_RL_MEM_RDFS_INF",
            "OWL2_RL_MEM_TRANS_INF",
    })
    public void testObjectIntersectionOf(TestSpec spec) {
        OntModel data = OntModelFactory.createModel();
        OntDataProperty p = data.createDataProperty("p");
        OntClass c0 = data.createOntClass("c0");
        OntClass c1 = data.createOntClass("c1");
        OntClass c2 = data.createOntClass("c2");
        OntClass c3 = data.createDataMinCardinality(p, 42, data.getDatatype(XSD.xlong));
        OntClass c4 = data.createObjectIntersectionOf(c0);
        OntClass c5 = data.createObjectIntersectionOf(c1, c3);
        OntClass c6 = data.createObjectIntersectionOf(c1, c2);

        OntModel m = OntModelFactory.createModel(data.getGraph(), spec.inst);
        Assertions.assertFalse(c4.inModel(m).canAs(OntClass.class));
        Assertions.assertFalse(c5.inModel(m).canAs(OntClass.class));
        Assertions.assertTrue(c6.inModel(m).canAs(OntClass.class));

        Assertions.assertEquals(4, m.ontObjects(OntClass.class).count());
        Assertions.assertEquals(1, m.ontObjects(OntClass.LogicalExpression.class).count());
        Assertions.assertEquals(1, m.ontObjects(OntClass.CollectionOf.class).count());
        Assertions.assertEquals(1, m.ontObjects(OntClass.IntersectionOf.class).count());

        Assertions.assertThrows(OntJenaException.Unsupported.class, () -> m.createObjectIntersectionOf(c1, c3));

        m.createObjectIntersectionOf(c1, c5, c2);
        Assertions.assertEquals(5, m.ontObjects(OntClass.class).count());
        Assertions.assertEquals(2, m.ontObjects(OntClass.LogicalExpression.class).count());
        Assertions.assertEquals(2, m.ontObjects(OntClass.CollectionOf.class).count());
        Assertions.assertEquals(2, m.ontObjects(OntClass.IntersectionOf.class).count());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_RL_MEM",
            "OWL2_RL_MEM_RDFS_INF",
            "OWL2_RL_MEM_TRANS_INF",
    })
    public void testObjectUnionOf(TestSpec spec) {
        OntModel data = OntModelFactory.createModel();
        OntDataProperty p = data.createDataProperty("p");
        OntClass c0 = data.createOntClass("c0");
        OntClass c1 = data.createOntClass("c1");
        OntClass c2 = data.createOntClass("c2");
        OntClass c3 = data.createDataMinCardinality(p, 42, data.getDatatype(XSD.xlong));
        OntClass c4 = data.createObjectUnionOf(c0);
        OntClass c5 = data.createObjectUnionOf(c1, c3);
        OntClass c6 = data.createObjectUnionOf(c1, c2);

        OntModel m = OntModelFactory.createModel(data.getGraph(), spec.inst);
        Assertions.assertFalse(c4.inModel(m).canAs(OntClass.class));
        Assertions.assertFalse(c5.inModel(m).canAs(OntClass.class));
        Assertions.assertTrue(c6.inModel(m).canAs(OntClass.class));

        Assertions.assertEquals(4, m.ontObjects(OntClass.class).count());
        Assertions.assertEquals(1, m.ontObjects(OntClass.LogicalExpression.class).count());
        Assertions.assertEquals(1, m.ontObjects(OntClass.CollectionOf.class).count());
        Assertions.assertEquals(1, m.ontObjects(OntClass.UnionOf.class).count());

        Assertions.assertThrows(OntJenaException.Unsupported.class, () -> m.createObjectUnionOf(c1, c3));

        m.createObjectUnionOf(c1, c5, c2);
        Assertions.assertEquals(5, m.ontObjects(OntClass.class).count());
        Assertions.assertEquals(2, m.ontObjects(OntClass.LogicalExpression.class).count());
        Assertions.assertEquals(2, m.ontObjects(OntClass.CollectionOf.class).count());
        Assertions.assertEquals(2, m.ontObjects(OntClass.UnionOf.class).count());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_RL_MEM",
            "OWL2_RL_MEM_RDFS_INF",
            "OWL2_RL_MEM_TRANS_INF",
    })
    public void testSuperObjectMaxCardinality(TestSpec spec) {
        OntModel data = OntModelFactory.createModel();
        OntObjectProperty p0 = data.createObjectProperty("p0");
        OntObjectProperty p1 = data.createObjectProperty("p1");
        OntClass c0 = data.createOntClass("c0");
        OntClass c1 = data.createOntClass("c1");
        OntClass c2 = data.createOntClass("c2");
        OntClass c3 = data.createObjectMaxCardinality(p0, 42, null);
        OntClass c4 = data.createObjectMaxCardinality(p1, 0, null);
        OntClass c5 = data.createObjectMaxCardinality(p1, 1, c0);
        OntClass c6 = data.createObjectMaxCardinality(p1, 42, c1);

        OntModel m = OntModelFactory.createModel(data.getGraph(), spec.inst);
        Assertions.assertFalse(c3.inModel(m).canAs(OntClass.class));
        Assertions.assertTrue(c4.inModel(m).canAs(OntClass.class));
        Assertions.assertTrue(c5.inModel(m).canAs(OntClass.class));
        Assertions.assertFalse(c6.inModel(m).canAs(OntClass.class));

        Assertions.assertEquals(5, m.ontObjects(OntClass.class).count());
        Assertions.assertEquals(2, m.ontObjects(OntClass.Restriction.class).count());
        Assertions.assertEquals(2, m.ontObjects(OntClass.UnaryRestriction.class).count());
        Assertions.assertEquals(2, m.ontObjects(OntClass.CardinalityRestriction.class).count());
        Assertions.assertEquals(2, m.ontObjects(OntClass.ObjectMaxCardinality.class).count());

        Assertions.assertThrows(OntJenaException.Unsupported.class, () -> m.createObjectMaxCardinality(p0, 42, c1));
        Assertions.assertThrows(OntJenaException.Unsupported.class, () -> m.createObjectMaxCardinality(p0, 1, c5));

        m.createObjectMaxCardinality(p1, 1, c2);
        Assertions.assertEquals(6, m.ontObjects(OntClass.class).count());
        Assertions.assertEquals(3, m.ontObjects(OntClass.Restriction.class).count());
        Assertions.assertEquals(3, m.ontObjects(OntClass.UnaryRestriction.class).count());
        Assertions.assertEquals(3, m.ontObjects(OntClass.CardinalityRestriction.class).count());
        Assertions.assertEquals(3, m.ontObjects(OntClass.ObjectMaxCardinality.class).count());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_RL_MEM",
            "OWL2_RL_MEM_RDFS_INF",
            "OWL2_RL_MEM_TRANS_INF",
    })
    public void testSuperDataMaxCardinality(TestSpec spec) {
        OntModel data = OntModelFactory.createModel();
        OntDataProperty p0 = data.createDataProperty("p0");
        OntDataProperty p1 = data.createDataProperty("p1");
        OntDataRange d0 = data.createDatatype("d0");
        OntDataRange d1 = data.getDatatype(XSD.xstring.getURI());
        OntDataRange d2 = data.createDatatype("d2");
        OntClass c3 = data.createDataMaxCardinality(p0, 42, null);
        OntClass c4 = data.createDataMaxCardinality(p1, 0, null);
        OntClass c5 = data.createDataMaxCardinality(p1, 1, d0);
        OntClass c6 = data.createDataMaxCardinality(p1, 42, d1);

        OntModel m = OntModelFactory.createModel(data.getGraph(), spec.inst);
        Assertions.assertFalse(c3.inModel(m).canAs(OntClass.class));
        Assertions.assertTrue(c4.inModel(m).canAs(OntClass.class));
        Assertions.assertTrue(c5.inModel(m).canAs(OntClass.class));
        Assertions.assertFalse(c6.inModel(m).canAs(OntClass.class));

        Assertions.assertEquals(2, m.ontObjects(OntClass.class).count());
        Assertions.assertEquals(2, m.ontObjects(OntClass.Restriction.class).count());
        Assertions.assertEquals(2, m.ontObjects(OntClass.UnaryRestriction.class).count());
        Assertions.assertEquals(2, m.ontObjects(OntClass.CardinalityRestriction.class).count());
        Assertions.assertEquals(2, m.ontObjects(OntClass.DataMaxCardinality.class).count());

        Assertions.assertThrows(OntJenaException.Unsupported.class, () -> m.createDataMaxCardinality(p0, 42, d1));
        Assertions.assertThrows(OntJenaException.Unsupported.class, () -> m.createDataMaxCardinality(p0, 42, null));

        m.createDataMaxCardinality(p1, 1, d2);
        Assertions.assertEquals(3, m.ontObjects(OntClass.class).count());
        Assertions.assertEquals(3, m.ontObjects(OntClass.Restriction.class).count());
        Assertions.assertEquals(3, m.ontObjects(OntClass.UnaryRestriction.class).count());
        Assertions.assertEquals(3, m.ontObjects(OntClass.CardinalityRestriction.class).count());
        Assertions.assertEquals(3, m.ontObjects(OntClass.DataMaxCardinality.class).count());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_RL_MEM",
            "OWL2_RL_MEM_RDFS_INF",
            "OWL2_RL_MEM_TRANS_INF",
    })
    public void testSuperObjectComplementOf(TestSpec spec) {
        OntModel data = OntModelFactory.createModel();

        OntClass c0 = data.createOntClass("c0");
        OntClass c1 = data.createOntClass("c1");
        OntClass c2 = data.createObjectOneOf(data.createIndividual("X"));
        OntClass c3 = data.createObjectMaxCardinality(data.createObjectProperty("p1"), 0, null);

        OntClass c4 = data.createObjectComplementOf(c2); // true
        OntClass c5 = data.createObjectComplementOf(c0); // true
        OntClass c6 = data.createObjectComplementOf(c3); // false

        OntModel m = OntModelFactory.createModel(data.getGraph(), spec.inst);
        Assertions.assertTrue(c4.inModel(m).canAs(OntClass.class));
        Assertions.assertTrue(c5.inModel(m).canAs(OntClass.class));
        Assertions.assertFalse(c6.inModel(m).canAs(OntClass.class));

        Assertions.assertEquals(6, m.ontObjects(OntClass.class).count());
        Assertions.assertEquals(3, m.ontObjects(OntClass.LogicalExpression.class).count());
        Assertions.assertEquals(2, m.ontObjects(OntClass.ComplementOf.class).count());

        Assertions.assertThrows(OntJenaException.Unsupported.class, () -> m.createObjectComplementOf(c3));
        m.createObjectComplementOf(c1);
        Assertions.assertEquals(7, m.ontObjects(OntClass.class).count());
        Assertions.assertEquals(4, m.ontObjects(OntClass.LogicalExpression.class).count());
        Assertions.assertEquals(3, m.ontObjects(OntClass.ComplementOf.class).count());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_RL_MEM",
            "OWL2_RL_MEM_RDFS_INF",
            "OWL2_RL_MEM_TRANS_INF",
    })
    public void testSuperObjectAllValuesFrom(TestSpec spec) {
        OntModel data = OntModelFactory.createModel();
        OntObjectProperty p0 = data.createObjectProperty("p0");
        OntObjectProperty p1 = data.createObjectProperty("p1");
        OntDataProperty p2 = data.createDataProperty("p2");

        OntClass c0 = data.createOntClass("c0");
        OntClass c1 = data.createDataHasValue(p2, data.createTypedLiteral(42));
        OntClass c2 = data.createObjectOneOf(data.createIndividual("X"));

        OntClass c3 = data.createObjectAllValuesFrom(p0, c0); // true
        OntClass c4 = data.createObjectAllValuesFrom(p0, c2); // false
        OntClass c5 = data.createObjectAllValuesFrom(p0, c1); // true

        OntModel m = OntModelFactory.createModel(data.getGraph(), spec.inst);
        Assertions.assertTrue(c3.inModel(m).canAs(OntClass.class));
        Assertions.assertFalse(c4.inModel(m).canAs(OntClass.class));
        Assertions.assertTrue(c5.inModel(m).canAs(OntClass.class));

        Assertions.assertEquals(5, m.ontObjects(OntClass.class).count());
        Assertions.assertEquals(3, m.ontObjects(OntClass.Restriction.class).count());
        Assertions.assertEquals(3, m.ontObjects(OntClass.UnaryRestriction.class).count());
        Assertions.assertEquals(3, m.ontObjects(OntClass.ValueRestriction.class).count());
        Assertions.assertEquals(3, m.ontObjects(OntClass.ComponentRestriction.class).count());
        Assertions.assertEquals(2, m.ontObjects(OntClass.ObjectAllValuesFrom.class).count());

        Assertions.assertThrows(OntJenaException.Unsupported.class, () -> m.createObjectAllValuesFrom(p1, c2));
        m.createObjectAllValuesFrom(p1, c0);
        Assertions.assertEquals(6, m.ontObjects(OntClass.class).count());
        Assertions.assertEquals(4, m.ontObjects(OntClass.Restriction.class).count());
        Assertions.assertEquals(4, m.ontObjects(OntClass.UnaryRestriction.class).count());
        Assertions.assertEquals(4, m.ontObjects(OntClass.ValueRestriction.class).count());
        Assertions.assertEquals(4, m.ontObjects(OntClass.ComponentRestriction.class).count());
        Assertions.assertEquals(3, m.ontObjects(OntClass.ObjectAllValuesFrom.class).count());
    }


    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_RL_MEM",
            "OWL2_RL_MEM_RDFS_INF",
            "OWL2_RL_MEM_TRANS_INF",
    })
    public void testClassAssertions(TestSpec spec) {
        // OWL 2 RL restricts class expressions in positive assertions to superClassExpression.
        // All other assertions are the same as in the structural specification

        OntModel data = OntModelFactory.createModel();
        OntObjectProperty p0 = data.createObjectProperty("p0");
        OntDataProperty p1 = data.createDataProperty("p1");

        OntClass c0 = data.createOntClass("c0"); // can be super
        OntClass c1 = data.createDataHasValue(p1, data.createTypedLiteral(42)); // can be super
        OntClass c2 = data.createObjectOneOf(data.createIndividual("X")); // cannot be super, can be sub

        OntClass c3 = data.createObjectSomeValuesFrom(p0, c0); // cannot be super
        OntClass c4 = data.createObjectAllValuesFrom(p0, c2); // cannot be super, cannot be sub
        OntClass c5 = data.createDataMinCardinality(p1, 42, data.getDatatype(XSD.xstring)); // cannot be super and sub
        OntClass c6 = data.createDataMaxCardinality(p1, 0, data.getDatatype(XSD.xstring)); // can be super

        OntClass c7 = data.createObjectIntersectionOf(c1, c0); // can be supper, can be sub
        OntClass c8 = data.createObjectIntersectionOf(c1, c2); // cannot be supper, can be sub

        OntClass c9 = data.createObjectComplementOf(c2); // can be super, cannot be sub
        OntClass c10 = data.createObjectComplementOf(c9); // cannot be super, cannot be sub

        data.createIndividual("i1").attachClass(c3).attachClass(c4).attachClass(c5).attachClass(c7);
        data.createIndividual("i2").attachClass(c1).attachClass(c2).attachClass(c6);
        data.createIndividual("i3").attachClass(c9).attachClass(c10).attachClass(c8);

        OntModel m = OntModelFactory.createModel(data.getGraph(), spec.inst);

        Assertions.assertEquals(List.of(c7), m.getIndividual("i1").classes().toList());
        Assertions.assertEquals(Set.of(c1, c6), m.getIndividual("i2").classes().collect(Collectors.toSet()));
        Assertions.assertEquals(List.of(c9), m.getIndividual("i3").classes().toList());

        Assertions.assertTrue(c0.inModel(m).as(OntClass.class).canAsAssertionClass());
        Assertions.assertTrue(c1.inModel(m).as(OntClass.class).canAsAssertionClass());
        Assertions.assertFalse(c2.inModel(m).as(OntClass.class).canAsAssertionClass());
        Assertions.assertThrows(OntJenaException.Unsupported.class, c2.inModel(m).as(OntClass.class)::asAssertionClass);
        Assertions.assertFalse(c3.inModel(m).as(OntClass.class).canAsAssertionClass());
        Assertions.assertThrows(OntJenaException.Unsupported.class, c3.inModel(m).as(OntClass.class)::asAssertionClass);
        Assertions.assertThrows(OntJenaException.Conversion.class, () -> c4.inModel(m).as(OntClass.class));
        Assertions.assertThrows(OntJenaException.Conversion.class, () -> c5.inModel(m).as(OntClass.class));
        Assertions.assertTrue(c6.inModel(m).as(OntClass.class).canAsAssertionClass());
        Assertions.assertTrue(c7.inModel(m).as(OntClass.class).canAsAssertionClass());
        Assertions.assertFalse(c8.inModel(m).as(OntClass.class).canAsAssertionClass());
        Assertions.assertThrows(OntJenaException.Unsupported.class, c8.inModel(m).as(OntClass.class)::asAssertionClass);
        Assertions.assertTrue(c9.inModel(m).as(OntClass.class).canAsAssertionClass());
        Assertions.assertThrows(OntJenaException.Conversion.class, () -> c10.inModel(m).as(OntClass.class));
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_RL_MEM",
            "OWL2_RL_MEM_RDFS_INF",
            "OWL2_RL_MEM_TRANS_INF",
    })
    public void testDisjointAxiom(TestSpec spec) {
        OntModel data = OntModelFactory.createModel();
        OntObjectProperty p0 = data.createObjectProperty("p0");
        OntDataProperty p1 = data.createDataProperty("p1");

        OntClass c0 = data.createOntClass("c0"); // can be super, can be sub
        OntClass c1 = data.createDataHasValue(p1, data.createTypedLiteral(42)); // can be super, can be sub
        OntClass c2 = data.createObjectOneOf(data.createIndividual("X")); // cannot be super, can be sub

        OntClass c3 = data.createObjectSomeValuesFrom(p0, c0); // cannot be super, can be sub
        OntClass c4 = data.createObjectAllValuesFrom(p0, c2); // cannot be super, cannot be sub
        OntClass c5 = data.createDataMinCardinality(p1, 42, data.getDatatype(XSD.xstring)); // cannot be super and sub
        OntClass c6 = data.createDataMaxCardinality(p1, 0, data.getDatatype(XSD.xstring)); // can be super, cannot be sub

        OntClass c7 = data.createObjectIntersectionOf(c1, c0); // can be supper, can be sub
        OntClass c8 = data.createObjectIntersectionOf(c1, c2); // cannot be supper, can be sub

        OntClass c9 = data.createObjectComplementOf(c2); // can be super, cannot be sub
        OntClass c10 = data.createObjectComplementOf(c9); // cannot be super, cannot be sub

        OntModel m = OntModelFactory.createModel(data.getGraph(), spec.inst);

        OntClass mc0 = c0.inModel(m).as(OntClass.class);
        OntClass mc1 = c1.inModel(m).as(OntClass.class);
        OntClass mc2 = c2.inModel(m).as(OntClass.class);
        OntClass mc3 = c3.inModel(m).as(OntClass.class);
        OntClass mc6 = c6.inModel(m).as(OntClass.class);
        OntClass mc7 = c7.inModel(m).as(OntClass.class);
        OntClass mc8 = c8.inModel(m).as(OntClass.class);
        OntClass mc9 = c9.inModel(m).as(OntClass.class);

        Assertions.assertTrue(mc0.canAsDisjointClass());
        Assertions.assertTrue(mc1.canAsDisjointClass());
        Assertions.assertTrue(mc2.canAsDisjointClass());
        Assertions.assertTrue(mc3.canAsDisjointClass());
        Assertions.assertThrows(OntJenaException.Conversion.class, () -> c4.inModel(m).as(OntClass.class));
        Assertions.assertThrows(OntJenaException.Conversion.class, () -> c5.inModel(m).as(OntClass.class));
        Assertions.assertFalse(mc6.canAsDisjointClass());
        Assertions.assertTrue(mc7.canAsDisjointClass());
        Assertions.assertTrue(mc8.canAsDisjointClass());
        Assertions.assertFalse(mc9.canAsDisjointClass());
        Assertions.assertThrows(OntJenaException.Conversion.class, () -> c10.inModel(m).as(OntClass.class));

        Assertions.assertSame(mc0, mc0.asDisjointClass());
        Assertions.assertSame(mc1, mc1.asDisjointClass());
        Assertions.assertSame(mc2, mc2.asDisjointClass());
        Assertions.assertSame(mc3, mc3.asDisjointClass());
        Assertions.assertNotSame(mc7, mc7.asDisjointClass());
        Assertions.assertEquals(mc7, mc7.asDisjointClass());
        Assertions.assertNotSame(mc8, mc8.asDisjointClass());
        Assertions.assertEquals(mc8, mc8.asDisjointClass());
        Assertions.assertThrows(OntJenaException.Unsupported.class, mc6::asDisjointClass);
        Assertions.assertThrows(OntJenaException.Unsupported.class, mc9::asDisjointClass);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_RL_MEM",
            "OWL2_RL_MEM_RDFS_INF",
            "OWL2_RL_MEM_TRANS_INF",
    })
    public void testEquivalentAxiom(TestSpec spec) {
        OntModel data = OntModelFactory.createModel();
        OntObjectProperty p0 = data.createObjectProperty("p0");
        OntDataProperty p1 = data.createDataProperty("p1");

        OntClass c0 = data.createOntClass("c0"); // can be super, can be sub, can be equiv
        OntClass c1 = data.createDataHasValue(p1, data.createTypedLiteral(42)); // can be super, can be sub, can be equiv
        OntClass c2 = data.createObjectOneOf(data.createIndividual("X")); // cannot be super, can be sub, cannot be equiv

        OntClass c3 = data.createObjectSomeValuesFrom(p0, c0); // cannot be super, can be sub, cannot be equiv
        OntClass c6 = data.createDataMaxCardinality(p1, 0, data.getDatatype(XSD.xstring)); // can be super, cannot be sub, cannot be equiv

        OntClass c7 = data.createObjectIntersectionOf(c1, c0); // can be supper, can be sub, can be equiv
        OntClass c8 = data.createObjectIntersectionOf(c1, c2); // cannot be supper, can be sub, cannot be equiv
        OntClass c9 = data.createObjectComplementOf(c2); // can be super, cannot be sub, cannot be equiv

        OntModel m = OntModelFactory.createModel(data.getGraph(), spec.inst);

        OntClass mc0 = c0.inModel(m).as(OntClass.class);
        OntClass mc1 = c1.inModel(m).as(OntClass.class);
        OntClass mc2 = c2.inModel(m).as(OntClass.class);
        OntClass mc3 = c3.inModel(m).as(OntClass.class);
        OntClass mc6 = c6.inModel(m).as(OntClass.class);
        OntClass mc7 = c7.inModel(m).as(OntClass.class);
        OntClass mc8 = c8.inModel(m).as(OntClass.class);
        OntClass mc9 = c9.inModel(m).as(OntClass.class);

        Assertions.assertFalse(m.getOWLThing().canAsEquivalentClass());
        Assertions.assertTrue(mc0.canAsEquivalentClass());
        Assertions.assertTrue(mc1.canAsEquivalentClass());
        Assertions.assertFalse(mc2.canAsEquivalentClass());
        Assertions.assertFalse(mc3.canAsEquivalentClass());
        Assertions.assertFalse(mc6.canAsEquivalentClass());
        Assertions.assertTrue(mc7.canAsEquivalentClass());
        Assertions.assertFalse(mc8.canAsEquivalentClass());
        Assertions.assertFalse(mc9.canAsEquivalentClass());

        Assertions.assertSame(mc0, mc0.asEquivalentClass());
        Assertions.assertSame(mc1, mc1.asEquivalentClass());
        Assertions.assertNotSame(mc7, mc7.asEquivalentClass());
        Assertions.assertEquals(mc7, mc7.asEquivalentClass());
        Assertions.assertThrows(OntJenaException.Unsupported.class, mc2::asEquivalentClass);
        Assertions.assertThrows(OntJenaException.Unsupported.class, mc3::asEquivalentClass);
        Assertions.assertThrows(OntJenaException.Unsupported.class, mc6::asEquivalentClass);
        Assertions.assertThrows(OntJenaException.Unsupported.class, mc8::asEquivalentClass);
        Assertions.assertThrows(OntJenaException.Unsupported.class, mc9::asEquivalentClass);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_RL_MEM",
            "OWL2_RL_MEM_RDFS_INF",
            "OWL2_RL_MEM_TRANS_INF",
    })
    public void testOWLThing(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst);
        OntClass c0 = m.createOntClass("c0");
        OntClass c1 = m.createOntClass("c1");
        c0.addSuperClass(c1);

        c0.addProperty(RDFS.subClassOf, OWL2.Thing);
        Assertions.assertEquals(List.of(c1), c0.superClasses().collect(Collectors.toList()));
        Assertions.assertThrows(OntJenaException.Unsupported.class, () -> c1.addSuperClass(m.getOWLThing()));

        OWL2.Thing.inModel(m).addProperty(RDFS.subClassOf, c1);
        Assertions.assertEquals(List.of(c0), c1.subClasses().collect(Collectors.toList()));
        Assertions.assertThrows(OntJenaException.Unsupported.class, () -> m.getOWLThing().addSuperClass(m.getOWLThing()));

        c0.addProperty(OWL2.equivalentClass, OWL2.Thing).addProperty(OWL2.equivalentClass, c1);
        Assertions.assertEquals(List.of(c0), c1.equivalentClasses().collect(Collectors.toList()));
        Assertions.assertEquals(List.of(c1), c0.equivalentClasses().collect(Collectors.toList()));
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_RL_MEM",
            "OWL2_RL_MEM_RDFS_INF",
            "OWL2_RL_MEM_TRANS_INF",
            "OWL2_RL_MEM_RULES_INF",
    })
    public void testBuiltins(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst);
        Assertions.assertNotNull(m.getOWLThing());
        Assertions.assertNotNull(m.getOWLNothing());
        Assertions.assertNull(m.getOWLBottomDataProperty());
        Assertions.assertNull(m.getOWLBottomObjectProperty());
        Assertions.assertNull(m.getOWLTopObjectProperty());
        Assertions.assertNull(m.getOWLTopDataProperty());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_RL_MEM",
            "OWL2_RL_MEM_RDFS_INF",
            "OWL2_RL_MEM_TRANS_INF",
    })
    public void testOntDisjointClasses(TestSpec spec) {
        OntModel data = OntModelFactory.createModel();
        data.createDisjointClasses(
                data.createOntClass("A"), data.createObjectComplementOf(data.createOntClass("B"))
        );
        data.createDisjointClasses(
                data.createOntClass("C")
        );
        data.createDisjointClasses(
                data.createDataAllValuesFrom(data.createDataProperty("p1"), data.getDatatype(XSD.xstring)), // super
                data.createOntClass("D"),
                data.createObjectSomeValuesFrom(data.createObjectProperty("p2"), data.createOntClass("E")),
                data.createObjectComplementOf(data.createOntClass("F")) // super
        );

        OntModel m = OntModelFactory.createModel(data.getGraph(), spec.inst);

        List<OntDisjoint.Classes> disjoints1 = m.ontObjects(OntDisjoint.Classes.class).toList();
        Assertions.assertEquals(1, disjoints1.size());
        List<OntClass> members = disjoints1.get(0).members().toList();
        Assertions.assertEquals(2, members.size());
        Assertions.assertTrue(members.stream().anyMatch(it -> it.canAs(OntClass.Named.class)));
        Assertions.assertTrue(members.stream().anyMatch(it -> it.canAs(OntClass.ObjectSomeValuesFrom.class)));

        Assertions.assertThrows(OntJenaException.Unsupported.class, () ->
                m.createDisjointClasses(
                        m.createDataAllValuesFrom(m.createDataProperty("p1"), m.getDatatype(XSD.xstring)), // super
                        m.createOntClass("D"),
                        m.createObjectComplementOf(m.createOntClass("F")) // super
                )
        );
        Assertions.assertThrows(OntJenaException.Unsupported.class, () ->
                m.createDisjointClasses(m.createOntClass("G"))
        );
        m.createDisjointClasses(m.createOntClass("G"), m.createOntClass("H"));
        List<OntDisjoint.Classes> disjoints2 = m.ontObjects(OntDisjoint.Classes.class).toList();
        Assertions.assertEquals(2, disjoints2.size());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_RL_MEM",
            "OWL2_RL_MEM_RDFS_INF",
            "OWL2_RL_MEM_TRANS_INF",
    })
    public void testHasKey(TestSpec spec) {
        OntModel data = OntModelFactory.createModel();
        OntClass c11 = data.createOntClass("A").addHasKey(data.createObjectProperty("p1"));
        OntClass c12 = data.createObjectMaxCardinality(data.createObjectProperty("p2"), 1, data.createOntClass("B"));

        OntModel m = OntModelFactory.createModel(data.getGraph(), spec.inst);
        OntClass c21 = c11.inModel(m).as(OntClass.class);
        OntClass c22 = c12.inModel(m).as(OntClass.class);
        List<OntList<OntRelationalProperty>> hasKey1 = c21.hasKeys().toList();
        Assertions.assertEquals(1, hasKey1.size());
        List<OntList<OntRelationalProperty>> hasKey2 = c22.hasKeys().toList();
        Assertions.assertEquals(0, hasKey2.size());

        Assertions.assertThrows(OntJenaException.Unsupported.class, () ->
                c22.createHasKey(List.of(m.createObjectProperty("p1")), List.of())
        );
        c21.createHasKey(List.of(m.createObjectProperty("p3")), List.of(m.createDataProperty("p4")));
        List<OntList<OntRelationalProperty>> hasKey3 = c21.hasKeys().toList();
        Assertions.assertEquals(2, hasKey3.size());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_RL_MEM",
            "OWL2_RL_MEM_RDFS_INF",
            "OWL2_RL_MEM_TRANS_INF",
    })
    public void testDisjointObjectProperties(TestSpec spec) {
        OntModel data = OntModelFactory.createModel();
        data.createDisjointObjectProperties(data.createObjectProperty("a"));
        data.createResource()
                .addProperty(RDF.type, OWL2.AllDisjointProperties)
                .addProperty(OWL2.members, data.createList());
        data.createDisjointObjectProperties(data.createObjectProperty("b"), data.createObjectProperty("c"));

        OntModel m = OntModelFactory.createModel(data.getGraph(), spec.inst);

        List<OntDisjoint.ObjectProperties> res1 = m.ontObjects(OntDisjoint.ObjectProperties.class).toList();
        Assertions.assertEquals(1, res1.size());
        Assertions.assertEquals(
                Set.of("b", "c"),
                res1.get(0).members().map(Resource::getURI).collect(Collectors.toSet())
        );

        Assertions.assertThrows(
                OntJenaException.Unsupported.class,
                () -> m.createDisjointObjectProperties(m.createObjectProperty("d"))
        );

        m.createDisjointObjectProperties(
                m.createObjectProperty("e"), m.createObjectProperty("f"), m.createObjectProperty("g")
        );

        List<OntDisjoint.ObjectProperties> res2 = m.ontObjects(OntDisjoint.ObjectProperties.class).toList();
        Assertions.assertEquals(2, res2.size());
    }
}
