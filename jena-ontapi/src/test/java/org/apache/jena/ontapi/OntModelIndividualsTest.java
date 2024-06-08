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

import org.apache.jena.graph.Graph;
import org.apache.jena.ontapi.model.OntClass;
import org.apache.jena.ontapi.model.OntIndividual;
import org.apache.jena.ontapi.model.OntModel;
import org.apache.jena.ontapi.testutils.RDFIOTestUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.reasoner.InfGraph;
import org.apache.jena.riot.Lang;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.jena.ontapi.TestModelFactory.NS;

public class OntModelIndividualsTest {

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM",
            "OWL2_MEM_RDFS_INF",
            "OWL2_MEM_TRANS_INF",
            "OWL1_MEM",
            "OWL1_MEM_RDFS_INF",
            "OWL1_MEM_TRANS_INF",
            "OWL1_MEM_RULES_INF",
            "OWL1_LITE_MEM",
            "OWL1_LITE_MEM_RDFS_INF",
            "OWL1_LITE_MEM_RULES_INF",
            "OWL1_LITE_MEM_TRANS_INF",
    })
    public void testListIndividuals1(TestSpec spec) {
        OntModel m = RDFIOTestUtils.readResourceToModel(OntModelFactory.createModel(spec.inst),
                "/list-syntax-categories-test.rdf", Lang.RDFXML);
        Assertions.assertEquals(Set.of("A0", "A1", "C0", "a0", "a1", "a2", "z0", "z1"),
                m.individuals().map(Resource::getLocalName).collect(Collectors.toSet()));
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
            "OWL1_MEM_RULES_INF",
            "OWL1_LITE_MEM",
            "OWL1_LITE_MEM_RDFS_INF",
            "OWL1_LITE_MEM_RULES_INF",
            "OWL1_LITE_MEM_TRANS_INF",
    })
    public void testListIndividuals2(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst);
        Resource a0 = m.createResource(NS + "A0");
        m.add(a0, RDF.type, OWL2.Class);
        m.add(OWL2.Class, RDF.type, RDFS.Class);
        Assertions.assertTrue(m.individuals().findFirst().isEmpty());
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
    public void testListIndividuals3(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst);
        Resource a0 = m.createResource(NS + "A0");
        m.add(a0, RDF.type, OWL2.Class);
        m.add(OWL2.Class, RDF.type, OWL2.Class);
        Assertions.assertTrue(m.individuals().findFirst().isEmpty());
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
            "OWL1_MEM_RULES_INF",
            "OWL1_LITE_MEM",
            "OWL1_LITE_MEM_RDFS_INF",
            "OWL1_LITE_MEM_TRANS_INF",
            "OWL1_LITE_MEM_RULES_INF",
            "RDFS_MEM",
            "RDFS_MEM_TRANS_INF",
    })
    public void testListIndividuals4(TestSpec spec) {
        // For inference model
        OntModel m = OntModelFactory.createModel(spec.inst);
        Resource a0 = m.createResource(NS + "A0");
        m.add(a0, RDF.type, OWL2.Class);
        Assertions.assertTrue(m.individuals().findFirst().isEmpty());
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
            "OWL1_LITE_MEM_RULES_INF",
    })
    public void testListIndividuals5(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst);
        // a0 should be an individual, since we are punning
        Resource a0 = m.createResource(NS + "A0");
        Resource a1 = m.createResource(NS + "A1");
        m.add(a0, RDF.type, OWL2.Class);
        m.add(a1, RDF.type, OWL2.Class);
        m.add(a0, RDF.type, a1);
        Assertions.assertEquals(List.of(NS + "A0"), m.individuals().map(Resource::getURI).collect(Collectors.toList()));
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
    })
    public void testListIndividuals6a(TestSpec spec) {
        OntModel m = RDFIOTestUtils.readResourceToModel(OntModelFactory.createModel(spec.inst),
                "/list-syntax-categories-test-comps.rdf", Lang.RDFXML);
        Assertions.assertEquals(
                List.of(
                        "urn:x-hp:eg/DTPGraphics",
                        "urn:x-hp:eg/budgetGraphics",
                        "urn:x-hp:eg/gamingGraphics"),
                m.individuals().distinct().map(Resource::getURI).sorted().collect(Collectors.toList()));
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL1_LITE_MEM_RULES_INF",
    })
    public void testListIndividuals6b(TestSpec spec) {
        OntModel m = RDFIOTestUtils.readResourceToModel(OntModelFactory.createModel(spec.inst),
                "/list-syntax-categories-test-comps.rdf", Lang.RDFXML);
        Assertions.assertEquals(
                Arrays.asList(
                        null,
                        null,
                        null,
                        "urn:x-hp:eg/DTPGraphics",
                        "urn:x-hp:eg/budgetGraphics",
                        "urn:x-hp:eg/gamingGraphics"
                ),
                m.individuals()
                        .map(Resource::getURI)
                        .sorted(Comparator.nullsFirst(Comparator.naturalOrder()))
                        .collect(Collectors.toList())
        );
    }

    @ParameterizedTest
    @EnumSource(names = {
            "RDFS_MEM",
            "RDFS_MEM_TRANS_INF",
    })
    public void testListIndividuals6c(TestSpec spec) {
        OntModel m = RDFIOTestUtils.readResourceToModel(OntModelFactory.createModel(spec.inst),
                "/list-syntax-categories-test-comps.rdf", Lang.RDFXML);
        Assertions.assertEquals(0, m.individuals().count());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "RDFS_MEM_RDFS_INF",
    })
    public void testListIndividuals6d(TestSpec spec) {
        OntModel m = RDFIOTestUtils.readResourceToModel(OntModelFactory.createModel(spec.inst),
                "/list-syntax-categories-test-comps.rdf", Lang.RDFXML);
        Assertions.assertEquals(
                Arrays.asList(
                        null, null, null,
                        "urn:x-hp:eg/Bundle",
                        "urn:x-hp:eg/Computer",
                        "urn:x-hp:eg/DTPGraphics",
                        "urn:x-hp:eg/GameBundle",
                        "urn:x-hp:eg/GamingComputer",
                        "urn:x-hp:eg/GraphicsCard",
                        "urn:x-hp:eg/MotherBoard",
                        "urn:x-hp:eg/budgetGraphics",
                        "urn:x-hp:eg/gamingGraphics",
                        "urn:x-hp:eg/hasBundle",
                        "urn:x-hp:eg/hasComponent",
                        "urn:x-hp:eg/hasGraphics",
                        "urn:x-hp:eg/hasMotherBoard"
                ),
                m.individuals()
                        .map(Resource::getURI)
                        .sorted(Comparator.nullsFirst(Comparator.naturalOrder()))
                        .collect(Collectors.toList())
        );
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
    })
    public void testListIndividuals7(TestSpec spec) {
        Model schema = ModelFactory.createDefaultModel();
        Model data = ModelFactory.createDefaultModel();
        Resource c = schema.createResource("http://example.com/foo#AClass");
        Resource i = data.createResource("http://example.com/foo#anInd");
        schema.add(c, RDF.type, OWL2.Class);
        data.add(i, RDF.type, c);

        OntModel composite = OntModelFactory.createModel(schema.getGraph(), spec.inst);
        Graph g = composite.getGraph();
        UnionGraph ug = (UnionGraph) (g instanceof UnionGraph ? g : ((InfGraph) g).getRawGraph());
        ug.addSubGraph(data.getGraph());

        Assertions.assertEquals(
                List.of("http://example.com/foo#anInd"),
                composite.individuals().map(Resource::getURI).collect(Collectors.toList())
        );
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM",
            "OWL2_MEM_TRANS_INF",
    })
    public void testListIndividuals8(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst);
        OntClass.Named c0 = m.getOWLThing();
        OntClass.Named c1 = m.createOntClass(NS + "C1");
        OntClass.Named c2 = m.createOntClass(NS + "C2");
        OntClass.Named c3 = m.createOntClass(NS + "C3");

        OntIndividual i1 = c0.createIndividual(NS + "I1");
        OntIndividual i2 = m.createResource(NS + "I2", OWL2.NamedIndividual).as(OntIndividual.class);
        OntIndividual i3 = m.createResource(NS + "I3", OWL2.NamedIndividual).as(OntIndividual.class);
        OntIndividual i4 = m.createResource(NS + "I4", OWL2.NamedIndividual).as(OntIndividual.class);
        OntIndividual i6 = c3.createIndividual();
        OntIndividual i5 = c1.createIndividual(NS + "I5");

        c2.createIndividual(NS + "I5");
        c2.createIndividual(NS + "I3");
        c3.createIndividual(NS + "I3");

        i1.addSameAsStatement(i2);
        i3.addDifferentIndividual(i4);
        i3.addDifferentIndividual(i6);

        // class-assertions:
        Assertions.assertEquals(6, m.statements(null, RDF.type, null)
                .filter(x -> x.getObject().canAs(OntClass.class)).count());
        // all individuals:
        Assertions.assertEquals(6, m.ontObjects(OntIndividual.class).count());
        // named individuals:
        m.namedIndividuals().forEach(x -> Assertions.assertTrue(x.isURIResource()));
        Assertions.assertEquals(5, m.namedIndividuals().count());

        // distinct class asserted individuals:
        Assertions.assertEquals(4, m.individuals().count());
        Assertions.assertEquals(1, m.individuals().filter(RDFNode::isAnon).count());
        Assertions.assertEquals(Set.of(i1, i3, i5), m.individuals().filter(it -> !it.isAnon()).collect(Collectors.toSet()));
    }

    @ParameterizedTest
    @EnumSource(TestSpec.class)
    public void testListIndividuals9(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst);

        m.createResource("x", m.createResource("X"));
        m.createResource().addProperty(RDF.type, m.createResource("Y"));

        OntClass clazz = m.createOntClass("Q");
        clazz.createIndividual("q");
        clazz.createIndividual("w");
        if (!spec.isOWL2EL() && !spec.isOWL2QL()) {
            clazz.createIndividual();
        }

        List<OntIndividual> individuals = m.individuals().toList();

        int expectedNumOfIndividuals;
        if (spec == TestSpec.RDFS_MEM_RDFS_INF) {
            expectedNumOfIndividuals = 5;
        } else if (spec.isOWL2EL() || spec.isOWL2QL()) {
            expectedNumOfIndividuals = 2;
        } else {
            expectedNumOfIndividuals = 3;
        }
        Assertions.assertEquals(expectedNumOfIndividuals, individuals.size());
    }
}
