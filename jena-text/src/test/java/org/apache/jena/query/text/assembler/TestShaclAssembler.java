/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.query.text.assembler;

import static org.junit.Assert.*;

import org.apache.jena.assembler.Assembler;
import org.apache.jena.query.text.ShaclIndexMapping;
import org.apache.jena.query.text.ShaclIndexMapping.FieldDef;
import org.apache.jena.query.text.ShaclTextIndexLucene;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.path.*;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.junit.Test;

/**
 * Tests for SHACL assembler config parsing.
 */
public class TestShaclAssembler {

    private static final String SH = "http://www.w3.org/ns/shacl#";
    private static final String EX = "http://example.org/";

    static {
        JenaSystem.init();
        TextAssembler.init();
    }

    private Model createModel() {
        return ModelFactory.createDefaultModel();
    }

    /**
     * Build a valid text:shapes index spec in the model.
     */
    private Resource buildShaclIndexSpec(Model model) {
        // Define the shape
        Resource bookShape = model.createResource(EX + "BookShape")
            .addProperty(model.createProperty(SH, "targetClass"), model.createResource(EX + "Book"))
            .addProperty(
                model.createProperty(SH, "property"),
                model.createResource()
                    .addProperty(model.createProperty(IndexVocab.NS, "fieldName"), "label")
                    .addProperty(model.createProperty(IndexVocab.NS, "fieldType"), IndexVocab.TextField)
                    .addProperty(model.createProperty(IndexVocab.NS, "defaultSearch"), model.createTypedLiteral(true))
                    .addProperty(model.createProperty(SH, "path"), RDFS.label)
            );

        // Build the shapes list
        RDFNode shapesList = model.createList(new RDFNode[]{ bookShape });

        // Build the index spec
        return model.createResource(EX + "index")
            .addProperty(RDF.type, TextVocab.textIndexShacl)
            .addProperty(TextVocab.pDirectory, model.createLiteral("mem"))
            .addProperty(TextVocab.pShapes, shapesList);
    }

    @Test
    public void testShaclShapesParsed() {
        Model model = createModel();
        Resource indexSpec = buildShaclIndexSpec(model);

        ShaclTextIndexLucene index = (ShaclTextIndexLucene) Assembler.general().open(indexSpec);
        try {
            assertTrue("Should be in SHACL mode", index.isShaclMode());
            ShaclIndexMapping mapping = index.getShaclMapping();
            assertNotNull(mapping);
            assertEquals(1, mapping.getProfiles().size());

            ShaclIndexMapping.IndexProfile profile = mapping.getProfiles().get(0);
            assertEquals(1, profile.getFields().size());
            assertEquals("label", profile.getFields().get(0).getFieldName());
        } finally {
            index.close();
        }
    }

    @Test
    public void testDerivedEntityDefinition() {
        Model model = createModel();
        Resource indexSpec = buildShaclIndexSpec(model);

        ShaclTextIndexLucene index = (ShaclTextIndexLucene) Assembler.general().open(indexSpec);
        try {
            assertNotNull(index.getDocDef());
            assertEquals("uri", index.getDocDef().getEntityField());
            assertEquals("label", index.getDocDef().getPrimaryField());
            assertEquals(RDFS.label.asNode(), index.getDocDef().getPrimaryPredicate());
        } finally {
            index.close();
        }
    }

    @Test
    public void testInversePathParsed() {
        Model model = createModel();

        // Shape with inverse path: sh:path [ sh:inversePath ex:wrote ]
        Resource bookShape = model.createResource(EX + "BookShape")
            .addProperty(model.createProperty(SH, "targetClass"), model.createResource(EX + "Book"))
            .addProperty(
                model.createProperty(SH, "property"),
                model.createResource()
                    .addProperty(model.createProperty(IndexVocab.NS, "fieldName"), "title")
                    .addProperty(model.createProperty(IndexVocab.NS, "defaultSearch"), model.createTypedLiteral(true))
                    .addProperty(model.createProperty(SH, "path"), RDFS.label)
            )
            .addProperty(
                model.createProperty(SH, "property"),
                model.createResource()
                    .addProperty(model.createProperty(IndexVocab.NS, "fieldName"), "wroteBy")
                    .addProperty(model.createProperty(IndexVocab.NS, "fieldType"), IndexVocab.KeywordField)
                    .addProperty(model.createProperty(SH, "path"),
                        model.createResource()
                            .addProperty(model.createProperty(SH, "inversePath"),
                                model.createResource(EX + "wrote")))
            );

        RDFNode shapesList = model.createList(new RDFNode[]{ bookShape });
        Resource indexSpec = model.createResource(EX + "index")
            .addProperty(RDF.type, TextVocab.textIndexShacl)
            .addProperty(TextVocab.pDirectory, model.createLiteral("mem"))
            .addProperty(TextVocab.pShapes, shapesList);

        ShaclTextIndexLucene index = (ShaclTextIndexLucene) Assembler.general().open(indexSpec);
        try {
            ShaclIndexMapping mapping = index.getShaclMapping();
            FieldDef wroteByField = null;
            for (FieldDef f : mapping.getProfiles().get(0).getFields()) {
                if ("wroteBy".equals(f.getFieldName())) {
                    wroteByField = f;
                }
            }
            assertNotNull("Should have wroteBy field", wroteByField);
            assertTrue("wroteBy should have complex path", wroteByField.hasComplexPath());
            assertTrue("wroteBy path should be P_Inverse", wroteByField.getPath() instanceof P_Inverse);
        } finally {
            index.close();
        }
    }

    @Test
    public void testSequencePathParsed() {
        Model model = createModel();

        // Shape with sequence path: sh:path ( ex:author ex:name )
        Resource authorPath = model.createList(new RDFNode[]{
            model.createResource(EX + "author"),
            model.createResource(EX + "name")
        }).asResource();

        Resource bookShape = model.createResource(EX + "BookShape")
            .addProperty(model.createProperty(SH, "targetClass"), model.createResource(EX + "Book"))
            .addProperty(
                model.createProperty(SH, "property"),
                model.createResource()
                    .addProperty(model.createProperty(IndexVocab.NS, "fieldName"), "title")
                    .addProperty(model.createProperty(IndexVocab.NS, "defaultSearch"), model.createTypedLiteral(true))
                    .addProperty(model.createProperty(SH, "path"), RDFS.label)
            )
            .addProperty(
                model.createProperty(SH, "property"),
                model.createResource()
                    .addProperty(model.createProperty(IndexVocab.NS, "fieldName"), "authorName")
                    .addProperty(model.createProperty(IndexVocab.NS, "fieldType"), IndexVocab.KeywordField)
                    .addProperty(model.createProperty(SH, "path"), authorPath)
            );

        RDFNode shapesList = model.createList(new RDFNode[]{ bookShape });
        Resource indexSpec = model.createResource(EX + "index")
            .addProperty(RDF.type, TextVocab.textIndexShacl)
            .addProperty(TextVocab.pDirectory, model.createLiteral("mem"))
            .addProperty(TextVocab.pShapes, shapesList);

        ShaclTextIndexLucene index = (ShaclTextIndexLucene) Assembler.general().open(indexSpec);
        try {
            ShaclIndexMapping mapping = index.getShaclMapping();
            FieldDef authorNameField = null;
            for (FieldDef f : mapping.getProfiles().get(0).getFields()) {
                if ("authorName".equals(f.getFieldName())) {
                    authorNameField = f;
                }
            }
            assertNotNull("Should have authorName field", authorNameField);
            assertTrue("authorName should have complex path", authorNameField.hasComplexPath());
            assertTrue("authorName path should be P_Seq", authorNameField.getPath() instanceof P_Seq);

            // Verify leaf predicates extracted for change listener
            assertEquals("Should have 2 leaf predicates", 2, authorNameField.getPredicates().size());
        } finally {
            index.close();
        }
    }

}
