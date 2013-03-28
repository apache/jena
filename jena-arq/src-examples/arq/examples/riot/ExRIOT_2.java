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

import org.apache.jena.riot.RDFLanguages ;
import org.apache.jena.riot.RiotReader ;
import org.apache.jena.riot.lang.LangRIOT ;
import org.apache.jena.riot.system.* ;

/** Example of using RIOT directly.
 * 
 * RDFDataMgr is the general place to read data - see {@link ExRIOT_1}  
 * 
 * RiotReader is the place for making parsers and can be used to read
 * from files or InputStreams. It can give more detailed control of error handling
 * and specialised destination of parser output.
 * It does not perform HTTP content negotiation.  
 */
public class ExRIOT_2
{
    public static void main(String...argv) throws FileNotFoundException
    {
        // ---- Parse to a Sink.
        StreamRDF noWhere = StreamRDFLib.sinkNull() ;

        // RIOT controls the conversion from bytes to java chars.
        InputStream in = new FileInputStream("data.trig") ;
        
        // Better is:
        // RDFDataMgr.parse(noWhere, in, "http://example/base", RDFLanguages.TRIG, null) ;
        RiotReader.parse(in, RDFLanguages.TRIG, "http://example/base", noWhere) ;
        
        // --- Or create a parser and do the parsing as separate steps.
        String baseURI = "http://example/base" ;
            
        // It is always better to use an  InputStream, rather than a Java Reader.
        // The parsers will do the necessary character set conversion.  
        in = new FileInputStream("data.trig") ;
        LangRIOT parser = RiotReader.createParser(in, RDFLanguages.TRIG, "http://example/base", noWhere) ;
        
        // Access the setup of the RIOT built-in parsers.
        
        // Parser to first error or warning.
        ErrorHandler errHandler = ErrorHandlerFactory.errorHandlerStrict ;

        // Now enable stricter checking, even N-TRIPLES must have absolute URIs. 
        ParserProfile profile = RiotLib.profile(baseURI, true, true, errHandler) ;

        // Just set the error handler.
        parser.getProfile().setHandler(errHandler) ;
        
        // Or replace the whole parser profile.
        parser.setProfile(profile) ;

        // Do the work.
        parser.parse() ;
    }
}
