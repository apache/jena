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

package org.apache.jena.riot.system;

import static org.apache.jena.riot.RDFLanguages.NQUADS ;
import static org.apache.jena.riot.RDFLanguages.NTRIPLES ;
import static org.apache.jena.riot.RDFLanguages.RDFJSON ;
import static org.apache.jena.riot.RDFLanguages.sameLang ;
import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFLanguages ;
import org.apache.jena.riot.tokens.Token ;
import org.apache.jena.riot.tokens.Tokenizer ;
import org.apache.jena.riot.tokens.TokenizerFactory ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.rdf.model.AnonId ;
import com.hp.hpl.jena.sparql.ARQConstants ;

/** Misc RIOT code */
public class RiotLib
{
    final static String bNodeLabelStart = "_:" ;
    final static boolean skolomizedBNodes = ARQ.isTrue(ARQ.constantBNodeLabels) ;
    
    /** Implement <_:....> as a 2bNode IRI"
     * that is, use the given label as the BNode internal label.
     * Use with care.
     */
    public static Node createIRIorBNode(String iri)
    {
        // Is it a bNode label? i.e. <_:xyz>
        if ( isBNodeIRI(iri) )
        {
            String s = iri.substring(bNodeLabelStart.length()) ;
            Node n = Node.createAnon(new AnonId(s)) ;
            return n ;
        }
        return Node.createURI(iri) ;
    }

    /** Test whether  */
    public static boolean isBNodeIRI(String iri)
    {
        return skolomizedBNodes && iri.startsWith(bNodeLabelStart) ;
    }
    
    static ParserProfile profile = profile(RDFLanguages.TURTLE, null, null) ;
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
        return profile(lang, baseIRI, ErrorHandlerFactory.getDefaultErrorHandler()) ;
    }

    public static ParserProfile profile(Lang lang, String baseIRI, ErrorHandler handler)
    {
        if ( sameLang(NTRIPLES, lang) || sameLang(NQUADS, lang) )
            return profile(baseIRI, false, false, handler) ;
        if ( sameLang(RDFJSON, lang) )
            return profile(baseIRI, false, true, handler) ;
        return profile(baseIRI, true, true, handler) ;
    }

    public static ParserProfile profile(String baseIRI, boolean resolveIRIs, boolean checking, ErrorHandler handler)
    {
        Prologue prologue ;
        if ( resolveIRIs )
            prologue = new Prologue(new PrefixMapStd(), IRIResolver.create(baseIRI)) ;
        else
            prologue = new Prologue(new PrefixMapStd(), IRIResolver.createNoResolve()) ;
    
        if ( checking )
            return new ParserProfileChecker(prologue, handler) ;
        else
            return new ParserProfileBase(prologue, handler) ;
    }
}
