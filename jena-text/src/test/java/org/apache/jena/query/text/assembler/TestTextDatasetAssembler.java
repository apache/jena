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

import java.util.Iterator;

import org.apache.jena.assembler.Assembler ;
import org.apache.jena.assembler.exceptions.AssemblerException;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.query.Dataset ;
import org.apache.jena.query.ReadWrite ;
import org.apache.jena.query.text.* ;
import org.apache.jena.rdf.model.Resource ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.Quad ;
import org.apache.jena.sparql.core.QuadAction ;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.tdb.assembler.AssemblerTDB ;
import org.apache.jena.tdb.sys.TDBInternal;
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
    private static final Resource customDyadicTextDocProducerSpec;

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

    @Test
    public void testCustomTextDocProducerDyadicConstructor() {
        Dataset dataset = (Dataset)Assembler.general.open(customDyadicTextDocProducerSpec) ;
        DatasetGraphText dsgText = (DatasetGraphText)dataset.asDatasetGraph() ;
        assertTrue(dsgText.getMonitor() instanceof CustomDyadicTextDocProducer) ;

        Node G = NodeFactory.createURI("http://example.com/G");
        Node S = NodeFactory.createURI("http://example.com/S");
        Node P = NodeFactory.createURI("http://example.com/P");
        Node O = NodeFactory.createLiteral("http://example.com/O");

        dsgText.begin(ReadWrite.WRITE);
        dsgText.add(G, S, P, O);
        dsgText.commit();
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

        customDyadicTextDocProducerSpec =
            model.createResource(TESTBASE + "customDyadicTextDocProducerSpec")
                 .addProperty(RDF.type, TextVocab.textDataset)
                 .addProperty(TextVocab.pDataset, SIMPLE_DATASET_SPEC)
                 .addProperty(TextVocab.pIndex, SIMPLE_INDEX_SPEC5)
                 .addProperty(TextVocab.pTextDocProducer, model.createResource("java:org.apache.jena.query.text.assembler.TestTextDatasetAssembler$CustomDyadicTextDocProducer"));
    }

    private static class CustomTextDocProducer implements TextDocProducer {

        public CustomTextDocProducer(TextIndex textIndex) { }

        @Override
        public void start() { }

        @Override
        public void finish() { }

        @Override
        public void change(QuadAction qaction, Node g, Node s, Node p, Node o) { }

        @Override
        public void reset() {}
    }


    private static class CustomDyadicTextDocProducer implements TextDocProducer {

        final DatasetGraph dg;
        Node lastSubject = null;

        public CustomDyadicTextDocProducer(DatasetGraph dg, TextIndex textIndex) { 
            this.dg = dg;
        }

        @Override
        public void start() { }

        @Override
        public void finish() { 
            Iterator<Quad> qi = dg.find(null, lastSubject, Node.ANY, Node.ANY);
            while (qi.hasNext()) qi.next();
        }

        @Override
        public void change(QuadAction qaction, Node g, Node s, Node p, Node o) { 
            lastSubject = s;
        }

        @Override
        public void reset() {}
    }

}
