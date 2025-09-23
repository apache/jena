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

import static org.apache.jena.arq.junit.riot.RiotTestsConfig.fragment;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.function.Consumer;

import org.apache.jena.arq.junit.manifest.AbstractManifestTest;
import org.apache.jena.arq.junit.manifest.ManifestEntry;
import org.apache.jena.graph.Graph;
import org.apache.jena.riot.*;
import org.apache.jena.riot.system.ErrorHandlerFactory;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFLib;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.util.IsoMatcher;

public class RiotEvalTest extends AbstractManifestTest {
    final private boolean       positiveTest;
    final private Lang          lang;
    final private String        filename;

    final private String baseIRI;
    final private String input;
    final private String output;
    final private Consumer<StreamRDF> parser;

    public RiotEvalTest(ManifestEntry entry, String base, Lang lang, boolean positiveTest) {
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
    public void runTest()
    {
        // Could generalise run4() to cover both cases.
        // run3() predates dataset reading and is more tested.
        if ( RDFLanguages.isTriples(lang) )
            run3();
        else
            run4();
    }

    // Triples test.
    private void run3() {
        Graph graph = GraphFactory.createDefaultGraph();
        StreamRDF dest = StreamRDFLib.graph(graph);
        try {
            parser.accept(dest);

            if ( ! positiveTest ) {
                String fragment = fragment(manifestEntry.getURI());
                if ( fragment != null )
                    fail(fragment+": Passed bad syntax eval test");
                else
                    fail("Passed bad syntax eval test");
            }

            Lang outLang = RDFLanguages.filenameToLang(output, Lang.NQUADS);

            Graph results = GraphFactory.createDefaultGraph();
            try {
                RDFParser.create().errorHandler(ErrorHandlerFactory.errorHandlerNoWarnings)
                    .base(baseIRI)
                    .forceLang(outLang)
                    .source(output)
                    .parse(results);
            } catch (RiotException ex) {
                fail("Failed to read results: "+ex.getMessage());
            }

            boolean b = IsoMatcher.isomorphic(graph, results);

            if ( !b ) {
                // graph.isIsomorphicWith(results);
                System.out.println("---- Parsed");
                RDFDataMgr.write(System.out, graph, Lang.TURTLE);
                System.out.println("---- Expected");
                results.getPrefixMapping().setNsPrefixes(graph.getPrefixMapping());
                RDFDataMgr.write(System.out, results, Lang.TURTLE);
                System.out.println("--------");
            }
            assertTrue(b, "Graphs not isomorphic");
        } catch (RiotException ex) {
            if ( positiveTest )
                throw ex;
        }
    }

    private void run4() {
        DatasetGraph dsg = DatasetGraphFactory.create();
        StreamRDF dest = StreamRDFLib.dataset(dsg);
        try {
            parser.accept(dest);

            if ( ! positiveTest )
                fail("Passed bad syntax eval test");

            Lang outLang = RDFLanguages.filenameToLang(output, Lang.NQUADS);

            DatasetGraph results = DatasetGraphFactory.create();
            try {
                RDFParser.create().errorHandler(ErrorHandlerFactory.errorHandlerNoWarnings)
                    .base(baseIRI)
                    .forceLang(outLang)
                    .source(output)
                    .parse(results);
            } catch (RiotException ex) {
                fail("Failed to read results: "+ex.getMessage());
            }

            boolean b = isomorphic(dsg, results);

            if ( !b )
            {
                System.out.println("**** Test: "+manifestEntry.getName());
                System.out.println("---- Parsed");
                RDFDataMgr.write(System.out, dsg, Lang.TRIG);
                System.out.println("---- Expected");
                RDFDataMgr.write(System.out, results, Lang.TRIG);
                System.out.println("--------");
            }

            assertTrue(b, "Datasets not isomorphic");
        } catch (RiotException ex) {
            if ( positiveTest )
                throw ex;
        }
    }

    private boolean isomorphic(DatasetGraph dsg1, DatasetGraph dsg2) {
        return IsoMatcher.isomorphic(dsg1, dsg2);
    }
}
