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

package org.apache.jena.riot;

import java.io.FileInputStream ;
import java.io.IOException ;

import org.apache.jena.atlas.junit.BaseTest ;
import org.junit.AfterClass ;
import org.junit.BeforeClass ;
import org.junit.Test ;
import org.openjena.riot.RIOT ;

import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.query.DatasetFactory ;
import com.hp.hpl.jena.sparql.util.Context ;

public class TestReadDataset extends BaseTest
{
    private static final String directory = "testing/RIOT/Reader" ;

    private static Context context = new Context() ;
    
    @BeforeClass static public void beforeClass()
    { 
        RIOT.init() ;
    }
    
    @AfterClass static public void afterClass()
    { 
        // Unwire?
    }

    @Test public void read_01() { read("D.nq") ; }
    @Test public void read_02() { read("D.trig") ; }
    @Test public void read_03() { read("D.nq",   RDFLanguages.NQuads) ; }
    @Test public void read_04() { read("D.trig", RDFLanguages.TriG) ; }
    
    @Test public void read_11() { read("D.nq", "N-Quads") ; }

    @Test public void read_12() { read("D.nq", "NQuads") ; }
    @Test public void read_13() { read("D.nq", "NQ") ; }
    @Test public void read_14() { read("D.trig", "TriG") ; }
    @Test public void read_15() { read("D.trig", "trig") ; } 

    @Test public void read_21() { read("D-nq",  RDFLanguages.NQuads) ; }
    @Test public void read_22() { read("D-trig", RDFLanguages.TriG) ; }
    @Test public void read_23() { read("D-nq",  "NQuads") ; }
    @Test public void read_24() { read("D-trig", "TRIG") ; }

    @Test public void read_input_1() throws IOException
    { read_stream("D.nq", "NQ") ; }
        
    @Test public void read_input_2() throws IOException
    { read_stream("D.trig", "trig") ; }
    
    private static String filename(String filename) { return directory+"/"+filename ; }

    // Base.
    
    private static void read(String dataurl) { read(dataurl, (Lang2)null) ; }
    
    private static void read(String dataurl, String lang)
    {
        read(dataurl, RDFLanguages.nameToLang(lang)) ;
    }
    
    private static void read(String dataurl, Lang2 lang)
    {
        dataurl = filename(dataurl) ;
        Dataset ds = DatasetFactory.createMem() ;
        WebReader2.read(ds, dataurl, lang) ;
    }

    private static void read_stream(String filename, String lang) throws IOException
    {
        read_stream(filename, RDFLanguages.nameToLang(lang)) ;
    }
    
    private static void read_stream(String filename, Lang2 lang) throws IOException
    {
        filename = filename(filename) ;
        
        // Read with a base
        Dataset ds0 = DatasetFactory.createMem() ;
        FileInputStream in0 = new FileInputStream(filename) ;
        WebReader2.read(ds0, in0, "http://example/base2", lang) ;
        in0.close() ;

        // Read again, but without base
        Dataset ds1 = DatasetFactory.createMem() ;
        FileInputStream in1 = new FileInputStream(filename) ;
        WebReader2.read(ds1, in1, null, lang) ;
        in1.close() ;
    }

}

