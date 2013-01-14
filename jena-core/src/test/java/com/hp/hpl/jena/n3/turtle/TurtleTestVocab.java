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

package com.hp.hpl.jena.n3.turtle ;

import com.hp.hpl.jena.rdf.model.*;
 
/**
 * Vocabulary definitions from TurtleTestVocab.ttl 
 */
public class TurtleTestVocab {
    /** <p>The RDF model that holds the vocabulary terms</p> */
    private static Model m_model = ModelFactory.createDefaultModel();
    
    /** <p>The namespace of the vocabulary as a string</p> */
    public static final String NS = "http://jena.hpl.hp.com/2005/12/test-turtle#";
    
    /** <p>The namespace of the vocabulary as a string</p>
     *  @see #NS */
    public static String getURI() {return NS;}
    
    /** <p>The namespace of the vocabulary as a resource</p> */
    public static final Resource NAMESPACE = m_model.createResource( NS );
    
    /** <p>Declare a IRI for the test input</p> */
    public static final Property inputIRI = m_model.createProperty( "http://jena.hpl.hp.com/2005/12/test-turtle#inputIRI" );
    
    /** <p>Output of a test</p> */
    public static final Property output = m_model.createProperty( "http://jena.hpl.hp.com/2005/12/test-turtle#output" );
    
    /** <p>Input to a test</p> */
    public static final Property input = m_model.createProperty( "http://jena.hpl.hp.com/2005/12/test-turtle#input" );
    
    public static final Resource TestInOut = m_model.createResource( "http://jena.hpl.hp.com/2005/12/test-turtle#TestInOut" );
    
    public static final Resource TestBadSyntax = m_model.createResource( "http://jena.hpl.hp.com/2005/12/test-turtle#TestBadSyntax" );
    
    public static final Resource TestSyntax = m_model.createResource( "http://jena.hpl.hp.com/2005/12/test-turtle#TestSyntax" );
    
    public static final Resource Test = m_model.createResource( "http://jena.hpl.hp.com/2005/12/test-turtle#Test" );
    
}
