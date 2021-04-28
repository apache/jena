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

import static org.junit.Assert.fail;

import org.apache.jena.arq.junit.manifest.ManifestEntry;
import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.atlas.lib.IRILib;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.RiotNotFoundException;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFLib;
import org.apache.jena.shared.NotFoundException;

public class RiotSyntaxTest implements Runnable {

    final private boolean       expectLegalSyntax;
    final private ManifestEntry testEntry;
    final private Lang lang;
    final private String filename;

    public RiotSyntaxTest(ManifestEntry entry, Lang lang, boolean positiveTest) {
        this.testEntry = entry;
        this.expectLegalSyntax = positiveTest;
        this.filename = entry.getAction().getURI();
        this.lang = lang;
    }

    @Override
    public void run() {
        StreamRDF stream = StreamRDFLib.sinkNull();
        // Check so the parse step does not confuse missing with bad syntax.
        String fn = IRILib.IRIToFilename(filename);
        if ( ! FileOps.exists(fn) ) {
            throw new NotFoundException("File not found: "+filename) {
                @Override public Throwable fillInStackTrace() { return this; }
            };
        }
        try {
            ParseForTest.parse(stream, filename, lang);
            if (! expectLegalSyntax ) {
                String s = IO.readWholeFileAsUTF8(fn);
                System.err.println();
                System.err.println("== "+filename);
                System.err.print(s);
                fail("Parsing succeeded in a bad syntax test");
            }
        } catch(RiotNotFoundException ex) {
            throw ex;
        } catch(RiotException ex) {
            if ( expectLegalSyntax )
                fail("Parse error: "+ex.getMessage());
        }
    }
}
