/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.junit;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.vocabulary.EARL;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDFS;

public class EarlReport
{
    /*
     * [ a earl:Assertion;
         earl:assertedBy _8:chime;
         earl:result [ a earl:TestResult;
                 earl:outcome earl:pass];
         earl:subject <http://rdflib.net>;
         earl:test _7:dawg-graph-02].
     */
    
    Model earl = ModelFactory.createDefaultModel() ;
    
    public EarlReport(String label, String title, String version, String homepage)
    {
        Resource system = earl.createResource(EARL.Software);
        if ( label != null )
            system.addProperty(RDFS.label, label);
        if ( title != null )
            system.addProperty(DC.title, title);
        if ( version != null )
            system.addProperty(DCTerms.hasVersion, version);
        if ( homepage != null )
            system.addProperty(FOAF.homepage, earl.createResource(homepage));
    }

    /*return earl.createResource(EARL.Assertion).
  140          addProperty(EARL.test,tests[i])
  141          .addProperty(EARL.mode,
  142                       mode
  143                  )
  144          .addProperty(
  145               EARL.result,
  146               rslt
  147 //                    .addProperty(DC.date,date)
  148          );
    */
    public void success()
    {
        earl.createResource(EARL.TestResult)
                .addProperty(EARL.outcome, EARL.pass) ;
    }
    
    public void failure()
    {
        earl.createResource(EARL.TestResult)
        .addProperty(EARL.outcome, EARL.pass) ;

    }

    public void notApplicable()
    {}
    
    private Resource createAssertion(String testURI, Resource outcome)
    {
        return earl.createResource(EARL.Assertion)
                    .addProperty(EARL.test, testURI)
                    .addProperty(EARL.result, createResult(outcome))
                    .addProperty(EARL.subject, "ARQ") ; // Resource
    }
    
    private Resource createResult(Resource outcome)
    {
        return earl.createResource(EARL.TestResult)
                   .addProperty(EARL.outcome, outcome) ;
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