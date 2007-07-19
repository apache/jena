/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.junit;

import static com.hp.hpl.jena.sparql.junit.TestQueryUtils.getQuerySyntax;
import static com.hp.hpl.jena.util.junit.TestUtils.getResource;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Resource;

import com.hp.hpl.jena.sdb.shared.StoreList;
import com.hp.hpl.jena.sdb.store.Store;
import com.hp.hpl.jena.sdb.util.Pair;

import com.hp.hpl.jena.sparql.core.DataFormat;
import com.hp.hpl.jena.sparql.junit.EarlReport;
import com.hp.hpl.jena.sparql.junit.QueryTestException;
import com.hp.hpl.jena.sparql.junit.SurpressedTest;
import com.hp.hpl.jena.sparql.junit.TestItem;
import com.hp.hpl.jena.sparql.vocabulary.TestManifestX;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.junit.TestFactoryManifest;


public class QueryTestSDBFactory extends TestFactoryManifest
{
    public static EarlReport results = null ;
    
    public static TestSuite makeSuite(String storeListFile, String manifestFile)
    {
        TestSuite ts = new TestSuite() ;
        make(ts, storeListFile, manifestFile) ;
        return ts ;
    }
    
    public static void make(TestSuite ts, String storeList, String manifestFile)
    {
        for ( Pair<Store, String> p : StoreList.stores(storeList) )
        {
            Store store = p.car() ;
            String label = p.cdr();
            if ( label != null && !label.equals("") )
                label = label+" - " ;
            TestSuite ts2 = QueryTestSDBFactory.make(store, manifestFile, label) ;
            ts.addTest(ts2) ;
        }
    }
    
    static public TestSuite make(Store store, String manifestFile, String testRootName) 
    {
        QueryTestSDBFactory f = new QueryTestSDBFactory(store, testRootName) ;
        TestSuite ts = f.process(manifestFile) ;
        if ( testRootName != null )
            ts.setName(testRootName+ts.getName()) ;
        return ts ;
    }

    FileManager fileManager = FileManager.get() ;
    Store store ;
    private String testRootName ;
    
    
    
    private QueryTestSDBFactory(Store store, String testRootName)
    {
        this.store = store ;
        this.testRootName = testRootName ;
    }
    
    @Override
    public Test makeTest(Resource manifest, Resource entry, String testName, Resource action, Resource result)
    {
            // Defaults.
            Syntax querySyntax = getQuerySyntax(manifest)  ;
            
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
            Resource defaultTestType = getResource(manifest, TestManifestX.defaultTestType) ;
            // test name
            // test type
            // action -> query specific query[+data]
            // results
            
            TestItem testItem = new TestItem(entry, defaultTestType, querySyntax, DataFormat.langXML) ;
            TestCase test = null ;
            
            if ( testItem.getTestType() != null )
            {
                if ( testItem.getTestType().equals(TestManifestX.TestQuery) )
                    test = new QueryTestSDB(store, testName, results, fileManager, testItem) ;
                
                if ( testItem.getTestType().equals(TestManifestX.TestSurpressed) )
                    test = new SurpressedTest(testName, results, testItem) ;
                
                if ( test == null )
                    System.err.println("Unrecognized test type: "+testItem.getTestType()) ;
            }
            // Default 
            if ( test == null )
                test = new QueryTestSDB(store, testName, results, fileManager, testItem) ;

            Resource action2 = testItem.getAction() ;
            if ( action2.hasProperty(TestManifestX.option))
                System.out.println("OPTION") ;
            
            return test ;
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