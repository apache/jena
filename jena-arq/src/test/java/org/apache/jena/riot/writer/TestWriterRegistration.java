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

package org.apache.jena.riot.writer;

import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFFormat ;
import org.apache.jena.riot.RDFWriterRegistry ;
import org.junit.Test ;

public class TestWriterRegistration extends BaseTest
{
    static { RDFWriterRegistry.init(); }
    @Test public void registration_01() { testregistration(Lang.RDFXML) ; }
    @Test public void registration_02() { testregistration(Lang.NTRIPLES) ; }
    @Test public void registration_03() { testregistration(Lang.NT) ; }
    @Test public void registration_04() { testregistration(Lang.N3) ; } 
    @Test public void registration_05() { testregistration(Lang.TURTLE) ; }   
    @Test public void registration_06() { testregistration(Lang.TTL) ; }
    @Test public void registration_07() { testregistration(Lang.RDFJSON) ; }   
    @Test public void registration_08() { testregistration(Lang.NQUADS) ; }
    @Test public void registration_09() { testregistration(Lang.NQ) ; }
    @Test public void registration_10() { testregistration(Lang.TRIG) ; }
    @Test public void registration_11() { testregistration(Lang.RDFNULL) ; }
    
    @Test public void registration_20() { testregistration(RDFFormat.TURTLE_PRETTY) ; }
    @Test public void registration_21() { testregistration(RDFFormat.TURTLE) ; }
    @Test public void registration_22() { testregistration(RDFFormat.TTL) ; }
    @Test public void registration_23() { testregistration(RDFFormat.TURTLE_BLOCKS) ; }
    @Test public void registration_24() { testregistration(RDFFormat.TURTLE_FLAT) ; }
    
    @Test public void registration_25() { testregistration(RDFFormat.NTRIPLES) ; }
    @Test public void registration_26() { testregistration(RDFFormat.NQUADS) ; }
    @Test public void registration_25a() { testregistration(RDFFormat.NTRIPLES_UTF8) ; }
    @Test public void registration_26a() { testregistration(RDFFormat.NQUADS_UTF8) ; }
    @Test public void registration_25b() { testregistration(RDFFormat.NTRIPLES_ASCII) ; }
    @Test public void registration_26b() { testregistration(RDFFormat.NQUADS_ASCII) ; }

    @Test public void registration_27() { testregistration(RDFFormat.TRIG_PRETTY) ; }
    @Test public void registration_28() { testregistration(RDFFormat.TRIG) ; }
    @Test public void registration_29() { testregistration(RDFFormat.TRIG_BLOCKS) ; }
    @Test public void registration_30() { testregistration(RDFFormat.TRIG_FLAT) ; }
    @Test public void registration_31() { testregistration(RDFFormat.RDFXML_PRETTY) ; }
    @Test public void registration_32() { testregistration(RDFFormat.RDFXML_ABBREV) ; }
    @Test public void registration_33() { testregistration(RDFFormat.RDFXML) ; }
    @Test public void registration_34() { testregistration(RDFFormat.RDFXML_PLAIN) ; }
    @Test public void registration_35() { testregistration(RDFFormat.RDFJSON) ; }
    @Test public void registration_36() { testregistration(RDFFormat.RDFNULL) ; }

    private void testregistration(Lang lang)
    {
        assertTrue("No writer registered for language "+lang, RDFWriterRegistry.contains(lang)) ;
        assertTrue( RDFWriterRegistry.getWriterGraphFactory(lang) != null || RDFWriterRegistry.getWriterDatasetFactory(lang) != null ) ;
    }
    
    private void testregistration(RDFFormat format)
    {
        assertTrue("No writer registered for format "+format, RDFWriterRegistry.contains(format)) ;
        assertTrue( RDFWriterRegistry.getWriterGraphFactory(format) != null || RDFWriterRegistry.getWriterDatasetFactory(format) != null ) ;
    }

}

