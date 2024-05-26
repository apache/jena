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

import org.apache.jena.ontapi.impl.OntGraphModelImpl;
import org.apache.jena.ontapi.impl.objects.OntStatementImpl;
import org.apache.jena.ontapi.model.OntAnnotation;
import org.apache.jena.ontapi.model.OntAnnotationProperty;
import org.apache.jena.ontapi.model.OntClass;
import org.apache.jena.ontapi.model.OntDataProperty;
import org.apache.jena.ontapi.model.OntDataRange;
import org.apache.jena.ontapi.model.OntDisjoint;
import org.apache.jena.ontapi.model.OntID;
import org.apache.jena.ontapi.model.OntIndividual;
import org.apache.jena.ontapi.model.OntModel;
import org.apache.jena.ontapi.model.OntNegativeAssertion;
import org.apache.jena.ontapi.model.OntObject;
import org.apache.jena.ontapi.model.OntObjectProperty;
import org.apache.jena.ontapi.model.OntStatement;
import org.apache.jena.ontapi.utils.OntModels;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.util.iterator.UniqueFilter;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * To test annotated statements ({@link OntStatement}) and annotations within ont objects ({@link OntObject}).
 */
public class OntAnnotationsTest {

    @Test
    public void testCreatePlainAnnotations() {
        String uri = "http://test.com/graph/1";
        String ns = uri + "#";

        OntModel m = OntModelFactory.createModel().setNsPrefix("test", ns).setNsPrefixes(OntModelFactory.STANDARD);

        // 1) Assign version-iri and ontology comment.
        m.setID(uri).setVersionIRI(ns + "1.0.1");
        String comment = "Some comment";
        m.getID().addComment(comment, "fr");
        Assertions.assertEquals(comment, m.getID().getComment());
        Assertions.assertEquals(comment, m.getID().getComment("fr"));
        Assertions.assertEquals(1, m.getID().annotations().count(), "Should be one header annotation");

        // 2) Create class with two labels
        OntClass cl = m.createOntClass(ns + "ClassN1").addLabel("some label");
        OntStatement label2 = cl.addAnnotation(m.getRDFSLabel(), "another label", "de");

        Assertions.assertEquals("some label", cl.getLabel(""));
        Assertions.assertEquals("another label", cl.getLabel("de"));
        Assertions.assertEquals(2, m.listObjectsOfProperty(cl, RDFS.label).toList().size(), "Incorrect count of labels.");

        // 3) Annotate annotation `label2`
        OntStatement seeAlsoForLabel2 = label2.addAnnotation(m.getAnnotationProperty(RDFS.seeAlso),
                ResourceFactory.createResource("http://see.also/1"));
        OntStatement labelForLabel2 = label2.addAnnotation(m.getRDFSLabel(),
                ResourceFactory.createPlainLiteral("label"));

        Assertions.assertTrue(m.contains(null, RDF.type, OWL2.Axiom));
        Assertions.assertFalse(m.contains(null, RDF.type, OWL2.Annotation));

        // 4) Create annotation property and annotate `seeAlsoForLabel2` and `labelForLabel2`
        OntAnnotationProperty nap1 = m.createAnnotationProperty(ns + "annotation-prop-1");
        seeAlsoForLabel2.addAnnotation(nap1, ResourceFactory.createPlainLiteral("comment to see also"));
        OntStatement annotationForLabelForLabel2 = labelForLabel2.addAnnotation(nap1,
                ResourceFactory.createPlainLiteral("comment to see label"));

        Assertions.assertEquals(2, m.listStatements(null, RDF.type, OWL2.Annotation)
                .filterKeep(s -> !m.contains(null, null, s.getSubject()))
                .filterKeep(new UniqueFilter<>()).toList().size());
        Assertions.assertEquals(2, m.listStatements(null, RDF.type, OWL2.Annotation).toList().size());
        Assertions.assertEquals(1, m.listStatements(null, RDF.type, OWL2.Axiom).toList().size());

        // 5) Delete annotations for `labelForLabel2`
        labelForLabel2.deleteAnnotation(annotationForLabelForLabel2.getPredicate().as(OntAnnotationProperty.class),
                annotationForLabelForLabel2.getObject());

        Assertions.assertEquals(1, m.listStatements(null, RDF.type, OWL2.Annotation)
                .filterKeep(s -> !m.contains(null, null, s.getSubject()))
                .filterKeep(new UniqueFilter<>()).toList().size());
        Assertions.assertEquals(1, m.listStatements(null, RDF.type, OWL2.Annotation).toList().size());
        Assertions.assertEquals(1, m.listStatements(null, RDF.type, OWL2.Axiom).toList().size());


        // 6) Delete all annotations for `label2`
        label2.clearAnnotations();

        Assertions.assertEquals(2, m.listObjectsOfProperty(cl, RDFS.label).toList().size());
        Assertions.assertFalse(m.contains(null, RDF.type, OWL2.Axiom));
        Assertions.assertFalse(m.contains(null, RDF.type, OWL2.Annotation));

        // 7) Annotate sub-class-of
        OntStatement subClassOf = cl.addSubClassOfStatement(m.getOWLThing());
        OntStatement subClassOfAnnotation = subClassOf
                .addAnnotation(nap1, ResourceFactory.createPlainLiteral("test"));
        subClassOfAnnotation.addAnnotation(m.getRDFSLabel(), ResourceFactory.createPlainLiteral("test2"))
                .addAnnotation(m.getRDFSComment(), ResourceFactory.createPlainLiteral("test3"));


        Assertions.assertEquals(2, m.listStatements(null, RDF.type, OWL2.Annotation).toList().size());
        Assertions.assertEquals(1, m.listStatements(null, RDF.type, OWL2.Axiom).toList().size());
        Assertions.assertEquals(2, cl.annotations().count());

        // 8) Deleter all annotations for class `cl`

        //noinspection MappingBeforeCount
        Assertions.assertEquals(2, cl.content().peek(OntStatement::clearAnnotations).count());

        Assertions.assertEquals(0, cl.annotations().count(), "Found annotations for class " + cl);
        Assertions.assertFalse(m.contains(null, RDF.type, OWL2.Axiom));
        Assertions.assertFalse(m.contains(null, RDF.type, OWL2.Annotation));
    }

    @Test
    public void testCreateAnonAnnotations() {
        String uri = "http://test.com/graph/2";
        String ns = uri + "#";

        OntModel m = OntModelFactory.createModel()
                .setNsPrefix("test", ns)
                .setNsPrefixes(OntModelFactory.STANDARD)
                .setID(uri)
                .getModel();

        OntClass cl1 = m.createOntClass(ns + "Class1");
        OntClass cl2 = m.createOntClass(ns + "Class2");
        OntClass cl3 = m.createOntClass(ns + "Class3");
        OntAnnotationProperty nap1 = m.createAnnotationProperty(ns + "AnnotationProperty1");

        OntDisjoint.Classes disjointClasses = m.createDisjointClasses(cl1, cl2, cl3);
        Assertions.assertEquals(1, m.ontObjects(OntDisjoint.Classes.class).count());

        disjointClasses.addLabel("label1", "en");
        disjointClasses.addAnnotation(m.getRDFSLabel(), "comment", "xxxxxxxx")
                .annotate(nap1, ResourceFactory.createTypedLiteral("some txt"));

        Assertions.assertEquals(2, disjointClasses.as(OntAnnotation.class).assertions().count());
        Assertions.assertEquals(2, disjointClasses.as(OntAnnotation.class)
                .annotations().count());
        Assertions.assertEquals(3, OntModels.annotations(disjointClasses.getMainStatement()).count());
        Assertions.assertNull(disjointClasses.as(OntAnnotation.class).getBase());

        Assertions.assertFalse(m.contains(null, RDF.type, OWL2.Axiom));
        Assertions.assertEquals(1, m.listStatements(null, RDF.type, OWL2.Annotation).toList().size());

        OntObjectProperty nop1 = m.createObjectProperty(ns + "ObjectProperty1");
        OntIndividual ind1 = cl1.createIndividual(ns + "Individual1");
        OntIndividual.Anonymous ind2 = cl2.createIndividual();
        ind2.addComment("anonymous individual", "ru");
        Assertions.assertEquals("anonymous individual", ind2.getComment("ru"));
        OntNegativeAssertion.WithObjectProperty nopa = nop1.addNegativeAssertion(ind1, ind2);
        Assertions.assertEquals(1, nop1.negativeAssertions().count());
        nopa.addAnnotation(m.getRDFSLabel(), "label1")
                .addAnnotation(m.getRDFSLabel(), ResourceFactory.createTypedLiteral("label2"))
                .addAnnotation(m.getRDFSLabel(), ResourceFactory.createPlainLiteral("label3"));
        Assertions.assertEquals(3, m.listStatements(null, RDF.type, OWL2.Annotation).toList().size());
        Assertions.assertEquals("label1", nopa.getLabel(""));


        Assertions.assertEquals(2, m.ontObjects(OntAnnotation.class).count());
        OntStatement disjointWith = cl1.addDisjointWithStatement(cl3);
        Assertions.assertFalse(disjointWith.asAnnotationResource().isPresent(), "No annotation resource is expected.");
        disjointWith.addAnnotation(m.getAnnotationProperty(OWL2.deprecated), "disjoint with comment N1", null)
                .addAnnotation(m.getAnnotationProperty(OWL2.incompatibleWith), "disjoint with comment N2", "rur");

        Assertions.assertTrue(disjointWith.asAnnotationResource().isPresent(), "Should be annotation resource");
        Assertions.assertEquals(3, m.ontObjects(OntAnnotation.class).count());
    }

    @Test
    public void testRemoveAnnotations() {
        // Create model
        OntModel m = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD);
        m.getID().addAnnotation(m.getAnnotationProperty(OWL2.versionInfo), "anonymous ontology", "en");

        OntDataProperty p = m.createDataProperty("x");
        OntClass c = m.createOntClass("c");
        OntDataRange dt = m.getDatatype(RDFS.Literal);
        p.addRangeStatement(dt).addAnnotation(m.getRDFSComment(), "This is a range", null);
        p.addDomainStatement(c).addAnnotation(m.getRDFSLabel(), "This is a domain", null)
                .addAnnotation(m.getRDFSLabel(), "label", "hg");

        OntIndividual i = c.createIndividual("i");
        p.addNegativeAssertion(i, ResourceFactory.createPlainLiteral("test"))
                .addAnnotation(m.getRDFSComment(), "This is a negative data property assertion", null)
                .addAnnotation(m.getRDFSLabel(), "Label", "lk");


        // Remove annotated components
        OntNegativeAssertion.WithDataProperty assertion = p.negativeAssertions(i).findFirst().orElseThrow(AssertionError::new);
        OntStatement domain = m.statements(null, RDFS.domain, null).findFirst().orElseThrow(AssertionError::new);
        OntStatement range = m.statements(null, RDFS.range, null).findFirst().orElseThrow(AssertionError::new);

        m.removeOntObject(assertion).removeOntStatement(domain).removeOntStatement(range);

        Assertions.assertEquals(6, m.statements().count());
    }

    @Test
    public void testBuiltInsAnnotations() {
        OntModel m = OntModelFactory.createModel();
        String comment = "This is the Thing";
        m.getOWLThing().addComment(comment);
        Assertions.assertEquals(comment, m.getOWLThing().getComment());

        Assertions.assertEquals(1, m.size());
        Assertions.assertEquals(1, m.statements().count());
        Assertions.assertEquals(1, m.getOWLThing().statements().count());
        Assertions.assertEquals(1, m.getOWLThing().annotations().count());
        m.getOWLThing().annotations().forEach(s -> Assertions.assertFalse(s.hasAnnotations()));
        Assertions.assertEquals(0, m.getOWLNothing().annotations().count());
        Assertions.assertEquals(0, m.getOWLNothing().statements().count());

        m.getOWLThing().clearAnnotations();
        Assertions.assertTrue(m.isEmpty());
        m.getOWLBottomDataProperty()
                .addSubPropertyOfStatement(m.getOWLTopDataProperty())
                .annotate(m.getRDFSComment(), "Some sub-property-of");

        m.getOWLBottomDataProperty().addComment("x");
        Assertions.assertEquals("x", m.getOWLBottomDataProperty().getComment());

        Assertions.assertEquals(1, m.getOWLBottomDataProperty().annotations().count());
        Assertions.assertEquals(2, m.getOWLBottomDataProperty().statements().count());
        Assertions.assertEquals(7, m.statements().count());
        m.localStatements(OWL2.bottomDataProperty, RDFS.subPropertyOf, OWL2.topDataProperty)
                .findFirst().orElseThrow(AssertionError::new).clearAnnotations();
        Assertions.assertEquals(1, m.getOWLBottomDataProperty().annotations().count());
        Assertions.assertEquals(2, m.getOWLBottomDataProperty().statements().count());
        Assertions.assertEquals(2, m.size());
        m.getOWLBottomDataProperty().clearAnnotations();
        Assertions.assertEquals(0, m.getOWLBottomDataProperty().annotations().count());
        Assertions.assertEquals(1, m.size());

        m.getOWLTopObjectProperty()
                .addAnnotation(m.getRDFSComment(), "Top Object Property")
                .addAnnotation(m.getRDFSLabel(), "lab");

        Assertions.assertEquals(1, m.ontObjects(OntAnnotation.class).count());
        Assertions.assertEquals(1, m.getOWLTopObjectProperty().annotations().count());

    }

    @Test
    public void testListObjectAnnotations() {
        OntModel m = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD);
        OntClass clazz = m.createOntClass("C").addComment("xxx");
        Assertions.assertEquals("xxx", clazz.getComment());


        Assertions.assertEquals(2, m.size());
        Assertions.assertEquals(2, m.statements().count());
        Assertions.assertEquals(2, clazz.statements().count());
        Assertions.assertEquals(1, clazz.annotations().count());
        clazz.addAnnotation(m.getRDFSComment(), "yyy").addAnnotation(m.getRDFSLabel(), "zzz");


        Assertions.assertEquals("yyy", clazz.getComment());
        Assertions.assertEquals(2, clazz.annotations().count());
        m.statements(clazz, RDF.type, OWL2.Class)
                .findFirst()
                .orElseThrow(AssertionError::new)
                .addAnnotation(m.getRDFSComment(), "kkk");

        Assertions.assertEquals(3, clazz.annotations().count());

        clazz.addSubClassOfStatement(m.getOWLThing()).addAnnotation(m.getRDFSComment(), "mmm")
                .addAnnotation(m.getRDFSComment(), "ggg");

        Assertions.assertEquals(3, m.classes().findFirst().orElseThrow(AssertionError::new).annotations().count());

        Assertions.assertEquals(24, m.size());
        Model model = ModelFactory.createModelForGraph(m.getBaseGraph());
        Assertions.assertEquals(3, model.listStatements(null, RDF.type, OWL2.Axiom).toList().size());
        Assertions.assertEquals(1, model.listStatements(null, RDF.type, OWL2.Annotation).toList().size());
    }

    @Test
    public void testClearAnnotations() {
        OntModel m = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD);
        OntAnnotationProperty nap = m.createAnnotationProperty("nap");
        nap.addAnnotation(m.getRDFSComment(), "test1").addAnnotation(nap, "sub-test1");
        m.asStatement(nap.getMainStatement().asTriple()).addAnnotation(m.getRDFSComment(), "test2")
                .addAnnotation(m.getRDFSLabel(), "sub-test2");

        OntStatement subPropertyOf = nap.addSubPropertyOfStatement(m.getOWLBottomDataProperty()
                .addProperty(RDF.type, OWL2.AnnotationProperty).as(OntAnnotationProperty.class));
        subPropertyOf.addAnnotation(m.getRDFSLabel(), "test3")
                .addAnnotation(m.getRDFSLabel(), "sub-test3")
                .addAnnotation(m.getRDFSLabel(), "sub-sub-test3");

        Assertions.assertTrue(nap.getMainStatement().hasAnnotations());
        Assertions.assertEquals(2, nap.annotations().count());
        Assertions.assertEquals(2, nap.annotations().mapToLong(a -> a.annotations().count()).sum());

        nap.clearAnnotations();

        Assertions.assertEquals(0, nap.annotations().count());
        Assertions.assertTrue(subPropertyOf.hasAnnotations());
        Assertions.assertEquals(1, subPropertyOf.annotations().count());
        Assertions.assertEquals(1, subPropertyOf.annotations().mapToLong(a -> a.annotations().count()).sum());

        Assertions.assertSame(subPropertyOf, subPropertyOf.clearAnnotations());

        Assertions.assertFalse(subPropertyOf.hasAnnotations());
        Assertions.assertEquals(3, m.size());
    }


    @Test
    public void testRemoveAnnotatedObject() {
        OntModel m = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD);

        OntClass class1 = m.createOntClass("C-1");
        m.asStatement(class1.getMainStatement().asTriple()).addAnnotation(m.getRDFSComment(), "Class1::1")
                .getSubject(OntAnnotation.class).getBase()
                .getSubject(OntClass.Named.class)
                .addAnnotation(m.getRDFSComment(), "Class1::2");
        long size1 = m.size();

        OntClass class2 = m.createObjectComplementOf(class1);
        class2.addAnnotation(m.getRDFSComment(), "Class2::1")
                .addAnnotation(m.getRDFSComment(), "Class2::1::1")
                .addAnnotation(m.getRDFSComment(), "Class2::1::1::1");
        class2.addAnnotation(m.getRDFSComment(), "Class2::2")
                .addAnnotation(m.getRDFSComment(), "Class2::2::1")
                .addAnnotation(m.getRDFSComment(), "Class2::2::1::1");
        long size2 = m.size();

        OntClass class3 = m.createObjectIntersectionOf(class2, m.getOWLNothing());
        class3.addAnnotation(m.getRDFSComment(), "Class3::1").addAnnotation(m.getRDFSComment(), "Class3::1::1");
        class3.statements().filter(OntStatement::isAnnotationAssertion).findFirst().orElseThrow(AssertionError::new)
                .addAnnotation(m.getRDFSComment(), "Class3::2").addAnnotation(m.getRDFSComment(), "Class3::2::1");
        class3.addDisjointWithStatement(class1).annotate(m.getRDFSComment(), "class2 disjoint with class1");
        class3.addDisjointWithStatement(m.getOWLNothing()).annotate(m.getRDFSComment(), "class2 disjoint with nothing");


        m.removeOntObject(class3);

        Assertions.assertEquals(size2, m.size());

        m.removeOntObject(class2);

        Assertions.assertEquals(size1, m.size());

        m.removeOntObject(class1);

        Assertions.assertTrue(m.isEmpty());
    }

    @Test
    public void testAnnotationFunctionality() {
        OntModel m = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD);
        Resource r = m.createResource("A").addProperty(m.createProperty("B"), "C");
        OntStatement base = m.statements(r, null, null).findFirst().orElseThrow(AssertionError::new);

        Literal literal_1 = m.createLiteral("annotation-1");
        Literal literal_2 = m.createLiteral("annotation-2");
        Literal literal_3 = m.createLiteral("annotation-3");

        OntStatement s1 = base.addAnnotation(m.getRDFSLabel(), literal_1);
        Assertions.assertTrue(s1.getSubject().isAnon());
        OntStatement s3 = base.addAnnotation(m.getRDFSLabel(), literal_2).addAnnotation(m.getRDFSLabel(), literal_3);
        OntAnnotation annotation = m.ontObjects(OntAnnotation.class).findFirst().orElseThrow(AssertionError::new);
        Assertions.assertEquals(base, annotation.getBase());
        Assertions.assertFalse(annotation.parent().isPresent());
        Assertions.assertEquals(2, annotation.annotations().count());
        //noinspection MappingBeforeCount
        Assertions.assertEquals(1, annotation.descendants()
                .peek(x -> Assertions.assertEquals(annotation, x.parent().orElseThrow(AssertionError::new)))
                .count());
        Assertions.assertEquals(annotation, base.asAnnotationResource().orElseThrow(AbstractMethodError::new));
        long size = m.size();
        // has anonymous resources in the model cache:
        Assertions.assertNotNull(((OntGraphModelImpl) m).getNodeAs(s1.getSubject().asNode(), OntAnnotation.class));
        Assertions.assertNotNull(((OntGraphModelImpl) m).getNodeAs(s3.getSubject().asNode(), OntAnnotation.class));


        // attempt to delete annotation with children:
        try {
            base.deleteAnnotation(m.getRDFSLabel(), literal_2);
            Assertions.fail("Expected error");
        } catch (OntJenaException j) {
            // expected
        }
        Assertions.assertEquals(2, base.annotations().count());
        Assertions.assertEquals(size, m.statements().count());

        // success deletion `annotation-1`:
        Assertions.assertEquals("C", base.deleteAnnotation(s1.getPredicate().as(OntAnnotationProperty.class),
                s1.getObject()).getObject().asLiteral().getLexicalForm());

        Assertions.assertEquals(1, base.annotations().count());
        OntStatement s2 = annotation.annotations().findFirst().orElseThrow(AssertionError::new);
        Assertions.assertEquals(literal_2, s2.getObject());
        size = m.size();

        // no deletion, no error
        base.deleteAnnotation(s3.getPredicate().as(OntAnnotationProperty.class), literal_3);

        Assertions.assertEquals(1, base.annotations().count());
        Assertions.assertEquals(size, m.statements().count());

        // attempt to delete assertions from annotation object
        try {
            annotation.getMainStatement().deleteAnnotation(s3.getPredicate().as(OntAnnotationProperty.class));
            Assertions.fail("Expected error");
        } catch (OntJenaException j) {
            // expected
        }
        Assertions.assertEquals(size, m.statements().count());

        // delete 'annotation-3' and then annotation-2
        Assertions.assertEquals(0, s2.deleteAnnotation(m.getRDFSLabel(), literal_3)
                .getSubject(OntAnnotation.class).descendants().count());
        Assertions.assertEquals(0, annotation.descendants().count());
        m.removeOntObject(annotation);

        Assertions.assertEquals(1, m.size());
    }

    @Test
    public void testAssemblyAnnotations() {
        Model m = ModelFactory.createDefaultModel().setNsPrefixes(OntModelFactory.STANDARD);
        Property b = m.createProperty("B");
        Literal c = m.createLiteral("C");
        Resource a = m.createResource("A").addProperty(b, c);

        Resource an1 = m.createResource(OWL2.Axiom)
                .addProperty(RDFS.comment, "annotation-1")
                .addProperty(OWL2.annotatedProperty, b)
                .addProperty(OWL2.annotatedSource, a)
                .addProperty(OWL2.annotatedTarget, c);
        m.createResource(OWL2.Axiom)
                .addProperty(RDFS.comment, "annotation-2")
                .addProperty(OWL2.annotatedProperty, b)
                .addProperty(OWL2.annotatedSource, a)
                .addProperty(OWL2.annotatedTarget, c);
        m.createResource(OWL2.Annotation)
                .addProperty(RDFS.comment, "annotation-3")
                .addProperty(OWL2.annotatedProperty, RDFS.comment)
                .addProperty(OWL2.annotatedSource, an1)
                .addProperty(OWL2.annotatedTarget, m.listObjectsOfProperty(an1, RDFS.comment).toList().get(0));


        OntModel model = OntModelFactory.createModel(m.getGraph());
        Assertions.assertEquals(2, model.ontObjects(OntAnnotation.class).count());
        OntStatement base = model.statements(a, b, c).findFirst().orElseThrow(AssertionError::new);
        Assertions.assertEquals(2, base.annotations().count());
        base.annotate(model.getRDFSLabel(), "com-1").annotate(model.getRDFSLabel(), "com-2");


        Assertions.assertEquals(an1, base.asAnnotationResource().orElseThrow(AssertionError::new));
        Assertions.assertEquals(3, an1.inModel(model).as(OntAnnotation.class).annotations().count());
        //noinspection MappingBeforeCount
        Assertions.assertEquals(1, an1.inModel(model).as(OntAnnotation.class)
                .descendants()
                .peek(x -> Assertions.assertEquals(an1, x.parent().orElseThrow(AssertionError::new)))
                .count());

        Assertions.assertEquals(2, model.statements(null, RDF.type, OWL2.Axiom).count());
        Assertions.assertEquals(1, model.statements(null, RDF.type, OWL2.Annotation).count());
    }

    @Test
    public void testListAnnotationValues() {
        OntModel m = OntModelFactory.createModel();
        OntClass c = m.createOntClass("http://clazz")
                .addComment("c1", "en")
                .addComment("c2", "EN-GB")
                .addComment("c3", "pt")
                .addComment("c4")
                .annotate(m.getRDFSComment(), m.createResource("http://sss"))
                .addLabel("l1", "en")
                .addLabel("l2", "ru");
        Assertions.assertEquals(5, c.annotationValues(m.getRDFSComment()).count());
        Assertions.assertEquals(2, c.annotationValues(m.getRDFSLabel()).count());

        Assertions.assertEquals(4, c.annotationValues(m.getRDFSComment(), null).count());
        Assertions.assertEquals(2, c.annotationValues(m.getRDFSLabel(), null).count());

        Assertions.assertEquals(0, c.annotationValues(m.getRDFSComment(), "ru").count());
        Assertions.assertEquals(1, c.annotationValues(m.getRDFSComment(), "pt").count());
        Assertions.assertEquals(1, c.annotationValues(m.getRDFSComment(), "en-gb").count());
        Assertions.assertEquals(1, c.annotationValues(m.getRDFSComment(), "").count());
        Assertions.assertEquals(2, c.annotationValues(m.getRDFSComment(), "en").count());

        Assertions.assertEquals(0, c.annotationValues(m.getRDFSLabel(), "en-gb").count());
        Assertions.assertEquals(1, c.annotationValues(m.getRDFSLabel(), "en").count());
        Assertions.assertEquals(1, c.annotationValues(m.getRDFSLabel(), "ru").count());
    }

    @Test
    public void testAddAnnotations() {
        OntModel m = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD);
        OntClass c = m.createOntClass("C");
        OntStatementImpl s2 = (OntStatementImpl) c.addSubClassOfStatement(m.getOWLNothing());
        OntStatementImpl s1 = (OntStatementImpl) c.getMainStatement();


        Assertions.assertTrue(s1.isRootStatement());
        Assertions.assertFalse(s2.isRootStatement());
        Assertions.assertFalse(s1.belongsToAnnotation());
        Assertions.assertFalse(s2.belongsToAnnotation());

        OntStatementImpl a1 = (OntStatementImpl) s1.addAnnotation(m.getRDFSComment(), "x");

        Assertions.assertFalse(a1.belongsToAnnotation());
        Assertions.assertFalse(a1.isRootStatement());

        OntStatementImpl a2 = (OntStatementImpl) s2.addAnnotation(m.getRDFSComment(), "y");

        Assertions.assertTrue(a2.belongsToAnnotation());
        Assertions.assertFalse(a2.isRootStatement());
    }

    @Test
    public void testHeaderAnnotations() {
        OntModel m = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD);
        OntID id = m.getID().addVersionInfo("v1", "e").addVersionInfo("v2").addComment("com1", "e").addComment("com2");
        Assertions.assertEquals("v2", id.getVersionInfo(""));
        Assertions.assertEquals("v1", id.getVersionInfo("e"));
        Assertions.assertEquals("com1", id.getComment("e"));
        Assertions.assertEquals("com2", id.getComment(""));
    }

    @Test
    public void testAnnotationSplitting() {
        OntModel m = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD);
        OntClass clazz = m.createOntClass("A");
        OntStatement subClassOf = clazz.addSubClassOfStatement(m.getOWLThing());

        Assertions.assertEquals(0, subClassOf.annotations().count());
        subClassOf.addAnnotation(m.getRDFSLabel(), "label1").addAnnotation(m.getRDFSComment(), "comment1");
        OntStatement sub = subClassOf.addAnnotation(m.getRDFSLabel(), "label2")
                .addAnnotation(m.getRDFSComment(), "comment2");
        sub.addAnnotation(m.getRDFSLabel(), "label3");
        sub.addAnnotation(m.getRDFSLabel(), "label4");
        Assertions.assertEquals(2, subClassOf.annotations().count());
        Assertions.assertEquals(1, OntModels.listSplitStatements(subClassOf).toList().size());
        Assertions.assertEquals(6, OntModels.annotations(subClassOf).count());
        Assertions.assertEquals(2, OntModels.annotations(sub).count());

        sub.deleteAnnotation(m.getRDFSLabel(), m.createLiteral("label4"));
        Assertions.assertEquals(5, OntModels.annotations(subClassOf).count());

        Resource annotation = m.createResource(null, OWL2.Axiom);
        Assertions.assertEquals(1, OntModels.listSplitStatements(subClassOf).toList().size());
        annotation.addProperty(OWL2.annotatedSource, clazz)
                .addProperty(OWL2.annotatedProperty, RDFS.subClassOf)
                .addProperty(OWL2.annotatedTarget, OWL2.Thing);

        List<OntStatement> split1 = OntModels.listSplitStatements(subClassOf).toList();
        Assertions.assertEquals(2, split1.size());
        Assertions.assertEquals(2, split1.get(0).annotations().count());
        Assertions.assertEquals(0, split1.get(1).annotations().count());
        annotation.addProperty(m.getRDFSComment(), "comment3");

        Assertions.assertEquals(3, subClassOf.annotations().count());
        Assertions.assertEquals(2, split1.get(0).annotations().count());
        Assertions.assertEquals(1, split1.get(1).annotations().count());
        Assertions.assertEquals(split1.get(0), split1.get(1));
        Assertions.assertEquals(subClassOf, split1.get(1));
        Assertions.assertEquals(5, OntModels.annotations(split1.get(0)).count());
        Assertions.assertEquals(1, OntModels.annotations(split1.get(1)).count());

        OntStatement foundSubClassOf = m.statements(clazz, RDFS.subClassOf, OWL2.Thing)
                .findFirst().orElseThrow(AssertionError::new);
        Assertions.assertEquals(3, foundSubClassOf.annotations().count());
        Assertions.assertEquals(6, OntModels.annotations(foundSubClassOf).count());

        OntStatement declaration = clazz.getMainStatement();
        declaration.addAnnotation(m.getRDFSLabel(), "comment4").addAnnotation(m.getRDFSLabel(), "label5");
        declaration.addAnnotation(m.getRDFSComment(), "comment5");
        Assertions.assertEquals(3, OntModels.annotations(declaration).count());
        Assertions.assertEquals(1, OntModels.listSplitStatements(declaration).toList().size());
        m.createResource(null, OWL2.Axiom)
                .addProperty(OWL2.annotatedSource, clazz)
                .addProperty(OWL2.annotatedProperty, RDF.type)
                .addProperty(OWL2.annotatedTarget, OWL2.Class)
                .addProperty(m.getRDFSComment(), "comment6");
        m.createResource(null, OWL2.Axiom)
                .addProperty(OWL2.annotatedSource, clazz)
                .addProperty(OWL2.annotatedProperty, RDF.type)
                .addProperty(OWL2.annotatedTarget, OWL2.Class)
                .addProperty(m.getRDFSComment(), "comment7");

        Assertions.assertEquals(5, OntModels.annotations(declaration).count());
        List<OntStatement> split2 = OntModels.listSplitStatements(declaration).toList();
        Assertions.assertEquals(2, split2.size());
        Assertions.assertEquals(3, split2.get(0).annotations().count());
        Assertions.assertEquals(1, split2.get(1).annotations().count());
    }
}
