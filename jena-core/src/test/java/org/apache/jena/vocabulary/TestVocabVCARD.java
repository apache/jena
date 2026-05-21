/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.vocabulary;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test that the VCARD identifiers are what they're supposed to be.
 */
public class TestVocabVCARD {
    @Test
    public void testVCARD() {
        String ns = "http://www.w3.org/2001/vcard-rdf/3.0#";
        assertEquals(ns, VCARD.getURI());
        VocabTestLib.assertResource(ns + "ORGPROPERTIES", VCARD.ORGPROPERTIES);
        VocabTestLib.assertResource(ns + "ADRTYPES", VCARD.ADRTYPES);
        VocabTestLib.assertResource(ns + "NPROPERTIES", VCARD.NPROPERTIES);
        VocabTestLib.assertResource(ns + "EMAILTYPES", VCARD.EMAILTYPES);
        VocabTestLib.assertResource(ns + "TELTYPES", VCARD.TELTYPES);
        VocabTestLib.assertResource(ns + "ADRPROPERTIES", VCARD.ADRPROPERTIES);
        VocabTestLib.assertResource(ns + "TZTYPES", VCARD.TZTYPES);
        VocabTestLib.assertProperty(ns + "Street", VCARD.Street);
        VocabTestLib.assertProperty(ns + "AGENT", VCARD.AGENT);
        VocabTestLib.assertProperty(ns + "SOURCE", VCARD.SOURCE);
        VocabTestLib.assertProperty(ns + "LOGO", VCARD.LOGO);
        VocabTestLib.assertProperty(ns + "BDAY", VCARD.BDAY);
        VocabTestLib.assertProperty(ns + "REV", VCARD.REV);
        VocabTestLib.assertProperty(ns + "SORT-STRING", VCARD.SORT_STRING);
        VocabTestLib.assertProperty(ns + "Orgname", VCARD.Orgname);
        VocabTestLib.assertProperty(ns + "CATEGORIES", VCARD.CATEGORIES);
        VocabTestLib.assertProperty(ns + "N", VCARD.N);
        VocabTestLib.assertProperty(ns + "Pcode", VCARD.Pcode);
        VocabTestLib.assertProperty(ns + "Prefix", VCARD.Prefix);
        VocabTestLib.assertProperty(ns + "PHOTO", VCARD.PHOTO);
        VocabTestLib.assertProperty(ns + "FN", VCARD.FN);
        VocabTestLib.assertProperty(ns + "ORG", VCARD.ORG);
        VocabTestLib.assertProperty(ns + "Suffix", VCARD.Suffix);
        VocabTestLib.assertProperty(ns + "CLASS", VCARD.CLASS);
        VocabTestLib.assertProperty(ns + "ADR", VCARD.ADR);
        VocabTestLib.assertProperty(ns + "Region", VCARD.Region);
        VocabTestLib.assertProperty(ns + "GEO", VCARD.GEO);
        VocabTestLib.assertProperty(ns + "Extadd", VCARD.Extadd);
        VocabTestLib.assertProperty(ns + "GROUP", VCARD.GROUP);
        VocabTestLib.assertProperty(ns + "EMAIL", VCARD.EMAIL);
        VocabTestLib.assertProperty(ns + "UID", VCARD.UID);
        VocabTestLib.assertProperty(ns + "Family", VCARD.Family);
        VocabTestLib.assertProperty(ns + "TZ", VCARD.TZ);
        VocabTestLib.assertProperty(ns + "NAME", VCARD.NAME);
        VocabTestLib.assertProperty(ns + "Orgunit", VCARD.Orgunit);
        VocabTestLib.assertProperty(ns + "Country", VCARD.Country);
        VocabTestLib.assertProperty(ns + "SOUND", VCARD.SOUND);
        VocabTestLib.assertProperty(ns + "TITLE", VCARD.TITLE);
        VocabTestLib.assertProperty(ns + "NOTE", VCARD.NOTE);
        VocabTestLib.assertProperty(ns + "MAILER", VCARD.MAILER);
        VocabTestLib.assertProperty(ns + "Other", VCARD.Other);
        VocabTestLib.assertProperty(ns + "Locality", VCARD.Locality);
        VocabTestLib.assertProperty(ns + "Pobox", VCARD.Pobox);
        VocabTestLib.assertProperty(ns + "KEY", VCARD.KEY);
        VocabTestLib.assertProperty(ns + "PRODID", VCARD.PRODID);
        VocabTestLib.assertProperty(ns + "Given", VCARD.Given);
        VocabTestLib.assertProperty(ns + "LABEL", VCARD.LABEL);
        VocabTestLib.assertProperty(ns + "TEL", VCARD.TEL);
        VocabTestLib.assertProperty(ns + "NICKNAME", VCARD.NICKNAME);
        VocabTestLib.assertProperty(ns + "ROLE", VCARD.ROLE);
    }
}
