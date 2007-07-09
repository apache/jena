/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.junit;

//import java.io.IOException;

import com.hp.hpl.jena.query.*;


public class SyntaxTest extends EarlTestCase
{
    static int count = 0 ;
    String queryString ;
    boolean expectLegalSyntax ;
    TestItem testItem ;
    
    public SyntaxTest(String testName, EarlReport earl, TestItem t)
    {
        this(testName, earl, t, true) ;
    }

    public SyntaxTest(String testName, EarlReport earl, TestItem t, boolean positiveTest)
    {
        super(testName, t.getURI(), earl) ;
        testItem = t ;
        expectLegalSyntax = positiveTest ; 
    }

//    public SyntaxTest(String queryString, boolean positiveTest)
//    {
//        this(positiveTest?queryString:"X: "+queryString,
//             queryString,
//             positiveTest) ;
//    }
//
//
//    public SyntaxTest(String queryString)
//    {
//        this(queryString, true) ;
//    }
//    
//    public SyntaxTest(String testName, String queryString)
//    { this(testName, queryString, true) ; }

    
    public SyntaxTest(String testName, EarlReport earl, String queryString,  boolean positiveTest)
    {
        super(testName, TestItem.fakeURI(), earl) ;
        setTest(testName, queryString, positiveTest) ;
    }

    private void setTest(String testName, String _queryString, boolean positiveTest)
    {
        super.setName(testName) ;
        this.queryString = _queryString ;
        expectLegalSyntax = positiveTest ; 
    }
    
    
    protected void runTestForReal() throws Throwable
    {
        try {
            if ( queryString == null )
                queryFromTestItem(testItem) ;
            else
                queryFromString(queryString) ;
            
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