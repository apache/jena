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
import org.apache.jena.assembler.exceptions.AssemblerException;
import org.apache.jena.query.text.ShaclIndexMapping;
import org.apache.jena.query.text.TextIndexException;
import org.apache.jena.query.text.TextIndexLucene;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
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
            .addProperty(RDF.type, TextVocab.textIndexLucene)
            .addProperty(TextVocab.pDirectory, model.createLiteral("mem"))
            .addProperty(TextVocab.pShapes, shapesList);
    }

    @Test
    public void testShaclShapesParsed() {
        Model model = createModel();
        Resource indexSpec = buildShaclIndexSpec(model);

        TextIndexLucene index = (TextIndexLucene) Assembler.general().open(indexSpec);
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

        TextIndexLucene index = (TextIndexLucene) Assembler.general().open(indexSpec);
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
    public void testBothShapesAndEntityMapThrows() {
        Model model = createModel();

        // entityMap
        Resource entityMap = model.createResource(EX + "entMap")
            .addProperty(RDF.type, TextVocab.entityMap)
            .addProperty(TextVocab.pEntityField, "uri")
            .addProperty(TextVocab.pDefaultField, "text")
            .addProperty(TextVocab.pMap,
                model.createList(new RDFNode[]{
                    model.createResource()
                        .addProperty(TextVocab.pField, "text")
                        .addProperty(TextVocab.pPredicate, RDFS.label)
                }));

        // shapes list
        Resource bookShape = model.createResource(EX + "BookShape2")
            .addProperty(model.createProperty(SH, "targetClass"), model.createResource(EX + "Book"))
            .addProperty(
                model.createProperty(SH, "property"),
                model.createResource()
                    .addProperty(model.createProperty(IndexVocab.NS, "fieldName"), "text")
                    .addProperty(model.createProperty(IndexVocab.NS, "defaultSearch"), model.createTypedLiteral(true))
                    .addProperty(model.createProperty(SH, "path"), RDFS.label)
            );
        RDFNode shapesList = model.createList(new RDFNode[]{ bookShape });

        // Both specified — should throw
        Resource indexSpec = model.createResource(EX + "badIndex")
            .addProperty(RDF.type, TextVocab.textIndexLucene)
            .addProperty(TextVocab.pDirectory, model.createLiteral("mem"))
            .addProperty(TextVocab.pEntityMap, entityMap)
            .addProperty(TextVocab.pShapes, shapesList);

        try {
            Assembler.general().open(indexSpec);
            fail("Should have thrown an exception");
        } catch (AssemblerException e) {
            // Assembler wraps our TextIndexException
            assertTrue("Cause should be TextIndexException",
                e.getCause() instanceof TextIndexException);
            assertTrue(e.getCause().getMessage().contains("Cannot specify both"));
        }
    }

    @Test
    public void testNeitherShapesNorEntityMapThrows() {
        Model model = createModel();

        Resource indexSpec = model.createResource(EX + "emptyIndex")
            .addProperty(RDF.type, TextVocab.textIndexLucene)
            .addProperty(TextVocab.pDirectory, model.createLiteral("mem"));

        try {
            Assembler.general().open(indexSpec);
            fail("Should have thrown an exception");
        } catch (AssemblerException e) {
            assertTrue("Cause should be TextIndexException",
                e.getCause() instanceof TextIndexException);
            assertTrue(e.getCause().getMessage().contains("Must specify either"));
        }
    }
}
