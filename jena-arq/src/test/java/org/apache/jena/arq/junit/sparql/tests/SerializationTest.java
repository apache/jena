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


import org.apache.jena.arq.junit.manifest.ManifestEntry;
import org.apache.jena.atlas.io.IndentedLineBuffer ;
import org.apache.jena.query.Query ;
import org.apache.jena.query.Syntax ;
import org.apache.jena.sparql.sse.SSE_ParseException ;
import org.apache.jena.sparql.util.QueryUtils ;

public class SerializationTest implements Runnable
{
    static int count = 0 ;
    String queryString ;
    ManifestEntry testEntry;

    public SerializationTest(ManifestEntry entry) {
        testEntry = entry ;
    }
    // A serialization test is:
    //   Read query in.
    //   Serialize to string.
    //   Parse again.
    //   Are they equal?

    @Override
    public void run()
    {
        Query query = SparqlTestLib.queryFromEntry(testEntry);
        // Whatever was read in.
        runTestWorker(query, query.getSyntax()) ;
    }

    protected void runTestWorker(Query query, Syntax syntax)
    {
        IndentedLineBuffer buff = new IndentedLineBuffer() ;
        query.serialize(buff, syntax) ;
        String baseURI = null ;

        if ( ! query.explicitlySetBaseURI() )
            // Not in query - use the same one (e.g. file read from) .
            baseURI = query.getBaseURI() ;

        // Query syntax and algebra tests.

        try {
            QueryUtils.checkParse(query) ;
        }
        catch (RuntimeException ex)
        {
            System.err.println("**** Test: "+testEntry.getName()) ;
            System.err.println("** "+ex.getMessage()) ;
            System.err.println(query) ;
            throw ex ;
        }

        try {
            QueryUtils.checkOp(query, true) ;
        } catch (SSE_ParseException ex)
        {
            System.err.println("**** Test: "+testEntry.getName()) ;
            System.err.println("** Algebra error: "+ex.getMessage()) ;
        }
    }
}
