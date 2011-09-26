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

package com.hp.hpl.jena.update;

import java.io.InputStream ;

import org.openjena.atlas.io.IO ;

import com.hp.hpl.jena.n3.IRIResolver ;
import com.hp.hpl.jena.query.QuerySolution ;
import static com.hp.hpl.jena.query.Syntax.* ;
import com.hp.hpl.jena.query.Syntax ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.lang.UpdateParser ;

public class UpdateFactory
{
    /** Create an empty UpdateRequest */
    public static UpdateRequest create() { return new UpdateRequest() ; }
    
    /**  Create an UpdateRequest by parsing from a string.
     * See also <tt>read</tt> operations for parsing contents of a file.
     * @param string    The update request as a string.
     */
    public static UpdateRequest create(String string)
    { 
        return create(string, defaultUpdateSyntax) ;
    }

    /**  Create an UpdateRequest by parsing from a string.
     * See also <tt>read</tt> operations for parsing contents of a file.
     * @param string    The update request as a string.
     * @param syntax    The update language syntax 
     */
    public static UpdateRequest create(String string, Syntax syntax)
    { 
        return create(string, null, syntax) ;
    }
    
    /**  Create an UpdateRequest by parsing from a string.
     * See also <tt>read</tt> operations for parsing contents of a file.
     * @param string    The update request as a string.
     * @param baseURI   The base URI for resolving relative URIs. 
     */
    public static UpdateRequest create(String string, String baseURI)
    {
        return create(string, baseURI, defaultUpdateSyntax) ;
    }
    
    /**  Create an UpdateRequest by parsing from a string.
     * See also <tt>read</tt> operations for parsing contents of a file.
     * @param string    The update request as a string.
     * @param baseURI   The base URI for resolving relative URIs. 
     * @param syntax    The update language syntax 
     */
    public static UpdateRequest create(String string, String baseURI, Syntax syntax)
    {
        UpdateRequest request = new UpdateRequest() ;
        make(request, string, baseURI, syntax) ;
        return request ;
    }
    
    // Worker.
    /** Append update operations to a request */
    private static void make(UpdateRequest request, String input,  String baseURI, Syntax syntax)
    {
        UpdateParser parser = setupParser(request, baseURI, syntax) ;
        parser.parse(request, input) ;
    }
    
    /* Parse operations and add to an UpdateRequest */ 
    public static void parse(UpdateRequest request, String updateString)
    {
        make(request, updateString, null, defaultUpdateSyntax) ;
    }
    
    /* Parse operations and add to an UpdateRequest */ 
    public static void parse(UpdateRequest request, String updateString, Syntax syntax)
    {
        make(request, updateString, null, syntax) ;
    }
    
    /* Parse operations and add to an UpdateRequest */ 
    public static void parse(UpdateRequest request, String updateString, String baseURI)
    {
        make(request, updateString, baseURI, defaultUpdateSyntax) ;
    }
    
    /* Parse operations and add to an UpdateRequest */ 
    public static void parse(UpdateRequest request, String updateString, String baseURI, Syntax syntax)
    {
        make(request, updateString, baseURI, syntax) ;
    }
    
    /** Append update operations to a request */
    private static UpdateParser setupParser(UpdateRequest request, String baseURI, Syntax syntax)
    {
        if ( syntax != syntaxSPARQL_11 && syntax != syntaxARQ ) 
            throw new UnsupportedOperationException("Unrecognized syntax for parsing update: "+syntax) ;
            
        UpdateParser parser = UpdateParser.createParser(syntax) ;
        
        if ( parser == null )
            throw new UnsupportedOperationException("Unrecognized syntax for parsing update: "+syntax) ;
        
        if ( request.getResolver() == null )
        {
            // Sort out the baseURI - if that fails, dump in a dummy one and continue.
            try { baseURI = IRIResolver.chooseBaseURI(baseURI) ; }
            catch (Exception ex)
            { baseURI = "http://localhost/defaultBase#" ; }
            request.setResolver(new IRIResolver(baseURI)) ;
        }
        
        return parser ;
    }
    
    /** Create an UpdateRequest by reading it from a file */
    public static UpdateRequest read(String fileName)
    { 
        return read(fileName, null, defaultUpdateSyntax) ;
    }
    
    /** Create an UpdateRequest by reading it from a file */
    public static UpdateRequest read(String fileName, Syntax syntax)
    {
        return read(fileName, null, syntax) ;
    }

    /** Create an UpdateRequest by reading it from a file */
    public static UpdateRequest read(String fileName, String baseURI, Syntax syntax)
    { 
        InputStream in = null ;
        if ( fileName.equals("-") )
            in = System.in ;
        else
        {
            in = IO.openFile(fileName) ;
            if ( in == null )
                throw new UpdateException("File could not be opened: "+fileName) ;
        }
        return read(in, baseURI, syntax) ;
    }
    
    /**  Create an UpdateRequest by parsing from a string.
     * See also <tt>read</tt> operations for parsing contents of a file.
     * @param input     The source of the update request (must be UTF-8). 
     */
    public static UpdateRequest read(InputStream input)
    {
        return read(input, defaultUpdateSyntax) ;
    }

    /**  Create an UpdateRequest by parsing from a string.
     * See also <tt>read</tt> operations for parsing contents of a file.
     * @param input     The source of the update request (must be UTF-8). 
     * @param syntax    The update language syntax 
     */
    public static UpdateRequest read(InputStream input, Syntax syntax)
    {
        return read(input, null, syntax) ;
    }
    
    /**  Create an UpdateRequest by parsing from a string.
     * See also <tt>read</tt> operations for parsing contents of a file.
     * @param input     The source of the update request (must be UTF-8). 
     * @param baseURI   The base URI for resolving relative URIs. 
     */
    public static UpdateRequest read(InputStream input, String baseURI)
    { 
        return read(input, baseURI, defaultUpdateSyntax) ;
    }
    
    /**  Create an UpdateRequest by parsing from a string.
     * See also <tt>read</tt> operations for parsing contents of a file.
     * @param input     The source of the update request (must be UTF-8). 
     * @param baseURI   The base URI for resolving relative URIs. 
     * @param syntax    The update language syntax 
     */
    public static UpdateRequest read(InputStream input, String baseURI, Syntax syntax)
    {
        UpdateRequest request = new UpdateRequest() ;
        make(request, input, baseURI, syntax) ;
        return request ;
    }
    
    /** Append update operations to a request */
    private static void make(UpdateRequest request, InputStream input,  String baseURI, Syntax syntax)
    {
        UpdateParser parser = setupParser(request, baseURI, syntax) ;
        parser.parse(request, input) ;
    }
    
//    /** Create an UpdateRequest by reading it from a Reader */
//    private static UpdateRequest read(StringReader input, Syntax syntax)
//    {
//        UpdateRequest request = new UpdateRequest() ;
//        UpdateParser parser = setupParser(request, null, syntax) ;
//        parser.parse(request, input) ;
//        return request ;
//    }

    // OLD
    
    /** Create a UpdateProcessor appropriate to the GraphStore, or null if no available factory to make an UpdateProcessor 
     * @param update
     * @param graphStore
     * @return UpdateProcessor or null
     * @deprecated Use {@link UpdateExecutionFactory#create(Update,GraphStore)} instead
     */
    @Deprecated
    public static UpdateProcessor create(Update update, GraphStore graphStore)
    {
        return UpdateExecutionFactory.create(update, graphStore) ;
    }
    
    /** Create a UpdateProcessor appropriate to the GraphStore, or null if no available factory to make an UpdateProcessor 
     * @param update
     * @param graphStore
     * @param initialSolution
     * @return UpdateProcessor or null
     * @deprecated Use {@link UpdateExecutionFactory#create(Update,GraphStore,QuerySolution)} instead
     */
    @Deprecated
    public static UpdateProcessor create(Update update, GraphStore graphStore, QuerySolution initialSolution)
    {
        return UpdateExecutionFactory.create(update, graphStore, initialSolution) ;
    }
    
    /** Create a UpdateProcessor appropriate to the GraphStore, or null if no available factory to make an UpdateProcessor 
     * @param update
     * @param graphStore
     * @param initialBinding
     * @return UpdateProcessor or null
     * @deprecated Use {@link UpdateExecutionFactory#create(Update,GraphStore,Binding)} instead
     */
    @Deprecated
    public static UpdateProcessor create(Update update, GraphStore graphStore, Binding initialBinding)
    {
        return UpdateExecutionFactory.create(update, graphStore, initialBinding) ;
    }
    
    /** Create a UpdateProcessor appropriate to the GraphStore, or null if no available factory to make an UpdateProcessor 
     * @param updateRequest
     * @param graphStore
     * @return UpdateProcessor or null
     * @deprecated Use {@link UpdateExecutionFactory#create(UpdateRequest,GraphStore)} instead
     */
    @Deprecated
    public static UpdateProcessor create(UpdateRequest updateRequest, GraphStore graphStore)
    {
        return UpdateExecutionFactory.create(updateRequest, graphStore) ;
    }
    
    /** Create a UpdateProcessor appropriate to the GraphStore, or null if no available factory to make an UpdateProcessor 
     * @param updateRequest
     * @param graphStore
     * @param initialSolution
     * @return UpdateProcessor or null
     * @deprecated Use {@link UpdateExecutionFactory#create(UpdateRequest,GraphStore,QuerySolution)} instead
     */
    @Deprecated
    public static UpdateProcessor create(UpdateRequest updateRequest, GraphStore graphStore, QuerySolution initialSolution)
    {
        return UpdateExecutionFactory.create(updateRequest, graphStore, initialSolution) ;
    }
    
    /** Create a UpdateProcessor appropriate to the GraphStore, or null if no available factory to make an UpdateProcessor 
     * @param updateRequest
     * @param graphStore
     * @param initialBinding
     * @return UpdateProcessor or null
     * @deprecated Use {@link UpdateExecutionFactory#create(UpdateRequest,GraphStore,Binding)} instead
     */
    @Deprecated
    public static UpdateProcessor create(UpdateRequest updateRequest, GraphStore graphStore, Binding initialBinding)
    {
        return UpdateExecutionFactory.create(updateRequest, graphStore, initialBinding) ;
    }
}
