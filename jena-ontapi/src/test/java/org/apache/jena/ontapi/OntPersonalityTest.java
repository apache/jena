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

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.enhanced.EnhNode;
import org.apache.jena.graph.GraphMemFactory;
import org.apache.jena.graph.Node;
import org.apache.jena.ontapi.common.CommonEnhNodeFactoryImpl;
import org.apache.jena.ontapi.common.EnhNodeFactory;
import org.apache.jena.ontapi.common.EnhNodeFilter;
import org.apache.jena.ontapi.common.EnhNodeFinder;
import org.apache.jena.ontapi.common.EnhNodeProducer;
import org.apache.jena.ontapi.common.OntObjectPersonalityBuilder;
import org.apache.jena.ontapi.common.OntPersonalities;
import org.apache.jena.ontapi.common.OntPersonality;
import org.apache.jena.ontapi.common.OntVocabulary;
import org.apache.jena.ontapi.impl.objects.OntIndividualImpl;
import org.apache.jena.ontapi.model.OntAnnotationProperty;
import org.apache.jena.ontapi.model.OntClass;
import org.apache.jena.ontapi.model.OntDataProperty;
import org.apache.jena.ontapi.model.OntDataRange;
import org.apache.jena.ontapi.model.OntDisjoint;
import org.apache.jena.ontapi.model.OntEntity;
import org.apache.jena.ontapi.model.OntIndividual;
import org.apache.jena.ontapi.model.OntModel;
import org.apache.jena.ontapi.model.OntObject;
import org.apache.jena.ontapi.model.OntObjectProperty;
import org.apache.jena.ontapi.model.OntStatement;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("WeakerAccess")
public class OntPersonalityTest {

    private static OntPersonality buildCustomPersonality() {
        OntPersonality from = TestOntPersonalities.OWL2_PERSONALITY_LAX_PUNNS;
        EnhNodeFactory factory = createNamedIndividualFactory();
        OntPersonality res = OntObjectPersonalityBuilder.from(from)
                .add(OntIndividual.Named.class, factory)
                .remove(OntDataRange.OneOf.class)
                .build();
        Assertions.assertEquals(97, res.types().count());
        List<Class<? extends OntObject>> objects = res.types(OntObject.class).toList();
        List<Class<? extends OntEntity>> entities = res.types(OntEntity.class).toList();
        Assertions.assertEquals(88, objects.size());
        Assertions.assertEquals(8, entities.size());
        return res;
    }

    private static EnhNodeFactory createNamedIndividualFactory() {
        EnhNodeProducer maker = new EnhNodeProducer.Default(IndividualImpl.class) {
            @Override
            public EnhNode newInstance(Node node, EnhGraph eg) {
                return new IndividualImpl(node, eg);
            }
        };
        EnhNodeFinder finder = new EnhNodeFinder.ByPredicate(RDF.type);
        EnhNodeFilter filter = EnhNodeFilter.URI.and(new EnhNodeFilter.HasType(OWL2.NamedIndividual));
        return new CommonEnhNodeFactoryImpl(maker, finder, filter) {
            @Override
            public String toString() {
                return "NamedIndividualFactory";
            }
        };
    }

    @Test
    public void testPersonalityBuiltins() {
        Resource agent = FOAF.Agent;
        Resource document = FOAF.Document;
        String ns = "http://x#";
        Model g = ModelFactory.createDefaultModel()
                .setNsPrefixes(OntModelFactory.STANDARD)
                .setNsPrefix("x", ns)
                .setNsPrefix("foaf", FOAF.NS);
        String clazz = ns + "Class";
        g.createResource(clazz, OWL2.Class).addProperty(RDFS.subClassOf, agent);


        OntPersonality p1 = OntObjectPersonalityBuilder.from(TestOntPersonalities.OWL2_PERSONALITY_STRICT_PUNNS)
                .setBuiltins(OntPersonalities.createBuiltinsVocabulary(OntVocabulary.OWL2_FULL)).build();
        OntModel m1 = OntModelFactory.createModel(g.getGraph(), p1);
        Assertions.assertEquals(1, m1.classes().count());
        Assertions.assertNull(m1.getOntClass(agent));
        Assertions.assertNull(m1.getOntClass(document));
        Assertions.assertEquals(0, m1.getOntClass(clazz).superClasses().count());

        OntVocabulary SIMPLE_FOAF_VOC = OntVocabulary.Impls.create(OWL2.Class, agent, document);
        OntVocabulary voc = OntVocabulary.Impls.create(OntVocabulary.OWL2_FULL, SIMPLE_FOAF_VOC);
        OntPersonality p2 = OntObjectPersonalityBuilder.from(TestOntPersonalities.OWL2_PERSONALITY_STRICT_PUNNS)
                .setBuiltins(OntPersonalities.createBuiltinsVocabulary(voc)).build();
        OntModel m2 = OntModelFactory.createModel(g.getGraph(), p2);

        // listClasses only works with explicit owl-classes, it does not take into account builtins
        Assertions.assertEquals(1, m2.classes().count());
        Assertions.assertNotNull(m2.getOntClass(agent));
        Assertions.assertNotNull(m2.getOntClass(document));
        Assertions.assertEquals(1, m2.getOntClass(clazz).superClasses().count());
    }

    @Test
    public void testPersonalityReserved() {
        String ns = "http://x#";
        Model g = ModelFactory.createDefaultModel()
                .setNsPrefixes(OntModelFactory.STANDARD)
                .setNsPrefix("x", ns);
        Property p = g.createProperty(ns + "someProp");
        Resource individual = g.createResource(ns + "Individual", OWL2.NamedIndividual);
        g.createResource().addProperty(OWL2.sameAs, individual).addProperty(p, "Some assertion");


        OntModel m1 = OntModelFactory.createModel(g.getGraph());
        Assertions.assertEquals(2, m1.ontObjects(OntIndividual.class).count());

        OntVocabulary voc = OntVocabulary.Impls.create(RDF.Property, p);
        OntPersonality p2 = OntObjectPersonalityBuilder.from(TestOntPersonalities.OWL2_PERSONALITY_STRICT_PUNNS)
                .setReserved(OntPersonalities.createReservedVocabulary(voc)).build();
        OntModel m2 = OntModelFactory.createModel(g.getGraph(), p2);
        Assertions.assertEquals(1, m2.ontObjects(OntIndividual.class).count());
    }

    @Test
    public void testPersonalityPunnings() {
        String ns = "http://x#";
        OntModel m1 = OntModelFactory.createModel(GraphMemFactory.createDefaultGraph(), TestOntPersonalities.OWL2_PERSONALITY_STRICT_PUNNS)
                .setNsPrefixes(OntModelFactory.STANDARD)
                .setNsPrefix("x", ns);
        OntClass.Named c1 = m1.createOntClass(ns + "C1");
        OntClass.Named c2 = m1.createOntClass(ns + "C2");
        OntIndividual i1 = c1.createIndividual(ns + "I1");
        OntIndividual i2 = c2.createIndividual(ns + "I2");
        c1.createIndividual(ns + "I3");
        m1.createDatatype(i2.getURI());
        m1.createOntClass(i1.getURI());

        Assertions.assertEquals(3, m1.classes().count());
        Assertions.assertEquals(3, m1.individuals().count());
        Assertions.assertEquals(1, m1.datatypes().count());

        OntPersonality.Punnings punnings = new OntPersonality.Punnings() {
            @Override
            public Set<Node> get(Class<? extends OntObject> type) throws OntJenaException {
                if (OntIndividual.Named.class.equals(type)) {
                    return Set.of(OWL2.Class.asNode(), RDFS.Datatype.asNode());
                }
                if (OntClass.Named.class.equals(type)) {
                    return Set.of(RDFS.Datatype.asNode(), OWL2.NamedIndividual.asNode());
                }
                if (OntDataRange.Named.class.equals(type)) {
                    return Set.of(OWL2.Class.asNode(), OWL2.NamedIndividual.asNode());
                }
                if (OntObjectProperty.Named.class.equals(type)) {
                    return Set.of(OWL2.AnnotationProperty.asNode(), OWL2.DatatypeProperty.asNode());
                }
                if (OntDataProperty.class.equals(type)) {
                    return Set.of(OWL2.AnnotationProperty.asNode(), OWL2.ObjectProperty.asNode());
                }
                if (OntAnnotationProperty.class.equals(type)) {
                    return Set.of(OWL2.ObjectProperty.asNode(), OWL2.DatatypeProperty.asNode());
                }
                throw new AssertionError();
            }

            @Override
            public boolean supports(Class<? extends OntObject> type) {
                return true;
            }
        };
        OntPersonality p2 = OntObjectPersonalityBuilder.from(TestOntPersonalities.OWL2_PERSONALITY_STRICT_PUNNS).setPunnings(punnings).build();
        OntEntity.TYPES.forEach(t -> Assertions.assertEquals(2, p2.getPunnings().get(t).size()));

        OntModel m2 = OntModelFactory.createModel(m1.getGraph(), p2);
        Assertions.assertEquals(1, m2.individuals().count());
        Assertions.assertEquals(0, m2.datatypes().count());
        Assertions.assertEquals(2, m2.classes().count());
    }

    @Test
    public void testClassDatatypePunn() {
        String ns = "http://ex.com#";
        OntModel m = OntModelFactory.createModel(OntModelFactory.createDefaultGraph(), TestOntPersonalities.OWL2_PERSONALITY_LAX_PUNNS)
                .setNsPrefixes(OntModelFactory.STANDARD);
        OntClass c1 = m.createOntClass(ns + "class1");
        OntClass c2 = m.createOntClass(ns + "class2");
        OntIndividual i1 = c1.createIndividual(ns + "indi1");
        OntIndividual i2 = m.createObjectComplementOf(c1).createIndividual(ns + "individual2");
        OntIndividual i3 = c2.createIndividual(ns + "individual3");
        m.createDifferentIndividuals(i1, i2, i3);

        Assertions.assertEquals(0, m.datatypes().count());
        Assertions.assertEquals(2, m.classes().count());
        Assertions.assertEquals(1, m.ontObjects(OntDisjoint.Individuals.class).count());
        Assertions.assertEquals(3, m.ontObjects(OntClass.class).count());
        // add punn:
        m.createDatatype(ns + "class1");
        OntModel m2 = OntModelFactory.createModel(m.getBaseGraph(), TestOntPersonalities.OWL2_PERSONALITY_STRICT_PUNNS);

        try {
            m2.createDatatype(ns + "class2");
            Assertions.fail("Possible to add punn");
        } catch (OntJenaException e) {
            // expected
        }

        Assertions.assertEquals(0, m2.datatypes().count());
        Assertions.assertEquals(1, m2.classes().count());
        Assertions.assertEquals(1, m2.ontObjects(OntDisjoint.Individuals.class).count());
        List<OntClass> ces = m2.ontObjects(OntClass.class).toList();
        Assertions.assertEquals(1, ces.size(), "Wrong ces list: " + ces);
        OntModel m3 = OntModelFactory.createModel(m2.getBaseGraph(), TestOntPersonalities.OWL2_PERSONALITY_LAX_PUNNS);
        Assertions.assertEquals(1, m3.datatypes().count());
        Assertions.assertEquals(2, m3.classes().count());
        Assertions.assertEquals(1, m3.ontObjects(OntDisjoint.Individuals.class).count());
        Assertions.assertEquals(3, m3.ontObjects(OntClass.class).count());
    }

    @Test
    public void testPropertyPunn() {
        String ns = "http://ex.com#";
        OntModel m = OntModelFactory.createModel(OntModelFactory.createDefaultGraph(), TestOntPersonalities.OWL2_PERSONALITY_LAX_PUNNS)
                .setNsPrefixes(OntModelFactory.STANDARD);
        OntClass c1 = m.createOntClass(ns + "class1");
        OntObjectProperty.Named p1 = m.createObjectProperty(ns + "prop1");
        OntObjectProperty p2 = m.createObjectProperty(ns + "prop2").createInverse();

        OntIndividual i1 = c1.createIndividual(ns + "individual1");
        OntIndividual i2 = m.createObjectComplementOf(c1).createIndividual(ns + "individual2");
        OntIndividual i3 = m.createObjectSomeValuesFrom(p1, c1).createIndividual(ns + "individual3");
        OntIndividual i4 = m.createObjectCardinality(p2, 1, c1).createIndividual(ns + "individual4");
        m.createDifferentIndividuals(i1, i2, i3, i4);

        Assertions.assertEquals(0, m.dataProperties().count());
        Assertions.assertEquals(0, m.annotationProperties().count());
        Assertions.assertEquals(2, m.objectProperties().count());
        Assertions.assertEquals(1, m.classes().count());
        Assertions.assertEquals(4, m.ontObjects(OntIndividual.class).count());
        Assertions.assertEquals(4, m.ontObjects(OntClass.class).count());
        // add punns:
        m.createDataProperty(ns + "prop1");
        m.createAnnotationProperty(ns + "prop2");
        OntModel m2 = OntModelFactory.createModel(m.getBaseGraph(), TestOntPersonalities.OWL2_PERSONALITY_STRICT_PUNNS);

        try {
            m2.createDataProperty(ns + "prop2");
            Assertions.fail("Possible to add punn");
        } catch (OntJenaException e) {
            // expected
        }

        Assertions.assertEquals(0, m2.objectProperties().count());
        Assertions.assertEquals(0, m2.dataProperties().count());
        Assertions.assertEquals(0, m2.annotationProperties().count());
        List<OntClass> ces = m2.ontObjects(OntClass.class).toList();
        // no ObjectSomeValuesFrom, no ObjectCardinality
        Assertions.assertEquals(2, ces.size(), "Wrong ces list: " + ces);
        OntModel m3 = OntModelFactory.createModel(m2.getBaseGraph(), TestOntPersonalities.OWL2_PERSONALITY_LAX_PUNNS);
        Assertions.assertEquals(1, m3.dataProperties().count());
        Assertions.assertEquals(1, m3.annotationProperties().count());
        Assertions.assertEquals(2, m3.objectProperties().count());
        Assertions.assertEquals(1, m3.ontObjects(OntDisjoint.Individuals.class).count());
        Assertions.assertEquals(4, m3.ontObjects(OntClass.class).count());
    }

    @Test
    public void testCustomPersonality() {
        OntPersonality personality = buildCustomPersonality();

        String ns = "http://ex.com#";
        OntModel m = OntModelFactory.createModel(OntModelFactory.createDefaultGraph(), TestOntPersonalities.OWL2_PERSONALITY_LAX_PUNNS)
                .setNsPrefixes(OntModelFactory.STANDARD); // STANDARD PERSONALITY
        OntClass c1 = m.createOntClass(ns + "class1");
        OntDataProperty p1 = m.createDataProperty(ns + "prop1");
        OntDataRange.Named d1 = m.createDatatype(ns + "dt1");
        OntClass c2 = m.createDataAllValuesFrom(p1, d1);

        c1.createIndividual();
        c1.createIndividual(ns + "individual1");
        c2.createIndividual(ns + "individual2");
        m.createResource(ns + "individual3", c2);
        m.createResource(ns + "individual4", c1);

        Assertions.assertEquals(2, m.namedIndividuals().count());
        Assertions.assertEquals(5, m.ontObjects(OntIndividual.class).count());
        Assertions.assertEquals(5, m.individuals().count());

        // custom (owl:NamedIndividual is required)
        OntModel m2 = OntModelFactory.createModel(m.getGraph(), personality);
        Assertions.assertEquals(2, m2.namedIndividuals().count());
        Assertions.assertEquals(3, m2.ontObjects(OntIndividual.class).count());
        Resource individual5 = m.createResource(ns + "inid5", c2);
        Assertions.assertEquals(3, m2.ontObjects(OntIndividual.class).count());
        Assertions.assertEquals(3, m2.individuals().count());
        // no OneOf DR
        Assertions.assertThrows(OntJenaException.Unsupported.class, () -> m2.createDataOneOf(m.createTypedLiteral(42)));

        OntDisjoint.Individuals disjoint2 = m2.createDifferentIndividuals(m2.ontObjects(OntIndividual.class)
                .collect(Collectors.toList()));
        disjoint2.getList().as(RDFList.class).add(individual5);

        Assertions.assertEquals(3, disjoint2.members().count());

        // back to standard personality
        OntModel m3 = OntModelFactory.createModel(m2.getGraph(),
                TestOntPersonalities.OWL2_PERSONALITY_MEDIUM_PUNNS
        );
        Assertions.assertEquals(2, m3.namedIndividuals().count());
        Assertions.assertEquals(6, m3.ontObjects(OntIndividual.class).count());
        OntDisjoint.Individuals disjoint3 = m3.ontObjects(OntDisjoint.Individuals.class).findFirst()
                .orElseThrow(AssertionError::new);
        Assertions.assertEquals(4, disjoint3.members().count());
    }

    /**
     * Named individual that requires explicit {@code _:x rdf:type owl:NamedIndividual} declaration, just only class.
     */
    public static class IndividualImpl extends OntIndividualImpl.NamedImpl {
        private IndividualImpl(Node n, EnhGraph m) {
            super(n, m);
        }

        @Override
        public Optional<OntStatement> findRootStatement() {
            return getRequiredRootStatement(this, OWL2.NamedIndividual);
        }
    }
}
