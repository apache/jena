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
import org.apache.jena.vocabulary.OWL2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

public class OntClassHierarchyRootTest {

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM",
            "OWL2_MEM_RDFS_INF",
            "OWL2_MEM_TRANS_INF",
            "OWL1_MEM",
            "OWL1_MEM_RDFS_INF",
            "OWL1_MEM_TRANS_INF",
    })
    public void testIsHierarchyRoot1(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst);
        Assertions.assertTrue(m.getOWLThing().isHierarchyRoot());
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
    })
    public void testIsHierarchyRoot2(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst);
        Assertions.assertFalse(m.getOWLNothing().isHierarchyRoot());
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
            "RDFS_MEM_RDFS_INF",
            "RDFS_MEM_TRANS_INF",
    })
    public void testIsHierarchyRoot4(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst);
        OntClass a = m.createOntClass(TestModelFactory.NS + "A");
        OntClass b = m.createOntClass(TestModelFactory.NS + "B");
        a.addSubClass(b);
        Assertions.assertTrue(a.isHierarchyRoot());
        Assertions.assertFalse(b.isHierarchyRoot());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_MEM",
            "OWL2_MEM_TRANS_INF",
            "OWL1_MEM",
            "OWL1_MEM_TRANS_INF",
    })
    public void testIsHierarchyRoot5(TestSpec spec) {
        // D  THING    G
        // |    |    / .
        // C    F   K  .
        // |    |   |  .
        // B    E   H  .
        // |         \ .
        // A           G
        OntModel m = TestModelFactory.createClassesDGCFKBEHAG(OntModelFactory.createModel(spec.inst));
        OntClass Thing = OWL2.Thing.inModel(m).as(OntClass.class);
        OntClass Nothing = OWL2.Nothing.inModel(m).as(OntClass.class);
        m.getOntClass(TestModelFactory.NS + "F").addSuperClass(Thing);

        Assertions.assertFalse(m.getOntClass(TestModelFactory.NS + "A").isHierarchyRoot());
        Assertions.assertFalse(m.getOntClass(TestModelFactory.NS + "B").isHierarchyRoot());
        Assertions.assertFalse(m.getOntClass(TestModelFactory.NS + "C").isHierarchyRoot());
        Assertions.assertTrue(m.getOntClass(TestModelFactory.NS + "D").isHierarchyRoot());
        Assertions.assertFalse(m.getOntClass(TestModelFactory.NS + "E").isHierarchyRoot());
        Assertions.assertTrue(m.getOntClass(TestModelFactory.NS + "F").isHierarchyRoot());
        Assertions.assertFalse(m.getOntClass(TestModelFactory.NS + "G").isHierarchyRoot());
        Assertions.assertFalse(m.getOntClass(TestModelFactory.NS + "H").isHierarchyRoot());
        Assertions.assertFalse(m.getOntClass(TestModelFactory.NS + "K").isHierarchyRoot());
        Assertions.assertTrue(Thing.isHierarchyRoot());
        Assertions.assertFalse(Nothing.isHierarchyRoot());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM_RDFS_INF",
            "OWL1_MEM_RDFS_INF",
    })
    public void testIsHierarchyRoot6(TestSpec spec) {
        // D  THING    G
        // |    |    / .
        // C    F   K  .
        // |    |   |  .
        // B    E   H  .
        // |         \ .
        // A           G
        OntModel m = TestModelFactory.createClassesDGCFKBEHAG(OntModelFactory.createModel(spec.inst));
        OntClass Thing = OWL2.Thing.inModel(m).as(OntClass.class);
        OntClass Nothing = OWL2.Nothing.inModel(m).as(OntClass.class);
        m.getOntClass(TestModelFactory.NS + "F").addSuperClass(Thing);

        Assertions.assertFalse(m.getOntClass(TestModelFactory.NS + "A").isHierarchyRoot());
        Assertions.assertFalse(m.getOntClass(TestModelFactory.NS + "B").isHierarchyRoot());
        Assertions.assertFalse(m.getOntClass(TestModelFactory.NS + "C").isHierarchyRoot());
        Assertions.assertTrue(m.getOntClass(TestModelFactory.NS + "D").isHierarchyRoot());
        Assertions.assertFalse(m.getOntClass(TestModelFactory.NS + "E").isHierarchyRoot());
        Assertions.assertTrue(m.getOntClass(TestModelFactory.NS + "F").isHierarchyRoot());
        Assertions.assertTrue(m.getOntClass(TestModelFactory.NS + "G").isHierarchyRoot());
        Assertions.assertTrue(m.getOntClass(TestModelFactory.NS + "H").isHierarchyRoot());
        Assertions.assertTrue(m.getOntClass(TestModelFactory.NS + "K").isHierarchyRoot());
        Assertions.assertTrue(Thing.isHierarchyRoot());
        Assertions.assertFalse(Nothing.isHierarchyRoot());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_MEM",
            "OWL2_MEM_TRANS_INF",
            "OWL1_MEM",
            "OWL1_MEM_TRANS_INF",
            "RDFS_MEM",
            "RDFS_MEM_TRANS_INF",
    })
    public void testIsHierarchyRoot7(TestSpec spec) {
        // D        G
        // |      / .
        // C  F  K  .
        // |  |  |  .
        // B  E  H  .
        // |      \ .
        // A        G
        OntModel m = TestModelFactory.createClassesDGCFKBEHAG(TestModelFactory.createClassesDGCFKBEHAG(OntModelFactory.createModel(spec.inst)));

        Assertions.assertFalse(m.getOntClass(TestModelFactory.NS + "A").isHierarchyRoot());
        Assertions.assertFalse(m.getOntClass(TestModelFactory.NS + "B").isHierarchyRoot());
        Assertions.assertFalse(m.getOntClass(TestModelFactory.NS + "C").isHierarchyRoot());
        Assertions.assertTrue(m.getOntClass(TestModelFactory.NS + "D").isHierarchyRoot());
        Assertions.assertFalse(m.getOntClass(TestModelFactory.NS + "E").isHierarchyRoot());
        Assertions.assertTrue(m.getOntClass(TestModelFactory.NS + "F").isHierarchyRoot());
        Assertions.assertFalse(m.getOntClass(TestModelFactory.NS + "G").isHierarchyRoot());
        Assertions.assertFalse(m.getOntClass(TestModelFactory.NS + "H").isHierarchyRoot());
        Assertions.assertFalse(m.getOntClass(TestModelFactory.NS + "K").isHierarchyRoot());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_MEM",
            "OWL1_MEM",
            "RDFS_MEM",
    })
    public void testIsHierarchyRoot8(TestSpec spec) {
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

        boolean isHierarchyRootA = m.getOntClass(TestModelFactory.NS + "A").isHierarchyRoot();
        boolean isHierarchyRootB = m.getOntClass(TestModelFactory.NS + "B").isHierarchyRoot();
        boolean isHierarchyRootC = m.getOntClass(TestModelFactory.NS + "C").isHierarchyRoot();
        boolean isHierarchyRootD = m.getOntClass(TestModelFactory.NS + "D").isHierarchyRoot();
        boolean isHierarchyRootE = m.getOntClass(TestModelFactory.NS + "E").isHierarchyRoot();
        boolean isHierarchyRootF = m.getOntClass(TestModelFactory.NS + "F").isHierarchyRoot();
        boolean isHierarchyRootG = m.getOntClass(TestModelFactory.NS + "G").isHierarchyRoot();
        boolean isHierarchyRootH = m.getOntClass(TestModelFactory.NS + "H").isHierarchyRoot();
        boolean isHierarchyRootK = m.getOntClass(TestModelFactory.NS + "K").isHierarchyRoot();
        boolean isHierarchyRootL = m.getOntClass(TestModelFactory.NS + "L").isHierarchyRoot();
        boolean isHierarchyRootM = m.getOntClass(TestModelFactory.NS + "M").isHierarchyRoot();

        Assertions.assertTrue(isHierarchyRootA);
        Assertions.assertFalse(isHierarchyRootB);
        Assertions.assertFalse(isHierarchyRootC);
        Assertions.assertFalse(isHierarchyRootD);
        Assertions.assertFalse(isHierarchyRootE);
        Assertions.assertFalse(isHierarchyRootF);
        Assertions.assertFalse(isHierarchyRootG);
        Assertions.assertFalse(isHierarchyRootH);
        Assertions.assertTrue(isHierarchyRootK);
        Assertions.assertFalse(isHierarchyRootL);
        Assertions.assertFalse(isHierarchyRootM);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM_RDFS_INF",
            "OWL2_MEM_TRANS_INF",
            "OWL1_MEM_RDFS_INF",
            "OWL1_MEM_TRANS_INF",
            "RDFS_MEM_RDFS_INF",
            "RDFS_MEM_TRANS_INF",
    })
    public void testIsHierarchyRoot9(TestSpec spec) {
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

        boolean isHierarchyRootA = m.getOntClass(TestModelFactory.NS + "A").isHierarchyRoot();
        boolean isHierarchyRootB = m.getOntClass(TestModelFactory.NS + "B").isHierarchyRoot();
        boolean isHierarchyRootC = m.getOntClass(TestModelFactory.NS + "C").isHierarchyRoot();
        boolean isHierarchyRootD = m.getOntClass(TestModelFactory.NS + "D").isHierarchyRoot();
        boolean isHierarchyRootE = m.getOntClass(TestModelFactory.NS + "E").isHierarchyRoot();
        boolean isHierarchyRootF = m.getOntClass(TestModelFactory.NS + "F").isHierarchyRoot();
        boolean isHierarchyRootG = m.getOntClass(TestModelFactory.NS + "G").isHierarchyRoot();
        boolean isHierarchyRootH = m.getOntClass(TestModelFactory.NS + "H").isHierarchyRoot();
        boolean isHierarchyRootK = m.getOntClass(TestModelFactory.NS + "K").isHierarchyRoot();
        boolean isHierarchyRootL = m.getOntClass(TestModelFactory.NS + "L").isHierarchyRoot();
        boolean isHierarchyRootM = m.getOntClass(TestModelFactory.NS + "M").isHierarchyRoot();

        Assertions.assertTrue(isHierarchyRootA);
        Assertions.assertFalse(isHierarchyRootB);
        Assertions.assertFalse(isHierarchyRootC);
        Assertions.assertFalse(isHierarchyRootD);
        Assertions.assertFalse(isHierarchyRootE);
        Assertions.assertFalse(isHierarchyRootF);
        Assertions.assertFalse(isHierarchyRootG);
        Assertions.assertFalse(isHierarchyRootH);
        Assertions.assertFalse(isHierarchyRootK);
        Assertions.assertFalse(isHierarchyRootL);
        Assertions.assertFalse(isHierarchyRootM);
    }

}
