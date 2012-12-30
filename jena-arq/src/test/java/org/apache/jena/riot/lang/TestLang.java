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
    static { RDFLanguages.init() ; }
    
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
            Assert.assertNotNull( l+" does not have file extensions defined", l.getFileExtensions());
            Assert.assertTrue( l+" does not have file extensions defined", l.getFileExtensions().size() > 0);
        }
    }
    
    @Test
    public void testFileExtensionUnique()
    {
        Map<String, Lang> exts = new HashMap<String, Lang>();
        
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
            Assert.assertTrue( l+" default extension not in file extensions list", l.getFileExtensions().contains( l.getFileExtensions().get(0))  );
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
