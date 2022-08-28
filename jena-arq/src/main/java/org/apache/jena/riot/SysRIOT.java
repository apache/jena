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

package org.apache.jena.riot;

import java.util.Objects;

import org.apache.jena.atlas.lib.IRILib ;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.irix.IRIs;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

public class SysRIOT
{
    public static final String riotLoggerName = "org.apache.jena.riot" ;
    private static Logger riotLogger = LoggerFactory.getLogger(riotLoggerName) ;

    private static String riotBase = "http://jena.apache.org/riot/" ;
    private static boolean strictMode                  = false ;

    /**
     * Context key for old style RDFWriter properties. The value of this in a
     * {@link Context} must be a {@code Map<String, Object>}. The entries of the
     * map are used to set writer properties before the
     * {@link RDFWriter} is called. Only has any effect on RDF/XML and
     * RDF/XML-ABBREV.
     */
    public static final Symbol sysRdfWriterProperties      = Symbol.create(riotBase+"rdfWriter_properties") ;

    /**
     * Context key for old style RDFReader properties. The value of this in a
     * {@link Context} must be a {@code Map<String, Object>}. The entries of the
     * map are used to set reader properties before the
     * RDFReader is called. Only has any effect on RDF/XML
     */
    public static final Symbol sysRdfReaderProperties      = Symbol.create(riotBase+"rdfReader_properties") ;

    /** Context key for the StreamManager */
    public static Symbol sysStreamManager = Symbol.create(riotBase+"streamManager") ;

    public static void setStrictMode(boolean state) {
        SysRIOT.strictMode = state ;
        //SysRIOT.StrictXSDLexicialForms = state ;
        //SysRIOT.AbsURINoNormalization = state ;
    }

    public static boolean isStrictMode() {
        return SysRIOT.strictMode ;
    }

    public static String fmtMessage(String message, long line, long col) {
        if ( col == -1 && line == -1 )
            return message;
        if ( col == -1 && line != -1 )
            return String.format("[line: %d] %s", line, message);
        if ( col != -1 && line == -1 )
            return String.format("[col: %d] %s", col, message);
        // Mild attempt to keep some alignment
        return String.format("[line: %d, col: %-2d] %s", line, col, message);
    }

    public static Logger getLogger() {
        return riotLogger;
    }

    /** @deprecated Use {@code IRIs.getBaseStr();} */
    @Deprecated
    public static String chooseBaseIRI() {
        return IRIs.getBaseStr();
    }

    /**
     * Return a URI suitable for a baseURI, based on some input (which may be null).
     *
     * @deprecated Use {@link IRIs#toBase(String)}
     */
    @Deprecated
    public static String chooseBaseIRI(String baseURI) {
        return IRIs.toBase(baseURI);
    }

    /**
     * Choose base IRI, from a given one and a filename. Prefer the given base;
     * turn any filename into an IRI which is resolved as well.
     */
    public static String chooseBaseIRI(String baseIRI, String fileOrIri) {
        if ( baseIRI != null )
            return baseIRI;
        if ( fileOrIri == null || fileOrIri.equals("-") )
            return "http://localhost/stdin/";
        return IRIs.toBase(fileOrIri);
    }

    public static String filename2baseIRI(String filename) {
        if ( filename == null || filename.equals("-") )
            return "http://localhost/stdin/";
        String x = IRILib.filenameToIRI(filename);
        return x;
    }


    private static Boolean isJSONLD11 = null;
    private static ReaderRIOTFactory readerFactoryJsonldDft = null;
    private static ReaderRIOTFactory readerFactoryJsonld10;
    private static ReaderRIOTFactory readerFactoryJsonld11;

    /**
     * <p>
     * Flip between JSON-LD 1.0 and JSON-LD 1.1 as the default parser for JSON-LD
     * ({@code Lang.JSONLD}). Both are available as {@code Lang.JSONLD10} and
     * {@code Lang.JSONLD11} respectively.
     * </p><p>
     * This function controls the setting of content type
     * "application/ld+json" and {@code Lang.JSONLD} for input (parsing).
     * </p><p>
     * <em>This function is not a permanent API.</em>
     * </p><p>
     * The default output is currently fixed as JSON-LD 1.1.
     * A specific version can be obtained by choosing the versioned language name,
     * {@code Lang.JSONLD10} or {@code Lang.JSONLD11}.
     * </p><p>
     * Apache Jena uses
     * <a href="https://github.com/jsonld-java/jsonld-java">jsonld-java</a> for JSON-LD 1.0
     * and
     * <a href="https://github.com/filip26/titanium-json-ld">Titanium</a> for JSON-LD 1.1.
     * </p><p>We are grateful to each of communities for the work in implementing and maintaining these projects.
     * </p>
     *
     * @param version   A string that is either "1.1" or "1.0" or "" (reset to system installation default)
     */
    public static void setDefaultJSONLD(String version) {
        Objects.requireNonNull(version, "Argument 'version' must be \"1.1\", \"1.0\" or \"\" (empty string)");

        if ( readerFactoryJsonldDft == null ) {
            readerFactoryJsonldDft = RDFParserRegistry.getFactory(Lang.JSONLD);
            readerFactoryJsonld10  = RDFParserRegistry.getFactory(Lang.JSONLD10);
            readerFactoryJsonld11  = RDFParserRegistry.getFactory(Lang.JSONLD11);
        }
        switch (version) {
            case "":
                RDFParserRegistry.registerLangTriples(Lang.JSONLD, readerFactoryJsonldDft);
                RDFParserRegistry.registerLangQuads(Lang.JSONLD, readerFactoryJsonldDft);
                return;
            case "1.1":
                RDFParserRegistry.registerLangTriples(Lang.JSONLD, readerFactoryJsonld11);
                RDFParserRegistry.registerLangQuads(Lang.JSONLD, readerFactoryJsonld11);
                return;
            case "1.0":
                RDFParserRegistry.registerLangTriples(Lang.JSONLD, readerFactoryJsonld10);
                RDFParserRegistry.registerLangQuads(Lang.JSONLD, readerFactoryJsonld10);
                return;
            default:
                Log.warn(SysRIOT.class, "Version string not recognized: '"+version+"'");
        }
    }
}
