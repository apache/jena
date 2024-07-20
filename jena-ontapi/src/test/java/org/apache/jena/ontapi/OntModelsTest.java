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
import org.apache.jena.ontapi.model.OntModel;
import org.apache.jena.ontapi.testutils.RDFIOTestUtils;
import org.apache.jena.ontapi.utils.OntModels;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.OWL2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class OntModelsTest {

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_DL_MEM",
            "OWL2_DL_MEM_RDFS_INF",
            "OWL2_MEM",
            "OWL2_MEM_RDFS_INF",
            "OWL2_MEM_MICRO_RULES_INF",
            "OWL2_EL_MEM",
            "OWL2_EL_MEM_RDFS_INF",
            "OWL2_QL_MEM",
            "OWL2_QL_MEM_RDFS_INF",
            "OWL2_RL_MEM",
            "OWL2_RL_MEM_RDFS_INF",
            "OWL1_DL_MEM",
            "OWL1_DL_MEM_RDFS_INF",
            "OWL1_MEM",
            "OWL1_MEM_RDFS_INF",
            "OWL1_MEM_MICRO_RULES_INF",
            "OWL1_LITE_MEM",
            "OWL1_LITE_MEM_RDFS_INF",
            "RDFS_MEM",
            "RDFS_MEM_TRANS_INF",
    })
    public void testNamedHierarchyRoots1a(TestSpec spec) {
        //     A
        //   /  / \
        //  /  B   C
        //  | / \ / \
        //  D   E   F
        // / \
        // G  H = K
        //       / \
        //      L   M
        OntModel m = TestModelFactory.createClassesABCDEFGHKLM(OntModelFactory.createModel(spec.inst));
        OntClass A = m.getOntClass(TestModelFactory.NS + "A");
        Assertions.assertEquals(List.of(A), OntModels.namedHierarchyRoots(m).toList());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_TRANS_INF",
            "OWL2_DL_MEM_RULES_INF",
            "OWL2_MEM_TRANS_INF",
            "OWL2_MEM_RULES_INF",
            "OWL2_MEM_MINI_RULES_INF",
            "OWL2_EL_MEM_TRANS_INF",
            "OWL2_EL_MEM_RULES_INF",
            "OWL2_QL_MEM_TRANS_INF",
            "OWL2_QL_MEM_RULES_INF",
            "OWL2_RL_MEM_TRANS_INF",
            "OWL2_RL_MEM_RULES_INF",
            "OWL1_DL_MEM_TRANS_INF",
            "OWL1_DL_MEM_RULES_INF",
            "OWL1_MEM_TRANS_INF",
            "OWL1_MEM_RULES_INF",
            "OWL1_MEM_MINI_RULES_INF",
            "OWL1_LITE_MEM_TRANS_INF",
            "OWL1_LITE_MEM_RULES_INF",
            "RDFS_MEM_RDFS_INF",
    })
    public void testNamedHierarchyRoots1b(TestSpec spec) {
        //     A
        //   /  / \
        //  /  B   C
        //  | / \ / \
        //  D   E   F
        // / \
        // G  H = K
        //       / \
        //      L   M
        OntModel m = TestModelFactory.createClassesABCDEFGHKLM(OntModelFactory.createModel(spec.inst));
        Assertions.assertEquals(List.of(), OntModels.namedHierarchyRoots(m).toList());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_DL_MEM",
            "OWL2_DL_MEM_RDFS_INF",
            "OWL2_MEM",
            "OWL2_MEM_RDFS_INF",
            "OWL2_EL_MEM",
            "OWL2_EL_MEM_RDFS_INF",
            "OWL2_QL_MEM",
            "OWL2_QL_MEM_RDFS_INF",
            "OWL2_RL_MEM",
            "OWL2_RL_MEM_RDFS_INF",
            "OWL1_DL_MEM",
            "OWL1_DL_MEM_RDFS_INF",
            "OWL1_MEM",
            "OWL1_MEM_RDFS_INF",
            "OWL1_LITE_MEM",
            "OWL1_LITE_MEM_RDFS_INF",
    })
    public void testNamedHierarchyRoots2a(TestSpec spec) {
        OntModel m = RDFIOTestUtils.readResourceToModel(OntModelFactory.createModel(spec.inst), "/pizza.ttl", Lang.TURTLE);
        Set<String> actual = OntModels.namedHierarchyRoots(m).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> expected = Set.of(
                "NonVegetarianPizza",
                "VegetarianTopping",
                "DomainConcept",
                "SpicyPizza",
                "VegetarianPizza",
                "SpicyPizzaEquivalent",
                "MeatyPizza",
                "CheeseyPizza",
                "VegetarianPizzaEquivalent2",
                "Country",
                "ThinAndCrispyPizza",
                "SpicyTopping",
                "ValuePartition",
                "VegetarianPizzaEquivalent1",
                "InterestingPizza",
                "RealItalianPizza");
        Assertions.assertEquals(expected, actual);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "RDFS_MEM_RDFS_INF",
    })
    public void testNamedHierarchyRoots2b(TestSpec spec) {
        OntModel m = RDFIOTestUtils.readResourceToModel(OntModelFactory.createModel(spec.inst), "/pizza.ttl", Lang.TURTLE);
        Set<String> actual = OntModels.namedHierarchyRoots(m).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> expected = Set.of(
                // reserved in OWL, but not in RDFS
                "ObjectProperty",
                "InverseFunctionalProperty",
                "Ontology",
                "TransitiveProperty",
                "FunctionalProperty",
                "Restriction",
                "AllDifferent",
                "Class",
                "NamedIndividual",
                "Thing",

                "DomainConcept",
                "Country",
                "ValuePartition",
                "RealItalianPizza");
        Assertions.assertEquals(expected, actual);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_TRANS_INF",
            "OWL2_MEM_TRANS_INF",
            "OWL2_EL_MEM_TRANS_INF",
            "OWL2_QL_MEM_TRANS_INF",
            "OWL2_RL_MEM_TRANS_INF",
            "OWL1_DL_MEM_TRANS_INF",
            "OWL1_MEM_TRANS_INF",
            "OWL1_LITE_MEM_TRANS_INF",
            "RDFS_MEM",
            "RDFS_MEM_TRANS_INF",
    })
    public void testNamedHierarchyRoots2c(TestSpec spec) {
        OntModel m = RDFIOTestUtils.readResourceToModel(OntModelFactory.createModel(spec.inst), "/pizza.ttl", Lang.TURTLE);
        Set<String> actual = OntModels.namedHierarchyRoots(m).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> expected = Set.of();
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void testNamedHierarchyRoots3() {
        OntModel m = OntModelFactory.createModel(OntSpecification.OWL2_FULL_MEM_MICRO_RULES_INF);
        OntClass A = m.createOntClass(TestModelFactory.NS + "A");
        OntClass B = m.createOntClass(TestModelFactory.NS + "B");
        OntClass C = m.createOntClass(TestModelFactory.NS + "C");
        OntClass D = m.createOntClass(TestModelFactory.NS + "D");
        OntClass E = m.createOntClass(TestModelFactory.NS + "E");
        OntClass F = m.createOntClass(TestModelFactory.NS + "F");
        OntClass G = m.createOntClass(TestModelFactory.NS + "G");

        A.addSubClass(B);
        B.addSubClass(C);
        C.addSubClass(D);
        E.addSubClass(E);
        E.addSubClass(F);

        List<OntClass.Named> nhr = OntModels.namedHierarchyRoots(m).toList();
        Assertions.assertEquals(3, nhr.size());
        Assertions.assertTrue(nhr.contains(A));
        Assertions.assertTrue(nhr.contains(E));
        Assertions.assertTrue(nhr.contains(G));
    }

    @Test
    public void testNamedHierarchyRoots4() {
        OntModel m = OntModelFactory.createModel(OntSpecification.OWL2_FULL_MEM_MICRO_RULES_INF);
        OntClass A = m.createOntClass(TestModelFactory.NS + "A");
        OntClass B = m.createOntClass(TestModelFactory.NS + "B");
        OntClass C = m.createOntClass(TestModelFactory.NS + "C");
        OntClass D = m.createOntClass(TestModelFactory.NS + "D");
        OntClass E = m.createOntClass(TestModelFactory.NS + "E");
        OntClass F = m.createOntClass(TestModelFactory.NS + "F");
        OntClass G = m.createOntClass(TestModelFactory.NS + "G");

        A.addSubClass(B);
        B.addSubClass(C);
        C.addSubClass(D);
        E.addSubClass(E);
        E.addSubClass(F);

        OntClass anon0 = m.createObjectUnionOf(A, F);
        anon0.addSubClass(A);
        anon0.addSubClass(E);

        List<OntClass.Named> nhr = OntModels.namedHierarchyRoots(m).toList();
        Assertions.assertEquals(3, nhr.size());
        Assertions.assertTrue(nhr.contains(A));
        Assertions.assertTrue(nhr.contains(E));
        Assertions.assertTrue(nhr.contains(G));
    }

    @Test
    public void testNamedHierarchyRoots5() {
        OntModel m = OntModelFactory.createModel(OntSpecification.OWL2_FULL_MEM_MICRO_RULES_INF);
        OntClass A = m.createOntClass(TestModelFactory.NS + "A");
        OntClass B = m.createOntClass(TestModelFactory.NS + "B");
        OntClass C = m.createOntClass(TestModelFactory.NS + "C");
        OntClass D = m.createOntClass(TestModelFactory.NS + "D");
        OntClass E = m.createOntClass(TestModelFactory.NS + "E");
        OntClass F = m.createOntClass(TestModelFactory.NS + "F");
        OntClass G = m.createOntClass(TestModelFactory.NS + "G");

        OntClass anon0 = m.createObjectUnionOf(A, B, G);
        OntClass anon1 = m.createObjectUnionOf(C, D, anon0);
        anon0.addSubClass(A);
        anon0.addSubClass(E);
        anon0.addSubClass(anon1);
        anon1.addSubClass(G);

        A.addSubClass(B);
        B.addSubClass(C);
        C.addSubClass(D);
        E.addSubClass(E);
        E.addSubClass(F);

        List<OntClass.Named> nhr = OntModels.namedHierarchyRoots(m).toList();
        Assertions.assertEquals(3, nhr.size());
        Assertions.assertTrue(nhr.contains(A));
        Assertions.assertTrue(nhr.contains(E));
        Assertions.assertTrue(nhr.contains(G));
    }

    @Test
    public void testNamedHierarchyRoots6() {
        OntModel m = OntModelFactory.createModel(OntSpecification.OWL2_FULL_MEM_MICRO_RULES_INF);
        OntClass A = m.createOntClass(TestModelFactory.NS + "A");
        OntClass B = m.createOntClass(TestModelFactory.NS + "B");
        OntClass C = m.createOntClass(TestModelFactory.NS + "C");
        OntClass D = m.createOntClass(TestModelFactory.NS + "D");
        OntClass E = m.createOntClass(TestModelFactory.NS + "E");
        OntClass F = m.createOntClass(TestModelFactory.NS + "F");
        OntClass G = m.createOntClass(TestModelFactory.NS + "G");

        OntClass anon0 = m.createObjectComplementOf(F);
        OntClass anon1 = m.createObjectUnionOf(F);
        anon0.addSubClass(A);
        anon1.addSubClass(A);

        // only A is root
        A.addSubClass(B);
        A.addSubClass(C);
        A.addSubClass(D);
        A.addSubClass(E);
        A.addSubClass(F);
        A.addSubClass(G);

        List<OntClass.Named> nhr = OntModels.namedHierarchyRoots(m).toList();
        Assertions.assertEquals(List.of(A), nhr);
    }

    @Test
    public void testNamedHierarchyRoots7() {
        OntModel m = OntModelFactory.createModel(OntSpecification.OWL2_FULL_MEM_MICRO_RULES_INF);
        OntClass A = m.createOntClass(TestModelFactory.NS + "A");
        OntClass B = m.createOntClass(TestModelFactory.NS + "B");
        OntClass C = m.createOntClass(TestModelFactory.NS + "C");
        OntClass D = m.createOntClass(TestModelFactory.NS + "D");
        OntClass E = m.createOntClass(TestModelFactory.NS + "E");
        OntClass F = m.createOntClass(TestModelFactory.NS + "F");
        OntClass G = m.createOntClass(TestModelFactory.NS + "G");

        OntClass anon0 = m.createObjectUnionOf(A, B);
        OntClass anon1 = m.createObjectUnionOf(C, D, anon0);
        anon0.addSubClass(A);
        anon1.addSubClass(B);

        // only A is root, because B is a subclass of A
        // even though B is a subclass of an anon root
        A.addSubClass(B);
        A.addSubClass(C);
        A.addSubClass(D);
        A.addSubClass(E);
        A.addSubClass(F);
        A.addSubClass(G);

        List<OntClass.Named> nhr = OntModels.namedHierarchyRoots(m).toList();
        Assertions.assertEquals(List.of(A), nhr);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_DL_MEM",
            "OWL2_DL_MEM_RDFS_INF",
            "OWL2_MEM",
            "OWL2_MEM_RDFS_INF",
            "OWL1_DL_MEM",
            "OWL1_DL_MEM_RDFS_INF",
            "OWL1_MEM",
            "OWL1_MEM_RDFS_INF",
    })
    public void testNamedHierarchyRoots9a(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst);

        OntClass c0 = m.createOntClass(":C0");
        OntClass c1 = m.createOntClass(":C1");
        OntClass c2 = m.createOntClass(":C2");
        OntClass c3 = m.createOntClass(":C3");
        OntClass c4 = m.createOntClass(":C4");
        OntClass c5 = m.createOntClass(":C5");
        OntClass c6 = m.createOntClass(":C6");
        OntClass c7 = m.createOntClass(":C7");
        OntClass c8 = m.createOntClass(":C8");
        OntClass c9 = m.createDataSomeValuesFrom(m.createDataProperty(":p1"), m.createDataOneOf(m.createLiteral("42")));
        OntClass c10 = m.createObjectOneOf(m.createIndividual(null, c0), m.createIndividual(null, c1));
        OntClass c11 = m.createObjectComplementOf(c6);
        OntClass c12 = OWL.Thing.inModel(m).as(OntClass.class);
        OntClass c13 = OWL.Nothing.inModel(m).as(OntClass.class);

        c1.addSuperClass(c2);
        c2.addSuperClass(c3);
        c3.addSuperClass(c4);
        c5.addSuperClass(c6);
        c6.addSuperClass(c13);
        c8.addSuperClass(c11);
        c9.addSuperClass(c10);
        c10.addSuperClass(c5);
        c10.addSuperClass(c7);
        c11.addSuperClass(c12);

        Set<OntClass> actual = OntModels.namedHierarchyRoots(m).collect(Collectors.toSet());
        Set<Resource> expected = Set.of(c0, c4, c7, c8);

        Assertions.assertEquals(expected, actual);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RULES_INF",
            "OWL2_MEM_RULES_INF",
            "OWL2_MEM_MINI_RULES_INF",
            "OWL1_DL_MEM_RULES_INF",
            "OWL1_MEM_RULES_INF",
            "OWL1_MEM_MINI_RULES_INF",
    })
    public void testNamedHierarchyRoots9b(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst);

        OntClass c0 = m.createOntClass(":C0");
        OntClass c1 = m.createOntClass(":C1");
        OntClass c2 = m.createOntClass(":C2");
        OntClass c3 = m.createOntClass(":C3");
        OntClass c4 = m.createOntClass(":C4");
        OntClass c5 = m.createOntClass(":C5");
        OntClass c6 = m.createOntClass(":C6");
        OntClass c7 = m.createOntClass(":C7");
        OntClass c8 = m.createOntClass(":C8");
        OntClass c9 = m.createDataSomeValuesFrom(m.createDataProperty(":p1"), m.createDataOneOf(m.createLiteral("42")));
        OntClass c10 = m.createObjectOneOf(m.createIndividual(null, c0), m.createIndividual(null, c1));
        OntClass c11 = m.createObjectComplementOf(c6);
        OntClass c12 = OWL.Thing.inModel(m).as(OntClass.class);
        OntClass c13 = OWL.Nothing.inModel(m).as(OntClass.class);

        c1.addSuperClass(c2);
        c2.addSuperClass(c3);
        c3.addSuperClass(c4);
        c5.addSuperClass(c6);
        c6.addSuperClass(c13);
        c8.addSuperClass(c11);
        c9.addSuperClass(c10);
        c10.addSuperClass(c5);
        c10.addSuperClass(c7);
        c11.addSuperClass(c12);

        List<OntClass.Named> actual = OntModels.namedHierarchyRoots(m).toList();
        Assertions.assertTrue(actual.isEmpty());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_TRANS_INF",
            "OWL2_MEM_TRANS_INF",
            "OWL1_DL_MEM_TRANS_INF",
            "OWL1_MEM_TRANS_INF",
    })
    public void testNamedHierarchyRoots9c(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst);

        OntClass c0 = m.createOntClass(":C0");
        OntClass c1 = m.createOntClass(":C1");
        OntClass c2 = m.createOntClass(":C2");
        OntClass c3 = m.createOntClass(":C3");
        OntClass c4 = m.createOntClass(":C4");
        OntClass c5 = m.createOntClass(":C5");
        OntClass c6 = m.createOntClass(":C6");
        OntClass c7 = m.createOntClass(":C7");
        OntClass c8 = m.createOntClass(":C8");
        OntClass c9 = m.createDataSomeValuesFrom(m.createDataProperty(":p1"), m.createDataOneOf(m.createLiteral("42")));
        OntClass c10 = m.createObjectOneOf(m.createIndividual(null, c0), m.createIndividual(null, c1));
        OntClass c11 = m.createObjectComplementOf(c6);
        OntClass c12 = OWL.Thing.inModel(m).as(OntClass.class);
        OntClass c13 = OWL.Nothing.inModel(m).as(OntClass.class);

        c1.addSuperClass(c2);
        c2.addSuperClass(c3);
        c3.addSuperClass(c4);
        c5.addSuperClass(c6);
        c6.addSuperClass(c13);
        c8.addSuperClass(c11);
        c9.addSuperClass(c10);
        c10.addSuperClass(c5);
        c10.addSuperClass(c7);
        c11.addSuperClass(c12);

        List<OntClass.Named> actual = OntModels.namedHierarchyRoots(m).toList();
        Set<Resource> expected = Set.of(c12, c8);

        Assertions.assertEquals(2, actual.size());
        Assertions.assertEquals(expected, new HashSet<>(actual));
    }

    @Test
    public void testIndexLCA0() {
        OntModel m = TestModelFactory.createClassesABCDEFGThing(
                OntModelFactory.createModel(OntSpecification.OWL2_FULL_MEM_MICRO_RULES_INF)
        );
        OntClass A = m.getOntClass(TestModelFactory.NS + "A");
        OntClass B = m.getOntClass(TestModelFactory.NS + "B");
        OntClass C = m.getOntClass(TestModelFactory.NS + "C");
        A.addSubClass(B);
        A.addSubClass(C);

        Assertions.assertEquals(A, OntModels.getLCA(B, C));
    }

    @Test
    public void testIndexLCA1() {
        OntModel m = TestModelFactory.createClassesABCDEFGThing(
                OntModelFactory.createModel(OntSpecification.OWL2_FULL_MEM_MICRO_RULES_INF)
        );
        OntClass A = m.getOntClass(TestModelFactory.NS + "A");
        OntClass B = m.getOntClass(TestModelFactory.NS + "B");
        OntClass C = m.getOntClass(TestModelFactory.NS + "C");
        A.addSubClass(B);
        A.addSubClass(C);

        Assertions.assertEquals(A, OntModels.getLCA(C, B));
    }

    @Test
    public void testIndexLCA2() {
        OntModel m = TestModelFactory.createClassesABCDEFGThing(
                OntModelFactory.createModel(OntSpecification.OWL2_FULL_MEM_MICRO_RULES_INF)
        );
        OntClass A = m.getOntClass(TestModelFactory.NS + "A");
        OntClass B = m.getOntClass(TestModelFactory.NS + "B");
        OntClass C = m.getOntClass(TestModelFactory.NS + "C");
        A.addSubClass(B);
        A.addSubClass(C);

        Assertions.assertEquals(A, OntModels.getLCA(A, C));
    }

    @Test
    public void testIndexLCA3() {
        OntModel m = TestModelFactory.createClassesABCDEFGThing(
                OntModelFactory.createModel(OntSpecification.OWL2_FULL_MEM_MICRO_RULES_INF)
        );
        OntClass A = m.getOntClass(TestModelFactory.NS + "A");
        OntClass B = m.getOntClass(TestModelFactory.NS + "B");
        OntClass C = m.getOntClass(TestModelFactory.NS + "C");
        A.addSubClass(B);
        A.addSubClass(C);

        Assertions.assertEquals(A, OntModels.getLCA(B, A));
    }

    @Test
    public void testIndexLCA4() {
        OntModel m = TestModelFactory.createClassesABCDEFGThing(
                OntModelFactory.createModel(OntSpecification.OWL2_FULL_MEM_MICRO_RULES_INF)
        );
        OntClass A = m.getOntClass(TestModelFactory.NS + "A");
        OntClass B = m.getOntClass(TestModelFactory.NS + "B");
        OntClass C = m.getOntClass(TestModelFactory.NS + "C");
        OntClass D = m.getOntClass(TestModelFactory.NS + "D");
        A.addSubClass(B);
        A.addSubClass(C);
        B.addSubClass(D);

        Assertions.assertEquals(A, OntModels.getLCA(D, C));
    }

    @Test
    public void testIndexLCA5() {
        OntModel m = TestModelFactory.createClassesABCDEFGThing(
                OntModelFactory.createModel(OntSpecification.OWL2_FULL_MEM_MICRO_RULES_INF)
        );
        OntClass A = m.getOntClass(TestModelFactory.NS + "A");
        OntClass B = m.getOntClass(TestModelFactory.NS + "B");
        OntClass C = m.getOntClass(TestModelFactory.NS + "C");
        OntClass D = m.getOntClass(TestModelFactory.NS + "D");
        A.addSubClass(B);
        A.addSubClass(C);
        B.addSubClass(D);

        Assertions.assertEquals(A, OntModels.getLCA(C, D));
    }

    @Test
    public void testIndexLCA6() {
        OntModel m = TestModelFactory.createClassesABCDEFGThing(
                OntModelFactory.createModel(OntSpecification.OWL2_FULL_MEM_MICRO_RULES_INF)
        );
        OntClass A = m.getOntClass(TestModelFactory.NS + "A");
        OntClass B = m.getOntClass(TestModelFactory.NS + "B");
        OntClass C = m.getOntClass(TestModelFactory.NS + "C");
        OntClass D = m.getOntClass(TestModelFactory.NS + "D");
        OntClass E = m.getOntClass(TestModelFactory.NS + "E");

        A.addSubClass(B);
        A.addSubClass(C);
        B.addSubClass(D);
        C.addSubClass(E);

        Assertions.assertEquals(A, OntModels.getLCA(D, E));
    }

    @Test
    public void testIndexLCA7() {
        OntModel m = TestModelFactory.createClassesABCDEFGThing(
                OntModelFactory.createModel(OntSpecification.OWL2_FULL_MEM_MICRO_RULES_INF)
        );
        OntClass A = m.getOntClass(TestModelFactory.NS + "A");
        OntClass B = m.getOntClass(TestModelFactory.NS + "B");
        OntClass C = m.getOntClass(TestModelFactory.NS + "C");
        OntClass D = m.getOntClass(TestModelFactory.NS + "D");
        OntClass E = m.getOntClass(TestModelFactory.NS + "E");

        A.addSubClass(B);
        A.addSubClass(C);
        B.addSubClass(D);
        C.addSubClass(E);

        Assertions.assertEquals(A, OntModels.getLCA(E, D));
    }

    @Test
    public void testIndexLCA8() {
        OntModel m = TestModelFactory.createClassesABCDEFGThing(
                OntModelFactory.createModel(OntSpecification.OWL2_FULL_MEM_MICRO_RULES_INF)
        );
        OntClass A = m.getOntClass(TestModelFactory.NS + "A");
        OntClass B = m.getOntClass(TestModelFactory.NS + "B");
        OntClass C = m.getOntClass(TestModelFactory.NS + "C");
        OntClass D = m.getOntClass(TestModelFactory.NS + "D");
        OntClass E = m.getOntClass(TestModelFactory.NS + "E");

        A.addSubClass(B);
        A.addSubClass(C);
        B.addSubClass(D);
        D.addSubClass(E);

        Assertions.assertEquals(A, OntModels.getLCA(C, E));
    }

    @Test
    public void testIndexLCA9() {
        OntModel m = TestModelFactory.createClassesABCDEFGThing(
                OntModelFactory.createModel(OntSpecification.OWL2_FULL_MEM_MICRO_RULES_INF)
        );
        OntClass A = m.getOntClass(TestModelFactory.NS + "A");
        OntClass B = m.getOntClass(TestModelFactory.NS + "B");
        OntClass C = m.getOntClass(TestModelFactory.NS + "C");
        OntClass D = m.getOntClass(TestModelFactory.NS + "D");
        OntClass E = m.getOntClass(TestModelFactory.NS + "E");

        A.addSubClass(B);
        A.addSubClass(C);
        B.addSubClass(D);
        D.addSubClass(E);

        Assertions.assertEquals(A, OntModels.getLCA(B, C));
    }

    @Test
    public void testIndexLCA10() {
        OntModel m = TestModelFactory.createClassesABCDEFGThing(
                OntModelFactory.createModel(OntSpecification.OWL2_FULL_MEM_MICRO_RULES_INF)
        );
        OntClass A = m.getOntClass(TestModelFactory.NS + "A");
        OntClass B = m.getOntClass(TestModelFactory.NS + "B");
        OntClass C = m.getOntClass(TestModelFactory.NS + "C");
        OntClass D = m.getOntClass(TestModelFactory.NS + "D");
        OntClass E = m.getOntClass(TestModelFactory.NS + "E");
        OntClass F = m.getOntClass(TestModelFactory.NS + "F");

        A.addSubClass(B);
        A.addSubClass(C);
        A.addSubClass(D);
        C.addSubClass(E);
        D.addSubClass(F);

        Assertions.assertEquals(A, OntModels.getLCA(B, E));
    }

    @Test
    public void testIndexLCA11() {
        OntModel m = TestModelFactory.createClassesABCDEFGThing(
                OntModelFactory.createModel(OntSpecification.OWL2_FULL_MEM_MICRO_RULES_INF)
        );
        OntClass A = m.getOntClass(TestModelFactory.NS + "A");
        OntClass B = m.getOntClass(TestModelFactory.NS + "B");
        OntClass C = m.getOntClass(TestModelFactory.NS + "C");
        OntClass D = m.getOntClass(TestModelFactory.NS + "D");
        OntClass E = m.getOntClass(TestModelFactory.NS + "E");
        OntClass F = m.getOntClass(TestModelFactory.NS + "F");

        A.addSubClass(B);
        A.addSubClass(C);
        A.addSubClass(D);
        C.addSubClass(E);
        D.addSubClass(F);

        Assertions.assertEquals(A, OntModels.getLCA(B, F));
    }

    @Test
    public void testIndexLCA12() {
        OntModel m = TestModelFactory.createClassesABCDEFGThing(
                OntModelFactory.createModel(OntSpecification.OWL2_FULL_MEM_MICRO_RULES_INF)
        );
        OntClass A = m.getOntClass(TestModelFactory.NS + "A");
        OntClass B = m.getOntClass(TestModelFactory.NS + "B");
        OntClass C = m.getOntClass(TestModelFactory.NS + "C");
        OntClass D = m.getOntClass(TestModelFactory.NS + "D");
        OntClass E = m.getOntClass(TestModelFactory.NS + "E");
        OntClass F = m.getOntClass(TestModelFactory.NS + "F");

        A.addSubClass(B);
        A.addSubClass(C);
        A.addSubClass(D);
        D.addSubClass(E);
        D.addSubClass(F);

        Assertions.assertEquals(D, OntModels.getLCA(F, E));
    }

    @Test
    public void testIndexLCA13() {
        OntModel m = TestModelFactory.createClassesABCDEFGThing(
                OntModelFactory.createModel(OntSpecification.OWL2_FULL_MEM_MICRO_RULES_INF)
        );
        OntClass A = m.getOntClass(TestModelFactory.NS + "A");
        OntClass B = m.getOntClass(TestModelFactory.NS + "B");
        OntClass C = m.getOntClass(TestModelFactory.NS + "C");
        OntClass D = m.getOntClass(TestModelFactory.NS + "D");
        OntClass E = m.getOntClass(TestModelFactory.NS + "E");
        OntClass F = m.getOntClass(TestModelFactory.NS + "F");

        A.addSubClass(B);
        A.addSubClass(C);
        A.addSubClass(D);
        C.addSubClass(E);
        D.addSubClass(E);
        D.addSubClass(F);

        Assertions.assertEquals(D, OntModels.getLCA(F, E));
    }

    @Test
    public void testIndexLCA14() {
        OntModel m = TestModelFactory.createClassesABCDEFGThing(
                OntModelFactory.createModel(OntSpecification.OWL2_FULL_MEM_MICRO_RULES_INF)
        );
        OntClass A = m.getOntClass(TestModelFactory.NS + "A");
        OntClass B = m.getOntClass(TestModelFactory.NS + "B");
        OntClass C = m.getOntClass(TestModelFactory.NS + "C");
        OntClass E = m.getOntClass(TestModelFactory.NS + "E");

        A.addSubClass(B);
        A.addSubClass(C);

        Assertions.assertEquals(OWL2.Thing, OntModels.getLCA(B, E));
        Assertions.assertEquals(OWL2.Thing, OntModels.getLCA(C, E));
        Assertions.assertEquals(OWL2.Thing, OntModels.getLCA(A, E));
    }

}
