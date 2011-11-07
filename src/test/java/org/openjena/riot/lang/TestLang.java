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

package org.openjena.riot.lang;

import org.junit.Test ;
import org.openjena.atlas.junit.BaseTest ;
import org.openjena.riot.Lang ;
import org.openjena.riot.WebContent ;

import com.hp.hpl.jena.util.FileUtils ;

public class TestLang extends BaseTest
{
    @Test public void lang_01()
    { assertEquals(Lang.TURTLE, Lang.get("TTL")) ; }

    @Test public void lang_02()
    { assertEquals(Lang.TURTLE, Lang.get("ttl")) ; }

    @Test public void lang_03()
    { assertEquals(Lang.TURTLE, Lang.get("Turtle")) ; }

    @Test public void lang_04()
    { assertEquals(Lang.RDFXML, Lang.get(FileUtils.langXML)) ; }

    @Test public void lang_05()
    { assertEquals(Lang.RDFXML, Lang.get(FileUtils.langXMLAbbrev)) ; }

    @Test public void lang_06()
    { assertEquals(Lang.NTRIPLES, Lang.get(FileUtils.langNTriple)) ; }
    
    @Test public void lang_07()
    { assertEquals(Lang.NTRIPLES, Lang.get(WebContent.langNTriples)) ; }
    
    @Test public void lang_08()
    { assertEquals(Lang.NQUADS, Lang.get(WebContent.langNQuads)) ; }
    
    @Test public void lang_09()
    { assertEquals(Lang.TRIG, Lang.get(WebContent.langTriG)) ; }

    @Test public void lang_10()
    { assertEquals(Lang.RDFJSON, Lang.get("RDF/JSON")) ; }

    @Test public void lang_11()
    { assertEquals(Lang.RDFJSON, Lang.get(WebContent.langRdfJson)) ; }

}
