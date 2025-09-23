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

package org.apache.jena.arq.junit.riot;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.function.Consumer;

import org.apache.jena.arq.junit.manifest.AbstractManifestTest;
import org.apache.jena.arq.junit.manifest.ManifestEntry;
import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.lib.IRILib;
import org.apache.jena.graph.Graph;
import org.apache.jena.riot.*;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFLib;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.graph.GraphFactory;

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

        boolean silentWarnings = RiotTestsConfig.allowWarnings(manifestEntry);
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

    public void run3()
    {
        Graph graph = GraphFactory.createGraphMem();
        StreamRDF dest = StreamRDFLib.graph(graph);
        try {
            parser.accept(dest);

            Lang outLang = RDFLanguages.filenameToLang(output, Lang.NTRIPLES);

            // Exactly this string.
            String actual = RDFWriter.source(graph).format(RDFFormat.NTRIPLES_UTF8).asString();
            String expected;
            try {
                expected = readFile(output);
            } catch (RiotException ex) {
                fail("Failed to read results: "+ex.getMessage());
                return;
            }
            boolean b = expected.equals(actual);

            if ( !b ) {
                System.out.println("**** Test: "+manifestEntry.getName());
                System.out.println("---- Input");
                String inputString = readFile(input);
                System.out.print(inputString);
                System.out.println("---- Actual");
                System.out.print(actual);
                System.out.println("---- Expected");
                System.out.print(expected);
                System.out.println("--------");
            }
            assertTrue(b, "Does not match expected canonical text");
        } catch (RiotException ex) {
            if ( positiveTest )
                throw ex;
        }
    }

    public void run4()
    {
        DatasetGraph dsg = DatasetGraphFactory.create();
        StreamRDF dest = StreamRDFLib.dataset(dsg);
        try {
            parser.accept(dest);

            Lang outLang = RDFLanguages.filenameToLang(output, Lang.NQUADS);

            // Exactly this string.
            String actual = RDFWriter.source(dsg).format(RDFFormat.NQUADS_UTF8).asString();

            String expected;
            try {
                expected = readFile(output);
            } catch (RiotException ex) {
                fail("Failed to read results: "+ex.getMessage());
                return;
            }
            boolean b = expected.equals(actual);

            if ( !b ) {
                System.out.println("**** Test: "+manifestEntry.getName());
                System.out.println("---- Input");
                String inputString = readFile(input);
                System.out.print(inputString);
                System.out.println("---- Actual");
                System.out.print(actual);
                System.out.println("---- Expected");
                System.out.print(expected);
                System.out.println("--------");
            }
            assertTrue(b, "Does not match expected canonical text");
        } catch (RiotException ex) {
            if ( positiveTest )
                throw ex;
        }
    }

    String readFile(String name) {
        if ( name.startsWith("file:") )
            return IO.readWholeFileAsUTF8(IRILib.IRIToFilename(name));
        return IO.readWholeFileAsUTF8(name);
    }
}
