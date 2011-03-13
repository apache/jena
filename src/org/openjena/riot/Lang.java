/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
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
    RDFXML(     "RDF/XML",      true, langRDFXML, langRDFXMLAbbrev) ,
    NTRIPLES(   "N-Triples",    true, langNTriples, langNTriple) ,
    N3(         "N3",           true) ,
    TURTLE(     "Turtle",       true, langTTL) ,
   
    NQUADS(     "N-Quads",      false, langNQuads) ,
    TRIG(       "TriG",         false) ,
    //TUPLE("rdf-tuples", true, langNTuple)
    ;
    
    private final String name ;
    private final boolean isTriples ;
    private final String[] altNames ;

//    public static final String langXML          = langXML ;
//    public static final String langNTriple      = langNTriple ; // FileUtils is wrong.
//    public static final String langN3           = FileUtils.langN3 ;
//    public static final String langTurtle       = FileUtils.langTurtle ;
//    
//    public static final String langNQuads       = "N-QUADS" ;
//    public static final String langTrig         = "TRIG" ;

    // File extension names
    private static final String[] extRDFXML      = { "rdf", "owl", "xml" } ;
    private static final String[] extNTriples    = { "nt" } ;
    private static final String[] extNTurtle     = { "ttl" } ;
    private static final String[] extN3          = { "n3" } ;
    private static final String[] extNQuads      = { "nq" } ;
    private static final String[] extTrig        = { "trig" } ;
    
    private Lang(String name, boolean isTriples, String...altNames)
    {
        this.name = name ;
        this.isTriples = isTriples ;
        this.altNames = altNames ;
    }
    
    public String getName() { return name ; }
    
    public boolean isTriples() { return isTriples ; }
    public boolean isQuads() { return ! isTriples ; }
    
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
        
        if ( isOneOf(ext, extRDFXML) )      return RDFXML ;
        if ( isOneOf(ext, extNTriples) )    return NTRIPLES ;
        if ( isOneOf(ext, extNTurtle) )     return TURTLE ;
        if ( isOneOf(ext, extN3) )          return N3 ;
        if ( isOneOf(ext, extNQuads) )      return NQUADS ;
        if ( isOneOf(ext, extTrig) )        return TRIG ;
        return null ;
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

/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */