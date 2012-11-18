/**
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

package org.apache.jena.riot;

import org.apache.jena.riot.Lang2 ;
import org.apache.jena.riot.Langs ;
import org.junit.Test ;
import org.openjena.atlas.junit.BaseTest ;

public class TestLangRIOT extends BaseTest
{
    @Test public void lang_01() { test(Langs.langNTriples, "NT") ; }
    @Test public void lang_02() { test(Langs.langNTriples, "N-Triples") ; }
    @Test public void lang_03() { test(Langs.langNTriples, "N-TRIPLES") ; }
    @Test public void lang_04() { test(Langs.langNTriples, "NTRIPLE") ; }
    @Test public void lang_05() { test(Langs.langNTriples, "NTRIPLES") ; }
    
    @Test public void lang_11() { test(Langs.langTurtle, "TTL") ; }
    @Test public void lang_12() { test(Langs.langTurtle, "TUrtLE") ; }

    @Test public void lang_21() { test(Langs.langRDFXML, "RDF/XML") ; }
    @Test public void lang_22() { test(Langs.langRDFXML, "RDFXML") ; }
    @Test public void lang_23() { test(Langs.langRDFXML, "RDF/XML-ABBREV") ; }
    
    @Test public void lang_30() { test(Langs.langRDFJSON, "RDFJSON") ; }
    @Test public void lang_31() { test(Langs.langRDFJSON, "RDF/json") ; }

    @Test public void lang_40() { test(Langs.langNQuads,  "N-QUADS") ; }
    @Test public void lang_41() { test(Langs.langNQuads, "NQuads") ; }
    
    @Test public void lang_50() { test(Langs.langTriG,  "TriG") ; }
    @Test public void lang_51() { test(Langs.langTriG, "trig") ; }
    @Test public void lang_52() { test(Langs.langTriG, "TRIG") ; }
    
    @Test public void guess_01() { guess("D.nt", Langs.langNTriples) ; }
    @Test public void guess_02() { guess("D.ttl.nt", Langs.langNTriples) ; }

    @Test public void guess_03() { guess("D.ttl", Langs.langTurtle) ; }

    @Test public void guess_04() { guess("D.rdf", Langs.langRDFXML) ; }
    @Test public void guess_05() { guess("D.owl", Langs.langRDFXML) ; }
    
    @Test public void guess_06() { guess("D.rj", Langs.langRDFJSON) ; }
    @Test public void guess_07() { guess("D.json", Langs.langRDFJSON) ; }

    @Test public void guess_08() { guess("D.nq", Langs.langNQuads) ; }
    @Test public void guess_09() { guess("D.trig", Langs.langTriG) ; }
    
    private void test(Lang2 expected, String string)
    {
        Lang2 lang = Langs.nameToLang(string) ;
        assertEquals(expected, lang) ;
    }

    private void guess(String string, Lang2 expected)
    {
        Lang2 lang = Langs.guess(string) ;
        assertEquals(expected, lang) ;
    }

}

