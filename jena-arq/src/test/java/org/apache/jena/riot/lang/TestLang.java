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

import java.util.HashMap ;
import java.util.Map ;

import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFLanguages ;
import org.apache.jena.riot.WebContent ;
import org.junit.Assert ;
import org.junit.Test ;

import com.hp.hpl.jena.util.FileUtils ;

public class TestLang extends BaseTest
{
    @Test public void registration_01() { testregistration(RDFLanguages.RDFXML) ; }
    @Test public void registration_02() { testregistration(RDFLanguages.NTRIPLES) ; }
    @Test public void registration_03() { testregistration(RDFLanguages.NT) ; }
    @Test public void registration_04() { testregistration(RDFLanguages.N3) ; } 
    @Test public void registration_05() { testregistration(RDFLanguages.TURTLE) ; }   
    @Test public void registration_06() { testregistration(RDFLanguages.TTL) ; }
    @Test public void registration_07() { testregistration(RDFLanguages.RDFJSON) ; }   
    @Test public void registration_08() { testregistration(RDFLanguages.NQUADS) ; }
    @Test public void registration_09() { testregistration(RDFLanguages.NQ) ; }
    @Test public void registration_10() { testregistration(RDFLanguages.TRIG) ; }
    @Test public void registration_11() { testregistration(RDFLanguages.RDFNULL) ; }

    @Test public void registration_01a() { testregistration(Lang.RDFXML) ; }
    @Test public void registration_02a() { testregistration(Lang.NTRIPLES) ; }
    @Test public void registration_03a() { testregistration(Lang.NT) ; }
    @Test public void registration_04a() { testregistration(Lang.N3) ; } 
    @Test public void registration_05a() { testregistration(Lang.TURTLE) ; }   
    @Test public void registration_06a() { testregistration(Lang.TTL) ; }
    @Test public void registration_07a() { testregistration(Lang.RDFJSON) ; }   
    @Test public void registration_08a() { testregistration(Lang.NQUADS) ; }
    @Test public void registration_09a() { testregistration(Lang.NQ) ; }
    @Test public void registration_10a() { testregistration(Lang.TRIG) ; }
    @Test public void registration_11a() { testregistration(Lang.RDFNULL) ; }

    private void testregistration(Lang lang)
    {
        assertTrue("No registration for "+lang, RDFLanguages.getRegisteredLanguages().contains(lang)) ;
    }
    
    @Test public void lang_01()
    { assertEquals(RDFLanguages.TURTLE, RDFLanguages.shortnameToLang("TTL")) ; }

    @Test public void lang_02()
    { assertEquals(RDFLanguages.TURTLE, RDFLanguages.shortnameToLang("ttl")) ; }

    @Test public void lang_03()
    { assertEquals(RDFLanguages.TURTLE, RDFLanguages.shortnameToLang("Turtle")) ; }

    @Test public void lang_04()
    { assertEquals(RDFLanguages.RDFXML, RDFLanguages.shortnameToLang(FileUtils.langXML)) ; }

    @Test public void lang_05()
    { assertEquals(RDFLanguages.RDFXML, RDFLanguages.shortnameToLang(FileUtils.langXMLAbbrev)) ; }

    @Test public void lang_06()
    { assertEquals(RDFLanguages.NTRIPLES, RDFLanguages.shortnameToLang(FileUtils.langNTriple)) ; }
    
    
    @Test public void lang_07()
    { assertEquals(RDFLanguages.NTRIPLES, RDFLanguages.shortnameToLang(WebContent.langNTriples)) ; }
    
    @Test public void lang_08()
    { assertEquals(RDFLanguages.NQUADS, RDFLanguages.shortnameToLang(WebContent.langNQuads)) ; }
    
    @Test public void lang_09()
    { assertEquals(RDFLanguages.TRIG, RDFLanguages.shortnameToLang(WebContent.langTriG)) ; }

    @Test public void lang_10()
    { assertEquals(RDFLanguages.RDFJSON, RDFLanguages.shortnameToLang("RDF/JSON")) ; }

    @Test public void lang_11()
    { assertEquals(RDFLanguages.RDFJSON, RDFLanguages.shortnameToLang(WebContent.langRdfJson)) ; }

    @Test
    public void testFileExtensionsProvided()
    {
        for (Lang l : RDFLanguages.getRegisteredLanguages() )
        {
            if ( RDFLanguages.RDFNULL.equals(l) )
                continue ;
            Assert.assertNotNull( l+" does not have file extensions defined", l.getFileExtensions());
            Assert.assertTrue( l+" does not have file extensions defined", l.getFileExtensions().size() > 0);
        }
    }
    
    @Test
    public void testFileExtensionUnique()
    {
        Map<String, Lang> exts = new HashMap<>();
        
        for (Lang lang1 : RDFLanguages.getRegisteredLanguages() )
        {
            for (String ext : lang1.getFileExtensions())
            {
                Lang lang2 = exts.get(ext) ;
                Assert.assertTrue( "The "+ext+" file extensions in "+lang1+" was already used", lang2 == null || lang1 == lang2) ;
                exts.put(ext, lang1) ;
            }
        }
        
    }
    
    @Test
    public void testDefaultInExtensions()
    {
        for (Lang l : RDFLanguages.getRegisteredLanguages() )
        {
            if ( RDFLanguages.RDFNULL.equals(l) )
                continue ;
            Assert.assertTrue( l+" default extension not in file extensions list", l.getFileExtensions().contains( l.getFileExtensions().get(0))  );
        }
    }
    
    @Test
    public void testGet()
    {
        for (Lang l : RDFLanguages.getRegisteredLanguages() )
        {
            Assert.assertNotNull( l+" can not be parsed by name", RDFLanguages.shortnameToLang( l.getName())  );
        }
    }

    
}
