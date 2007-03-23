/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.n3.turtle.test;

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

/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
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
