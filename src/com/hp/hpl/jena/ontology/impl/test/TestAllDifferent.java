/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            27-May-2003
 * Filename           $RCSfile: TestAllDifferent.java,v $
 * Revision           $Revision: 1.8 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2005-02-21 12:07:13 $
 *               by   $Author: andy_seaborne $
 *
 * (c) Copyright 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology.impl.test;



// Imports
///////////////
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.RDFNode;

import junit.framework.*;


/**
 * <p>
 * Unit tests for the AllDifferent declaration.
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: TestAllDifferent.java,v 1.8 2005-02-21 12:07:13 andy_seaborne Exp $
 */
public class TestAllDifferent
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
    
    static public TestSuite suite() {
        return new TestAllDifferent( "TestAllDifferent" );
    }
    
    public TestAllDifferent( String name ) {
        super( name );
    }
    

    // External signature methods
    //////////////////////////////////

    public OntTestCase[] getTests() {
        return new OntTestCase[] {
            new OntTestCase( "AllDifferent.distinctMembers", true, true, false, false ) {
                public void ontTest( OntModel m ) throws Exception {
                    Profile prof = m.getProfile();
                    AllDifferent a = m.createAllDifferent();
                    OntResource b = (OntResource) m.getResource( NS + "b" ).as( OntResource.class );
                    OntResource c = (OntResource) m.getResource( NS + "c" ).as( OntResource.class );
                    
                    a.addDistinctMember( b );
                    assertEquals( "Cardinality should be 1", 1, a.getCardinality( prof.DISTINCT_MEMBERS() ) );
                    assertEquals( "List size should be 1", 1, a.getDistinctMembers().size() );
                    assertTrue( "a should have b as distinct", a.hasDistinctMember( b ) );
                    
                    a.addDistinctMember( c );
                    assertEquals( "Cardinality should be 1", 1, a.getCardinality( prof.DISTINCT_MEMBERS() ) );
                    assertEquals( "List size should be 2", 2, a.getDistinctMembers().size() );
                    iteratorTest( a.listDistinctMembers(), new Object[] {b, c} );
                    
                    assertTrue( "a should have b as distinct", a.hasDistinctMember( b ) );
                    assertTrue( "a should have c as distinct", a.hasDistinctMember( c ) );
                    
                    a.setDistinctMembers( m.createList( new RDFNode[] {b} ) );
                    assertEquals( "Cardinality should be 1", 1, a.getCardinality( prof.DISTINCT_MEMBERS() ) );
                    assertEquals( "List size should be 1", 1, a.getDistinctMembers().size() );
                    assertTrue( "a should have b as distinct", a.hasDistinctMember( b ) );
                    assertTrue( "a should not have c as distinct", !a.hasDistinctMember( c ) );
                    
                    a.removeDistinctMember( b );
                    assertTrue( "a should have not b as distinct", !a.hasDistinctMember( b ) );
                }
            },
        };
    }
    
    // Internal implementation methods
    //////////////////////////////////

    //==============================================================================
    // Inner class definitions
    //==============================================================================

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
