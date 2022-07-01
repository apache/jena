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

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.SysRIOT;
import org.apache.jena.sparql.util.Context;

/** Set properties of the RDF/XML parser (ARP) */
public class ExRIOT_RDFXML_ReaderProperties {
    static { LogCtl.setLogging(); }
    
    public static void main(String[] args) {
        // Inline illustrative data.
        String data = StrUtils.strjoinNL
            ("<?xml version=\"1.0\" encoding=\"utf-8\"?>"
            ,"<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\""
            ,"         xmlns:ex=\"http://examples.org/\">"
            // This rdf:ID starts with a digit which normal causes a warning.
            ,"  <ex:Type rdf:ID='012345'></ex:Type>"
            ,"</rdf:RDF>"
            );
        System.out.println(data);
        System.out.println();
        // Properties to be set.
        // This is a map propertyName->value 
        Map<String, Object> properties = new HashMap<>();
        // See class ARPErrorNumbers for the possible ARP properies.
        properties.put("WARN_BAD_NAME", "EM_IGNORE");
        
        // Put a properties object into the Context.
        Context cxt = new Context();
        cxt.set(SysRIOT.sysRdfReaderProperties, properties);
        
        Model model = ModelFactory.createDefaultModel();
        // Build and run a parser
        RDFParser.create()
            .lang(Lang.RDFXML)
            .source(new StringReader(data))
            .context(cxt)
            .parse(model);
        System.out.println("== Parsed data output in Turtle");
        RDFDataMgr.write(System.out,  model, Lang.TURTLE);
    }
}
