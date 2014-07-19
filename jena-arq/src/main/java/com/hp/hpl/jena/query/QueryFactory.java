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

package com.hp.hpl.jena.query;

import org.apache.jena.riot.system.IRIResolver ;

import com.hp.hpl.jena.sparql.lang.SPARQLParser ;
import com.hp.hpl.jena.sparql.lang.ParserARQ ;
import com.hp.hpl.jena.sparql.lang.SPARQLParserRegistry ;
import com.hp.hpl.jena.sparql.syntax.Element ;
import com.hp.hpl.jena.sparql.syntax.Template ;
import com.hp.hpl.jena.util.FileManager ;


public class QueryFactory
{
    /** Create a SPARQL query from the given string.
     *
     * @param queryString      The query string
     * @throws QueryException  Thrown when a parse error occurs
     */
    
    static public Query create(String queryString)
    {
        return create(queryString, Syntax.defaultQuerySyntax) ;
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
        return parse(query, queryString, baseURI, Syntax.defaultQuerySyntax) ;
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
       return parse(query, queryString, baseURI, syntax) ;
   }
   
   /**
    * Make a query - no parsing done  
    */
   static public Query create() { return new Query() ; }

   
   /**
     * Make a query - no parsing done - old name: {@link #create()} preferred. 
     */
    static public Query make() { return create() ; }

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

        SPARQLParser parser = SPARQLParser.createParser(syntaxURI) ;
        
        if ( parser == null )
            throw new UnsupportedOperationException("Unrecognized syntax for parsing: "+syntaxURI) ;
        
        if ( query.getResolver() == null )
        {
            IRIResolver resolver = null ;
            try { 
                if ( baseURI != null ) { 
                    // Sort out the baseURI - if that fails, dump in a dummy one and continue.
                    resolver = IRIResolver.create(baseURI) ; 
                }
                else { 
                    resolver = IRIResolver.create() ;
                }
            }
            catch (Exception ex) {}
            if ( resolver == null )   
                resolver = IRIResolver.create("http://localhost/query/defaultBase#") ;
            query.setResolver(resolver) ;
            
        }
        return parser.parse(query, queryString) ;
    }
    
    static boolean knownParserSyntax(Syntax syntaxURI)
    {
        return SPARQLParserRegistry.get().containsFactory(syntaxURI) ;
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
            langURI = Syntax.guessFileSyntax(url) ;
        
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
