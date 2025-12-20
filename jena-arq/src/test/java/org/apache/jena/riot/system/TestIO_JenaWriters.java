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
import org.apache.jena.rdf.model.RDFWriterF;
import org.apache.jena.rdf.model.impl.RDFWriterFImpl;
import org.apache.jena.riot.IO_Jena;
import org.apache.jena.riot.adapters.RDFWriterRIOT;
import org.apache.jena.shared.NoWriterForLangException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestIO_JenaWriters {

    @BeforeAll public static void beforeClass() { }
    @AfterAll public static void afterClass()   { IO_Jena.wireIntoJena(); }

    @Test
    public void testWireIntoJena() {
        IO_Jena.wireIntoJena();
        RDFWriterF writerF = new RDFWriterFImpl();
        assertEquals(RDFWriterRIOT.class, writerF.getWriter(null).getClass());
        assertEquals(RDFWriterRIOT.class, writerF.getWriter("RDF/XML").getClass());
        assertEquals(RDFWriterRIOT.class, writerF.getWriter("RDF/XML-ABBREV").getClass());
        assertEquals(RDFWriterRIOT.class, writerF.getWriter("N-TRIPLE").getClass());
        assertEquals(RDFWriterRIOT.class, writerF.getWriter("N-Triples").getClass());
        assertEquals(RDFWriterRIOT.class, writerF.getWriter("N-TRIPLE").getClass());
        assertEquals(RDFWriterRIOT.class, writerF.getWriter("NT").getClass());

        assertEquals(RDFWriterRIOT.class, writerF.getWriter("N3").getClass());
        assertEquals(RDFWriterRIOT.class, writerF.getWriter("TURTLE").getClass());
        assertEquals(RDFWriterRIOT.class, writerF.getWriter("Turtle").getClass());
        assertEquals(RDFWriterRIOT.class, writerF.getWriter("TTL").getClass());

        assertEquals(RDFWriterRIOT.class, writerF.getWriter("RDF/JSON").getClass());
        assertEquals(RDFWriterRIOT.class, writerF.getWriter("RDFJSON").getClass());
    }

    @SuppressWarnings("removal")
    @Test
    public void testResetJena() {
        IO_Jena.wireIntoJena();
        IO_Jena.resetJena();
        try {
            RDFWriterF writerF = new RDFWriterFImpl();
            Model model = ModelFactory.createDefaultModel();
            Logger logger = LoggerFactory.getLogger("RDFWriter");

            LogCtl.withLevel(logger, "off", ()->{
                assertThrows(NoWriterForLangException.class, ()->writerF.getWriter("RDF/XML"));
                assertThrows(NoWriterForLangException.class, ()->writerF.getWriter("RDF/XML-ABBREV"));

                assertThrows(NoWriterForLangException.class, ()->model.write(System.err));
                assertThrows(NoWriterForLangException.class, ()->model.write(System.err, "RDF/XML"));
            });

            assertNotEquals(RDFWriterRIOT.class, writerF.getWriter("N-TRIPLE").getClass());
            assertNotEquals(RDFWriterRIOT.class, writerF.getWriter("N-Triples").getClass());
            assertNotEquals(RDFWriterRIOT.class, writerF.getWriter("N-TRIPLE").getClass());

            // It's not called "NT" in jena-core on it's own.
            assertThrows(NoWriterForLangException.class, ()->writerF.getWriter("NT"));
            assertThrows(NoWriterForLangException.class, ()->writerF.getWriter("TURTLE"));
            assertThrows(NoWriterForLangException.class, ()->writerF.getWriter("JSON-LD"));
        } finally {
            IO_Jena.wireIntoJena();
        }
    }
}
