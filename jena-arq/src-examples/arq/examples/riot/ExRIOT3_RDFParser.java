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
import java.io.IOException ;
import java.io.InputStream ;

import org.apache.jena.riot.RDFLanguages ;
import org.apache.jena.riot.RDFParser ;
import org.apache.jena.riot.system.ErrorHandlerFactory ;
import org.apache.jena.riot.system.StreamRDF ;
import org.apache.jena.riot.system.StreamRDFLib ;

/** Example of using RIOT directly.
 * 
 * The parsers produce a stream of triples and quads so processing does not need
 * to hold everything in memory at the same time. See also {@code ExRIOT_4}
 */
public class ExRIOT3_RDFParser
{
    public static void main(String...argv) throws IOException
    {
        // ---- Parse to a Sink.
        StreamRDF noWhere = StreamRDFLib.sinkNull() ;

        // --- Or create a parser and do the parsing with detailed setup.
        String baseURI = "http://example/base" ;
        
        // It is always better to use an InputStream, rather than a Java Reader.
        // The parsers will do the necessary character set conversion.  
        try (InputStream in = new FileInputStream("data.trig")) {
            RDFParser.create()
                .source(in)
                .lang(RDFLanguages.TRIG)
                .errorHandler(ErrorHandlerFactory.errorHandlerStrict)
                .base("http://example/base")
                .parse(noWhere);
        }
    }
}
