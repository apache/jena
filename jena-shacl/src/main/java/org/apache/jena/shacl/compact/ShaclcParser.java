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

import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.graph.Graph;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFLib;
import org.apache.jena.shacl.Shapes;
import org.apache.jena.shacl.vocabulary.SHACL;
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;
import org.apache.jena.shacl.compact.parser.ParseException;
import org.apache.jena.shacl.compact.parser.ShaclCompactParserJJ;

public class ShaclcParser {

    /**
     * Parse the file to get SHACL shapes.
     * @param filename
     * @return Shapes
     */
    public static Shapes parse(String filename) {
        return parse(filename, null);
    }
    /**
     * Parse the file to get SHACL shapes.
     * @param filename
     * @param baseURI
     * @return Shapes
     */
    public static Shapes parse(String filename, String baseURI) {
        InputStream input = IO.openFile(filename);
        Graph graph = parseSHACLC(input, baseURI);
        return Shapes.parse(graph);
    }

    /** Parse from an {@code InputStream} to get the SHACL graph. */
    public static Graph parseSHACLC(InputStream input) {
        return parseSHACLC(input, (String)null);
    }

    /** Parse from an {@code InputStream} to get the SHACL graph. */
    public static Graph parseSHACLC(InputStream input, String baseURI) {
        ShaclCompactParserJJ parser = new ShaclCompactParserJJ(input, StandardCharsets.UTF_8.name());
        return parseIntoGraph$(parser, baseURI);
    }

    /** Parse from an {@code InputStream} sending the triples to a {@link StreamRDF}. */
    public static void parseSHACLC(InputStream input, StreamRDF stream) {
        parseSHACLC(input, null, stream);
    }
    
    /** Parse from an {@code InputStream} sending the triples to a {@link StreamRDF}. */
    public static void parseSHACLC(InputStream input, String baseURI, StreamRDF stream) {
        ShaclCompactParserJJ parser = new ShaclCompactParserJJ(input, StandardCharsets.UTF_8.name());
        parse$(parser, stream, baseURI);
    }
    
    /**
     * Parse from an {@code Reader} to get the SHACL graph.
     * The reader should be UTF-8
     */
    /*package*/ static Graph parseSHACLC(Reader reader, String baseURI) {
        ShaclCompactParserJJ parser = new ShaclCompactParserJJ(reader);
        return parseIntoGraph$(parser, baseURI);
    }

    private static Graph parseIntoGraph$(ShaclCompactParserJJ parser, String baseURI) {
        Graph graph = GraphFactory.createDefaultGraph();
        StreamRDF stream = StreamRDFLib.graph(graph);
        parse$(parser, stream, baseURI);
        return graph;
    }
    
    private static void parse$(ShaclCompactParserJJ parser, StreamRDF stream, String baseURI) {
        Prologue prologue = parser.getPrologue();
        stream.start();
        if ( true ) {
            // Always add these prefixes to the output stream.
            // These are required by the test suite.
            // Do before parsing in case the SHACLC file overrides them.
            prefix(stream, prologue, "rdf",  RDF.getURI());
            prefix(stream, prologue, "rdfs", RDFS.getURI());
            prefix(stream, prologue, "sh",   SHACL.getURI());
            prefix(stream, prologue, "xsd",  XSD.getURI());
            prefix(stream, prologue, "owl",  OWL.getURI());
        }

        parser.start(stream);
        try {
            if ( baseURI != null )
                parser.getPrologue().setBaseURI(baseURI);
            parser.Unit();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        parser.finish();
        stream.finish();
    }
    
    private static void prefix(StreamRDF stream, Prologue prologue, String prefix, String uri) {
        stream.prefix(prefix, uri);
        prologue.getPrefixMapping().setNsPrefix(prefix, uri);
    }
}

