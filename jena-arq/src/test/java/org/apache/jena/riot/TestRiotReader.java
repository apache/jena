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

package org.apache.jena.riot;

import static org.junit.Assert.assertEquals ;
import static org.junit.Assert.assertFalse ;
import static org.junit.Assert.assertNotNull ;
import static org.junit.Assert.assertTrue ;

import java.io.ByteArrayInputStream ;
import java.io.UnsupportedEncodingException ;
import java.util.Iterator ;

import org.apache.jena.atlas.lib.StrUtils ;
import org.junit.Test ;

import com.hp.hpl.jena.graph.Triple ;

public class TestRiotReader
{
    @Test
    public void testCreateIteratorTriples_01() throws UnsupportedEncodingException
    {
        Iterator<Triple> it = RiotReader.createIteratorTriples(new ByteArrayInputStream("".getBytes("UTF-8")), RDFLanguages.NTRIPLES, "http://example/");
        
        assertFalse(it.hasNext());
    }
    
    @Test
    public void testEncodedUTF8() throws UnsupportedEncodingException
    {
        Iterator<Triple> it = RiotReader.createIteratorTriples(new ByteArrayInputStream("<a> <b> \"\\u263A\" .".getBytes("UTF-8")), RDFLanguages.NTRIPLES, null);
        
        assertTrue(it.hasNext());
        assertEquals("☺", it.next().getObject().getLiteralLexicalForm());
    }
    
    @Test
    public void testRawUTF8() throws UnsupportedEncodingException
    {
        Iterator<Triple> it = RiotReader.createIteratorTriples(new ByteArrayInputStream("<a> <b> \"☺\" .".getBytes("UTF-8")), RDFLanguages.NTRIPLES, null);
        
        assertTrue(it.hasNext());
        assertEquals("☺", it.next().getObject().getLiteralLexicalForm());
    }
    
    @Test
    public void testCreateIteratorTriples_02()
    {
        String x = StrUtils.strjoinNL(
                "<rdf:RDF", 
                "   xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"",
                "   xmlns:j.0=\"http://example/\">" ,
                "  <rdf:Description rdf:about=\"http://example/s\">" ,
                "     <j.0:p rdf:resource=\"http://example/o\"/>" ,
                "   </rdf:Description>" ,
                "</rdf:RDF>") ;
        
        Iterator<Triple> it = RiotReader.createIteratorTriples(new ByteArrayInputStream(x.getBytes()), RDFLanguages.RDFXML, "http://example/");
        
        assertTrue(it.hasNext());
        Triple t = it.next();
        assertNotNull(t);
        assertEquals("http://example/s", t.getSubject().getURI());
        assertEquals("http://example/p", t.getPredicate().getURI());
        assertEquals("http://example/o", t.getObject().getURI());
        
        assertFalse(it.hasNext());
    }
}
