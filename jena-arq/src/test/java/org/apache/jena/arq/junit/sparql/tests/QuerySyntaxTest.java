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

package org.apache.jena.arq.junit.sparql.tests;

import static org.junit.jupiter.api.Assertions.fail;

import org.apache.jena.arq.junit.LibTest;
import org.apache.jena.arq.junit.manifest.AbstractManifestTest;
import org.apache.jena.arq.junit.manifest.ManifestEntry;
import org.apache.jena.query.Query;

//import java.io.IOException;

import org.apache.jena.query.QueryException;
import org.apache.jena.query.Syntax;

public class QuerySyntaxTest extends AbstractManifestTest {
    final boolean       expectLegalSyntax;
    // Required syntax, null for "by file extension".
    final Syntax        testSyntax ;

    public QuerySyntaxTest(ManifestEntry entry, Syntax defSyntax, boolean positiveTest) {
        super(entry);
        testSyntax = defSyntax;
        expectLegalSyntax = positiveTest;
    }

    @Override
    public void runTest() {
        try {
            Query query = SparqlTestLib.queryFromEntry(manifestEntry, testSyntax);
            if ( !expectLegalSyntax ) {
                String filename = SparqlTestLib.queryFile(manifestEntry);
                System.out.printf("==== %s\n", "Negative Syntax test");
                LibTest.printFile(filename);
                fail("Expected parse failure");
            }
        } catch (QueryException qEx) {
            if ( expectLegalSyntax ) {
                // Development
                // System.err.println("AssertionError: "+super.manifestEntry.getURI()+" type="+manifestEntry.getTestType());
                String filename = SparqlTestLib.queryFile(manifestEntry);
                System.out.printf("==== %s\n", "Positive Syntax test");
                LibTest.printFile(filename);
                throw qEx;
            }
        } catch (AssertionError ex) {
            // Development
            // System.err.println("AssertionError: "+super.manifestEntry.getURI()+" type="+manifestEntry.getTestType());
            throw ex;
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("Exception: " + ex.getClass().getName() + ": " + ex.getMessage());
        }
    }


}
