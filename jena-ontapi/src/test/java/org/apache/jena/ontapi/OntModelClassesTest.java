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
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.shared.PrefixMapping;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class OntModelClassesTest {

    @SuppressWarnings("ExtractMethodRecommender")
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
    public void testListClasses1a(TestSpec spec) {
        OntModel m = RDFIOTestUtils.readResourceToModel(
                OntModelFactory.createModel(spec.inst), "/list-syntax-categories-test.rdf", Lang.RDFXML
        );
        List<String> actual = m.ontObjects(OntClass.class)
                .map(it -> it.isAnon() ? "null" : it.getLocalName())
                .sorted()
                .collect(Collectors.toList());
        // there is DataHasValue Restriction in the RDF,
        //  but connected property (`owl:onProperty`) has no `owl:DatatypeProperty`
        //  declaration (it is declared as bar `rdf:Property`),
        //  so in strict mode such construction cannot be considered as a valid class expression;
        //  for OWL1 this is correct for compatibility with OntModel
        List<String> expected = new ArrayList<>(Arrays.asList("A", "B", "C", "D", "E", "X0", "X1", "Y0", "Y1", "Z"));
        if (!spec.inst.getPersonality().getName().startsWith("OWL2")) {
            expected.add("null");
        }
        Assertions.assertEquals(expected, actual);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "RDFS_MEM",
            "RDFS_MEM_TRANS_INF",
    })
    public void testListClasses1b(TestSpec spec) {
        OntModel m = RDFIOTestUtils.readResourceToModel(
                OntModelFactory.createModel(spec.inst), "/list-syntax-categories-test.rdf", Lang.RDFXML
        );
        Assertions.assertEquals(0, m.ontObjects(OntClass.class).count());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL1_MEM_RULES_INF",
            "OWL1_LITE_MEM_RULES_INF",
            "RDFS_MEM_RDFS_INF",
    })
    public void testListClasses1c(TestSpec spec) {
        OntModel m = RDFIOTestUtils.readResourceToModel(
                OntModelFactory.createModel(spec.inst), "/list-syntax-categories-test.rdf", Lang.RDFXML
        );
        int expectedClassesSize;
        /*if (spec == TestSpec.OWL1_MEM_MICRO_RULES_INF) { // TODO
            expectedClassesSize = 36;
        } else*/
        if (spec == TestSpec.RDFS_MEM_RDFS_INF) {
            expectedClassesSize = 25;
        } else {
            expectedClassesSize = 42;
        }
        boolean expectedContainsAll = TestSpec.RDFS_MEM_RDFS_INF != spec;
        Assertions.assertEquals(expectedClassesSize, m.ontObjects(OntClass.class).distinct().count());
        Assertions.assertEquals(
                expectedContainsAll,
                m.ontObjects(OntClass.class).map(Resource::getLocalName).collect(Collectors.toSet())
                        .containsAll(Set.of("Y0", "Z", "B", "D", "Y1", "X0", "C", "E", "X1", "A"))
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
    public void testListClasses2a(TestSpec spec) {
        OntModel m = TestModelFactory.withBuiltIns(
                RDFIOTestUtils.readResourceToModel(
                        OntModelFactory.createModel(spec.inst), "/list-syntax-categories-test-with-import.rdf", Lang.RDFXML
                ));
        List<String> expected = new ArrayList<>(Arrays.asList("A", "B", "C", "D", "E", "Nothing", "Thing", "X0", "X1", "Y0", "Y1", "Z", "null"));
        if (!spec.inst.getPersonality().getName().equals("OWL2")) {
            expected.add("null");
        }
        List<String> actual = m.ontObjects(OntClass.class).distinct()
                .map(it -> it.isAnon() ? "null" : it.getLocalName())
                .sorted()
                .collect(Collectors.toList());
        Assertions.assertEquals(expected, actual);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "RDFS_MEM",
            "RDFS_MEM_TRANS_INF",
    })
    public void testListClasses2b(TestSpec spec) {
        OntModel m = RDFIOTestUtils.readResourceToModel(
                OntModelFactory.createModel(spec.inst), "/list-syntax-categories-test-with-import.rdf", Lang.RDFXML
        );
        Assertions.assertEquals(0, m.classes().count());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL1_MEM_RULES_INF",
            "OWL1_LITE_MEM_RULES_INF",
            "RDFS_MEM_RDFS_INF",
    })
    public void testListClasses2c(TestSpec spec) {
        OntModel m;
        if (spec == TestSpec.RDFS_MEM_RDFS_INF) {
            m = RDFIOTestUtils.readResourceToModel(
                    OntModelFactory.createModel(spec.inst), "/list-syntax-categories-test-with-import.rdf", Lang.RDFXML
            );
        } else {
            m = TestModelFactory.withBuiltIns(
                    RDFIOTestUtils.readResourceToModel(
                            OntModelFactory.createModel(spec.inst), "/list-syntax-categories-test-with-import.rdf", Lang.RDFXML
                    )).setNsPrefixes(PrefixMapping.Standard);
        }
        List<String> actual = m.ontObjects(OntClass.class)
                .distinct()
                .map(it -> it.isAnon() ? "null" : it.getLocalName())
                .sorted()
                .toList();
        Set<String> probes = Set.of("A", "B", "C", "D", "E", "Nothing", "Thing", "X0", "X1", "Y0", "Y1", "Z", "null");
        boolean expectedContainsAll = spec != TestSpec.RDFS_MEM_RDFS_INF;
        int expectedClassCount;
//        if (spec == TestSpec.OWL_MEM_MICRO_RULE_INF) { // TODO
//            expectedClassCount = 43;
//        } else
        if (spec == TestSpec.RDFS_MEM_RDFS_INF) {
            expectedClassCount = 25;
        } else {
            expectedClassCount = 61;
        }

        Assertions.assertEquals(expectedContainsAll, actual.containsAll(probes));
        Assertions.assertEquals(expectedClassCount, actual.size());
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
    public void testListClasses3a(TestSpec spec) {
        OntModel m = RDFIOTestUtils.readResourceToModel(
                OntModelFactory.createModel(spec.inst), "/list-syntax-categories-test-comps.rdf", Lang.RDFXML
        );
        List<String> expected = List.of(
                "eg:Bundle", "eg:Computer", "eg:GameBundle", "eg:GamingComputer", "eg:GraphicsCard", "eg:MotherBoard",
                "null", "null", "null"
        );
        List<String> actual = m.ontObjects(OntClass.class)
                .map(it -> it.isAnon() ? "null" : m.shortForm(it.getURI()))
                .sorted()
                .collect(Collectors.toList());
        Assertions.assertEquals(expected, actual);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL1_MEM_RULES_INF",
            "OWL1_LITE_MEM_RULES_INF",
    })
    public void testListClasses3b(TestSpec spec) {
        OntModel m = RDFIOTestUtils.readResourceToModel(
                OntModelFactory.createModel(spec.inst), "/list-syntax-categories-test-comps.rdf", Lang.RDFXML
        ).setNsPrefixes(PrefixMapping.Standard);
        List<String> expected = List.of(
                "eg:Bundle", "eg:Computer", "eg:GameBundle", "eg:GamingComputer", "eg:GraphicsCard", "eg:MotherBoard",
                "null", "null", "null", "null", "null",
                "owl:Class", "owl:Nothing", "owl:Ontology", "owl:Property", "owl:Restriction", "owl:Thing",
                "rdf:List", "rdf:Property", "rdf:Statement",
                "rdfs:Class", "rdfs:Literal", "rdfs:Resource",
                "xsd:boolean", "xsd:byte", "xsd:date", "xsd:dateTime", "xsd:decimal", "xsd:duration", "xsd:float", "xsd:int",
                "xsd:integer", "xsd:long", "xsd:nonNegativeInteger", "xsd:nonPositiveInteger", "xsd:short", "xsd:string",
                "xsd:time", "xsd:unsignedByte", "xsd:unsignedInt", "xsd:unsignedLong", "xsd:unsignedShort"
        );
        List<String> actual = m.ontObjects(OntClass.class)
                .distinct()
                .map(it -> it.isAnon() ? "null" : m.shortForm(it.getURI()))
                .sorted()
                .collect(Collectors.toList());
        Assertions.assertEquals(expected, actual);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "RDFS_MEM",
            "RDFS_MEM_TRANS_INF",
    })
    public void testListClasses3c(TestSpec spec) {
        OntModel m = RDFIOTestUtils.readResourceToModel(
                OntModelFactory.createModel(spec.inst), "/list-syntax-categories-test-comps.rdf", Lang.RDFXML
        );
        Assertions.assertTrue(m.ontObjects(OntClass.class).findFirst().isEmpty());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "RDFS_MEM_RDFS_INF",
    })
    public void testListClasses3d(TestSpec spec) {
        OntModel m = RDFIOTestUtils.readResourceToModel(
                OntModelFactory.createModel(spec.inst), "/list-syntax-categories-test-comps.rdf", Lang.RDFXML
        );
        List<String> expected = List.of(
                "eg:Bundle", "eg:Computer", "eg:GameBundle", "eg:GraphicsCard", "eg:MotherBoard",
                "null",
                "owl:Class", "owl:ObjectProperty", "owl:Restriction", "owl:TransitiveProperty",
                "rdf:Alt", "rdf:Bag", "rdf:List", "rdf:Property", "rdf:Seq", "rdf:Statement", "rdf:XMLLiteral",
                "rdfs:Class", "rdfs:Container", "rdfs:ContainerMembershipProperty", "rdfs:Datatype", "rdfs:Literal", "rdfs:Resource"
        );
        List<String> actual = m.ontObjects(OntClass.class)
                .map(it -> it.isAnon() ? "null" : m.shortForm(it.getURI()))
                .sorted()
                .collect(Collectors.toList());
        Assertions.assertEquals(expected, actual);
    }

}
