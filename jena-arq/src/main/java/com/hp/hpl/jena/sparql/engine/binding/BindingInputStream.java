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

package com.hp.hpl.jena.sparql.engine.binding;

import static org.apache.jena.riot.tokens.TokenType.DOT ;
import static org.apache.jena.riot.tokens.TokenType.IRI ;
import static org.apache.jena.riot.tokens.TokenType.PREFIXED_NAME ;

import java.io.InputStream ;
import java.util.ArrayList ;
import java.util.Collections ;
import java.util.Iterator ;
import java.util.List ;


import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;

import org.apache.jena.atlas.iterator.IteratorSlotted ;
import org.apache.jena.atlas.lib.Closeable ;
import org.apache.jena.iri.IRI ;
import org.apache.jena.riot.lang.LabelToNode ;
import org.apache.jena.riot.lang.LangEngine ;
import org.apache.jena.riot.out.NodeFmtLib ;
import org.apache.jena.riot.system.* ;
import org.apache.jena.riot.tokens.Token ;
import org.apache.jena.riot.tokens.TokenType ;
import org.apache.jena.riot.tokens.Tokenizer ;
import org.apache.jena.riot.tokens.TokenizerFactory ;

import com.hp.hpl.jena.rdf.model.AnonId ;
import com.hp.hpl.jena.sparql.core.Var ;

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
        this(TokenizerFactory.makeTokenizerUTF8(in)) ;
    }
    
    public BindingInputStream(Tokenizer tokenizer)
    {
        this(tokenizer,  profile()) ;
    }
    
    static ParserProfile profile()
    {
        // Don't do anything with IRIs.
        Prologue prologue = new Prologue(PrefixMapFactory.createForInput(), IRIResolver.createNoResolve()) ;
        ErrorHandler handler = ErrorHandlerFactory.getDefaultErrorHandler() ;
        ParserProfile profile = new ParserProfileBase(prologue, handler) ;
        profile.setLabelToNode(LabelToNode.createUseLabelAsGiven()) ;
        // Include safe bNode labels.
        return profile ;
    }
    
    /** Create an RDF Tuples parser.
     *  No need to pass in a buffered InputStream; the code 
     *  will do it's own buffering.
     */
    
    private BindingInputStream(Tokenizer tokenizer, ParserProfile profile)
    {
        super(tokenizer, profile) ;
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
                Token t = nextToken() ;
                if ( t.getImage().equalsIgnoreCase("VARS") )
                {
                    directiveVars() ;
                    continue ;
                }
                if ( t.getImage().equalsIgnoreCase("PREFIX") )
                {
                    directivePrefix() ;
                    continue ;
                }
            }
        }

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
                    if ( token.hasType(TokenType.STAR ) || ( token.isCtlCode() && token.getCntrlCode() == -1 ) )
                        n = lastLine.get(v) ;
                    else if ( token.hasType(TokenType.BNODE) )
                        n = NodeFactory.createAnon(new AnonId(NodeFmtLib.decodeBNodeLabel(token.getImage()))) ;
                    else
                        n = profile.create(null, token) ;
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
            IRI iri = profile.makeIRI(iriStr, currLine, currCol) ;
            profile.getPrologue().getPrefixMap().add(prefix, iri) ;
            nextToken() ;
            expect("PREFIX directive not terminated by a dot", DOT) ;
        }
    }

    @Override
    public void close()     { super.tokens.close() ; }
}
