/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       ian.dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            11-Sep-2003
 * Filename           $RCSfile: TestConsistency.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2004-05-18 14:50:40 $
 *               by   $Author: ian_dickinson $
 *
 * (c) Copyright 2001, 2002, 2003, Hewlett-Packard Development Company, LP
 * [See end of file]
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.reasoner.dig.test;



// Imports
///////////////
import java.util.Iterator;

import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.reasoner.ReasonerRegistry;
import com.hp.hpl.jena.reasoner.ValidityReport;
import com.hp.hpl.jena.reasoner.dig.*;
import com.hp.hpl.jena.vocabulary.OWL;

import junit.framework.*;



/**
 * <p>
 * Abstract test harness for DIG reasoners 
 * </p>
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian.Dickinson@hp.com">email</a>)
 * @version Release @release@ ($Id: TestConsistency.java,v 1.1 2004-05-18 14:50:40 ian_dickinson Exp $)
 */
public class TestConsistency 
    extends TestCase
{
    // Constants
    //////////////////////////////////

    // Static variables
    //////////////////////////////////

    // Instance variables
    //////////////////////////////////

    // Constructors
    //////////////////////////////////

    // External signature methods
    //////////////////////////////////

    public void testConsistent0() {
        String NS = "http://example.org/foo#";
        
        OntModel base = ModelFactory.createOntologyModel( OntModelSpec.OWL_DL_MEM, null );
        OntClass F0 = base.createClass( NS + "F0" );
        OntClass F1 = base.createClass( NS + "F1" );
        F0.addDisjointWith( F1 );
        Individual i0 = base.createIndividual( NS + "i0", OWL.Thing );
        i0.setRDFType( F0 );
        
        DIGReasoner r = (DIGReasoner) ReasonerRegistry.theRegistry().create( DIGReasonerFactory.URI, null );
        
        OntModelSpec spec = new OntModelSpec( OntModelSpec.OWL_DL_MEM );
        spec.setReasoner( r );
        OntModel m = ModelFactory.createOntologyModel( spec, base );
        assertTrue( "KB should be consistent", m.validate().isValid() );
    }

    public void testConsistent1() {
        String NS = "http://example.org/foo#";
        
        OntModel base = ModelFactory.createOntologyModel( OntModelSpec.OWL_DL_MEM, null );
        OntClass F0 = base.createClass( NS + "F0" );
        OntClass F1 = base.createClass( NS + "F1" );
        F0.addDisjointWith( F1 );
        Individual i0 = base.createIndividual( NS + "i0", OWL.Thing );
        i0.setRDFType( F0 );
        i0.addRDFType( F1 );
        
        DIGReasoner r = (DIGReasoner) ReasonerRegistry.theRegistry().create( DIGReasonerFactory.URI, null );
        
        OntModelSpec spec = new OntModelSpec( OntModelSpec.OWL_DL_MEM );
        spec.setReasoner( r );
        OntModel m = ModelFactory.createOntologyModel( spec, base );
        ValidityReport report = m.validate();
        
        if (!report.isValid()) {
            for (Iterator i = report.getReports(); i.hasNext(); ) {
                ValidityReport.Report rp = (ValidityReport.Report) i.next();
                LogFactory.getLog( getClass() ).debug( "Problem report: " + rp.type + " - " + rp.description );
            }
        }
        assertFalse( "KB should not be consistent", m.validate().isValid() );
    }

    public void testConsistent2() {
        String NS = "http://example.org/foo#";
        
        OntModel base = ModelFactory.createOntologyModel( OntModelSpec.OWL_DL_MEM, null );
        OntClass F0 = base.createClass( NS + "F0" );
        OntClass F1 = base.createClass( NS + "F1" );
        OntClass F2 = base.createClass( NS + "F2" );
        
        F0.addDisjointWith( F1 );
        F2.addSuperClass( F0 );
        F2.addSuperClass( F1 );
        
        DIGReasoner r = (DIGReasoner) ReasonerRegistry.theRegistry().create( DIGReasonerFactory.URI, null );
        
        OntModelSpec spec = new OntModelSpec( OntModelSpec.OWL_DL_MEM );
        spec.setReasoner( r );
        OntModel m = ModelFactory.createOntologyModel( spec, base );
        ValidityReport report = m.validate();
        
        if (!report.isValid()) {
            for (Iterator i = report.getReports(); i.hasNext(); ) {
                ValidityReport.Report rp = (ValidityReport.Report) i.next();
                LogFactory.getLog( getClass() ).debug( "Problem report: " + rp.type + " - " + rp.description );
            }
        }
        assertFalse( "KB should not be consistent", m.validate().isValid() );
    }

    // Internal implementation methods
    //////////////////////////////////

    //==============================================================================
    // Inner class definitions
    //==============================================================================

}


/*
 *  (c) Copyright 2001, 2002, 2003 Hewlett-Packard Development Company, LP
 *  All rights reserved.
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
