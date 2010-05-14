/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.junit;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.junit.TestFactoryManifest;

import com.hp.hpl.jena.sparql.core.DataFormat;
import com.hp.hpl.jena.sparql.junit.EarlReport;
import com.hp.hpl.jena.sparql.junit.SurpressedTest;
import com.hp.hpl.jena.sparql.junit.TestItem;
import com.hp.hpl.jena.sparql.vocabulary.TestManifestX;

import com.hp.hpl.jena.query.Syntax;

import com.hp.hpl.jena.tdb.sys.DatasetGraphMakerTDB;

public class TestFactoryTDB extends TestFactoryManifest
{
    public static EarlReport report = null ;
    
    public static void make(TestSuite ts, String manifestFile, String testRootName, DatasetGraphMakerTDB factory)
    {
        // for each graph type do
        TestSuite ts2 = makeSuite(manifestFile, testRootName, factory) ;
        ts.addTest(ts2) ;
    }
    
    public static TestSuite makeSuite(String manifestFile, String testRootName, DatasetGraphMakerTDB factory)
    {
        TestFactoryTDB f = new TestFactoryTDB(testRootName, factory) ;
        TestSuite ts = f.process(manifestFile) ;
        if ( testRootName != null )
            ts.setName(testRootName+ts.getName()) ;
        return ts ;
    }
    
    // Factory
    
    public String testRootName ;
    private DatasetGraphMakerTDB factory ;

    public TestFactoryTDB(String testRootName, DatasetGraphMakerTDB factory)
    {
        this.testRootName = testRootName ;
        this.factory = factory ;
    }
    
    @Override
    protected Test makeTest(Resource manifest, Resource entry, String testName, Resource action, Resource result)
    {
        if ( testRootName != null )
            testName = testRootName+testName ;
        
        TestItem testItem = TestItem.create(entry, null, Syntax.syntaxARQ, DataFormat.langXML) ;
        
        TestCase test = null ;
        
        if ( testItem.getTestType() != null )
        {
            if ( testItem.getTestType().equals(TestManifestX.TestQuery) )
                test = new QueryTestTDB(testName, report, testItem, factory) ;
            
            if ( testItem.getTestType().equals(TestManifestX.TestSurpressed) )
                test = new SurpressedTest(testName, report, testItem) ;
            
            if ( test == null )
                System.err.println("Unrecognized test type: "+testItem.getTestType()) ;
        }
        // Default 
        if ( test == null )
            test = new QueryTestTDB(testName, report, testItem, factory) ;

        return test ;
    }

}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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