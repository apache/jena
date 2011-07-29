/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.riot.system;

import org.openjena.atlas.logging.Log ;
import org.openjena.riot.ErrorHandler ;
import org.openjena.riot.ErrorHandlerFactory ;
import org.openjena.riot.Lang ;
import org.openjena.riot.tokens.Token ;
import org.openjena.riot.tokens.Tokenizer ;
import org.openjena.riot.tokens.TokenizerFactory ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.ARQConstants ;

/** Misc RIOT code */
public class RiotLib
{
    static ParserProfile profile = profile(Lang.TURTLE, null, null) ;
    static {
        PrefixMap pmap = profile.getPrologue().getPrefixMap() ;
        pmap.add("rdf",  ARQConstants.rdfPrefix) ;
        pmap.add("rdfs", ARQConstants.rdfsPrefix) ;
        pmap.add("xsd",  ARQConstants.xsdPrefix) ;
        pmap.add("owl" , ARQConstants.owlPrefix) ;
        pmap.add("fn" ,  ARQConstants.fnPrefix) ; 
        pmap.add("op" ,  ARQConstants.fnPrefix) ; 
        pmap.add("ex" ,  "http://example/ns#") ;
        pmap.add("" ,    "http://example/") ;
    }
    
    /** Parse a string to get one Node (the first token in the string) */ 
    public static Node parse(String string)
    {
        Tokenizer tokenizer = TokenizerFactory.makeTokenizerString(string) ;
        if ( ! tokenizer.hasNext() )
            return null ;
        Token t = tokenizer.next();
        Node n = profile.create(null, t) ;
        if ( tokenizer.hasNext() )
            Log.warn(RiotLib.class, "String has more than one token in it: "+string) ;
        return n ;
    }

    public static ParserProfile profile(Lang lang, String baseIRI)
    {
        return profile(lang, baseIRI, ErrorHandlerFactory.errorHandlerStd) ;
    }

    public static ParserProfile profile(Lang lang, String baseIRI, ErrorHandler handler)
    {
        switch (lang)
        {
            case NQUADS :
            case NTRIPLES :
                return profile(baseIRI, false, false, handler) ;
            case N3 :
            case TURTLE :
            case TRIG :
            case RDFXML :
                return profile(baseIRI, true, true, handler) ;
            
        }
        return null ;
    }

    public static ParserProfile profile(String baseIRI, boolean resolveIRIs, boolean checking, ErrorHandler handler)
    {
        Prologue prologue ;
        if ( resolveIRIs )
            prologue = new Prologue(new PrefixMap(), IRIResolver.create(baseIRI)) ;
        else
            prologue = new Prologue(new PrefixMap(), IRIResolver.createNoResolve()) ;
    
        if ( checking )
            return new ParserProfileChecker(prologue, handler) ;
        else
            return new ParserProfileBase(prologue, handler) ;
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