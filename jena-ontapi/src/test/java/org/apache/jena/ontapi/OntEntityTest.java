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
import org.apache.jena.ontapi.model.OntDataRange;
import org.apache.jena.ontapi.model.OntModel;
import org.apache.jena.ontapi.model.OntObjectProperty;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.SKOS;
import org.apache.jena.vocabulary.XSD;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class OntEntityTest {

    @Test
    public void testDefaultBuiltinClasses() {
        OntModel m = OntModelFactory.createModel();
        Assertions.assertTrue(OWL2.Thing.inModel(m).canAs(OntClass.class));
        Assertions.assertTrue(SKOS.Collection.inModel(m).canAs(OntClass.class));

        Assertions.assertFalse(RDF.type.inModel(m).canAs(OntClass.class));
        Assertions.assertFalse(RDFS.Resource.inModel(m).canAs(OntClass.class));
        Assertions.assertFalse(RDF.Bag.inModel(m).canAs(OntClass.class));
        Assertions.assertFalse(SKOS.broader.inModel(m).canAs(OntClass.class));
    }

    @Test
    public void testDefaultBuiltinDatatypes() {
        OntModel m = OntModelFactory.createModel();
        Assertions.assertTrue(RDF.PlainLiteral.inModel(m).canAs(OntDataRange.class));
        Assertions.assertTrue(RDFS.Literal.inModel(m).canAs(OntDataRange.class));
        Assertions.assertTrue(XSD.xdouble.inModel(m).canAs(OntDataRange.class));

        Assertions.assertFalse(RDF.type.inModel(m).canAs(OntDataRange.class));
        Assertions.assertFalse(RDFS.Resource.inModel(m).canAs(OntDataRange.class));
        Assertions.assertFalse(RDF.Bag.inModel(m).canAs(OntDataRange.class));
        Assertions.assertFalse(SKOS.broader.inModel(m).canAs(OntDataRange.class));
        Assertions.assertFalse(OWL2.Nothing.inModel(m).canAs(OntDataRange.class));
        Assertions.assertFalse(OWL2.bottomDataProperty.inModel(m).canAs(OntDataRange.class));
    }

    @Test
    public void testDefaultBuiltinDatatypeProperties() {
        OntModel m = OntModelFactory.createModel();
        Assertions.assertTrue(OWL2.topDataProperty.inModel(m).canAs(OntDataProperty.class));

        Assertions.assertFalse(SKOS.altLabel.inModel(m).canAs(OntDataProperty.class));
        Assertions.assertFalse(RDFS.comment.inModel(m).canAs(OntDataProperty.class));
        Assertions.assertFalse(RDF.type.inModel(m).canAs(OntDataProperty.class));
        Assertions.assertFalse(RDFS.Resource.inModel(m).canAs(OntDataProperty.class));
        Assertions.assertFalse(RDF.Bag.inModel(m).canAs(OntDataProperty.class));
        Assertions.assertFalse(SKOS.ConceptScheme.inModel(m).canAs(OntDataProperty.class));
        Assertions.assertFalse(OWL2.Thing.inModel(m).canAs(OntDataProperty.class));
        Assertions.assertFalse(OWL2.topObjectProperty.inModel(m).canAs(OntDataProperty.class));
    }

    @Test
    public void testDefaultBuiltinObjectProperties() {
        OntModel m = OntModelFactory.createModel();
        Assertions.assertTrue(OWL2.topObjectProperty.inModel(m).canAs(OntObjectProperty.class));
        Assertions.assertTrue(SKOS.exactMatch.inModel(m).canAs(OntObjectProperty.class));

        Assertions.assertFalse(SKOS.altLabel.inModel(m).canAs(OntObjectProperty.class));
        Assertions.assertFalse(RDFS.comment.inModel(m).canAs(OntObjectProperty.class));
        Assertions.assertFalse(RDF.type.inModel(m).canAs(OntObjectProperty.class));
        Assertions.assertFalse(RDFS.Resource.inModel(m).canAs(OntObjectProperty.class));
        Assertions.assertFalse(RDF.Bag.inModel(m).canAs(OntObjectProperty.class));
        Assertions.assertFalse(SKOS.ConceptScheme.inModel(m).canAs(OntObjectProperty.class));
        Assertions.assertFalse(OWL2.Thing.inModel(m).canAs(OntObjectProperty.class));
    }

    @Test
    public void testDefaultBuiltinAnnotationProperties() {
        OntModel m = OntModelFactory.createModel();
        Assertions.assertTrue(OWL2.incompatibleWith.inModel(m).canAs(OntAnnotationProperty.class));
        Assertions.assertTrue(SKOS.altLabel.inModel(m).canAs(OntAnnotationProperty.class));
        Assertions.assertTrue(RDFS.comment.inModel(m).canAs(OntAnnotationProperty.class));

        Assertions.assertFalse(RDF.type.inModel(m).canAs(OntAnnotationProperty.class));
        Assertions.assertFalse(RDFS.Resource.inModel(m).canAs(OntAnnotationProperty.class));
        Assertions.assertFalse(RDF.Bag.inModel(m).canAs(OntAnnotationProperty.class));
        Assertions.assertFalse(SKOS.ConceptScheme.inModel(m).canAs(OntAnnotationProperty.class));
        Assertions.assertFalse(OWL2.Thing.inModel(m).canAs(OntAnnotationProperty.class));
        Assertions.assertFalse(OWL2.topObjectProperty.inModel(m).canAs(OntAnnotationProperty.class));
    }
}
