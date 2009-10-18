/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.lang;

import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.query.Syntax;

/** original code - contribution from Olaf Hartig */


public class ParserRegistry
{
    // the map contains the registered factories hashed by the syntaxes
    Map<Syntax, ParserFactory> factories = new HashMap<Syntax, ParserFactory>() ;
    
    // Singleton
    static ParserRegistry registry = null ;
    static synchronized public ParserRegistry get()
    {
        if ( registry == null )
            init() ;
        return registry;
    }
    
    private ParserRegistry() { }
    
    private static synchronized void init()
    {
        ParserRegistry reg = new ParserRegistry() ;
        
        reg.add(Syntax.syntaxSPARQL_10, 
                     new ParserFactory() {
            public boolean accept( Syntax syntax ) { return Syntax.syntaxSPARQL_10.equals(syntax) ; } 
            public Parser create( Syntax syntax ) { return new ParserSPARQL10() ; } }) ;
        
        reg.add(Syntax.syntaxSPARQL_11, 
                new ParserFactory() {
            public boolean accept( Syntax syntax ) { return Syntax.syntaxSPARQL_11.equals(syntax) ; } 
            public Parser create( Syntax syntax ) { return new ParserSPARQL11() ; } }) ;
   
        reg.add(Syntax.syntaxARQ, 
                     new ParserFactory() {
            public boolean accept(Syntax syntax ) { return Syntax.syntaxARQ.equals(syntax) ; } 
            public Parser create ( Syntax syntax ) { return new ParserARQ() ; } }) ;

        reg.add(Syntax.syntaxRDQL, 
                     new ParserFactory() {
            public boolean accept ( Syntax syntax ) { return Syntax.syntaxRDQL.equals(syntax) ; } 
            public Parser create ( Syntax syntax ) { return new ParserRDQL() ; } }) ;
        
        // Defend against concurrent start up (even if not synchronised).
        // Protects against, not fixes, the problem.
        registry = reg ;
    }
    
    /** Return a suitable factory for the given syntax
     *
     * @param syntax the syntax to be processed
     * @return a parser factory or null if none accept the request
     */
    
    public static ParserFactory findFactory(Syntax syntax)
    { return get().getFactory(syntax) ; }
    
    /** Return a suitable parser for the given syntax
     *
     * @param syntax the syntax to be processed
     * @return a parser or null if none accept the request
     */
    
    public static Parser parser(Syntax syntax)
    { return get().createParser(syntax) ; }
    
    /** Return a suitable parser factory for the given syntax
     *
     * @param syntax the syntax to be processed
     * @return a parser factory or null if none accept the request
     */
    
    public ParserFactory getFactory(Syntax syntax)
    { return factories.get(syntax) ; }
    
    /** Return a suitable parser for the given syntax
     *
     * @param syntax the syntax to be processed
     * @return a parser or null if none accept the request
     */
    
    public Parser createParser(Syntax syntax)
    {
        ParserFactory f = getFactory(syntax) ;
        return ( f != null ) ? f.create(syntax) : null ;
    }
    
    /** Register the given parser factory for the specified syntax.
     *  If another factory is registered for the syntax it is replaced by the
     *  given one.
     */
    public static void addFactory(Syntax syntax, ParserFactory f)
    { get().add(syntax, f) ; }
    
    /** Register the given parser factory for the specified syntax.
     *  If another factory is registered for the syntax it is replaced by the
     *  given one.
     */
    public void add(Syntax syntax, ParserFactory f)
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
