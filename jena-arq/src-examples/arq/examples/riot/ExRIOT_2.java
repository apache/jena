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

package arq.examples.riot;

import java.io.FileInputStream ;
import java.io.FileNotFoundException ;
import java.io.InputStream ;

import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.riot.RDFLanguages ;
import org.apache.jena.riot.ReaderRIOT ;
import org.apache.jena.riot.system.* ;

/** Example of using RIOT directly.
 * 
 * RDFDataMgr is the general place to read data.
 * 
 * The parsers produce a stream of triples and quads so processing does not need
 * to hold everything in memory at the same time. See also {@link ExRIOT_4}
 */
public class ExRIOT_2
{
    public static void main(String...argv) throws FileNotFoundException
    {
        // ---- Parse to a Sink.
        StreamRDF noWhere = StreamRDFLib.sinkNull() ;

        // RIOT controls the conversion from bytes to java chars.
        InputStream in = new FileInputStream("data.trig") ;
        
        RDFDataMgr.parse(noWhere, in, "http://example/base", RDFLanguages.TRIG, null) ;
        
        // --- Or create a parser and do the parsing as separate steps.
        String baseURI = "http://example/base" ;
            
        // It is always better to use an InputStream, rather than a Java Reader.
        // The parsers will do the necessary character set conversion.  
        in = new FileInputStream("data.trig") ;
        
        ReaderRIOT parser = RDFDataMgr.createReader(RDFLanguages.TRIG) ;
        
        // Access the setup of the RIOT built-in parsers.
        
        // Parser to first error or warning.
        ErrorHandler errHandler = ErrorHandlerFactory.errorHandlerStrict ;

        // Now enable stricter checking, even N-TRIPLES must have absolute URIs. 
        ParserProfile profile = RiotLib.profile(baseURI, true, true, errHandler) ;

        // Just set the error handler.
        parser.setErrorHandler(errHandler) ;
        
        // Or replace the whole parser profile.
        parser.setParserProfile(profile) ;

        // Do the work.
        parser.read(in, "http://example/base", null, noWhere, null);
    }
}
