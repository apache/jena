/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            22 Feb 2003
 * Filename           $RCSfile: TestOntEvents.java,v $
 * Revision           $Revision: 1.4 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2005-02-21 12:06:04 $
 *               by   $Author: andy_seaborne $
 *
 * (c) Copyright 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology.event.test;



// Imports
///////////////
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.ontology.event.OntEventHandler;
import com.hp.hpl.jena.ontology.impl.test.*;
import com.hp.hpl.jena.rdf.model.*;


/**
 * <p>
 * Test cases for ontology events
 * </p>
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian.Dickinson@hp.com">email</a>)
 * @version Release @release@ ($Id: TestOntEvents.java,v 1.4 2005-02-21 12:06:04 andy_seaborne Exp $)
 */
public class TestOntEvents 
    extends OntTestBase
{

    // Constants
    //////////////////////////////////

    // Static variables
    //////////////////////////////////

    // Instance variables
    //////////////////////////////////

    // Constructors
    //////////////////////////////////

    public TestOntEvents(String name) {
        super(name);
    }


    // External signature methods
    //////////////////////////////////

    // Internal implementation methods
    //////////////////////////////////

    //==============================================================================
    // Inner class definitions
    //==============================================================================

    private static class OntEventTestCase
        extends OntTestBase
    {
        private ModelUpdater m_updater;
        private OntModel m_model;
        private Resource m_expected;
        private boolean m_taleOfExpected = false;
        private boolean m_taleOfUnexpected = false;

        private OntEventTestCase( String name, ModelUpdater upd, String lang, Resource event ) {
            super( name );
            m_updater = upd;
            m_model = ModelFactory.createOntologyModel( lang );
            m_expected = event;
        }
        
        public void runTest() {
            // first register an event handler for the expected event
            m_model.getEventManager().addHandler( m_expected, new OntEventHandler() {
                public void action(Resource event, boolean add, Model source, RDFNode arg0, RDFNode arg1, RDFNode arg2) {
                    
                }
            });
        }
        
        public void setUp() {
            // ensure the ont doc manager is in a consistent state
            OntDocumentManager.getInstance().reset( true );
        }
        
        
    }
    
    private static interface ModelUpdater {
        public void update( OntModel m );
    }
}



/*
    (c) Copyright 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
