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

package org.apache.jena.tdb1.store.nodetable;

import static org.apache.jena.riot.tokens.TokenType.EOF;

import org.apache.jena.atlas.AtlasException;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.RiotParseException;
import org.apache.jena.riot.out.NodeFmtLib;
import org.apache.jena.riot.tokens.Token ;
import org.apache.jena.riot.tokens.TokenType ;
import org.apache.jena.riot.tokens.Tokenizer ;
import org.apache.jena.riot.tokens.TokenizerText;
import org.apache.jena.tdb1.TDB1Exception;

/**
 * This decode is specific to TDB1. With {@link NodecSSE}, RDF-star triple terms are
 * encoded with {@link NodeFmtLib#str(Node)}. The format for encodes blank node
 * labels (e.g. "-" character and adds a leading "B") using
 * {@link NodeFmtLib#encodeBNodeLabel}.
 * <p>
 * We need to reverse that process to recover the internal blank node label using
 * {@link NodeFmtLib#decodeBNodeLabel}. This code exists to enable the exact
 * requirements details of TDB1 to be satisfied.
 * <p>
 * This format is "on-disk" so it is difficult to change and should be coordinated
 * with any more important major version data reload release.
 * <p>
 * For blank nodes not in triple terms,
 * {@link NodecSSE#encode}/{@link NodecSSE#decode} handles it directly.
 * <p>
 * TDB2 uses a different binary encoding scheme.
 */

class DecoderSSE
{
    private static final Token tokenEOF = new Token(EOF);

    protected static Node parseNode(String str) {
        Tokenizer tokens = TokenizerText.fromString(str);
        return parseNode(tokens);
    }

    protected static Node parseNode(Tokenizer tokens) {
        Token token = nextToken(tokens);
        if ( token.hasType(TokenType.LT2) )
            return parseTripleTerm(tokens);
        return tokenAsNode(token);
    }

    // After "<<" (LT2)
    protected static Node parseTripleTerm(Tokenizer tokens) {
        Triple t = parseTriple(tokens);
        Token x = nextToken(tokens);
        if ( x.getType() != TokenType.GT2 )
            exception(x, "Triple term not terminated by >>: %s");
        return NodeFactory.createTripleNode(t);
    }

    // Parse three terms and produce a triple.
    // Does not consume the terminator (GT2).
    // Already seen and consumed << (LT2)
    protected static Triple parseTriple(Tokenizer tokens) {
        Token sToken = nextToken(tokens);
        if ( sToken.isEOF() )
            exception(sToken, "Premature end of file: %s");
        Node s = sToken.hasType(TokenType.LT2) ? parseTripleTerm(tokens) : tokenAsNode(sToken);

        Token pToken = nextToken(tokens);
        if ( pToken.isEOF() )
            exception(pToken, "Premature end of file: %s");
        Node p = tokenAsNode(pToken);

        Token oToken = nextToken(tokens);
        if ( oToken.isEOF() )
            exception(oToken, "Premature end of file: %s");
        Node o = oToken.hasType(TokenType.LT2) ? parseTripleTerm(tokens) : tokenAsNode(oToken);

        return Triple.create(s, p, o);
    }

    private static void exception(Token token, String string) {
        String msg = string;
        if ( token != null )
            msg = String.format(string, token);
        throw new TDB1Exception(msg);
    }

    private static Node tokenAsNode(Token token) {
        if ( token.isBNode() ) {
            // Cope with NodeFmtLib.written blank nodes that have a leading "_:B"
            String id = token.getImage();
            id = NodeFmtLib.decodeBNodeLabel(id);
            return NodeFactory.createBlankNode(id);
        }
        return token.asNode();
    }

    // ---- Managing tokens.
    private static final Token nextToken(Tokenizer tokens) {
        if ( tokens.eof() )
            return tokenEOF;

        // Tokenizer errors appear here!
        try {
            Token t = tokens.next();
            return t;
        }
        catch (RiotParseException ex) {
            exception(null, ex.getMessage());
            throw ex;
        }
        catch (AtlasException ex) {
            exception(null, ex.getMessage());
            throw ex;
        }
    }

    // Other token operations, in case they are needed.

//    protected static Token peekToken(Tokenizer tokens) {
//        // Avoid repeating.
//        if ( eof(tokens) )
//            return tokenEOF;
//        return tokens.peek();
//    }
//
//    // Set when we get to EOF to record line/col of the EOF.
//
//    private static boolean eof(Tokenizer tokens) {
//        return tokens.eof();
//    }
//
//
//    private static void raiseException(Exception ex) {
//        exception(null, ex.getMessage());
//    }
//
//    protected final void expectOrEOF(String msg, TokenType tokenType) {
//        if ( eof() )
//            return;
//        expect(msg, tokenType);
//    }
//
//    private void expect(String msg, TokenType tokenType) {}

//    protected final void skipIf(TokenType ttype) {
//        if ( lookingAt(ttype) )
//            nextToken();
//    }
}
