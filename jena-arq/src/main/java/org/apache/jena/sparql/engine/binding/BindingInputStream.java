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

package org.apache.jena.sparql.engine.binding;

import static org.apache.jena.riot.tokens.TokenType.DOT ;
import static org.apache.jena.riot.tokens.TokenType.IRI ;
import static org.apache.jena.riot.tokens.TokenType.PREFIXED_NAME ;

import java.io.InputStream ;
import java.util.ArrayList ;
import java.util.Collections ;
import java.util.Iterator ;
import java.util.List ;

import org.apache.jena.atlas.iterator.IteratorSlotted ;
import org.apache.jena.atlas.lib.Closeable ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.riot.lang.LabelToNode ;
import org.apache.jena.riot.lang.LangEngine ;
import org.apache.jena.riot.out.NodeFmtLib ;
import org.apache.jena.riot.system.* ;
import org.apache.jena.riot.tokens.*;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.graph.NodeConst ;

/** Language for reading in a stream of bindings.
 * See <a href="https://cwiki.apache.org/confluence/display/JENA/BindingIO">BindingIO</a>
 *
 * <p>Summary:</p>
 * <ul>
 * <li>Directives:
 *   <ul>
 *     <li>VARS - list of variables.</li>
 *     <li>PREFIX</li>
 *   </ul>
 *  </li>
 * <li>Lines of RDF terms (Turtle, no triple-quoted strings)</li>
 * <li>Items on line align with last VARS declaration</li>
 * <li>* for "same as last row"</li>
 * <li>- for "undef"</li>
 * </ul>
 */
public class BindingInputStream extends LangEngine implements Iterator<Binding>, Closeable
{
    // In effect, multiple Inheritance.
    // We implementation-inherit from LangEngine(no public methods)
    // and also IteratorTuples (redirecting calls to be object)
    private final IteratorTuples iter ;

    public BindingInputStream(InputStream in)
    {
        this(TokenizerText.create().source(in).build()) ;
    }

    public BindingInputStream(Tokenizer tokenizer)
    {
        this(tokenizer,  profile()) ;
    }

    static ParserProfile profile()
    {
        // Don't do anything with IRIs or blank nodes.
        ErrorHandler handler = ErrorHandlerFactory.getDefaultErrorHandler() ;
        FactoryRDF factory = RiotLib.factoryRDF(LabelToNode.createUseLabelAsGiven()) ;
        ParserProfile profile = RiotLib.createParserProfile(factory, handler, false);
        return profile ;
    }

    /** Create an RDF Tuples parser.
     *  No need to pass in a buffered InputStream; the code
     *  will do its own buffering.
     */

    private BindingInputStream(Tokenizer tokenizer, ParserProfile profile)
    {
        super(tokenizer, profile, profile.getErrorHandler()) ;
        iter = new IteratorTuples() ;
    }

    @Override
    public boolean hasNext()
    {
        return iter.hasNext() ;
    }

    @Override
    public Binding next()
    {
        return iter.next() ;
    }

    @Override
    public void remove()
    { iter.remove() ; }

    public List<Var> vars()
    { return Collections.unmodifiableList(iter.vars) ; }

    class IteratorTuples extends IteratorSlotted<Binding>
    {
        private Binding lastLine ;
        List<Var> vars = new ArrayList<>() ;

        // Process any directive immediately.
        public IteratorTuples()
        {
            directives() ;
        }

        private void directives()
        {
            while ( lookingAt(TokenType.KEYWORD) )
            {
                Token t = peekToken() ;
                if ( t.getImage().equalsIgnoreCase("VARS") )
                {
                    nextToken();
                    directiveVars() ;
                    continue ;
                }
                if ( t.getImage().equalsIgnoreCase("PREFIX") )
                {
                    nextToken();
                    directivePrefix() ;
                    continue ;
                }
                // Not a directive.
                break;
            }
        }

        protected final static String  KW_TRUE        = "true" ;
        protected final static String  KW_FALSE       = "false" ;

        @Override
        protected Binding moveToNext()
        {
            directives() ;

            BindingMap binding = BindingFactory.create() ;

            int i = 0 ;

            while( ! lookingAt(TokenType.DOT) )
            {
                if ( i >= vars.size() )
                    exception(peekToken(), "Too many items in a line.  Expected "+vars.size()) ;

                Var v = vars.get(i) ;

                Token token = nextToken() ;
                if ( ! token.hasType(TokenType.MINUS ) )
                {
                    Node n ;
                    // One case; VARS line then *
                    if ( token.hasType(TokenType.STAR ) )
                        n = lastLine.get(v) ;
                    else if ( token.hasType(TokenType.BNODE) )
                        n = NodeFactory.createBlankNode(NodeFmtLib.decodeBNodeLabel(token.getImage())) ;
                    else if ( token.hasType(TokenType.KEYWORD) ) {
                        // Keywords values.
                        String lex = token.getImage();
                        if ( lex.equals(KW_TRUE) )
                            n = NodeConst.nodeTrue ;
                        else if ( lex.equals(KW_FALSE) )
                            n = NodeConst.nodeFalse ;
                        else {
                            exception(token, "Keyword out of place: "+lex);
                            n = null;
                        }
                    } else {
                        n = profile.create(null, token) ;
                    }
                    binding.add(v, n) ;
                }
                i++ ;
            }
            if ( eof() )
                exception(peekToken(), "Line does not end with a DOT") ;

            Token dot = nextToken() ;

            if ( i != vars.size() )
            {
                Var v = vars.get(vars.size()-1) ;
                exception(dot, "Too many items in a line.  Expected "+vars.size()) ;
            }
            lastLine = binding ;
            return binding ;
        }

        @Override
        protected boolean hasMore()
        {
            return moreTokens() ;
        }

        private void directiveVars()
        {
            vars.clear() ;
            while (! eof() && ! lookingAt(DOT) )
            {
                Token t = nextToken() ;
                if ( ! t.hasType(TokenType.VAR) )
                    exception(t, "VARS requires a list of variables (found '"+t+"')") ;
                Var v = Var.alloc(t.getImage()) ;
                vars.add(v) ;
            }
            nextToken() ;   // DOT
        }

        private void directivePrefix()
        {
            if ( ! lookingAt(PREFIXED_NAME) )
                exception(peekToken(), "PREFIX requires a prefix (found '"+peekToken()+"')") ;
            if ( peekToken().getImage2().length() != 0 )
                exception(peekToken(), "PREFIX requires a prefix and no suffix (found '"+peekToken()+"')") ;
            String prefix = peekToken().getImage() ;
            nextToken() ;
            if ( ! lookingAt(IRI) )
                exception(peekToken(), "@prefix requires an IRI (found '"+peekToken()+"')") ;
            String iriStr = peekToken().getImage() ;
            String iri = profile.resolveIRI(iriStr, currLine, currCol) ;
            profile.getPrefixMap().add(prefix, iri) ;
            nextToken() ;
            expect("PREFIX directive not terminated by a dot", DOT) ;
        }
    }

    @Override
    public void close()     { super.tokens.close() ; }
}
