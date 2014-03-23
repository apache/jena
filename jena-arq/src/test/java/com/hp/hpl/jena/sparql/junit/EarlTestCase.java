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

import junit.framework.AssertionFailedError ;
import junit.framework.TestCase ;

import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryFactory ;
import com.hp.hpl.jena.query.Syntax ;
import com.hp.hpl.jena.sparql.ARQException ;
import com.hp.hpl.jena.update.UpdateFactory ;
import com.hp.hpl.jena.update.UpdateRequest ;


public abstract class EarlTestCase extends TestCase
{
    protected EarlReport report = null ;
    protected String testURI = null ;
    private boolean resultRecorded = false ;
    
    protected EarlTestCase(String name, String testURI, EarlReport earl)
    { 
        super(name) ;
        this.report = earl ;
        this.testURI = testURI ;
    }
    
    public void setEARL(EarlReport earl)
    {
        this.report = earl ;
    }
    
    protected Query queryFromString(String qStr)
    {
        Query query = QueryFactory.create(qStr) ;
        return query ;
    }

    protected Query queryFromTestItem(TestItem testItem)
    {
        if ( testItem.getQueryFile() == null )
        {
            fail("Query test file is null") ;
            return null ;
        }
        
        Query query = QueryFactory.read(testItem.getQueryFile(), null, testItem.getFileSyntax()) ;
        return query ;
    }

    protected UpdateRequest updateFromString(String str)
    {
        return UpdateFactory.create(str) ;
    }

    protected UpdateRequest updateFromTestItem(TestItem testItem)
    {
        if ( testItem.getQueryFile() == null )
        {
            fail("Query test file is null") ;
            return null ;
        }

        UpdateRequest request = UpdateFactory.read(testItem.getQueryFile(), Syntax.syntaxSPARQL_11) ;
        return request ;
    }

    @Override
    final protected void runTest() throws Throwable
    { 
        try {
            runTestForReal() ;
            if ( ! resultRecorded )
                success() ;
        } catch (AssertionFailedError ex)
        { 
            if ( ! resultRecorded )
                failure() ;
            throw ex ;
        }
    }
    
    protected abstract void runTestForReal() throws Throwable ;

    // Increase visibility.
    @Override
    protected void setUp() throws Exception {
        setUpTest() ;
    }

    @Override
    protected void tearDown() throws Exception {
        tearDownTest() ;
    }

    // Decouple from JUnit3.
    public void setUpTest()    throws Exception {}
    public void tearDownTest() throws Exception {}

    protected void success()
    {
        note() ;
        if ( report == null ) return ;
        report.success(testURI) ;
    }

    protected void failure()
    {
        note() ;
        if ( report == null ) return ;
        report.failure(testURI) ;
    }

    protected void notApplicable()
    {
        note() ;
        if ( report == null ) return ;
        report.notApplicable(testURI) ;
    }
    
    protected void notTested()
    {
        resultRecorded = true ;
        if ( report == null ) return ;
        report.notTested(testURI) ;
    }
    
    private void note()
    {
        if ( resultRecorded )
            throw new ARQException("Duplictaed test results: "+getName()) ;
        resultRecorded = true ;
    }

}
