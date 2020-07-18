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

package org.apache.jena.shacl.compact;

import org.apache.jena.riot.*;
import org.apache.jena.riot.system.ParserProfile;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapFactory;
import org.apache.jena.shacl.compact.reader.ReaderRIOTShaclc;
import org.apache.jena.shacl.compact.writer.WriterRIOTShaclc;
import org.apache.jena.shacl.vocabulary.SHACL;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;

/** SHACL Compact Syntax setup */
public class SHACLC {

    public static void init() {
        // Lang.SHACLC is in RIOT RDFLanguages.
        ReaderRIOTFactory factoryReader = (Lang language, ParserProfile profile)->new ReaderRIOTShaclc();
        RDFParserRegistry.registerLangTriples(Lang.SHACLC, factoryReader);

        WriterGraphRIOTFactory factoryWriter = (rdfFormat)->new WriterRIOTShaclc();
        RDFWriterRegistry.register(RDFFormat.SHACLC, factoryWriter);
    }

    /** Return a copy of the {@link PrefixMap} with the SHACLC standard prefixes added */
    public static PrefixMap withStandardPrefixes(PrefixMap pmap) {
        PrefixMap pmap2 = PrefixMapFactory.create();
        addStandardPrefixes(pmap2);
        // Add second to override any of the standard settings.
        pmap2.putAll(pmap);
        return pmap2;
    }

    /** Return a copy of the {@link PrefixMap} with the SHACLC standard prefixes added */
    public static PrefixMap withStandardPrefixes() {
        PrefixMap pmap = PrefixMapFactory.create();
        //pmap.add("owl",  OWL.getURI());
        return addStandardPrefixes(pmap);
    }

    /** Update {@link PrefixMap} with the SHACLC standard prefixes */
    public static PrefixMap addStandardPrefixes(PrefixMap pmap) {
        pmap.add("rdf",  RDF.getURI());
        pmap.add("rdfs", RDFS.getURI());
        pmap.add("sh",   SHACL.getURI());
        pmap.add("xsd",  XSD.getURI());
        //pmap.add("owl",  OWL.getURI());
        return pmap;
    }

    /** Return a copy of the {@link PrefixMapping} with the SHACLC standard prefixes added */
    public static PrefixMapping withStandardPrefixes(PrefixMapping prefixMapping) {
        PrefixMapping pm = new PrefixMappingImpl();
        addStandardPrefixes(pm);
        // Add second to override any of the standard settings.
        pm.setNsPrefixes(prefixMapping);
        return pm;
    }


    /** Update {@link PrefixMapping} with the SHACLC standard prefixes */
    public static void addStandardPrefixes(PrefixMapping prefixMapping) {
        // Always add these prefixes to the prologue of the parser.
        // These are required by the SHACLC test suite.
        // The parser passes any declared prefixes to the output StreamRDF
        prefixMapping.setNsPrefix("sh",   SHACL.getURI());
        prefixMapping.setNsPrefix("rdf",  RDF.getURI());
        prefixMapping.setNsPrefix("rdfs", RDFS.getURI());
        prefixMapping.setNsPrefix("xsd",  XSD.getURI());
    }
}
