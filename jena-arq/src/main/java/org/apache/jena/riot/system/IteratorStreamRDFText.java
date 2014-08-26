/**
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

import static org.apache.jena.riot.tokens.TokenType.DIRECTIVE ;
import static org.apache.jena.riot.tokens.TokenType.DOT ;
import static org.apache.jena.riot.tokens.TokenType.IRI ;
import static org.apache.jena.riot.tokens.TokenType.PREFIXED_NAME ;

import java.io.InputStream ;
import java.util.* ;

import org.apache.jena.atlas.lib.Closeable ;
import org.apache.jena.atlas.lib.NotImplemented ;
import org.apache.jena.riot.RiotException ;
import org.apache.jena.riot.system.RiotLib ;
import org.apache.jena.riot.system.StreamRowRDF ;
import org.apache.jena.riot.tokens.Token ;
import org.apache.jena.riot.tokens.Tokenizer ;
import org.apache.jena.riot.tokens.TokenizerFactory ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.graph.Node ;

/** Testing/development convenience.
 *  Iterator of StreamRowRDF (always a tuple) for an input stream of tokenized RDT terms.
 */  
public class IteratorStreamRDFText extends IteratorStreamRDF implements Iterator<StreamRowRDF> {
    private final TokenInputStream in ;
    private Node[] previousTuple = null ;

    private /*public*/ IteratorStreamRDFText(InputStream input) {
        Tokenizer t = TokenizerFactory.makeTokenizerUTF8(input) ;
        in = new TokenInputStream(null, t) ;
    }

    @Override
    protected boolean hasMore() {
        return true ;
    }

    @Override
    protected StreamRowRDF moveToNext() {
        if ( ! in.hasNext() ) return null ; 
        List<Token> line = in.next() ;
        StreamRowRDF row = line2row(line) ;
        return row ;
    }

    private StreamRowRDF line2row(List<Token> line) {
        if ( line.size() != 3 && line.size() != 4 )
            throw new RiotException("Input line is not 3 or 4 items long") ; 
        
        Node[] tuple = new Node[line.size()] ;
        int idx = 0 ;
        for ( Token token : line ) {
            Node n = null ;
            if ( ( token.isWord() && token.getImage().equals("R") ) 
                 //|| ( token.isCtlCode() && token.getCntrlCode() == -1 )     // *
                ) {
                if ( previousTuple == null )
                    throw new RiotException("Repeat without previous data row") ; 
                if ( idx >= previousTuple.length)
                    throw new RiotException("Repeat position beyond previous data row") ;
                n = previousTuple[idx] ;
            } else if ( token.isNode() ) { 
                n = asNode(token) ;
            }
            if ( n == null )
                throw new RiotException("Unrecognized token : "+token ) ;
            tuple[idx] = n ;
            idx++ ;
        }
        previousTuple = tuple ;

        // Needs rethink.
        throw new NotImplemented() ;
        
//        if ( line.size() == 3 )
//            return new StreamRowRDFBase(Triple.create(tuple[0], tuple[1], tuple[2])) ;  
//        else 
//            return new StreamRowRDFBase(Quad.create(tuple[0], tuple[1], tuple[2], tuple[3])) ;
//        return new StreamRowRDFBase(Tuple.create(tuple)) ;
    }

    private static Node asNode(Token t) {
        // <_:...> bnodes.
        if ( t.isIRI() )
            return RiotLib.createIRIorBNode(t.getImage()) ;
        return t.asNode() ;
    }
    
    /** Tokenizer that sorts out prefixes and groups into sequences of token */
    private static class TokenInputStream implements Iterator<List<Token>>, Iterable<List<Token>>, Closeable {
        private static Logger       log      = LoggerFactory.getLogger(TokenInputStream.class) ;
        private boolean             finished = false ;
        private final Tokenizer     tokens ;
        private List<Token>         list ;
        private Map<String, String> map      = new HashMap<String, String>() ;
        private String              label ;

        public TokenInputStream(String label, Tokenizer tokens) {
            this.tokens = tokens ;
            this.label = label ;
        }

        @Override
        public boolean hasNext() {
            if ( finished )
                return false ;

            if ( list != null ) // Already got the reply.
                return true ;

            try {
                if ( !tokens.hasNext() ) {
                    finished = true ;
                    return false ;
                }
                list = buildOneLine() ;
                if ( false && log.isDebugEnabled() )
                    log.debug("Tokens: " + list) ;
                if ( list == null )
                    finished = true ;
                return list != null ;
            } catch (Exception ex) {
                finished = true ;
                return false ;
            }
        }

        private List<Token> buildOneLine() {
            List<Token> tuple = new ArrayList<Token>() ;
            boolean isDirective = false ;
            for (; tokens.hasNext();) {
                Token token = tokens.next() ;

                if ( token.hasType(DIRECTIVE) )
                    isDirective = true ;

                if ( token.hasType(DOT) ) {
                    if ( tuple.size() > 0 && tuple.get(0).hasType(DIRECTIVE) ) {
                        directive(tuple) ;
                        tuple.clear() ;
                        isDirective = false ;
                        // Start again.
                        continue ;
                    }
                    return tuple ;
                }

                // Fixup prefix names.
                if ( !isDirective && token.hasType(PREFIXED_NAME) ) {
                    String ns = map.get(token.getImage()) ;
                    String iri ;
                    if ( ns == null ) {
                        log.warn("Can't resolve '" + token.toString(false) + "'", ns) ;
                        iri = "unresolved:" + token.getImage() + ":" + token.getImage2() ;
                    } else
                        iri = ns + token.getImage2() ;
                    token.setType(IRI) ;
                    token.setImage(iri) ;
                    token.setImage2(null) ;
                }

                tuple.add(token) ;
            }

            // No final DOT
            return tuple ;
        }

        private void directive(List<Token> tuple) {
            if ( tuple.size() != 3 )
                throw new RiotException("Bad directive: " + tuple) ;

            String x = tuple.get(0).getImage() ;

            if ( x.equals("prefix") ) {
                // Raw - unresolved prefix name.
                if ( !tuple.get(1).hasType(PREFIXED_NAME) )
                    throw new RiotException("@prefix requires a prefix (found '" + tuple.get(1) + "')") ;
                if ( tuple.get(1).getImage2().length() != 0 )
                    throw new RiotException("@prefix requires a prefix and no suffix (found '" + tuple.get(1) + "')") ;
                String prefix = tuple.get(1).getImage() ;

                if ( !tuple.get(2).hasType(IRI) )
                    throw new RiotException("@prefix requires an IRI (found '" + tuple.get(1) + "')") ;
                String iriStr = tuple.get(2).getImage() ;
                map.put(prefix, iriStr) ;
                return ;
            }
            throw new RiotException("Unregcognized directive: " + x) ;
        }

        @Override
        public List<Token> next() {
            if ( !hasNext() )
                throw new NoSuchElementException() ;
            List<Token> r = list ;
            if ( log.isDebugEnabled() ) {
                if ( label != null )
                    log.debug("<< " + label + ": " + r) ;
                else
                    log.debug("<< " + r.toString()) ;
            }
            list = null ;
            return r ;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException() ;
        }

        @Override
        public Iterator<List<Token>> iterator() {
            return this ;
        }

        @Override
        public void close() {}
    }

}

