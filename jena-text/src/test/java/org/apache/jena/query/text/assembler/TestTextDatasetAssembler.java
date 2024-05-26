/**
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

package org.apache.jena.query.text.assembler;

import static org.junit.Assert.assertTrue ;

import org.apache.jena.assembler.Assembler ;
import org.apache.jena.assembler.exceptions.AssemblerException;
import org.apache.jena.graph.Node ;
import org.apache.jena.query.Dataset ;
import org.apache.jena.query.text.* ;
import org.apache.jena.query.text.changes.TextQuadAction;
import org.apache.jena.rdf.model.Resource ;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.tdb1.assembler.AssemblerTDB;
import org.apache.jena.tdb1.sys.TDBInternal;
import org.apache.jena.vocabulary.RDF ;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test ;


/**
 * Test the text dataset assembler.
 */
public class TestTextDatasetAssembler extends AbstractTestTextAssembler {

    private static final String TESTBASE = "http://example.org/testDatasetAssembler/";

    private static final Resource spec1;
    private static final Resource noDatasetPropertySpec;
    private static final Resource noIndexPropertySpec;
    private static final Resource customTextDocProducerSpec;

    @BeforeClass public static void clearBefore() {
        TDBInternal.reset();
    }

    @After public void clearAfter() {
        TDBInternal.reset();
    }

    @Test
    public void testSimpleDatasetAssembler() {
        Dataset dataset = (Dataset) Assembler.general.open(spec1);
        assertTrue(dataset.getContext().get(TextQuery.textIndex) instanceof TextIndexLucene);
        dataset.close();
    }

    @Test(expected = AssemblerException.class)
    public void testErrorOnNoDataset() {
        Assembler.general.open(noDatasetPropertySpec);
    }

    @Test(expected = AssemblerException.class)
    public void testErrorOnNoIndex() {
        Assembler.general.open(noIndexPropertySpec);
    }

    @Test
    public void testCustomTextDocProducer() {
        Dataset dataset = (Dataset)Assembler.general.open(customTextDocProducerSpec) ;
        DatasetGraphText dsgText = (DatasetGraphText)dataset.asDatasetGraph() ;
        assertTrue(dsgText.getMonitor() instanceof CustomTextDocProducer) ;
        dataset.close();
    }

    static {
        JenaSystem.init();
        TextAssembler.init();
        AssemblerTDB.init();
        spec1 =
            model.createResource(TESTBASE + "spec1")
                 .addProperty(RDF.type, TextVocab.textDataset)
                 .addProperty(TextVocab.pDataset, SIMPLE_DATASET_SPEC)
                 .addProperty(TextVocab.pIndex, SIMPLE_INDEX_SPEC3);
        noDatasetPropertySpec =
            model.createResource(TESTBASE + "noDatasetPropertySpec")
                 .addProperty(RDF.type, TextVocab.textDataset)
                 .addProperty(TextVocab.pIndex, SIMPLE_INDEX_SPEC4);
        noIndexPropertySpec =
            model.createResource(TESTBASE + "noIndexPropertySpec")
                 .addProperty(RDF.type, TextVocab.textDataset)
                 .addProperty(TextVocab.pDataset, SIMPLE_DATASET_SPEC);
        customTextDocProducerSpec =
            model.createResource(TESTBASE + "customTextDocProducerSpec")
                 .addProperty(RDF.type, TextVocab.textDataset)
                 .addProperty(TextVocab.pDataset, SIMPLE_DATASET_SPEC)
                 .addProperty(TextVocab.pIndex, SIMPLE_INDEX_SPEC5)
                 .addProperty(TextVocab.pTextDocProducer, model.createResource("java:org.apache.jena.query.text.assembler.TestTextDatasetAssembler$CustomTextDocProducer"));
    }

    private static class CustomTextDocProducer implements TextDocProducer {

        public CustomTextDocProducer(TextIndex textIndex) { }

        @Override
        public void start() { }

        @Override
        public void finish() { }

        @Override
        public void change(TextQuadAction qaction, Node g, Node s, Node p, Node o) { }

        @Override
        public void reset() {}
    }
}
