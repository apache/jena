/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.update;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Reader;

import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingUtils;
import com.hp.hpl.jena.sparql.lang.ParserARQUpdate ;
//import com.hp.hpl.jena.sparql.lang.ParserSPARQLUpdate;
import com.hp.hpl.jena.sparql.modify.UpdateProcessorFactory;
import com.hp.hpl.jena.sparql.modify.UpdateProcessorRegistry;
import com.hp.hpl.jena.sparql.modify.op.Update;

import com.hp.hpl.jena.query.QuerySolution;

public class UpdateFactory
{
    /** Create an empty UpdateRequest */
    public static UpdateRequest create() { return new UpdateRequest() ; }
    
    /** Create an UpdateRequest by parsing the given string */
    public static UpdateRequest create(String str)
    { 
        //ParserSPARQLUpdate p = new ParserSPARQLUpdate() ;
        ParserARQUpdate p = new ParserARQUpdate() ;
        UpdateRequest update = new UpdateRequest() ;
        p.parse(update, str) ;
        return update ;
    }
    
    /** Create an UpdateRequest by reading it from a file */
    public static UpdateRequest read(String fileName)
    { 
        InputStream in = null ;
        if ( fileName.equals("-") )
            in = System.in ;
        else
            try
            {
                in = new FileInputStream(fileName) ;
            } catch (FileNotFoundException ex)
            {
                throw new UpdateException("File not found: "+fileName) ;
            }
        return read(in) ;
    }
    
    /** Create an UpdateRequest by reading it from an InputStream (note that conversion to UTF-8 will be applied automatically) */
    public static UpdateRequest read(InputStream in)
    {
        //ParserSPARQLUpdate p = new ParserSPARQLUpdate() ;
        ParserARQUpdate p = new ParserARQUpdate() ;
        UpdateRequest update = new UpdateRequest() ;
        p.parse(update, in) ;
        return update ;
    }

    /** Create an UpdateRequest by reading it from a Reader */
    private static UpdateRequest read(Reader in)
    {
        //ParserSPARQLUpdate p = new ParserSPARQLUpdate() ;
        ParserARQUpdate p = new ParserARQUpdate() ;
        UpdateRequest update = new UpdateRequest() ;
        p.parse(update, in) ;
        return update ;
    }

    /** Create a UpdateProcessor appropriate to the GraphStore, or null if no available factory to make an UpdateProcessor 
     * @param update
     * @param graphStore
     * @return UpdateProcessor or null
     */
    public static UpdateProcessor create(Update update, GraphStore graphStore)
    {
        return create(update, graphStore, (Binding)null) ;
    }
    
    /** Create a UpdateProcessor appropriate to the GraphStore, or null if no available factory to make an UpdateProcessor 
     * @param update
     * @param graphStore
     * @param initialSolution
     * @return UpdateProcessor or null
     */
    public static UpdateProcessor create(Update update, GraphStore graphStore, QuerySolution initialSolution)
    {        
        Binding b = null ;
        if ( initialSolution != null )
            b = BindingUtils.asBinding(initialSolution) ;
        return create(update, graphStore, b) ;
    }
    
    /** Create a UpdateProcessor appropriate to the GraphStore, or null if no available factory to make an UpdateProcessor 
     * @param update
     * @param graphStore
     * @param initialBinding
     * @return UpdateProcessor or null
     */
    public static UpdateProcessor create(Update update, GraphStore graphStore, Binding initialBinding)
    {        
        return create(new UpdateRequest(update), graphStore, initialBinding) ;
    }
    
    /** Create a UpdateProcessor appropriate to the GraphStore, or null if no available factory to make an UpdateProcessor 
     * @param updateRequest
     * @param graphStore
     * @return UpdateProcessor or null
     */
    public static UpdateProcessor create(UpdateRequest updateRequest, GraphStore graphStore)
    {
        return create(updateRequest, graphStore, (Binding)null) ;
    }
    
    /** Create a UpdateProcessor appropriate to the GraphStore, or null if no available factory to make an UpdateProcessor 
     * @param updateRequest
     * @param graphStore
     * @param initialSolution
     * @return UpdateProcessor or null
     */
    public static UpdateProcessor create(UpdateRequest updateRequest, GraphStore graphStore, QuerySolution initialSolution)
    {        
        Binding b = null ;
        if ( initialSolution != null )
            b = BindingUtils.asBinding(initialSolution) ;
        return create(updateRequest, graphStore, b) ;
    }
    
    /** Create a UpdateProcessor appropriate to the GraphStore, or null if no available factory to make an UpdateProcessor 
     * @param updateRequest
     * @param graphStore
     * @param initialBinding
     * @return UpdateProcessor or null
     */
    public static UpdateProcessor create(UpdateRequest updateRequest, GraphStore graphStore, Binding initialBinding)
    {        
        UpdateProcessorFactory f = UpdateProcessorRegistry.get().find(updateRequest, graphStore) ;
        if ( f == null )
            return null ;
        UpdateProcessor uProc = f.create(updateRequest, graphStore, initialBinding) ;
        return uProc ;
    }
    
}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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