/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       ian.dickinson@hp.com
 * Package            Jena2
 * Web site           http://jena.sourceforge.net
 * Created            07-Dec-2004
 * Filename           $RCSfile: TestOntClass.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2004-12-07 13:17:04 $
 *               by   $Author: ian_dickinson $
 *
 * (c) Copyright 2002, 2003, 2004 Hewlett-Packard Development Company, LP
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology.impl.test;


// Imports
///////////////
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.test.ModelTestBase;


/**
 * <p>
 * Misc. tests for OntClass, over and above those in 
 * {@link TestClassExpression}
 * </p>
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian.Dickinson@hp.com">email</a>)
 * @version CVS $Id: TestOntClass.java,v 1.1 2004-12-07 13:17:04 ian_dickinson Exp $
 */
public class TestOntClass 
    extends ModelTestBase
{
    // Constants
    //////////////////////////////////

    // Static variables
    //////////////////////////////////

    private static final String NS = "http://example.com/test#";
    
    // Instance variables
    //////////////////////////////////

    // Constructors
    //////////////////////////////////

    public TestOntClass( String name ) {
        super( name );
    }

    // External signature methods
    //////////////////////////////////

    public void testSuperClassNE() {
        OntModel m = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
        OntClass a = m.createClass( NS + "A" );
        
        assertNull( a.getSuperClass() );
        assertFalse( a.hasSuperClass() );
    }
    
    public void testSubClassNE() {
        OntModel m = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
        OntClass a = m.createClass( NS + "A" );
        
        assertNull( a.getSubClass() );
        assertFalse( a.hasSubClass() );
    }
    
    // Internal implementation methods
    //////////////////////////////////

    //==============================================================================
    // Inner class definitions
    //==============================================================================

}



/*
    (c) Copyright 2002, 2003, 2004 Hewlett-Packard Development Company, LP
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

