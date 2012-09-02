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

package org.openjena.riot;

import static org.openjena.riot.WebContent.langNQuads ;
import static org.openjena.riot.WebContent.langNTriple ;
import static org.openjena.riot.WebContent.langNTriples ;
import static org.openjena.riot.WebContent.langRDFXML ;
import static org.openjena.riot.WebContent.langRDFXMLAbbrev ;
import static org.openjena.riot.WebContent.langTTL ;

import com.hp.hpl.jena.util.FileUtils ;

public enum Lang
{
    RDFXML (    "RDF/XML",      true,  
                new String[]{ "rdf", "owl", "xml" },    
                langRDFXML, langRDFXMLAbbrev) ,
                
    NTRIPLES (  "N-Triples",    true,
                new String[]{ "nt" },  
                langNTriples, langNTriple) ,
                
    N3 (        "N3",           true,
                new String[]{ "n3" }) ,
    
    TURTLE (    "Turtle",       true,
                new String[]{ "ttl" },   
                langTTL) ,
                
    RDFJSON (   "RDF/JSON",		true,
            	new String[]{ "rj", "json" }) ,
   
    NQUADS (    "N-Quads",      false,
                new String[]{ "nq" },
                langNQuads) ,
                
    TRIG (      "TriG",         false,
                new String[]{ "trig" }) ,
                
    //TUPLE("rdf-tuples", true, langNTuple)
    ;
    
    private final String name ;
    private final boolean isTriples ;
    private final String[] altNames ;
    private final String[] fileExtensions;

//    public static final String langXML          = langXML ;
//    public static final String langNTriple      = langNTriple ; // FileUtils is wrong.
//    public static final String langN3           = FileUtils.langN3 ;
//    public static final String langTurtle       = FileUtils.langTurtle ;
//    
//    public static final String langNQuads       = "N-QUADS" ;
//    public static final String langTrig         = "TRIG" ;

//    
    private Lang(String name, boolean isTriples, String[] fileExtensions, String...altNames )
    {
        this.name = name ;
        this.isTriples = isTriples ;
        this.altNames = altNames ;
        this.fileExtensions = fileExtensions;
    }
    
    public String getName() { return name ; }
    
    /** get the list of potential file extensions. */
    public String[] getFileExtensions()
    {
    	return fileExtensions; 	
    }
    
    /** Get the default file extension. */
    public String getDefaultFileExtension() {
        if ( getFileExtensions() == null )
            return null ;
    	return getFileExtensions()[0];
    }
    
    public boolean isTriples()  { return isTriples ; }
    public boolean isQuads()    { return ! isTriples ; }
    
    public String getContentType() { return WebContent.mapLangToContentType(this) ; }
    
    @Override
    public String toString() { return "lang:"+name ; }
    
    /** Translate a name into a Lang
     * Throws RiotException if the name is not recognized.
     */
    public static Lang get(String name)
    {
        Lang lang = get(name, null) ;
        if ( lang == null )
            throw new RiotException("No such language: "+name) ;
        return lang ;
    }
    
    /** Translate a name into a Lang, rturn the default if no match found.
     */
    public static Lang get(String name, Lang dftLang)
    {
        if ( matchesLangName(name, Lang.RDFXML) )       return RDFXML ;
        if ( matchesLangName(name, Lang.NTRIPLES) )     return NTRIPLES ;
        if ( matchesLangName(name, Lang.TURTLE) )       return TURTLE ;
        if ( matchesLangName(name, Lang.NQUADS) )       return NQUADS ;
        if ( matchesLangName(name, Lang.TRIG) )         return TRIG ;
        if ( matchesLangName(name, Lang.RDFJSON) )		return RDFJSON ;
        return dftLang ;
    }

    private static boolean matchesLangName(String name, Lang lang)
    {
        if ( name.equalsIgnoreCase(lang.name) ) return true ;
        if ( lang.altNames != null )
            for ( String x : lang.altNames )
            {
                if ( x.equalsIgnoreCase(name))
                    return true ;
            }
        return false ;
    }
    
    /** Guess the language, based on filename, or URL, extenstion.
     * Returns null if there isn't a guess available
     */
    public static Lang guess(String resourceIRI, Lang dftLang)
    {
        Lang lang = guess(resourceIRI) ;
        if ( lang != null )
            return lang ;
        return dftLang ;
    }
    
    /** Guess the language, based on filename, or URL, extenstion.
     * Returns null if there isn't a guess available
     */
    public static Lang guess(String resourceIRI)
    {
        if ( resourceIRI == null )
            return null ;
        String ext = FileUtils.getFilenameExt(resourceIRI).toLowerCase() ;
        if ( ext != null && ext.equals("gz") )
        {
            resourceIRI = resourceIRI.substring(0, resourceIRI.length()-".gz".length()) ;
            ext = FileUtils.getFilenameExt(resourceIRI).toLowerCase() ;
        }
        for (Lang lang : Lang.values())
        {
        	if (isOneOf( ext, lang.fileExtensions))
        		return lang;
        }
        return null;
    }

    private static boolean isOneOf(String ext, String[] names)
    {
        for ( String x : names )
        {
            if ( ext.equals(x) )
                return true ;
        }
        return false ;
    }
    
}
