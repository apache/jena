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
import org.apache.jena.ontapi.model.OntIndividual;
import org.apache.jena.ontapi.model.OntModel;
import org.apache.jena.ontapi.model.OntObjectProperty;
import org.apache.jena.ontapi.model.OntProperty;
import org.apache.jena.ontapi.model.OntRelationalProperty;
import org.apache.jena.ontapi.testutils.RDFIOTestUtils;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Set;
import java.util.stream.Collectors;

public class OntClassDeclaredPropertiesTest {
    private static final String BASE = "http://jena.hpl.hp.com/testing/ontology";
    private static final String NS = BASE + "#";

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
    public void testListDeclaredProperties1(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst).setNsPrefixes(OntModelFactory.STANDARD);
        OntClass A = m.createOntClass("A");
        OntClass B = m.createOntClass("B");
        OntClass C = m.createOntClass("C");
        OntClass D = m.createOntClass("D");
        OntClass E = m.createOntClass("E");
        A.addSuperClass(B);
        B.addSuperClass(C);
        C.addSuperClass(D);
        E.addSuperClass(m.getOWLThing());

        OntDataProperty d1 = m.createDataProperty("d1");
        OntDataProperty d2 = m.createDataProperty("d2");
        OntObjectProperty o1 = m.createObjectProperty("o1");
        OntObjectProperty o2 = m.createObjectProperty("o2");
        o1.addSuperProperty(o2);
        d1.addDomain(A);
        d2.addDomain(B);
        o1.addDomain(C);
        o2.addDomain(m.getOWLThing());

        Assertions.assertEquals(Set.of(o2), m.getOWLThing().declaredProperties(true).collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(o2), m.getOWLThing().declaredProperties(false).collect(Collectors.toSet()));

        Assertions.assertEquals(Set.of(), m.getOWLNothing().declaredProperties(true).collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(o2), m.getOWLNothing().declaredProperties(false).collect(Collectors.toSet()));

        Assertions.assertEquals(Set.of(d1), A.declaredProperties(true).collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(d1, d2, o1, o2), A.declaredProperties(false).collect(Collectors.toSet()));

        Assertions.assertEquals(Set.of(d2), B.declaredProperties(true).collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(d2, o1, o2), B.declaredProperties(false).collect(Collectors.toSet()));

        Assertions.assertEquals(Set.of(o1), C.declaredProperties(true).collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(o1, o2), C.declaredProperties(false).collect(Collectors.toSet()));

        Assertions.assertEquals(Set.of(o2), D.declaredProperties(true).collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(o2), D.declaredProperties(false).collect(Collectors.toSet()));

        Assertions.assertEquals(Set.of(o2), E.declaredProperties(true).collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(o2), E.declaredProperties(false).collect(Collectors.toSet()));
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM",
            "OWL2_MEM_RDFS_INF",
            "OWL2_MEM_TRANS_INF",
            "OWL1_MEM",
            "OWL1_MEM_TRANS_INF",
            "OWL1_MEM_RDFS_INF",
            "RDFS_MEM",
            "RDFS_MEM_RDFS_INF",
            "RDFS_MEM_TRANS_INF",
    })
    public void testListDeclaredProperties2(TestSpec spec) {
        //    D
        //  /  \
        // B    F
        // |    |
        // C    E
        //  \  /
        //    A
        OntModel m = TestModelFactory.createClassesDBFCEA(OntModelFactory.createModel(spec.inst));

        Resource d1 = m.createResource(TestModelFactory.NS + "d1", OWL2.DatatypeProperty).addProperty(RDF.type, RDF.Property);
        Resource d2 = m.createResource(TestModelFactory.NS + "d2", OWL2.DatatypeProperty).addProperty(RDF.type, RDF.Property);
        Resource o1 = m.createResource(TestModelFactory.NS + "o1", OWL2.ObjectProperty).addProperty(RDF.type, RDF.Property);
        Resource o2 = m.createResource(TestModelFactory.NS + "o2", OWL2.ObjectProperty).addProperty(RDF.type, RDF.Property);
        o1.addProperty(RDFS.subClassOf, o2);
        o2.addProperty(RDFS.subClassOf, OWL2.topObjectProperty);
        d1.addProperty(RDFS.domain, m.getResource(TestModelFactory.NS + "A"));
        d2.addProperty(RDFS.domain, m.getResource(TestModelFactory.NS + "B"));
        o1.addProperty(RDFS.domain, m.getResource(TestModelFactory.NS + "C"));
        o2.addProperty(RDFS.domain, m.getResource(TestModelFactory.NS + "F"));
        o2.addProperty(RDFS.range, m.getResource(TestModelFactory.NS + "E"));
        d2.addProperty(RDFS.range, m.getResource(TestModelFactory.NS + "D"));

        OntClass A = m.createOntClass(TestModelFactory.NS + "A");
        OntClass B = m.createOntClass(TestModelFactory.NS + "B");
        OntClass C = m.createOntClass(TestModelFactory.NS + "C");
        OntClass D = m.createOntClass(TestModelFactory.NS + "D");
        OntClass E = m.createOntClass(TestModelFactory.NS + "E");
        OntClass F = m.createOntClass(TestModelFactory.NS + "F");

        Assertions.assertEquals(Set.of(d1), A.declaredProperties(true).collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(o1, o2, d1, d2), A.declaredProperties(false).collect(Collectors.toSet()));

        Assertions.assertEquals(Set.of(d2), B.declaredProperties(true).collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(d2), B.declaredProperties(false).collect(Collectors.toSet()));

        Assertions.assertEquals(Set.of(o1), C.declaredProperties(true).collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(o1, d2), C.declaredProperties(false).collect(Collectors.toSet()));

        Assertions.assertEquals(Set.of(), D.declaredProperties(true).collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(), D.declaredProperties(false).collect(Collectors.toSet()));

        Assertions.assertEquals(Set.of(), E.declaredProperties(true).collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(o2), E.declaredProperties(false).collect(Collectors.toSet()));

        Assertions.assertEquals(Set.of(o2), F.declaredProperties(true).collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(o2), F.declaredProperties(false).collect(Collectors.toSet()));
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
    public void testListDeclaredProperties3(TestSpec spec) {
        OntModel m0 = OntModelFactory.createModel(spec.inst).setID("http://m0").getModel();

        // in model M0, p0 has class c0 in the domain
        OntClass c0 = m0.createOntClass(TestModelFactory.NS + "c0");
        OntObjectProperty p0 = m0.createObjectProperty(TestModelFactory.NS + "p0");
        p0.addDomain(c0);

        // in model M1, class c1 is a subClass of c0
        OntModel m1 = OntModelFactory.createModel(spec.inst);
        OntClass c1 = m1.createOntClass(TestModelFactory.NS + "c1");
        c1.addSuperClass(c0);

        // simulate imports
        m1.addImport(m0);

        // get a c0 reference from m1
        OntClass cc0 = m1.getOntClass(TestModelFactory.NS + "c0");
        Assertions.assertNotNull(cc0);

        Assertions.assertEquals(Set.of(p0), c1.declaredProperties().collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(p0), c0.declaredProperties(false).collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(p0), cc0.declaredProperties(false).collect(Collectors.toSet()));
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
    public void testListDeclaredProperties4(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst);

        // a simple class hierarchy  organism -> vertebrate -> mammal -> dog
        OntClass organism = m.createOntClass(TestModelFactory.NS + "Organism");
        OntClass vertebrate = m.createOntClass(TestModelFactory.NS + "Vertebrate");
        OntClass mammal = m.createOntClass(TestModelFactory.NS + "Mammal");
        OntClass dog = m.createOntClass(TestModelFactory.NS + "Dog");

        organism.addSubClass(vertebrate);
        vertebrate.addSubClass(mammal);
        mammal.addSubClass(dog);

        // hair as a covering
        OntClass covering = m.createOntClass(TestModelFactory.NS + "Covering");
        OntIndividual hair = covering.createIndividual(TestModelFactory.NS + "hair");

        // various properties
        OntObjectProperty limbsCount = m.createObjectProperty(TestModelFactory.NS + "limbsCount");
        OntObjectProperty hasCovering = m.createObjectProperty(TestModelFactory.NS + "hasCovering");
        OntObjectProperty numYoung = m.createObjectProperty(TestModelFactory.NS + "numYoung");

        // vertebrates have limbs, mammals have live young
        limbsCount.addDomain(vertebrate);
        numYoung.addDomain(mammal);

        // mammals have-covering = hair
        OntClass r = m.createObjectHasValue(hasCovering, hair);
        mammal.addSuperClass(r);

        Assertions.assertEquals(Set.of(hasCovering), organism.declaredProperties().collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(limbsCount, hasCovering), vertebrate.declaredProperties().collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(limbsCount, hasCovering, numYoung), mammal.declaredProperties().collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(limbsCount, hasCovering, numYoung), dog.declaredProperties().collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(hasCovering), r.declaredProperties().collect(Collectors.toSet()));

        Assertions.assertEquals(Set.of(hasCovering), organism.declaredProperties(true).collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(limbsCount), vertebrate.declaredProperties(true).collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(numYoung), mammal.declaredProperties(true).collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(), dog.declaredProperties(true).collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(hasCovering), r.declaredProperties(true).collect(Collectors.toSet()));

        Assertions.assertEquals(Set.of(hasCovering), organism.declaredProperties(false).collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(hasCovering, limbsCount), vertebrate.declaredProperties(false).collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(hasCovering, numYoung, limbsCount), mammal.declaredProperties(false).collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(hasCovering, numYoung, limbsCount), dog.declaredProperties(false).collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(hasCovering), r.declaredProperties(false).collect(Collectors.toSet()));
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
    public void testListDeclaredProperties5a(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst);
        OntClass A = m.createOntClass(NS + "A");
        OntClass C = m.createOntClass(NS + "C");
        C.addSuperClass(A);

        OntRelationalProperty p = m.createDataProperty(NS + "p");
        OntRelationalProperty q = m.createDataProperty(NS + "q");
        OntRelationalProperty s = m.createDataProperty(NS + "s");

        p.addDomain(A);
        q.addDomain(A);
        s.addDomain(C);

        Assertions.assertEquals(Set.of(p, q, s), C.declaredProperties().collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(p, q, s), C.declaredProperties(false).collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(s), C.declaredProperties(true).collect(Collectors.toSet()));
    }

    @ParameterizedTest
    @EnumSource(names = {
            "RDFS_MEM",
            "RDFS_MEM_RDFS_INF",
            "RDFS_MEM_TRANS_INF",
    })
    public void testListDeclaredProperties5b(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst);
        OntClass A = m.createOntClass(NS + "A");
        OntClass C = m.createOntClass(NS + "C");
        C.addSuperClass(A);

        OntProperty p = m.createRDFProperty(NS + "p");
        OntProperty q = m.createRDFProperty(NS + "q");
        OntProperty s = m.createRDFProperty(NS + "s");

        p.addDomainStatement(A);
        q.addDomainStatement(A);
        s.addDomainStatement(C);

        Assertions.assertEquals(Set.of(p, q, s), C.declaredProperties().collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(p, q, s), C.declaredProperties(false).collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(s), C.declaredProperties(true).collect(Collectors.toSet()));
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
    public void testListDeclaredProperties6(TestSpec spec) {
        OntModel m = RDFIOTestUtils.readResourceToModel(
                OntModelFactory.createModel(spec.inst),
                "/frame-view-test-ldp.rdf",
                Lang.RDFXML
        );

        OntClass A = m.getOntClass(NS + "A");
        OntClass B = m.getOntClass(NS + "B");
        OntClass C = m.getOntClass(NS + "C");

        OntObjectProperty pA = m.getObjectProperty(NS + "pA");
        OntObjectProperty pB = m.getObjectProperty(NS + "pB");
        OntObjectProperty pC = m.getObjectProperty(NS + "pC");
        OntObjectProperty qA = m.getObjectProperty(NS + "qA");
        OntObjectProperty global = m.getObjectProperty(NS + "global");
        OntObjectProperty qB = m.getObjectProperty(NS + "qB");

        Assertions.assertEquals(Set.of(pA, qA, global, qB), A.declaredProperties(false).collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(pA, qA, global, qB), A.declaredProperties(true).collect(Collectors.toSet()));

        Assertions.assertEquals(Set.of(pA, pB, qA, global, qB), B.declaredProperties(false).collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(pB), B.declaredProperties(true).collect(Collectors.toSet()));

        Assertions.assertEquals(Set.of(pA, pB, pC, qA, global, qB), C.declaredProperties(false).collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(pC), C.declaredProperties(true).collect(Collectors.toSet()));
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
    public void testListDeclaredProperties7a(TestSpec spec) {
        OntModel m = RDFIOTestUtils.readResourceToModel(
                OntModelFactory.createModel(spec.inst),
                "/frame-view-test-ldp.rdf",
                Lang.RDFXML
        );

        OntClass Union1 = m.getOntClass(NS + "Union1");
        OntClass Union2 = m.getOntClass(NS + "Union2");
        OntClass Intersect1 = m.getOntClass(NS + "Intersect1");
        OntClass Intersect2 = m.getOntClass(NS + "Intersect2");
        OntClass HasAnn = m.getOntClass(NS + "HasAnn");

        OntAnnotationProperty ann = m.getAnnotationProperty(NS + "ann");
        OntObjectProperty global = m.getObjectProperty(NS + "global");
        OntObjectProperty qB = m.getObjectProperty(NS + "qB");

        Assertions.assertEquals(Set.of(global, qB), Union1.declaredProperties(false).collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(global, qB), Union2.declaredProperties(false).collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(global, qB), Union1.declaredProperties(true).collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(global, qB), Union2.declaredProperties(true).collect(Collectors.toSet()));

        Assertions.assertEquals(Set.of(qB, global), Intersect1.declaredProperties(false).collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(qB, global), Intersect2.declaredProperties(false).collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(qB, global), Intersect1.declaredProperties(true).collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(qB, global), Intersect2.declaredProperties(true).collect(Collectors.toSet()));

        Assertions.assertEquals(Set.of(ann, global, qB), HasAnn.declaredProperties(false).collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(ann, global, qB), HasAnn.declaredProperties(true).collect(Collectors.toSet()));
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM",
            "OWL2_MEM_RDFS_INF",
            "OWL2_MEM_TRANS_INF",
    })
    public void testHasDeclaredProperties1(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst).setNsPrefixes(OntModelFactory.STANDARD);
        OntClass c1 = m.createObjectUnionOf(m.getOWLThing());
        OntClass c2 = m.createObjectOneOf(m.createIndividual("i1"), m.createIndividual("i2"));
        OntClass c3 = m.createOntClass("C3"); // root
        OntClass c4 = m.createOntClass("C4"); // root
        OntClass c5 = m.createOntClass("C5"); // root

        c1.addSuperClass(c2.addSuperClass(c3));
        c5.addSuperClass(m.getOWLThing());
        OntDataProperty d1 = m.createDataProperty("d1").addDomain(c1);
        OntDataProperty d2 = m.createDataProperty("d2").addDomain(c2);
        OntObjectProperty o1 = m.createObjectProperty("o1").addDomain(c3);
        OntObjectProperty o2 = m.createObjectProperty("o2"); // global

        Assertions.assertFalse(c1.hasDeclaredProperty(m.getOWLBottomDataProperty(), true));
        Assertions.assertFalse(c4.hasDeclaredProperty(m.getOWLTopObjectProperty(), false));

        Assertions.assertTrue(c1.hasDeclaredProperty(d1, false));
        Assertions.assertTrue(c1.hasDeclaredProperty(d2, false));
        Assertions.assertTrue(c1.hasDeclaredProperty(o1, false));
        Assertions.assertTrue(c1.hasDeclaredProperty(o2, false));
        Assertions.assertTrue(c1.hasDeclaredProperty(d1, true));
        Assertions.assertFalse(c1.hasDeclaredProperty(d2, true));
        Assertions.assertFalse(c1.hasDeclaredProperty(o1, true));
        Assertions.assertFalse(c1.hasDeclaredProperty(o2, true));

        Assertions.assertFalse(c2.hasDeclaredProperty(d1, false));
        Assertions.assertTrue(c2.hasDeclaredProperty(d2, false));
        Assertions.assertTrue(c2.hasDeclaredProperty(o1, false));
        Assertions.assertTrue(c2.hasDeclaredProperty(o2, false));
        Assertions.assertFalse(c2.hasDeclaredProperty(d1, true));
        Assertions.assertTrue(c2.hasDeclaredProperty(d2, true));
        Assertions.assertFalse(c2.hasDeclaredProperty(o1, true));
        Assertions.assertFalse(c2.hasDeclaredProperty(o2, true));

        Assertions.assertFalse(c3.hasDeclaredProperty(d1, false));
        Assertions.assertFalse(c3.hasDeclaredProperty(d2, false));
        Assertions.assertTrue(c3.hasDeclaredProperty(o1, false));
        Assertions.assertTrue(c3.hasDeclaredProperty(o2, false));
        Assertions.assertFalse(c3.hasDeclaredProperty(d1, true));
        Assertions.assertFalse(c3.hasDeclaredProperty(d2, true));
        Assertions.assertTrue(c3.hasDeclaredProperty(o1, true));
        Assertions.assertTrue(c3.hasDeclaredProperty(o2, true));

        Assertions.assertFalse(c4.hasDeclaredProperty(d1, false));
        Assertions.assertFalse(c4.hasDeclaredProperty(d2, false));
        Assertions.assertFalse(c4.hasDeclaredProperty(o1, false));
        Assertions.assertTrue(c4.hasDeclaredProperty(o2, false));
        Assertions.assertFalse(c4.hasDeclaredProperty(d1, true));
        Assertions.assertFalse(c4.hasDeclaredProperty(d2, true));
        Assertions.assertFalse(c4.hasDeclaredProperty(o1, true));
        Assertions.assertTrue(c4.hasDeclaredProperty(o2, true));

        Assertions.assertFalse(c5.hasDeclaredProperty(d1, false));
        Assertions.assertFalse(c5.hasDeclaredProperty(d2, false));
        Assertions.assertFalse(c5.hasDeclaredProperty(o1, false));
        Assertions.assertTrue(c5.hasDeclaredProperty(o2, false));
        Assertions.assertFalse(c5.hasDeclaredProperty(d1, true));
        Assertions.assertFalse(c5.hasDeclaredProperty(d2, true));
        Assertions.assertFalse(c5.hasDeclaredProperty(o1, true));
        Assertions.assertTrue(c5.hasDeclaredProperty(o2, true));
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
    public void testHasDeclaredProperties3a(TestSpec spec) {
        OntModel m = RDFIOTestUtils.readResourceToModel(
                OntModelFactory.createModel(spec.inst),
                "/frame-view-test-ldp.rdf",
                Lang.RDFXML
        );
        OntClass A = m.getOntClass(NS + "A");

        OntObjectProperty pA = m.getObjectProperty(NS + "pA");
        OntObjectProperty pB = m.getObjectProperty(NS + "pB");

        Assertions.assertTrue(A.hasDeclaredProperty(pA, false));
        Assertions.assertFalse(A.hasDeclaredProperty(pB, false));

        Assertions.assertTrue(A.hasDeclaredProperty(pA, true));
        Assertions.assertFalse(A.hasDeclaredProperty(pB, true));
    }

    @ParameterizedTest
    @EnumSource(names = {
            "RDFS_MEM",
            "RDFS_MEM_RDFS_INF",
            "RDFS_MEM_TRANS_INF",
    })
    public void testHasDeclaredProperties3b(TestSpec spec) {
        OntModel m = RDFIOTestUtils.readResourceToModel(
                OntModelFactory.createModel(spec.inst),
                "/frame-view-test-ldp.rdf",
                Lang.RDFXML
        );
        OntClass A = m.getOntClass(NS + "A");

        OntProperty pA = m.createResource(NS + "pA", RDF.Property).as(OntProperty.class);
        OntProperty pB = m.createResource(NS + "pB", RDF.Property).as(OntProperty.class);

        Assertions.assertTrue(A.hasDeclaredProperty(pA, false));
        Assertions.assertFalse(A.hasDeclaredProperty(pB, false));

        Assertions.assertTrue(A.hasDeclaredProperty(pA, true));
        Assertions.assertFalse(A.hasDeclaredProperty(pB, true));
    }
}
