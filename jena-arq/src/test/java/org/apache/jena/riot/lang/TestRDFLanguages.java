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

package org.apache.jena.riot.lang;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.WebContent;
import org.apache.jena.riot.resultset.ResultSetLang;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.util.FileUtils;

public class TestRDFLanguages
{
    static { JenaSystem.init(); }
    @Test public void registration_01() { testregistration(RDFLanguages.RDFXML); }
    @Test public void registration_02() { testregistration(RDFLanguages.NTRIPLES); }
    @Test public void registration_03() { testregistration(RDFLanguages.NT); }
    @Test public void registration_04() { testregistration(RDFLanguages.N3); }
    @Test public void registration_05() { testregistration(RDFLanguages.TURTLE); }
    @Test public void registration_06() { testregistration(RDFLanguages.TTL); }
    @Test public void registration_07() { testregistration(RDFLanguages.RDFJSON); }
    @Test public void registration_08() { testregistration(RDFLanguages.NQUADS); }
    @Test public void registration_09() { testregistration(RDFLanguages.NQ); }
    @Test public void registration_10() { testregistration(RDFLanguages.TRIG); }
    @Test public void registration_11() { testregistration(RDFLanguages.RDFNULL); }

    @Test public void registration_01a() { testregistration(Lang.RDFXML); }
    @Test public void registration_02a() { testregistration(Lang.NTRIPLES); }
    @Test public void registration_03a() { testregistration(Lang.NT); }
    @Test public void registration_04a() { testregistration(Lang.N3); }
    @Test public void registration_05a() { testregistration(Lang.TURTLE); }
    @Test public void registration_06a() { testregistration(Lang.TTL); }
    @Test public void registration_07a() { testregistration(Lang.RDFJSON); }
    @Test public void registration_08a() { testregistration(Lang.NQUADS); }
    @Test public void registration_09a() { testregistration(Lang.NQ); }
    @Test public void registration_10a() { testregistration(Lang.TRIG); }
    @Test public void registration_11a() { testregistration(Lang.RDFNULL); }

    private void testregistration(Lang lang)
    {
        assertTrue(RDFLanguages.getRegisteredLanguages().contains(lang), ()->"No registration for "+lang);
    }

    @Test public void lang_01()
    { assertEquals(RDFLanguages.TURTLE, RDFLanguages.shortnameToLang("TTL")); }

    @Test public void lang_02()
    { assertEquals(RDFLanguages.TURTLE, RDFLanguages.shortnameToLang("ttl")); }

    @Test public void lang_03()
    { assertEquals(RDFLanguages.TURTLE, RDFLanguages.shortnameToLang("Turtle")); }

    @Test public void lang_04()
    { assertEquals(RDFLanguages.RDFXML, RDFLanguages.shortnameToLang(FileUtils.langXML)); }

    @Test public void lang_05()
    { assertEquals(RDFLanguages.RDFXML, RDFLanguages.shortnameToLang(FileUtils.langXMLAbbrev)); }

    @Test public void lang_06()
    { assertEquals(RDFLanguages.NTRIPLES, RDFLanguages.shortnameToLang(FileUtils.langNTriple)); }


    @Test public void lang_07()
    { assertEquals(RDFLanguages.NTRIPLES, RDFLanguages.shortnameToLang(WebContent.langNTriples)); }

    @Test public void lang_08()
    { assertEquals(RDFLanguages.NQUADS, RDFLanguages.shortnameToLang(WebContent.langNQuads)); }

    @Test public void lang_09()
    { assertEquals(RDFLanguages.TRIG, RDFLanguages.shortnameToLang(WebContent.langTriG)); }

    @Test public void lang_10()
    { assertEquals(RDFLanguages.RDFJSON, RDFLanguages.shortnameToLang("RDF/JSON")); }

    @Test public void lang_11()
    { assertEquals(RDFLanguages.RDFJSON, RDFLanguages.shortnameToLang(WebContent.langRdfJson)); }

    @Test
    public void testFileExtensionsProvided() {
        for (Lang lang : RDFLanguages.getRegisteredLanguages() )
        {
            if ( RDFLanguages.RDFNULL.equals(lang) )
                continue;
            if ( ResultSetLang.RS_None.equals(lang) )
                continue;
            assertNotNull( lang.getFileExtensions(), ()->lang+" does not have file extensions defined");
            assertTrue( lang.getFileExtensions().size() > 0, ()->lang+" does not have file extensions defined");
        }
    }

    @Test
    public void testFileExtensionUnique() {
        Map<String, Lang> exts = new HashMap<>();

        for (Lang lang1 : RDFLanguages.getRegisteredLanguages() )
        {
            for (String ext : lang1.getFileExtensions())
            {
                Lang lang2 = exts.get(ext);
                assertTrue( lang2 == null || lang1 == lang2, ()->"The "+ext+" file extensions in "+lang1+" was already used");
                exts.put(ext, lang1);
            }
        }

    }

    @Test
    public void testDefaultInExtensions()
    {
        for (Lang lang : RDFLanguages.getRegisteredLanguages() )
        {
            if ( lang.getFileExtensions() == null || lang.getFileExtensions().isEmpty())
                continue;
            assertTrue( lang.getFileExtensions().contains( lang.getFileExtensions().get(0)),
                        ()->lang+" default extension not in file extensions list");
        }
    }

    @Test
    public void testGet()
    {
        for (Lang l : RDFLanguages.getRegisteredLanguages() )
        {
            assertNotNull( RDFLanguages.shortnameToLang( l.getName()), ()->l+" can not be parsed by name" );
        }
    }


}
