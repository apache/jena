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

import org.apache.jena.atlas.lib.IRILib ;
import org.apache.jena.irix.IRIs;
import org.apache.jena.rdf.model.RDFReaderI;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

public class SysRIOT
{
    public static final String riotLoggerName = "org.apache.jena.riot" ;
    private static Logger riotLogger = LoggerFactory.getLogger(riotLoggerName) ;

    /** @deprecated Do not use - lexical forms are always strict. */
    @Deprecated
    public static boolean StrictXSDLexicialForms      = false ;

    public static boolean strictMode                  = false ;

    /** Some people argue that absolute URIs should not be normalized.
     * This flag puts IRI resolution in that mode.
     * Beware: inconsistencies arise - relative URIs are still normalized so
     * where the unnormalized part is in a prefix name changes the outcome.
     * Jena has always normalized absolute URIs.
     */
    public static final boolean AbsURINoNormalization   = false ;
    public static final String BNodeGenIdPrefix         = "genid" ;

    private static String riotBase = "http://jena.apache.org/riot/" ;

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
     * {@link RDFReaderI} is called. Only has any effect on RDF/XML,
     */
    public static final Symbol sysRdfReaderProperties      = Symbol.create(riotBase+"rdfReader_properties") ;

    /** @deprecated Use {@link #sysRdfWriterProperties} */
    @Deprecated
    public static final Symbol rdfWriterProperties      = sysRdfWriterProperties ;

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
     * Choose base IRI, from a given one and a filename. Prefer the given base ; turn
     * any filename into an IRI. String will need to be resolved as well.
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
}
