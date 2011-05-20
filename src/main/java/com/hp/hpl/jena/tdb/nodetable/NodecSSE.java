/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.nodetable;

import java.nio.ByteBuffer ;

import org.openjena.atlas.lib.Bytes ;
import org.openjena.atlas.lib.StrUtils ;
import org.openjena.riot.tokens.Tokenizer ;
import org.openjena.riot.tokens.TokenizerFactory ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.rdf.model.AnonId ;
import com.hp.hpl.jena.shared.PrefixMapping ;
import com.hp.hpl.jena.tdb.TDBException ;
import com.hp.hpl.jena.tdb.lib.NodeFmtLib ;
import com.hp.hpl.jena.tdb.lib.StringAbbrev ;

/** Simple encoder/decoder for nodes that uses Turtle term string encoding. */

public class NodecSSE implements Nodec
{
    private static boolean SafeChars = false ;
    // Characters in IRIs that are illegal and cause SSE problems, but we wish to keep.
    final private static char MarkerChar = '_' ;
    final private static char[] invalidIRIChars = { MarkerChar , ' ' } ; 
    
    public NodecSSE() {}
    
    @Override
    public int maxSize(Node node)
    {
        return maxLength(node) ;
    }

    @Override
    public int encode(Node node, ByteBuffer bb, PrefixMapping pmap)
    {
        if ( node.isURI() ) 
        {
            // Pesky spaces etc
            String x = StrUtils.encodeHex(node.getURI(), MarkerChar, invalidIRIChars) ;
            if ( x != node.getURI() )
                node = Node.createURI(x) ; 
        }
        
        // Node->String
        String str = NodeFmtLib.serialize(node) ;
        // String -> bytes
        int x = Bytes.toByteBuffer(str, bb) ;
        bb.position(0) ;        // Around the space used
        bb.limit(x) ;           // The space we have used.
        return x ;
    }

    @Override
    public Node decode(ByteBuffer bb, PrefixMapping pmap)
    {
        // Ideally, this would be straight from the byte buffer.
        // But currently we go bytes -> string -> node 

        // Byte -> String
        String str = Bytes.fromByteBuffer(bb) ;
        // String -> Node
        
        // Easy cases.
        if ( str.startsWith("_:") )   
        {
            // Must be done this way.
            // In particular, bnode labels can contain ":" from Jena
            // TokenizerText does not recognize these.
            str = str.substring(2) ;
            return Node.createAnon(new AnonId(str)) ;
        }

        if ( str.startsWith("<") )
        {
            // Do directly.
            // (is it quicker?)
            str = str.substring(1,str.length()-1) ;
            str = StrUtils.unescapeString(str) ;
            str = StrUtils.decodeHex(str, MarkerChar) ;
            return Node.createURI(str) ;
        }
        // -- Old - expensive.
        // Node n = NodeFactory.parseNode(str) ;
        
        Tokenizer tokenizer = TokenizerFactory.makeTokenizerString(str) ;
        Node n = tokenizer.next().asNode() ;
        if ( n == null )
            throw new TDBException("Not a node: "+str) ;

        // Not a URI or bNode.
//        if ( n.isURI() && n.getURI().indexOf(MarkerChar) >= 0 )
//        {
//            String uri = StrUtils.decode(n.getURI(), '_') ;
//            if ( uri != n.getURI() )
//                n = Node.createURI(uri) ;
//        }
        return n ;
    }

    // Over-estimate the length of the encoding.
    private static int maxLength(Node node)
    {
        if ( node.isBlank() )
            // "_:"
            return 2+maxLength(node.getBlankNodeLabel()) ;    
        if ( node.isURI() )
            // "<>"
            return 2+maxLength(node.getURI()) ;
        if ( node.isLiteral() )
        {
            if ( node.getLiteralDatatypeURI() != null )
                // The quotes and also space for ^^<>
                return 6+maxLength(node.getLiteralLexicalForm())+maxLength(node.getLiteralDatatypeURI()) ;
            else if ( node.getLiteralLanguage() != null )
                // The quotes and also space for @ (language tag is ASCII)
                return 3+maxLength(node.getLiteralLexicalForm())+node.getLiteralLanguage().length() ;
            else
                return 2+maxLength(node.getLiteralLexicalForm()) ;
        }
        if ( node.isVariable() )
            // "?"
            return 1+maxLength(node.getName()) ;
        throw new TDBException("Unrecognized node type: "+node) ;
    }

    private static int maxLength(String string)
    {
        // Very worse case for UTF-8 - and then some.
        // Encoding every character as _XX or bad UTF-8 conversion (3 bytes)
        // Max 3 bytes UTF-8 for up to 10FFFF (NB Java treats above 16bites as surrogate pairs only). 
        return string.length()*3 ;
    }
    
    // URI compression can be effective but literals are more of a problem.  More variety. 
    public final static boolean compression = false ; 
    private static StringAbbrev abbreviations = new StringAbbrev() ;
    static {
        abbreviations.add(  "rdf",      "<http://www.w3.org/1999/02/22-rdf-syntax-ns#") ;
        abbreviations.add(  "rdfs",     "<http://www.w3.org/2000/01/rdf-schema#") ;
        abbreviations.add(  "xsd",      "<http://www.w3.org/2001/XMLSchema#") ;

        // MusicBrainz
        abbreviations.add(  "mal",      "<http://musicbrainz.org/mm-2.1/album/") ;
        abbreviations.add(  "mt",       "<http://musicbrainz.org/mm-2.1/track/") ;
        abbreviations.add(  "mar",      "<http://musicbrainz.org/mm-2.1/artist/") ;
        abbreviations.add(  "mtr",      "<http://musicbrainz.org/mm-2.1/trmid/") ;
        abbreviations.add(  "mc",       "<http://musicbrainz.org/mm-2.1/cdindex/") ;

        abbreviations.add(  "m21",      "<http://musicbrainz.org/mm/mm-2.1#") ;
        abbreviations.add(  "dc",       "<http://purl.org/dc/elements/1.1/") ;
        // DBPedia
        abbreviations.add(  "r",        "<http://dbpedia/resource/") ;
        abbreviations.add(  "p",        "<http://dbpedia/property/") ;
    }
    private String compress(String str)
    {
        if ( !compression || abbreviations == null ) return str ;
        return abbreviations.abbreviate(str) ;
    }

    private String decompress(String x)
    {
        if ( !compression || abbreviations == null ) return x ;
        return abbreviations.expand(x) ;
    }

}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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