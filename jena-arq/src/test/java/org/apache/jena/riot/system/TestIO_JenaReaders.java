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
package org.apache.jena.riot.system;

import static org.junit.Assert.assertEquals ;
import static org.junit.Assert.assertFalse ;

import java.util.LinkedHashMap ;
import java.util.Map ;
import java.util.Properties ;

import org.apache.jena.riot.IO_Jena ;
import org.apache.jena.riot.adapters.JenaReadersWriters.RDFReaderRIOT_NT ;
import org.apache.jena.riot.adapters.JenaReadersWriters.RDFReaderRIOT_RDFJSON ;
import org.apache.jena.riot.adapters.JenaReadersWriters.RDFReaderRIOT_JSONLD ;
import org.apache.jena.riot.adapters.JenaReadersWriters.RDFReaderRIOT_RDFXML ;
import org.apache.jena.riot.adapters.JenaReadersWriters.RDFReaderRIOT_TTL ;
import org.apache.jena.riot.adapters.* ;
import org.junit.AfterClass ;
import org.junit.BeforeClass ;
import org.junit.Test ;

import com.hp.hpl.jena.rdf.model.impl.RDFReaderFImpl ;

public class TestIO_JenaReaders {
    @BeforeClass public static void beforeClass() { } 
    @AfterClass public static void afterClass()   { IO_Jena.wireIntoJena(); }
    
    private final class RDFReaderFImplExposingProtected extends RDFReaderFImpl {
        public Map<String, String> defaultReaders() {
            Map<String,String> defaults = new LinkedHashMap<>();
            for (int i=0; i<LANGS.length; i++) {
                defaults.put(LANGS[i], DEFAULTREADERS[i]);
            }
            return defaults;
        }
        public Properties getLangToClassName() {
            return langToClassName;
        }
    }

    @Test
    public void wireIntoJena() throws Exception {
        IO_JenaReaders.wireIntoJena();
        RDFReaderFImpl readerF = new RDFReaderFImpl();
        assertEquals(RDFReaderRIOT_Web.class,       readerF.getReader().getClass());
        assertEquals(RDFReaderRIOT_RDFXML.class,    readerF.getReader("RDF/XML").getClass());
        assertEquals(RDFReaderRIOT_RDFXML.class,    readerF.getReader("RDF/XML-ABBREV").getClass());
        assertEquals(RDFReaderRIOT_NT.class,        readerF.getReader("N-TRIPLES").getClass());
        assertEquals(RDFReaderRIOT_NT.class,        readerF.getReader("N-Triples").getClass());
        assertEquals(RDFReaderRIOT_NT.class,        readerF.getReader("N-TRIPLE").getClass());
        assertEquals(RDFReaderRIOT_TTL.class,       readerF.getReader("N3").getClass());
        assertEquals(RDFReaderRIOT_TTL.class,       readerF.getReader("TURTLE").getClass());
        assertEquals(RDFReaderRIOT_TTL.class,       readerF.getReader("Turtle").getClass());
        assertEquals(RDFReaderRIOT_TTL.class,       readerF.getReader("TTL").getClass());
        assertEquals(RDFReaderRIOT_JSONLD.class,    readerF.getReader("JSON-LD").getClass());
        assertEquals(RDFReaderRIOT_JSONLD.class,    readerF.getReader("JSONLD").getClass());
        assertEquals(RDFReaderRIOT_RDFJSON.class,   readerF.getReader("RDF/JSON").getClass());
        
    }
    
    @Test
    public void resetJena() throws Exception {
        IO_JenaReaders.wireIntoJena();
        IO_JenaReaders.resetJena();
        RDFReaderFImplExposingProtected readerF = new RDFReaderFImplExposingProtected();
        Map<String, String> defaults = readerF.defaultReaders();
        assertFalse(defaults.isEmpty());
        for (String lang : defaults.keySet()) {
            assertEquals(defaults.get(lang), readerF.getLangToClassName().get(lang));
            if (lang.equals("GRDDL")) {
                // Can't load
                continue;
            }
            assertEquals(defaults.get(lang), readerF.getReader(lang).getClass().getName());
        }
        
        // And unregistered our additional langs
        assertEquals("", readerF.getLangToClassName().get("JSON-LD"));
        assertEquals("", readerF.getLangToClassName().get("RDF/JSON"));
        IO_JenaReaders.wireIntoJena();
    }

    
}
