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
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDFS;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.jena.ontapi.TestModelFactory.NS;
import static org.apache.jena.ontapi.TestModelFactory.createClassesABC;
import static org.apache.jena.ontapi.TestModelFactory.createClassesABCA;
import static org.apache.jena.ontapi.TestModelFactory.createClassesABCD;
import static org.apache.jena.ontapi.TestModelFactory.createClassesABCDEF;
import static org.apache.jena.ontapi.TestModelFactory.createClassesABCDEFBCF;
import static org.apache.jena.ontapi.TestModelFactory.createClassesABCDEFGHKLM;
import static org.apache.jena.ontapi.TestModelFactory.createClassesBCA;

public class OntClassSubClassesTest {

    private static Set<String> subClasses(OntModel m, String name, boolean direct) {
        return m.getResource(NS + name).as(OntClass.class).subClasses(direct).map(Resource::getLocalName).collect(Collectors.toSet());
    }

    private static Set<String> subClasses(OntModel m, String name) {
        return m.getResource(NS + name).as(OntClass.class).subClasses().map(Resource::getLocalName).collect(Collectors.toSet());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_DL_MEM",
            "OWL2_DL_MEM_RDFS_INF",
            "OWL2_DL_MEM_TRANS_INF",
            "OWL2_MEM",
            "OWL2_MEM_RDFS_INF",
            "OWL2_MEM_TRANS_INF",
            "OWL2_EL_MEM",
            "OWL2_EL_MEM_RDFS_INF",
            "OWL2_EL_MEM_TRANS_INF",
            "OWL2_RL_MEM",
            "OWL2_RL_MEM_RDFS_INF",
            "OWL2_RL_MEM_TRANS_INF",
            "OWL2_QL_MEM",
            "OWL2_QL_MEM_RDFS_INF",
            "OWL2_QL_MEM_TRANS_INF",
            "OWL1_DL_MEM",
            "OWL1_DL_MEM_RDFS_INF",
            "OWL1_DL_MEM_TRANS_INF",
            "OWL1_MEM",
            "OWL1_MEM_RDFS_INF",
            "OWL1_MEM_TRANS_INF",
            "OWL1_LITE_MEM",
            "OWL1_LITE_MEM_RDFS_INF",
            "OWL1_LITE_MEM_TRANS_INF",
            "RDFS_MEM",
            "RDFS_MEM_TRANS_INF",
    })
    public void testGetSubClass1a(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst);
        OntClass a = m.createOntClass(NS + "A");
        Assertions.assertTrue(a.subClass().isEmpty());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_MEM_RULES_INF",
            "OWL2_DL_MEM_RULES_INF",
            "OWL2_MEM_MINI_RULES_INF",
            "OWL2_EL_MEM_RULES_INF",
            "OWL2_RL_MEM_RULES_INF",
            "OWL2_QL_MEM_RULES_INF",
            "OWL1_DL_MEM_RULES_INF",
            "OWL1_MEM_RULES_INF",
            "OWL1_MEM_MINI_RULES_INF",
            "OWL1_LITE_MEM_RULES_INF",
            "RDFS_MEM_RDFS_INF",
    })
    public void testGetSubClass1b(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst);
        OntClass a = m.createOntClass(NS + "A");
        Assertions.assertEquals(a, a.subClass().orElseThrow());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_MEM_MICRO_RULES_INF",
            "OWL1_MEM_MICRO_RULES_INF",
    })
    public void testGetSubClass1c(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst);
        OntClass a = m.createOntClass(NS + "A");
        Set<? extends Resource> subClasses =
                m.listStatements(null, RDFS.subClassOf, a).mapWith(it -> it.getSubject().as(OntClass.class)).toSet();
        Assertions.assertEquals(Set.of(OWL2.Nothing, a), subClasses);
        Assertions.assertTrue(a.subClass().isPresent());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_DL_MEM",
            "OWL2_DL_MEM_RDFS_INF",
            "OWL2_DL_MEM_RULES_INF",
            "OWL2_DL_MEM_TRANS_INF",
            "OWL2_MEM",
            "OWL2_MEM_RDFS_INF",
            "OWL2_MEM_RULES_INF",
            "OWL2_MEM_TRANS_INF",
            "OWL2_MEM_MINI_RULES_INF",
            "OWL2_EL_MEM",
            "OWL2_EL_MEM_RDFS_INF",
            "OWL2_EL_MEM_TRANS_INF",
            "OWL2_EL_MEM_RULES_INF",
            "OWL2_QL_MEM",
            "OWL2_QL_MEM_RDFS_INF",
            "OWL2_QL_MEM_TRANS_INF",
            "OWL2_QL_MEM_RULES_INF",
            "OWL2_RL_MEM",
            "OWL2_RL_MEM_RDFS_INF",
            "OWL2_RL_MEM_TRANS_INF",
            "OWL2_RL_MEM_RULES_INF",
            "OWL1_MEM",
            "OWL1_MEM_RULES_INF",
            "OWL1_MEM_RDFS_INF",
            "OWL1_MEM_TRANS_INF",
            "OWL1_MEM_MINI_RULES_INF",
            "OWL1_DL_MEM",
            "OWL1_DL_MEM_RDFS_INF",
            "OWL1_DL_MEM_RULES_INF",
            "OWL1_DL_MEM_TRANS_INF",
            "OWL1_LITE_MEM",
            "OWL1_LITE_MEM_RDFS_INF",
            "OWL1_LITE_MEM_RULES_INF",
            "OWL1_LITE_MEM_TRANS_INF",
            "RDFS_MEM",
            "RDFS_MEM_RDFS_INF",
            "RDFS_MEM_TRANS_INF",
    })
    public void testListSubClasses1a(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst);
        OntClass A = m.createOntClass(NS + "A");
        A.addSubClass(A);

        Assertions.assertTrue(A.subClasses(true).findFirst().isEmpty());
        Assertions.assertTrue(A.subClasses(false).findFirst().isEmpty());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_MEM_MICRO_RULES_INF",
            "OWL1_MEM_MICRO_RULES_INF",
    })
    public void testListSubClasses1b(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst);
        OntClass A = m.createOntClass(NS + "A");
        A.addSubClass(A);

        Assertions.assertEquals(List.of(OWL2.Nothing), A.subClasses(true).collect(Collectors.toList()));
        Assertions.assertEquals(List.of(OWL2.Nothing), A.subClasses(false).collect(Collectors.toList()));
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_MEM",
            "OWL2_EL_MEM",
            "OWL2_QL_MEM",
            "OWL2_RL_MEM",
            "OWL1_MEM",
            "OWL1_LITE_MEM",
            "RDFS_MEM",
    })
    public void testListSubClasses2(TestSpec spec) {
        //    A
        //  / |
        // B  C
        //     \
        //      D

        OntModel m = createClassesABCD(OntModelFactory.createModel(spec.inst));
        OntClass A = m.getResource(NS + "A").as(OntClass.class);
        OntClass B = m.getResource(NS + "B").as(OntClass.class);
        OntClass C = m.getResource(NS + "C").as(OntClass.class);
        OntClass D = m.getResource(NS + "D").as(OntClass.class);

        Assertions.assertEquals(Set.of(B, C), A.subClasses().collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(B, C), A.subClasses(true).collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(B, C), A.subClasses(false).collect(Collectors.toSet()));

        A.addSubClass(D);
        Assertions.assertEquals(Set.of(B, C, D), A.subClasses().collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(B, C), A.subClasses(true).collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(B, C, D), A.subClasses(false).collect(Collectors.toSet()));
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_DL_MEM_RDFS_INF",
            "OWL2_DL_MEM_TRANS_INF",
            "OWL2_DL_MEM_RULES_INF",
            "OWL2_MEM_RDFS_INF",
            "OWL2_MEM_TRANS_INF",
            "OWL2_MEM_RULES_INF",
            "OWL2_EL_MEM_RDFS_INF",
            "OWL2_EL_MEM_TRANS_INF",
            "OWL2_EL_MEM_RULES_INF",
            "OWL2_QL_MEM_RDFS_INF",
            "OWL2_QL_MEM_TRANS_INF",
            "OWL2_QL_MEM_RULES_INF",
            "OWL2_RL_MEM_RDFS_INF",
            "OWL2_RL_MEM_TRANS_INF",
            "OWL2_RL_MEM_RULES_INF",
            "OWL1_DL_MEM_RDFS_INF",
            "OWL1_DL_MEM_TRANS_INF",
            "OWL1_DL_MEM_RULES_INF",
            "OWL1_MEM_RDFS_INF",
            "OWL1_MEM_TRANS_INF",
            "OWL1_MEM_RULES_INF",
            "OWL1_LITE_MEM_RDFS_INF",
            "OWL1_LITE_MEM_RULES_INF",
            "OWL1_LITE_MEM_TRANS_INF",
            "RDFS_MEM_RDFS_INF",
            "RDFS_MEM_TRANS_INF",
    })
    public void testListSubClasses3a(TestSpec spec) {
        //      A
        //     / \
        //    B   C
        //   / \ / \
        //  D   E   F

        OntModel m = createClassesABCDEF(OntModelFactory.createModel(spec.inst));

        Set<String> directA = subClasses(m, "A", true);
        Set<String> indirectA = subClasses(m, "A", false);

        Set<String> directB = subClasses(m, "B", true);
        Set<String> indirectB = subClasses(m, "B", false);

        Set<String> directC = subClasses(m, "C", true);
        Set<String> indirectC = subClasses(m, "C");

        Set<String> directD = subClasses(m, "D", true);
        Set<String> indirectD = subClasses(m, "D", false);

        Set<String> directE = subClasses(m, "E", true);
        Set<String> indirectE = subClasses(m, "E");

        Set<String> directF = subClasses(m, "F", true);
        Set<String> indirectF = subClasses(m, "F", false);

        Assertions.assertEquals(Set.of("C", "B"), directA, "Wrong direct nodes for A");
        Assertions.assertEquals(Set.of("D", "E"), directB, "Wrong direct nodes for B");
        Assertions.assertEquals(Set.of("F", "E"), directC, "Wrong direct nodes for C");
        Assertions.assertEquals(Set.of(), directD, "Wrong direct nodes for D");
        Assertions.assertEquals(Set.of(), directE, "Wrong direct nodes for E");
        Assertions.assertEquals(Set.of(), directF, "Wrong direct nodes for F");

        Assertions.assertEquals(Set.of("C", "B", "D", "E", "F"), indirectA, "Wrong indirect nodes for A");
        Assertions.assertEquals(Set.of("E", "D"), indirectB, "Wrong indirect nodes for B");
        Assertions.assertEquals(Set.of("F", "E"), indirectC, "Wrong indirect nodes for C");
        Assertions.assertEquals(Set.of(), indirectD, "Wrong indirect nodes for D");
        Assertions.assertEquals(Set.of(), indirectE, "Wrong indirect nodes for E");
        Assertions.assertEquals(Set.of(), indirectF, "Wrong indirect nodes for F");
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_MEM",
            "OWL2_DL_MEM",
            "OWL2_EL_MEM",
            "OWL2_QL_MEM",
            "OWL2_RL_MEM",
            "OWL1_MEM",
            "OWL1_DL_MEM",
            "OWL1_LITE_MEM",
            "RDFS_MEM",
    })
    public void testListSubClasses3b(TestSpec spec) {
        //      A
        //     / \
        //    B   C
        //   / \ / \
        //  D   E   F

        OntModel m = createClassesABCDEF(OntModelFactory.createModel(spec.inst));

        Set<String> directA = subClasses(m, "A", true);
        Set<String> indirectA = subClasses(m, "A", false);

        Set<String> directB = subClasses(m, "B", true);
        Set<String> indirectB = subClasses(m, "B", false);

        Set<String> directC = subClasses(m, "C", true);
        Set<String> indirectC = subClasses(m, "C");

        Set<String> directD = subClasses(m, "D", true);
        Set<String> indirectD = subClasses(m, "D", false);

        Set<String> directE = subClasses(m, "E", true);
        Set<String> indirectE = subClasses(m, "E", false);

        Set<String> directF = subClasses(m, "F", true);
        Set<String> indirectF = subClasses(m, "F", false);

        Assertions.assertEquals(Set.of("C", "B"), directA, "wrong direct nodes for A");
        Assertions.assertEquals(Set.of("D", "E"), directB, "wrong direct nodes for B");
        Assertions.assertEquals(Set.of("F", "E"), directC, "wrong direct nodes for C");
        Assertions.assertEquals(Set.of(), directD, "wrong direct nodes for D");
        Assertions.assertEquals(Set.of(), directE, "wrong direct nodes for E");
        Assertions.assertEquals(Set.of(), directF, "wrong direct nodes for F");

        Assertions.assertEquals(Set.of("C", "B"), indirectA);
        Assertions.assertEquals(Set.of("E", "D"), indirectB);
        Assertions.assertEquals(Set.of("F", "E"), indirectC);
        Assertions.assertEquals(Set.of(), indirectD);
        Assertions.assertEquals(Set.of(), indirectE);
        Assertions.assertEquals(Set.of(), indirectF);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_MEM_MICRO_RULES_INF",
            "OWL1_MEM_MICRO_RULES_INF",
    })
    public void testListSubClasses3c(TestSpec spec) {
        //      A
        //     / \
        //    B   C
        //   / \ / \
        //  D   E   F

        OntModel m = createClassesABCDEF(OntModelFactory.createModel(spec.inst));

        Set<String> directA = subClasses(m, "A", true);
        Set<String> indirectA = subClasses(m, "A", false);

        Set<String> directB = subClasses(m, "B", true);
        Set<String> indirectB = subClasses(m, "B", false);

        Set<String> directC = subClasses(m, "C", true);
        Set<String> indirectC = subClasses(m, "C");

        Set<String> directD = subClasses(m, "D", true);
        Set<String> indirectD = subClasses(m, "D", false);

        Set<String> directE = subClasses(m, "E", true);
        Set<String> indirectE = subClasses(m, "E", false);

        Set<String> directF = subClasses(m, "F", true);
        Set<String> indirectF = subClasses(m, "F", false);

        Assertions.assertEquals(Set.of("C", "B"), directA);
        Assertions.assertEquals(Set.of("D", "E"), directB);
        Assertions.assertEquals(Set.of("F", "E"), directC);
        Assertions.assertEquals(Set.of("Nothing"), directD);
        Assertions.assertEquals(Set.of("Nothing"), directE);
        Assertions.assertEquals(Set.of("Nothing"), directF);

        Assertions.assertEquals(Set.of("C", "B", "D", "E", "F", "Nothing"), indirectA);
        Assertions.assertEquals(Set.of("E", "D", "Nothing"), indirectB);
        Assertions.assertEquals(Set.of("F", "E", "Nothing"), indirectC);
        Assertions.assertEquals(Set.of("Nothing"), indirectD);
        Assertions.assertEquals(Set.of("Nothing"), indirectE);
        Assertions.assertEquals(Set.of("Nothing"), indirectF);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_MEM",
            "OWL2_DL_MEM",
            "OWL2_EL_MEM",
            "OWL2_QL_MEM",
            "OWL2_RL_MEM",
            "OWL1_MEM",
            "OWL1_DL_MEM",
            "OWL1_LITE_MEM",
            "RDFS_MEM",
    })
    public void testListSubClasses4a(TestSpec spec) {
        //      A
        //   /  / \
        //  /  B   C
        //  | / \ / \
        //  D   E   F
        // / \
        // G  H = K
        //       / \
        //      L   M

        OntModel m = createClassesABCDEFGHKLM(OntModelFactory.createModel(spec.inst));

        Set<String> directA = subClasses(m, "A", true);
        Set<String> indirectA = subClasses(m, "A", false);

        Set<String> directB = subClasses(m, "B", true);
        Set<String> indirectB = subClasses(m, "B", false);

        Set<String> directC = subClasses(m, "C", true);
        Set<String> indirectC = subClasses(m, "C", false);

        Set<String> directD = subClasses(m, "D", true);
        Set<String> indirectD = subClasses(m, "D", false);

        Set<String> directE = subClasses(m, "E", true);
        Set<String> indirectE = subClasses(m, "E", false);

        Set<String> directF = subClasses(m, "F", true);
        Set<String> indirectF = subClasses(m, "F", false);

        Set<String> directG = subClasses(m, "G", true);
        Set<String> indirectG = subClasses(m, "G", false);

        Set<String> directH = subClasses(m, "H", true);
        Set<String> indirectH = subClasses(m, "H", true);

        Set<String> directK = subClasses(m, "K", true);
        Set<String> indirectK = subClasses(m, "K", false);

        Set<String> directL = subClasses(m, "L", true);
        Set<String> indirectL = subClasses(m, "L", false);

        Set<String> directM = subClasses(m, "M", true);
        Set<String> indirectM = subClasses(m, "M", false);

        Assertions.assertEquals(Set.of("C", "B"), directA, "wrong direct nodes for A");
        Assertions.assertEquals(Set.of("E", "D"), directB, "wrong direct nodes for B");
        Assertions.assertEquals(Set.of("F", "E"), directC, "wrong direct nodes for C");
        Assertions.assertEquals(Set.of("H", "G"), directD, "wrong direct nodes for D");
        Assertions.assertEquals(Set.of(), directE, "wrong direct nodes for E");
        Assertions.assertEquals(Set.of(), directF, "wrong direct nodes for F");
        Assertions.assertEquals(Set.of(), directG, "wrong direct nodes for G");
        Assertions.assertEquals(Set.of(), directH, "wrong direct nodes for H");
        Assertions.assertEquals(Set.of("M", "L"), directK, "wrong direct nodes for K");
        Assertions.assertEquals(Set.of(), directL, "wrong direct nodes for L");
        Assertions.assertEquals(Set.of(), directM, "wrong direct nodes for M");

        Assertions.assertEquals(Set.of("C", "B", "D"), indirectA);
        Assertions.assertEquals(Set.of("E", "D"), indirectB);
        Assertions.assertEquals(Set.of("F", "E"), indirectC);
        Assertions.assertEquals(Set.of("H", "G"), indirectD);
        Assertions.assertEquals(Set.of(), indirectE);
        Assertions.assertEquals(Set.of(), indirectF);
        Assertions.assertEquals(Set.of(), indirectG);
        Assertions.assertEquals(Set.of(), indirectH);
        Assertions.assertEquals(Set.of("M", "L", "H"), indirectK);
        Assertions.assertEquals(Set.of(), indirectL);
        Assertions.assertEquals(Set.of(), indirectM);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_DL_MEM_RDFS_INF",
            "OWL2_DL_MEM_RULES_INF",
            "OWL2_MEM_RDFS_INF",
            "OWL2_MEM_RULES_INF",
            "OWL2_MEM_MINI_RULES_INF",
            "OWL2_EL_MEM_RDFS_INF",
            "OWL2_EL_MEM_RULES_INF",
            "OWL2_QL_MEM_RDFS_INF",
            "OWL2_QL_MEM_RULES_INF",
            "OWL2_RL_MEM_RDFS_INF",
            "OWL2_RL_MEM_RULES_INF",
            "OWL1_DL_MEM_RDFS_INF",
            "OWL1_DL_MEM_RULES_INF",
            "OWL1_MEM_RDFS_INF",
            "OWL1_MEM_RULES_INF",
            "OWL1_MEM_MINI_RULES_INF",
            "OWL1_LITE_MEM_RDFS_INF",
            "OWL1_LITE_MEM_RULES_INF",
            "RDFS_MEM_RDFS_INF",
    })
    public void testListSubClasses4b(TestSpec spec) {
        //      A
        //   /  / \
        //  /  B   C
        //  | / \ / \
        //  D   E   F
        // / \
        // G  H = K
        //       / \
        //      L   M

        OntModel m = createClassesABCDEFGHKLM(OntModelFactory.createModel(spec.inst));

        Set<String> directA = subClasses(m, "A", true);
        Set<String> indirectA = subClasses(m, "A", false);

        Set<String> directB = subClasses(m, "B", true);
        Set<String> indirectB = subClasses(m, "B", false);

        Set<String> directC = subClasses(m, "C", true);
        Set<String> indirectC = subClasses(m, "C", false);

        Set<String> directD = subClasses(m, "D", true);
        Set<String> indirectD = subClasses(m, "D", false);

        Set<String> directE = subClasses(m, "E", true);
        Set<String> indirectE = subClasses(m, "E", false);

        Set<String> directF = subClasses(m, "F", true);
        Set<String> indirectF = subClasses(m, "F", false);

        Set<String> directG = subClasses(m, "G", true);
        Set<String> indirectG = subClasses(m, "G", false);

        Set<String> directH = subClasses(m, "H", true);
        Set<String> indirectH = subClasses(m, "H", true);

        Set<String> directK = subClasses(m, "K", true);
        Set<String> indirectK = subClasses(m, "K", false);

        Set<String> directL = subClasses(m, "L", true);
        Set<String> indirectL = subClasses(m, "L", false);

        Set<String> directM = subClasses(m, "M", true);
        Set<String> indirectM = subClasses(m, "M", false);

        Assertions.assertEquals(Set.of("C", "B"), directA, "wrong direct nodes for A");
        Assertions.assertEquals(Set.of("E", "D"), directB, "wrong direct nodes for B");
        Assertions.assertEquals(Set.of("F", "E"), directC, "wrong direct nodes for C");
        Assertions.assertEquals(Set.of("H", "G", "K"), directD, "wrong direct nodes for D");
        Assertions.assertEquals(Set.of(), directE, "wrong direct nodes for E");
        Assertions.assertEquals(Set.of(), directF, "wrong direct nodes for F");
        Assertions.assertEquals(Set.of(), directG, "wrong direct nodes for G");
        Assertions.assertEquals(Set.of("M", "L"), directH, "wrong direct nodes for H");
        Assertions.assertEquals(Set.of("M", "L"), directK, "wrong direct nodes for K");
        Assertions.assertEquals(Set.of(), directL, "wrong direct nodes for L");
        Assertions.assertEquals(Set.of(), directM, "wrong direct nodes for M");

        Assertions.assertEquals(Set.of("B", "C", "D", "E", "F", "G", "H", "K", "L", "M"), indirectA, "wrong indirect nodes for A");
        Assertions.assertEquals(Set.of("D", "E", "G", "H", "K", "L", "M"), indirectB, "wrong indirect nodes for B");
        Assertions.assertEquals(Set.of("F", "E"), indirectC, "wrong indirect nodes for C");
        Assertions.assertEquals(Set.of("G", "H", "K", "L", "M"), indirectD, "wrong indirect nodes for D");
        Assertions.assertEquals(Set.of(), indirectE, "wrong indirect nodes for E");
        Assertions.assertEquals(Set.of(), indirectF, "wrong indirect nodes for F");
        Assertions.assertEquals(Set.of(), indirectG, "wrong indirect nodes for G");
        Assertions.assertEquals(Set.of("L", "M"), indirectH, "wrong indirect nodes for H");
        Assertions.assertEquals(Set.of("H", "L", "M"), indirectK, "wrong indirect nodes for K");
        Assertions.assertEquals(Set.of(), indirectL, "wrong indirect nodes for L");
        Assertions.assertEquals(Set.of(), indirectM, "wrong indirect nodes for M");
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_MEM_RULES_INF",
            "OWL2_MEM_MINI_RULES_INF",
            "OWL2_DL_MEM_RULES_INF",
            "OWL2_EL_MEM_RULES_INF",
            "OWL2_QL_MEM_RULES_INF",
            "OWL2_RL_MEM_RULES_INF",
            "OWL1_MEM_RULES_INF",
            "OWL1_MEM_MINI_RULES_INF",
            "OWL1_DL_MEM_RULES_INF",
            "OWL1_LITE_MEM_RULES_INF",
    })
    public void testListSubClasses5a(TestSpec spec) {
        //     A
        //     |
        // D = B = C

        OntModel m = OntModelFactory.createModel(spec.inst);
        OntClass A = m.createOntClass(NS + "A");
        OntClass B = m.createOntClass(NS + "B");
        OntClass C = m.createOntClass(NS + "C");
        OntClass D = m.createOntClass(NS + "D");
        A.addSubClass(B);
        B.addEquivalentClass(C);
        D.addEquivalentClass(B);

        Set<String> directA = subClasses(m, "A", true);
        Set<String> directB = subClasses(m, "B", true);
        Set<String> directC = subClasses(m, "C", true);
        Set<String> directD = subClasses(m, "D", true);

        Set<String> indirectA = subClasses(m, "A");
        Set<String> indirectB = subClasses(m, "B", false);
        Set<String> indirectC = subClasses(m, "C", false);
        Set<String> indirectD = subClasses(m, "D", false);

        Assertions.assertEquals(Set.of("C", "B", "D"), directA);
        Assertions.assertEquals(Set.of(), directB);
        Assertions.assertEquals(Set.of(), directC);
        Assertions.assertEquals(Set.of(), directD);

        Assertions.assertEquals(Set.of("B", "C", "D"), indirectA);
        Assertions.assertEquals(Set.of("C", "D"), indirectB);
        Assertions.assertEquals(Set.of("B", "D"), indirectC);
        Assertions.assertEquals(Set.of("B", "C"), indirectD);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_DL_MEM",
            "OWL2_DL_MEM_RDFS_INF",
            "OWL2_DL_MEM_TRANS_INF",
            "OWL2_MEM",
            "OWL2_MEM_RDFS_INF",
            "OWL2_MEM_TRANS_INF",
            "OWL2_EL_MEM",
            "OWL2_EL_MEM_RDFS_INF",
            "OWL2_EL_MEM_TRANS_INF",
            "OWL2_QL_MEM",
            "OWL2_QL_MEM_RDFS_INF",
            "OWL2_QL_MEM_TRANS_INF",
            "OWL2_RL_MEM",
            "OWL2_RL_MEM_RDFS_INF",
            "OWL2_RL_MEM_TRANS_INF",
            "OWL1_DL_MEM",
            "OWL1_DL_MEM_RDFS_INF",
            "OWL1_DL_MEM_TRANS_INF",
            "OWL1_MEM",
            "OWL1_MEM_RDFS_INF",
            "OWL1_MEM_TRANS_INF",
            "OWL1_LITE_MEM",
            "OWL1_LITE_MEM_RDFS_INF",
            "OWL1_LITE_MEM_TRANS_INF",
    })
    public void testListSubClasses5b(TestSpec spec) {
        //     A
        //     |
        // D = B = C

        OntModel m = OntModelFactory.createModel(spec.inst);
        OntClass A = m.createOntClass(NS + "A");
        OntClass B = m.createOntClass(NS + "B");
        OntClass C = m.createOntClass(NS + "C");
        OntClass D = m.createOntClass(NS + "D");
        A.addSubClass(B);
        B.addEquivalentClass(C);
        D.addEquivalentClass(B);

        Set<String> directA = subClasses(m, "A", true);
        Set<String> directB = subClasses(m, "B", true);
        Set<String> directC = subClasses(m, "C", true);
        Set<String> directD = subClasses(m, "D", true);

        Set<String> indirectA = subClasses(m, "A");
        Set<String> indirectB = subClasses(m, "B", false);
        Set<String> indirectC = subClasses(m, "C", false);
        Set<String> indirectD = subClasses(m, "D", false);

        Assertions.assertEquals(Set.of("B"), directA, "wrong direct nodes for A");
        Assertions.assertEquals(Set.of(), directB, "wrong direct nodes for B");
        Assertions.assertEquals(Set.of(), directC, "wrong direct nodes for C");
        Assertions.assertEquals(Set.of(), directD, "wrong direct nodes for D");

        Assertions.assertEquals(Set.of("B"), indirectA);
        Assertions.assertEquals(Set.of(), indirectB);
        Assertions.assertEquals(Set.of(), indirectC);
        Assertions.assertEquals(Set.of(), indirectD);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_DL_MEM",
            "OWL2_DL_MEM_RDFS_INF",
            "OWL2_DL_MEM_RULES_INF",
            "OWL2_MEM",
            "OWL2_MEM_RDFS_INF",
            "OWL2_MEM_RULES_INF",
            "OWL2_EL_MEM",
            "OWL2_EL_MEM_RDFS_INF",
            "OWL2_EL_MEM_RULES_INF",
            "OWL2_QL_MEM",
            "OWL2_QL_MEM_RDFS_INF",
            "OWL2_QL_MEM_RULES_INF",
            "OWL2_RL_MEM",
            "OWL2_RL_MEM_RDFS_INF",
            "OWL2_RL_MEM_RULES_INF",
            "OWL1_DL_MEM",
            "OWL1_DL_MEM_RDFS_INF",
            "OWL1_DL_MEM_RULES_INF",
            "OWL1_MEM",
            "OWL1_MEM_RDFS_INF",
            "OWL1_MEM_RULES_INF",
            "OWL1_LITE_MEM",
            "OWL1_LITE_MEM_RDFS_INF",
            "OWL1_LITE_MEM_RULES_INF",
            "RDFS_MEM",
            "RDFS_MEM_RDFS_INF",
    })
    public void testListSubClasses6a(TestSpec spec) {
        // B = C
        //  \ |
        //    A

        OntModel m = createClassesBCA(OntModelFactory.createModel(spec.inst));

        Set<String> directA = subClasses(m, "A", true);
        Set<String> directB = subClasses(m, "B", true);
        Set<String> directC = subClasses(m, "C", true);

        Set<String> indirectA = subClasses(m, "A");
        Set<String> indirectB = subClasses(m, "B", false);
        Set<String> indirectC = subClasses(m, "C", false);

        Assertions.assertEquals(Set.of(), directA, "wrong direct nodes for A");
        Assertions.assertEquals(Set.of("A"), directB, "wrong direct nodes for B");
        Assertions.assertEquals(Set.of("A"), directC, "wrong direct nodes for C");

        Assertions.assertEquals(Set.of(), indirectA, "wrong indirect nodes for A");
        Assertions.assertEquals(Set.of("A", "C"), indirectB, "wrong indirect nodes for B");
        Assertions.assertEquals(Set.of("A", "B"), indirectC, "wrong indirect nodes for C");
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_MEM",
            "OWL2_DL_MEM",
            "OWL2_EL_MEM",
            "OWL2_QL_MEM",
            "OWL2_RL_MEM",
            "OWL1_MEM",
            "OWL1_DL_MEM",
            "OWL1_LITE_MEM",
            "RDFS_MEM",
    })
    public void testListSubClasses7a(TestSpec spec) {
        //    A
        //  / .
        // B  .
        // |  .
        // C  .
        //  \ .
        //    A

        OntModel m = createClassesABCA(OntModelFactory.createModel(spec.inst));

        Set<String> directA = subClasses(m, "A", true);
        Set<String> directB = subClasses(m, "B", true);
        Set<String> directC = subClasses(m, "C", true);

        Set<String> indirectA = subClasses(m, "A", false);
        Set<String> indirectB = subClasses(m, "B", false);
        Set<String> indirectC = subClasses(m, "C", false);

        Assertions.assertEquals(Set.of("B"), directA, "wrong direct nodes for A");
        Assertions.assertEquals(Set.of("C"), directB, "wrong direct nodes for B");
        Assertions.assertEquals(Set.of("A"), directC, "wrong direct nodes for C");

        Assertions.assertEquals(Set.of("B"), indirectA);
        Assertions.assertEquals(Set.of("C"), indirectB);
        Assertions.assertEquals(Set.of("A"), indirectC);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_DL_MEM_RDFS_INF",
            "OWL2_DL_MEM_RULES_INF",
            "OWL2_MEM_RDFS_INF",
            "OWL2_MEM_RULES_INF",
            "OWL2_EL_MEM_RDFS_INF",
            "OWL2_EL_MEM_RULES_INF",
            "OWL2_QL_MEM_RDFS_INF",
            "OWL2_QL_MEM_RULES_INF",
            "OWL2_RL_MEM_RDFS_INF",
            "OWL2_RL_MEM_RULES_INF",
            "OWL1_DL_MEM_RDFS_INF",
            "OWL1_DL_MEM_RULES_INF",
            "OWL1_MEM_RDFS_INF",
            "OWL1_MEM_RULES_INF",
            "OWL1_LITE_MEM_RDFS_INF",
            "OWL1_LITE_MEM_RULES_INF",
            "RDFS_MEM_RDFS_INF",
    })
    public void testListSubClasses7b(TestSpec spec) {
        //    A
        //  / .
        // B  .
        // |  .
        // C  .
        //  \ .
        //    A

        OntModel m = createClassesABCA(OntModelFactory.createModel(spec.inst));

        Set<String> directA = subClasses(m, "A", true);
        Set<String> directB = subClasses(m, "B", true);
        Set<String> directC = subClasses(m, "C", true);

        Set<String> indirectA = subClasses(m, "A", false);
        Set<String> indirectB = subClasses(m, "B", false);
        Set<String> indirectC = subClasses(m, "C", false);

        Assertions.assertEquals(Set.of(), directA, "wrong direct nodes for A");
        Assertions.assertEquals(Set.of(), directB, "wrong direct nodes for B");
        Assertions.assertEquals(Set.of(), directC, "wrong direct nodes for C");

        Assertions.assertEquals(Set.of("B", "C"), indirectA);
        Assertions.assertEquals(Set.of("A", "C"), indirectB);
        Assertions.assertEquals(Set.of("A", "B"), indirectC);
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
            "RDFS_MEM_TRANS_INF",
    })
    public void testListSubClasses7c(TestSpec spec) {
        //    A
        //  / .
        // B  .
        // |  .
        // C  .
        //  \ .
        //    A

        OntModel m = createClassesABCA(OntModelFactory.createModel(spec.inst));

        Set<String> directA = subClasses(m, "A", true);
        Set<String> directB = subClasses(m, "B", true);
        Set<String> directC = subClasses(m, "C", true);

        Set<String> indirectA = subClasses(m, "A", false);
        Set<String> indirectB = subClasses(m, "B", false);
        Set<String> indirectC = subClasses(m, "C", false);

        Assertions.assertEquals(Set.of("B", "C"), directA);
        Assertions.assertEquals(Set.of("A", "C"), directB);
        Assertions.assertEquals(Set.of("A", "B"), directC);

        Assertions.assertEquals(Set.of("B", "C"), indirectA);
        Assertions.assertEquals(Set.of("A", "C"), indirectB);
        Assertions.assertEquals(Set.of("A", "B"), indirectC);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_MEM_MICRO_RULES_INF",
            "OWL1_MEM_MICRO_RULES_INF",
    })
    public void testListSubClasses7d(TestSpec spec) {
        //    A
        //  / .
        // B  .
        // |  .
        // C  .
        //  \ .
        //    A

        OntModel m = createClassesABCA(OntModelFactory.createModel(spec.inst));

        Set<String> directA = subClasses(m, "A", true);
        Set<String> directB = subClasses(m, "B", true);
        Set<String> directC = subClasses(m, "C", true);

        Set<String> indirectA = subClasses(m, "A", false);
        Set<String> indirectB = subClasses(m, "B", false);
        Set<String> indirectC = subClasses(m, "C", false);

        Assertions.assertEquals(Set.of("Nothing"), directA);
        Assertions.assertEquals(Set.of("Nothing"), directB);
        Assertions.assertEquals(Set.of("Nothing"), directC);

        Assertions.assertEquals(Set.of("B", "C", "Nothing"), indirectA);
        Assertions.assertEquals(Set.of("A", "C", "Nothing"), indirectB);
        Assertions.assertEquals(Set.of("A", "B", "Nothing"), indirectC);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_DL_MEM",
            "OWL2_DL_MEM_RDFS_INF",
            "OWL2_DL_MEM_RULES_INF",
            "OWL2_MEM",
            "OWL2_MEM_RDFS_INF",
            "OWL2_MEM_RULES_INF",
            "OWL2_EL_MEM",
            "OWL2_EL_MEM_RDFS_INF",
            "OWL2_EL_MEM_RULES_INF",
            "OWL2_QL_MEM",
            "OWL2_QL_MEM_RDFS_INF",
            "OWL2_QL_MEM_RULES_INF",
            "OWL2_RL_MEM",
            "OWL2_RL_MEM_RDFS_INF",
            "OWL2_RL_MEM_RULES_INF",
            "OWL1_DL_MEM",
            "OWL1_DL_MEM_RDFS_INF",
            "OWL1_DL_MEM_RULES_INF",
            "OWL1_MEM",
            "OWL1_MEM_RDFS_INF",
            "OWL1_MEM_RULES_INF",
            "OWL1_LITE_MEM",
            "OWL1_LITE_MEM_RDFS_INF",
            "OWL1_LITE_MEM_RULES_INF",
            "RDFS_MEM",
            "RDFS_MEM_RDFS_INF",
    })
    public void testListSubClasses8a(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst);
        OntClass A = m.createOntClass(NS + "A");
        OntClass B = m.createOntClass(NS + "B");
        A.addSubClass(B);
        B.addSubClass(A);

        Set<String> directA = subClasses(m, "A", true);
        Set<String> indirectA = subClasses(m, "A", false);

        Set<String> directB = subClasses(m, "B", true);
        Set<String> indirectB = subClasses(m, "B", false);

        Assertions.assertEquals(Set.of(), directA, "wrong direct nodes for A");
        Assertions.assertEquals(Set.of(), directB, "wrong direct nodes for B");

        Assertions.assertEquals(Set.of("B"), indirectA);
        Assertions.assertEquals(Set.of("A"), indirectB);
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
            "RDFS_MEM_TRANS_INF",
    })
    public void testListSubClasses8b(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst);
        OntClass A = m.createOntClass(NS + "A");
        OntClass B = m.createOntClass(NS + "B");
        A.addSubClass(B);
        B.addSubClass(A);

        Set<String> directA = subClasses(m, "A", true);
        Set<String> indirectA = subClasses(m, "A", false);

        Set<String> directB = subClasses(m, "B", true);
        Set<String> indirectB = subClasses(m, "B", false);

        Assertions.assertEquals(Set.of("B"), directA);
        Assertions.assertEquals(Set.of("A"), directB);

        Assertions.assertEquals(Set.of("B"), indirectA);
        Assertions.assertEquals(Set.of("A"), indirectB);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_MEM_MICRO_RULES_INF",
            "OWL1_MEM_MICRO_RULES_INF",
    })
    public void testListSubClasses8c(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst);
        OntClass A = m.createOntClass(NS + "A");
        OntClass B = m.createOntClass(NS + "B");
        A.addSubClass(B);
        B.addSubClass(A);

        Set<String> directA = subClasses(m, "A", true);
        Set<String> indirectA = subClasses(m, "A", false);

        Set<String> directB = subClasses(m, "B", true);
        Set<String> indirectB = subClasses(m, "B", false);

        Assertions.assertEquals(Set.of("Nothing"), directA);
        Assertions.assertEquals(Set.of("Nothing"), directB);

        Assertions.assertEquals(Set.of("B", "Nothing"), indirectA);
        Assertions.assertEquals(Set.of("A", "Nothing"), indirectB);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_DL_MEM_RDFS_INF",
            "OWL2_DL_MEM_RULES_INF",
            "OWL2_MEM_RDFS_INF",
            "OWL2_MEM_RULES_INF",
            "OWL2_EL_MEM_RDFS_INF",
            "OWL2_EL_MEM_RULES_INF",
            "OWL2_QL_MEM_RDFS_INF",
            "OWL2_QL_MEM_RULES_INF",
            "OWL2_RL_MEM_RDFS_INF",
            "OWL2_RL_MEM_RULES_INF",
            "OWL1_DL_MEM_RDFS_INF",
            "OWL1_DL_MEM_RULES_INF",
            "OWL1_MEM_RDFS_INF",
            "OWL1_MEM_RULES_INF",
            "OWL1_LITE_MEM_RDFS_INF",
            "OWL1_LITE_MEM_RULES_INF",
            "RDFS_MEM_RDFS_INF",
    })
    public void testListSubClasses9a(TestSpec spec) {
        //  A   B
        //  .\ /.
        //  . C .
        //  . | .
        //  . D .
        //  ./  .
        //  A   .   E
        //   \  .  |
        //    \ . /
        //      B
        OntModel m = TestModelFactory.createClassesABCDAEB(OntModelFactory.createModel(spec.inst));

        Set<String> directA = subClasses(m, "A", true);
        Set<String> indirectA = subClasses(m, "A", false);

        Set<String> directB = subClasses(m, "B", true);
        Set<String> indirectB = subClasses(m, "B", false);

        Set<String> directC = subClasses(m, "C", true);
        Set<String> indirectC = subClasses(m, "C", false);

        Set<String> directD = subClasses(m, "D", true);
        Set<String> indirectD = subClasses(m, "D", false);

        Set<String> directE = subClasses(m, "E", true);
        Set<String> indirectE = subClasses(m, "E", false);

        Assertions.assertEquals(Set.of(), directA, "wrong direct nodes for A");
        Assertions.assertEquals(Set.of(), directB, "wrong direct nodes for B");
        Assertions.assertEquals(Set.of(), directC, "wrong direct nodes for C");
        Assertions.assertEquals(Set.of(), directD, "wrong direct nodes for D");
        Assertions.assertEquals(Set.of("A", "B", "C", "D"), directE, "wrong direct nodes for E");

        Assertions.assertEquals(Set.of("B", "C", "D"), indirectA);
        Assertions.assertEquals(Set.of("A", "C", "D"), indirectB);
        Assertions.assertEquals(Set.of("A", "B", "D"), indirectC);
        Assertions.assertEquals(Set.of("A", "B", "C"), indirectD);
        Assertions.assertEquals(Set.of("A", "B", "C", "D"), indirectE);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM",
            "OWL2_MEM",
            "OWL2_EL_MEM",
            "OWL2_QL_MEM",
            "OWL2_RL_MEM",
            "OWL1_DL_MEM",
            "OWL1_MEM",
            "OWL1_LITE_MEM",
            "RDFS_MEM",
    })
    public void testListSubClasses9b(TestSpec spec) {
        //  A   B
        //  .\ /.
        //  . C .
        //  . | .
        //  . D .
        //  ./  .
        //  A   .   E
        //   \  .  |
        //    \ . /
        //      B
        OntModel m = TestModelFactory.createClassesABCDAEB(OntModelFactory.createModel(spec.inst));

        Set<String> directA = subClasses(m, "A", true);
        Set<String> indirectA = subClasses(m, "A", false);

        Set<String> directB = subClasses(m, "B", true);
        Set<String> indirectB = subClasses(m, "B", false);

        Set<String> directC = subClasses(m, "C", true);
        Set<String> indirectC = subClasses(m, "C", false);

        Set<String> directD = subClasses(m, "D", true);
        Set<String> indirectD = subClasses(m, "D", false);

        Set<String> directE = subClasses(m, "E", true);
        Set<String> indirectE = subClasses(m, "E", false);

        Assertions.assertEquals(Set.of("B"), directA, "wrong direct nodes for A");
        Assertions.assertEquals(Set.of("C"), directB, "wrong direct nodes for B");
        Assertions.assertEquals(Set.of("D"), directC, "wrong direct nodes for C");
        Assertions.assertEquals(Set.of("A"), directD, "wrong direct nodes for D");
        Assertions.assertEquals(Set.of("B"), directE, "wrong direct nodes for E");

        Assertions.assertEquals(Set.of("B", "C"), indirectA, "wrong indirect nodes for A");
        Assertions.assertEquals(Set.of("C"), indirectB, "wrong indirect nodes for B");
        Assertions.assertEquals(Set.of("D"), indirectC, "wrong indirect nodes for C");
        Assertions.assertEquals(Set.of("A"), indirectD, "wrong indirect nodes for D");
        Assertions.assertEquals(Set.of("B"), indirectE, "wrong indirect nodes for E");
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
            "RDFS_MEM_TRANS_INF",
    })
    public void testListSubClasses9c(TestSpec spec) {
        //  A   B
        //  .\ /.
        //  . C .
        //  . | .
        //  . D .
        //  ./  .
        //  A   .   E
        //   \  .  |
        //    \ . /
        //      B
        OntModel m = TestModelFactory.createClassesABCDAEB(OntModelFactory.createModel(spec.inst));

        Set<String> directA = subClasses(m, "A", true);
        Set<String> indirectA = subClasses(m, "A", false);

        Set<String> directB = subClasses(m, "B", true);
        Set<String> indirectB = subClasses(m, "B", false);

        Set<String> directC = subClasses(m, "C", true);
        Set<String> indirectC = subClasses(m, "C", false);

        Set<String> directD = subClasses(m, "D", true);
        Set<String> indirectD = subClasses(m, "D", false);

        Set<String> directE = subClasses(m, "E", true);
        Set<String> indirectE = subClasses(m, "E", false);

        Assertions.assertEquals(Set.of("B", "C", "D"), directA);
        Assertions.assertEquals(Set.of("A", "C", "D"), directB);
        Assertions.assertEquals(Set.of("A", "B", "D"), directC);
        Assertions.assertEquals(Set.of("A", "B", "C"), directD);
        Assertions.assertEquals(Set.of("A", "B", "C", "D"), directE);

        Assertions.assertEquals(Set.of("B", "C", "D"), indirectA);
        Assertions.assertEquals(Set.of("A", "C", "D"), indirectB);
        Assertions.assertEquals(Set.of("A", "B", "D"), indirectC);
        Assertions.assertEquals(Set.of("A", "B", "C"), indirectD);
        Assertions.assertEquals(Set.of("A", "B", "C", "D"), indirectE);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_MEM_MICRO_RULES_INF",
            "OWL1_MEM_MICRO_RULES_INF",
    })
    public void testListSubClasses9d(TestSpec spec) {
        //  A   B
        //  .\ /.
        //  . C .
        //  . | .
        //  . D .
        //  ./  .
        //  A   .   E
        //   \  .  |
        //    \ . /
        //      B
        OntModel m = TestModelFactory.createClassesABCDAEB(OntModelFactory.createModel(spec.inst));

        Set<String> directA = subClasses(m, "A", true);
        Set<String> indirectA = subClasses(m, "A", false);

        Set<String> directB = subClasses(m, "B", true);
        Set<String> indirectB = subClasses(m, "B", false);

        Set<String> directC = subClasses(m, "C", true);
        Set<String> indirectC = subClasses(m, "C", false);

        Set<String> directD = subClasses(m, "D", true);
        Set<String> indirectD = subClasses(m, "D", false);

        Set<String> directE = subClasses(m, "E", true);
        Set<String> indirectE = subClasses(m, "E", false);

        Assertions.assertEquals(Set.of("Nothing"), directA);
        Assertions.assertEquals(Set.of("Nothing"), directB);
        Assertions.assertEquals(Set.of("Nothing"), directC);
        Assertions.assertEquals(Set.of("Nothing"), directD);
        Assertions.assertEquals(Set.of("A", "B", "C", "D"), directE);

        Assertions.assertEquals(Set.of("B", "C", "D", "Nothing"), indirectA);
        Assertions.assertEquals(Set.of("A", "C", "D", "Nothing"), indirectB);
        Assertions.assertEquals(Set.of("A", "B", "D", "Nothing"), indirectC);
        Assertions.assertEquals(Set.of("A", "B", "C", "Nothing"), indirectD);
        Assertions.assertEquals(Set.of("A", "B", "C", "D", "Nothing"), indirectE);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM",
            "OWL2_MEM",
            "OWL2_EL_MEM",
            "OWL2_QL_MEM",
            "OWL2_RL_MEM",
            "OWL1_DL_MEM",
            "OWL1_MEM",
            "OWL1_LITE_MEM",
            "RDFS_MEM",
    })
    public void testListSubClasses10a(TestSpec spec) {
        //      A       B
        //    /   \   / |
        //  /       C   |
        // |      / .   |
        // |    D   .   |
        // |  / |   .   |
        // E    |   .   |
        //   \  |   .   |
        //     F ...... F
        //       \  .
        //        \ .
        //          C
        OntModel m = createClassesABCDEFBCF(OntModelFactory.createModel(spec.inst));

        Set<String> directA = subClasses(m, "A", true);
        Set<String> indirectA = subClasses(m, "A", false);

        Set<String> directB = subClasses(m, "B", true);
        Set<String> indirectB = subClasses(m, "B", false);

        Set<String> directC = subClasses(m, "C", true);
        Set<String> indirectC = subClasses(m, "C", false);

        Set<String> directD = subClasses(m, "D", true);
        Set<String> indirectD = subClasses(m, "D", false);

        Set<String> directE = subClasses(m, "E", true);
        Set<String> indirectE = subClasses(m, "E", false);

        Set<String> directF = subClasses(m, "F", true);
        Set<String> indirectF = subClasses(m, "F", false);

        Assertions.assertEquals(Set.of("C", "E"), directA, "wrong direct nodes for A");
        Assertions.assertEquals(Set.of("F"), directB, "wrong direct nodes for B");
        Assertions.assertEquals(Set.of("D"), directC, "wrong direct nodes for C");
        Assertions.assertEquals(Set.of("E"), directD, "wrong direct nodes for D");
        Assertions.assertEquals(Set.of("F"), directE, "wrong direct nodes for E");
        Assertions.assertEquals(Set.of("C"), directF, "wrong direct nodes for F");

        Assertions.assertEquals(Set.of("C", "E"), indirectA, "wrong indirect nodes for A");
        Assertions.assertEquals(Set.of("C", "F"), indirectB, "wrong indirect nodes for B");
        Assertions.assertEquals(Set.of("D"), indirectC, "wrong indirect nodes for C");
        Assertions.assertEquals(Set.of("E", "F"), indirectD, "wrong indirect nodes for D");
        Assertions.assertEquals(Set.of("F"), indirectE, "wrong indirect nodes for E");
        Assertions.assertEquals(Set.of("C"), indirectF, "wrong indirect nodes for F");
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_INF",
            "OWL2_DL_MEM_RULES_INF",
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM_RDFS_INF",
            "OWL2_MEM_RULES_INF",
            "OWL2_EL_MEM_RDFS_INF",
            "OWL2_EL_MEM_RULES_INF",
            "OWL2_QL_MEM_RDFS_INF",
            "OWL2_QL_MEM_RULES_INF",
            "OWL2_RL_MEM_RDFS_INF",
            "OWL2_RL_MEM_RULES_INF",
            "OWL1_DL_MEM_RDFS_INF",
            "OWL1_DL_MEM_RULES_INF",
            "OWL1_MEM_RDFS_INF",
            "OWL1_MEM_RULES_INF",
            "OWL1_LITE_MEM_RDFS_INF",
            "OWL1_LITE_MEM_RULES_INF",
            "RDFS_MEM_RDFS_INF",
    })
    public void testListSubClasses10b(TestSpec spec) {
        //      A       B
        //    /   \   / |
        //  /       C   |
        // |      / .   |
        // |    D   .   |
        // |  / |   .   |
        // E    |   .   |
        //   \  |   .   |
        //     F ...... F
        //       \  .
        //        \ .
        //          C
        OntModel m = createClassesABCDEFBCF(OntModelFactory.createModel(spec.inst));

        Set<String> directA = subClasses(m, "A", true);
        Set<String> indirectA = subClasses(m, "A", false);

        Set<String> directB = subClasses(m, "B", true);
        Set<String> indirectB = subClasses(m, "B", false);

        Set<String> directC = subClasses(m, "C", true);
        Set<String> indirectC = subClasses(m, "C", false);

        Set<String> directD = subClasses(m, "D", true);
        Set<String> indirectD = subClasses(m, "D", false);

        Set<String> directE = subClasses(m, "E", true);
        Set<String> indirectE = subClasses(m, "E", false);

        Set<String> directF = subClasses(m, "F", true);
        Set<String> indirectF = subClasses(m, "F", false);

        Assertions.assertEquals(Set.of("C", "D", "E", "F"), directA, "wrong direct nodes for A");
        Assertions.assertEquals(Set.of("C", "D", "E", "F"), directB, "wrong direct nodes for B");
        Assertions.assertEquals(Set.of(), directC, "wrong direct nodes for C");
        Assertions.assertEquals(Set.of(), directD, "wrong direct nodes for D");
        Assertions.assertEquals(Set.of(), directE, "wrong direct nodes for E");
        Assertions.assertEquals(Set.of(), directF, "wrong direct nodes for F");

        Assertions.assertEquals(Set.of("C", "D", "E", "F"), indirectA, "wrong indirect nodes for A");
        Assertions.assertEquals(Set.of("C", "D", "E", "F"), indirectB, "wrong indirect nodes for B");
        Assertions.assertEquals(Set.of("D", "E", "F"), indirectC, "wrong indirect nodes for C");
        Assertions.assertEquals(Set.of("C", "E", "F"), indirectD, "wrong indirect nodes for D");
        Assertions.assertEquals(Set.of("C", "D", "F"), indirectE, "wrong indirect nodes for E");
        Assertions.assertEquals(Set.of("C", "D", "E"), indirectF, "wrong indirect nodes for F");
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_TRANS_INF",
            "OWL2_MEM_TRANS_INF",
            "OWL2_EL_MEM_TRANS_INF",
            "OWL2_QL_MEM_TRANS_INF",
            "OWL1_DL_MEM_TRANS_INF",
            "OWL2_RL_MEM_TRANS_INF",
            "OWL1_MEM_TRANS_INF",
            "OWL1_LITE_MEM_TRANS_INF",
            "RDFS_MEM_TRANS_INF",
    })
    public void testListSubClasses10c(TestSpec spec) {
        //      A       B
        //    /   \   / |
        //  /       C   |
        // |      / .   |
        // |    D   .   |
        // |  / |   .   |
        // E    |   .   |
        //   \  |   .   |
        //     F ...... F
        //       \  .
        //        \ .
        //          C
        OntModel m = createClassesABCDEFBCF(OntModelFactory.createModel(spec.inst));

        Set<String> directA = subClasses(m, "A", true);
        Set<String> indirectA = subClasses(m, "A", false);

        Set<String> directB = subClasses(m, "B", true);
        Set<String> indirectB = subClasses(m, "B", false);

        Set<String> directC = subClasses(m, "C", true);
        Set<String> indirectC = subClasses(m, "C", false);

        Set<String> directD = subClasses(m, "D", true);
        Set<String> indirectD = subClasses(m, "D", false);

        Set<String> directE = subClasses(m, "E", true);
        Set<String> indirectE = subClasses(m, "E", false);

        Set<String> directF = subClasses(m, "F", true);
        Set<String> indirectF = subClasses(m, "F", false);

        Assertions.assertEquals(Set.of("C", "D", "E", "F"), directA, "wrong direct nodes for A");
        Assertions.assertEquals(Set.of("C", "D", "E", "F"), directB, "wrong direct nodes for B");
        Assertions.assertEquals(Set.of("D", "E", "F"), directC, "wrong direct nodes for C");
        Assertions.assertEquals(Set.of("C", "E", "F"), directD, "wrong direct nodes for D");
        Assertions.assertEquals(Set.of("C", "D", "F"), directE, "wrong direct nodes for E");
        Assertions.assertEquals(Set.of("C", "D", "E"), directF, "wrong direct nodes for F");

        Assertions.assertEquals(Set.of("C", "D", "E", "F"), indirectA, "wrong indirect nodes for A");
        Assertions.assertEquals(Set.of("C", "D", "E", "F"), indirectB, "wrong indirect nodes for B");
        Assertions.assertEquals(Set.of("D", "E", "F"), indirectC, "wrong indirect nodes for C");
        Assertions.assertEquals(Set.of("C", "E", "F"), indirectD, "wrong indirect nodes for D");
        Assertions.assertEquals(Set.of("C", "D", "F"), indirectE, "wrong indirect nodes for E");
        Assertions.assertEquals(Set.of("C", "D", "E"), indirectF, "wrong indirect nodes for F");
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_MEM_MICRO_RULES_INF",
            "OWL1_MEM_MICRO_RULES_INF",
    })
    public void testListSubClasses10d(TestSpec spec) {
        //      A       B
        //    /   \   / |
        //  /       C   |
        // |      / .   |
        // |    D   .   |
        // |  / |   .   |
        // E    |   .   |
        //   \  |   .   |
        //     F ...... F
        //       \  .
        //        \ .
        //          C
        OntModel m = createClassesABCDEFBCF(OntModelFactory.createModel(spec.inst));

        Set<String> directA = subClasses(m, "A", true);
        Set<String> indirectA = subClasses(m, "A", false);

        Set<String> directB = subClasses(m, "B", true);
        Set<String> indirectB = subClasses(m, "B", false);

        Set<String> directC = subClasses(m, "C", true);
        Set<String> indirectC = subClasses(m, "C", false);

        Set<String> directD = subClasses(m, "D", true);
        Set<String> indirectD = subClasses(m, "D", false);

        Set<String> directE = subClasses(m, "E", true);
        Set<String> indirectE = subClasses(m, "E", false);

        Set<String> directF = subClasses(m, "F", true);
        Set<String> indirectF = subClasses(m, "F", false);

        Assertions.assertEquals(Set.of("C", "D", "E", "F"), directA, "wrong direct nodes for A");
        Assertions.assertEquals(Set.of("C", "D", "E", "F"), directB, "wrong direct nodes for B");
        Assertions.assertEquals(Set.of("Nothing"), directC, "wrong direct nodes for C");
        Assertions.assertEquals(Set.of("Nothing"), directD, "wrong direct nodes for D");
        Assertions.assertEquals(Set.of("Nothing"), directE, "wrong direct nodes for E");
        Assertions.assertEquals(Set.of("Nothing"), directF, "wrong direct nodes for F");

        Assertions.assertEquals(Set.of("C", "D", "E", "F", "Nothing"), indirectA, "wrong indirect nodes for A");
        Assertions.assertEquals(Set.of("C", "D", "E", "F", "Nothing"), indirectB, "wrong indirect nodes for B");
        Assertions.assertEquals(Set.of("D", "E", "F", "Nothing"), indirectC, "wrong indirect nodes for C");
        Assertions.assertEquals(Set.of("C", "E", "F", "Nothing"), indirectD, "wrong indirect nodes for D");
        Assertions.assertEquals(Set.of("C", "D", "F", "Nothing"), indirectE, "wrong indirect nodes for E");
        Assertions.assertEquals(Set.of("C", "D", "E", "Nothing"), indirectF, "wrong indirect nodes for F");
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM",
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_DL_MEM_RDFS_INF",
            "OWL2_DL_MEM_RULES_INF",
            "OWL2_MEM",
            "OWL2_MEM_RDFS_INF",
            "OWL2_MEM_RULES_INF",
            "OWL2_EL_MEM",
            "OWL2_EL_MEM_RDFS_INF",
            "OWL2_EL_MEM_RULES_INF",
            "OWL2_QL_MEM",
            "OWL2_QL_MEM_RDFS_INF",
            "OWL2_QL_MEM_RULES_INF",
            "OWL2_RL_MEM",
            "OWL2_RL_MEM_RDFS_INF",
            "OWL2_RL_MEM_RULES_INF",
            "OWL1_DL_MEM",
            "OWL1_DL_MEM_RDFS_INF",
            "OWL1_DL_MEM_RULES_INF",
            "OWL1_MEM",
            "OWL1_MEM_RDFS_INF",
            "OWL1_MEM_RULES_INF",
            "OWL1_LITE_MEM",
            "OWL1_LITE_MEM_RDFS_INF",
            "OWL1_LITE_MEM_RULES_INF",
            "RDFS_MEM",
            "RDFS_MEM_RDFS_INF",
    })
    public void testListSubClasses11a(TestSpec spec) {
        //    A
        //  /  \
        // B  = C
        OntModel m = createClassesABC(OntModelFactory.createModel(spec.inst));

        Set<String> directA = subClasses(m, "A", true);
        Set<String> indirectA = subClasses(m, "A", false);

        Set<String> directB = subClasses(m, "B", true);
        Set<String> indirectB = subClasses(m, "B", false);

        Set<String> directC = subClasses(m, "C", true);
        Set<String> indirectC = subClasses(m, "C", false);

        Assertions.assertEquals(Set.of("B", "C"), directA, "wrong direct nodes for A");
        Assertions.assertEquals(Set.of(), directB, "wrong direct nodes for B");
        Assertions.assertEquals(Set.of(), directC, "wrong direct nodes for C");

        Assertions.assertEquals(Set.of("B", "C"), indirectA, "wrong indirect nodes for A");
        Assertions.assertEquals(Set.of("C"), indirectB, "wrong indirect nodes for B");
        Assertions.assertEquals(Set.of("B"), indirectC, "wrong indirect nodes for C");
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
            "RDFS_MEM_TRANS_INF",
    })
    public void testListSubClasses11b(TestSpec spec) {
        //    A
        //  /  \
        // B  = C
        OntModel m = createClassesABC(OntModelFactory.createModel(spec.inst));

        Set<String> directA = subClasses(m, "A", true);
        Set<String> indirectA = subClasses(m, "A", false);

        Set<String> directB = subClasses(m, "B", true);
        Set<String> indirectB = subClasses(m, "B", false);

        Set<String> directC = subClasses(m, "C", true);
        Set<String> indirectC = subClasses(m, "C", false);

        Assertions.assertEquals(Set.of("B", "C"), directA, "wrong direct nodes for A");
        Assertions.assertEquals(Set.of("C"), directB, "wrong direct nodes for B");
        Assertions.assertEquals(Set.of("B"), directC, "wrong direct nodes for C");

        Assertions.assertEquals(Set.of("B", "C"), indirectA, "wrong indirect nodes for A");
        Assertions.assertEquals(Set.of("C"), indirectB, "wrong indirect nodes for B");
        Assertions.assertEquals(Set.of("B"), indirectC, "wrong indirect nodes for C");
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_MEM_MICRO_RULES_INF",
            "OWL1_MEM_MICRO_RULES_INF",
    })
    public void testListSubClasses11d(TestSpec spec) {
        //    A
        //  /  \
        // B  = C
        OntModel m = createClassesABC(OntModelFactory.createModel(spec.inst));

        Set<String> directA = subClasses(m, "A", true);
        Set<String> indirectA = subClasses(m, "A", false);

        Set<String> directB = subClasses(m, "B", true);
        Set<String> indirectB = subClasses(m, "B", false);

        Set<String> directC = subClasses(m, "C", true);
        Set<String> indirectC = subClasses(m, "C", false);

        Assertions.assertEquals(Set.of("B", "C"), directA, "wrong direct nodes for A");
        Assertions.assertEquals(Set.of("Nothing"), directB, "wrong direct nodes for B");
        Assertions.assertEquals(Set.of("Nothing"), directC, "wrong direct nodes for C");

        Assertions.assertEquals(Set.of("B", "C", "Nothing"), indirectA, "wrong indirect nodes for A");
        Assertions.assertEquals(Set.of("C", "Nothing"), indirectB, "wrong indirect nodes for B");
        Assertions.assertEquals(Set.of("B", "Nothing"), indirectC, "wrong indirect nodes for C");
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM",
            "OWL2_MEM",
            "OWL2_EL_MEM",
            "OWL2_QL_MEM",
            "OWL2_RL_MEM",
            "OWL1_DL_MEM",
            "OWL1_MEM",
            "OWL1_LITE_MEM",
            "RDFS_MEM",
    })
    public void testHasSubClasses1a(TestSpec spec) {
        //      A
        //     / \
        //    B   C
        //   / \ / \
        //  D   E   F
        OntModel m = createClassesABCDEF(OntModelFactory.createModel(spec.inst));

        OntClass A = m.getOntClass(NS + "A");
        OntClass B = m.getOntClass(NS + "B");
        OntClass C = m.getOntClass(NS + "C");
        OntClass D = m.getOntClass(NS + "D");
        OntClass E = m.getOntClass(NS + "E");
        OntClass F = m.getOntClass(NS + "F");

        Assertions.assertTrue(A.hasSubClass(A, false));
        Assertions.assertTrue(A.hasSubClass(B, false));
        Assertions.assertTrue(A.hasSubClass(C, false));
        Assertions.assertFalse(A.hasSubClass(D, false));
        Assertions.assertFalse(A.hasSubClass(E, false));
        Assertions.assertFalse(A.hasSubClass(F, false));
        Assertions.assertFalse(B.hasSubClass(A, false));
        Assertions.assertTrue(B.hasSubClass(B, false));
        Assertions.assertFalse(B.hasSubClass(C, false));
        Assertions.assertTrue(B.hasSubClass(D, false));
        Assertions.assertTrue(B.hasSubClass(E, false));
        Assertions.assertFalse(B.hasSubClass(F, false));
        Assertions.assertFalse(C.hasSubClass(A, false));
        Assertions.assertFalse(C.hasSubClass(B, false));
        Assertions.assertTrue(C.hasSubClass(C, false));
        Assertions.assertFalse(C.hasSubClass(D, false));
        Assertions.assertTrue(C.hasSubClass(E, false));
        Assertions.assertTrue(C.hasSubClass(F, false));
        Assertions.assertFalse(D.hasSubClass(A, false));
        Assertions.assertFalse(D.hasSubClass(B, false));
        Assertions.assertFalse(D.hasSubClass(C, false));
        Assertions.assertTrue(D.hasSubClass(D, false));
        Assertions.assertFalse(D.hasSubClass(E, false));
        Assertions.assertFalse(D.hasSubClass(F, false));
        Assertions.assertFalse(E.hasSubClass(A, false));
        Assertions.assertFalse(E.hasSubClass(B, false));
        Assertions.assertFalse(E.hasSubClass(C, false));
        Assertions.assertFalse(E.hasSubClass(D, false));
        Assertions.assertTrue(E.hasSubClass(E, false));
        Assertions.assertFalse(E.hasSubClass(F, false));
        Assertions.assertFalse(F.hasSubClass(A, false));
        Assertions.assertFalse(F.hasSubClass(B, false));
        Assertions.assertFalse(F.hasSubClass(C, false));
        Assertions.assertFalse(F.hasSubClass(D, false));
        Assertions.assertFalse(F.hasSubClass(E, false));
        Assertions.assertTrue(F.hasSubClass(F, false));

        Assertions.assertTrue(A.hasSubClass(A, true));
        Assertions.assertTrue(A.hasSubClass(B, true));
        Assertions.assertTrue(A.hasSubClass(C, true));
        Assertions.assertFalse(A.hasSubClass(D, true));
        Assertions.assertFalse(A.hasSubClass(E, true));
        Assertions.assertFalse(A.hasSubClass(F, true));
        Assertions.assertFalse(B.hasSubClass(A, true));
        Assertions.assertTrue(B.hasSubClass(B, true));
        Assertions.assertFalse(B.hasSubClass(C, true));
        Assertions.assertTrue(B.hasSubClass(D, true));
        Assertions.assertTrue(B.hasSubClass(E, true));
        Assertions.assertFalse(B.hasSubClass(F, true));
        Assertions.assertFalse(C.hasSubClass(A, true));
        Assertions.assertFalse(C.hasSubClass(B, true));
        Assertions.assertTrue(C.hasSubClass(C, true));
        Assertions.assertFalse(C.hasSubClass(D, true));
        Assertions.assertTrue(C.hasSubClass(E, true));
        Assertions.assertTrue(C.hasSubClass(F, true));
        Assertions.assertFalse(D.hasSubClass(A, true));
        Assertions.assertFalse(D.hasSubClass(B, true));
        Assertions.assertFalse(D.hasSubClass(C, true));
        Assertions.assertTrue(D.hasSubClass(D, true));
        Assertions.assertFalse(D.hasSubClass(E, true));
        Assertions.assertFalse(D.hasSubClass(F, true));
        Assertions.assertFalse(E.hasSubClass(A, true));
        Assertions.assertFalse(E.hasSubClass(B, true));
        Assertions.assertFalse(E.hasSubClass(C, true));
        Assertions.assertFalse(E.hasSubClass(D, true));
        Assertions.assertTrue(E.hasSubClass(E, true));
        Assertions.assertFalse(E.hasSubClass(F, true));
        Assertions.assertFalse(F.hasSubClass(A, true));
        Assertions.assertFalse(F.hasSubClass(B, true));
        Assertions.assertFalse(F.hasSubClass(C, true));
        Assertions.assertFalse(F.hasSubClass(D, true));
        Assertions.assertFalse(F.hasSubClass(E, true));
        Assertions.assertTrue(F.hasSubClass(F, true));
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_DL_MEM_RDFS_INF",
            "OWL2_DL_MEM_RULES_INF",
            "OWL2_MEM_RDFS_INF",
            "OWL2_MEM_RULES_INF",
            "OWL2_MEM_MINI_RULES_INF",
            "OWL2_MEM_MICRO_RULES_INF",
            "OWL2_EL_MEM_RDFS_INF",
            "OWL2_EL_MEM_RULES_INF",
            "OWL2_QL_MEM_RDFS_INF",
            "OWL2_QL_MEM_RULES_INF",
            "OWL2_RL_MEM_RDFS_INF",
            "OWL2_RL_MEM_RULES_INF",
            "OWL1_DL_MEM_RDFS_INF",
            "OWL1_DL_MEM_RULES_INF",
            "OWL1_MEM_RDFS_INF",
            "OWL1_MEM_RULES_INF",
            "OWL1_MEM_MINI_RULES_INF",
            "OWL1_MEM_MICRO_RULES_INF",
            "OWL1_LITE_MEM_RDFS_INF",
            "OWL1_LITE_MEM_RULES_INF",
            "RDFS_MEM_RDFS_INF",
    })
    public void testHasSubClasses1b(TestSpec spec) {
        //      A
        //     / \
        //    B   C
        //   / \ / \
        //  D   E   F
        OntModel m = createClassesABCDEF(OntModelFactory.createModel(spec.inst));

        OntClass A = m.getOntClass(NS + "A");
        OntClass B = m.getOntClass(NS + "B");
        OntClass C = m.getOntClass(NS + "C");
        OntClass D = m.getOntClass(NS + "D");
        OntClass E = m.getOntClass(NS + "E");
        OntClass F = m.getOntClass(NS + "F");

        Assertions.assertTrue(A.hasSubClass(A, false));
        Assertions.assertTrue(A.hasSubClass(B, false));
        Assertions.assertTrue(A.hasSubClass(C, false));
        Assertions.assertTrue(A.hasSubClass(D, false));
        Assertions.assertTrue(A.hasSubClass(E, false));
        Assertions.assertTrue(A.hasSubClass(F, false));
        Assertions.assertFalse(B.hasSubClass(A, false));
        Assertions.assertTrue(B.hasSubClass(B, false));
        Assertions.assertFalse(B.hasSubClass(C, false));
        Assertions.assertTrue(B.hasSubClass(D, false));
        Assertions.assertTrue(B.hasSubClass(E, false));
        Assertions.assertFalse(B.hasSubClass(F, false));
        Assertions.assertFalse(C.hasSubClass(A, false));
        Assertions.assertFalse(C.hasSubClass(B, false));
        Assertions.assertTrue(C.hasSubClass(C, false));
        Assertions.assertFalse(C.hasSubClass(D, false));
        Assertions.assertTrue(C.hasSubClass(E, false));
        Assertions.assertTrue(C.hasSubClass(F, false));
        Assertions.assertFalse(D.hasSubClass(A, false));
        Assertions.assertFalse(D.hasSubClass(B, false));
        Assertions.assertFalse(D.hasSubClass(C, false));
        Assertions.assertTrue(D.hasSubClass(D, false));
        Assertions.assertFalse(D.hasSubClass(E, false));
        Assertions.assertFalse(D.hasSubClass(F, false));
        Assertions.assertFalse(E.hasSubClass(A, false));
        Assertions.assertFalse(E.hasSubClass(B, false));
        Assertions.assertFalse(E.hasSubClass(C, false));
        Assertions.assertFalse(E.hasSubClass(D, false));
        Assertions.assertTrue(E.hasSubClass(E, false));
        Assertions.assertFalse(E.hasSubClass(F, false));
        Assertions.assertFalse(F.hasSubClass(A, false));
        Assertions.assertFalse(F.hasSubClass(B, false));
        Assertions.assertFalse(F.hasSubClass(C, false));
        Assertions.assertFalse(F.hasSubClass(D, false));
        Assertions.assertFalse(F.hasSubClass(E, false));
        Assertions.assertTrue(F.hasSubClass(F, false));

        Assertions.assertTrue(A.hasSubClass(A, true));
        Assertions.assertTrue(A.hasSubClass(B, true));
        Assertions.assertTrue(A.hasSubClass(C, true));
        Assertions.assertFalse(A.hasSubClass(D, true));
        Assertions.assertFalse(A.hasSubClass(E, true));
        Assertions.assertFalse(A.hasSubClass(F, true));
        Assertions.assertFalse(B.hasSubClass(A, true));
        Assertions.assertTrue(B.hasSubClass(B, true));
        Assertions.assertFalse(B.hasSubClass(C, true));
        Assertions.assertTrue(B.hasSubClass(D, true));
        Assertions.assertTrue(B.hasSubClass(E, true));
        Assertions.assertFalse(B.hasSubClass(F, true));
        Assertions.assertFalse(C.hasSubClass(A, true));
        Assertions.assertFalse(C.hasSubClass(B, true));
        Assertions.assertTrue(C.hasSubClass(C, true));
        Assertions.assertFalse(C.hasSubClass(D, true));
        Assertions.assertTrue(C.hasSubClass(E, true));
        Assertions.assertTrue(C.hasSubClass(F, true));
        Assertions.assertFalse(D.hasSubClass(A, true));
        Assertions.assertFalse(D.hasSubClass(B, true));
        Assertions.assertFalse(D.hasSubClass(C, true));
        Assertions.assertTrue(D.hasSubClass(D, true));
        Assertions.assertFalse(D.hasSubClass(E, true));
        Assertions.assertFalse(D.hasSubClass(F, true));
        Assertions.assertFalse(E.hasSubClass(A, true));
        Assertions.assertFalse(E.hasSubClass(B, true));
        Assertions.assertFalse(E.hasSubClass(C, true));
        Assertions.assertFalse(E.hasSubClass(D, true));
        Assertions.assertTrue(E.hasSubClass(E, true));
        Assertions.assertFalse(E.hasSubClass(F, true));
        Assertions.assertFalse(F.hasSubClass(A, true));
        Assertions.assertFalse(F.hasSubClass(B, true));
        Assertions.assertFalse(F.hasSubClass(C, true));
        Assertions.assertFalse(F.hasSubClass(D, true));
        Assertions.assertFalse(F.hasSubClass(E, true));
        Assertions.assertTrue(F.hasSubClass(F, true));
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
            "RDFS_MEM_TRANS_INF",
    })
    public void testHasSubClasses1c(TestSpec spec) {
        //      A
        //     / \
        //    B   C
        //   / \ / \
        //  D   E   F
        OntModel m = createClassesABCDEF(OntModelFactory.createModel(spec.inst));

        OntClass A = m.getOntClass(NS + "A");
        OntClass B = m.getOntClass(NS + "B");
        OntClass C = m.getOntClass(NS + "C");
        OntClass D = m.getOntClass(NS + "D");
        OntClass E = m.getOntClass(NS + "E");
        OntClass F = m.getOntClass(NS + "F");

        Assertions.assertTrue(A.hasSubClass(A, false));
        Assertions.assertTrue(A.hasSubClass(B, false));
        Assertions.assertTrue(A.hasSubClass(C, false));
        Assertions.assertTrue(A.hasSubClass(D, false));
        Assertions.assertTrue(A.hasSubClass(E, false));
        Assertions.assertTrue(A.hasSubClass(F, false));
        Assertions.assertFalse(B.hasSubClass(A, false));
        Assertions.assertTrue(B.hasSubClass(B, false));
        Assertions.assertFalse(B.hasSubClass(C, false));
        Assertions.assertTrue(B.hasSubClass(D, false));
        Assertions.assertTrue(B.hasSubClass(E, false));
        Assertions.assertFalse(B.hasSubClass(F, false));
        Assertions.assertFalse(C.hasSubClass(A, false));
        Assertions.assertFalse(C.hasSubClass(B, false));
        Assertions.assertTrue(C.hasSubClass(C, false));
        Assertions.assertFalse(C.hasSubClass(D, false));
        Assertions.assertTrue(C.hasSubClass(E, false));
        Assertions.assertTrue(C.hasSubClass(F, false));
        Assertions.assertFalse(D.hasSubClass(A, false));
        Assertions.assertFalse(D.hasSubClass(B, false));
        Assertions.assertFalse(D.hasSubClass(C, false));
        Assertions.assertTrue(D.hasSubClass(D, false));
        Assertions.assertFalse(D.hasSubClass(E, false));
        Assertions.assertFalse(D.hasSubClass(F, false));
        Assertions.assertFalse(E.hasSubClass(A, false));
        Assertions.assertFalse(E.hasSubClass(B, false));
        Assertions.assertFalse(E.hasSubClass(C, false));
        Assertions.assertFalse(E.hasSubClass(D, false));
        Assertions.assertTrue(E.hasSubClass(E, false));
        Assertions.assertFalse(E.hasSubClass(F, false));
        Assertions.assertFalse(F.hasSubClass(A, false));
        Assertions.assertFalse(F.hasSubClass(B, false));
        Assertions.assertFalse(F.hasSubClass(C, false));
        Assertions.assertFalse(F.hasSubClass(D, false));
        Assertions.assertFalse(F.hasSubClass(E, false));
        Assertions.assertTrue(F.hasSubClass(F, false));

        Assertions.assertTrue(A.hasSubClass(A, true));
        Assertions.assertTrue(A.hasSubClass(B, true));
        Assertions.assertTrue(A.hasSubClass(C, true));
        Assertions.assertFalse(A.hasSubClass(D, true));
        Assertions.assertFalse(A.hasSubClass(E, true));
        Assertions.assertFalse(A.hasSubClass(F, true));
        Assertions.assertFalse(B.hasSubClass(A, true));
        Assertions.assertTrue(B.hasSubClass(B, true));
        Assertions.assertFalse(B.hasSubClass(C, true));
        Assertions.assertTrue(B.hasSubClass(D, true));
        Assertions.assertTrue(B.hasSubClass(E, true));
        Assertions.assertFalse(B.hasSubClass(F, true));
        Assertions.assertFalse(C.hasSubClass(A, true));
        Assertions.assertFalse(C.hasSubClass(B, true));
        Assertions.assertTrue(C.hasSubClass(C, true));
        Assertions.assertFalse(C.hasSubClass(D, true));
        Assertions.assertTrue(C.hasSubClass(E, true));
        Assertions.assertTrue(C.hasSubClass(F, true));
        Assertions.assertFalse(D.hasSubClass(A, true));
        Assertions.assertFalse(D.hasSubClass(B, true));
        Assertions.assertFalse(D.hasSubClass(C, true));
        Assertions.assertTrue(D.hasSubClass(D, true));
        Assertions.assertFalse(D.hasSubClass(E, true));
        Assertions.assertFalse(D.hasSubClass(F, true));
        Assertions.assertFalse(E.hasSubClass(A, true));
        Assertions.assertFalse(E.hasSubClass(B, true));
        Assertions.assertFalse(E.hasSubClass(C, true));
        Assertions.assertFalse(E.hasSubClass(D, true));
        Assertions.assertTrue(E.hasSubClass(E, true));
        Assertions.assertFalse(E.hasSubClass(F, true));
        Assertions.assertFalse(F.hasSubClass(A, true));
        Assertions.assertFalse(F.hasSubClass(B, true));
        Assertions.assertFalse(F.hasSubClass(C, true));
        Assertions.assertFalse(F.hasSubClass(D, true));
        Assertions.assertFalse(F.hasSubClass(E, true));
        Assertions.assertTrue(F.hasSubClass(F, true));
    }

}
