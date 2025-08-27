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

import org.apache.jena.arq.junit.manifest.AbstractManifestTest;
import org.apache.jena.arq.junit.manifest.ManifestEntry;
import org.apache.jena.query.QueryException ;
import org.apache.jena.query.Syntax;

public class UpdateSyntaxTest extends AbstractManifestTest
{
    static int count = 0 ;
    final boolean expectLegalSyntax ;
    final Syntax testSyntax;

    public UpdateSyntaxTest(ManifestEntry entry, Syntax defSyntax, boolean positiveTest) {
        super(entry);
        testSyntax = defSyntax;
        expectLegalSyntax = positiveTest ;
    }

    @Override
    public void runTest()
    {
        try {
            SparqlTestLib.updateFromEntry(manifestEntry, testSyntax) ;
            if ( ! expectLegalSyntax )
                fail("Expected parse failure") ;
        }
        catch (QueryException qEx)
        {
            if ( expectLegalSyntax )
                throw qEx ;
        }

        catch (Exception ex)
        {
            fail( "Exception: "+ex.getClass().getName()+": "+ex.getMessage()) ;
        }
    }
}
