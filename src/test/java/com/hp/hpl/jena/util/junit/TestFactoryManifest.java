/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
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

/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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