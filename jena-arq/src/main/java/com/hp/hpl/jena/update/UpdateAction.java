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

import java.io.InputStream ;

import org.apache.jena.atlas.io.IO ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.query.Syntax ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.sparql.ARQException ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.lang.UpdateParser ;
import com.hp.hpl.jena.sparql.modify.UpdateSink ;
import com.hp.hpl.jena.sparql.modify.UsingList ;
import com.hp.hpl.jena.sparql.modify.UsingUpdateSink ;
import com.hp.hpl.jena.sparql.modify.request.UpdateWithUsing ;

/** A class of forms for executing SPARQL Update operations. 
 * parse* means the update request is in a string;
 * read* means read the contents of a file.    
 */

public class UpdateAction
{
    /** Read a file containing SPARQL Update operations, and execute the operations.
     * @param filename
     * @param model
     */
    public static void readExecute(String filename, Model model)
    {
        readExecute(filename, GraphStoreFactory.create(model)) ; 
    }
    
    /** Read a file containing SPARQL Update operations, and execute the operations.
     * @param filename
     * @param dataset
     */
    public static void readExecute(String filename, Dataset dataset)
    {
        readExecute(filename, GraphStoreFactory.create(dataset)) ;
    }

    /** Read a file containing SPARQL Update operations, and execute the operations.
     * @param filename
     * @param dataset
     */
    public static void readExecute(String filename, DatasetGraph dataset)
    {
        readExecute(filename, GraphStoreFactory.create(dataset)) ;
    }

    /** Read a file containing SPARQL Update operations, and execute the operations.
     * @param filename
     * @param graph
     */
    public static void readExecute(String filename, Graph graph)
    {
        readExecute(filename, GraphStoreFactory.create(graph)) ; 
    }

    /** Read a file containing SPARQL Update operations, and execute the operations.
     * @param filename
     * @param graphStore
     */
    public static void readExecute(String filename, GraphStore graphStore)
    {
        UpdateRequest req = UpdateFactory.read(filename) ;
        execute(req, graphStore) ;
    }
    

    /** Parse a string containing SPARQL Update operations, and execute the operations.
     * @param updateString
     * @param model
     */
    public static void parseExecute(String updateString, Model model)
    {
        parseExecute(updateString, GraphStoreFactory.create(model)) ; 
    }
    
    /** Parse a string containing SPARQL Update operations, and execute the operations.
     * @param updateString
     * @param dataset
     */
    public static void parseExecute(String updateString, Dataset dataset)
    {
        parseExecute(updateString, GraphStoreFactory.create(dataset)) ; 
    }

    /** Parse a string containing SPARQL Update operations, and execute the operations.
     * @param updateString
     * @param dataset
     */
    public static void parseExecute(String updateString, DatasetGraph dataset)
    {
        parseExecute(updateString, GraphStoreFactory.create(dataset)) ; 
    }


    /** Parse a string containing SPARQL Update operations, and execute the operations.
     * @param updateString
     * @param graph
     */
    public static void parseExecute(String updateString, Graph graph)
    {
        parseExecute(updateString, GraphStoreFactory.create(graph)) ; 
    }

    /** Parse a string containing SPARQL Update operations, and execute the operations.
     * @param updateString
     * @param graphStore
     */
    public static void parseExecute(String updateString, GraphStore graphStore)
    {
        UpdateRequest req = UpdateFactory.create(updateString) ;
        execute(req, graphStore) ;
    }
    

    /** Execute SPARQL Update operations.
     * @param request
     * @param model
     */
    public static void execute(UpdateRequest request, Model model)
    {
        execute(request, GraphStoreFactory.create(model)) ; 
    }
    
    /** Execute SPARQL Update operations.
     * @param request
     * @param dataset
     */
    public static void execute(UpdateRequest request, Dataset dataset)
    {
        execute(request, GraphStoreFactory.create(dataset)) ; 
    }

    /** Execute SPARQL Update operations.
     * @param request
     * @param dataset
     */
    public static void execute(UpdateRequest request, DatasetGraph dataset)
    {
        execute(request, GraphStoreFactory.create(dataset)) ; 
    }

    /** Execute SPARQL Update operations.
     * @param request
     * @param graph
     */
    public static void execute(UpdateRequest request, Graph graph)
    {
        execute(request, GraphStoreFactory.create(graph)) ; 
    }

    /** Execute SPARQL Update operations.
     * @param request
     * @param graphStore
     */
    public static void execute(UpdateRequest request, GraphStore graphStore)
    {
        execute$(request, graphStore) ;
    }
    
    // All non-streaming updates come through here.
    private static void execute$(UpdateRequest request, GraphStore graphStore)
    {
        UpdateProcessor uProc = UpdateExecutionFactory.create(request, graphStore);
        if (uProc == null)
            throw new ARQException("No suitable update procesors are registered/able to execute your updates");
        uProc.execute();
    }
    

    /** Execute a single SPARQL Update operation.
     * @param update
     * @param model
     */
    public static void execute(Update update, Model model)
    {
        execute(update, GraphStoreFactory.create(model)) ; 
    }
    
    /** Execute a single SPARQL Update operation.
     * @param update
     * @param dataset
     */
    public static void execute(Update update, Dataset dataset)
    {
        execute(update, GraphStoreFactory.create(dataset)) ; 
    }

    /** Execute a single SPARQL Update operation.
     * @param update
     * @param dataset
     */
    public static void execute(Update update, DatasetGraph dataset)
    {
        execute(update, GraphStoreFactory.create(dataset)) ; 
    }

    /** Execute a single SPARQL Update operation.
     * @param update
     * @param graph
     */
    public static void execute(Update update, Graph graph)
    {
        execute(update, GraphStoreFactory.create(graph)) ; 
    }

    /** Execute a single SPARQL Update operation.
     * @param update
     * @param graphStore
     */
    public static void execute(Update update, GraphStore graphStore)
    {
        execute$(update, graphStore) ;
    }
    
    private static void execute$(Update update, GraphStore graphStore)
    {
        UpdateRequest request = new UpdateRequest() ;
        request.add(update) ;
        execute$(request, graphStore) ;
    }  

    
    
    // Streaming Updates:
    
    /** Parse update operations into a GraphStore by reading it from a file */
    public static void parseExecute(UsingList usingList, DatasetGraph dataset, String fileName)
    { 
        parseExecute(usingList, dataset, fileName, null, Syntax.defaultUpdateSyntax) ;
    }
    
    /** Parse update operations into a GraphStore by reading it from a file */
    public static void parseExecute(UsingList usingList, DatasetGraph dataset, String fileName, Syntax syntax)
    {
        parseExecute(usingList, dataset, fileName, null, syntax) ;
    }

    /** Parse update operations into a GraphStore by reading it from a file */
    public static void parseExecute(UsingList usingList, DatasetGraph dataset, String fileName, String baseURI, Syntax syntax)
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
        parseExecute(usingList, dataset, in, baseURI, syntax) ;
    }
    
    /** 
     * Parse update operations into a GraphStore by parsing from an InputStream.
     * @param usingList A list of USING or USING NAMED statements that be added to all {@link UpdateWithUsing} queries
     * @param input     The source of the update request (must be UTF-8). 
     */
    public static void parseExecute(UsingList usingList, DatasetGraph dataset, InputStream input)
    {
        parseExecute(usingList, dataset, input, Syntax.defaultUpdateSyntax) ;
    }

    /** 
     * Parse update operations into a GraphStore by parsing from an InputStream.
     * @param usingList A list of USING or USING NAMED statements that be added to all {@link UpdateWithUsing} queries
     * @param input     The source of the update request (must be UTF-8). 
     * @param syntax    The update language syntax 
     */
    public static void parseExecute(UsingList usingList, DatasetGraph dataset, InputStream input, Syntax syntax)
    {
        parseExecute(usingList, dataset, input, null, syntax) ;
    }
    
    /**
     * Parse update operations into a GraphStore by parsing from an InputStream.
     * @param usingList A list of USING or USING NAMED statements that be added to all {@link UpdateWithUsing} queries
     * @param input     The source of the update request (must be UTF-8). 
     * @param baseURI   The base URI for resolving relative URIs. 
     */
    public static void parseExecute(UsingList usingList, DatasetGraph dataset, InputStream input, String baseURI)
    { 
        parseExecute(usingList, dataset, input, baseURI, Syntax.defaultUpdateSyntax) ;
    }
    
    /**
     * Parse update operations into a GraphStore by parsing from an InputStream.
     * @param usingList A list of USING or USING NAMED statements that be added to all {@link UpdateWithUsing} queries
     * @param dataset   The dataset to apply the changes to
     * @param input     The source of the update request (must be UTF-8). 
     * @param baseURI   The base URI for resolving relative URIs. 
     * @param syntax    The update language syntax 
     */
    public static void parseExecute(UsingList usingList, DatasetGraph dataset, InputStream input, String baseURI, Syntax syntax)
    {
        GraphStore graphStore = GraphStoreFactory.create(dataset);
        
        UpdateProcessorStreaming uProc = UpdateExecutionFactory.createStreaming(graphStore) ;
        if (uProc == null)
            throw new ARQException("No suitable update procesors are registered/able to execute your updates");
        
        uProc.startRequest();
        try
        {
            UpdateSink sink = new UsingUpdateSink(uProc.getUpdateSink(), usingList) ;
            try
            {
                UpdateParser parser = UpdateFactory.setupParser(sink.getPrologue(), baseURI, syntax) ;
                parser.parse(sink, input) ;
            }
            finally
            {
                sink.close() ;
            }
        }
        finally
        {
            uProc.finishRequest();
        }
    }
}
