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

import org.apache.jena.ontapi.model.OntModel;
import org.apache.jena.ontapi.model.OntRelationalProperty;
import org.apache.jena.ontapi.testutils.RDFIOTestUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDF;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OntModelPropertiesTest {

    private static final String BASE = "http://www.test.com/test";
    private static final String NS = BASE + "#";

    @Test
    public void testListPropertiesWithPunnings() {
        OntModel m = OntModelFactory.createModel(TestSpec.OWL2_DL_MEM_RDFS_BUILTIN_INF.inst);
        m.createResource("X", OWL2.ObjectProperty);
        m.createResource("X", OWL2.DatatypeProperty);
        Assertions.assertEquals(0, m.objectProperties().count());
        Assertions.assertEquals(0, m.dataProperties().count());
        Assertions.assertEquals(0, m.ontObjects(OntRelationalProperty.class).count());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_MEM",
            "OWL2_MEM_TRANS_INF",
            "OWL1_MEM",
            "OWL1_MEM_TRANS_INF",
    })
    public void testListAllOntProperties1a(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst);
        // named object property
        Resource op = m.createResource(NS + "op", OWL2.ObjectProperty);
        // inverse object property:
        m.createResource().addProperty(OWL2.inverseOf, op);
        // datatype property
        m.createResource(NS + "dp", OWL2.DatatypeProperty);
        // annotation property
        m.createResource(NS + "ap", OWL2.AnnotationProperty);

        m.createResource(NS + "rp1", RDF.Property);
        m.createResource(NS + "rp2", RDF.Property);
        m.createResource(NS + "rest", RDF.rest);

        List<String> expected = new ArrayList<>(Arrays.asList("ap", "dp", "op", "rp1", "rp2"));
        if (spec.inst.getPersonality().getName().startsWith("OWL2")) {
            // support inverseOf property
            expected.add("null");
        }
        expected.sort(String::compareTo);

        List<String> actual = m.properties()
                .map(it -> it.isAnon() ? "null" : it.getLocalName())
                .sorted()
                .collect(Collectors.toList());
        Assertions.assertEquals(expected, actual);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_MEM_RDFS_INF",
            "OWL1_MEM_RDFS_INF",
    })
    public void testListAllOntProperties1b(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst);
        // named object property
        Resource op = m.createResource(NS + "op", OWL2.ObjectProperty);
        // inverse object property:
        m.createResource().addProperty(OWL2.inverseOf, op);
        // datatype property
        m.createResource(NS + "dp", OWL2.DatatypeProperty);
        // annotation property
        m.createResource(NS + "ap", OWL2.AnnotationProperty);

        m.createResource(NS + "rp1", RDF.Property);
        m.createResource(NS + "rp2", RDF.Property);
        m.createResource(NS + "rest", RDF.rest);

        List<String> expected = new ArrayList<>(Arrays.asList(
                "ap", "comment", "domain", "dp", "first", "isDefinedBy", "label", "object", "rp1", "op",
                "predicate", "range", "rp2", "rest", "seeAlso", "subClassOf", "subPropertyOf", "subject", "type"
        ));
        if (spec.inst.getPersonality().getName().startsWith("OWL2")) {
            // support inverseOf property
            expected.add("null");
        }
        expected.sort(String::compareTo);

        List<String> actual = m.properties()
                .map(it -> it.isAnon() ? "null" : it.getLocalName())
                .sorted()
                .collect(Collectors.toList());
        Assertions.assertEquals(expected, actual);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "RDFS_MEM",
    })
    public void testListAllOntProperties1e(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst);
        // named object property
        Resource op = m.createResource(NS + "op", OWL2.ObjectProperty);
        // inverse object property:
        m.createResource().addProperty(OWL2.inverseOf, op);
        // datatype property
        m.createResource(NS + "dp", OWL2.DatatypeProperty);
        // annotation property
        m.createResource(NS + "ap", OWL2.AnnotationProperty);

        m.createResource(NS + "rp1", RDF.Property);
        m.createResource(NS + "rp2", RDF.Property);
        m.createResource(NS + "rest", RDF.rest);

        List<String> expected = Stream.of("rp1", "rp2").sorted().collect(Collectors.toList());

        List<String> actual = m.properties()
                .map(Resource::getLocalName)
                .sorted()
                .collect(Collectors.toList());
        Assertions.assertEquals(expected, actual);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM",
            "OWL2_MEM_TRANS_INF",
            "OWL1_MEM",
            "OWL1_MEM_TRANS_INF",
    })
    public void testListAllOntProperties2a(TestSpec spec) {
        OntModel m = RDFIOTestUtils.readResourceToModel(
                OntModelFactory.createModel(spec.inst), "/list-syntax-categories-test.rdf", Lang.RDFXML
        );
        Assertions.assertEquals(5, m.properties().count());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_MEM_RDFS_INF",
            "OWL1_MEM_RDFS_INF",
    })
    public void testListAllOntProperties2b(TestSpec spec) {
        OntModel m = RDFIOTestUtils.readResourceToModel(
                OntModelFactory.createModel(spec.inst), "/list-syntax-categories-test.rdf", Lang.RDFXML
        );
        Assertions.assertEquals(19, m.properties().count());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "RDFS_MEM_RDFS_INF",
    })
    public void testListAllOntProperties2c(TestSpec spec) {
        OntModel m = RDFIOTestUtils.readResourceToModel(
                OntModelFactory.createModel(spec.inst), "/list-syntax-categories-test.rdf", Lang.RDFXML
        );
        Assertions.assertEquals(15, m.properties().count());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "RDFS_MEM",
            "RDFS_MEM_TRANS_INF",
    })
    public void testListAllOntProperties2d(TestSpec spec) {
        OntModel m = RDFIOTestUtils.readResourceToModel(
                OntModelFactory.createModel(spec.inst), "/list-syntax-categories-test.rdf", Lang.RDFXML
        );
        Assertions.assertEquals(1, m.properties().count());
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
    public void testListAllOntProperties3a(TestSpec spec) {
        OntModel m = TestModelFactory.withBuiltIns(
                RDFIOTestUtils.readResourceToModel(
                        OntModelFactory.createModel(spec.inst), "/list-syntax-categories-test-with-import.rdf", Lang.RDFXML
                ));
        Assertions.assertEquals(44, m.properties().count());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "RDFS_MEM_RDFS_INF",
    })
    public void testListOntProperties4b(TestSpec spec) {
        testListOntProperties4(spec, 15, 15);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM",
            "OWL1_MEM",
            "OWL2_MEM_TRANS_INF",
            "OWL1_MEM_TRANS_INF",
    })
    public void testListOntProperties4c(TestSpec spec) {
        testListOntProperties4(spec, 5, 5);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_MEM_RDFS_INF",
            "OWL1_MEM_RDFS_INF",
    })
    public void testListOntProperties4d(TestSpec spec) {
        testListOntProperties4(spec, 19, 19);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "RDFS_MEM",
            "RDFS_MEM_TRANS_INF",
    })
    public void testListOntProperties4e(TestSpec spec) {
        testListOntProperties4(spec, 1, 1);
    }

    private void testListOntProperties4(TestSpec spec, int expected1, int expected2) {
        OntModel m = RDFIOTestUtils.readResourceToModel(
                OntModelFactory.createModel(spec.inst), "/list-syntax-categories-test-with-import.rdf", Lang.RDFXML
        );
        int actual1 = (int) m.properties().count();
        int actual2 = (int) m.properties().distinct().count();
        Assertions.assertEquals(expected1, actual1);
        Assertions.assertEquals(expected2, actual2);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM",
            "OWL2_MEM_TRANS_INF",
    })
    public void testListOntProperties5a(TestSpec spec) {
        testListOntProperties5(spec, 13, 7, 2, 1);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL1_MEM",
            "OWL1_MEM_TRANS_INF",
    })
    public void testListOntProperties5b(TestSpec spec) {
        testListOntProperties5(spec, 11, 5, 2, 1);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "RDFS_MEM",
            "RDFS_MEM_TRANS_INF",
    })
    public void testListOntProperties5c(TestSpec spec) {
        testListOntProperties5(spec, 3, 0, 0, 0);
    }


    private void testListOntProperties5(TestSpec spec,
                                        long expectedOntProperties,
                                        long expectedObjectProperties,
                                        long expectedDataProperties,
                                        long expectedAnnotationProperties) {
        Model g = ModelFactory.createDefaultModel();
        g.createResource(NS + "op1", OWL2.ObjectProperty);
        g.createResource(NS + "op1", OWL2.SymmetricProperty);
        g.createResource(NS + "op2", OWL2.SymmetricProperty);
        g.createResource(NS + "op3", OWL2.InverseFunctionalProperty);
        g.createResource(NS + "op4", OWL2.ReflexiveProperty);
        g.createResource(NS + "op5", OWL2.IrreflexiveProperty);
        g.createResource(NS + "op6", OWL2.SymmetricProperty);
        g.createResource(NS + "op6", OWL2.TransitiveProperty);
        g.createResource(NS + "op7", OWL2.TransitiveProperty);
        g.createResource(NS + "dp1", OWL2.DatatypeProperty);
        g.createResource(NS + "dp2", OWL2.DatatypeProperty);
        g.createResource(NS + "xp1", OWL2.FunctionalProperty);
        g.createResource(NS + "ap1", OWL2.AnnotationProperty);
        g.createResource(NS + "ap1", RDF.Property);
        g.createResource(NS + "rp1", RDF.Property);
        g.createResource(NS + "rp2", RDF.Property);

        OntModel m = OntModelFactory.createModel(g.getGraph(), spec.inst);
        long actualObjectProperties = m.objectProperties().count();
        long actualDataProperties = m.dataProperties().count();
        long actualAnnotationProperties = m.annotationProperties().count();
        long actualOntProperties = m.properties().count();
        Assertions.assertEquals(expectedObjectProperties, actualObjectProperties);
        Assertions.assertEquals(expectedDataProperties, actualDataProperties);
        Assertions.assertEquals(expectedAnnotationProperties, actualAnnotationProperties);
        Assertions.assertEquals(expectedOntProperties, actualOntProperties);
    }
}
