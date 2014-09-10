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

package org.apache.jena.riot;

import static org.apache.jena.riot.WebContent.* ;

import java.util.Collection ;
import java.util.Collections ;
import java.util.Locale ;
import java.util.Map ;

import org.apache.jena.atlas.lib.DS ;
import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.atlas.web.ContentType ;
import org.apache.jena.atlas.web.MediaType ;

import com.hp.hpl.jena.util.FileUtils ;

/** Central registry of RDF languages and syntaxes.
 * @see RDFParserRegistry
 * @see RDFFormat
 */
public class RDFLanguages
{
    // Display names
    public static final String strLangRDFXML     = "RDF/XML" ;
    public static final String strLangTurtle     = "Turtle" ;
    public static final String strLangNTriples   = "N-Triples" ;
    public static final String strLangN3         = "N3" ;
    public static final String strLangRDFJSON    = "RDF/JSON" ;
    public static final String strLangJSONLD     = "JSON-LD" ;
    public static final String strLangNQuads     = "N-Quads" ;
    public static final String strLangTriG       = "TriG" ;
    public static final String strLangCSV        = "CSV";
    
    /*
     * ".owl" is not a formally registered file extension for OWL 
     *  using RDF/XML. It was mentioned in OWL1 (when there was
     *  formally only one syntax for publishing RDF).
     *   
     * OWL2 does not mention it.
     * 
     * ".owx" is the OWL direct XML syntax.
     */

    /** <a href="http://www.w3.org/TR/REC-rdf-syntax/">RDF/XML</a> */
    public static final Lang RDFXML   = LangBuilder.create(strLangRDFXML, contentTypeRDFXML)
                                                .addAltNames("RDFXML", "RDF/XML-ABBREV", "RDFXML-ABBREV")
                                                .addFileExtensions("rdf", "owl", "xml")
                                                .build() ;
    
    /** <a href="http://www.w3.org/TR/turtle/">Turtle</a>*/
    public static final Lang TURTLE   = LangBuilder.create(strLangTurtle, contentTypeTurtle)
                                                .addAltNames("TTL")
                                                .addAltContentTypes(contentTypeTurtleAlt1, contentTypeTurtleAlt2)
                                                .addFileExtensions("ttl")
                                                .build() ;
    /** Alternative constant for {@linkplain #TURTLE} */
    public static final Lang TTL    = TURTLE ;
    
    /** N3 (treat as Turtle) */
    public static final Lang N3   = LangBuilder.create(strLangN3, contentTypeN3)
                                                .addAltContentTypes(contentTypeN3, contentTypeN3Alt1, contentTypeN3Alt2)
                                                .addFileExtensions("n3")
                                                .build() ;
    
    /** <a href="http://www.w3.org/TR/n-triples/">N-Triples</a>*/
    public static final Lang NTRIPLES = LangBuilder.create(strLangNTriples, contentTypeNTriples)
                                                .addAltNames("NT", "NTriples", "NTriple", "N-Triple", "N-Triples")
                                                 // Remove? Causes more trouble than it's worth.
                                                .addAltContentTypes(contentTypeNTriplesAlt)
                                                .addFileExtensions("nt")
                                                .build() ;
    /** Alternative constant for {@linkplain #NTRIPLES} */
    public static final Lang NT     = NTRIPLES ;

    /** <a href="http://www.w3.org/TR/json-ld/">JSON-LD</a>. */
    public static final Lang JSONLD = LangBuilder.create(strLangJSONLD, "application/ld+json")
                                                .addAltNames("JSONLD")
                                                .addFileExtensions("jsonld")
                                                .build() ;
    
    /** <a href="http://www.w3.org/TR/rdf-json/">RDF/JSON</a>.  This is not <a href="http://www.w3.org/TR/json-ld/">JSON-LD</a>. */
    public static final Lang RDFJSON  = LangBuilder.create(strLangRDFJSON, contentTypeRDFJSON)
                                                .addAltNames("RDFJSON")
                                                .addFileExtensions("rj", "json")
                                                .build() ;
    
    /** <a href="http://www.w3.org/TR/trig/">TriG</a> */
    public static final Lang TRIG     = LangBuilder.create(strLangTriG, contentTypeTriG)
                                                .addAltContentTypes(contentTypeTriGAlt1, contentTypeTriGAlt2)
                                                .addFileExtensions("trig")
                                                .build() ;
    
    /** <a href="http://www.w3.org/TR/n-quads">N-Quads</a> */
    public static final Lang NQUADS   = LangBuilder.create(strLangNQuads, contentTypeNQuads)
                                                .addAltNames("NQ", "NQuads", "NQuad", "N-Quad", "N-Quads")   
                                                .addAltContentTypes(contentTypeNQuadsAlt1, contentTypeNQuadsAlt2)
                                                .addFileExtensions("nq")
                                                .build() ;
    
    /** Alternative constant {@linkplain #NQUADS} */
    public static final Lang NQ     = NQUADS ;
    
    /** CSV data.  This can be read into an RDF model with simple conversion */
    public static final Lang CSV   = LangBuilder.create(strLangCSV, contentTypeTextCSV)
                                                .addAltNames("csv")   
                                                .addFileExtensions("csv")
                                                .build() ;

    /** The RDF syntax "RDF Thrift" : see http://jena.apache.org/documentation/io */ 
    public static final Lang THRIFT = LangBuilder.create("RDF_THRIFT", contentTypeRDFThrift)
                                                 .addAltNames("RDF-THRIFT", "TRDF")
                                                 .addFileExtensions("rt", "trdf")
                                                 .build() ;
    
    /** Text */
    public static final Lang TEXT   = LangBuilder.create("text", contentTypeTextPlain)
                                                 .addAltNames("TEXT")   
                                                 .addFileExtensions("txt")
                                                 .build() ;

    /** The "null" language */
    public static final Lang RDFNULL  = LangBuilder.create("rdf/null", "null/rdf")
                                                .addAltNames("NULL", "null")  
                                                .build() ;
    
    // ---- Central registry
    
    /** Mapping of colloquial name to language */
    private static Map<String, Lang> mapLabelToLang                    = DS.map() ;
    
    // For testing mainly.
    public static Collection<Lang> getRegisteredLanguages()     { return Collections.unmodifiableCollection(mapLabelToLang.values()); }
    
    /** Mapping of content type (main and alternatives) to language */  
    private static Map<String, Lang> mapContentTypeToLang              = DS.map() ;

    /** Mapping of file extension to language */
    private static Map<String, Lang> mapFileExtToLang                  = DS.map() ;

    // ----------------------
    public static void init() {}
    static { init$() ; }
    private static synchronized void init$()
    {
        initStandard() ;
        // Needed to avoid a class initialization loop. 
        Lang.RDFXML     = RDFLanguages.RDFXML ; 
        Lang.NTRIPLES   = RDFLanguages.NTRIPLES ;
        Lang.NT         = RDFLanguages.NT ;
        Lang.N3         = RDFLanguages.N3 ; 
        Lang.TURTLE     = RDFLanguages.TURTLE ;
        Lang.TTL        = RDFLanguages.TTL ;
        Lang.JSONLD     = RDFLanguages.JSONLD ;
        Lang.RDFJSON    = RDFLanguages.RDFJSON ; 
        Lang.NQUADS     = RDFLanguages.NQUADS ;
        Lang.NQ         = RDFLanguages.NQ ;
        Lang.TRIG       = RDFLanguages.TRIG ;
        Lang.RDFTHRIFT     = RDFLanguages.THRIFT ;
        Lang.CSV        = RDFLanguages.CSV ;
        Lang.RDFNULL    = RDFLanguages.RDFNULL ;
    }
    // ----------------------
    
    /** Standard built-in languages */  
    private static void initStandard()
    {
        register(RDFXML) ;
        register(TURTLE) ;
        register(N3) ;
        register(NTRIPLES) ;
        register(JSONLD) ;
        register(RDFJSON) ;
        register(TRIG) ;
        register(NQUADS) ;
        register(THRIFT) ;
        register(CSV) ;
        register(RDFNULL) ;
        
        // Check for JSON-LD engine.
        String clsName = "com.github.jsonldjava.core.JsonLdProcessor" ;
        try {
            Class.forName(clsName) ;
        } catch (ClassNotFoundException ex) {
            Log.warn(RDFLanguages.class, "java-jsonld classes not on the classpath - JSON-LD input-output not available.") ;
            Log.warn(RDFLanguages.class, "Minimum jarfiles are jsonld-java, jackson-core, jackson-annotations") ;
            Log.warn(RDFLanguages.class, "If using a Jena distribution, put all jars in the lib/ directory on the classpath") ;
            return ;
        }
    }

    /** Register a language.
     * To create a {@link Lang} object use {@link LangBuilder}.
     * See also 
     * {@link RDFParserRegistry#registerLang}
     * for registering a language and it's RDF parser fatory.
     * 
     * @see RDFParserRegistry
     */
    public static void register(Lang lang)
    {
        if ( lang == null )
            throw new IllegalArgumentException("null for language") ;
        checkRegistration(lang) ;

        mapLabelToLang.put(canonicalKey(lang.getLabel()),  lang) ;
        
        for (String altName : lang.getAltNames() )
            mapLabelToLang.put(canonicalKey(altName), lang) ;
        
        mapContentTypeToLang.put(canonicalKey(lang.getContentType().getContentType()), lang) ;
        for ( String ct : lang.getAltContentTypes() )
            mapContentTypeToLang.put(canonicalKey(ct), lang) ;
        for ( String ext : lang.getFileExtensions() )
        {
            if ( ext.startsWith(".") ) 
                ext = ext.substring(1) ;
            mapFileExtToLang.put(canonicalKey(ext), lang) ;
        }
    }

    private static void checkRegistration(Lang lang)
    {
        if ( lang == null )
            return ;
        String label = canonicalKey(lang.getLabel()) ;
        Lang lang2 = mapLabelToLang.get(label) ;
        if ( lang2 == null )
            return ;
        if ( lang.equals(lang2) )
            return ;
        
        // Content type.
        if ( mapContentTypeToLang.containsKey(lang.getContentType().getContentType()))
        {
            String k = lang.getContentType().getContentType() ;
            error("Language overlap: " +lang+" and "+mapContentTypeToLang.get(k)+" on content type "+k) ;
        }
        for (String altName : lang.getAltNames() )
            if ( mapLabelToLang.containsKey(altName) )
                error("Language overlap: " +lang+" and "+mapLabelToLang.get(altName)+" on name "+altName) ;
        for (String ct : lang.getAltContentTypes() )
            if ( mapContentTypeToLang.containsKey(ct) )
                error("Language overlap: " +lang+" and "+mapContentTypeToLang.get(ct)+" on content type "+ct) ;
        for (String ext : lang.getFileExtensions() )
            if ( mapFileExtToLang.containsKey(ext) )
                error("Language overlap: " +lang+" and "+mapFileExtToLang.get(ext)+" on file extension type "+ext) ;
    }

    /** Remove a regsitration of a language - this also removes all recorded mapping
     * of content types and file extensions. 
     */
    
    public static void unregister(Lang lang)
    {
        if ( lang == null )
            throw new IllegalArgumentException("null for language") ;
        checkRegistration(lang) ; 
        mapLabelToLang.remove(canonicalKey(lang.getLabel())) ;
        mapContentTypeToLang.remove(canonicalKey(lang.getContentType().getContentType())) ;
        
        for ( String ct : lang.getAltContentTypes() )
            mapContentTypeToLang.remove(canonicalKey(ct)) ;
        for ( String ext : lang.getFileExtensions() )
            mapFileExtToLang.remove(canonicalKey(ext)) ;
    }
    
    public static boolean isRegistered(Lang lang)
    {
        if ( lang == null )
            throw new IllegalArgumentException("null for language") ;
        String label = canonicalKey(lang.getLabel()) ;
        Lang lang2 = mapLabelToLang.get(label) ;
        if ( lang2 == null )
            return false ;
        checkRegistration(lang) ;
        return true ;
    }
    
    /** return true if the language is registered as a triples language */
    public static boolean isTriples(Lang lang) { return RDFParserRegistry.isTriples(lang) ; }
    
    /** return true if the language is registered as a quads language */
    public static boolean isQuads(Lang lang) { return RDFParserRegistry.isQuads(lang) ; }

    /** Map a content type (without charset) to a {@link Lang} */
    public static Lang contentTypeToLang(String contentType)
    {
        if ( contentType == null )
            return null ;
        String key = canonicalKey(contentType) ;
        return mapContentTypeToLang.get(key) ;
    }

    /** Map a content type (without charset) to a {@link Lang} */
    public static Lang contentTypeToLang(ContentType ct)
    {
        if ( ct == null )
            return null ;
        String key = canonicalKey(ct.getContentType()) ;
        return mapContentTypeToLang.get(key) ;
    }

    public static String getCharsetForContentType(String contentType)
    {
        MediaType ct = MediaType.create(contentType) ;
        if ( ct.getCharset() != null )
            return ct.getCharset() ;
        
        String mt = ct.getContentType() ;
        if ( contentTypeNTriples.equals(mt) )       return charsetUTF8 ;
        if ( contentTypeNTriplesAlt.equals(mt) )    return charsetASCII ;
        if ( contentTypeNQuads.equals(mt) )         return charsetUTF8 ;
        if ( contentTypeNQuadsAlt1.equals(mt) )      return charsetASCII ;
        if ( contentTypeNQuadsAlt2.equals(mt) )      return charsetASCII ;
        return charsetUTF8 ;
    }

    
    /** Map a colloquial name (e.g. "Turtle") to a {@link Lang} */
    public static Lang shortnameToLang(String label)
    {
        if ( label == null )
            return null ;
        String key = canonicalKey(label) ;
        return mapLabelToLang.get(key) ;
    }
    
    /** Try to map a file extension to a {@link Lang}; return null on no registered mapping */
    public static Lang fileExtToLang(String ext)
    {
        if ( ext == null ) return null ;
        if ( ext.startsWith(".") ) 
            ext = ext.substring(1) ;
        ext = canonicalKey(ext) ;
        return mapFileExtToLang.get(ext) ;
    }

    /** Try to map a resource name to a {@link Lang}; return null on no registered mapping */
    public static Lang resourceNameToLang(String resourceName) { return filenameToLang(resourceName) ; }
    
    /** Try to map a resource name to a {@link Lang}; return the given default where there is no registered mapping */
    public static Lang resourceNameToLang(String resourceName, Lang dftLang) { return filenameToLang(resourceName, dftLang) ; }
    
    /** Try to map a file name to a {@link Lang}; return null on no registered mapping */
    public static Lang filenameToLang(String filename)
    {
        if ( filename == null ) return null ;
        if ( filename.endsWith(".gz") )
            filename = filename.substring(0, filename.length()-3) ;
        return fileExtToLang(FileUtils.getFilenameExt(filename)) ;
    }

    /** Try to map a file name to a {@link Lang}; return the given default where there is no registered mapping */
    public static Lang filenameToLang(String filename, Lang dftLang)
    {
        Lang lang = filenameToLang(filename) ;
        return (lang == null) ? dftLang : lang ;
    }


    /** Turn a name for a language into a {@link Lang} object.
     *  The name can be a label, or a content type.
     */
    public static Lang nameToLang(String langName)
    {
        if ( langName == null )
            return null ;
        
        Lang lang = shortnameToLang(langName) ;
        if ( lang != null )
            return lang ;
        lang = contentTypeToLang(langName) ;
        return lang ;
    }
    
    static String canonicalKey(String x) { return x.toLowerCase(Locale.ROOT) ; }

    public static ContentType guessContentType(String resourceName)
    {
        if ( resourceName == null )
            return null ;
        Lang lang = filenameToLang(resourceName) ;
        if ( lang == null )
            return null ;
        return lang.getContentType() ;
    }

    private static void error(String message)
    {
        throw new RiotException(message) ; 
    }

    public static boolean sameLang(Lang lang1, Lang lang2)
    {
        if ( lang1 == null || lang2 == null ) return false ; 
        if ( lang1 == lang2 ) return true ;
        return lang1.getLabel() == lang2.getLabel() ;
    }
}

