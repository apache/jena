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

import static org.junit.jupiter.api.Assertions.fail;

import java.util.function.Consumer;

import org.apache.jena.arq.junit.manifest.AbstractManifestTest;
import org.apache.jena.arq.junit.manifest.ManifestEntry;
import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.atlas.lib.IRILib;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.RiotNotFoundException;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFLib;
import org.apache.jena.shared.NotFoundException;

public class RiotSyntaxTest extends AbstractManifestTest {

    final private boolean expectLegalSyntax;
    final private String filename;
    final private String baseIRI;
    final private Lang lang;
    final private Consumer<StreamRDF> parser;

    public RiotSyntaxTest(ManifestEntry entry, Lang lang, boolean positiveTest) {
        this(entry, null, lang, positiveTest);
    }

    public RiotSyntaxTest(ManifestEntry entry, String baseIRI, Lang lang, boolean positiveTest) {
        super(entry);
        this.filename = entry.getAction().getURI();
        this.baseIRI = ( baseIRI == null ) ? filename : baseIRI;
        this.expectLegalSyntax = positiveTest;
        this.lang = lang;
        boolean silentWarnings = RiotTestsConfig.allowWarnings(manifestEntry);
        parser = ( baseIRI != null )
            ? ParsingStepForTest.parse(filename, baseIRI, lang, silentWarnings)
            : ParsingStepForTest.parse(filename, lang, silentWarnings);

    }

    @Override
    public void runTest() {
        StreamRDF stream = StreamRDFLib.sinkNull();
        // Check so the parse step does not confuse missing with bad syntax.
        String fn = IRILib.IRIToFilename(filename);
        if ( ! FileOps.exists(fn) ) {
            throw new NotFoundException("File not found: "+filename) {
                @Override public Throwable fillInStackTrace() { return this; }
            };
        }
        try {
            parser.accept(stream);
            if (! expectLegalSyntax ) {
                String reason = "Parsing succeeded in a bad syntax test";
                outputFailure(reason, fn, null);
                String s = IO.readWholeFileAsUTF8(fn);
                System.out.println();
                System.out.println("== "+filename);
                System.out.println(s);
                fail(reason);
            }
        } catch(RiotNotFoundException ex) {
            throw ex;
        } catch(RiotException ex) {
            if ( expectLegalSyntax ) {
                String reason = "Parsing failed in a good syntax test";
                outputFailure(reason, fn, ex);
                fail(reason+" : "+ex.getMessage());
            }
        }
    }

    private void outputFailure(String reason, String fn, Throwable th) {
        String s = IO.readWholeFileAsUTF8(fn);
        System.err.println();
        System.err.println("== "+filename+ " -- "+reason);
        System.err.print(s);
        if ( !s.endsWith("\n") )
            System.err.println();
        fail("Parsing succeeded in a bad syntax test");
    }
}
