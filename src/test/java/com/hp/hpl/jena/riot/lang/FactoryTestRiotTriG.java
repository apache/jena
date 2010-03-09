/*
 * (c) Copyright 2010 Talis Information Ltd.
 * All rights reserved. [See end of file]
 */

package com.hp.hpl.jena.riot.lang ;

import junit.framework.Test ;
import junit.framework.TestSuite ;

import com.hp.hpl.jena.rdf.model.Resource ;
import com.hp.hpl.jena.riot.TestVocabRIOT ;
import com.hp.hpl.jena.sparql.vocabulary.VocabTestQuery ;
import com.hp.hpl.jena.util.junit.TestFactoryManifest ;
import com.hp.hpl.jena.util.junit.TestUtils ;
import com.hp.hpl.jena.vocabulary.RDF ;

public class FactoryTestRiotTriG extends TestFactoryManifest
{

    public static TestSuite make(String manifest, Resource dftTestType, String labelPrefix)
    {
        return new FactoryTestRiotTriG(dftTestType, labelPrefix).process(manifest) ;
    }

    private Resource dftTestType ;
    private String labelPrefix ;

    public FactoryTestRiotTriG(Resource dftTestType, String labelPrefix)
    {
        // FileManager? 
        
        this.dftTestType = dftTestType ;
        this.labelPrefix = labelPrefix ;
    }
    
    @Override
    public Test makeTest(Resource manifest, Resource item, String testName, Resource action, Resource result)
    {
        try
        {
            Resource r = TestUtils.getResource(item, RDF.type) ;
            if ( r == null )
                r = dftTestType ;
            if ( labelPrefix != null )
                testName = labelPrefix+testName ;
            
            Resource input = TestUtils.getResource(action, VocabTestQuery.data) ;
            Resource output = result ; 
            
            String baseIRI = TestVocabRIOT.assumedBaseURI ;
            String x = input.getLocalName() ;
            // Yuk, yuk, yuk.
            baseIRI = baseIRI+x ;
            

            if (r.equals(TestVocabRIOT.TestInOut))
            {
                return new UnitTestTrig(testName, input.getURI(), output.getURI(), baseIRI) ;
            }

            if (r.equals(TestVocabRIOT.TestSyntax))
            {
                return new UnitTestTrigSyntax(testName, input.getURI()) ;
            }

            if (r.equals(TestVocabRIOT.TestBadSyntax))
            {
                return new UnitTestTrigBadSyntax(testName, input.getURI()) ;
            }

            if ( r.equals(TestVocabRIOT.TestSurpressed ))
                return new TestSupressed(testName, null) ;

            System.err.println("Unrecognized test : " + testName) ;
            return null ;

        } catch (Exception ex)
        {
            ex.printStackTrace(System.err) ;
            System.err.println("Failed to grok test : " + testName) ;
            return null ;
        }
    }

}

/*
 * (c) Copyright 2010 Talis Information Ltd.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. The name of the author may not
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */