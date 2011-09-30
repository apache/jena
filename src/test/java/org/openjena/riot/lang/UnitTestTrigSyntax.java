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

package org.openjena.riot.lang;

import java.io.IOException ;
import java.io.InputStream ;

import junit.framework.TestCase ;
import org.openjena.atlas.io.IO ;
import org.openjena.atlas.lib.SinkNull ;
import org.openjena.riot.RiotReader ;

import com.hp.hpl.jena.sparql.core.Quad ;


public class UnitTestTrigSyntax extends TestCase
{
    String uri ;
    public UnitTestTrigSyntax(String name, String uri) { super(name) ; this.uri = uri ; }
    
    @Override
    public void runTest()
    {
        InputStream in = IO.openFile(uri) ;
        assertNotNull(in) ;
        LangRIOT parser = RiotReader.createParserTriG(in, uri, new SinkNull<Quad>()) ;
        parser.parse() ;
        // Check EOF.
        try { 
            int eof = in.read() ;
            assertEquals(-1, eof) ;
        } catch (IOException ex) { IO.exception(ex) ; }

        
//        Model model = ModelFactory.createDefaultModel() ;
//        RDFReader t = new JenaReaderTurtle2() ;
//        try {
//            t.read(model, uri) ;
//        } catch (ParseException ex)
//        {
//            throw ex ;    
//        }
    }

}
