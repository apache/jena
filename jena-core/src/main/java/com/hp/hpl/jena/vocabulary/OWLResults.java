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

package com.hp.hpl.jena.vocabulary;

import com.hp.hpl.jena.rdf.model.*;
 
/**
 * The vocabulary used by WebOnt to encode results of test runs.
 * <p>
 * Vocabulary definitions from file:data/resultsOntology.rdf
 */
public class OWLResults {
    /** <p>The RDF model that holds the vocabulary terms</p> */
    private static Model m_model = ModelFactory.createDefaultModel();
    
    /** <p>The namespace of the vocabalary as a string ({@value})</p> */
    public static final String NS = "http://www.w3.org/2002/03owlt/resultsOntology#";
    
    /** <p>The namespace of the vocabalary as a string</p>
     *  @see #NS */
    public static String getURI() {return NS;}
    
    /** <p>The namespace of the vocabalary as a resource</p> */
    public static final Resource NAMESPACE = m_model.createResource( NS );
    
    /** <p>A web page presenting the output of the test run, including system-specific 
     *  additional details.</p>
     */
    public static final Property output = m_model.createProperty( "http://www.w3.org/2002/03owlt/resultsOntology#output" );
    
    
    /** <p>The test in question, such as an otest:PositiveEntailmentTest.</p> */
    public static final Property test = m_model.createProperty( "http://www.w3.org/2002/03owlt/resultsOntology#test" );
    
    /** <p>Syntactic tests.</p> */
    public static final Property syntacticLevelTestFrom = m_model.createProperty( "http://www.w3.org/2002/03owlt/resultsOntology#syntacticLevelTestFrom" );
    
    /** <p>The complete system instance on which the test was run, conceptually including 
     *  software and hardware components. No range restriction is provided, however, 
     *  so the details are flexible; providing an rdfs:label and rdfs:comment may 
     *  be sufficient for many applications.</p>
     */
    public static final Property system = m_model.createProperty( "http://www.w3.org/2002/03owlt/resultsOntology#system" );
    
    /** <p>The point in time at which the test run started; an <a xmlns="http://www.w3.org/1999/xhtml" 
     *  href="http://www.w3.org/TR/xmlschema-2/#dateTime">xsd:dateTime</a>.</p>
     */
    public static final Property begins = m_model.createProperty( "http://www.w3.org/2002/03owlt/resultsOntology#begins" );
    
    /** <p>The time taken for the test to run (as far as it did run) on the tested system; 
     *  an <a xmlns="http://www.w3.org/1999/xhtml" href="http://www.w3.org/TR/xmlschema-2/#duration">xsd:duration</a>. 
     *  This value depends on many factors, of course, such as the performance of 
     *  the hardware components of the tested system. If the system is opaque, these 
     *  values should only be used for comparison against other tests run on same 
     *  system.</p>
     */
    public static final Property duration = m_model.createProperty( "http://www.w3.org/2002/03owlt/resultsOntology#duration" );
    
    /** <p>An event where some system attempts to pass some test.</p> */
    public static final Resource TestRun = m_model.createResource( "http://www.w3.org/2002/03owlt/resultsOntology#TestRun" );
    
    /** <p>A TestRun where the system's behavior does not pass the test, but also does 
     *  not fail. Typically this is caused by behaving in a way the system SHOULD 
     *  NOT. For OWL tests, returning an UNKNOWN for most tests should be reported 
     *  as an IncompleteRun.</p>
     */
    public static final Resource IncompleteRun = m_model.createResource( "http://www.w3.org/2002/03owlt/resultsOntology#IncompleteRun" );
    
    /** <p>A TestRun where the system's behavior fails the test, violating a MUST NOT.</p> */
    public static final Resource FailingRun = m_model.createResource( "http://www.w3.org/2002/03owlt/resultsOntology#FailingRun" );
    
    /** <p>A TestRun where the system's behavior is sufficient for passing the test.</p> */
    public static final Resource PassingRun = m_model.createResource( "http://www.w3.org/2002/03owlt/resultsOntology#PassingRun" );
    
}
