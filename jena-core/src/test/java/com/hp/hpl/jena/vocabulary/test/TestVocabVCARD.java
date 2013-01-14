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

package com.hp.hpl.jena.vocabulary.test;

import com.hp.hpl.jena.vocabulary.*;
import junit.framework.*;

/**
    Test that the VCARD identifiers are what they're supposed to be.
    TODO ensure that there are no untested identifiers.
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
