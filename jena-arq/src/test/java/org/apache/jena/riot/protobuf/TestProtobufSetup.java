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

package org.apache.jena.riot.protobuf;

import static org.apache.jena.riot.Lang.RDFPROTO;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.jena.riot.*;
import org.junit.Test;

public class TestProtobufSetup {

    @Test public void setup_01() {
        assertTrue(RDFLanguages.isRegistered(RDFPROTO)) ;
    }

    @Test public void setup_02() {
        Lang lang = RDFLanguages.filenameToLang("data.rpb") ;
        assertEquals(lang, RDFPROTO) ;
    }

    @Test public void setup_03() {
        assertTrue(RDFParserRegistry.isQuads(RDFPROTO)) ;
        assertTrue(RDFParserRegistry.isTriples(RDFPROTO)) ;
        assertTrue(RDFParserRegistry.isRegistered(RDFPROTO));
        assertNotNull(RDFParserRegistry.getFactory(RDFPROTO)) ;
    }

    @Test public void setup_04() {
        assertTrue(RDFWriterRegistry.contains(RDFPROTO)) ;
        assertNotNull(RDFWriterRegistry.getWriterDatasetFactory(RDFPROTO)) ;
        assertTrue(RDFWriterRegistry.contains(RDFFormat.RDF_PROTO)) ;
        assertNotNull(RDFWriterRegistry.getWriterDatasetFactory(RDFFormat.RDF_PROTO)) ;
        assertTrue(RDFWriterRegistry.contains(RDFFormat.RDF_PROTO_VALUES)) ;
        assertNotNull(RDFWriterRegistry.getWriterDatasetFactory(RDFFormat.RDF_PROTO_VALUES)) ;
    }
}

