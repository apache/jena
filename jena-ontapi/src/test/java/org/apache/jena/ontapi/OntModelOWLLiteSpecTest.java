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
import org.apache.jena.ontapi.model.OntNamedProperty;
import org.apache.jena.ontapi.model.OntObject;
import org.apache.jena.ontapi.model.OntObjectProperty;
import org.apache.jena.ontapi.model.OntProperty;
import org.apache.jena.ontapi.model.OntRelationalProperty;
import org.apache.jena.ontapi.testutils.RDFIOTestUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDFS;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.apache.jena.ontapi.OntModelOWLSpecsTest.assertOntObjectsCount;

public class OntModelOWLLiteSpecTest {

    @ParameterizedTest
    @EnumSource(names = {
            "OWL1_LITE_MEM",
    })
    public void testFamilyListObjectsOWL1Lite(TestSpec spec) {
        String ns = "http://www.co-ode.org/roberts/family-tree.owl#";
        OntModel m = OntModelFactory.createModel(
                RDFIOTestUtils.loadResourceAsModel("/family.ttl", Lang.TURTLE).getGraph(),
                spec.inst);

        List<OntClass> equivalentToWife = m.getOntClass(ns + "Wife").equivalentClasses().toList();
        Assertions.assertEquals(1, equivalentToWife.size());
        Assertions.assertInstanceOf(OntClass.IntersectionOf.class, equivalentToWife.get(0));
        Assertions.assertEquals(OntClass.IntersectionOf.class, equivalentToWife.get(0).objectType());
        List<OntClass> equivalentToSex = m.getOntClass(ns + "Sex").equivalentClasses().toList();
        Assertions.assertEquals(1, equivalentToSex.size());
        // generic class:
        Assertions.assertFalse(equivalentToSex.get(0) instanceof OntClass.UnionOf);
        Assertions.assertNotEquals(OntClass.UnionOf.class, equivalentToSex.get(0).objectType());

        assertOntObjectsCount(m, OntObject.class, 1684);
        assertOntObjectsCount(m, OntEntity.class, 151);
        assertOntObjectsCount(m, OntNamedProperty.class, 90);
        assertOntObjectsCount(m, OntClass.Named.class, 58);
        assertOntObjectsCount(m, OntDataRange.Named.class, 0);
        // owl:NamedIndividual is not valid class-type in OWL1:
        assertOntObjectsCount(m, OntIndividual.Named.class, 3);
        assertOntObjectsCount(m, OntObjectProperty.Named.class, 80);
        assertOntObjectsCount(m, OntAnnotationProperty.class, 1);
        assertOntObjectsCount(m, OntDataProperty.class, 9);

        assertOntObjectsCount(m, OntObjectProperty.class, 80);
        assertOntObjectsCount(m, OntRelationalProperty.class, 89);
        assertOntObjectsCount(m, OntProperty.class, 90);

        assertOntObjectsCount(m, OntDataRange.class, 0);
        assertOntObjectsCount(m, OntDataRange.Named.class, 0);
        assertOntObjectsCount(m, OntDataRange.OneOf.class, 0);
        assertOntObjectsCount(m, OntDataRange.Restriction.class, 0);
        assertOntObjectsCount(m, OntDataRange.UnionOf.class, 0);
        assertOntObjectsCount(m, OntDataRange.ComplementOf.class, 0);
        assertOntObjectsCount(m, OntDataRange.IntersectionOf.class, 0);
        assertOntObjectsCount(m, OntDataRange.Combination.class, 0);

        assertOntObjectsCount(m, OntDisjoint.class, 1);
        assertOntObjectsCount(m, OntDisjoint.Classes.class, 0);
        assertOntObjectsCount(m, OntDisjoint.Individuals.class, 1);
        assertOntObjectsCount(m, OntDisjoint.DataProperties.class, 0);
        assertOntObjectsCount(m, OntDisjoint.ObjectProperties.class, 0);
        assertOntObjectsCount(m, OntDisjoint.Properties.class, 0);

        assertOntObjectsCount(m, OntClass.class, 289);
        assertOntObjectsCount(m, OntClass.Named.class, 58);
        assertOntObjectsCount(m, OntClass.CollectionOf.class, 109);
        assertOntObjectsCount(m, OntClass.LogicalExpression.class, 109);
        assertOntObjectsCount(m, OntClass.ValueRestriction.class, 111);
        assertOntObjectsCount(m, OntClass.UnaryRestriction.class, 111);
        assertOntObjectsCount(m, OntClass.Restriction.class, 111);
        assertOntObjectsCount(m, OntClass.NaryRestriction.class, 0);
        assertOntObjectsCount(m, OntClass.ComponentRestriction.class, 111);
        assertOntObjectsCount(m, OntClass.CardinalityRestriction.class, 0);
        assertOntObjectsCount(m, OntClass.CollectionOf.class, 109);
        assertOntObjectsCount(m, OntClass.IntersectionOf.class, 109);
        assertOntObjectsCount(m, OntClass.UnionOf.class, 0);
        assertOntObjectsCount(m, OntClass.OneOf.class, 0);
        assertOntObjectsCount(m, OntClass.ObjectCardinality.class, 0);
        assertOntObjectsCount(m, OntClass.ObjectMinCardinality.class, 0);
        assertOntObjectsCount(m, OntClass.ObjectMinCardinality.class, 0);
        assertOntObjectsCount(m, OntClass.ObjectHasValue.class, 0);
        assertOntObjectsCount(m, OntClass.ObjectSomeValuesFrom.class, 111);
        assertOntObjectsCount(m, OntClass.ObjectAllValuesFrom.class, 0);
        assertOntObjectsCount(m, OntClass.DataCardinality.class, 0);
        assertOntObjectsCount(m, OntClass.DataMinCardinality.class, 0);
        assertOntObjectsCount(m, OntClass.DataMinCardinality.class, 0);
        assertOntObjectsCount(m, OntClass.DataHasValue.class, 0);
        assertOntObjectsCount(m, OntClass.DataSomeValuesFrom.class, 0);
        assertOntObjectsCount(m, OntClass.DataAllValuesFrom.class, 0);
        assertOntObjectsCount(m, OntClass.HasSelf.class, 0);
        assertOntObjectsCount(m, OntClass.NaryDataAllValuesFrom.class, 0);
        assertOntObjectsCount(m, OntClass.NaryDataSomeValuesFrom.class, 0);
    }


    @ParameterizedTest
    @EnumSource(names = {
            "OWL1_LITE_MEM",
    })
    public void testDataRangesForOWL1Lite(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst);
        Assertions.assertEquals(0, m.ontObjects(OntDataRange.class).count());
        Assertions.assertEquals(0, m.ontObjects(OntDataRange.Combination.class).count());
        Assertions.assertEquals(0, m.ontObjects(OntDataRange.OneOf.class).count());
        Assertions.assertEquals(0, m.ontObjects(OntDataRange.Restriction.class).count());
        Assertions.assertEquals(0, m.datatypes().count());

        m.createResource("X", OWL2.DataRange)
                .addProperty(OWL2.oneOf, m.createList(m.createLiteral("A")));
        m.createResource(null, OWL2.DataRange)
                .addProperty(OWL2.oneOf, m.createList(m.createLiteral("B")));
        m.createResource(null, RDFS.Datatype)
                .addProperty(OWL2.oneOf, m.createList(m.createLiteral("C")));
        m.createResource("Q", RDFS.Datatype)
                .addProperty(OWL2.oneOf, m.createList(m.createLiteral("D")));

        Assertions.assertThrows(OntJenaException.Unsupported.class, () -> m.createDataOneOf(m.createTypedLiteral(42)));
        Assertions.assertThrows(OntJenaException.Unsupported.class, m::createDataUnionOf);
        Assertions.assertThrows(OntJenaException.Unsupported.class, m::createDataIntersectionOf);

        Assertions.assertEquals(0, m.ontObjects(OntDataRange.class).count());
        Assertions.assertEquals(0, m.ontObjects(OntDataRange.Combination.class).count());
        Assertions.assertEquals(0, m.ontObjects(OntDataRange.OneOf.class).count());
        Assertions.assertEquals(0, m.ontObjects(OntDataRange.Restriction.class).count());
        Assertions.assertEquals(0, m.datatypes().count());
    }

    @Test
    public void testDisabledFeaturesOWL1Lite() {
        Model d = OntModelFactory.createDefaultModel();
        d.createResource("X", OWL2.Class).addProperty(OWL2.disjointWith, d.createResource("Q", OWL2.Class));
        d.createResource("iW", d.createResource("W", OWL2.Class))
                .addProperty(OWL2.sameAs, d.createResource("iQ", d.getResource("Q")));

        OntModel m = OntModelFactory.createModel(d.getGraph(), OntSpecification.OWL1_LITE_MEM);
        OntClass.Named x = m.getOntClass("X");
        OntClass.Named q = m.getOntClass("Q");
        OntIndividual iW = m.getIndividual("iW");
        OntIndividual iQ = m.getIndividual("iQ");

        Assertions.assertThrows(OntJenaException.Unsupported.class, () -> x.addDisjointClass(q));
        Assertions.assertThrows(OntJenaException.Unsupported.class, () -> x.removeDisjointClass(q));
        Assertions.assertEquals(0, x.disjoints().count());

        Assertions.assertThrows(OntJenaException.Unsupported.class, () -> iW.addSameIndividual(iQ));
        Assertions.assertThrows(OntJenaException.Unsupported.class, () -> iQ.removeSameIndividual(iW));
        Assertions.assertEquals(0, iW.sameIndividuals().count());
    }
}
