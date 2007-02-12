/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.query.junit;


import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.query.util.IndentedLineBuffer;

public class SerializerTest extends TestCaseARQ
{
    static int count = 0 ;
    String queryString ;
    TestItem testItem ;
    
    public SerializerTest(String testName, TestItem t)
    {
        super(testName) ;
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

    private void setTest(String testName, String _queryString)
    {
        super.setName(testName) ;
        this.queryString = _queryString ;
    }
    
    // A serialization test is:
    //   Read query in.
    //   Serialize to string.
    //   Parse again.
    //   Are they equal?
    
    protected void runTest() throws Throwable
    {
        Query query = null ;
        if ( queryString == null )
            query = queryFromTestItem(testItem) ;
        else
            query = queryFromString(queryString) ;
        
        // Whatever was read in.
        runTestWorker(query, query.getSyntax()) ;
        // Prefix version
        runTestWorker(query, Syntax.syntaxPrefix) ;

    }
    
    protected void runTestWorker(Query query, Syntax syntax)
    {
        IndentedLineBuffer buff = new IndentedLineBuffer() ;
        query.serialize(buff, syntax) ;
        String baseURI = null ;
        
        if ( ! query.explicitlySetBaseURI() )
            // Not in query - use the same one (e.g. file read from) .  
            baseURI = query.getBaseURI() ;
        
        Query query2 = QueryFactory.create(buff.toString(), baseURI, syntax) ;
        
        if ( ! query.equals(query2) )
            baseURI = null ;
        
        assertEquals("Query.equals", query, query2) ;
    }

}

/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */