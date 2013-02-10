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

import static org.apache.jena.riot.WebContent.contentTypeN3 ;
import static org.apache.jena.riot.WebContent.contentTypeN3Alt1 ;
import static org.apache.jena.riot.WebContent.contentTypeN3Alt2 ;
import static org.apache.jena.riot.WebContent.contentTypeNQuads ;
import static org.apache.jena.riot.WebContent.contentTypeNQuadsAlt1 ;
import static org.apache.jena.riot.WebContent.contentTypeNQuadsAlt2 ;
import static org.apache.jena.riot.WebContent.contentTypeNTriples ;
import static org.apache.jena.riot.WebContent.contentTypeNTriplesAlt ;
import static org.apache.jena.riot.WebContent.contentTypeRDFJSON ;
import static org.apache.jena.riot.WebContent.contentTypeRDFXML ;
import static org.apache.jena.riot.WebContent.contentTypeTriG ;
import static org.apache.jena.riot.WebContent.contentTypeTriGAlt1 ;
import static org.apache.jena.riot.WebContent.contentTypeTriGAlt2 ;
import static org.apache.jena.riot.WebContent.contentTypeTurtle ;
import static org.apache.jena.riot.WebContent.contentTypeTurtleAlt1 ;
import static org.apache.jena.riot.WebContent.contentTypeTurtleAlt2 ;

import java.util.Collection ;
import java.util.Collections ;
import java.util.Locale ;
import java.util.Map ;

import org.apache.jena.atlas.lib.DS ;
import org.apache.jena.atlas.web.ContentType ;

import com.hp.hpl.jena.util.FileUtils ;

/** Central registry of RDF langauges and syntaxes.
 * @see RDFParserRegistry
 */
public class RDFLanguages
{
    // Display names
    public static final String strLangRDFXML     = "RDF/XML" ;
    public static final String strLangTurtle     = "Turtle" ;
    public static final String strLangNTriples   = "N-Triples" ;
    public static final String strLangN3         = "N3" ;
    public static final String strLangRDFJSON    = "RDF/JSON" ;
    public static final String strLangNQuads     = "N-Quads" ;
    public static final String strLangTriG       = "TriG" ;
    
    /** RDF/XML */
    public static final Lang RDFXML   = LangBuilder.create(strLangRDFXML, contentTypeRDFXML)
                                                .addAltNames("RDFXML", "RDF/XML-ABBREV", "RDFXML-ABBREV")
                                                .addFileExtensions("rdf","owl","xml")
                                                .build() ;
    
    /** Turtle */
    public static final Lang TURTLE   = LangBuilder.create(strLangTurtle, contentTypeTurtle)
                                                .addAltNames("TTL")
                                                .addAltContentTypes(contentTypeTurtleAlt1, contentTypeTurtleAlt2)
                                                .addFileExtensions("ttl")
                                                //.addFileExtensions("n3")
                                                .build() ;
    /** N3 (treat as Turtle) */
    public static final Lang N3   = LangBuilder.create(strLangN3, contentTypeN3)
                                                .addAltContentTypes(contentTypeN3, contentTypeN3Alt1, contentTypeN3Alt2)
                                                .addFileExtensions("n3")
                                                .build() ;
    
    /** N-Triples */
    public static final Lang NTRIPLES = LangBuilder.create(strLangNTriples, contentTypeNTriples)
                                                .addAltNames("NT", "NTriples", "NTriple", "N-Triple", "N-Triples")
                                                .addAltContentTypes(contentTypeNTriplesAlt)
                                                .addFileExtensions("nt")
                                                .build() ;

    /** RDF/JSON (this is not JSON-LD) */
    public static final Lang RDFJSON  = LangBuilder.create(strLangRDFJSON, contentTypeRDFJSON)
                                                .addAltNames("RDFJSON")
                                                .addFileExtensions("rj", "json")
                                                .build() ;
    
    /** TriG */
    public static final Lang TRIG     = LangBuilder.create(strLangTriG, contentTypeTriG)
                                                .addAltContentTypes(contentTypeTriGAlt1, contentTypeTriGAlt2)
                                                .addFileExtensions("trig")
                                                .build() ;
    
    /** N-Quads */
    public static final Lang NQUADS   = LangBuilder.create(strLangNQuads, contentTypeNQuads)
                                                .addAltNames("NQ", "NQuads", "NQuad", "N-Quad", "N-Quads")   
                                                .addAltContentTypes(contentTypeNQuadsAlt1, contentTypeNQuadsAlt2)
                                                .addFileExtensions("nq")
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
        Lang.RDFXML = RDFLanguages.RDFXML ; 
        Lang.NTRIPLES = RDFLanguages.NTRIPLES ; 
        Lang.N3 = RDFLanguages.N3 ; 
        Lang.TURTLE = RDFLanguages.TURTLE ; 
        Lang.RDFJSON = RDFLanguages.RDFJSON ; 
        Lang.NQUADS = RDFLanguages.NQUADS ; 
        Lang.TRIG = RDFLanguages.TRIG ; 
    }
    // ----------------------
    
    /** Standard built-in languages */  
    private static void initStandard()
    {
        register(RDFXML) ;
        register(TURTLE) ;
        register(N3) ;
        register(NTRIPLES) ;
        register(RDFJSON) ;
        register(TRIG) ;
        register(NQUADS) ;
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
        String key = canonicalKey(contentType) ;
        return mapContentTypeToLang.get(key) ;
    }

    /** Map a content type (without charset) to a {@link Lang} */
    public static Lang contentTypeToLang(ContentType ct)
    {
        String key = canonicalKey(ct.getContentType()) ;
        return mapContentTypeToLang.get(key) ;
    }

    /** Map a colloquial name (e.g. "Turtle") to a {@link Lang} */
    public static Lang shortnameToLang(String label)
    {
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

    /** Try to map a file name to a {@link Lang}; return null on no registered mapping */
    public static Lang filenameToLang(String filename)
    {
        if ( filename == null ) return null ;
        if ( filename.endsWith(".gz") )
            filename = filename.substring(0, filename.length()-3) ;
        return fileExtToLang(FileUtils.getFilenameExt(filename)) ;
    }

    /** Try to map a file name to a {@link Lang}; return null on no registered mapping */
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
    
    static String canonicalKey(String x) { return x.toLowerCase(Locale.US) ; }

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

