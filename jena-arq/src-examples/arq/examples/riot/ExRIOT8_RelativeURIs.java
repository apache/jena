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

package arq.examples.riot;

import org.apache.jena.atlas.logging.LogCtl ;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.RDFWriter;
import org.apache.jena.riot.RIOT;

/** Example of registering a new writer with RIOT */
public class ExRIOT8_RelativeURIs
{
    static { LogCtl.setLogging(); }
    
    public static void main(String[] args)
    {
        System.out.println("## Example of I/O with relative URIs.") ; 
        System.out.println() ;
        
        Model model = ModelFactory.createDefaultModel(); 
        
        // Read data that has relative URIs, and does not include a BASE directive.
        // (When reading a URL (including files), the base defaults to the URL.)
        RDFParser.create()
            .base("http://theBase/")
            .source("SomeData.ttl")     // Implies "lang(Lang.TTL)"
            .parse(model);

        // == With BASE URI
        // Write data, with relative URIs and with BASE.
        RDFWriter.create()
            // Cause a BASE to output and URIs to be made relative.
            .base("http://theBase/")
            .lang(Lang.TTL)
            .source(model)
            .output(System.out);
        
        // == Without BASE URI
        // Write data, with relative URIs but no base.
        // The data is not portable - the exact triples it contains when read back in
        // will be influenced by the base URI of the parsing step.
        RDFWriter.create()
            // Don't print "BASE".  
            .set(RIOT.symTurtleOmitBase, true)
            // Cause URIs to be made relative using the base.
            .base("http://theBase/")
            .lang(Lang.TTL)
            .source(model)
            .output(System.out);
    }
}

