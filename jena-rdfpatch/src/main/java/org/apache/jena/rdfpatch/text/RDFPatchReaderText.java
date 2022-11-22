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

package org.apache.jena.rdfpatch.text;

import static org.apache.jena.rdfpatch.changes.PatchCodes.*;
import static org.apache.jena.riot.tokens.TokenType.DOT;
import static org.apache.jena.riot.tokens.TokenType.GT2;
import static org.apache.jena.riot.tokens.TokenType.LT2;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdfpatch.PatchException;
import org.apache.jena.rdfpatch.PatchHeader;
import org.apache.jena.rdfpatch.PatchProcessor;
import org.apache.jena.rdfpatch.RDFChanges;
import org.apache.jena.rdfpatch.changes.PatchCodes;
import org.apache.jena.riot.SysRIOT;
import org.apache.jena.riot.system.ErrorHandler;
import org.apache.jena.riot.system.ErrorHandlerFactory;
import org.apache.jena.riot.system.RiotLib;
import org.apache.jena.riot.tokens.Token;
import org.apache.jena.riot.tokens.TokenType;
import org.apache.jena.riot.tokens.Tokenizer;
import org.apache.jena.riot.tokens.TokenizerText;

/** RDF Patch reader for text format. */
public class RDFPatchReaderText implements PatchProcessor {
    private final Tokenizer tokenizer;

    // Return true on end of transaction.
    private static void read(Tokenizer tokenizer, RDFChanges changes) {
        while( tokenizer.hasNext() ) {
            apply1(tokenizer, changes);
        }
    }

    public RDFPatchReaderText(InputStream input) {
        this(input, ErrorHandlerFactory.errorHandlerExceptionOnError());
    }

    public RDFPatchReaderText(InputStream input, ErrorHandler errorHandler) {
        tokenizer = TokenizerText.create().source(input).errorHandler(errorHandler).build();
    }

    @Override
    public void apply(RDFChanges processor) {
        read(tokenizer, processor);
    }

    /**
     * Execute one tuple, skipping blanks and comments.
     * Return true if there is the possibility of more.
     */
    private static boolean apply1(Tokenizer input, RDFChanges sink) {
        boolean oneTransaction = true;
        long lineNumber = 0;
        while(input.hasNext()) {
            try {
                lineNumber++;
                boolean b = doOneLine(input, sink);
                if ( oneTransaction && b )
                    return true;
            } catch (Exception ex) {
                sink.txnAbort();
                throw ex;
            }
        }
        return false;
    }

    // Return true for "end transaction".
    private static boolean doOneLine(Tokenizer tokenizer, RDFChanges sink) {
        if ( !tokenizer.hasNext() )
            return false;
        Token tokCode = tokenizer.next();
        if ( tokCode.hasType(DOT) )
            throw exception(tokCode, "Empty line");
        if ( ! tokCode.isWord() )
            throw exception(tokCode, "Expected keyword at start of patch record");

        String code = tokCode.getImage();
        switch (code) {
            case HEADER: {
                readHeaderLine(tokenizer, (f,v)->sink.header(f, v));
                return false;
            }

            case ADD_DATA: {
                Node s = nextNode(tokenizer);
                Node p = nextNode(tokenizer);
                Node o = nextNode(tokenizer);
                Node g = nextNodeMaybe(tokenizer);
                skip(tokenizer, DOT);
                sink.add(g, s, p, o);
                return false;
            }
            case DEL_DATA: {
                Node s = nextNode(tokenizer);
                Node p = nextNode(tokenizer);
                Node o = nextNode(tokenizer);
                Node g = nextNodeMaybe(tokenizer);
                skip(tokenizer, DOT);
                sink.delete(g, s, p, o);
                return false;
            }
            case ADD_PREFIX: {
                Token tokPrefix = nextToken(tokenizer);
                if ( tokPrefix == null )
                    throw exception(tokenizer, "Prefix add tuple too short");
                String prefix = tokPrefix.asString();
                if ( prefix == null )
                    throw exception(tokPrefix, "Prefix is not a string: %s", tokPrefix);
                String uriStr;
                Token tokURI = nextToken(tokenizer);
                if ( tokURI.isIRI() )
                    uriStr = tokURI.getImage();
                else if ( tokURI.isString() )
                    uriStr = tokURI.asString();
                else
                    throw exception(tokURI, "Prefix error: URI slot is not a URI nor a string");
                Node gn = nextNodeMaybe(tokenizer);
                skip(tokenizer, DOT);
                sink.addPrefix(gn, prefix, uriStr);
                return false;
            }
            case DEL_PREFIX: {
                Token tokPrefix = nextToken(tokenizer);
                if ( tokPrefix == null )
                    throw exception(tokenizer, "Prefix delete tuple too short");
                String prefix = tokPrefix.asString();
                if ( prefix == null )
                    throw exception(tokPrefix, "Prefix is not a string: %s", tokPrefix);
                Node gn = nextNodeMaybe(tokenizer);
                skip(tokenizer, DOT);
                sink.deletePrefix(gn, prefix);
                return false;
            }
            case TXN_BEGIN:
                // Alternative name:
            case "TB": {
                skip(tokenizer, DOT);
                sink.txnBegin();
                return false;
            }
            case TXN_COMMIT: {
                skip(tokenizer, DOT);
                sink.txnCommit();
                return true;
            }
            case TXN_ABORT: {
                skip(tokenizer, DOT);
                sink.txnAbort();
                return true;
            }
            case SEGMENT: {
                skip(tokenizer, DOT);
                sink.segment();
                return false;
            }
            default:  {
                throw exception(tokenizer, "Code '%s' not recognized", code);
            }
        }
    }

    private final static String bNodeLabelStart = "_:";
    private static Node tokenToNode(Token token) {
        if ( token.isIRI() )
            // URI or <_:...>
            return RiotLib.createIRIorBNode(token.getImage());
        if ( token.isBNode() ) {
            // Blank node as _:...
            String label = token.getImage().substring(bNodeLabelStart.length());
            return NodeFactory.createBlankNode(label);
        }
        Node node = token.asNode();
        if ( node == null )
            throw exception(token, "Expect a Node, got %s",token);
        return node;
    }

    /** Read patch header. */
    public static PatchHeader readerHeader(InputStream input) {
        Tokenizer tokenizer = TokenizerText.create().source(input).build();
        Map<String, Node> header = new LinkedHashMap<>();
        int lineNumber = 0;
        while(tokenizer.hasNext()) {
          Token tokCode = tokenizer.next();
          if ( tokCode.hasType(DOT) )
              throw exception(tokCode, "Empty header line");
          if ( ! tokCode.isWord() )
              throw exception(tokCode, "Expected keyword at start of patch header");
          String code = tokCode.getImage();
          lineNumber ++;
          if ( ! code.equals(PatchCodes.HEADER) )
              break;
          readHeaderLine(tokenizer, (f,n)->header.put(f, n));
      }
      return new PatchHeader(header);
    }

    /** Known-to-be-header line */
    private static void readHeaderLine(Tokenizer tokenizer, BiConsumer<String, Node> action) {
        Token token2 = nextToken(tokenizer);
        if ( ! token2.isWord() && ! token2.isString() )
            throw exception(tokenizer, "Header does not have a key that is a word: "+token2);
        String field = token2.getImage();
        Node v = nextNode(tokenizer);
        skip(tokenizer, DOT);
        action.accept(field, v);
    }

    private static void skip(Tokenizer tokenizer, TokenType tokenType ) {
        Token tok = tokenizer.next();
        if ( ! tok.hasType(tokenType) )
            throw exception(tok, "Expected token type: "+tokenType+": got "+tok);
    }

    private static Node nextNodeMaybe(Tokenizer tokenizer) {
        Token tok = tokenizer.peek();
        if ( tok.hasType(DOT) )
            return null;
        if ( tok.isEOF() )
            throw exception(tokenizer, "Input truncated: no DOT seen on last line");
        return tokenToNode(tokenizer.next());
    }

    // Next token, required, must not be EOF or DOT.
    private static Token nextToken(Tokenizer tokenizer) {
        if ( ! tokenizer.hasNext() )
            throw exception(tokenizer, "Input truncated");
        Token tok = tokenizer.next();
        if ( tok.hasType(DOT) )
            throw exception(tok, "Input truncated by DOT: line too short");
        if ( tok.isEOF() )
            throw exception(tok, "Input truncated: no DOT seen on last line");
        return tok;
    }

    private static Node nextNode(Tokenizer tokenizer) {
        Token tok = nextToken(tokenizer);
        if ( tok.hasType(LT2) ) {
            Node s = nextNode(tokenizer);
            Node p = nextNode(tokenizer);
            Node o = nextNode(tokenizer);
            Token tok2 = nextToken(tokenizer);
            if ( ! tok2.hasType(GT2) )
                exception(tok2, "Expected token type: "+GT2+": got "+tok2);
            return NodeFactory.createTripleNode(s, p, o);
        }
        return tokenToNode(tok);
    }

    private static PatchException exception(Tokenizer tokenizer, String fmt, Object... args) {
        String msg = String.format(fmt, args);
        if ( tokenizer != null )
            msg = SysRIOT.fmtMessage(msg, tokenizer.getLine(), tokenizer.getColumn());
        return new PatchException(msg);
    }

    private static PatchException exception(Token token, String fmt, Object... args) {
        String msg = String.format(fmt, args);
        if ( token != null )
            msg = SysRIOT.fmtMessage(msg, token.getLine(), token.getColumn());
        return new PatchException(msg);
   }
}
