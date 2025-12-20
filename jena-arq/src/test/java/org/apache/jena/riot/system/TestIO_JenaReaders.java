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
package org.apache.jena.riot.system;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFReaderF;
import org.apache.jena.rdf.model.impl.RDFReaderFImpl;
import org.apache.jena.riot.IO_Jena;
import org.apache.jena.riot.adapters.RDFReaderRIOT;
import org.apache.jena.shared.NoReaderForLangException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestIO_JenaReaders {
    @BeforeAll public static void beforeClass() { }
    @AfterAll public static void afterClass()   { IO_Jena.wireIntoJena(); }

    @Test
    public void wireIntoJena() {
        IO_Jena.wireIntoJena();
        RDFReaderF readerF = new RDFReaderFImpl();

        assertEquals(RDFReaderRIOT.class, readerF.getReader(null).getClass());
        assertEquals(RDFReaderRIOT.class, readerF.getReader("RDF/XML").getClass());
        assertEquals(RDFReaderRIOT.class, readerF.getReader("RDF/XML-ABBREV").getClass());
        assertEquals(RDFReaderRIOT.class, readerF.getReader("N-TRIPLES").getClass());
        assertEquals(RDFReaderRIOT.class, readerF.getReader("N-Triples").getClass());
        assertEquals(RDFReaderRIOT.class, readerF.getReader("N-TRIPLE").getClass());
        assertEquals(RDFReaderRIOT.class, readerF.getReader("N3").getClass());
        assertEquals(RDFReaderRIOT.class, readerF.getReader("TURTLE").getClass());
        assertEquals(RDFReaderRIOT.class, readerF.getReader("Turtle").getClass());
        assertEquals(RDFReaderRIOT.class, readerF.getReader("TTL").getClass());
        assertEquals(RDFReaderRIOT.class, readerF.getReader("JSON-LD").getClass());
        assertEquals(RDFReaderRIOT.class, readerF.getReader("JSONLD").getClass());
        assertEquals(RDFReaderRIOT.class, readerF.getReader("RDF/JSON").getClass());
    }

    @Test
    public void resetJena() {
        IO_Jena.wireIntoJena();
        IO_Jena.resetJena();
        try {
            RDFReaderF readerF = new RDFReaderFImpl();
            Model model = ModelFactory.createDefaultModel();
            Logger logger = LoggerFactory.getLogger("RDFReader");

            LogCtl.withLevel(logger, "off", ()->{
                assertThrows(NoReaderForLangException.class, ()->readerF.getReader("RDF"));
                assertThrows(NoReaderForLangException.class, ()->readerF.getReader("RDF/XML"));
                assertThrows(NoReaderForLangException.class, ()->readerF.getReader("RDF/XML-ABBREV"));

                assertThrows(NoReaderForLangException.class, ()->model.read("http://example/"));
                assertThrows(NoReaderForLangException.class, ()->model.read("http://example/", "RDF/XML"));
            });

            assertNotEquals(RDFReaderRIOT.class, readerF.getReader("N-TRIPLES").getClass());
            assertNotEquals(RDFReaderRIOT.class, readerF.getReader("N-Triples").getClass());
            assertNotEquals(RDFReaderRIOT.class, readerF.getReader("N-TRIPLE").getClass());

            // It's not called "NT" in jena-core on it's own.
            assertThrows(NoReaderForLangException.class, ()->readerF.getReader("NT"));
            assertThrows(NoReaderForLangException.class, ()->readerF.getReader("TURTLE"));
            assertThrows(NoReaderForLangException.class, ()->readerF.getReader("JSON-LD"));
        } finally {
            IO_Jena.wireIntoJena();
        }
    }


}
