/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.junit;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.sparql.util.Utils;
import com.hp.hpl.jena.sparql.vocabulary.EARL;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDF;

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
    
    public EarlReport(String title, String version, String homepage)
    {
        earl = ModelFactory.createDefaultModel() ;
        earl.setNsPrefix("earl", EARL.getURI()) ;
        earl.setNsPrefix("foaf", FOAF.getURI()) ;
        earl.setNsPrefix("rdf", RDF.getURI()) ;
        earl.setNsPrefix("dc", DC.getURI()) ;
        
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
        if ( title != null )
            system.addProperty(DC.title, title);
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

/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
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