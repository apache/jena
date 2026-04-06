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

package org.apache.jena.arq.junit.riot;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

import org.apache.jena.arq.junit.manifest.AbstractManifestTest;
import org.apache.jena.arq.junit.manifest.ManifestEntry;
import org.apache.jena.atlas.io.IOX;
import org.apache.jena.atlas.lib.Bytes;
import org.apache.jena.atlas.lib.IRILib;
import org.apache.jena.graph.Graph;
import org.apache.jena.riot.*;
import org.apache.jena.riot.lang.LabelToNode;
import org.apache.jena.riot.system.ErrorHandlerFactory;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFLib;
import org.apache.jena.riot.writer.c14n.DatasetGraphOrdered;
import org.apache.jena.riot.writer.c14n.GraphOrdered;
import org.apache.jena.sparql.core.DatasetGraph;

public class RiotC14NTest extends AbstractManifestTest {

    final private boolean       positiveTest;
    final private Lang          lang;
    final private String        filename;

    final private String baseIRI;
    final private String input;
    final private String output;
    final private Consumer<StreamRDF> parser;

    public RiotC14NTest(ManifestEntry entry, String base, Lang lang, boolean positiveTest) {
        super(entry);
        this.positiveTest = positiveTest;
        this.filename = entry.getAction().getURI();
        this.lang = lang;
        baseIRI = base;
        input = entry.getAction().getURI();
        output = positiveTest ? entry.getResult().getURI() : null;

        boolean silentWarnings = true; //RiotTestsConfig.allowWarnings(manifestEntry);
        parser = ( baseIRI != null )
            ? ParsingStepForTest.parse(input, baseIRI, lang, silentWarnings)
            : ParsingStepForTest.parse(input, lang, silentWarnings);
    }

    @Override
    public void runTest() {
        if ( RDFLanguages.isTriples(lang) )
            run3();
        else
            run4();
    }

    public void run3() {
        Graph graph = new GraphOrdered();
        StreamRDF dest = StreamRDFLib.graph(graph);
        try {
            RDFParser.create().errorHandler(ErrorHandlerFactory.errorHandlerIgnoreWarnings(null))
                .strict(true).forceLang(lang).base(baseIRI).source(input)
                // ******
                .labelToNode(LabelToNode.createUseLabelAsGiven())
                .parse(dest);

            Lang outLang = RDFLanguages.filenameToLang(output, Lang.NTRIPLES);

            // Special writer. NTriplesWriter_C14N / NodeFormatter_C14N / EscapeStr_C14N
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            RDFWriter.source(graph).format(RDFFormat.NTRIPLES_C14N).output(out);

            byte[] actual = out.toByteArray();
            byte[] expected;
            try {
                expected = readFile(output);
            } catch (RiotException ex) {
                fail("Failed to read results: "+ex.getMessage());
                return;
            }

            // Compare byte-for-byte
            boolean b = Bytes.compare(expected, actual) == 0;
            if ( !b ) {
                System.out.println("**** Test: "+manifestEntry.getName());
                System.out.println("---- Input");
                byte[] bytes = readFile(input);
                String inputString = Bytes.bytes2string(actual);
                System.out.print(inputString);
                System.out.println("---- Actual");
                System.out.print(Bytes.bytes2string(actual));
                System.out.println("---- Expected");
                System.out.print(Bytes.bytes2string(expected));
                System.out.println("--------");
            }
            assertTrue(b, "Does not match expected canonical text");
        } catch (RiotException ex) {
            if ( positiveTest ) {
                fail(ex.getMessage());
                //throw ex;
            }
        }
    }

    public void run4() {
        DatasetGraph dsg = new DatasetGraphOrdered();
        StreamRDF dest = StreamRDFLib.dataset(dsg);
        try {
            RDFParser.create().errorHandler(ErrorHandlerFactory.errorHandlerIgnoreWarnings(null))
            .strict(true).forceLang(lang).base(baseIRI).source(input)
            // ******
            .labelToNode(LabelToNode.createUseLabelAsGiven())
            .parse(dest);

            Lang outLang = RDFLanguages.filenameToLang(output, Lang.NTRIPLES);

            // Special writer. NQuadsWriter_C14N / NodeFormatter_C14N / EscapeStr_C14N
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            RDFWriter.source(dsg).format(RDFFormat.NQUADS_C14N).output(out);

            byte[] actual = out.toByteArray();
            byte[] expected;
            try {
                expected = readFile(output);
            } catch (RiotException ex) {
                fail("Failed to read results: "+ex.getMessage());
                return;
            }
            boolean b = Bytes.compare(expected, actual) == 0;
            if ( !b ) {
                System.out.println("**** Test: "+manifestEntry.getName());
                System.out.println("---- Input");
                byte[] bytes = readFile(input);
                String inputString = Bytes.bytes2string(actual);
                System.out.print(inputString);
                System.out.println("---- Actual");
                System.out.print(Bytes.bytes2string(actual));
                System.out.println("---- Expected");
                System.out.print(Bytes.bytes2string(expected));
                System.out.println("--------");
            }
            assertTrue(b, "Does not match expected canonical text");
        } catch (RiotException ex) {
            if ( positiveTest )
                throw ex;
        }
    }

    private byte[] readFile(String filename) {
        String fn = ( filename.startsWith("file:")) ? IRILib.IRIToFilename(filename) : filename;
        Path path = Path.of(fn);
        try {
            return Files.readAllBytes(path);
        } catch (IOException ex) {
            throw IOX.exception(ex);
        }
    }
}
