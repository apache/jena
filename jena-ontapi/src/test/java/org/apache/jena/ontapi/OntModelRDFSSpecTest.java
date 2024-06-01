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

import org.apache.jena.enhanced.UnsupportedPolymorphismException;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.ontapi.model.OntClass;
import org.apache.jena.ontapi.model.OntDataProperty;
import org.apache.jena.ontapi.model.OntEntity;
import org.apache.jena.ontapi.model.OntIndividual;
import org.apache.jena.ontapi.model.OntModel;
import org.apache.jena.ontapi.model.OntObjectProperty;
import org.apache.jena.ontapi.model.OntProperty;
import org.apache.jena.ontapi.model.OntRelationalProperty;
import org.apache.jena.ontapi.model.OntStatement;
import org.apache.jena.ontapi.utils.StdModels;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OntModelRDFSSpecTest {

    @ParameterizedTest
    @EnumSource(names = {
            "RDFS_MEM",
            "RDFS_MEM_TRANS_INF",
    })
    public void testClasses(TestSpec spec) {
        Model base = ModelFactory.createDefaultModel();
        base.createResource("1", OWL2.Class);
        base.createResource("2", RDFS.Datatype);
        base.createResource("3", RDFS.Class);
        base.createResource("4", RDFS.Class);
        base.createResource(null, RDFS.Class);

        OntModel m = OntModelFactory.createModel(base.getGraph(), spec.inst);

        List<OntClass.Named> res1 = m.classes().toList();
        Assertions.assertEquals(List.of("3", "4"),
                res1.stream().map(Resource::getURI).sorted().collect(Collectors.toList())
        );
        List<OntEntity> res2 = m.ontEntities().toList();
        Assertions.assertEquals(List.of("3", "4"),
                res2.stream().map(Resource::getURI).sorted().collect(Collectors.toList())
        );
        List<OntClass> res4 = m.ontObjects(OntClass.Named.class).collect(Collectors.toList());
        Assertions.assertEquals(List.of("3", "4"),
                res4.stream().map(Resource::getURI).sorted().collect(Collectors.toList())
        );

        List<OntClass> res3 = m.ontObjects(OntClass.class).toList();
        Assertions.assertEquals(3, res3.size());
        Assertions.assertEquals(List.of("3", "4"),
                res3.stream().filter(RDFNode::isURIResource).map(Resource::getURI).sorted().collect(Collectors.toList())
        );
    }

    @ParameterizedTest
    @EnumSource(names = {
            "RDFS_MEM",
            "RDFS_MEM_TRANS_INF",
    })
    public void testProperties(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst);
        Resource p1 = m.createResource("1", RDF.Property);
        Resource p2 = m.createResource("2", OWL2.ObjectProperty);
        Resource p3 = m.createResource("3", OWL2.DatatypeProperty);
        Resource p4 = m.createResource("4", OWL2.AnnotationProperty);

        Assertions.assertEquals(List.of("1"),
                m.properties().map(Resource::getURI).collect(Collectors.toList())
        );
        Assertions.assertEquals(List.of("1"),
                m.ontObjects(OntProperty.class).map(Resource::getURI).collect(Collectors.toList())
        );

        Assertions.assertEquals(List.of(), m.ontObjects(OntObjectProperty.class).collect(Collectors.toList()));
        Assertions.assertEquals(List.of(), m.ontEntities().collect(Collectors.toList()));

        Assertions.assertTrue(p1.canAs(OntProperty.class));
        Assertions.assertFalse(p2.canAs(OntProperty.class));
        Assertions.assertFalse(p3.canAs(OntProperty.class));
        Assertions.assertFalse(p4.canAs(OntProperty.class));

        Stream.of(OntClass.class, OntRelationalProperty.class, OntDataProperty.class)
                .forEach(it -> Assertions.assertFalse(p1.canAs(it)));
    }

    @ParameterizedTest
    @EnumSource(names = {
            "RDFS_MEM",
            "RDFS_MEM_TRANS_INF",
    })
    public void testIndividuals(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst);
        Resource c1 = m.createResource("c1", RDFS.Class);
        Resource c2 = m.createResource("c2", RDFS.Class);
        Resource i1 = m.createResource("i1", c1);
        Resource i2 = m.createResource("i2", c2);
        Resource i3 = m.createResource(null, c2);

        List<OntIndividual> res1 = m.individuals().toList();
        Assertions.assertEquals(3, res1.size());
        Assertions.assertEquals(List.of("i1", "i2"),
                res1.stream().filter(RDFNode::isURIResource).map(Resource::getURI).sorted().collect(Collectors.toList())
        );

        List<OntEntity> res2 = m.ontEntities().toList();
        Assertions.assertEquals(List.of("c1", "c2", "i1", "i2"),
                res2.stream().map(Resource::getURI).sorted().collect(Collectors.toList())
        );

        List<OntEntity> res3 = m.ontObjects(OntIndividual.Named.class).collect(Collectors.toList());
        Assertions.assertEquals(List.of("i1", "i2"),
                res3.stream().map(Resource::getURI).sorted().collect(Collectors.toList())
        );

        List<OntIndividual> res5 = m.ontObjects(OntIndividual.Anonymous.class).collect(Collectors.toList());
        Assertions.assertEquals(1, res5.size());
        Assertions.assertTrue(res5.get(0).isAnon());

        Assertions.assertFalse(c1.canAs(OntIndividual.class));
        Assertions.assertFalse(c2.canAs(OntIndividual.class));
        Assertions.assertTrue(i1.canAs(OntIndividual.class));
        Assertions.assertTrue(i2.canAs(OntIndividual.class));
        Assertions.assertTrue(i3.canAs(OntIndividual.class));
    }

    @ParameterizedTest
    @EnumSource(names = {
            "RDFS_MEM",
            "RDFS_MEM_RDFS_INF",
            "RDFS_MEM_TRANS_INF",
    })
    public void testUnsupportedObjects(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst);
        Resource x = m.createResource("x", OWL2.DatatypeProperty);
        Assertions.assertThrows(UnsupportedPolymorphismException.class, () -> x.as(OntDataProperty.class));

        Assertions.assertThrows(OntJenaException.Unsupported.class, () -> m.createDataHasValue(null, null));
    }

    @ParameterizedTest
    @EnumSource(names = {
            "RDFS_MEM",
            "RDFS_MEM_RDFS_INF",
            "RDFS_MEM_TRANS_INF",
    })
    public void testOntClassCastRDFS(TestSpec spec) {
        Model g = ModelFactory.createDefaultModel();
        Resource namedRdfsClass = g.createResource("rdfsClass", RDFS.Class);
        Resource namedRdfsDatatype = g.createResource("rdfsDatatype", RDFS.Datatype);
        Resource namedOwlClass = g.createResource("owlClass", OWL2.Class);
        Resource anonRdfsClass = g.createResource(RDFS.Class);
        Resource anonRdfsDatatype = g.createResource(RDFS.Datatype);
        Resource anonOwlClass = g.createResource(OWL2.Class);
        Resource anonRdfsDomain = g.createResource();
        Resource anonRdfsRange = g.createResource();
        Resource namedRdfsDomain = g.createResource("rdfsDomain");
        Resource namedRdfsRange = g.createResource("rdfsRange");
        g.createResource("p", RDF.Property).addProperty(RDFS.domain, anonRdfsDomain).addProperty(RDFS.range, namedRdfsRange);
        g.createResource(null, RDF.Property).addProperty(RDFS.domain, namedRdfsDomain).addProperty(RDFS.range, anonRdfsRange);

        OntModel m = OntModelFactory.createModel(g.getGraph(), spec.inst).setNsPrefixes(PrefixMapping.Standard);

        if (spec == TestSpec.RDFS_MEM_RDFS_INF) {
            Assertions.assertTrue(
                    m.ontObjects(OntClass.class).map(RDFNode::asResource).collect(Collectors.toSet())
                            .containsAll(Set.of(anonRdfsClass, namedRdfsClass))
            );
        } else {
            Assertions.assertEquals(
                    List.of(anonRdfsClass, namedRdfsClass),
                    m.ontObjects(OntClass.class).sorted(StdModels.RDF_NODE_COMPARATOR).collect(Collectors.toList())
            );
        }
        Stream.of(namedOwlClass, anonOwlClass).forEach(it -> Assertions.assertFalse(it.inModel(m).canAs(OntClass.class)));
        Stream.of(anonRdfsClass, anonRdfsDatatype, anonRdfsDomain, anonRdfsRange)
                .forEach(it ->
                        Assertions.assertTrue(it.inModel(m).canAs(OntClass.class) && !it.inModel(m).canAs(OntClass.Named.class))
                );
        Stream.of(namedRdfsClass, namedRdfsDatatype, namedRdfsDomain, namedRdfsRange)
                .forEach(it -> Assertions.assertTrue(it.inModel(m).canAs(OntClass.Named.class)));
    }

    @Test
    public void testDisabledFeatures() {
        OntModel d = OntModelFactory.createModel();
        d.createOntClass("X")
                .addHasKey(d.createObjectProperty("p"))
                .addDisjointUnion(d.createOntClass("Q"));
        d.createOntClass("Q").addDisjointClass(d.createOntClass("W"));
        d.createOntClass("Q").addEquivalentClass(d.createOntClass("F"));
        d.createResource("X", RDFS.Class);
        d.createResource("Q", RDFS.Class);
        d.createResource("W", RDFS.Class);
        d.createResource("F", RDFS.Class);

        OntModel m = OntModelFactory.createModel(d.getGraph(), OntSpecification.RDFS_MEM);
        OntClass.Named x = m.getOntClass("X");
        OntClass.Named q = m.getOntClass("Q");

        Assertions.assertThrows(OntJenaException.Unsupported.class, () -> x.removeHasKey(m.createList()));
        Assertions.assertEquals(0, x.hasKeys().count());

        Assertions.assertThrows(OntJenaException.Unsupported.class, () -> x.addDisjointUnion(m.createOntClass("Q")));
        Assertions.assertThrows(OntJenaException.Unsupported.class, () -> x.removeDisjointUnion(m.createList()));
        Assertions.assertEquals(0, x.disjointUnions().count());

        Assertions.assertThrows(OntJenaException.Unsupported.class, () -> x.addDisjointClass(q));
        Assertions.assertThrows(OntJenaException.Unsupported.class, () -> x.removeDisjointClass(q));
        Assertions.assertEquals(0, x.disjoints().count());

        Assertions.assertThrows(OntJenaException.Unsupported.class, () -> x.addEquivalentClass(q));
        Assertions.assertThrows(OntJenaException.Unsupported.class, () -> x.removeEquivalentClass(q));
        Assertions.assertEquals(0, x.equivalentClasses().count());
    }

    @Test
    public void testAnnotationProperties() {
        OntModel m = OntModelFactory.createModel(OntSpecification.RDFS_MEM);

        OntClass.Named x = m.createOntClass("X");
        OntClass.Named q = m.createOntClass("Q");

        x.addComment("XXX").addLabel("RRR", "ua");
        OntStatement s1 = x.addAnnotation(m.getRDFSSeeAlso(), m.createResource("http://ex.com#"));
        OntStatement s2 = q.addAnnotation(m.getRDFSIsDefinedBy(), m.createLiteral("http://ex.com#"));

        Assertions.assertEquals(
                Triple.create(x.asNode(), RDFS.seeAlso.asNode(), NodeFactory.createURI("http://ex.com#")), s1.asTriple()
        );
        Assertions.assertEquals(
                Triple.create(q.asNode(), RDFS.isDefinedBy.asNode(), NodeFactory.createLiteralString("http://ex.com#")), s2.asTriple()
        );

        Assertions.assertThrows(OntJenaException.Unsupported.class, () ->
                s2.addAnnotation(m.getRDFSComment(), m.createTypedLiteral(42))
        );

        Assertions.assertEquals(0, m.annotationProperties().count());

        Assertions.assertEquals(6, m.size());
    }

    @Test
    public void testBuiltins() {
        OntModel m = OntModelFactory.createModel(OntSpecification.RDFS_MEM);

        Assertions.assertNotNull(m.getRDFSLabel());
        Assertions.assertNotNull(m.getRDFSComment());
        Assertions.assertNotNull(m.getRDFSSeeAlso());
        Assertions.assertNotNull(m.getRDFSIsDefinedBy());
        Assertions.assertNull(m.getOWLDeprecated());
        Assertions.assertNull(m.getOWLVersionInfo());
        Assertions.assertNull(m.getOWLPriorVersion());
        Assertions.assertNull(m.getOWLBackwardCompatibleWith());
        Assertions.assertNull(m.getOWLIncompatibleWith());
        Assertions.assertNull(m.getOWLThing());
        Assertions.assertNull(m.getOWLNothing());
        Assertions.assertNull(m.getOWLTopDataProperty());
        Assertions.assertNull(m.getOWLTopObjectProperty());
        Assertions.assertNull(m.getOWLBottomDataProperty());
        Assertions.assertNull(m.getOWLBottomObjectProperty());

        Assertions.assertTrue(m.getRDFSLabel().isBuiltIn());
        Assertions.assertTrue(m.getRDFSComment().isBuiltIn());
        Assertions.assertTrue(m.getRDFSSeeAlso().isBuiltIn());
        Assertions.assertTrue(m.getRDFSIsDefinedBy().isBuiltIn());
    }
}
