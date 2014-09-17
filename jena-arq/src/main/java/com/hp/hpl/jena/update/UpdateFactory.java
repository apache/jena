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

package com.hp.hpl.jena.update;

import static com.hp.hpl.jena.query.Syntax.defaultUpdateSyntax ;

import java.io.InputStream ;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.riot.system.IRIResolver ;

import com.hp.hpl.jena.query.Syntax ;
import com.hp.hpl.jena.sparql.core.Prologue ;
import com.hp.hpl.jena.sparql.lang.UpdateParser ;
import com.hp.hpl.jena.sparql.modify.UpdateRequestSink ;
import com.hp.hpl.jena.sparql.modify.UpdateSink ;
import com.hp.hpl.jena.sparql.modify.UsingUpdateSink ;
import com.hp.hpl.jena.sparql.modify.UsingList ;

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
        parser.parse(new UpdateRequestSink(request), input) ;
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
    protected static UpdateParser setupParser(Prologue prologue, String baseURI, Syntax syntax)
    {
        UpdateParser parser = UpdateParser.createParser(syntax) ;
        
        if ( parser == null )
            throw new UnsupportedOperationException("Unrecognized syntax for parsing update: "+syntax) ;
        
        if ( prologue.getResolver() == null )
        {
            IRIResolver resolver = null ;
            try {
                if ( baseURI != null ) 
                    // Sort out the baseURI - if that fails, dump in a dummy one and continue.
                    resolver = IRIResolver.create(baseURI) ;
                else
                    resolver = IRIResolver.create() ;
            }
            catch (Exception ex) {}
            if ( resolver == null )   
                resolver = IRIResolver.create("http://localhost/update/defaultBase#") ;
            prologue.setResolver(resolver) ;
        }
        return parser ;
    }
    
    /** Create an UpdateRequest by reading it from a file */
    public static UpdateRequest read(UsingList usingList, String fileName)
    { 
        return read(usingList, fileName, null, defaultUpdateSyntax) ;
    }
    
    /** Create an UpdateRequest by reading it from a file */
    public static UpdateRequest read(String fileName)
    { 
        return read(fileName, fileName, defaultUpdateSyntax) ;
    }
    
    /** Create an UpdateRequest by reading it from a file */
    public static UpdateRequest read(String fileName, Syntax syntax)
    {
        return read(fileName, fileName, syntax) ;
    }
    
    /** Create an UpdateRequest by reading it from a file */
    public static UpdateRequest read(UsingList usingList, String fileName, Syntax syntax)
    {
        return read(usingList, fileName, fileName, syntax) ;
    }

    /** Create an UpdateRequest by reading it from a file */
    public static UpdateRequest read(String fileName, String baseURI, Syntax syntax)
    { 
        return read(null, fileName, baseURI, syntax);
    }
    
    /** Create an UpdateRequest by reading it from a file */
    public static UpdateRequest read(UsingList usingList, String fileName, String baseURI, Syntax syntax)
    { 
        InputStream in = null ;
        try {
            if ( fileName.equals("-") )
                in = System.in ;
            else
            {
                in = IO.openFile(fileName) ;
                if ( in == null )
                    throw new UpdateException("File could not be opened: "+fileName) ;
            }
            return read(usingList, in, baseURI, syntax) ;
        } finally {
            if ( in != null && ! fileName.equals("-") )
                IO.close(in) ;
        }
    }
    
    /**  Create an UpdateRequest by parsing from an InputStream.
     * See also <tt>read</tt> operations for parsing contents of a file.
     * @param input     The source of the update request (must be UTF-8). 
     */
    public static UpdateRequest read(InputStream input)
    {
        return read(input, defaultUpdateSyntax) ;
    }
    
    /**  Create an UpdateRequest by parsing from an InputStream.
     * See also <tt>read</tt> operations for parsing contents of a file.
     * @param usingList The list of externally defined USING statements
     * @param input     The source of the update request (must be UTF-8). 
     */
    public static UpdateRequest read(UsingList usingList, InputStream input)
    {
        return read(usingList, input, defaultUpdateSyntax) ;
    }

    /**  Create an UpdateRequest by parsing from an InputStream.
     * See also <tt>read</tt> operations for parsing contents of a file.
     * @param input     The source of the update request (must be UTF-8). 
     * @param syntax    The update language syntax 
     */
    public static UpdateRequest read(InputStream input, Syntax syntax)
    {
        return read(input, null, syntax) ;
    }
    
    /**  Create an UpdateRequest by parsing from an InputStream.
     * See also <tt>read</tt> operations for parsing contents of a file.
     * @param usingList The list of externally defined USING statements
     * @param input     The source of the update request (must be UTF-8). 
     * @param syntax    The update language syntax 
     */
    public static UpdateRequest read(UsingList usingList, InputStream input, Syntax syntax)
    {
        return read(usingList, input, null, syntax) ;
    }
    
    /**  Create an UpdateRequest by parsing from an InputStream.
     * See also <tt>read</tt> operations for parsing contents of a file.
     * @param input     The source of the update request (must be UTF-8). 
     * @param baseURI   The base URI for resolving relative URIs. 
     */
    public static UpdateRequest read(InputStream input, String baseURI)
    { 
        return read(input, baseURI, defaultUpdateSyntax) ;
    }
    
    /**  Create an UpdateRequest by parsing from an InputStream.
     * See also <tt>read</tt> operations for parsing contents of a file.
     * @param usingList The list of externally defined USING statements
     * @param input     The source of the update request (must be UTF-8). 
     * @param baseURI   The base URI for resolving relative URIs. 
     */
    public static UpdateRequest read(UsingList usingList, InputStream input, String baseURI)
    { 
        return read(usingList, input, baseURI, defaultUpdateSyntax) ;
    }
    
    /**  Create an UpdateRequest by parsing from an InputStream.
     * See also <tt>read</tt> operations for parsing contents of a file.
     * @param input     The source of the update request (must be UTF-8). 
     * @param baseURI   The base URI for resolving relative URIs. 
     * @param syntax    The update language syntax 
     */
    public static UpdateRequest read(InputStream input, String baseURI, Syntax syntax)
    {
        return read(null, input, baseURI, syntax);
    }
    
    /**  Create an UpdateRequest by parsing from an InputStream.
     * See also <tt>read</tt> operations for parsing contents of a file.
     * @param usingList The list of externally defined USING statements
     * @param input     The source of the update request (must be UTF-8). 
     * @param baseURI   The base URI for resolving relative URIs. 
     * @param syntax    The update language syntax 
     */
    public static UpdateRequest read(UsingList usingList, InputStream input, String baseURI, Syntax syntax)
    {
        UpdateRequest request = new UpdateRequest() ;
        make(request, usingList, input, baseURI, syntax) ;
        return request ;
    }
    
    /** Append update operations to a request */
    private static void make(UpdateRequest request, UsingList usingList, InputStream input,  String baseURI, Syntax syntax)
    {
        UpdateParser parser = setupParser(request, baseURI, syntax) ;
        UpdateSink sink = new UsingUpdateSink(new UpdateRequestSink(request), usingList) ;
        try
        {
            parser.parse(sink, input) ;
        }
        finally
        {
            sink.close() ;
        }
    }
}
