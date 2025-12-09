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
import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.irix.IRIs;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.RiotNotFoundException;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFLib;
import org.apache.jena.shared.NotFoundException;

public class RiotSyntaxTest extends AbstractManifestTest {

    final private boolean expectLegalSyntax;
    final private String actionURI;
    final private String baseIRI;
    final private Lang lang;
    final private Consumer<StreamRDF> parser;

    public RiotSyntaxTest(ManifestEntry entry, Lang lang, boolean positiveTest) {
        this(entry, null, lang, positiveTest);
    }

    public RiotSyntaxTest(ManifestEntry entry, String baseIRI, Lang lang, boolean positiveTest) {
        super(entry);
        this.actionURI = entry.getAction().getURI();
        this.baseIRI = ( baseIRI == null ) ? actionURI : baseIRI;
        this.expectLegalSyntax = positiveTest;
        this.lang = lang;
        boolean silentWarnings = RiotTestsConfig.allowWarnings(manifestEntry);
        parser = ( baseIRI != null )
            ? ParsingStepForTest.parse(actionURI, baseIRI, lang, silentWarnings)
            : ParsingStepForTest.parse(actionURI, lang, silentWarnings);
    }

    @Override
    public void runTest() {
        StreamRDF stream = StreamRDFLib.sinkNull();
        // If a file, check so the parse step does not confuse missing with bad syntax.
        String uriScheme = IRIs.scheme(actionURI);
        boolean isLocal = "file".equals(Lib.lowercase(uriScheme));
        String fn = isLocal ? IRILib.IRIToFilename(actionURI) : actionURI;
        if (isLocal) {
            if ( ! FileOps.exists(fn) ) {
                throw new NotFoundException("File not found: "+actionURI) {
                    @Override public Throwable fillInStackTrace() { return this; }
                };
            }
        }

        try {
            parser.accept(stream);
            if (! expectLegalSyntax ) {
                String reason = "Parsing succeeded in a bad syntax test: "+actionURI;
                outputFailure(reason, fn, null);
                if ( isLocal )
                printIfFile(reason, actionURI, fn);
                System.out.println("== "+actionURI);
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

    private void printIfFile(String reason, String uri, String fn) {
        String s = IO.readWholeFileAsUTF8(fn);
        System.err.println();
        System.err.println("== "+actionURI+ " -- "+reason);
        System.err.print(s);
        if ( !s.endsWith("\n") )
            System.err.println();
    }

    private void outputFailure(String reason, String fn, Throwable th) {
        fail("Parsing succeeded in a bad syntax test");
    }
}
