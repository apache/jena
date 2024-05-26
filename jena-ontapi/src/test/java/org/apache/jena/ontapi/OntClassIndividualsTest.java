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
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDF;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.jena.ontapi.TestModelFactory.NS;
import static org.apache.jena.ontapi.TestModelFactory.createClassesABCDAEB;
import static org.apache.jena.ontapi.TestModelFactory.createClassesABCDEF;
import static org.apache.jena.ontapi.TestModelFactory.createClassesABCDEFGHKLM;
import static org.apache.jena.ontapi.TestModelFactory.createClassesBCA;
import static org.apache.jena.ontapi.TestModelFactory.createClassesDBCA;

public class OntClassIndividualsTest {

    private static Set<String> individuals(OntModel m, String name, boolean direct) {
        return m.getOntClass(NS + name).individuals(direct).map(Resource::getLocalName).collect(Collectors.toSet());
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
    public void testRemoveIndividual(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst)
                .setNsPrefixes(OntModelFactory.STANDARD);
        OntClass c1 = m.createOntClass(":C1");
        OntClass c2 = m.createOntClass(":C2");
        Resource i1 = m.createResource(":I1", c1).addProperty(RDF.type, OWL2.NamedIndividual);
        m.createResource(":I2", c2).addProperty(RDF.type, OWL2.NamedIndividual);
        Assertions.assertEquals(2, m.individuals().count());

        Assertions.assertSame(c1, c1.removeIndividual(i1));
        Assertions.assertEquals(0, c1.individuals().count());
        Assertions.assertEquals(1, m.individuals().count());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM_RDFS_INF",
            "OWL1_MEM_RDFS_INF",
            "RDFS_MEM_RDFS_INF",
    })
    public void testListIndividuals1(TestSpec spec) {
        //      A
        //     / \
        //    B   C
        //   / \ / \
        //  D   E   F

        OntModel m = createClassesABCDEF(OntModelFactory.createModel(spec.inst));
        OntClass a = m.getResource(NS + "A").as(OntClass.class);
        OntClass b = m.getResource(NS + "B").as(OntClass.class);
        OntClass c = m.getResource(NS + "C").as(OntClass.class);
        OntClass d = m.getResource(NS + "D").as(OntClass.class);
        OntClass e = m.getResource(NS + "E").as(OntClass.class);

        OntIndividual ia = a.createIndividual(NS + "iA");
        OntIndividual ib = b.createIndividual(NS + "iB");
        OntIndividual ic = c.createIndividual(NS + "iC");
        OntIndividual id = d.createIndividual(NS + "iD");
        OntIndividual ie = e.createIndividual(NS + "iE");

        Set<Resource> directA = a.individuals(true).collect(Collectors.toSet());
        Set<Resource> indirectA = a.individuals(false).collect(Collectors.toSet());
        Set<Resource> directB = b.individuals(true).collect(Collectors.toSet());
        Set<Resource> indirectB = b.individuals(false).collect(Collectors.toSet());

        Assertions.assertEquals(Set.of(ia, ib, ic, id, ie), indirectA);
        Assertions.assertEquals(Set.of(ib, id, ie), indirectB);

        Assertions.assertEquals(Set.of(ia), directA);
        Assertions.assertEquals(Set.of(ib), directB);
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
    public void testListIndividuals2(TestSpec spec) {
        //      A
        //     / \
        //    B   C
        //   / \ / \
        //  D   E   F

        OntModel m = createClassesABCDEF(OntModelFactory.createModel(spec.inst));
        OntClass a = m.getResource(NS + "A").as(OntClass.class);
        OntClass b = m.getResource(NS + "B").as(OntClass.class);
        OntClass c = m.getResource(NS + "C").as(OntClass.class);
        OntClass d = m.getResource(NS + "D").as(OntClass.class);
        OntClass e = m.getResource(NS + "E").as(OntClass.class);

        OntIndividual ia = a.createIndividual(NS + "iA");
        OntIndividual ib = b.createIndividual(NS + "iB");
        OntIndividual ic = c.createIndividual(NS + "iC");
        OntIndividual id = d.createIndividual(NS + "iD");
        OntIndividual ie = e.createIndividual(NS + "iE");

        Set<Resource> directA = a.individuals(true).collect(Collectors.toSet());
        Set<Resource> indirectA = a.individuals(false).collect(Collectors.toSet());
        Set<Resource> directB = b.individuals(true).collect(Collectors.toSet());
        Set<Resource> indirectB = b.individuals(false).collect(Collectors.toSet());

        Assertions.assertEquals(Set.of(ia), indirectA);
        Assertions.assertEquals(Set.of(ib), indirectB);

        Assertions.assertEquals(Set.of(ia), directA);
        Assertions.assertEquals(Set.of(ib), directB);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM_RDFS_INF",
            "OWL1_MEM_RDFS_INF",
            "RDFS_MEM_RDFS_INF",
    })
    public void testListIndividuals3a(TestSpec spec) {
        //     A
        //   /  / \
        //  /  B   C
        //  | / \ / \
        //  D   E   F
        // / \
        // G  H = K
        //       / \
        //      L   M

        OntModel m = createClassesABCDEFGHKLM(OntModelFactory.createModel(spec.inst));
        Stream.of("A", "B", "C", "D", "E", "F", "G", "H", "K", "L", "M")
                .forEach(s -> m.createResource(NS + "i" + s, m.getResource(NS + s)));

        Set<String> directA = individuals(m, "A", true);
        Set<String> indirectA = individuals(m, "A", false);

        Set<String> directB = individuals(m, "B", true);
        Set<String> indirectB = individuals(m, "B", false);

        Set<String> directC = individuals(m, "C", true);
        Set<String> indirectC = individuals(m, "C", false);

        Set<String> directD = individuals(m, "D", true);
        Set<String> indirectD = individuals(m, "D", false);

        Set<String> directE = individuals(m, "E", true);
        Set<String> indirectE = individuals(m, "E", false);

        Set<String> directF = individuals(m, "F", true);
        Set<String> indirectF = individuals(m, "F", false);

        Set<String> directG = individuals(m, "G", true);
        Set<String> indirectG = individuals(m, "G", false);

        Set<String> directH = individuals(m, "H", true);
        Set<String> indirectH = individuals(m, "H", true);

        Set<String> directK = individuals(m, "K", true);
        Set<String> indirectK = individuals(m, "K", false);

        Set<String> directL = individuals(m, "L", true);
        Set<String> indirectL = individuals(m, "L", false);

        Set<String> directM = individuals(m, "M", true);
        Set<String> indirectM = individuals(m, "M", false);

        Assertions.assertEquals(Set.of("iA"), directA);
        Assertions.assertEquals(Set.of("iB"), directB);
        Assertions.assertEquals(Set.of("iC"), directC);
        Assertions.assertEquals(Set.of("iD"), directD);
        Assertions.assertEquals(Set.of("iE"), directE);
        Assertions.assertEquals(Set.of("iF"), directF);
        Assertions.assertEquals(Set.of("iG"), directG);
        Assertions.assertEquals(Set.of("iK", "iH"), directH);
        Assertions.assertEquals(Set.of("iK", "iH"), directK);
        Assertions.assertEquals(Set.of("iL"), directL);
        Assertions.assertEquals(Set.of("iM"), directM);

        Assertions.assertEquals(Set.of("iA", "iB", "iC", "iD", "iE", "iF", "iG", "iH", "iK", "iL", "iM"), indirectA);
        Assertions.assertEquals(Set.of("iB", "iD", "iE", "iG", "iH", "iK", "iL", "iM"), indirectB);
        Assertions.assertEquals(Set.of("iC", "iE", "iF"), indirectC);
        Assertions.assertEquals(Set.of("iD", "iG", "iH", "iK", "iL", "iM"), indirectD);
        Assertions.assertEquals(Set.of("iE"), indirectE);
        Assertions.assertEquals(Set.of("iF"), indirectF);
        Assertions.assertEquals(Set.of("iG"), indirectG);
        Assertions.assertEquals(Set.of("iK", "iH"), indirectH);
        Assertions.assertEquals(Set.of("iH", "iK", "iL", "iM"), indirectK);
        Assertions.assertEquals(Set.of("iL"), indirectL);
        Assertions.assertEquals(Set.of("iM"), indirectM);
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
    public void testListIndividuals3b(TestSpec spec) {
        //     A
        //   /  / \
        //  /  B   C
        //  | / \ / \
        //  D   E   F
        // / \
        // G  H = K
        //       / \
        //      L   M

        OntModel m = createClassesABCDEFGHKLM(OntModelFactory.createModel(spec.inst));
        Stream.of("A", "B", "C", "D", "E", "F", "G", "H", "K", "L", "M")
                .forEach(s -> m.createResource(NS + "i" + s, m.getResource(NS + s)));

        Stream.of("A", "B", "C", "D", "E", "F", "G", "H", "K", "L", "M")
                .forEach(s -> {
                            Set<String> direct = individuals(m, s, true);
                            Set<String> indirect = individuals(m, s, false);
                            Assertions.assertEquals(Set.of("i" + s), direct);
                            Assertions.assertEquals(Set.of("i" + s), indirect);
                        }
                );
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM_RDFS_INF",
            "OWL1_MEM_RDFS_INF",
            "RDFS_MEM_RDFS_INF",
    })
    public void testListIndividuals4a(TestSpec spec) {
        // B = C
        //  \ |
        //    A

        OntModel m = createClassesBCA(OntModelFactory.createModel(spec.inst));

        m.createResource(NS + "iA", m.getResource(NS + "A"));
        m.createResource(NS + "iB", m.getResource(NS + "B"));
        m.createResource(NS + "iC", m.getResource(NS + "C"));

        Set<String> directA = individuals(m, "A", true);
        Set<String> indirectA = individuals(m, "A", false);

        Set<String> directB = individuals(m, "B", true);
        Set<String> indirectB = individuals(m, "B", false);

        Set<String> directC = individuals(m, "C", true);
        Set<String> indirectC = individuals(m, "C", false);

        Assertions.assertEquals(Set.of("iA"), directA);
        Assertions.assertEquals(Set.of("iB", "iC"), directB);
        Assertions.assertEquals(Set.of("iB", "iC"), directC);
        Assertions.assertEquals(Set.of("iA"), indirectA);
        Assertions.assertEquals(Set.of("iA", "iB", "iC"), indirectB);
        Assertions.assertEquals(Set.of("iA", "iB", "iC"), indirectC);
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
    public void testListIndividuals4b(TestSpec spec) {
        // B = C
        //  \ |
        //    A

        OntModel m = createClassesBCA(OntModelFactory.createModel(spec.inst));

        m.createResource(NS + "iA", m.getResource(NS + "A"));
        m.createResource(NS + "iB", m.getResource(NS + "B"));
        m.createResource(NS + "iC", m.getResource(NS + "C"));

        Set<String> directA = individuals(m, "A", true);
        Set<String> indirectA = individuals(m, "A", false);

        Set<String> directB = individuals(m, "B", true);
        Set<String> indirectB = individuals(m, "B", false);

        Set<String> directC = individuals(m, "C", true);
        Set<String> indirectC = individuals(m, "C", false);

        Assertions.assertEquals(Set.of("iA"), directA);
        Assertions.assertEquals(Set.of("iB"), directB);
        Assertions.assertEquals(Set.of("iC"), directC);
        Assertions.assertEquals(Set.of("iA"), indirectA);
        Assertions.assertEquals(Set.of("iB"), indirectB);
        Assertions.assertEquals(Set.of("iC"), indirectC);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM_RDFS_INF",
            "OWL1_MEM_RDFS_INF",
            "RDFS_MEM_RDFS_INF",
    })
    public void testListIndividuals5a(TestSpec spec) {
        //     D
        //    | \
        // B  |  C
        //  \ | /
        //    A

        OntModel m = createClassesDBCA(OntModelFactory.createModel(spec.inst));

        m.getResource(NS + "A").as(OntClass.class).createIndividual(NS + "iA");
        m.getResource(NS + "B").as(OntClass.class).createIndividual(NS + "iB");
        m.getResource(NS + "C").as(OntClass.class).createIndividual(NS + "iC");
        m.getResource(NS + "D").as(OntClass.class).createIndividual(NS + "iD");

        Set<String> directA = individuals(m, "A", true);
        Set<String> indirectA = individuals(m, "A", false);

        Set<String> directB = individuals(m, "B", true);
        Set<String> indirectB = individuals(m, "B", false);

        Set<String> directC = individuals(m, "C", true);
        Set<String> indirectC = individuals(m, "C", false);

        Set<String> directD = individuals(m, "D", true);
        Set<String> indirectD = individuals(m, "D", false);

        Assertions.assertEquals(Set.of("iA"), directA);
        Assertions.assertEquals(Set.of("iB"), directB);
        Assertions.assertEquals(Set.of("iC"), directC);
        Assertions.assertEquals(Set.of("iD"), directD);
        Assertions.assertEquals(Set.of("iA"), indirectA);
        Assertions.assertEquals(Set.of("iA", "iB"), indirectB);
        Assertions.assertEquals(Set.of("iA", "iC"), indirectC);
        Assertions.assertEquals(Set.of("iA", "iC", "iD"), indirectD);
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
    public void testListIndividuals5b(TestSpec spec) {
        //     D
        //    | \
        // B  |  C
        //  \ | /
        //    A

        OntModel m = createClassesDBCA(OntModelFactory.createModel(spec.inst));

        m.createResource(NS + "iA", m.getResource(NS + "A"));
        m.createResource(NS + "iB", m.getResource(NS + "B"));
        m.createResource(NS + "iC", m.getResource(NS + "C"));
        m.createResource(NS + "iD", m.getResource(NS + "D"));

        Set<String> directA = individuals(m, "A", true);
        Set<String> indirectA = individuals(m, "A", false);

        Set<String> directB = individuals(m, "B", true);
        Set<String> indirectB = individuals(m, "B", false);

        Set<String> directC = individuals(m, "C", true);
        Set<String> indirectC = individuals(m, "C", false);

        Set<String> directD = individuals(m, "D", true);
        Set<String> indirectD = individuals(m, "D", false);

        Assertions.assertEquals(Set.of("iA"), directA);
        Assertions.assertEquals(Set.of("iB"), directB);
        Assertions.assertEquals(Set.of("iC"), directC);
        Assertions.assertEquals(Set.of("iD"), directD);
        Assertions.assertEquals(Set.of("iA"), indirectA);
        Assertions.assertEquals(Set.of("iB"), indirectB);
        Assertions.assertEquals(Set.of("iC"), indirectC);
        Assertions.assertEquals(Set.of("iD"), indirectD);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM_RDFS_INF",
            "OWL1_MEM_RDFS_INF",
            "RDFS_MEM_RDFS_INF",
    })
    public void testListIndividuals6a(TestSpec spec) {
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

        A.createIndividual(NS + "iA");
        B.createIndividual(NS + "iB");
        C.createIndividual(NS + "iC");
        D.createIndividual(NS + "iD");
        E.createIndividual(NS + "iE");

        Set<String> directA = individuals(m, "A", true);
        Set<String> indirectA = individuals(m, "A", false);

        Set<String> directB = individuals(m, "B", true);
        Set<String> indirectB = individuals(m, "B", false);

        Set<String> directC = individuals(m, "C", true);
        Set<String> indirectC = individuals(m, "C", false);

        Set<String> directD = individuals(m, "D", true);
        Set<String> indirectD = individuals(m, "D", false);

        Set<String> directE = individuals(m, "E", true);
        Set<String> indirectE = individuals(m, "E", false);

        Set<String> directF = individuals(m, "F", true);
        Set<String> indirectF = individuals(m, "F", false);

        Assertions.assertEquals(Set.of("iA"), directA);
        Assertions.assertEquals(Set.of("iB"), directB);
        Assertions.assertEquals(Set.of("iC"), directC);
        Assertions.assertEquals(Set.of("iD"), directD);
        Assertions.assertEquals(Set.of("iE"), directE);
        Assertions.assertEquals(Set.of(), directF);
        Assertions.assertEquals(Set.of("iA", "iB", "iC", "iD", "iE"), indirectA);
        Assertions.assertEquals(Set.of("iB", "iD", "iE"), indirectB);
        Assertions.assertEquals(Set.of("iE", "iC"), indirectC);
        Assertions.assertEquals(Set.of("iD"), indirectD);
        Assertions.assertEquals(Set.of("iE"), indirectE);
        Assertions.assertEquals(Set.of(), indirectF);
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
    public void testListIndividuals6b(TestSpec spec) {
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

        A.createIndividual(NS + "iA");
        B.createIndividual(NS + "iB");
        C.createIndividual(NS + "iC");
        D.createIndividual(NS + "iD");
        E.createIndividual(NS + "iE");

        Set<String> directA = individuals(m, "A", true);
        Set<String> indirectA = individuals(m, "A", false);

        Set<String> directB = individuals(m, "B", true);
        Set<String> indirectB = individuals(m, "B", false);

        Set<String> directC = individuals(m, "C", true);
        Set<String> indirectC = individuals(m, "C", false);

        Set<String> directD = individuals(m, "D", true);
        Set<String> indirectD = individuals(m, "D", false);

        Set<String> directE = individuals(m, "E", true);
        Set<String> indirectE = individuals(m, "E", false);

        Set<String> directF = individuals(m, "F", true);
        Set<String> indirectF = individuals(m, "F", false);

        Assertions.assertEquals(Set.of("iA"), directA);
        Assertions.assertEquals(Set.of("iB"), directB);
        Assertions.assertEquals(Set.of("iC"), directC);
        Assertions.assertEquals(Set.of("iD"), directD);
        Assertions.assertEquals(Set.of("iE"), directE);
        Assertions.assertEquals(Set.of(), directF);
        Assertions.assertEquals(Set.of("iA"), indirectA);
        Assertions.assertEquals(Set.of("iB"), indirectB);
        Assertions.assertEquals(Set.of("iC"), indirectC);
        Assertions.assertEquals(Set.of("iD"), indirectD);
        Assertions.assertEquals(Set.of("iE"), indirectE);
        Assertions.assertEquals(Set.of(), indirectF);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_MEM",
            "OWL1_MEM",
            "RDFS_MEM",
    })
    public void testListIndividuals7a(TestSpec spec) {
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

        OntModel m = createClassesABCDAEB(OntModelFactory.createModel(spec.inst));
        OntClass A = m.getResource(NS + "A").as(OntClass.class);
        OntClass B = m.getResource(NS + "B").as(OntClass.class);
        OntClass C = m.getResource(NS + "C").as(OntClass.class);
        m.getResource(NS + "D").as(OntClass.class);
        OntClass E = m.getResource(NS + "E").as(OntClass.class);

        A.createIndividual(NS + "iA");
        B.createIndividual(NS + "iB");
        OntIndividual CE = C.createIndividual(NS + "iCE");
        CE.attachClass(E);
        OntIndividual DBA = B.createIndividual(NS + "iDBA");
        DBA.attachClass(B);
        DBA.attachClass(A);

        Set<String> directA = individuals(m, "A", true);
        Set<String> indirectA = individuals(m, "A", false);

        Set<String> directB = individuals(m, "B", true);
        Set<String> indirectB = individuals(m, "B", false);

        Set<String> directC = individuals(m, "C", true);
        Set<String> indirectC = individuals(m, "C", false);

        Set<String> directD = individuals(m, "D", true);
        Set<String> indirectD = individuals(m, "D", false);

        Set<String> directE = individuals(m, "E", true);
        Set<String> indirectE = individuals(m, "E", false);

        Assertions.assertEquals(Set.of("iA"), directA);
        Assertions.assertEquals(Set.of("iB", "iDBA"), directB);
        Assertions.assertEquals(Set.of("iCE"), directC);
        Assertions.assertEquals(Set.of(), directD);
        Assertions.assertEquals(Set.of("iCE"), directE);
        Assertions.assertEquals(Set.of("iA", "iDBA"), indirectA);
        Assertions.assertEquals(Set.of("iB", "iDBA"), indirectB);
        Assertions.assertEquals(Set.of("iCE"), indirectC);
        Assertions.assertEquals(Set.of(), indirectD);
        Assertions.assertEquals(Set.of("iCE"), indirectE);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM_RDFS_INF",
            "OWL1_MEM_RDFS_INF",
            "RDFS_MEM_RDFS_INF",
    })
    public void testListIndividuals7b(TestSpec spec) {
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

        OntModel m = createClassesABCDAEB(OntModelFactory.createModel(spec.inst));
        OntClass A = m.getResource(NS + "A").as(OntClass.class);
        OntClass B = m.getResource(NS + "B").as(OntClass.class);
        OntClass C = m.getResource(NS + "C").as(OntClass.class);
        m.getResource(NS + "D").as(OntClass.class);
        OntClass E = m.getResource(NS + "E").as(OntClass.class);

        A.createIndividual(NS + "iA");
        B.createIndividual(NS + "iB");
        OntIndividual CE = C.createIndividual(NS + "iCE");
        CE.attachClass(E);
        OntIndividual DBA = B.createIndividual(NS + "iDBA");
        DBA.attachClass(B);
        DBA.attachClass(A);

        Set<String> directA = individuals(m, "A", true);
        Set<String> indirectA = individuals(m, "A", false);

        Set<String> directB = individuals(m, "B", true);
        Set<String> indirectB = individuals(m, "B", false);

        Set<String> directC = individuals(m, "C", true);
        Set<String> indirectC = individuals(m, "C", false);

        Set<String> directD = individuals(m, "D", true);
        Set<String> indirectD = individuals(m, "D", false);

        Set<String> directE = individuals(m, "E", true);
        Set<String> indirectE = individuals(m, "E", false);

        Assertions.assertEquals(Set.of("iA", "iB", "iDBA", "iCE"), directA);
        Assertions.assertEquals(Set.of("iA", "iB", "iDBA", "iCE"), directB);
        Assertions.assertEquals(Set.of("iA", "iB", "iDBA", "iCE"), directC);
        Assertions.assertEquals(Set.of("iA", "iB", "iDBA", "iCE"), directD);
        Assertions.assertEquals(Set.of(), directE);
        Assertions.assertEquals(Set.of("iA", "iB", "iDBA", "iCE"), indirectA);
        Assertions.assertEquals(Set.of("iA", "iB", "iDBA", "iCE"), indirectB);
        Assertions.assertEquals(Set.of("iA", "iB", "iDBA", "iCE"), indirectC);
        Assertions.assertEquals(Set.of("iA", "iB", "iDBA", "iCE"), indirectD);
        Assertions.assertEquals(Set.of("iA", "iB", "iDBA", "iCE"), indirectE);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_MEM_TRANS_INF",
            "OWL1_MEM_TRANS_INF",
            "RDFS_MEM_TRANS_INF",
    })
    public void testListIndividuals7c(TestSpec spec) {
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

        OntModel m = createClassesABCDAEB(OntModelFactory.createModel(spec.inst));
        OntClass A = m.getResource(NS + "A").as(OntClass.class);
        OntClass B = m.getResource(NS + "B").as(OntClass.class);
        OntClass C = m.getResource(NS + "C").as(OntClass.class);
        m.getResource(NS + "D").as(OntClass.class);
        OntClass E = m.getResource(NS + "E").as(OntClass.class);

        A.createIndividual(NS + "iA");
        B.createIndividual(NS + "iB");
        OntIndividual CE = C.createIndividual(NS + "iCE");
        CE.attachClass(E);
        OntIndividual DBA = B.createIndividual(NS + "iDBA");
        DBA.attachClass(B);
        DBA.attachClass(A);

        Set<String> directA = individuals(m, "A", true);
        Set<String> indirectA = individuals(m, "A", false);

        Set<String> directB = individuals(m, "B", true);
        Set<String> indirectB = individuals(m, "B", false);

        Set<String> directC = individuals(m, "C", true);
        Set<String> indirectC = individuals(m, "C", false);

        Set<String> directD = individuals(m, "D", true);
        Set<String> indirectD = individuals(m, "D", false);

        Set<String> directE = individuals(m, "E", true);
        Set<String> indirectE = individuals(m, "E", false);

        Assertions.assertEquals(Set.of("iA", "iDBA"), directA);
        Assertions.assertEquals(Set.of("iB", "iDBA"), directB);
        Assertions.assertEquals(Set.of("iCE"), directC);
        Assertions.assertEquals(Set.of(), directD);
        Assertions.assertEquals(Set.of(), directE);
        Assertions.assertEquals(Set.of("iA", "iDBA"), indirectA);
        Assertions.assertEquals(Set.of("iB", "iDBA"), indirectB);
        Assertions.assertEquals(Set.of("iCE"), indirectC);
        Assertions.assertEquals(Set.of(), indirectD);
        Assertions.assertEquals(Set.of("iCE"), indirectE);
    }
}
