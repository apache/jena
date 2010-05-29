/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.riot.lang;

import com.hp.hpl.jena.util.FileUtils ;

import org.junit.Test ;
import org.openjena.atlas.junit.BaseTest ;
import org.openjena.riot.Lang ;
import org.openjena.riot.WebContent ;

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
}

/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */