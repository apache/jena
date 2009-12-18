/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.query;

import com.hp.hpl.jena.n3.IRIResolver;
import com.hp.hpl.jena.sparql.lang.Parser;
import com.hp.hpl.jena.sparql.lang.ParserRegistry;
import com.hp.hpl.jena.sparql.lang.ParserARQ;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.Template;
import com.hp.hpl.jena.util.FileManager;


public class QueryFactory
{

    // ---- static methods for making a query
    
    /** Create a SPARQL query from the given string.
     *
     * @param queryString      The query string
     * @throws QueryException  Thrown when a parse error occurs
     */
    
    static public Query create(String queryString)
    {
        return create(queryString, Syntax.defaultSyntax) ;
    }

    /** Create a query from the given string with the 
     *
     * @param queryString      The query string
     * @param syntax           {@link Syntax}
     * @throws QueryException  Thrown when a parse error occurs
     */
    
    static public Query create(String queryString, Syntax syntax)
    {
        return create(queryString, null, syntax) ;
    }

    /** Create a query from the given string by calling the parser.
     *
     * @param queryString      The query string
     * @param baseURI          Base URI
     * @throws QueryException  Thrown when a parse error occurs
     */
    
    static public Query create(String queryString, String baseURI)
    {
        Query query = new Query() ;
        parse(query, queryString, baseURI, Syntax.defaultSyntax) ;
        return query ;
        
    }
    
    /** Create a query from the given string by calling the parser.
    *
    * @param queryString      The query string
    * @param baseURI          Base URI
    * @param syntax           {@link Syntax}
    * @throws QueryException  Thrown when a parse error occurs
    */
   
   static public Query create(String queryString, String baseURI, Syntax syntax)
   {
       Query query = new Query() ;
       parse(query, queryString, baseURI, syntax) ;
       return query ;
       
   }
   
    /**
     * Make a query - no parsing done  
     */
    static public Query make() { return new Query() ; }

    /**
     * Make a query from another one by deep copy (a clone).
     * The returned query will be .equals to the original.
     * The returned query can be mutated without changing the
     * original (at which point it will stop being .equals)
     * 
     * @param originalQuery  The query to clone.
     *   
     */

    static public Query create(Query originalQuery)
    {
        return originalQuery.cloneQuery() ;
    }
    

    /** Parse a query from the given string by calling the parser.
     *
     * @param query            Existing, uninitialized query
     * @param queryString      The query string
     * @param baseURI          URI for relative URI expansion
     * @param syntaxURI        URI for the syntax
     * @throws QueryException  Thrown when a parse error occurs
     */
    
    static public Query parse(Query query, String queryString, String baseURI, Syntax syntaxURI)
    {
        if ( syntaxURI == null )
            syntaxURI = query.getSyntax() ;
        else
            query.setSyntax(syntaxURI) ;

        Parser parser = Parser.createParser(syntaxURI) ;
        
        if ( parser == null )
            throw new UnsupportedOperationException("Unrecognized syntax for parsing: "+syntaxURI) ;
        
        if ( query.getResolver() == null )
        {
            
            // Sort out the baseURI - if that fails, dump in a dummy one and continue.
            try { baseURI = IRIResolver.chooseBaseURI(baseURI) ; }
            catch (Exception ex)
            { baseURI = "http://localhost/defaultBase#" ; }
    
            query.setResolver(new IRIResolver(baseURI)) ;
        }
        return parser.parse(query, queryString) ;
    }
    
    static boolean knownParserSyntax(Syntax syntaxURI)
    {
        return ParserRegistry.get().containsFactory(syntaxURI) ;
    }


    /**
     * Read a SPARQL query from a file.
     * 
     * @param url
     *            URL (file: or http: or anything a FileManager can handle)
     * @return A new query object
     */
    static public Query read(String url)
    {
        return read(url, null, null, null) ;
    }

    /** Read a SPARQL query from a file.
     * 
     * @param url            URL (file: or http: or anything a FileManager can handle)
     * @param baseURI        BaseURI for the query
     * @return               A new query object 
     */
    static public Query read(String url, String baseURI)
    {
        return read(url, null, baseURI, null) ;
    }

    /** Read a query from a file.
     * 
     * @param url            URL (file: or http: or anything a FileManager can handle)
     * @param langURI        Query syntax
     * @return               A new query object 
     */
    static public Query read(String url, Syntax langURI)
    {
        return read(url, null, null, langURI) ;
    }

    /** Read a query from a file.
     * 
     * @param url            URL (file: or http: or anything a FileManager can handle)
     * @param baseURI        BaseURI for the query
     * @param langURI        Query syntax
     * @return               A new query object 
     */
    static public Query read(String url, String baseURI, Syntax langURI)
    {
        return read(url, null, baseURI, langURI) ;
    }

    /** Read a query from a file.
     * 
     * @param url            URL (file: or http: or anything a FileManager can handle)
     * @param filemanager    Optional filemanager
     * @param baseURI        BaseURI for the query
     * @param langURI        Query syntax
     * @return               A new query object 
     */
    static public Query read(String url, FileManager filemanager, String baseURI, Syntax langURI)
    {
        if ( filemanager == null )
            filemanager = FileManager.get() ;
        String qStr = filemanager.readWholeFileAsUTF8(url) ;
        if ( baseURI == null )
            baseURI = url ;
        if ( langURI == null )
            langURI = Syntax.guessQueryFileSyntax(url) ;
        
        return create(qStr, baseURI, langURI) ;
    }
    
    static public Element createElement(String elementString)
    {
        return ParserARQ.parseElement(elementString) ;
    }

    static public Template createTemplate(String templateString)
    {
        return ParserARQ.parseTemplate(templateString) ;
    }
}

/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
