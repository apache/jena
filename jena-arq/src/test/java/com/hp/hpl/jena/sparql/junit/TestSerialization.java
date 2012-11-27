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

package com.hp.hpl.jena.sparql.junit;


import org.apache.jena.atlas.io.IndentedLineBuffer ;

import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.Syntax ;
import com.hp.hpl.jena.sparql.sse.SSEParseException ;
import com.hp.hpl.jena.sparql.util.QueryUtils ;

public class TestSerialization extends EarlTestCase
{
    static int count = 0 ;
    String queryString ;
    TestItem testItem ;
    
    public TestSerialization(String testName, EarlReport earl, TestItem t)
    {
        super(testName, t.getURI(), earl) ;
        testItem = t ;
    }

//    public SerializerTest(String queryString)
//    {
//        this(queryString, queryString) ;
//    }
//    
//    public SerializerTest(String testName, String queryString)
//    {
//        super(testName) ;
//        setTest(testName, queryString) ;
//    }

    private void setTest(String testName, EarlReport earl, String _queryString)
    {
        super.setName(testName) ;
        this.queryString = _queryString ;
    }
    
    // A serialization test is:
    //   Read query in.
    //   Serialize to string.
    //   Parse again.
    //   Are they equal?
    
    @Override
    protected void runTestForReal() throws Throwable
    {
        Query query = null ;
        if ( queryString == null )
            query = queryFromTestItem(testItem) ;
        else
            query = queryFromString(queryString) ;
        
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
            System.err.println("**** Test: "+getName()) ;
            System.err.println("** "+ex.getMessage()) ;
            System.err.println(query) ;
            throw ex ; 
        }

        try {
            QueryUtils.checkOp(query, true) ;
        } catch (SSEParseException ex)
        {
            System.err.println("**** Test: "+getName()) ; 
            System.err.println("** Algebra error: "+ex.getMessage()) ;
        }
    }

}
