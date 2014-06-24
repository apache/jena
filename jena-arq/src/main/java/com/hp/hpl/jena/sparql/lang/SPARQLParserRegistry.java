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

package com.hp.hpl.jena.sparql.lang;

import java.util.HashMap ;
import java.util.Map ;

import com.hp.hpl.jena.query.Syntax ;

public class SPARQLParserRegistry
{
    // the map contains the registered factories hashed by the syntaxes
    Map<Syntax, SPARQLParserFactory> factories = new HashMap<>() ;
    
    // Singleton
    static SPARQLParserRegistry registry = null ;
    static synchronized public SPARQLParserRegistry get()
    {
        if ( registry == null )
            init() ;
        return registry;
    }
    
    private SPARQLParserRegistry() { }
    
    private static synchronized void init()
    {
        SPARQLParserRegistry reg = new SPARQLParserRegistry() ;
        
        reg.add(Syntax.syntaxSPARQL_10, 
                new SPARQLParserFactory() {
            @Override
            public boolean accept( Syntax syntax ) { return Syntax.syntaxSPARQL_10.equals(syntax) ; } 
            @Override
            public SPARQLParser create( Syntax syntax ) { return new ParserSPARQL10() ; } }) ;
        
        reg.add(Syntax.syntaxSPARQL_11, 
                new SPARQLParserFactory() {
            @Override
            public boolean accept( Syntax syntax ) { return Syntax.syntaxSPARQL_11.equals(syntax) ; } 
            @Override
            public SPARQLParser create( Syntax syntax ) { return new ParserSPARQL11() ; } }) ;
   
        reg.add(Syntax.syntaxARQ, 
                new SPARQLParserFactory() {
            @Override
            public boolean accept(Syntax syntax ) { return Syntax.syntaxARQ.equals(syntax) ; } 
            @Override
            public SPARQLParser create ( Syntax syntax ) { return new ParserARQ() ; } }) ;

        // Defend against concurrent start up (even if not synchronised).
        // Protects against, not fixes, the problem.
        registry = reg ;
    }
    
    /** Return a suitable factory for the given syntax
     *
     * @param syntax the syntax to be processed
     * @return a parser factory or null if none accept the request
     */
    
    public static SPARQLParserFactory findFactory(Syntax syntax)
    { return get().getFactory(syntax) ; }
    
    /** Return a suitable parser for the given syntax
     *
     * @param syntax the syntax to be processed
     * @return a parser or null if none accept the request
     */
    
    public static SPARQLParser parser(Syntax syntax)
    { return get().createParser(syntax) ; }
    
    /** Return a suitable parser factory for the given syntax
     *
     * @param syntax the syntax to be processed
     * @return a parser factory or null if none accept the request
     */
    
    public SPARQLParserFactory getFactory(Syntax syntax)
    { return factories.get(syntax) ; }
    
    /** Return a suitable parser for the given syntax
     *
     * @param syntax the syntax to be processed
     * @return a parser or null if none accept the request
     */
    
    public SPARQLParser createParser(Syntax syntax)
    {
        SPARQLParserFactory f = getFactory(syntax) ;
        return ( f != null ) ? f.create(syntax) : null ;
    }
    
    /** Register the given parser factory for the specified syntax.
     *  If another factory is registered for the syntax it is replaced by the
     *  given one.
     */
    public static void addFactory(Syntax syntax, SPARQLParserFactory f)
    { get().add(syntax, f) ; }
    
    /** Register the given parser factory for the specified syntax.
     *  If another factory is registered for the syntax it is replaced by the
     *  given one.
     */
    public void add(Syntax syntax, SPARQLParserFactory f)
    {
        if ( ! f.accept(syntax) )
            throw new IllegalArgumentException( "The given parser factory does not accept the specified syntax." );
        factories.put(syntax, f) ;
    }
    
    /** Unregister the parser factory associated with the given syntax */
    public static void removeFactory(Syntax syntax)
    { get().remove(syntax) ; }
    
    /** Unregister the parser factory associated with the given syntax */
    public void remove(Syntax syntax)
    { factories.remove(syntax) ; }
    
    /** Checks whether a parser factory is registered for the given syntax */
    public static boolean containsParserFactory(Syntax syntax)
    { return get().containsFactory(syntax) ; }
    
    /** Checks whether a parser factory is registered for the given syntax */
    public boolean containsFactory(Syntax syntax)
    { return factories.containsKey(syntax) ; }

}
