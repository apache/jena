/*
 	(c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 	All rights reserved.
 	$Id: TestConfigVocabulary.java,v 1.2 2008-12-28 19:32:00 andy_seaborne Exp $
*/

package com.hp.hpl.jena.reasoner.rulesys.test;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.test.ModelTestBase;
import com.hp.hpl.jena.reasoner.ReasonerRegistry;
import com.hp.hpl.jena.vocabulary.*;

/**
    Tests for configuration vocabulary added as part of ModelSpec removal

 	@author kers
*/
public class TestConfigVocabulary extends ModelTestBase
    {
    public TestConfigVocabulary( String name )
        { super( name ); }

    public void testExistingVocabulary()
        {
        assertIsProperty( "name", ReasonerVocabulary.nameP );
        assertIsProperty( "description", ReasonerVocabulary.descriptionP );
        assertIsProperty( "version", ReasonerVocabulary.versionP );
        assertIsProperty( "supports", ReasonerVocabulary.supportsP );
        assertIsProperty( "configurationProperty", ReasonerVocabulary.configurationP );
        assertIsProperty( "individualAsThing", ReasonerVocabulary.individualAsThingP );
        }
    
    public void testPropVocavulary()
        {
        assertIsPropProperty( "derivationLogging", ReasonerVocabulary.PROPderivationLogging );
        assertIsPropProperty( "traceOn", ReasonerVocabulary.PROPtraceOn );
        assertIsPropProperty( "ruleMode", ReasonerVocabulary.PROPruleMode );
        assertIsPropProperty( "enableOWLTranslation", ReasonerVocabulary.PROPenableOWLTranslation );
        assertIsPropProperty( "enableTGCCaching", ReasonerVocabulary.PROPenableTGCCaching );
        assertIsPropProperty( "enableCMPScan", ReasonerVocabulary.PROPenableCMPScan );
        assertIsPropProperty( "setRDFSLevel", ReasonerVocabulary.PROPsetRDFSLevel );
        assertIsPropProperty( "enableFunctorFiltering", ReasonerVocabulary.PROPenableFunctorFiltering );
        }

    public void testDirectVocabulary()
        {
        assertIsDirectProperty( RDFS.subClassOf, ReasonerVocabulary.directSubClassOf );
        assertIsDirectProperty( RDFS.subPropertyOf, ReasonerVocabulary.directSubPropertyOf );
        assertIsDirectProperty( RDF.type, ReasonerVocabulary.directRDFType );
        }

    public void testRuleSetVocabulary()
        {
        assertIsProperty( "ruleSet", ReasonerVocabulary.ruleSet );
        assertIsProperty( "ruleSetURL", ReasonerVocabulary.ruleSetURL );
        assertIsProperty( "hasRule", ReasonerVocabulary.hasRule );
        assertIsProperty( "schemaURL", ReasonerVocabulary.schemaURL );
        }

    private void assertIsDirectProperty( Resource r, Property p )
        {
        assertEquals( ReasonerRegistry.makeDirect( r.getURI() ), p.getURI() );
        }

    private void assertIsProperty( String name, Property p )
        {
        assertEquals( ReasonerVocabulary.getJenaReasonerNS() + name, p.getURI() );
        }

    private void assertIsPropProperty( String name, Property p )
        {
        assertEquals( ReasonerVocabulary.PropURI + "#" + name, p.getURI() );
        }
    }
/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP All rights reserved.
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
