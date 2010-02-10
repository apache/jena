/*
 * (c) Copyright 2010 Talis Information Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.riot;

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

    public static final String langXML          = FileUtils.langXML ;
    public static final String langNTriple      = "N-TRIPLES" ; // FileUtils is wrong.
    public static final String langN3           = FileUtils.langN3 ;
    public static final String langTurtle       = FileUtils.langTurtle ;
    
    public static final String langNQuads       = "N-QUADS" ;
    public static final String langTrig         = "TRIG" ;
    
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
    
    public static Lang get(String name)
    {
        if ( name.equalsIgnoreCase(langXML) )                   return RDFXML ;
        if ( name.equalsIgnoreCase(FileUtils.langXMLAbbrev) )   return RDFXML ;
        if ( name.equalsIgnoreCase(langNTriple) )               return NTRIPLES ;
        if ( name.equalsIgnoreCase(FileUtils.langNTriple) )     return NTRIPLES ;
        if ( name.equalsIgnoreCase(langTurtle) )                return TURTLE ;
        if ( name.equalsIgnoreCase(langNQuads) )                return NQUADS ;
        if ( name.equalsIgnoreCase(langTrig) )                  return TRIG ;
        throw new RiotException("No such language: "+name) ;
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