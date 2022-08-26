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
import static org.junit.Assert.assertNotEquals ;
import static org.junit.Assert.fail ;

import org.apache.jena.rdf.model.RDFReaderF ;
import org.apache.jena.rdf.model.impl.RDFReaderFImpl ;
import org.apache.jena.riot.IO_Jena ;
import org.apache.jena.riot.adapters.RDFReaderRIOT ;
import org.apache.jena.shared.NoReaderForLangException ;
import org.junit.AfterClass ;
import org.junit.BeforeClass ;
import org.junit.Test ;

public class TestIO_JenaReaders {
    @BeforeClass public static void beforeClass() { }
    @AfterClass public static void afterClass()   { IO_Jena.wireIntoJena(); }

    @Test
    public void wireIntoJena() {
        IO_Jena.wireIntoJena();
        RDFReaderF readerF = new RDFReaderFImpl();

        assertEquals(RDFReaderRIOT.class, readerF.getReader().getClass());
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
        RDFReaderF readerF = new RDFReaderFImpl();

        assertNotEquals(RDFReaderRIOT.class, readerF.getReader().getClass());
        assertNotEquals(RDFReaderRIOT.class, readerF.getReader("RDF/XML").getClass());
        assertNotEquals(RDFReaderRIOT.class, readerF.getReader("RDF/XML-ABBREV").getClass());
        assertNotEquals(RDFReaderRIOT.class, readerF.getReader("N-TRIPLES").getClass());
        assertNotEquals(RDFReaderRIOT.class, readerF.getReader("N-Triples").getClass());
        assertNotEquals(RDFReaderRIOT.class, readerF.getReader("N-TRIPLE").getClass());

        try { readerF.getReader("NT")      ; fail("Exception expected") ; } catch (NoReaderForLangException e) {}
        try { readerF.getReader("JSON_LD") ; fail("Exception expected") ; } catch (NoReaderForLangException e) {}
        try { readerF.getReader("RDF/JSON"); fail("Exception expected") ; } catch (NoReaderForLangException e) {}

        IO_Jena.wireIntoJena();
    }


}
