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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.jena.ontapi.TestModelFactory.NS;

public class OntClassSuperClassesTest {
    private static Set<String> superClasses(OntModel m, String name, boolean direct) {
        return m.getOntClass(NS + name).superClasses(direct).map(Resource::getLocalName).collect(Collectors.toSet());
    }

    private static Set<String> superClasses(OntModel m, String name) {
        return m.getOntClass(NS + name).superClasses().map(Resource::getLocalName).collect(Collectors.toSet());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM",
            "OWL2_MEM_RDFS_INF",
            "OWL2_MEM_TRANS_INF",
            "OWL1_MEM",
            "OWL1_MEM_RDFS_INF",
            "OWL1_MEM_TRANS_INF",
            "RDFS_MEM",
            "RDFS_MEM_TRANS_INF",
    })
    public void testSuperClassNE(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst);
        OntClass a = m.createOntClass(NS + "A");
        Assertions.assertTrue(a.superClass().isEmpty());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_MEM",
            "OWL1_MEM",
            "OWL1_LITE_MEM",
            "RDFS_MEM",
    })
    public void testListSuperClasses1a(TestSpec spec) {
        //      A
        //     / \
        //    B   C
        //   / \ / \
        //  D   E   F

        OntModel m = TestModelFactory.createClassesABCDEF(OntModelFactory.createModel(spec.inst));

        Set<String> directA = superClasses(m, "A", true);
        Set<String> indirectA = superClasses(m, "A", false);

        Set<String> directB = superClasses(m, "B", true);
        Set<String> indirectB = superClasses(m, "B", false);

        Set<String> directC = superClasses(m, "C", true);
        Set<String> indirectC = superClasses(m, "C", false);

        Set<String> directD = superClasses(m, "D", true);
        Set<String> indirectD = superClasses(m, "D", false);

        Set<String> directE = superClasses(m, "E", true);
        Set<String> indirectE = superClasses(m, "E", false);

        Set<String> directF = superClasses(m, "F", true);
        Set<String> indirectF = superClasses(m, "F", false);

        Assertions.assertEquals(Set.of(), directA);
        Assertions.assertEquals(Set.of("A"), directB);
        Assertions.assertEquals(Set.of("A"), directC);
        Assertions.assertEquals(Set.of("B"), directD);
        Assertions.assertEquals(Set.of("B", "C"), directE);
        Assertions.assertEquals(Set.of("C"), directF);
        Assertions.assertEquals(Set.of(), indirectA);
        Assertions.assertEquals(Set.of("A"), indirectB);
        Assertions.assertEquals(Set.of("A"), indirectC);
        Assertions.assertEquals(Set.of("B"), indirectD);
        Assertions.assertEquals(Set.of("B", "C"), indirectE);
        Assertions.assertEquals(Set.of("C"), indirectF);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM_RDFS_INF",
            "OWL1_MEM_RDFS_INF",
    })
    public void testListSuperClasses1b(TestSpec spec) {
        //      A
        //     / \
        //    B   C
        //   / \ / \
        //  D   E   F

        OntModel m = TestModelFactory.createClassesABCDEF(OntModelFactory.createModel(spec.inst));

        Set<String> directA = superClasses(m, "A", true);
        Set<String> indirectA = superClasses(m, "A", false);

        Set<String> directB = superClasses(m, "B", true);
        Set<String> indirectB = superClasses(m, "B", false);

        Set<String> directC = superClasses(m, "C", true);
        Set<String> indirectC = superClasses(m, "C", false);

        Set<String> directD = superClasses(m, "D", true);
        Set<String> indirectD = superClasses(m, "D", false);

        Set<String> directE = superClasses(m, "E", true);
        Set<String> indirectE = superClasses(m, "E", false);

        Set<String> directF = superClasses(m, "F", true);
        Set<String> indirectF = superClasses(m, "F", false);

        Assertions.assertEquals(Set.of(), directA);
        Assertions.assertEquals(Set.of("A"), directB);
        Assertions.assertEquals(Set.of("A"), directC);
        Assertions.assertEquals(Set.of("B"), directD);
        Assertions.assertEquals(Set.of("B", "C"), directE);
        Assertions.assertEquals(Set.of("C"), directF);
        Assertions.assertEquals(Set.of(), indirectA);
        Assertions.assertEquals(Set.of("A"), indirectB);
        Assertions.assertEquals(Set.of("A"), indirectC);
        Assertions.assertEquals(Set.of("A", "B"), indirectD);
        Assertions.assertEquals(Set.of("A", "B", "C"), indirectE);
        Assertions.assertEquals(Set.of("A", "C"), indirectF);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL1_MEM_RULES_INF",
            "OWL1_LITE_MEM_RULES_INF",
    })
    public void testListSuperClasses1c(TestSpec spec) {
        //      A
        //     / \
        //    B   C
        //   / \ / \
        //  D   E   F

        OntModel m = TestModelFactory.createClassesABCDEF(OntModelFactory.createModel(spec.inst));

        Set<String> directA = superClasses(m, "A", true);
        Set<String> indirectA = superClasses(m, "A", false);

        Set<String> directB = superClasses(m, "B", true);
        Set<String> indirectB = superClasses(m, "B", false);

        Set<String> directC = superClasses(m, "C", true);
        Set<String> indirectC = superClasses(m, "C", false);

        Set<String> directD = superClasses(m, "D", true);
        Set<String> indirectD = superClasses(m, "D", false);

        Set<String> directE = superClasses(m, "E", true);
        Set<String> indirectE = superClasses(m, "E", false);

        Set<String> directF = superClasses(m, "F", true);
        Set<String> indirectF = superClasses(m, "F", false);

        Assertions.assertEquals(Set.of("Thing"), directA);
        Assertions.assertEquals(Set.of("A"), directB);
        Assertions.assertEquals(Set.of("A"), directC);
        Assertions.assertEquals(Set.of("B"), directD);
        Assertions.assertEquals(Set.of("B", "C"), directE);
        Assertions.assertEquals(Set.of("C"), directF);

        Assertions.assertEquals(Set.of("Resource", "Thing"), indirectA);
        Assertions.assertEquals(Set.of("A", "Resource", "Thing"), indirectB);
        Assertions.assertEquals(Set.of("A", "Resource", "Thing"), indirectC);
        Assertions.assertEquals(Set.of("A", "B", "Resource", "Thing"), indirectD);
        Assertions.assertEquals(Set.of("A", "B", "C", "Resource", "Thing"), indirectE);
        Assertions.assertEquals(Set.of("A", "C", "Resource", "Thing"), indirectF);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM",
            "OWL2_MEM_RDFS_INF",
            "OWL1_MEM",
            "OWL1_MEM_RDFS_INF",
            "RDFS_MEM",
    })
    public void testListSuperClasses3a(TestSpec spec) {
        // B = C
        //  \ |
        //    A

        OntModel m = TestModelFactory.createClassesBCA(OntModelFactory.createModel(spec.inst));

        Set<String> directA = superClasses(m, "A", true);
        Set<String> directB = superClasses(m, "B", true);
        Set<String> directC = superClasses(m, "C", true);

        Set<String> indirectA = superClasses(m, "A");
        Set<String> indirectB = superClasses(m, "B", false);
        Set<String> indirectC = superClasses(m, "C", false);

        Assertions.assertEquals(Set.of("B", "C"), directA);
        Assertions.assertEquals(Set.of(), directB);
        Assertions.assertEquals(Set.of(), directC);

        Assertions.assertEquals(Set.of("B", "C"), indirectA);
        Assertions.assertEquals(Set.of("C"), indirectB);
        Assertions.assertEquals(Set.of("B"), indirectC);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL1_MEM_RULES_INF",
            "OWL1_LITE_MEM_RULES_INF",
    })
    public void testListSuperClasses3b(TestSpec spec) {
        // B = C
        //  \ |
        //    A

        OntModel m = TestModelFactory.createClassesBCA(OntModelFactory.createModel(spec.inst));

        Set<String> directA = superClasses(m, "A", true);
        Set<String> directB = superClasses(m, "B", true);
        Set<String> directC = superClasses(m, "C", true);

        Set<String> indirectA = superClasses(m, "A");
        Set<String> indirectB = superClasses(m, "B", false);
        Set<String> indirectC = superClasses(m, "C", false);

        Assertions.assertEquals(Set.of("B", "C"), directA);
        Assertions.assertEquals(Set.of("Thing"), directB);
        Assertions.assertEquals(Set.of("Thing"), directC);

        Assertions.assertEquals(Set.of("B", "C", "Resource", "Thing"), indirectA);
        Assertions.assertEquals(Set.of("C", "Resource", "Thing"), indirectB);
        Assertions.assertEquals(Set.of("B", "Resource", "Thing"), indirectC);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_MEM_TRANS_INF",
            "OWL1_MEM_TRANS_INF",
            "RDFS_MEM_TRANS_INF",
    })
    public void testListSuperClasses3c(TestSpec spec) {
        // B = C
        //  \ |
        //    A

        OntModel m = TestModelFactory.createClassesBCA(OntModelFactory.createModel(spec.inst));

        Set<String> directA = superClasses(m, "A", true);
        Set<String> directB = superClasses(m, "B", true);
        Set<String> directC = superClasses(m, "C", true);

        Set<String> indirectA = superClasses(m, "A");
        Set<String> indirectB = superClasses(m, "B", false);
        Set<String> indirectC = superClasses(m, "C", false);

        Assertions.assertEquals(Set.of("B", "C"), directA);
        Assertions.assertEquals(Set.of("C"), directB);
        Assertions.assertEquals(Set.of("B"), directC);

        Assertions.assertEquals(Set.of("B", "C"), indirectA);
        Assertions.assertEquals(Set.of("C"), indirectB);
        Assertions.assertEquals(Set.of("B"), indirectC);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "RDFS_MEM_RDFS_INF",
    })
    public void testListSuperClasses3d(TestSpec spec) {
        // B = C
        //  \ |
        //    A

        OntModel m = TestModelFactory.createClassesBCA(OntModelFactory.createModel(spec.inst));

        Set<String> directA = superClasses(m, "A", true);
        Set<String> directB = superClasses(m, "B", true);
        Set<String> directC = superClasses(m, "C", true);

        Set<String> indirectA = superClasses(m, "A");
        Set<String> indirectB = superClasses(m, "B", false);
        Set<String> indirectC = superClasses(m, "C", false);

        String RT = spec == TestSpec.RDFS_MEM_RDFS_INF ? "Resource" : "Thing";

        Assertions.assertEquals(Set.of("B", "C"), directA);
        Assertions.assertEquals(Set.of(RT), directB);
        Assertions.assertEquals(Set.of(RT), directC);

        Assertions.assertEquals(Set.of("B", "C", RT), indirectA);
        Assertions.assertEquals(Set.of("C", RT), indirectB);
        Assertions.assertEquals(Set.of("B", RT), indirectC);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM_RDFS_INF",
            "OWL1_MEM_RDFS_INF",
    })
    public void testListSuperClasses5a(TestSpec spec) {
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

        Set<String> directA = superClasses(m, "A", true);
        Set<String> indirectA = superClasses(m, "A", false);

        Set<String> directB = superClasses(m, "B", true);
        Set<String> indirectB = superClasses(m, "B");

        Set<String> directC = superClasses(m, "C", true);
        Set<String> indirectC = superClasses(m, "C", false);

        Set<String> directD = superClasses(m, "D", true);
        Set<String> indirectD = superClasses(m, "D", false);

        Set<String> directE = superClasses(m, "E", true);
        Set<String> indirectE = superClasses(m, "E");

        Set<String> directF = superClasses(m, "F", true);
        Set<String> indirectF = superClasses(m, "F", false);

        Set<String> directG = superClasses(m, "G", true);
        Set<String> indirectG = superClasses(m, "G", false);

        Set<String> directH = superClasses(m, "H", true);
        Set<String> indirectH = superClasses(m, "H", true);

        Set<String> directK = superClasses(m, "K", true);
        Set<String> indirectK = superClasses(m, "K");

        Set<String> directL = superClasses(m, "L", true);
        Set<String> indirectL = superClasses(m, "L", false);

        Set<String> directM = superClasses(m, "M", true);
        Set<String> indirectM = superClasses(m, "M");

        Assertions.assertEquals(Set.of(), directA);
        Assertions.assertEquals(Set.of("A"), directB);
        Assertions.assertEquals(Set.of("A"), directC);
        Assertions.assertEquals(Set.of("B"), directD);
        Assertions.assertEquals(Set.of("B", "C"), directE);
        Assertions.assertEquals(Set.of("C"), directF);
        Assertions.assertEquals(Set.of("D"), directG);
        Assertions.assertEquals(Set.of("D"), directH);
        Assertions.assertEquals(Set.of("D"), directK);
        Assertions.assertEquals(Set.of("H", "K"), directL);
        Assertions.assertEquals(Set.of("H", "K"), directM);

        Assertions.assertEquals(Set.of(), indirectA);
        Assertions.assertEquals(Set.of("A"), indirectB);
        Assertions.assertEquals(Set.of("A"), indirectC);
        Assertions.assertEquals(Set.of("A", "B"), indirectD);
        Assertions.assertEquals(Set.of("A", "B", "C"), indirectE);
        Assertions.assertEquals(Set.of("A", "C"), indirectF);
        Assertions.assertEquals(Set.of("A", "B", "D"), indirectG);
        Assertions.assertEquals(Set.of("D"), indirectH);
        Assertions.assertEquals(Set.of("A", "B", "D", "H"), indirectK);
        Assertions.assertEquals(Set.of("A", "B", "D", "H", "K"), indirectL);
        Assertions.assertEquals(Set.of("A", "B", "D", "H", "K"), indirectM);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM",
            "OWL2_MEM_RDFS_INF",
            "OWL2_MEM_TRANS_INF",
            "OWL1_MEM",
            "OWL1_MEM_RDFS_INF",
            "OWL1_MEM_TRANS_INF",
            "RDFS_MEM",
            "RDFS_MEM_TRANS_INF",
    })
    public void testListSuperClasses6a(TestSpec spec) {
        //     D
        //    | \
        // B  |  C
        //  \ | /
        //    A

        OntModel m = TestModelFactory.createClassesDBCA(OntModelFactory.createModel(spec.inst));

        Set<String> directA = superClasses(m, "A", true);
        Set<String> directB = superClasses(m, "B", true);
        Set<String> directC = superClasses(m, "C", true);
        Set<String> directD = superClasses(m, "D", true);

        Set<String> indirectA = superClasses(m, "A");
        Set<String> indirectB = superClasses(m, "B", false);
        Set<String> indirectC = superClasses(m, "C", false);
        Set<String> indirectD = superClasses(m, "D", true);

        Assertions.assertEquals(Set.of("B", "C"), directA);
        Assertions.assertEquals(Set.of(), directB);
        Assertions.assertEquals(Set.of("D"), directC);
        Assertions.assertEquals(Set.of(), directD);

        Assertions.assertEquals(Set.of("B", "C", "D"), indirectA);
        Assertions.assertEquals(Set.of(), indirectB);
        Assertions.assertEquals(Set.of("D"), indirectC);
        Assertions.assertEquals(Set.of(), indirectD);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM",
            "OWL2_MEM_RDFS_INF",
            "OWL2_MEM_TRANS_INF",
            "OWL1_MEM",
            "OWL1_MEM_RDFS_INF",
            "OWL1_MEM_TRANS_INF",
            "RDFS_MEM",
            "RDFS_MEM_TRANS_INF",
    })
    public void testListSuperClasses7a(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst);
        OntClass A = m.createOntClass(NS + "A");
        A.addSuperClass(A);

        Assertions.assertTrue(A.superClasses(true).findFirst().isEmpty());
        Assertions.assertTrue(A.superClasses(false).findFirst().isEmpty());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_MEM",
            "OWL1_MEM",
            "RDFS_MEM",
    })
    public void testListSuperClasses8a(TestSpec spec) {
        //    D
        //  /  \
        // B    F
        // |    |
        // C    E
        //  \  /
        //    A

        OntModel m = TestModelFactory.createClassesDBFCEA(OntModelFactory.createModel(spec.inst));

        Set<String> directA = superClasses(m, "A", true);
        Set<String> indirectA = superClasses(m, "A", false);

        Set<String> directB = superClasses(m, "B", true);
        Set<String> indirectB = superClasses(m, "B", false);

        Set<String> directC = superClasses(m, "C", true);
        Set<String> indirectC = superClasses(m, "C", false);

        Set<String> directD = superClasses(m, "D", true);
        Set<String> indirectD = superClasses(m, "D", false);

        Set<String> directE = superClasses(m, "E", true);
        Set<String> indirectE = superClasses(m, "E", false);

        Set<String> directF = superClasses(m, "F", true);
        Set<String> indirectF = superClasses(m, "F", false);

        Assertions.assertEquals(Set.of("C", "E"), directA);
        Assertions.assertEquals(Set.of("D"), directB);
        Assertions.assertEquals(Set.of("B"), directC);
        Assertions.assertEquals(Set.of(), directD);
        Assertions.assertEquals(Set.of("F"), directE);
        Assertions.assertEquals(Set.of("D"), directF);

        Assertions.assertEquals(Set.of("C", "E"), indirectA);
        Assertions.assertEquals(Set.of("D"), indirectB);
        Assertions.assertEquals(Set.of("B"), indirectC);
        Assertions.assertEquals(Set.of(), indirectD);
        Assertions.assertEquals(Set.of("F"), indirectE);
        Assertions.assertEquals(Set.of("D"), indirectF);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM_RDFS_INF",
            "OWL2_MEM_TRANS_INF",
            "OWL1_MEM_RDFS_INF",
            "OWL1_MEM_TRANS_INF",
            "RDFS_MEM_TRANS_INF",
    })
    public void testListSuperClasses8b(TestSpec spec) {
        //    D
        //  /  \
        // B    F
        // |    |
        // C    E
        //  \  /
        //    A

        OntModel m = TestModelFactory.createClassesDBFCEA(OntModelFactory.createModel(spec.inst));

        Set<String> directA = superClasses(m, "A", true);
        Set<String> indirectA = superClasses(m, "A", false);

        Set<String> directB = superClasses(m, "B", true);
        Set<String> indirectB = superClasses(m, "B", false);

        Set<String> directC = superClasses(m, "C", true);
        Set<String> indirectC = superClasses(m, "C", false);

        Set<String> directD = superClasses(m, "D", true);
        Set<String> indirectD = superClasses(m, "D", false);

        Set<String> directE = superClasses(m, "E", true);
        Set<String> indirectE = superClasses(m, "E", false);

        Set<String> directF = superClasses(m, "F", true);
        Set<String> indirectF = superClasses(m, "F", false);

        Assertions.assertEquals(Set.of("C", "E"), directA);
        Assertions.assertEquals(Set.of("D"), directB);
        Assertions.assertEquals(Set.of("B"), directC);
        Assertions.assertEquals(Set.of(), directD);
        Assertions.assertEquals(Set.of("F"), directE);
        Assertions.assertEquals(Set.of("D"), directF);

        Assertions.assertEquals(Set.of("B", "C", "D", "E", "F"), indirectA);
        Assertions.assertEquals(Set.of("D"), indirectB);
        Assertions.assertEquals(Set.of("B", "D"), indirectC);
        Assertions.assertEquals(Set.of(), indirectD);
        Assertions.assertEquals(Set.of("D", "F"), indirectE);
        Assertions.assertEquals(Set.of("D"), indirectF);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL1_MEM_RULES_INF",
            "OWL1_LITE_MEM_RULES_INF",
    })
    public void testListSuperClasses8c(TestSpec spec) {
        //    D
        //  /  \
        // B    F
        // |    |
        // C    E
        //  \  /
        //    A

        OntModel m = TestModelFactory.createClassesDBFCEA(OntModelFactory.createModel(spec.inst));

        Set<String> directA = superClasses(m, "A", true);
        Set<String> indirectA = superClasses(m, "A", false);

        Set<String> directB = superClasses(m, "B", true);
        Set<String> indirectB = superClasses(m, "B", false);

        Set<String> directC = superClasses(m, "C", true);
        Set<String> indirectC = superClasses(m, "C", false);

        Set<String> directD = superClasses(m, "D", true);
        Set<String> indirectD = superClasses(m, "D", false);

        Set<String> directE = superClasses(m, "E", true);
        Set<String> indirectE = superClasses(m, "E", false);

        Set<String> directF = superClasses(m, "F", true);
        Set<String> indirectF = superClasses(m, "F", false);

        Assertions.assertEquals(Set.of("C", "E"), directA);
        Assertions.assertEquals(Set.of("D"), directB);
        Assertions.assertEquals(Set.of("B"), directC);
        Assertions.assertEquals(Set.of("Thing"), directD);
        Assertions.assertEquals(Set.of("F"), directE);
        Assertions.assertEquals(Set.of("D"), directF);

        Assertions.assertEquals(Set.of("B", "C", "D", "E", "F", "Resource", "Thing"), indirectA);
        Assertions.assertEquals(Set.of("D", "Resource", "Thing"), indirectB);
        Assertions.assertEquals(Set.of("B", "D", "Resource", "Thing"), indirectC);
        Assertions.assertEquals(Set.of("Resource", "Thing"), indirectD);
        Assertions.assertEquals(Set.of("D", "F", "Resource", "Thing"), indirectE);
        Assertions.assertEquals(Set.of("D", "Resource", "Thing"), indirectF);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "RDFS_MEM_RDFS_INF",
    })
    public void testListSuperClasses8g(TestSpec spec) {
        //    D
        //  /  \
        // B    F
        // |    |
        // C    E
        //  \  /
        //    A

        OntModel m = TestModelFactory.createClassesDBFCEA(OntModelFactory.createModel(spec.inst));

        Set<String> directA = superClasses(m, "A", true);
        Set<String> indirectA = superClasses(m, "A", false);

        Set<String> directB = superClasses(m, "B", true);
        Set<String> indirectB = superClasses(m, "B", false);

        Set<String> directC = superClasses(m, "C", true);
        Set<String> indirectC = superClasses(m, "C", false);

        Set<String> directD = superClasses(m, "D", true);
        Set<String> indirectD = superClasses(m, "D", false);

        Set<String> directE = superClasses(m, "E", true);
        Set<String> indirectE = superClasses(m, "E", false);

        Set<String> directF = superClasses(m, "F", true);
        Set<String> indirectF = superClasses(m, "F", false);

        Assertions.assertEquals(Set.of("C", "E"), directA);
        Assertions.assertEquals(Set.of("D"), directB);
        Assertions.assertEquals(Set.of("B"), directC);
        Assertions.assertEquals(Set.of("Resource"), directD);
        Assertions.assertEquals(Set.of("F"), directE);
        Assertions.assertEquals(Set.of("D"), directF);

        Assertions.assertEquals(Set.of("B", "C", "D", "E", "F", "Resource"), indirectA);
        Assertions.assertEquals(Set.of("D", "Resource"), indirectB);
        Assertions.assertEquals(Set.of("B", "D", "Resource"), indirectC);
        Assertions.assertEquals(Set.of("Resource"), indirectD);
        Assertions.assertEquals(Set.of("D", "F", "Resource"), indirectE);
        Assertions.assertEquals(Set.of("D", "Resource"), indirectF);
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
    public void testHasSuperClass1a(TestSpec spec) {
        //     D
        //    | \
        // B  |  C
        //  \ | /
        //    A
        OntModel m = TestModelFactory.createClassesDBCA(OntModelFactory.createModel(spec.inst));
        OntClass A = m.createOntClass(NS + "A");
        OntClass B = m.createOntClass(NS + "B");
        OntClass C = m.createOntClass(NS + "C");
        OntClass D = m.createOntClass(NS + "D");

        Assertions.assertTrue(A.hasSuperClass(A, false));
        Assertions.assertTrue(A.hasSuperClass(B, false));
        Assertions.assertTrue(A.hasSuperClass(C, false));
        Assertions.assertTrue(A.hasSuperClass(D, false));
        Assertions.assertFalse(B.hasSuperClass(A, false));
        Assertions.assertTrue(B.hasSuperClass(B, false));
        Assertions.assertFalse(B.hasSuperClass(C, false));
        Assertions.assertFalse(B.hasSuperClass(D, false));
        Assertions.assertFalse(C.hasSuperClass(A, false));
        Assertions.assertFalse(C.hasSuperClass(B, false));
        Assertions.assertTrue(C.hasSuperClass(C, false));
        Assertions.assertTrue(C.hasSuperClass(D, false));
        Assertions.assertFalse(D.hasSuperClass(A, false));
        Assertions.assertFalse(D.hasSuperClass(B, false));
        Assertions.assertFalse(D.hasSuperClass(C, false));
        Assertions.assertTrue(D.hasSuperClass(D, false));

        Assertions.assertTrue(A.hasSuperClass(A, true));
        Assertions.assertTrue(A.hasSuperClass(B, true));
        Assertions.assertTrue(A.hasSuperClass(C, true));
        Assertions.assertFalse(A.hasSuperClass(D, true));
        Assertions.assertFalse(B.hasSuperClass(A, true));
        Assertions.assertTrue(B.hasSuperClass(B, true));
        Assertions.assertFalse(B.hasSuperClass(C, true));
        Assertions.assertFalse(B.hasSuperClass(D, true));
        Assertions.assertFalse(C.hasSuperClass(A, true));
        Assertions.assertFalse(C.hasSuperClass(B, true));
        Assertions.assertTrue(C.hasSuperClass(C, true));
        Assertions.assertTrue(C.hasSuperClass(D, true));
        Assertions.assertFalse(D.hasSuperClass(A, true));
        Assertions.assertFalse(D.hasSuperClass(B, true));
        Assertions.assertFalse(D.hasSuperClass(C, true));
        Assertions.assertTrue(D.hasSuperClass(D, true));
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
    public void testHasSuperClass1b(TestSpec spec) {
        //     D
        //    | \
        // B  |  C
        //  \ | /
        //    A
        OntModel m = TestModelFactory.createClassesDBCA(OntModelFactory.createModel(spec.inst));
        OntClass A = m.createOntClass(NS + "A");
        OntClass B = m.createOntClass(NS + "B");
        OntClass C = m.createOntClass(NS + "C");
        OntClass D = m.createOntClass(NS + "D");

        Assertions.assertTrue(A.hasSuperClass(A, false));
        Assertions.assertTrue(A.hasSuperClass(B, false));
        Assertions.assertTrue(A.hasSuperClass(C, false));
        Assertions.assertTrue(A.hasSuperClass(D, false));
        Assertions.assertFalse(B.hasSuperClass(A, false));
        Assertions.assertTrue(B.hasSuperClass(B, false));
        Assertions.assertFalse(B.hasSuperClass(C, false));
        Assertions.assertFalse(B.hasSuperClass(D, false));
        Assertions.assertFalse(C.hasSuperClass(A, false));
        Assertions.assertFalse(C.hasSuperClass(B, false));
        Assertions.assertTrue(C.hasSuperClass(C, false));
        Assertions.assertTrue(C.hasSuperClass(D, false));
        Assertions.assertFalse(D.hasSuperClass(A, false));
        Assertions.assertFalse(D.hasSuperClass(B, false));
        Assertions.assertFalse(D.hasSuperClass(C, false));
        Assertions.assertTrue(D.hasSuperClass(D, false));

        Assertions.assertTrue(A.hasSuperClass(A, true));
        Assertions.assertTrue(A.hasSuperClass(B, true));
        Assertions.assertTrue(A.hasSuperClass(C, true));
        Assertions.assertFalse(A.hasSuperClass(D, true));
        Assertions.assertFalse(B.hasSuperClass(A, true));
        Assertions.assertTrue(B.hasSuperClass(B, true));
        Assertions.assertFalse(B.hasSuperClass(C, true));
        Assertions.assertFalse(B.hasSuperClass(D, true));
        Assertions.assertFalse(C.hasSuperClass(A, true));
        Assertions.assertFalse(C.hasSuperClass(B, true));
        Assertions.assertTrue(C.hasSuperClass(C, true));
        Assertions.assertTrue(C.hasSuperClass(D, true));
        Assertions.assertFalse(D.hasSuperClass(A, true));
        Assertions.assertFalse(D.hasSuperClass(B, true));
        Assertions.assertFalse(D.hasSuperClass(C, true));
        Assertions.assertTrue(D.hasSuperClass(D, true));
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
    public void testHasSuperClass1c(TestSpec spec) {
        //     D
        //    | \
        // B  |  C
        //  \ | /
        //    A
        OntModel m = TestModelFactory.createClassesDBCA(OntModelFactory.createModel(spec.inst));
        OntClass A = m.createOntClass(NS + "A");
        OntClass B = m.createOntClass(NS + "B");
        OntClass C = m.createOntClass(NS + "C");
        OntClass D = m.createOntClass(NS + "D");

        Assertions.assertTrue(A.hasSuperClass(A, false));
        Assertions.assertTrue(A.hasSuperClass(B, false));
        Assertions.assertTrue(A.hasSuperClass(C, false));
        Assertions.assertTrue(A.hasSuperClass(D, false));
        Assertions.assertFalse(B.hasSuperClass(A, false));
        Assertions.assertTrue(B.hasSuperClass(B, false));
        Assertions.assertFalse(B.hasSuperClass(C, false));
        Assertions.assertFalse(B.hasSuperClass(D, false));
        Assertions.assertFalse(C.hasSuperClass(A, false));
        Assertions.assertFalse(C.hasSuperClass(B, false));
        Assertions.assertTrue(C.hasSuperClass(C, false));
        Assertions.assertTrue(C.hasSuperClass(D, false));
        Assertions.assertFalse(D.hasSuperClass(A, false));
        Assertions.assertFalse(D.hasSuperClass(B, false));
        Assertions.assertFalse(D.hasSuperClass(C, false));
        Assertions.assertTrue(D.hasSuperClass(D, false));

        Assertions.assertTrue(A.hasSuperClass(A, true));
        Assertions.assertTrue(A.hasSuperClass(B, true));
        Assertions.assertTrue(A.hasSuperClass(C, true));
        Assertions.assertFalse(A.hasSuperClass(D, true));
        Assertions.assertFalse(B.hasSuperClass(A, true));
        Assertions.assertTrue(B.hasSuperClass(B, true));
        Assertions.assertFalse(B.hasSuperClass(C, true));
        Assertions.assertFalse(B.hasSuperClass(D, true));
        Assertions.assertFalse(C.hasSuperClass(A, true));
        Assertions.assertFalse(C.hasSuperClass(B, true));
        Assertions.assertTrue(C.hasSuperClass(C, true));
        Assertions.assertTrue(C.hasSuperClass(D, true));
        Assertions.assertFalse(D.hasSuperClass(A, true));
        Assertions.assertFalse(D.hasSuperClass(B, true));
        Assertions.assertFalse(D.hasSuperClass(C, true));
        Assertions.assertTrue(D.hasSuperClass(D, true));
    }
}
