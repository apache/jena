/*
  (c) Copyright 2003, Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: TestVocabVCARD.java,v 1.2 2003-08-27 13:08:12 andy_seaborne Exp $
*/

package com.hp.hpl.jena.vocabulary.test;

import com.hp.hpl.jena.vocabulary.*;
import junit.framework.*;

/**
    Test that the VCARD identifiers are what they're supposed to be.
    TODO ensure that there are no untested identifiers.
 	@author kers
*/
public class TestVocabVCARD extends VocabTestBase
    {
    public TestVocabVCARD( String name )
        { super( name ); }

    public static TestSuite suite()
        { return new TestSuite( TestVocabVCARD.class ); }
        
    public void testVCARD()
        {
        String ns = "http://www.w3.org/2001/vcard-rdf/3.0#";
        assertEquals( ns, VCARD.getURI() );
        assertResource( ns + "ORGPROPERTIES", VCARD.ORGPROPERTIES );
        assertResource( ns + "ADRTYPES", VCARD.ADRTYPES );
        assertResource( ns + "NPROPERTIES", VCARD.NPROPERTIES );
        assertResource( ns + "EMAILTYPES", VCARD.EMAILTYPES );
        assertResource( ns + "TELTYPES", VCARD.TELTYPES );
        assertResource( ns + "ADRPROPERTIES", VCARD.ADRPROPERTIES );
        assertResource( ns + "TZTYPES", VCARD.TZTYPES );
        assertProperty( ns + "Street", VCARD.Street );
        assertProperty( ns + "AGENT", VCARD.AGENT );
        assertProperty( ns + "SOURCE", VCARD.SOURCE );
        assertProperty( ns + "LOGO", VCARD.LOGO );
        assertProperty( ns + "BDAY", VCARD.BDAY );
        assertProperty( ns + "REV", VCARD.REV );
        assertProperty( ns + "SORT-STRING", VCARD.SORT_STRING );
        assertProperty( ns + "Orgname", VCARD.Orgname );
        assertProperty( ns + "CATEGORIES", VCARD.CATEGORIES );
        assertProperty( ns + "N", VCARD.N );
        assertProperty( ns + "Pcode", VCARD.Pcode );
        assertProperty( ns + "Prefix", VCARD.Prefix );
        assertProperty( ns + "PHOTO", VCARD.PHOTO );
        assertProperty( ns + "FN", VCARD.FN );
        assertProperty( ns + "ORG", VCARD.ORG );
        assertProperty( ns + "Suffix", VCARD.Suffix );
        assertProperty( ns + "CLASS", VCARD.CLASS );
        assertProperty( ns + "ADR", VCARD.ADR );
        assertProperty( ns + "Region", VCARD.Region );
        assertProperty( ns + "GEO", VCARD.GEO );
        assertProperty( ns + "Extadd", VCARD.Extadd );
        assertProperty( ns + "GROUP", VCARD.GROUP );
        assertProperty( ns + "EMAIL", VCARD.EMAIL );
        assertProperty( ns + "UID", VCARD.UID );
        assertProperty( ns + "Family", VCARD.Family );
        assertProperty( ns + "TZ", VCARD.TZ );
        assertProperty( ns + "NAME", VCARD.NAME );
        assertProperty( ns + "Orgunit", VCARD.Orgunit );
        assertProperty( ns + "Country", VCARD.Country );
        assertProperty( ns + "SOUND", VCARD.SOUND );
        assertProperty( ns + "TITLE", VCARD.TITLE );
        assertProperty( ns + "NOTE", VCARD.NOTE );
        assertProperty( ns + "MAILER", VCARD.MAILER );
        assertProperty( ns + "Other", VCARD.Other );
        assertProperty( ns + "Locality", VCARD.Locality );
        assertProperty( ns + "Pobox", VCARD.Pobox );
        assertProperty( ns + "KEY", VCARD.KEY );
        assertProperty( ns + "PRODID", VCARD.PRODID );
        assertProperty( ns + "Given", VCARD.Given );
        assertProperty( ns + "LABEL", VCARD.LABEL );
        assertProperty( ns + "TEL", VCARD.TEL );
        assertProperty( ns + "NICKNAME", VCARD.NICKNAME );
        assertProperty( ns + "ROLE", VCARD.ROLE );
    	}
    }
/*
    (c) Copyright 2003 Hewlett-Packard Development Company, LP
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
