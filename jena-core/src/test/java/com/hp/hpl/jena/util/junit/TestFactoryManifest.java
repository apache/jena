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

package com.hp.hpl.jena.util.junit;

import java.util.Iterator;

import junit.framework.*;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.JenaException;


public abstract class TestFactoryManifest implements ManifestItemHandler
{
    private TestSuite currentTestSuite = null ;
    private TestSuite testSuite = null ;
    
    public TestFactoryManifest() {}
    
    public TestSuite process(String filename)
    {
        return oneManifest(filename) ;
    }
    
    private TestSuite oneManifest(String filename)
    {
        TestSuite ts1 = new TestSuite() ;
        Manifest m = null ;
        try {
            m = new Manifest(filename) ;
        } catch (JenaException ex)
        { 
            LoggerFactory.getLogger(TestFactoryManifest.class).warn("Failed to load: "+filename+"\n"+ex.getMessage(), ex) ;
            ts1.setName("BROKEN") ;
            return ts1 ;
        }
        if ( m.getName() != null )
            ts1.setName(TestUtils.safeName(m.getName())) ;
        else
            ts1.setName("Unnamed Manifest") ; 

        // Recurse
        for (Iterator <String>iter = m.includedManifests() ; iter.hasNext() ; )
        {
            String n = iter.next() ;
            TestSuite ts2 = oneManifest(n) ;
            currentTestSuite = ts2 ;
            ts1.addTest(ts2) ;
        }
      
        currentTestSuite = ts1 ;
        m.apply(this) ;
        return ts1 ;
    }
    
    protected TestSuite getTestSuite() { return currentTestSuite ; }
    
    /** Handle an item in a manifest */
    @Override
    public final boolean processManifestItem(Resource manifest ,
                                       Resource item ,
                                       String testName ,
                                       Resource action ,
                                       Resource result)
    {
        Test t = makeTest(manifest, item, testName, action, result) ;
        if ( t != null )
            currentTestSuite.addTest(t) ;
        return true ;
    }

    
    protected abstract Test makeTest(Resource manifest ,
                           Resource item ,
                           String testName ,
                           Resource action ,
                           Resource result) ;
    
}
