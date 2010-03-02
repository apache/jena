/*
 * (c) Copyright 2010 Talis Information Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.riot;

import static com.hp.hpl.jena.riot.WebContent.* ;

import com.hp.hpl.jena.util.FileUtils ;

public enum Lang
{
    RDFXML("RDF/XML", true) ,
    NTRIPLES("N-Triples", true) ,
    N3("N3", true) ,
    TURTLE("Turtle", true) ,
    
    NQUADS("N-Quads", false) ,
    TRIG("TriG", false)
    ;
    
    
    private final String name ;
    private final boolean isTriples ;

//    public static final String langXML          = langXML ;
//    public static final String langNTriple      = langNTriple ; // FileUtils is wrong.
//    public static final String langN3           = FileUtils.langN3 ;
//    public static final String langTurtle       = FileUtils.langTurtle ;
//    
//    public static final String langNQuads       = "N-QUADS" ;
//    public static final String langTrig         = "TRIG" ;

    public static final String[] extRDFXML      = { "rdf", "owl", "xml" } ;
    public static final String[] extNTriples    = { "nt" } ;
    public static final String[] extNTurtle     = { "ttl" } ;
    public static final String[] extN3          = { "n3" } ;
    public static final String[] extNQuads      = { "nq" } ;
    public static final String[] extTrig        = { "trig" } ;

    
    private Lang(String name, boolean isTriples)
    {
        this.name = name ;
        this.isTriples = isTriples ;
    }
    
    
    public String getName() { return name ; }
    
    public boolean isTriples() { return isTriples ; }
    public boolean isQuads() { return !isTriples ; }
    
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
        if ( name.equalsIgnoreCase(langRDFXML) )                return RDFXML ;
        if ( name.equalsIgnoreCase(langRDFXMLAbbrev) )          return RDFXML ;
        if ( name.equalsIgnoreCase(langNTriple) )               return NTRIPLES ;
        if ( name.equalsIgnoreCase(langNTriples) )              return NTRIPLES ;
        if ( name.equalsIgnoreCase(langTurtle) )                return TURTLE ;
        if ( name.equalsIgnoreCase(langNQuads) )                return NQUADS ;
        if ( name.equalsIgnoreCase(langTriG) )                  return TRIG ;
        return dftLang ;
    }
    
    /** Guess the filetype, based on filename, or URL, extenstion.
     * Returns null if there isn't a guess available
     */
    public static Lang guess(String resourceIRI)
    {
        String ext = FileUtils.getFilenameExt(resourceIRI).toLowerCase() ;
        
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
 * (c) Copyright 2010 Talis Information Ltd.
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