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
import org.apache.jena.ontapi.model.OntModel;
import org.apache.jena.ontapi.model.OntObject;
import org.apache.jena.ontapi.model.OntObjectProperty;
import org.apache.jena.ontapi.testutils.RDFIOTestUtils;
import org.apache.jena.ontapi.vocabulary.OWL;
import org.apache.jena.ontapi.vocabulary.RDF;
import org.apache.jena.ontapi.vocabulary.XSD;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.Lang;
import org.apache.jena.vocabulary.RDFS;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        int expectedClassesCount = m.listStatements(null, RDF.type, OWL.Class)
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
        Assertions.assertEquals(List.of(c1), c3.subClasses().collect(Collectors.toList()));

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
    public void testOWLThing(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst);
        OntClass c0 = m.createOntClass("c0");
        OntClass c1 = m.createOntClass("c1");
        c0.addSuperClass(c1);

        c0.addProperty(RDFS.subClassOf, OWL.Thing);
        Assertions.assertEquals(List.of(c1), c0.superClasses().collect(Collectors.toList()));
        Assertions.assertThrows(OntJenaException.Unsupported.class, () -> c1.addSuperClass(m.getOWLThing()));

        OWL.Thing.inModel(m).addProperty(RDFS.subClassOf, c1);
        Assertions.assertEquals(List.of(c0), c1.subClasses().collect(Collectors.toList()));
        Assertions.assertThrows(OntJenaException.Unsupported.class, () -> m.getOWLThing().addSuperClass(m.getOWLThing()));

        c0.addProperty(OWL.equivalentClass, OWL.Thing).addProperty(OWL.equivalentClass, c1);
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
}
