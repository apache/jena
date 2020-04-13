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

package org.apache.jena.sparql.junit;

import junit.framework.AssertionFailedError ;
import junit.framework.TestCase ;
import org.apache.jena.query.Query ;
import org.apache.jena.query.QueryFactory ;
import org.apache.jena.query.Syntax ;
import org.apache.jena.sparql.ARQException ;
import org.apache.jena.update.UpdateFactory ;
import org.apache.jena.update.UpdateRequest ;


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

        String fn = testItem.getQueryFile();
        Syntax syntax = ( fn.endsWith(".aru") )? Syntax.syntaxARQ : Syntax.syntaxSPARQL_11;
        
        UpdateRequest request = UpdateFactory.read(fn, syntax);
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
    protected void setUp() {
        setUpTest() ;
    }

    @Override
    protected void tearDown() {
        tearDownTest() ;
    }

    // Decouple from JUnit3.
    public void setUpTest() {}
    public void tearDownTest() {}

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
