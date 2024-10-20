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

import org.apache.jena.ontapi.model.OntAnnotationProperty;
import org.apache.jena.ontapi.model.OntClass;
import org.apache.jena.ontapi.model.OntDataProperty;
import org.apache.jena.ontapi.model.OntDataRange;
import org.apache.jena.ontapi.model.OntDisjoint;
import org.apache.jena.ontapi.model.OntEntity;
import org.apache.jena.ontapi.model.OntIndividual;
import org.apache.jena.ontapi.model.OntModel;
import org.apache.jena.ontapi.model.OntObject;
import org.apache.jena.ontapi.model.OntObjectProperty;
import org.apache.jena.ontapi.model.OntProperty;
import org.apache.jena.ontapi.testutils.RDFIOTestUtils;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
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
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OntModelOWL2ELSpecTest {

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_EL_MEM",
            "OWL2_EL_MEM_RDFS_INF",
            "OWL2_EL_MEM_TRANS_INF",
    })
    public void testPizzaObjects1b(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(
                RDFIOTestUtils.loadResourceAsModel("/pizza.ttl", Lang.TURTLE).getGraph(), spec.inst);

        Map<Class<? extends OntObject>, Integer> expected = new HashMap<>();
        expected.put(OntClass.ObjectSomeValuesFrom.class, 155);
        expected.put(OntClass.DataSomeValuesFrom.class, 0);
        expected.put(OntClass.ObjectAllValuesFrom.class, 0);
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
        expected.put(OntClass.UnionOf.class, 0);
        expected.put(OntClass.OneOf.class, 0);
        expected.put(OntClass.IntersectionOf.class, 15);
        expected.put(OntClass.ComplementOf.class, 0);
        expected.put(OntClass.NaryDataAllValuesFrom.class, 0);
        expected.put(OntClass.NaryDataSomeValuesFrom.class, 0);
        expected.put(OntClass.LogicalExpression.class, 15);
        expected.put(OntClass.CollectionOf.class, 15);
        expected.put(OntClass.ValueRestriction.class, 161);
        expected.put(OntClass.CardinalityRestriction.class, 0);
        expected.put(OntClass.ComponentRestriction.class, 161);
        expected.put(OntClass.UnaryRestriction.class, 161);
        expected.put(OntClass.Restriction.class, 161);
        expected.put(OntClass.class, 276);

        OntModelOWLSpecsTest.testListObjects(m, expected);

        List<OntClass.Named> classes = m.ontObjects(OntClass.Named.class).toList();
        int expectedClassesCount = m.listStatements(null, RDF.type, OWL2.Class)
                .mapWith(Statement::getSubject).filterKeep(RDFNode::isURIResource).toSet().size();
        int actualClassesCount = classes.size();
        Assertions.assertEquals(expectedClassesCount, actualClassesCount);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_EL_MEM",
            "OWL2_EL_MEM_RDFS_INF",
            "OWL2_EL_MEM_TRANS_INF",
    })
    public void testOntProperties(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst);

        OntObjectProperty p1 = m.createObjectProperty("op-1");
        OntObjectProperty p2 = m.createObjectProperty("op-2");
        OntDataProperty d = m.createDataProperty("dp");
        Resource i = m.createResource().addProperty(OWL2.inverseOf, p1);

        Stream.of(
                OntProperty.class,
                OntObjectProperty.class,
                OntObjectProperty.Named.class,
                OntObjectProperty.Inverse.class,
                OntAnnotationProperty.class,
                OntAnnotationProperty.class).forEach(t -> {
            Assertions.assertFalse(m.createResource("x", OWL2.IrreflexiveProperty).canAs(t));
            Assertions.assertFalse(m.createResource("q", OWL2.InverseFunctionalProperty).canAs(t));
            Assertions.assertFalse(m.createResource("s", OWL2.IrreflexiveProperty).canAs(t));
            Assertions.assertFalse(m.createResource("d", OWL2.SymmetricProperty).canAs(t));
            Assertions.assertFalse(m.createResource("f", OWL2.AsymmetricProperty).canAs(t));
            Assertions.assertFalse(i.canAs(t), "Can as " + t.getSimpleName());
        });

        p1.addProperty(RDF.type, OWL2.FunctionalProperty);
        d.addProperty(RDF.type, OWL2.FunctionalProperty);
        Assertions.assertTrue(d.isFunctional());
        Assertions.assertFalse(p1.isFunctional());

        Assertions.assertThrows(OntJenaException.Unsupported.class, () -> p1.setAsymmetric(true));
        if (spec.isOWL2EL()) {
            Assertions.assertThrows(OntJenaException.Unsupported.class, () -> p1.addInverseProperty(p2));
            if (spec == TestSpec.OWL2_EL_MEM_RDFS_INF) {
                Assertions.assertEquals(17, m.properties().count());
            }
        } else {
            p1.addInverseProperty(p2);
            Assertions.assertEquals(3, m.properties().count());
        }
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_EL_MEM",
            "OWL2_EL_MEM_RDFS_INF",
            "OWL2_EL_MEM_TRANS_INF",
    })
    public void testCreateConstructs(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst);
        OntObjectProperty p1 = m.createObjectProperty("op-1");
        OntObjectProperty p2 = m.createObjectProperty("op-2");
        OntClass.Named c1 = m.createOntClass("c-1");
        OntClass.Named c2 = m.createOntClass("c-2");
        OntDataProperty d = m.createDataProperty("dp");
        Resource i = m.createResource().addProperty(RDF.type, c1);

        Assertions.assertThrows(OntJenaException.Unsupported.class,
                () -> m.createDataAllValuesFrom(d, m.getDatatype(XSD.xstring.getURI()))
        );

        Assertions.assertThrows(OntJenaException.Unsupported.class,
                () -> m.createObjectAllValuesFrom(p1, c1)
        );
        Assertions.assertThrows(OntJenaException.Unsupported.class,
                () -> m.createObjectMinCardinality(p2, 42, c1)
        );
        Assertions.assertThrows(OntJenaException.Unsupported.class,
                () -> c1.createDisjointUnion(List.of(c2))
        );
        Assertions.assertThrows(OntJenaException.Unsupported.class,
                () -> m.createObjectComplementOf(c2)
        );
        Assertions.assertThrows(OntJenaException.Unsupported.class,
                () -> m.createObjectUnionOf(c1)
        );
        Assertions.assertThrows(OntJenaException.Unsupported.class,
                () -> m.createDataUnionOf(m.createDatatype("dr"))
        );
        Assertions.assertThrows(OntJenaException.Unsupported.class,
                () -> m.createDataComplementOf(m.createDatatype("dr"))
        );
        Assertions.assertThrows(OntJenaException.Unsupported.class,
                () -> m.createDataRestriction(m.createDatatype("dr"))
        );

        Assertions.assertFalse(i.canAs(OntIndividual.class));
        Assertions.assertFalse(i.canAs(OntIndividual.Anonymous.class));
        Assertions.assertThrows(OntJenaException.Unsupported.class,
                () -> m.createIndividual(null, c2)
        );
        Assertions.assertThrows(OntJenaException.Unsupported.class,
                c1::createIndividual
        );
        c1.createIndividual("i");
        Assertions.assertEquals(1, m.individuals().count());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_EL_MEM",
            "OWL2_EL_MEM_RDFS_INF",
            "OWL2_EL_MEM_TRANS_INF",
            "OWL2_EL_MEM_RULES_INF",
    })
    public void testObjectOneOf(TestSpec spec) {
        Model m = ModelFactory.createDefaultModel();
        Resource c0 = m.createResource("C", OWL2.Class);
        Resource i1 = m.createResource("i1", c0);
        Resource i2 = m.createResource("i2", c0);
        m.createResource().addProperty(RDF.type, OWL2.Class).addProperty(OWL2.oneOf, m.createList());
        m.createResource().addProperty(RDF.type, OWL2.Class).addProperty(OWL2.oneOf, m.createList(i1));
        m.createResource().addProperty(RDF.type, OWL2.Class).addProperty(OWL2.oneOf, m.createList(i1, i2));

        OntModel om = OntModelFactory.createModel(m.getGraph(), spec.inst);
        OntIndividual oi1 = Objects.requireNonNull(om.getIndividual("i1"));
        OntIndividual oi2 = Objects.requireNonNull(om.getIndividual("i2"));
        OntClass.OneOf oc2 = om.ontObjects(OntClass.OneOf.class).findFirst().orElseThrow(AssertionError::new);
        Assertions.assertEquals(List.of(i1), oc2.getList().members().collect(Collectors.toList()));
        Assertions.assertThrows(OntJenaException.Unsupported.class, () -> om.createObjectOneOf(oi1, oi2));

        Assertions.assertEquals(spec == TestSpec.OWL2_EL_MEM_RULES_INF ? 14 : 2, om.ontObjects(OntClass.class).count());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_EL_MEM",
            "OWL2_EL_MEM_RDFS_INF",
            "OWL2_EL_MEM_TRANS_INF",
            "OWL2_EL_MEM_RULES_INF",
    })
    public void testDataOneOf(TestSpec spec) {
        Model m = ModelFactory.createDefaultModel();
        Literal v1 = m.createTypedLiteral(42);
        Literal v2 = m.createTypedLiteral("42");
        m.createResource().addProperty(RDF.type, RDFS.Datatype).addProperty(OWL2.oneOf, m.createList());
        m.createResource().addProperty(RDF.type, RDFS.Datatype).addProperty(OWL2.oneOf, m.createList(v1));
        m.createResource().addProperty(RDF.type, RDFS.Datatype).addProperty(OWL2.oneOf, m.createList(v1, v2));

        OntModel om = OntModelFactory.createModel(m.getGraph(), spec.inst);
        OntDataRange.OneOf oc2 = om.ontObjects(OntDataRange.OneOf.class).findFirst().orElseThrow(AssertionError::new);
        Assertions.assertEquals(List.of(v1), oc2.getList().members().collect(Collectors.toList()));
        Assertions.assertThrows(OntJenaException.Unsupported.class, () -> om.createDataOneOf(v1, v2));

        int expected;
        if (spec == TestSpec.OWL2_EL_MEM_RDFS_INF) {
            expected = 2;
        } else if (spec == TestSpec.OWL2_EL_MEM_RULES_INF) {
            expected = 18;
        } else {
            expected = 1;
        }
        Assertions.assertEquals(expected, om.ontObjects(OntDataRange.class).count());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_EL_MEM",
            "OWL2_EL_MEM_RDFS_INF",
            "OWL2_EL_MEM_TRANS_INF",
    })
    public void testBuiltins(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst);
        Assertions.assertNotNull(m.getOWLThing());
        Assertions.assertNotNull(m.getOWLNothing());
        Assertions.assertNotNull(m.getOWLBottomDataProperty());
        Assertions.assertNotNull(m.getOWLBottomObjectProperty());
        Assertions.assertNotNull(m.getOWLTopObjectProperty());
        Assertions.assertNotNull(m.getOWLTopDataProperty());

        OWL2.real.inModel(m).as(OntEntity.class);
        OWL2.rational.inModel(m).as(OntEntity.class);
        XSD.xstring.inModel(m).as(OntEntity.class);
        Stream.of(XSD.xdouble, XSD.xfloat, XSD.nonPositiveInteger, XSD.positiveInteger, XSD.negativeInteger,
                        XSD.xlong, XSD.xint, XSD.xshort, XSD.unsignedLong, XSD.unsignedInt, XSD.unsignedShort,
                        XSD.unsignedByte, XSD.language, XSD.xboolean)
                .forEach(it -> Assertions.assertThrows(
                        OntJenaException.class,
                        () -> it.inModel(m).as(OntEntity.class),
                        "wrong result for " + it)
                );
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_EL_MEM",
            "OWL2_EL_MEM_RDFS_INF",
            "OWL2_EL_MEM_TRANS_INF",
    })
    public void testHasKey(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst);
        OntClass a = m.createOntClass("a");
        OntDataProperty p1 = m.createDataProperty("p1");
        OntDataProperty p2 = m.createDataProperty("p2");
        a.addHasKey(p1, p2);
        Assertions.assertEquals(1L, a.hasKeys().count());
        a.removeHasKey(null);
        Assertions.assertEquals(0L, a.hasKeys().count());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_EL_MEM",
            "OWL2_EL_MEM_RDFS_INF",
            "OWL2_EL_MEM_TRANS_INF",
    })
    public void testDifferentIndividuals(TestSpec spec) {
        OntModel data = OntModelFactory.createModel();
        data.createDifferentIndividuals(data.createIndividual("a"));
        data.createResource()
                .addProperty(RDF.type, OWL2.AllDifferent)
                .addProperty(OWL2.members, data.createList());
        data.createDifferentIndividuals(data.createIndividual("b"), data.createIndividual("c"));

        OntModel m = OntModelFactory.createModel(data.getGraph(), spec.inst);

        List<OntDisjoint.Individuals> res1 = m.ontObjects(OntDisjoint.Individuals.class).toList();
        Assertions.assertEquals(1, res1.size());
        Assertions.assertEquals(
                Set.of("b", "c"),
                res1.get(0).members().map(Resource::getURI).collect(Collectors.toSet())
        );

        Assertions.assertThrows(
                OntJenaException.Unsupported.class,
                () -> m.createDifferentIndividuals(m.createIndividual("d"))
        );

        m.createDifferentIndividuals(m.createIndividual("e"), m.createIndividual("f"), m.createIndividual("g"));

        List<OntDisjoint.Individuals> res2 = m.ontObjects(OntDisjoint.Individuals.class).toList();
        Assertions.assertEquals(2, res2.size());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_EL_MEM",
            "OWL2_EL_MEM_RDFS_INF",
            "OWL2_EL_MEM_TRANS_INF",
    })
    public void testDisjointClasses(TestSpec spec) {
        OntModel data = OntModelFactory.createModel();
        data.createDisjointClasses(data.createOntClass("a"));
        data.createResource()
                .addProperty(RDF.type, OWL2.AllDisjointClasses)
                .addProperty(OWL2.members, data.createList());
        data.createDisjointClasses(data.createOntClass("b"), data.createOntClass("c"));

        OntModel m = OntModelFactory.createModel(data.getGraph(), spec.inst);

        List<OntDisjoint.Classes> res1 = m.ontObjects(OntDisjoint.Classes.class).toList();
        Assertions.assertEquals(1, res1.size());
        Assertions.assertEquals(
                Set.of("b", "c"),
                res1.get(0).members().map(Resource::getURI).collect(Collectors.toSet())
        );

        Assertions.assertThrows(
                OntJenaException.Unsupported.class,
                () -> m.createDisjointClasses(m.createOntClass("d"))
        );

        m.createDisjointClasses(m.createOntClass("e"), m.createOntClass("f"), m.createOntClass("g"));

        List<OntDisjoint.Classes> res2 = m.ontObjects(OntDisjoint.Classes.class).toList();
        Assertions.assertEquals(2, res2.size());
    }
}
