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
import org.apache.jena.ontapi.model.OntIndividual;
import org.apache.jena.ontapi.model.OntModel;
import org.apache.jena.ontapi.testutils.MiscUtils;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDFS;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.jena.ontapi.TestModelFactory.NS;
import static org.apache.jena.ontapi.TestModelFactory.createClassesABCDAEB;
import static org.apache.jena.ontapi.TestModelFactory.createClassesABCDEF;
import static org.apache.jena.ontapi.TestModelFactory.createClassesAGBCFDE;
import static org.apache.jena.ontapi.TestModelFactory.createClassesiAEDcCABiAE;

public class OntIndividualClassesTest {

    @ParameterizedTest
    @EnumSource
    public void testGetOntClass0(TestSpec spec) {
        //      A
        //     / \
        //    B   C
        //   / \ / \
        //  D   E   F

        OntModel m = createClassesABCDEF(OntModelFactory.createModel(spec.inst));
        OntClass A = m.getOntClass(NS + "A");
        OntIndividual iA = A.createIndividual("iA");
        Assertions.assertEquals(A, iA.ontClass().orElseThrow());
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
            "OWL1_LITE_MEM",
            "OWL1_LITE_MEM_RDFS_INF",
            "OWL1_LITE_MEM_TRANS_INF",
            "RDFS_MEM",
            "RDFS_MEM_TRANS_INF",
    })
    public void testListOntClasses1a(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst).setNsPrefixes(OntModelFactory.STANDARD);
        OntClass A = m.createOntClass(NS + "A");
        OntClass B = m.createOntClass(NS + "B");
        A.addSubClass(B);

        OntIndividual x = A.createIndividual();
        x.attachClass(B);

        Assertions.assertEquals(
                List.of(A, B),
                x.classes(false).sorted(Comparator.comparing(Resource::getURI)).collect(Collectors.toList())
        );
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL1_MEM_RULES_INF",
            "OWL1_LITE_MEM_RULES_INF",
    })
    public void testListOntClasses1b(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst).setNsPrefixes(OntModelFactory.STANDARD);
        OntClass A = m.createOntClass(NS + "A");
        OntClass B = m.createOntClass(NS + "B");
        A.addSubClass(B);

        OntIndividual x = A.createIndividual();
        x.attachClass(B);

        List<String> expected = List.of("A", "B", "Resource", "Thing");
        List<String> actual = x.classes(false).map(Resource::getLocalName).sorted().collect(Collectors.toList());
        Assertions.assertEquals(expected, actual);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "RDFS_MEM_RDFS_INF",
    })
    public void testListOntClasses1d(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst).setNsPrefixes(OntModelFactory.STANDARD);
        OntClass A = m.createOntClass(NS + "A");
        OntClass B = m.createOntClass(NS + "B");
        A.addSubClass(B);

        OntIndividual x = A.createIndividual();
        x.attachClass(B);

        Assertions.assertEquals(Set.of(A, B, RDFS.Resource), x.classes(false).collect(Collectors.toSet()));
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_EL_MEM",
            "OWL2_EL_MEM_RDFS_INF",
            "OWL2_EL_MEM_TRANS_INF",
            "OWL2_EL_MEM_RULES_INF",
            "OWL2_QL_MEM",
            "OWL2_QL_MEM_RDFS_INF",
            "OWL2_QL_MEM_TRANS_INF",
            "OWL2_QL_MEM_RULES_INF",
            "OWL2_RL_MEM_RULES_INF",
    }, mode = EnumSource.Mode.EXCLUDE)
    public void testListOntClasses2(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst).setNsPrefixes(OntModelFactory.STANDARD);
        OntClass A = m.createOntClass(NS + "A");
        OntClass B = m.createOntClass(NS + "B");
        A.addSubClass(B);

        OntIndividual x = A.createIndividual();
        x.attachClass(B);

        Assertions.assertEquals(List.of(B), x.classes(true).collect(Collectors.toList()));
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM_RDFS_INF",
            "OWL1_MEM_RDFS_INF",
            "OWL1_LITE_MEM_RDFS_INF",
    })
    public void testListOntClasses3a(TestSpec spec) {
        //      A   G
        //     / \   \
        //    B   C = F
        //   / \ /
        //  D   E
        OntModel m = createClassesAGBCFDE(OntModelFactory.createModel(spec.inst));

        OntIndividual iA = m.getOntClass(NS + "A").createIndividual("iA");
        OntIndividual iB = m.getOntClass(NS + "B").createIndividual("iB");
        OntIndividual iC = m.getOntClass(NS + "C").createIndividual("iC");
        OntIndividual iD = m.getOntClass(NS + "D").createIndividual("iD");
        OntIndividual iE = m.getOntClass(NS + "E").createIndividual("iE");
        OntIndividual iF = m.getOntClass(NS + "F").createIndividual("iF");
        OntIndividual iG = m.getOntClass(NS + "G").createIndividual("iG");

        m.classes().toList().forEach(x -> x.createIndividual(NS + "i" + x.getLocalName()));

        Set<String> directA = iA.classes(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectA = iA.classes(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directB = iB.classes(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectB = iB.classes(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directC = iC.classes(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectC = iC.classes(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directD = iD.classes(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectD = iD.classes(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directE = iE.classes(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectE = iE.classes(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directF = iF.classes(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectF = iF.classes(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directG = iG.classes(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectG = iG.classes(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Assertions.assertEquals(Set.of("A"), directA);
        Assertions.assertEquals(Set.of("B"), directB);
        Assertions.assertEquals(Set.of("C", "F"), directC);
        Assertions.assertEquals(Set.of("D"), directD);
        Assertions.assertEquals(Set.of("E"), directE);
        Assertions.assertEquals(Set.of("C", "F"), directF);
        Assertions.assertEquals(Set.of("G"), directG);

        Assertions.assertEquals(Set.of("A"), indirectA);
        Assertions.assertEquals(Set.of("A", "B"), indirectB);
        Assertions.assertEquals(Set.of("A", "C", "F", "G"), indirectC);
        Assertions.assertEquals(Set.of("A", "B", "D"), indirectD);
        Assertions.assertEquals(Set.of("A", "B", "C", "E", "F", "G"), indirectE);
        Assertions.assertEquals(Set.of("A", "C", "F", "G"), indirectF);
        Assertions.assertEquals(Set.of("G"), indirectG);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_MEM",
            "OWL2_MEM_TRANS_INF",
            "OWL1_MEM",
            "OWL1_MEM_TRANS_INF",
            "OWL1_LITE_MEM",
            "OWL1_LITE_MEM_TRANS_INF",
            "RDFS_MEM",
            "RDFS_MEM_TRANS_INF",
    })
    public void testListOntClasses3b(TestSpec spec) {
        //      A   G
        //     / \   \
        //    B   C = F
        //   / \ /
        //  D   E
        OntModel m = createClassesAGBCFDE(OntModelFactory.createModel(spec.inst));

        OntIndividual iA = m.getOntClass(NS + "A").createIndividual("iA");
        OntIndividual iB = m.getOntClass(NS + "B").createIndividual("iB");
        OntIndividual iC = m.getOntClass(NS + "C").createIndividual("iC");
        OntIndividual iD = m.getOntClass(NS + "D").createIndividual("iD");
        OntIndividual iE = m.getOntClass(NS + "E").createIndividual("iE");
        OntIndividual iF = m.getOntClass(NS + "F").createIndividual("iF");
        OntIndividual iG = m.getOntClass(NS + "G").createIndividual("iG");

        Set<String> directA = iA.classes(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectA = iA.classes(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directB = iB.classes(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectB = iB.classes(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directC = iC.classes(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectC = iC.classes(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directD = iD.classes(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectD = iD.classes(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directE = iE.classes(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectE = iE.classes(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directF = iF.classes(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectF = iF.classes(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directG = iG.classes(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectG = iG.classes(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Assertions.assertEquals(Set.of("A"), directA);
        Assertions.assertEquals(Set.of("B"), directB);
        Assertions.assertEquals(Set.of("C"), directC);
        Assertions.assertEquals(Set.of("D"), directD);
        Assertions.assertEquals(Set.of("E"), directE);
        Assertions.assertEquals(Set.of("F"), directF);
        Assertions.assertEquals(Set.of("G"), directG);

        Assertions.assertEquals(Set.of("A"), indirectA);
        Assertions.assertEquals(Set.of("B"), indirectB);
        Assertions.assertEquals(Set.of("C"), indirectC);
        Assertions.assertEquals(Set.of("D"), indirectD);
        Assertions.assertEquals(Set.of("E"), indirectE);
        Assertions.assertEquals(Set.of("F"), indirectF);
        Assertions.assertEquals(Set.of("G"), indirectG);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL1_MEM_RULES_INF",
            "OWL1_LITE_MEM_RULES_INF",
            "RDFS_MEM_RDFS_INF",
    })
    public void testListOntClasses3c(TestSpec spec) {
        //      A   G
        //     / \   \
        //    B   C = F
        //   / \ /
        //  D   E
        OntModel m = createClassesAGBCFDE(OntModelFactory.createModel(spec.inst));

        OntIndividual iA = m.getOntClass(NS + "A").createIndividual("iA");
        OntIndividual iB = m.getOntClass(NS + "B").createIndividual("iB");
        OntIndividual iC = m.getOntClass(NS + "C").createIndividual("iC");
        OntIndividual iD = m.getOntClass(NS + "D").createIndividual("iD");
        OntIndividual iE = m.getOntClass(NS + "E").createIndividual("iE");
        OntIndividual iF = m.getOntClass(NS + "F").createIndividual("iF");
        OntIndividual iG = m.getOntClass(NS + "G").createIndividual("iG");

        Set<String> directA = iA.classes(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectA = iA.classes(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directB = iB.classes(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectB = iB.classes(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directC = iC.classes(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectC = iC.classes(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directD = iD.classes(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectD = iD.classes(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directE = iE.classes(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectE = iE.classes(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directF = iF.classes(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectF = iF.classes(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directG = iG.classes(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectG = iG.classes(false).map(Resource::getLocalName).collect(Collectors.toSet());

        String R = "Resource";
        String T = spec.equals(TestSpec.RDFS_MEM_RDFS_INF) ? null : "Thing";

        Assertions.assertEquals(Set.of("A"), directA);
        Assertions.assertEquals(Set.of("B"), directB);
        Assertions.assertEquals(Set.of("C", "F"), directC);
        Assertions.assertEquals(Set.of("D"), directD);
        Assertions.assertEquals(Set.of("E"), directE);
        Assertions.assertEquals(Set.of("C", "F"), directF);
        Assertions.assertEquals(Set.of("G"), directG);

        Assertions.assertEquals(Stream.of("A", R, T).filter(Objects::nonNull).collect(Collectors.toSet()), indirectA);
        Assertions.assertEquals(Stream.of("A", "B", R, T).filter(Objects::nonNull).collect(Collectors.toSet()), indirectB);
        Assertions.assertEquals(Stream.of("A", "C", "F", "G", R, T).filter(Objects::nonNull).collect(Collectors.toSet()), indirectC);
        Assertions.assertEquals(Stream.of("A", "B", "D", R, T).filter(Objects::nonNull).collect(Collectors.toSet()), indirectD);
        Assertions.assertEquals(Stream.of("A", "B", "C", "E", "F", "G", R, T).filter(Objects::nonNull).collect(Collectors.toSet()), indirectE);
        Assertions.assertEquals(Stream.of("A", "C", "F", "G", R, T).filter(Objects::nonNull).collect(Collectors.toSet()), indirectF);
        Assertions.assertEquals(Stream.of("G", R, T).filter(Objects::nonNull).collect(Collectors.toSet()), indirectG);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM_RDFS_INF",
            "OWL1_MEM_RDFS_INF",
            "OWL1_LITE_MEM_RDFS_INF",
    })
    public void testListOntClasses4a(TestSpec spec) {
        //      A   G
        //     / \   \
        //    B   C = F
        //   / \ /
        //  D   E
        OntModel m = createClassesAGBCFDE(OntModelFactory.createModel(spec.inst));
        OntClass A = m.getOntClass(NS + "A");
        OntClass B = m.getOntClass(NS + "B");
        OntClass C = m.getOntClass(NS + "C");
        OntClass D = m.getOntClass(NS + "D");
        OntClass E = m.getOntClass(NS + "E");
        OntClass G = m.getOntClass(NS + "G");

        OntIndividual iAG = A.createIndividual("iA").attachClass(G);
        OntIndividual iBDC = B.createIndividual("iB").attachClass(D).attachClass(C);
        OntIndividual iDE = D.createIndividual("iD").attachClass(E);

        Set<String> directAG = iAG.classes(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectAG = iAG.classes(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directBDC = iBDC.classes(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectBDC = iBDC.classes(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directDE = iDE.classes(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectDE = iDE.classes(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Assertions.assertEquals(Set.of("A", "G"), directAG);
        Assertions.assertEquals(Set.of("C", "D", "F"), directBDC);
        Assertions.assertEquals(Set.of("D", "E"), directDE);

        Assertions.assertEquals(Set.of("A", "G"), indirectAG);
        Assertions.assertEquals(Set.of("A", "B", "C", "D", "F", "G"), indirectBDC);
        Assertions.assertEquals(Set.of("A", "B", "C", "D", "E", "F", "G"), indirectDE);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_MEM",
            "OWL2_MEM_TRANS_INF",
            "OWL1_MEM",
            "OWL1_MEM_TRANS_INF",
            "OWL1_LITE_MEM",
            "OWL1_LITE_MEM_TRANS_INF",
            "RDFS_MEM",
            "RDFS_MEM_TRANS_INF",
    })
    public void testListOntClasses4b(TestSpec spec) {
        //      A   G
        //     / \   \
        //    B   C = F
        //   / \ /
        //  D   E
        OntModel m = createClassesAGBCFDE(OntModelFactory.createModel(spec.inst));
        OntClass A = m.getOntClass(NS + "A");
        OntClass B = m.getOntClass(NS + "B");
        OntClass C = m.getOntClass(NS + "C");
        OntClass D = m.getOntClass(NS + "D");
        OntClass E = m.getOntClass(NS + "E");
        OntClass G = m.getOntClass(NS + "G");

        OntIndividual iAG = A.createIndividual("iA").attachClass(G);
        OntIndividual iBDC = B.createIndividual("iB").attachClass(D).attachClass(C);
        OntIndividual iDE = D.createIndividual("iD").attachClass(E);

        Set<String> directAG = iAG.classes(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectAG = iAG.classes(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directBDC = iBDC.classes(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectBDC = iBDC.classes(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directDE = iDE.classes(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectDE = iDE.classes(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Assertions.assertEquals(Set.of("A", "G"), directAG);
        Assertions.assertEquals(Set.of("C", "D"), directBDC);
        Assertions.assertEquals(Set.of("D", "E"), directDE);

        Assertions.assertEquals(Set.of("A", "G"), indirectAG);
        Assertions.assertEquals(Set.of("B", "C", "D"), indirectBDC);
        Assertions.assertEquals(Set.of("D", "E"), indirectDE);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL1_MEM_RULES_INF",
            "OWL1_LITE_MEM_RULES_INF",
            "RDFS_MEM_RDFS_INF",
    })
    public void testListOntClasses4c(TestSpec spec) {
        //      A   G
        //     / \   \
        //    B   C = F
        //   / \ /
        //  D   E
        OntModel m = createClassesAGBCFDE(OntModelFactory.createModel(spec.inst));
        OntClass A = m.getOntClass(NS + "A");
        OntClass B = m.getOntClass(NS + "B");
        OntClass C = m.getOntClass(NS + "C");
        OntClass D = m.getOntClass(NS + "D");
        OntClass E = m.getOntClass(NS + "E");
        OntClass G = m.getOntClass(NS + "G");

        OntIndividual iAG = A.createIndividual("iA").attachClass(G);
        OntIndividual iBDC = B.createIndividual("iB").attachClass(D).attachClass(C);
        OntIndividual iDE = D.createIndividual("iD").attachClass(E);

        Set<String> directAG = iAG.classes(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectAG = iAG.classes(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directBDC = iBDC.classes(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectBDC = iBDC.classes(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directDE = iDE.classes(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectDE = iDE.classes(false).map(Resource::getLocalName).collect(Collectors.toSet());

        String R = "Resource";
        String T = spec.equals(TestSpec.RDFS_MEM_RDFS_INF) ? null : "Thing";

        Assertions.assertEquals(Set.of("A", "G"), directAG);
        Assertions.assertEquals(Set.of("C", "D", "F"), directBDC);
        Assertions.assertEquals(Set.of("D", "E"), directDE);

        Assertions.assertEquals(Stream.of("A", "G", T, R).filter(Objects::nonNull).collect(Collectors.toSet()), indirectAG);
        Assertions.assertEquals(Stream.of("A", "B", "C", "D", "F", "G", T, R).filter(Objects::nonNull).collect(Collectors.toSet()), indirectBDC);
        Assertions.assertEquals(Stream.of("A", "B", "C", "D", "E", "F", "G", T, R).filter(Objects::nonNull).collect(Collectors.toSet()), indirectDE);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM_RDFS_INF",
            "OWL1_MEM_RDFS_INF",
            "OWL1_LITE_MEM_RDFS_INF",
    })
    public void textListOntClasses5a(TestSpec spec) {
        //  A   B
        //  .\ /.
        //  . C .
        //  . | .
        //  . D .
        //  ./  .
        //  A   .
        //   \  .  E
        //    \ . /
        //      B
        OntModel m = createClassesABCDAEB(OntModelFactory.createModel(spec.inst));

        OntClass A = m.getOntClass(NS + "A");
        OntClass B = m.getOntClass(NS + "B");
        OntClass D = m.getOntClass(NS + "D");

        OntIndividual iAD = A.createIndividual("iAD").attachClass(D);
        OntIndividual iDB = D.createIndividual().attachClass(B);

        Set<String> directDB = iDB.classes(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectDB = iDB.classes(false).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> directAD = iAD.classes(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectAD = iAD.classes(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Assertions.assertEquals(Set.of("A", "B", "C", "D"), directDB);
        Assertions.assertEquals(Set.of("A", "B", "C", "D", "E"), indirectDB);
        Assertions.assertEquals(Set.of("A", "B", "C", "D"), directAD);
        Assertions.assertEquals(Set.of("A", "B", "C", "D", "E"), indirectAD);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_MEM",
            "OWL1_MEM",
            "OWL1_LITE_MEM",
            "RDFS_MEM",
    })
    public void textListOntClasses5b(TestSpec spec) {
        //  A   B
        //  .\ /.
        //  . C .
        //  . | .
        //  . D .
        //  ./  .
        //  A   .
        //   \  .  E
        //    \ . /
        //      B
        OntModel m = createClassesABCDAEB(OntModelFactory.createModel(spec.inst));

        OntClass A = m.getOntClass(NS + "A");
        OntClass B = m.getOntClass(NS + "B");
        OntClass D = m.getOntClass(NS + "D");

        OntIndividual iAD = A.createIndividual("iAD").attachClass(D);
        OntIndividual iDB = D.createIndividual().attachClass(B);

        Set<String> directDB = iDB.classes(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectDB = iDB.classes(false).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> directAD = iAD.classes(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectAD = iAD.classes(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Assertions.assertEquals(Set.of("B", "D"), directDB);
        Assertions.assertEquals(Set.of("B", "D"), indirectDB);
        Assertions.assertEquals(Set.of("A"), directAD);
        Assertions.assertEquals(Set.of("A", "D"), indirectAD);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL1_MEM_RULES_INF",
            "OWL1_LITE_MEM_RULES_INF",
            "RDFS_MEM_RDFS_INF",
    })
    public void textListOntClasses5c(TestSpec spec) {
        //  A   B
        //  .\ /.
        //  . C .
        //  . | .
        //  . D .
        //  ./  .
        //  A   .
        //   \  .  E
        //    \ . /
        //      B
        OntModel m = createClassesABCDAEB(OntModelFactory.createModel(spec.inst));

        OntClass A = m.getOntClass(NS + "A");
        OntClass B = m.getOntClass(NS + "B");
        OntClass D = m.getOntClass(NS + "D");

        OntIndividual iAD = A.createIndividual("iAD").attachClass(D);
        OntIndividual iDB = D.createIndividual().attachClass(B);

        Set<String> directDB = iDB.classes(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectDB = iDB.classes(false).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> directAD = iAD.classes(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectAD = iAD.classes(false).map(Resource::getLocalName).collect(Collectors.toSet());

        String R = null;
        String T = null;
        if (TestSpec.OWL1_MEM_RULES_INF.equals(spec) ||
                TestSpec.OWL1_LITE_MEM_RULES_INF.equals(spec)) {
            R = "Resource";
            T = "Thing";
        } else if (TestSpec.RDFS_MEM_RDFS_INF.equals(spec)) {
            R = "Resource";
        }

        Assertions.assertEquals(Stream.of("A", "B", "C", "D").collect(Collectors.toSet()), directDB);
        Assertions.assertEquals(Stream.of("A", "B", "C", "D", "E", R, T).filter(Objects::nonNull).collect(Collectors.toSet()), indirectDB);
        Assertions.assertEquals(Stream.of("A", "B", "C", "D").collect(Collectors.toSet()), directAD);
        Assertions.assertEquals(Stream.of("A", "B", "C", "D", "E", R, T).filter(Objects::nonNull).collect(Collectors.toSet()), indirectAD);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_MEM_TRANS_INF",
            "OWL1_MEM_TRANS_INF",
            "OWL1_LITE_MEM_TRANS_INF",
            "RDFS_MEM_TRANS_INF",
    })
    public void textListOntClasses5d(TestSpec spec) {
        //  A   B
        //  .\ /.
        //  . C .
        //  . | .
        //  . D .
        //  ./  .
        //  A   .
        //   \  .  E
        //    \ . /
        //      B
        OntModel m = createClassesABCDAEB(OntModelFactory.createModel(spec.inst));

        OntClass A = m.getOntClass(NS + "A");
        OntClass B = m.getOntClass(NS + "B");
        OntClass D = m.getOntClass(NS + "D");

        OntIndividual iAD = A.createIndividual("iAD").attachClass(D);
        OntIndividual iDB = D.createIndividual().attachClass(B);

        Set<String> directDB = iDB.classes(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectDB = iDB.classes(false).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> directAD = iAD.classes(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectAD = iAD.classes(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Assertions.assertEquals(Set.of("B", "D"), directDB);
        Assertions.assertEquals(Set.of("B", "D"), indirectDB);
        Assertions.assertEquals(Set.of("A", "D"), directAD);
        Assertions.assertEquals(Set.of("A", "D"), indirectAD);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_MEM",
            "OWL1_MEM",
    })
    public void testListOntClasses6a(TestSpec spec) {
        //         I_AE
        //         |  .
        //        D   .
        //       /    .
        // C_C  A     .
        //  \  / \    .
        //   B    \   .
        //     \  /   .
        //       I_A_E
        OntModel m = createClassesiAEDcCABiAE(OntModelFactory.createModel(spec.inst));
        OntClass A = m.getResource(NS + "A").as(OntClass.class);
        OntClass B = m.getResource(NS + "B").as(OntClass.class);
        OntClass D = m.getResource(NS + "D").as(OntClass.class);

        OntIndividual iAD = A.createIndividual("iA").attachClass(D);
        OntIndividual iDB = D.createIndividual().attachClass(B);

        Set<String> directDB = iDB.classes(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectDB = iDB.classes(false).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> directAD = iAD.classes(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectAD = iAD.classes(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Assertions.assertEquals(Set.of("B", "D"), directDB);
        Assertions.assertEquals(Set.of("B", "D"), indirectDB);
        Assertions.assertEquals(Set.of("A"), directAD);
        Assertions.assertEquals(Set.of("A", "D"), indirectAD);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM_RDFS_INF",
            "OWL1_MEM_RDFS_INF",
    })
    public void testListOntClasses6b(TestSpec spec) {
        //         I_AE
        //         |  .
        //        D   .
        //       /    .
        // C_C  A     .
        //  \  / \    .
        //   B    \   .
        //     \  /   .
        //       I_A_E
        OntModel m = createClassesiAEDcCABiAE(OntModelFactory.createModel(spec.inst));
        OntClass A = m.getResource(NS + "A").as(OntClass.class);
        OntClass B = m.getResource(NS + "B").as(OntClass.class);
        OntClass D = m.getResource(NS + "D").as(OntClass.class);

        OntIndividual iAD = A.createIndividual("iA").attachClass(D);
        OntIndividual iDB = D.createIndividual().attachClass(B);

        Set<String> directDB = iDB.classes(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectDB = iDB.classes(false).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> directAD = iAD.classes(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectAD = iAD.classes(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Assertions.assertEquals(MiscUtils.hashSetOf(null, "A", "B", "D"), directDB);
        Assertions.assertEquals(MiscUtils.hashSetOf(null, "A", "B", "D"), indirectDB);
        Assertions.assertEquals(MiscUtils.hashSetOf(null, "A", "B", "D"), directAD);
        Assertions.assertEquals(MiscUtils.hashSetOf(null, "A", "B", "D"), indirectAD);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_MEM_TRANS_INF",
            "OWL1_MEM_TRANS_INF",
    })
    public void testListOntClasses6d(TestSpec spec) {
        //         I_AE
        //         |  .
        //        D   .
        //       /    .
        // C_C  A     .
        //  \  / \    .
        //   B    \   .
        //     \  /   .
        //       I_A_E
        OntModel m = createClassesiAEDcCABiAE(OntModelFactory.createModel(spec.inst));
        OntClass A = m.getResource(NS + "A").as(OntClass.class);
        OntClass B = m.getResource(NS + "B").as(OntClass.class);
        OntClass D = m.getResource(NS + "D").as(OntClass.class);

        OntIndividual iAD = A.createIndividual("iA").attachClass(D);
        OntIndividual iDB = D.createIndividual().attachClass(B);

        Set<String> directDB = iDB.classes(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectDB = iDB.classes(false).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> directAD = iAD.classes(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectAD = iAD.classes(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Assertions.assertEquals(MiscUtils.hashSetOf("B", "D"), directDB);
        Assertions.assertEquals(MiscUtils.hashSetOf("B", "D"), indirectDB);
        Assertions.assertEquals(MiscUtils.hashSetOf("A", "D"), directAD);
        Assertions.assertEquals(MiscUtils.hashSetOf("A", "D"), indirectAD);
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
    public void testHasOntClasses1a(TestSpec spec) {
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

        OntClass A = m.getOntClass(NS + "A");
        OntClass B = m.getOntClass(NS + "B");
        OntClass C = m.getOntClass(NS + "C");
        OntClass D = m.getOntClass(NS + "D");
        OntClass E = m.getOntClass(NS + "E");

        OntIndividual iAD = A.createIndividual(NS + "iAD");
        iAD.attachClass(D);
        OntIndividual iDB = D.createIndividual(NS + "iDB");
        iDB.attachClass(B);
        OntIndividual iC = C.createIndividual(NS + "iC");
        OntIndividual iE = E.createIndividual(NS + "iE");

        Assertions.assertTrue(iAD.hasOntClass(A, false));
        Assertions.assertFalse(iDB.hasOntClass(A, false));
        Assertions.assertFalse(iC.hasOntClass(A, false));
        Assertions.assertFalse(iE.hasOntClass(A, false));
        Assertions.assertFalse(iAD.hasOntClass(B, false));
        Assertions.assertTrue(iDB.hasOntClass(B, false));
        Assertions.assertFalse(iC.hasOntClass(B, false));
        Assertions.assertFalse(iE.hasOntClass(B, false));
        Assertions.assertFalse(iAD.hasOntClass(C, false));
        Assertions.assertFalse(iDB.hasOntClass(C, false));
        Assertions.assertTrue(iC.hasOntClass(C, false));
        Assertions.assertFalse(iE.hasOntClass(C, false));
        Assertions.assertTrue(iAD.hasOntClass(D, false));
        Assertions.assertTrue(iDB.hasOntClass(D, false));
        Assertions.assertFalse(iC.hasOntClass(D, false));
        Assertions.assertFalse(iE.hasOntClass(D, false));
        Assertions.assertFalse(iAD.hasOntClass(E, false));
        Assertions.assertFalse(iDB.hasOntClass(E, false));
        Assertions.assertFalse(iC.hasOntClass(E, false));
        Assertions.assertTrue(iE.hasOntClass(E, false));

        Assertions.assertTrue(iAD.hasOntClass(A, true));
        Assertions.assertFalse(iDB.hasOntClass(A, true));
        Assertions.assertFalse(iC.hasOntClass(A, true));
        Assertions.assertFalse(iE.hasOntClass(A, true));
        Assertions.assertFalse(iAD.hasOntClass(B, true));
        Assertions.assertTrue(iDB.hasOntClass(B, true));
        Assertions.assertFalse(iC.hasOntClass(B, true));
        Assertions.assertFalse(iE.hasOntClass(B, true));
        Assertions.assertFalse(iAD.hasOntClass(C, true));
        Assertions.assertFalse(iDB.hasOntClass(C, true));
        Assertions.assertTrue(iC.hasOntClass(C, true));
        Assertions.assertFalse(iE.hasOntClass(C, true));
        Assertions.assertFalse(iAD.hasOntClass(D, true));
        Assertions.assertTrue(iDB.hasOntClass(D, true));
        Assertions.assertFalse(iC.hasOntClass(D, true));
        Assertions.assertFalse(iE.hasOntClass(D, true));
        Assertions.assertFalse(iAD.hasOntClass(E, true));
        Assertions.assertFalse(iDB.hasOntClass(E, true));
        Assertions.assertFalse(iC.hasOntClass(E, true));
        Assertions.assertTrue(iE.hasOntClass(E, true));
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
    public void testHasOntClasses1b(TestSpec spec) {
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

        OntClass A = m.getOntClass(NS + "A");
        OntClass B = m.getOntClass(NS + "B");
        OntClass C = m.getOntClass(NS + "C");
        OntClass D = m.getOntClass(NS + "D");
        OntClass E = m.getOntClass(NS + "E");

        OntIndividual iAD = A.createIndividual(NS + "iAD");
        iAD.attachClass(D);
        OntIndividual iDB = D.createIndividual(NS + "iDB");
        iDB.attachClass(B);
        OntIndividual iC = C.createIndividual(NS + "iC");
        OntIndividual iE = E.createIndividual(NS + "iE");

        Assertions.assertTrue(iAD.hasOntClass(A, false));
        Assertions.assertTrue(iDB.hasOntClass(A, false));
        Assertions.assertTrue(iC.hasOntClass(A, false));
        Assertions.assertFalse(iE.hasOntClass(A, false));
        Assertions.assertTrue(iAD.hasOntClass(B, false));
        Assertions.assertTrue(iDB.hasOntClass(B, false));
        Assertions.assertTrue(iC.hasOntClass(B, false));
        Assertions.assertFalse(iE.hasOntClass(B, false));
        Assertions.assertTrue(iAD.hasOntClass(C, false));
        Assertions.assertTrue(iDB.hasOntClass(C, false));
        Assertions.assertTrue(iC.hasOntClass(C, false));
        Assertions.assertFalse(iE.hasOntClass(C, false));
        Assertions.assertTrue(iAD.hasOntClass(D, false));
        Assertions.assertTrue(iDB.hasOntClass(D, false));
        Assertions.assertTrue(iC.hasOntClass(D, false));
        Assertions.assertFalse(iE.hasOntClass(D, false));
        Assertions.assertTrue(iAD.hasOntClass(E, false));
        Assertions.assertTrue(iDB.hasOntClass(E, false));
        Assertions.assertTrue(iC.hasOntClass(E, false));
        Assertions.assertTrue(iE.hasOntClass(E, false));

        Assertions.assertTrue(iAD.hasOntClass(A, true));
        Assertions.assertTrue(iDB.hasOntClass(A, true));
        Assertions.assertTrue(iC.hasOntClass(A, true));
        Assertions.assertFalse(iE.hasOntClass(A, true));
        Assertions.assertTrue(iAD.hasOntClass(B, true));
        Assertions.assertTrue(iDB.hasOntClass(B, true));
        Assertions.assertTrue(iC.hasOntClass(B, true));
        Assertions.assertFalse(iE.hasOntClass(B, true));
        Assertions.assertTrue(iAD.hasOntClass(C, true));
        Assertions.assertTrue(iDB.hasOntClass(C, true));
        Assertions.assertTrue(iC.hasOntClass(C, true));
        Assertions.assertFalse(iE.hasOntClass(C, true));
        Assertions.assertTrue(iAD.hasOntClass(D, true));
        Assertions.assertTrue(iDB.hasOntClass(D, true));
        Assertions.assertTrue(iC.hasOntClass(D, true));
        Assertions.assertFalse(iE.hasOntClass(D, true));
        Assertions.assertFalse(iAD.hasOntClass(E, true));
        Assertions.assertFalse(iDB.hasOntClass(E, true));
        Assertions.assertFalse(iC.hasOntClass(E, true));
        Assertions.assertTrue(iE.hasOntClass(E, true));
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
    public void testHasOntClasses1c(TestSpec spec) {
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

        OntClass A = m.getOntClass(NS + "A");
        OntClass B = m.getOntClass(NS + "B");
        OntClass C = m.getOntClass(NS + "C");
        OntClass D = m.getOntClass(NS + "D");
        OntClass E = m.getOntClass(NS + "E");

        OntIndividual iAD = A.createIndividual(NS + "iAD");
        iAD.attachClass(D);
        OntIndividual iDB = D.createIndividual(NS + "iDB");
        iDB.attachClass(B);
        OntIndividual iC = C.createIndividual(NS + "iC");
        OntIndividual iE = E.createIndividual(NS + "iE");

        Assertions.assertTrue(iAD.hasOntClass(A, false));
        Assertions.assertFalse(iDB.hasOntClass(A, false));
        Assertions.assertFalse(iC.hasOntClass(A, false));
        Assertions.assertFalse(iE.hasOntClass(A, false));
        Assertions.assertFalse(iAD.hasOntClass(B, false));
        Assertions.assertTrue(iDB.hasOntClass(B, false));
        Assertions.assertFalse(iC.hasOntClass(B, false));
        Assertions.assertFalse(iE.hasOntClass(B, false));
        Assertions.assertFalse(iAD.hasOntClass(C, false));
        Assertions.assertFalse(iDB.hasOntClass(C, false));
        Assertions.assertTrue(iC.hasOntClass(C, false));
        Assertions.assertFalse(iE.hasOntClass(C, false));
        Assertions.assertTrue(iAD.hasOntClass(D, false));
        Assertions.assertTrue(iDB.hasOntClass(D, false));
        Assertions.assertFalse(iC.hasOntClass(D, false));
        Assertions.assertFalse(iE.hasOntClass(D, false));
        Assertions.assertFalse(iAD.hasOntClass(E, false));
        Assertions.assertFalse(iDB.hasOntClass(E, false));
        Assertions.assertFalse(iC.hasOntClass(E, false));
        Assertions.assertTrue(iE.hasOntClass(E, false));

        Assertions.assertTrue(iAD.hasOntClass(A, true));
        Assertions.assertFalse(iDB.hasOntClass(A, true));
        Assertions.assertFalse(iC.hasOntClass(A, true));
        Assertions.assertFalse(iE.hasOntClass(A, true));
        Assertions.assertFalse(iAD.hasOntClass(B, true));
        Assertions.assertTrue(iDB.hasOntClass(B, true));
        Assertions.assertFalse(iC.hasOntClass(B, true));
        Assertions.assertFalse(iE.hasOntClass(B, true));
        Assertions.assertFalse(iAD.hasOntClass(C, true));
        Assertions.assertFalse(iDB.hasOntClass(C, true));
        Assertions.assertTrue(iC.hasOntClass(C, true));
        Assertions.assertFalse(iE.hasOntClass(C, true));
        Assertions.assertTrue(iAD.hasOntClass(D, true));
        Assertions.assertTrue(iDB.hasOntClass(D, true));
        Assertions.assertFalse(iC.hasOntClass(D, true));
        Assertions.assertFalse(iE.hasOntClass(D, true));
        Assertions.assertFalse(iAD.hasOntClass(E, true));
        Assertions.assertFalse(iDB.hasOntClass(E, true));
        Assertions.assertFalse(iC.hasOntClass(E, true));
        Assertions.assertTrue(iE.hasOntClass(E, true));
    }

}
