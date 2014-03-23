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

package com.hp.hpl.jena.sdb.test.junit;

import static com.hp.hpl.jena.sparql.junit.TestQueryUtils.getQuerySyntax ;
import junit.framework.Test ;
import junit.framework.TestCase ;
import junit.framework.TestSuite ;

import com.hp.hpl.jena.query.Syntax ;
import com.hp.hpl.jena.rdf.model.Resource ;
import com.hp.hpl.jena.sdb.StoreDesc ;
import com.hp.hpl.jena.sdb.util.Pair ;
import com.hp.hpl.jena.sparql.junit.EarlReport ;
import com.hp.hpl.jena.sparql.junit.QueryTestException ;
import com.hp.hpl.jena.sparql.junit.SurpressedTest ;
import com.hp.hpl.jena.sparql.junit.TestItem ;
import com.hp.hpl.jena.sparql.vocabulary.TestManifestX ;
import com.hp.hpl.jena.util.junit.TestFactoryManifest ;

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
        for ( Pair<String, StoreDesc> p : StoreList.stores(storeList) )
        {
            String label = p.car();
            StoreDesc storeDesc = p.cdr() ;
            if ( label != null && !label.equals("") )
                label = label+" - " ;
            TestSuite ts2 = make(storeDesc, manifestFile, label) ;
            ts.addTest(ts2) ;
        }
    }
    
    static public TestSuite make(StoreDesc storeDesc, String manifestFile, String testRootName) 
    {
        QueryTestSDBFactory f = new QueryTestSDBFactory(storeDesc, testRootName) ;
        TestSuite ts = f.process(manifestFile) ;
        if ( testRootName != null )
            ts.setName(testRootName+ts.getName()) ;
        return ts ;
    }

    StoreDesc storeDesc ;
    private String testRootName ;
    
    private QueryTestSDBFactory(StoreDesc storeDesc, String testRootName)
    {
        this.storeDesc = storeDesc ;
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
            if ( ! querySyntax.equals(Syntax.syntaxARQ) &&
                ! querySyntax.equals(Syntax.syntaxSPARQL) )
                throw new QueryTestException("Unknown syntax: "+querySyntax) ;
        }

        TestItem testItem = TestItem.create(entry, TestManifestX.TestQuery) ;
        TestCase test = null ;

        if ( testItem.getTestType().equals(TestManifestX.TestQuery) )
            test = new QueryTestSDB(storeDesc, testName, results, testItem) ;

        if ( testItem.getTestType().equals(TestManifestX.TestSurpressed) )
            test = new SurpressedTest(testName, results, testItem) ;

        if ( test == null )
            System.err.println("Unrecognized test type: "+testItem.getTestType()) ;

        Resource action2 = testItem.getAction() ;
        if ( action2.hasProperty(TestManifestX.option))
            System.out.println("OPTION") ;

        return test ;
    }
}
