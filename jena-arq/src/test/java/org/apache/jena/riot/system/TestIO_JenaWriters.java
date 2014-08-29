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
import org.apache.jena.riot.adapters.JenaReadersWriters.RDFWriterRIOT_N3 ;
import org.apache.jena.riot.adapters.JenaReadersWriters.RDFWriterRIOT_N3Plain ;
import org.apache.jena.riot.adapters.JenaReadersWriters.RDFWriterRIOT_N3Triples ;
import org.apache.jena.riot.adapters.JenaReadersWriters.RDFWriterRIOT_N3TriplesAlt ;
import org.apache.jena.riot.adapters.JenaReadersWriters.RDFWriterRIOT_N3_PP ;
import org.apache.jena.riot.adapters.JenaReadersWriters.RDFWriterRIOT_NTriples ;
import org.apache.jena.riot.adapters.JenaReadersWriters.RDFWriterRIOT_RDFJSON ;
import org.apache.jena.riot.adapters.JenaReadersWriters.RDFWriterRIOT_Turtle ;
import org.apache.jena.riot.adapters.JenaReadersWriters.RDFWriterRIOT_Turtle1 ;
import org.apache.jena.riot.adapters.JenaReadersWriters.RDFWriterRIOT_Turtle2 ;
import org.junit.AfterClass ;
import org.junit.BeforeClass ;
import org.junit.Test ;

import com.hp.hpl.jena.n3.N3JenaWriter ;
import com.hp.hpl.jena.rdf.model.impl.RDFWriterFImpl ;
import com.hp.hpl.jena.rdfxml.xmloutput.impl.Abbreviated ;
import com.hp.hpl.jena.rdfxml.xmloutput.impl.Basic ;

public class TestIO_JenaWriters {
    
    @BeforeClass public static void beforeClass() { } 
    @AfterClass public static void afterClass()   { IO_Jena.wireIntoJena(); }
    
    private final class RDFWriterFImplExposingProtected extends RDFWriterFImpl {
        public Map<String, String> defaultWriters() {
            Map<String,String> defaults = new LinkedHashMap<>();
            for (int i=0; i<LANGS.length; i++) {
                defaults.put(LANGS[i], DEFAULTWRITERS[i]);
            }
            return defaults;
        }
        public Properties getLangToClassName() {
            return langToClassName;
        }
    }

    @Test
    public void testWireIntoJena() throws Exception {
        IO_JenaWriters.wireIntoJena();
        RDFWriterFImpl writerF = new RDFWriterFImpl();
        assertEquals(Basic.class, writerF.getWriter().getClass());
        assertEquals(Basic.class, writerF.getWriter("RDF/XML").getClass());
        assertEquals(Abbreviated.class, writerF.getWriter("RDF/XML-ABBREV").getClass());
        assertEquals(RDFWriterRIOT_NTriples.class, writerF.getWriter("N-TRIPLE").getClass());
        assertEquals(RDFWriterRIOT_NTriples.class, writerF.getWriter("N-Triples").getClass());
        assertEquals(RDFWriterRIOT_NTriples.class, writerF.getWriter("N-TRIPLE").getClass());
        assertEquals(RDFWriterRIOT_NTriples.class, writerF.getWriter("NT").getClass());

        assertEquals(RDFWriterRIOT_N3.class, writerF.getWriter("N3").getClass());
        assertEquals(RDFWriterRIOT_N3_PP.class, writerF.getWriter(N3JenaWriter.n3WriterPrettyPrinter).getClass());
        assertEquals(RDFWriterRIOT_N3Plain.class, writerF.getWriter(N3JenaWriter.n3WriterPlain).getClass());
        assertEquals(RDFWriterRIOT_N3Triples.class, writerF.getWriter(N3JenaWriter.n3WriterTriples).getClass());
        assertEquals(RDFWriterRIOT_N3TriplesAlt.class, writerF.getWriter(N3JenaWriter.n3WriterTriplesAlt).getClass());
        
        assertEquals(RDFWriterRIOT_Turtle.class, writerF.getWriter(N3JenaWriter.turtleWriter).getClass());
        assertEquals(RDFWriterRIOT_Turtle1.class, writerF.getWriter(N3JenaWriter.turtleWriterAlt1).getClass());
        assertEquals(RDFWriterRIOT_Turtle2.class, writerF.getWriter(N3JenaWriter.turtleWriterAlt2).getClass());

        assertEquals(RDFWriterRIOT_RDFJSON.class, writerF.getWriter("RDF/JSON").getClass());
        assertEquals(RDFWriterRIOT_RDFJSON.class, writerF.getWriter("RDFJSON").getClass());
    }
    
    @Test
    public void testResetJena() throws Exception {
        IO_JenaWriters.wireIntoJena();
        IO_JenaWriters.resetJena();
        RDFWriterFImplExposingProtected writerF = new RDFWriterFImplExposingProtected();
        Map<String, String> defaults = writerF.defaultWriters();
        assertFalse(defaults.isEmpty());
        for (String lang : defaults.keySet()) {
            assertEquals(defaults.get(lang), writerF.getLangToClassName().get(lang));
            assertEquals(defaults.get(lang), writerF.getWriter(lang).getClass().getName());
        }
        
        // And unregistered our additional langs
        assertEquals("", writerF.getLangToClassName().get("NT"));
        assertEquals("", writerF.getLangToClassName().get("RDF/JSON"));
        assertEquals("", writerF.getLangToClassName().get("RDFJSON"));
        IO_JenaWriters.wireIntoJena();
    }

    
}
