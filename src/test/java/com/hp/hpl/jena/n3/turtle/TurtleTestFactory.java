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

package com.hp.hpl.jena.n3.turtle;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.hp.hpl.jena.util.junit.TestFactoryManifest;
import com.hp.hpl.jena.util.junit.TestUtils;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;


public class TurtleTestFactory extends TestFactoryManifest
{
    
    public static TestSuite make(String filename)
    {
        return new TurtleTestFactory().process(filename) ;
    }
    
    @Override
    public Test makeTest(Resource manifest, Resource item, String testName, Resource action, Resource result)
    {
        try {
            Resource r = TestUtils.getResource(item, RDF.type) ;
            Resource input = TestUtils.getResource(action, TurtleTestVocab.input) ;
            Resource output = TestUtils.getResource(result, TurtleTestVocab.output) ;
            Resource inputIRIr = TestUtils.getResource(action, TurtleTestVocab.inputIRI) ;
            String baseIRI = (inputIRIr == null)?null:inputIRIr.getURI() ; 
            
            if ( r.equals(TurtleTestVocab.TestInOut))
            {
                return new TestTurtle(testName, input.getURI(), output.getURI(), baseIRI) ;
            }
            
            if ( r.equals(TurtleTestVocab.TestSyntax))
            {
                return new TestSyntax(testName, input.getURI()) ;
            }
            
            if ( r.equals(TurtleTestVocab.TestBadSyntax))
            {
                return new TestBadSyntax(testName, input.getURI()) ;
            }
            
            //if ( r.equals(TurtleTestVocab.TestSurpeessed )) 
            //    return new TestSupressed(testName, null) ;
            
            System.err.println("Unrecognized test : "+testName) ;
            return null ;
            
            
        } catch (Exception ex)
        {
            ex.printStackTrace(System.err) ;
            System.err.println("Failed to grok test : "+testName) ;
            return null ;
        }
        
    }
}
