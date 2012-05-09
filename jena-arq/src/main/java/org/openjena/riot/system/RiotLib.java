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
            case RDFJSON:
            	return profile(baseIRI, false, true, handler) ;
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
