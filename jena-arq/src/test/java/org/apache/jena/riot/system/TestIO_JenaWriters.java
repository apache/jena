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

import org.apache.jena.rdf.model.RDFWriterF ;
import org.apache.jena.rdf.model.impl.RDFWriterFImpl ;
import org.apache.jena.riot.IO_Jena ;
import org.apache.jena.riot.adapters.RDFWriterRIOT ;
import org.apache.jena.shared.NoWriterForLangException ;
import org.junit.AfterClass ;
import org.junit.BeforeClass ;
import org.junit.Test ;

public class TestIO_JenaWriters {

    @BeforeClass public static void beforeClass() { }
    @AfterClass public static void afterClass()   { IO_Jena.wireIntoJena(); }

    @Test
    public void testWireIntoJena() {
        IO_Jena.wireIntoJena();
        RDFWriterF writerF = new RDFWriterFImpl();
        assertEquals(RDFWriterRIOT.class, writerF.getWriter().getClass());
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

    @Test
    public void testResetJena() {
        IO_Jena.wireIntoJena();
        IO_Jena.resetJena();
        RDFWriterF writerF = new RDFWriterFImpl();

        assertNotEquals(RDFWriterRIOT.class, writerF.getWriter().getClass());
        assertNotEquals(RDFWriterRIOT.class, writerF.getWriter("RDF/XML").getClass());
        assertNotEquals(RDFWriterRIOT.class, writerF.getWriter("RDF/XML-ABBREV").getClass());
        assertNotEquals(RDFWriterRIOT.class, writerF.getWriter("N-TRIPLE").getClass());
        assertNotEquals(RDFWriterRIOT.class, writerF.getWriter("N-Triples").getClass());
        assertNotEquals(RDFWriterRIOT.class, writerF.getWriter("N-TRIPLE").getClass());

        assertNotEquals(RDFWriterRIOT.class, writerF.getWriter("N3").getClass());
        assertNotEquals(RDFWriterRIOT.class, writerF.getWriter("TURTLE").getClass());
        assertNotEquals(RDFWriterRIOT.class, writerF.getWriter("Turtle").getClass());
        assertNotEquals(RDFWriterRIOT.class, writerF.getWriter("TTL").getClass());

        try { writerF.getWriter("NT"); fail("Exception expected") ; } catch (NoWriterForLangException ex) {}
        try { writerF.getWriter("RDF/JSON") ; fail("Exception expected") ; } catch (NoWriterForLangException ex) {}
        try { writerF.getWriter("RDFJSON"); fail("Exception expected") ; } catch (NoWriterForLangException ex) {}
        IO_Jena.wireIntoJena();
    }


}
