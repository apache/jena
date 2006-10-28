/*
 * (c) Copyright 2005, 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.junit;

import junit.framework.*;

import com.hp.hpl.jena.query.DataFormat;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.query.junit.*;
import com.hp.hpl.jena.query.vocabulary.TestManifestX;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sdb.store.Store;
import com.hp.hpl.jena.util.FileManager;


public class QueryTestSDBFactory extends TestFactory
{
    FileManager fileManager = FileManager.get() ;
    Store store ;
    private String testRootName ;
    
    static public TestSuite make(Store store, String filename, String testRootName) 
    {
        QueryTestSDBFactory f = new QueryTestSDBFactory(store, testRootName) ;
        return f.process(filename) ;
    }   

    private QueryTestSDBFactory(Store store, String testRootName)
    {
        this.store = store ;
        this.testRootName = testRootName ;
    }
    
    @Override
    public Test makeTest(Resource manifest, Resource entry, String testName, Resource action, Resource result)
    {
            // Defaults.
            Syntax querySyntax = TestUtils.getQuerySyntax(manifest)  ;
            
            if ( testRootName != null )
                testName = testRootName+testName ;
            
            if ( querySyntax != null )
            {
                if ( ! querySyntax.equals(Syntax.syntaxRDQL) &&
                     ! querySyntax.equals(Syntax.syntaxARQ) &&
                     ! querySyntax.equals(Syntax.syntaxSPARQL) )
                    throw new QueryTestException("Unknown syntax: "+querySyntax) ;
            }
            
            // May be null
            Resource defaultTestType = TestUtils.getResource(manifest, TestManifestX.defaultTestType) ;
            // test name
            // test type
            // action -> query specific query[+data]
            // results
            
            TestItem testItem = new TestItem(entry, defaultTestType, querySyntax, DataFormat.langXML) ;
            TestCase test = null ;
            
            if ( testItem.getTestType() != null )
            {
                if ( testItem.getTestType().equals(TestManifestX.TestQuery) )
                    test = new QueryTestSDB(store, testName, fileManager, testItem) ;
                
                if ( testItem.getTestType().equals(TestManifestX.TestSurpressed) )
                    test = new SurpressedTest(testName, testItem.getComment()) ;
                
                if ( test == null )
                    System.err.println("Unrecognized test type: "+testItem.getTestType()) ;
            }
            // Default 
            if ( test == null )
                test = new QueryTestSDB(store, testName, fileManager, testItem) ;
            return test ;
    }
}

/*
 * (c) Copyright 2005, 2006 Hewlett-Packard Development Company, LP
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