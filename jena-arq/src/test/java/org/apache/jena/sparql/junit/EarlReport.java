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

package org.apache.jena.sparql.junit;

import org.apache.jena.atlas.lib.DateTimeUtils ;
import org.apache.jena.datatypes.xsd.XSDDatatype ;
import org.apache.jena.rdf.model.* ;
import org.apache.jena.sparql.vocabulary.DOAP ;
import org.apache.jena.sparql.vocabulary.EARL ;
import org.apache.jena.sparql.vocabulary.FOAF ;
import org.apache.jena.vocabulary.DC ;
import org.apache.jena.vocabulary.DCTerms ;
import org.apache.jena.vocabulary.RDF ;
import org.apache.jena.vocabulary.XSD ;

public class EarlReport
{
    // Ref: http://www.w3.org/TR/EARL10-Schema/
    // NB: This vocabulary has changed over time!
    /* An entry looks like:
     * [ rdf:type earl:Assertion;
         earl:assertedBy [ ...]
         earl:result [
                 rdf:type earl:TestResult;
                 earl:outcome earl:passed ];
         earl:subject <thingBeingTested>;
         earl:test <testPerformed> ;
         earl:mode .... ].
     */

    private Model earl = null ;

    private Resource system;

    public EarlReport(String systemURI)
    {
        earl = ModelFactory.createDefaultModel() ;

        earl.setNsPrefix("earl", EARL.getURI()) ;
        earl.setNsPrefix("foaf", FOAF.getURI()) ;
        earl.setNsPrefix("rdf", RDF.getURI()) ;
        earl.setNsPrefix("dc", DC.getURI()) ;
        earl.setNsPrefix("dct", DCTerms.getURI()) ;
        earl.setNsPrefix("doap", DOAP.getURI()) ;
        earl.setNsPrefix("xsd", XSD.getURI()) ;
        earl.setNsPrefix("rdft", "http://www.w3.org/ns/rdftest#");
        // Utils.
        system = (systemURI == null ) ? earl.createResource() : earl.createResource(systemURI) ;
    }

    public Resource getSystem() { return system ; }

    public void success(String testURI) {
        createAssertionResult(testURI, EARL.passed);
    }

    public void failure(String testURI) {
        createAssertionResult(testURI, EARL.failed);
    }

    public void notApplicable(String testURI) {
        createAssertionResult(testURI, EARL.inapplicable);
    }

    public void notTested(String testURI) {
        createAssertionResult(testURI, EARL.untested);
    }

    private void createAssertionResult(String testURI, Resource outcome) {
        Resource result = createResult(outcome);
        Resource assertion = createAssertion(testURI, result);
    }

    /*
     * Required: earl:assertedBy , earl:subject , earl:test , earl:result
     * Recommended: earl:mode
     */

    private Resource createAssertion(String testURI, Resource result) {
        Resource thisTest = earl.createResource(testURI);
        return earl.createResource(EARL.Assertion)
                .addProperty(EARL.test, thisTest)
                .addProperty(EARL.result, result)
                .addProperty(EARL.subject, system)
                .addProperty(EARL.assertedBy, system)
                .addProperty(EARL.mode, EARL.automatic);
    }

    private Resource createResult(Resource outcome) {
        String todayStr = DateTimeUtils.todayAsXSDDateString();
        Literal now = ResourceFactory.createTypedLiteral(todayStr, XSDDatatype.XSDdate);
        return earl.createResource(EARL.TestResult).addProperty(EARL.outcome, outcome).addProperty(DC.date, now);
    }

    public Model getModel() { return earl ; }

    public Model getDescription() { return earl ; }
}
