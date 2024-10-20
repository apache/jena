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
import org.apache.jena.ontapi.model.OntDisjoint;
import org.apache.jena.ontapi.model.OntIndividual;
import org.apache.jena.ontapi.model.OntModel;
import org.apache.jena.ontapi.model.OntObject;
import org.apache.jena.ontapi.model.OntObjectProperty;
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

public class OntModelOWL2QLSpecTest {

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_QL_MEM",
            "OWL2_QL_MEM_RDFS_INF",
            "OWL2_QL_MEM_TRANS_INF",
    })
    public void testPizzaObjects1c(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(
                RDFIOTestUtils.loadResourceAsModel("/pizza.ttl", Lang.TURTLE).getGraph(), spec.inst);

        Map<Class<? extends OntObject>, Integer> expected = new HashMap<>();
        expected.put(OntClass.ObjectSomeValuesFrom.class, 154);
        expected.put(OntClass.DataSomeValuesFrom.class, 0);
        expected.put(OntClass.ObjectAllValuesFrom.class, 0);
        expected.put(OntClass.DataAllValuesFrom.class, 0);
        expected.put(OntClass.ObjectHasValue.class, 0);
        expected.put(OntClass.DataHasValue.class, 0);
        expected.put(OntClass.ObjectMinCardinality.class, 0);
        expected.put(OntClass.DataMinCardinality.class, 0);
        expected.put(OntClass.ObjectMaxCardinality.class, 0);
        expected.put(OntClass.DataMaxCardinality.class, 0);
        expected.put(OntClass.ObjectCardinality.class, 0);
        expected.put(OntClass.DataCardinality.class, 0);
        expected.put(OntClass.HasSelf.class, 0);
        expected.put(OntClass.UnionOf.class, 0);
        expected.put(OntClass.OneOf.class, 0);
        expected.put(OntClass.IntersectionOf.class, 6);
        expected.put(OntClass.ComplementOf.class, 1);
        expected.put(OntClass.NaryDataAllValuesFrom.class, 0);
        expected.put(OntClass.NaryDataSomeValuesFrom.class, 0);
        expected.put(OntClass.LogicalExpression.class, 7);
        expected.put(OntClass.CollectionOf.class, 6);
        expected.put(OntClass.ValueRestriction.class, 154);
        expected.put(OntClass.CardinalityRestriction.class, 0);
        expected.put(OntClass.ComponentRestriction.class, 154);
        expected.put(OntClass.UnaryRestriction.class, 154);
        expected.put(OntClass.Restriction.class, 154);
        expected.put(OntClass.class, 261);

        testListObjects(m, expected);

        List<OntClass.Named> classes = m.ontObjects(OntClass.Named.class).toList();
        int expectedClassesCount = m.listStatements(null, RDF.type, OWL2.Class)
                .mapWith(Statement::getSubject).filterKeep(RDFNode::isURIResource).toSet().size();
        int actualClassesCount = classes.size();
        Assertions.assertEquals(expectedClassesCount, actualClassesCount);
    }


    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_QL_MEM",
            "OWL2_QL_MEM_RDFS_INF",
            "OWL2_QL_MEM_TRANS_INF",
    })
    public void testObjectSomeValuesFrom(TestSpec spec) {
        OntModel data = OntModelFactory.createModel();
        OntObjectProperty p = data.createObjectProperty("p");
        OntClass c0 = data.createOntClass("c");
        OntClass c1 = data.createObjectSomeValuesFrom(p, c0);
        OntClass c2 = data.createObjectSomeValuesFrom(p, data.createObjectUnionOf(c0));
        OntClass c3 = data.createObjectSomeValuesFrom(p, data.getOWLThing());

        OntModel m = OntModelFactory.createModel(data.getGraph(), spec.inst);
        Assertions.assertTrue(c1.inModel(m).canAs(OntClass.ObjectSomeValuesFrom.class));
        Assertions.assertFalse(c2.inModel(m).canAs(OntClass.ObjectSomeValuesFrom.class));
        Assertions.assertTrue(c3.inModel(m).canAs(OntClass.ObjectSomeValuesFrom.class));

        Assertions.assertTrue(c1.inModel(m).canAs(OntClass.class));
        Assertions.assertFalse(c2.inModel(m).canAs(OntClass.class));
        Assertions.assertTrue(c3.inModel(m).canAs(OntClass.class));

        Assertions.assertEquals(3, m.ontObjects(OntClass.class).count());

        OntClass oc1 = c1.inModel(m).as(OntClass.ObjectSomeValuesFrom.class);
        OntClass oc3 = c3.inModel(m).as(OntClass.ObjectSomeValuesFrom.class);

        Assertions.assertFalse(oc1.canAsSubClass());
        Assertions.assertThrows(OntJenaException.Unsupported.class, oc1::asSubClass);
        Assertions.assertSame(oc3, oc3.asSubClass());
        Assertions.assertSame(oc1, oc1.asSuperClass());
        Assertions.assertSame(oc3, oc3.asSuperClass());

        Assertions.assertThrows(OntJenaException.Unsupported.class, () -> m.createObjectSomeValuesFrom(p, oc1));

        Assertions.assertThrows(OntJenaException.Unsupported.class, () -> m.createObjectSomeValuesFrom(p, c0).asSubClass());
        Assertions.assertEquals(4, m.ontObjects(OntClass.class).count());
        Assertions.assertEquals(3, m.ontObjects(OntClass.ObjectSomeValuesFrom.class).count());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_QL_MEM",
            "OWL2_QL_MEM_RDFS_INF",
            "OWL2_QL_MEM_TRANS_INF",
            "OWL2_QL_MEM_RULES_INF",
    })
    public void testObjectIntersectionOf(TestSpec spec) {
        OntModel data = OntModelFactory.createModel();
        OntObjectProperty op = data.createObjectProperty("p");
        OntDataProperty dp = data.createDataProperty("d");
        OntClass c0 = data.createOntClass("c");
        OntClass c1 = data.createObjectUnionOf(c0);

        OntClass c2 = data.createObjectSomeValuesFrom(op, c0);
        OntClass c3 = data.createDataSomeValuesFrom(dp, data.getDatatype(XSD.xstring.getURI()));
        OntClass c4 = data.createDataSomeValuesFrom(dp, data.getDatatype(OWL2.rational.getURI()));
        OntClass c5 = data.createObjectSomeValuesFrom(op, data.getOWLThing());

        OntClass c6 = data.createObjectIntersectionOf(c2, c1);
        OntClass c7 = data.createObjectIntersectionOf(c2, c3);
        OntClass c8 = data.createObjectIntersectionOf(c2, c3, c5, c1);

        OntModel m = OntModelFactory.createModel(data.getGraph(), spec.inst);
        Assertions.assertFalse(c6.inModel(m).canAs(OntClass.IntersectionOf.class));
        Assertions.assertTrue(c7.inModel(m).canAs(OntClass.IntersectionOf.class));
        Assertions.assertTrue(c8.inModel(m).canAs(OntClass.IntersectionOf.class));

        Assertions.assertEquals(2, m.ontObjects(OntClass.IntersectionOf.class).count());
        Assertions.assertEquals(2, m.ontObjects(OntClass.LogicalExpression.class).count());
        Assertions.assertEquals(2, m.ontObjects(OntClass.CollectionOf.class).count());
        Assertions.assertEquals(spec == TestSpec.OWL2_QL_MEM_RULES_INF ? 19 : 7, m.ontObjects(OntClass.class).count());


        Assertions.assertThrows(OntJenaException.Unsupported.class, m::createObjectIntersectionOf);
        Assertions.assertThrows(OntJenaException.Unsupported.class, () -> m.createObjectIntersectionOf(c0));
        Assertions.assertThrows(OntJenaException.Unsupported.class, () -> m.createObjectIntersectionOf(c0, c1));
        m.createObjectIntersectionOf(c0, c4);
        Assertions.assertEquals(spec == TestSpec.OWL2_QL_MEM_RULES_INF ? 20 : 8, m.ontObjects(OntClass.class).count());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_QL_MEM",
            "OWL2_QL_MEM_RDFS_INF",
            "OWL2_QL_MEM_TRANS_INF",
            "OWL2_QL_MEM_RULES_INF",
    })
    public void testObjectComplementOf(TestSpec spec) {
        OntModel data = OntModelFactory.createModel();
        OntDataProperty dp = data.createDataProperty("d");

        OntClass c0 = data.createOntClass("c");
        OntClass c1 = data.createObjectUnionOf(c0);
        OntClass c2 = data.createDataSomeValuesFrom(dp, data.getDatatype(XSD.xstring.getURI()));

        OntClass c3 = data.createObjectComplementOf(c0);
        OntClass c4 = data.createObjectComplementOf(c1);
        OntClass c5 = data.createObjectComplementOf(c2);

        OntModel m = OntModelFactory.createModel(data.getGraph(), spec.inst);
        Assertions.assertTrue(c3.inModel(m).canAs(OntClass.ComplementOf.class));
        Assertions.assertFalse(c4.inModel(m).canAs(OntClass.ComplementOf.class));
        Assertions.assertTrue(c5.inModel(m).canAs(OntClass.ComplementOf.class));

        Assertions.assertEquals(2, m.ontObjects(OntClass.ComplementOf.class).count());
        Assertions.assertEquals(2, m.ontObjects(OntClass.LogicalExpression.class).count());
        Assertions.assertEquals(spec == TestSpec.OWL2_QL_MEM_RULES_INF ? 16 : 4, m.ontObjects(OntClass.class).count());

        Assertions.assertThrows(OntJenaException.Unsupported.class, () -> m.createObjectComplementOf(c1));
        m.createObjectComplementOf(c2);
        Assertions.assertEquals(spec == TestSpec.OWL2_QL_MEM_RULES_INF ? 17 : 5, m.ontObjects(OntClass.class).count());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_QL_MEM",
    })
    public void testSubClasses(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst);
        OntObjectProperty op = m.createObjectProperty("p");
        OntDataProperty dp = m.createDataProperty("d");

        OntClass c0 = m.createOntClass("c0");
        OntClass c1 = m.createDataSomeValuesFrom(dp, m.getDatatype(XSD.xstring.getURI()));
        OntClass c2 = m.createObjectSomeValuesFrom(op, c0);
        OntClass c3 = m.createObjectSomeValuesFrom(op, m.getOWLThing());
        OntClass c4 = m.createObjectComplementOf(c0);
        OntClass c5 = m.createObjectComplementOf(c1);

        c0.addProperty(RDFS.subClassOf, c4);
        c4.addProperty(RDFS.subClassOf, c1);
        c4.addProperty(RDFS.subClassOf, c5);
        c3.addProperty(RDFS.subClassOf, c0);
        c2.addProperty(RDFS.subClassOf, c0);

        Assertions.assertEquals(List.of(c3), c0.subClasses().collect(Collectors.toList()));
        Assertions.assertEquals(List.of(c4), c0.superClasses().collect(Collectors.toList()));
        Assertions.assertEquals(List.of(c0), c4.subClasses().collect(Collectors.toList()));
        Assertions.assertEquals(List.of(), c4.superClasses().collect(Collectors.toList()));
        Assertions.assertEquals(List.of(), c5.subClasses().collect(Collectors.toList()));

        Assertions.assertThrows(OntJenaException.Unsupported.class, () -> c0.addSubClass(c2));
        c0.addSuperClass(c2);
        Assertions.assertEquals(Set.of(c2, c4), c0.superClasses().collect(Collectors.toSet()));
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_QL_MEM",
            "OWL2_QL_MEM_RDFS_INF",
            "OWL2_QL_MEM_TRANS_INF",
    })
    public void testIndividuals(TestSpec spec) {
        // class assertions in OWL 2 QL can involve only atomic classes
        OntModel m = OntModelFactory.createModel(spec.inst);
        OntObjectProperty op = m.createObjectProperty("p");
        OntDataProperty dp = m.createDataProperty("d");

        OntClass c0 = m.createOntClass("c");
        OntClass c1 = m.createDataSomeValuesFrom(dp, m.getDatatype(XSD.xstring.getURI()));
        OntClass c2 = m.createObjectSomeValuesFrom(op, c0);
        OntClass c3 = m.createObjectSomeValuesFrom(op, m.getOWLThing());
        OntClass c4 = m.createObjectComplementOf(c0);
        OntClass c5 = m.createObjectIntersectionOf(c0, c1);

        OntIndividual i0 = m.createIndividual("i0");
        OntIndividual i1 = m.createIndividual("i1");
        OntIndividual i2 = m.createIndividual("i2");
        OntIndividual i3 = m.createIndividual("i3");

        i0.addProperty(RDF.type, c0);
        i1.addProperty(RDF.type, c1);
        i2.addProperty(RDF.type, c2);
        i3.addProperty(RDF.type, c3);
        i0.addProperty(RDF.type, c4);
        i0.addProperty(RDF.type, c5);

        Assertions.assertEquals(List.of(i0), c0.individuals().collect(Collectors.toList()));
        Assertions.assertEquals(List.of(), c1.individuals().collect(Collectors.toList()));
        Assertions.assertEquals(List.of(), c2.individuals().collect(Collectors.toList()));
        Assertions.assertEquals(List.of(), c3.individuals().collect(Collectors.toList()));
        Assertions.assertEquals(List.of(), c4.individuals().collect(Collectors.toList()));
        Assertions.assertEquals(List.of(), c5.individuals().collect(Collectors.toList()));

        Assertions.assertEquals(Set.of(c0), i0.classes().collect(Collectors.toSet()));
        Assertions.assertEquals(List.of(), i1.classes().collect(Collectors.toList()));
        Assertions.assertEquals(List.of(), i2.classes().collect(Collectors.toList()));
        Assertions.assertEquals(List.of(), i3.classes().collect(Collectors.toList()));

        Assertions.assertEquals(List.of(i0), m.individuals().collect(Collectors.toList()));
        Assertions.assertEquals(4, m.ontEntities(OntIndividual.Named.class).count());

        Assertions.assertThrows(OntJenaException.Unsupported.class, () -> c1.createIndividual("x"));
        Assertions.assertThrows(OntJenaException.Unsupported.class, c0::createIndividual);

        OntIndividual i4 = c0.createIndividual("i4");
        Assertions.assertEquals(Set.of(i0, i4), m.individuals().collect(Collectors.toSet()));
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_QL_MEM",
            "OWL2_QL_MEM_RDFS_INF",
            "OWL2_QL_MEM_TRANS_INF",
            "OWL2_QL_MEM_RULES_INF",
    })
    public void testDisjointClasses(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst);
        OntObjectProperty op = m.createObjectProperty("p");
        OntDataProperty dp = m.createDataProperty("d");

        OntClass c0 = m.createOntClass("c");
        OntClass c1 = m.createDataSomeValuesFrom(dp, m.getDatatype(XSD.xstring.getURI()));
        OntClass c2 = m.createObjectSomeValuesFrom(op, c0);

        Assertions.assertThrows(OntJenaException.Unsupported.class, () -> c1.addDisjointClass(c2));
        c0.addDisjointClass(c1);
        Assertions.assertEquals(List.of(c0), c1.disjointClasses().collect(Collectors.toList()));
        Assertions.assertEquals(List.of(c1), c0.disjointClasses().collect(Collectors.toList()));

        c1.addProperty(OWL2.disjointWith, c2);
        Assertions.assertEquals(List.of(c0), c1.disjointClasses().collect(Collectors.toList()));
        Assertions.assertEquals(List.of(), c2.disjointClasses().collect(Collectors.toList()));
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_QL_MEM",
            "OWL2_QL_MEM_RDFS_INF",
            "OWL2_QL_MEM_TRANS_INF",
    })
    public void testEquivalents(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst);
        OntObjectProperty op = m.createObjectProperty("p");
        OntDataProperty dp = m.createDataProperty("d");

        OntClass c0 = m.createOntClass("c");
        OntClass c1 = m.createDataSomeValuesFrom(dp, m.getDatatype(XSD.xstring.getURI()));
        OntClass c2 = m.createObjectSomeValuesFrom(op, c0);

        Assertions.assertThrows(OntJenaException.Unsupported.class, () -> c1.addEquivalentClass(c2));
        c0.addEquivalentClass(c1);
        Assertions.assertEquals(List.of(c0), c1.equivalentClasses().collect(Collectors.toList()));
        Assertions.assertEquals(List.of(c1), c0.equivalentClasses().collect(Collectors.toList()));

        c1.addProperty(OWL2.equivalentClass, c2);
        Assertions.assertEquals(List.of(c0), c1.equivalentClasses().collect(Collectors.toList()));
        Assertions.assertEquals(List.of(), c2.equivalentClasses().collect(Collectors.toList()));
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_QL_MEM",
            "OWL2_QL_MEM_RDFS_INF",
            "OWL2_QL_MEM_TRANS_INF",
    })
    public void testDisjointEquivalentAxioms(TestSpec spec) {
        OntModel data = OntModelFactory.createModel();
        OntObjectProperty p0 = data.createObjectProperty("p0");
        OntDataProperty p1 = data.createDataProperty("p1");

        OntClass c0 = data.createOntClass("c0");
        OntClass c1 = data.createDataHasValue(p1, data.createTypedLiteral(42));
        OntClass c2 = data.createObjectOneOf(data.createIndividual("X"));

        OntClass c3 = data.createObjectSomeValuesFrom(p0, c0);
        OntClass c4 = data.createObjectAllValuesFrom(p0, c2);
        OntClass c5 = data.createDataMinCardinality(p1, 42, data.getDatatype(XSD.xstring));
        OntClass c6 = data.createDataMaxCardinality(p1, 0, data.getDatatype(XSD.xstring));

        OntClass c7 = data.createObjectIntersectionOf(c3, c0);
        OntClass c8 = data.createObjectIntersectionOf(c1, c2);

        OntClass c9 = data.createObjectComplementOf(c2);
        OntClass c10 = data.createObjectComplementOf(c9);

        OntClass c11 = data.createObjectSomeValuesFrom(p0, data.getOWLThing());
        OntClass c12 = data.createDataSomeValuesFrom(p1, data.getDatatype(XSD.xstring));

        OntModel m = OntModelFactory.createModel(data.getGraph(), spec.inst);

        OntClass mc0 = c0.inModel(m).as(OntClass.class);
        Assertions.assertThrows(OntJenaException.Conversion.class, () -> c1.inModel(m).as(OntClass.class));
        Assertions.assertThrows(OntJenaException.Conversion.class, () -> c2.inModel(m).as(OntClass.class));
        OntClass mc3 = c3.inModel(m).as(OntClass.class);
        Assertions.assertThrows(OntJenaException.Conversion.class, () -> c4.inModel(m).as(OntClass.class));
        Assertions.assertThrows(OntJenaException.Conversion.class, () -> c5.inModel(m).as(OntClass.class));
        Assertions.assertThrows(OntJenaException.Conversion.class, () -> c6.inModel(m).as(OntClass.class));
        OntClass mc7 = c7.inModel(m).as(OntClass.class);
        Assertions.assertThrows(OntJenaException.Conversion.class, () ->  c8.inModel(m).as(OntClass.class));
        Assertions.assertThrows(OntJenaException.Conversion.class, () -> c9.inModel(m).as(OntClass.class));
        Assertions.assertThrows(OntJenaException.Conversion.class, () -> c10.inModel(m).as(OntClass.class));
        OntClass mc11 = c11.inModel(m).as(OntClass.class);
        OntClass mc12 = c12.inModel(m).as(OntClass.class);

        Assertions.assertTrue(mc0.canAsEquivalentClass());
        Assertions.assertTrue(mc0.canAsDisjointClass());
        Assertions.assertFalse(mc3.canAsEquivalentClass());
        Assertions.assertFalse(mc3.canAsDisjointClass());
        Assertions.assertFalse(mc7.canAsEquivalentClass());
        Assertions.assertFalse(mc7.canAsDisjointClass());
        Assertions.assertTrue(mc11.canAsEquivalentClass());
        Assertions.assertTrue(mc11.canAsDisjointClass());
        Assertions.assertTrue(mc12.canAsEquivalentClass());
        Assertions.assertTrue(mc12.canAsDisjointClass());

        Assertions.assertSame(mc0, mc0.asEquivalentClass());
        Assertions.assertSame(mc0, mc0.asDisjointClass());
        Assertions.assertSame(mc11, mc11.asEquivalentClass());
        Assertions.assertSame(mc11, mc11.asDisjointClass());
        Assertions.assertSame(mc12, mc12.asEquivalentClass());
        Assertions.assertSame(mc12, mc12.asDisjointClass());
        Assertions.assertThrows(OntJenaException.Unsupported.class, mc3::asDisjointClass);
        Assertions.assertThrows(OntJenaException.Unsupported.class, mc3::asEquivalentClass);
        Assertions.assertThrows(OntJenaException.Unsupported.class, mc7::asDisjointClass);
        Assertions.assertThrows(OntJenaException.Unsupported.class, mc7::asEquivalentClass);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_QL_MEM",
            "OWL2_QL_MEM_RDFS_INF",
            "OWL2_QL_MEM_TRANS_INF",
    })
    public void testHasKey(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst);
        OntClass a = m.createOntClass("a");
        OntDataProperty p1 = m.createDataProperty("p1");
        OntDataProperty p2 = m.createDataProperty("p2");
        Assertions.assertThrows(OntJenaException.Unsupported.class, () -> a.addHasKey(p1, p2));
        Assertions.assertThrows(OntJenaException.Unsupported.class, () -> a.removeHasKey(m.createList()));
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_QL_MEM",
            "OWL2_QL_MEM_RDFS_INF",
            "OWL2_QL_MEM_TRANS_INF",
    })
    public void testDisjointDataProperties(TestSpec spec) {
        OntModel data = OntModelFactory.createModel();
        data.createDisjointDataProperties(data.createDataProperty("a"));
        data.createResource()
                .addProperty(RDF.type, OWL2.AllDisjointProperties)
                .addProperty(OWL2.members, data.createList());
        data.createDisjointDataProperties(data.createDataProperty("b"), data.createDataProperty("c"));

        OntModel m = OntModelFactory.createModel(data.getGraph(), spec.inst);

        List<OntDisjoint.DataProperties> res1 = m.ontObjects(OntDisjoint.DataProperties.class).toList();
        Assertions.assertEquals(1, res1.size());
        Assertions.assertEquals(
                Set.of("b", "c"),
                res1.get(0).members().map(Resource::getURI).collect(Collectors.toSet())
        );

        Assertions.assertThrows(
                OntJenaException.Unsupported.class,
                () -> m.createDisjointDataProperties(m.createDataProperty("d"))
        );

        m.createDisjointDataProperties(m.createDataProperty("e"), m.createDataProperty("f"), m.createDataProperty("g"));

        List<OntDisjoint.DataProperties> res2 = m.ontObjects(OntDisjoint.DataProperties.class).toList();
        Assertions.assertEquals(2, res2.size());
    }
}
