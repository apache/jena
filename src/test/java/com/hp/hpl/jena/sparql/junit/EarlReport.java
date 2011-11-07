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

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype ;
import com.hp.hpl.jena.rdf.model.Literal ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.rdf.model.Resource ;
import com.hp.hpl.jena.rdf.model.ResourceFactory ;
import com.hp.hpl.jena.sparql.util.Utils ;
import com.hp.hpl.jena.sparql.vocabulary.EARL ;
import com.hp.hpl.jena.sparql.vocabulary.FOAF ;
import com.hp.hpl.jena.vocabulary.DC ;
import com.hp.hpl.jena.vocabulary.DCTerms ;
import com.hp.hpl.jena.vocabulary.RDF ;

public class EarlReport
{
    // Ref: http://www.w3.org/TR/EARL10-Schema/
    
    /* An entry looks like:
     * [ rdf:type earl:Assertion;
         earl:assertedBy [ ...] 
         earl:result [ 
                 rdf:type earl:TestResult;
                 earl:outcome earl:pass ];
         earl:subject <thingBeingTested>;
         earl:test <testPerformed> ;
         earl:mode .... ].
     */
    
    Model earl = null ;
    Resource system = null ;
    Resource reporter = null ;

    /* Required:
     * Recommended: DC title
     * Optional: dc:hasVersion, dc:description, homepage
     */
    
    public EarlReport(String name, String version, String homepage)
    {
        earl = ModelFactory.createDefaultModel() ;
        
        earl.setNsPrefix("earl", EARL.getURI()) ;
        earl.setNsPrefix("foaf", FOAF.getURI()) ;
        earl.setNsPrefix("rdf", RDF.getURI()) ;
        earl.setNsPrefix("dc", DC.getURI()) ;
        earl.setNsPrefix("dct", DCTerms.getURI()) ;
        /*
        <earl:Software rdf:about="#tool">
          <dc:title xml:lang="en">Cool Tool</dc:title>
          <dc:description xml:lang="en">My favorite tool!</dc:description>
          <foaf:homepage rdf:resource="http://example.org/tools/#cool"/>
          <dct:hasVersion>1.0.3</dct:hasVersion>
        </earl:Software>
        */
        
        // Utils.
        system = earl.createResource(EARL.Software);
        if ( name != null )
            system.addProperty(DC.title, name);
        if ( version != null )
            system.addProperty(DCTerms.hasVersion, version);
        if ( homepage != null )
            system.addProperty(FOAF.homepage, earl.createResource(homepage));
        
        // Can be a person or a thing.
        // But here it is automated tests unless told otherwise..
        reporter = system ;
    }
    
    public Resource getSystem() { return system ; }
    
    public Resource getReporter() { return reporter ; }
    public void setReporter(Resource reporter) { this.reporter = reporter ; }
    
    public void success(String testURI)
    { 
        createAssertionResult(testURI, EARL.pass) ;
    }
    
    public void failure(String testURI)
    {
        createAssertionResult(testURI, EARL.fail) ;
    }

    public void notApplicable(String testURI)
    {
        createAssertionResult(testURI, EARL.notApplicable);
    }
    
    public void notTested(String testURI)
    {
        createAssertionResult(testURI, EARL.notTested);
    }
    
    private void createAssertionResult(String testURI, Resource outcome)
    {
        Resource result = createResult(outcome) ;
        Resource assertion = createAssertion(testURI, result) ;
    }

    /* 
    *  Required: earl:assertedBy , earl:subject , earl:test , earl:result
    *  Recommended: earl:mode 
    */
    
    private Resource createAssertion(String testURI, Resource result)
    {
        Resource thisTest = earl.createResource(testURI) ;
        return earl.createResource(EARL.Assertion)
                    .addProperty(EARL.test, thisTest)
                    .addProperty(EARL.result, result)
                    .addProperty(EARL.subject, system)
                    .addProperty(EARL.assertedBy, system)
                    .addProperty(EARL.mode, EARL.automatic) ;
    }
    
    private Resource createResult(Resource outcome)
    {
//        String nowStr = Utils.nowAsXSDDateTimeString() ;
//        
//        Literal now = 
//            ResourceFactory.createTypedLiteral(nowStr, XSDDatatype.XSDdateTime) ;

        String todayStr = Utils.todayAsXSDDateString() ;
        
        Literal now = 
            ResourceFactory.createTypedLiteral(todayStr, XSDDatatype.XSDdate) ;
            
        return earl.createResource(EARL.TestResult)
                   .addProperty(EARL.outcome, outcome)
                   .addProperty(DC.date, now) ;
    }
        
    
    public Model getModel() { return earl ; }
    
    public Model getDescription() { return earl ; }
}
