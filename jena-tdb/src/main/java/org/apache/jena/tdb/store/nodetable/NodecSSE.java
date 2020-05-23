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

package org.apache.jena.tdb.store.nodetable;

import java.nio.ByteBuffer;

import org.apache.jena.atlas.io.BlockUTF8;
import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.atlas.logging.FmtLog;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Node_Triple;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.out.NodeFmtLib;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapNull;
import org.apache.jena.riot.tokens.Token;
import org.apache.jena.riot.tokens.Tokenizer;
import org.apache.jena.riot.tokens.TokenizerFactory;
import org.apache.jena.riot.web.LangTag;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.util.NodeUtils;
import org.apache.jena.tdb.TDB;
import org.apache.jena.tdb.TDBException;

/** Simple encoder/decoder for nodes that uses Turtle term string encoding. */

public class NodecSSE implements Nodec
{
    // Characters in IRIs that are illegal and cause SSE problems, but we wish to keep.
    private final static char MarkerChar = '_';
    private final static char[] invalidIRIChars = { MarkerChar , ' ' }; 
    
    public NodecSSE() {}
    
    @Override
    public int maxSize(Node node)
    {
        return maxLength(node);
    }

    private static final PrefixMap pmap0 = PrefixMapNull.empty;
    private static final boolean onlySafeBNodeLabels = false;
    @Override
    public int encode(Node node, ByteBuffer bb, PrefixMapping pmap)
    {
        if ( ! node.isConcrete() )
            FmtLog.warn(TDB.logInfo,"Attempt to encode non-concrete node: "+node);
            

        
        String str = null;

        if ( node.isURI() ) 
        {
            // Pesky spaces etc
            String x = StrUtils.encodeHex(node.getURI(), MarkerChar, invalidIRIChars);
            if ( x != node.getURI() )
                node = NodeFactory.createURI(x); 
        }
        
        if ( node.isLiteral() && NodeUtils.isLangString(node) )
        {
            // Check syntactically valid.
            String lang = node.getLiteralLanguage();
            if ( ! LangTag.check(lang) )
                throw new TDBException("bad language tag: "+node);
        }
        
        if ( node.isBlank() && ! onlySafeBNodeLabels ) {
            // Special case.
            str = "_:"+node.getBlankNodeLabel();
        }
        
        if ( node.isNodeTriple() ) {
            str = NodeFmtLib.str(node);
        }
        
        // Catch-all: Node->String
        if ( str == null )
            str = NodeFmtLib.str(node);
        // String -> bytes;
        BlockUTF8.fromChars(str, bb);
        bb.flip();
        return bb.limit();
    }
    
    @Override
    public Node decode(ByteBuffer bb, PrefixMapping pmap) {
        // Ideally, this would be straight from the byte buffer.
        // But currently we go bytes -> string -> node 

        // Byte -> String
        String str = BlockUTF8.toString(bb);
        // String -> Node
        
        // Easy cases.
        if ( str.startsWith("_:") )   
        {
            // Must be done this way.
            // In particular, bnode labels can contain ":" from Jena
            // TokenizerText does not recognize these.
            str = str.substring(2);
            return NodeFactory.createBlankNode(str);
        }

        if ( str.startsWith("<<") ) {
            // Complex - not a single token so use full machinery.
            return SSE.parseNode(str);
        }

        if ( str.startsWith("<") ) 
        {
            // Do directly.
            // (is it quicker?)
            str = str.substring(1,str.length()-1);
            str = StrUtils.unescapeString(str);
            str = StrUtils.decodeHex(str, MarkerChar);
            return NodeFactory.createURI(str);
        }

        Tokenizer tokenizer = TokenizerFactory.makeTokenizerString(str);
        if ( ! tokenizer.hasNext() )
            throw new TDBException("Failed to tokenize: "+str);
        Token t = tokenizer.next();

        try {
            Node n = t.asNode();
            if ( n == null ) throw new TDBException("Not a node: "+str);
            return n;
        } catch (RiotException ex)
        {
            throw new TDBException("Bad string for node: "+str);
        }
    }

    // Over-estimate the length of the encoding.
    private static int maxLength(Node node)
    {
        if ( node.isBlank() )
            // "_:"
            return 2+maxLength(node.getBlankNodeLabel());    
        if ( node.isURI() )
            // "<>"
            return 2+maxLength(node.getURI());
        if ( node.isLiteral() )
        {
            int len = 2+maxLength(node.getLiteralLexicalForm());
            if ( NodeUtils.isLangString(node) )
                // Space for @ (language tag is ASCII)
                len = len + 3 + node.getLiteralLanguage().length();
            else if ( ! NodeUtils.isSimpleString(node) )
                // The quotes and also space for ^^<>
                len = len + 4 + maxLength(node.getLiteralDatatypeURI());
            return len;
        }
        if ( node.isVariable() )
            // "?"
            return 1+maxLength(node.getName());
        if ( node.isNodeTriple() ) {
            Triple t = Node_Triple.triple(node);
            // Leading an trailing <<>>, 4 spaces
            return (2+4+2)+maxLength(t.getSubject())+maxLength(t.getPredicate())+maxLength(t.getObject()); 
        }
        
        throw new TDBException("Unrecognized node type: "+node);
    }

    private static int maxLength(String string)
    {
        // Very worse case for UTF-8 - and then some.
        // Encoding every character as _XX or bad UTF-8 conversion (3 bytes)
        // Max 3 bytes UTF-8 for up to 10FFFF (NB Java treats above 16bites as surrogate pairs only). 
        return string.length()*3;
    }
    
    // See also StringFile.
//    // URI compression can be effective but literals are more of a problem.  More variety. 
//    public final static boolean compression = false; 
//    private static StringAbbrev abbreviations = new StringAbbrev();
//    static {
//        abbreviations.add(  "rdf",      "<http://www.w3.org/1999/02/22-rdf-syntax-ns#");
//        abbreviations.add(  "rdfs",     "<http://www.w3.org/2000/01/rdf-schema#");
//        abbreviations.add(  "xsd",      "<http://www.w3.org/2001/XMLSchema#");
//
//        // MusicBrainz
//        abbreviations.add(  "mal",      "<http://musicbrainz.org/mm-2.1/album/");
//        abbreviations.add(  "mt",       "<http://musicbrainz.org/mm-2.1/track/");
//        abbreviations.add(  "mar",      "<http://musicbrainz.org/mm-2.1/artist/");
//        abbreviations.add(  "mtr",      "<http://musicbrainz.org/mm-2.1/trmid/");
//        abbreviations.add(  "mc",       "<http://musicbrainz.org/mm-2.1/cdindex/");
//
//        abbreviations.add(  "m21",      "<http://musicbrainz.org/mm/mm-2.1#");
//        abbreviations.add(  "dc",       "<http://purl.org/dc/elements/1.1/");
//        // DBPedia
//        abbreviations.add(  "r",        "<http://dbpedia/resource/");
//        abbreviations.add(  "p",        "<http://dbpedia/property/");
//    }
//    private String compress(String str)
//    {
//        if ( !compression || abbreviations == null ) return str;
//        return abbreviations.abbreviate(str);
//    }
//
//    private String decompress(String x)
//    {
//        if ( !compression || abbreviations == null ) return x;
//        return abbreviations.expand(x);
//    }
}
