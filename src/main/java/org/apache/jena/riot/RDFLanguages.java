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

import static org.openjena.riot.WebContent.contentTypeNQuads ;
import static org.openjena.riot.WebContent.contentTypeNQuadsAlt1 ;
import static org.openjena.riot.WebContent.contentTypeNQuadsAlt2 ;
import static org.openjena.riot.WebContent.contentTypeNTriples ;
import static org.openjena.riot.WebContent.contentTypeNTriplesAlt ;
import static org.openjena.riot.WebContent.contentTypeRDFJSON ;
import static org.openjena.riot.WebContent.contentTypeRDFXML ;
import static org.openjena.riot.WebContent.contentTypeTriG ;
import static org.openjena.riot.WebContent.contentTypeTriGAlt1 ;
import static org.openjena.riot.WebContent.contentTypeTriGAlt2 ;
import static org.openjena.riot.WebContent.contentTypeTurtle ;
import static org.openjena.riot.WebContent.contentTypeTurtleAlt1 ;
import static org.openjena.riot.WebContent.contentTypeTurtleAlt2 ;

import java.util.Locale ;
import java.util.Map ;

import org.openjena.atlas.lib.DS ;
import org.openjena.atlas.web.ContentType ;
import org.openjena.riot.Lang ;
import org.openjena.riot.RiotException ;

import com.hp.hpl.jena.util.FileUtils ;

/** Central registry of RDF langauges and syntaxes.
 * @see RDFParserRegistry
 */
public class RDFLanguages
{
    public static  void init () { initStandard() ; }
    
    public static final String strLangRDFXML     = "RDF/XML" ;
    public static final String strLangTurtle     = "Turtle" ;
    public static final String strLangNTriples   = "N-Triples" ;
    public static final String strLangN3         = "N3" ;
    public static final String strLangRDFJSON    = "RDF/JSON" ;
    public static final String strLangNQuads     = "N-Quads" ;
    public static final String strLangTriG       = "TriG" ;
    
    /** RDF/XML */
    public static final Lang2 langRDFXML   = LangBuilder.create(strLangRDFXML, contentTypeRDFXML)
                                                .addAltNames("RDFXML", "RDF/XML-ABBREV", "RDFXML-ABBREV")
                                                .addFileExtensions("rdf","owl","xml")
                                                .build() ;
    
    /** Turtle */
    public static final Lang2 langTurtle   = LangBuilder.create(strLangTurtle, contentTypeTurtle)
                                                .addAltNames("TTL")
                                                .addAltContentTypes(contentTypeTurtleAlt1, contentTypeTurtleAlt2)
                                                .addFileExtensions("ttl")
                                                //.addFileExtensions("n3")
                                                .build() ;
    
    /** N-Triples */
    public static final Lang2 langNTriples = LangBuilder.create(strLangNTriples, contentTypeNTriples)
                                                .addAltNames("NT", "NTriples", "NTriple", "N-Triple")
                                                .addAltContentTypes(contentTypeNTriplesAlt)
                                                .addFileExtensions("nt")
                                                .build() ;

    /** RDF/JSON (this is not JSON-LD) */
    public static final Lang2 langRDFJSON  = LangBuilder.create(strLangRDFJSON, contentTypeRDFJSON)
                                                .addAltNames("RDFJSON")
                                                .addFileExtensions("rj", "json")
                                                .build() ;
    
    /** TriG */
    public static final Lang2 langTriG     = LangBuilder.create(strLangTriG, contentTypeTriG)
                                                .addAltContentTypes(contentTypeTriGAlt1, contentTypeTriGAlt2)
                                                .addFileExtensions("trig")
                                                .build() ;
    
    /** N-Quads */
    public static final Lang2 langNQuads   = LangBuilder.create(strLangNQuads, contentTypeNQuads)
                                                .addAltNames("NQ", "NQuads", "NQuad", "N-Quad")   
                                                .addAltContentTypes(contentTypeNQuadsAlt1, contentTypeNQuadsAlt2)
                                                .addFileExtensions("nq")
                                                .build() ;

    // ---- Central registry
    
    /** Mapping of content type (main and alternatives) to language */  
    private static Map<String, Lang2> mapContentTypeToLang              = DS.map() ;

    /** Mapping of file extension to language */
    private static Map<String, Lang2> mapFileExtToLang                  = DS.map() ;

    /** Mapping of colloquial name to language */
    private static Map<String, Lang2> mapLabelToLang                    = DS.map() ;
    
    // ----------------------
    
    /** Standard built-in languages */  
    public static void initStandard()
    {
        register(langRDFXML) ;
        register(langTurtle) ;
        register(langNTriples) ;
        register(langRDFJSON) ;
        register(langTriG) ;
        register(langNQuads) ;
    }

    /** Register a language.
     * To create a {@link Lang2} object use {@link LangBuilder}.
     * See also 
     * {@link RDFParserRegistry#registerLangTriples} and 
     * {@link RDFParserRegistry#registerLangQuads}
     * for registering a language and it's RDF parser fatory.
     * 
     * @see RDFParserRegistry
     */
    public static void register(Lang2 lang)
    {
        if ( lang == null )
            throw new IllegalArgumentException("null for language") ;
        checkRegistration(lang) ;

        mapLabelToLang.put(canonicalKey(lang.getLabel()),  lang) ;
        for (String altName : lang.getAltNames() )
            mapContentTypeToLang.put(canonicalKey(altName), lang) ;
        
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

    /** Remove a regsitration of a language - this also removes all recorded mapping
     * of content types and file extensions. 
     */
    
    public static void unregister(Lang2 lang)
    {
        if ( lang == null )
            throw new IllegalArgumentException("null for language") ;
        mapLabelToLang.remove(canonicalKey(lang.getLabel())) ;
        
        mapContentTypeToLang.remove(canonicalKey(lang.getContentType().getContentType())) ;
        
        for ( String ct : lang.getAltContentTypes() )
            mapContentTypeToLang.remove(canonicalKey(ct)) ;
        for ( String ext : lang.getFileExtensions() )
            mapFileExtToLang.remove(canonicalKey(ext)) ;
    }

    private static void checkRegistration(Lang2 lang)
    {
        if ( true ) return ;
        if ( lang == null )
            return ;
        Lang2 lang2 = mapLabelToLang.get(canonicalKey(lang.getLabel())) ;
        if ( lang.equals(lang2)  )
            return ;
        throw new IllegalArgumentException("Lang '"+lang+"'is not consistent with "+lang2) ;
    }
    
    /** return true if the language is registered with the triples parser factories */
    public static boolean isTriples(Lang2 lang) { return RDFParserRegistry.isTriples(lang) ; }
    
    /** return true if the language is registered with the quads parser factories */
    public static boolean isQuads(Lang2 lang)   { return RDFParserRegistry.isQuads(lang) ; }
    
    /** Map a content type (without charset) to a {@link Lang2} */
    public static Lang2 contentTypeToLang(String contentType)
    {
        String key = canonicalKey(contentType) ;
        return mapContentTypeToLang.get(key) ;
    }

    /** Map a content type (without charset) to a {@link Lang2} */
    public static Lang2 contentTypeToLang(ContentType ct)
    {
        String key = canonicalKey(ct.getContentType()) ;
        return mapContentTypeToLang.get(key) ;
    }


    /** Map a colloquial name (e.g. "Turtle") to a {@link Lang2} */
    public static Lang2 shortnameToLang(String label)
    {
        String key = canonicalKey(label) ;
        return mapLabelToLang.get(key) ;
    }
    
    /** Try to map a file extension to a {@link Lang2}; return null on no registered mapping */
    public static Lang2 fileExtToLang(String ext)
    {
        if ( ext == null ) return null ;
        if ( ext.startsWith(".") ) 
            ext = ext.substring(1) ;
        ext = canonicalKey(ext) ;
        return mapFileExtToLang.get(ext) ;
    }

    /** Try to map a file name to a {@link Lang2}; return null on no registered mapping */
    public static Lang2 filenameToLang(String filename)
    {
        return fileExtToLang(FileUtils.getFilenameExt(filename)) ;
    }

    /** Turn a name for a language into a {@link Lang2} object.
     *  The name can be a label, or a content type.
     */
    public static Lang2 nameToLang(String langName)
    {
        if ( langName == null )
            return null ;
        
        Lang2 lang = shortnameToLang(langName) ;
        if ( lang != null )
            return lang ;
        lang = contentTypeToLang(langName) ;
        return lang ;
    }
    
    static String canonicalKey(String x) { return x.toLowerCase(Locale.US) ; }

    // TEMPORARY
    public static Lang convert(Lang2 lang2)
    {
        if ( lang2 == langRDFXML )      return Lang.RDFXML ;
        if ( lang2 == langTurtle )      return Lang.TURTLE ;
        if ( lang2 == langNTriples )    return Lang.NTRIPLES ;
        if ( lang2 == langRDFJSON )     return Lang.RDFJSON ;
        if ( lang2 == langTriG )        return Lang.TRIG ;
        if ( lang2 == langNQuads )      return Lang.NQUADS ;
        throw new RiotException("No such language to convert: "+lang2) ;
    }

    public static ContentType guessContentType(String resourceName)
    {
        if ( resourceName == null )
            return null ;
        Lang2 lang = filenameToLang(resourceName) ;
        if ( lang == null )
            return null ;
        return lang.getContentType() ;
    }
}

