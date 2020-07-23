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
import org.apache.jena.shacl.compact.reader.ShaclcParseException;
import org.apache.jena.shacl.compact.reader.parser.ParseException;
import org.apache.jena.shacl.compact.reader.parser.ShaclCompactParserJJ;
import org.apache.jena.shacl.compact.reader.parser.TokenMgrError;
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.util.Context;

/** Parse <a href="https://w3c.github.io/shacl/shacl-compact-syntax/">SHACL Compact Syntax</a> (July 2020). */
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
        return parse(input, baseURI);
    }

    /**
     * Parse the file to get SHACL shapes.
     * @param input
     * @param baseURI
     * @return Shapes
     */
    public static Shapes parse(InputStream input, String baseURI) {
        Graph graph = GraphFactory.createDefaultGraph();
        StreamRDF dest = StreamRDFLib.graph(graph);
        parseSHACLC(input, baseURI, dest, null);
        return Shapes.parse(graph);
    }

    /** Parse from an {@code InputStream} sending the triples to a {@link StreamRDF}. */
    public static void parseSHACLC(InputStream input, StreamRDF stream) {
        parseSHACLC(input, null, stream, null);
    }

    /** Parse from an {@code InputStream} sending the triples to a {@link StreamRDF}. */
    public static void parseSHACLC(InputStream input, String baseURI, StreamRDF stream, Context context) {
        ShaclCompactParserJJ parser = new ShaclCompactParserJJ(input, StandardCharsets.UTF_8.name());
        parse$(parser, stream, baseURI, context);
    }

    /**
     * Parse from an {@code Reader} sending the triples to a {@link StreamRDF}.
     * {@code InputStream} recommended unless it is a {@code StringReader}.
     * The reader should be UTF-8.
     */
    public static void parseSHACLC(Reader reader, String baseURI, StreamRDF stream, Context context) {
        ShaclCompactParserJJ parser = new ShaclCompactParserJJ(reader);
        parse$(parser, stream, baseURI, context);
    }

    private static void parse$(ShaclCompactParserJJ parser, StreamRDF stream, String baseURI, Context context) {
        Prologue prologue = parser.getPrologue();
        stream.start();

        SHACLC.addStandardPrefixes(prologue.getPrefixMapping());
        parser.start(stream);
        try {
            if ( baseURI != null )
                parser.getPrologue().setBaseURI(baseURI);
            parser.Unit();
        } catch (ParseException ex) {
            throw new ShaclcParseException(ex.getMessage(), ex.currentToken.beginLine, ex.currentToken.beginColumn);
        }
        catch ( TokenMgrError tErr) {
            int col = parser.token.endColumn ;
            int line = parser.token.endLine ;
            throw new ShaclcParseException(tErr.getMessage(), line, col) ;
        }
        parser.finish();
        stream.finish();
    }

    private static void prefix(Prologue prologue, String prefix, String uri) {
        prologue.getPrefixMapping().setNsPrefix(prefix, uri);
    }
}
