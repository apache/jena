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

package com.hp.hpl.jena.tdb.junit;

import junit.framework.Test ;
import junit.framework.TestCase ;
import junit.framework.TestSuite ;

import com.hp.hpl.jena.rdf.model.Resource ;
import com.hp.hpl.jena.sparql.junit.EarlReport ;
import com.hp.hpl.jena.sparql.junit.SurpressedTest ;
import com.hp.hpl.jena.sparql.junit.TestItem ;
import com.hp.hpl.jena.sparql.vocabulary.TestManifestX ;
import com.hp.hpl.jena.util.junit.TestFactoryManifest ;

public class TestFactoryTDB extends TestFactoryManifest
{
    public static EarlReport report = null ;
    
    public static void make(TestSuite ts, String manifestFile, String testRootName)
    {
        // for each graph type do
        TestSuite ts2 = makeSuite(manifestFile, testRootName) ;
        ts.addTest(ts2) ;
    }
    
    public static TestSuite makeSuite(String manifestFile, String testRootName)
    {
        TestFactoryTDB f = new TestFactoryTDB(testRootName) ;
        TestSuite ts = f.process(manifestFile) ;
        if ( testRootName != null )
            ts.setName(testRootName+ts.getName()) ;
        return ts ;
    }
    
    // Factory
    
    public String testRootName ;

    public TestFactoryTDB(String testRootName)
    {
        this.testRootName = testRootName ;
    }
    
    @Override
    protected Test makeTest(Resource manifest, Resource entry, String testName, Resource action, Resource result)
    {
        if ( testRootName != null )
            testName = testRootName+testName ;
        
        TestItem testItem = TestItem.create(entry, null) ;
        
        TestCase test = null ;
        
        if ( testItem.getTestType() != null )
        {
            if ( testItem.getTestType().equals(TestManifestX.TestQuery) )
                test = new QueryTestTDB(testName, report, testItem) ;
            
            if ( testItem.getTestType().equals(TestManifestX.TestSurpressed) )
                test = new SurpressedTest(testName, report, testItem) ;
            
            if ( test == null )
                System.err.println("Unrecognized test type: "+testItem.getTestType()) ;
        }
        // Default 
        if ( test == null )
            test = new QueryTestTDB(testName, report, testItem) ;

        return test ;
    }

}
