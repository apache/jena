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

package org.openjena.riot.langsuite ;

import junit.framework.Test ;
import junit.framework.TestSuite ;
import org.openjena.riot.TestVocabRIOT ;

import com.hp.hpl.jena.rdf.model.Resource ;
import com.hp.hpl.jena.sparql.vocabulary.VocabTestQuery ;
import com.hp.hpl.jena.util.junit.TestFactoryManifest ;
import com.hp.hpl.jena.util.junit.TestUtils ;
import com.hp.hpl.jena.vocabulary.RDF ;

public class FactoryTestRiotTurtle extends TestFactoryManifest
{

    public static TestSuite make(String manifest, Resource dftTestType, String labelPrefix)
    {
        return new FactoryTestRiotTurtle(dftTestType, labelPrefix).process(manifest) ;
    }

    private Resource dftTestType ;
    private String labelPrefix ;

    public FactoryTestRiotTurtle(Resource dftTestType, String labelPrefix)
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
//            // Convert baseIRI.
//            Resource inputIRIr = TestUtils.getResource(action, TestVocabRIOT.inputIRI) ;
//            String baseIRI = (inputIRIr == null) ? null : inputIRIr.getURI() ;
            // !!
            
            String baseIRI = TestVocabRIOT.assumedBaseURI ;
            String x = input.getLocalName() ;
            // Yuk, yuk, yuk.
            baseIRI = baseIRI+x ;
            

            if (r.equals(TestVocabRIOT.TestInOut))
            {
                return new UnitTestTurtle(testName, input.getURI(), output.getURI(), baseIRI) ;
            }

            if (r.equals(TestVocabRIOT.TestSyntax))
            {
                return new UnitTestTurtleSyntax(testName, input.getURI()) ;
            }

            if (r.equals(TestVocabRIOT.TestBadSyntax))
            {
                return new UnitTestTurtleBadSyntax(testName, input.getURI()) ;
            }

            // if ( r.equals(TestVocabRIOT.TestSurpeessed ))
            // return new TestSupressed(testName, null) ;

            System.err.println("Unrecognized turtle test : " + testName) ;
            return null ;

        } catch (Exception ex)
        {
            ex.printStackTrace(System.err) ;
            System.err.println("Failed to grok test : " + testName) ;
            return null ;
        }
    }

}
