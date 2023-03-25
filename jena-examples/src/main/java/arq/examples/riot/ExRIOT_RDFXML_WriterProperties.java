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
import java.util.HashMap ;
import java.util.Map ;

import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.*;

/** Example of setting properties for RDF/XML writer via RIOT */
public class ExRIOT_RDFXML_WriterProperties {
    static { LogCtl.setLogging(); }

    public static void main(String... args) {
        // Data.
        String x = StrUtils.strjoinNL
            ("PREFIX : <http://example.org/>"
            ,":s :p :o ."
            ,":s a :T ."
            );
        Model model = ModelFactory.createDefaultModel();
        RDFDataMgr.read(model, new StringReader(x), null, Lang.TURTLE);

        // Write, with properties
        writeProperties(model);
    }

    /** Write with properties */
    public static void writeProperties(Model model) {
        System.out.println("**** RDFXML_PLAIN+properties");
        System.out.println("**** Adds XML declaration");
        System.out.println();

        // Properties to be set.
        // See
        //   https://jena.apache.org/documentation/io/rdfxml-io.html
        //   https://jena.apache.org/documentation/io/rdfxml-output.html
        // for details of properties.
        Map<String, Object> properties = new HashMap<>() ;
        properties.put("showXmlDeclaration", "true");

        RDFWriter.create()
            .base("http://example.org/")
            .format(RDFFormat.RDFXML_PLAIN)
            .set(SysRIOT.sysRdfWriterProperties, properties)
            .source(model)
            .output(System.out);
    }
}
