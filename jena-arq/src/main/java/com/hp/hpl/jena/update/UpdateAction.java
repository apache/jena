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
import com.hp.hpl.jena.query.QuerySolution ;
import com.hp.hpl.jena.query.Syntax ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.binding.BindingUtils ;
import com.hp.hpl.jena.sparql.lang.UpdateParser ;
import com.hp.hpl.jena.sparql.modify.UpdateSink ;
import com.hp.hpl.jena.sparql.modify.UsingUpdateSink ;
import com.hp.hpl.jena.sparql.modify.UsingList ;

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
        readExecute(filename, model, null) ; 
    }
    
    /** Read a file containing SPARQL Update operations, and execute the operations.
     * @param filename
     * @param dataset
     */
    public static void readExecute(String filename, Dataset dataset)
    {
        readExecute(filename, dataset, null) ; 
    }

    /** Read a file containing SPARQL Update operations, and execute the operations.
     * @param filename
     * @param dataset
     */
    public static void readExecute(String filename, DatasetGraph dataset)
    {
        readExecute(filename, dataset, null) ; 
    }

    /** Read a file containing SPARQL Update operations, and execute the operations.
     * @param filename
     * @param graph
     */
    public static void readExecute(String filename, Graph graph)
    {
        readExecute(filename, GraphStoreFactory.create(graph), null) ; 
    }

    /** Read a file containing SPARQL Update operations, and execute the operations.
     * @param filename
     * @param graphStore
     */
    public static void readExecute(String filename, GraphStore graphStore)
    {
        readExecute(filename, graphStore, null) ;
    }


    /** Read a file containing SPARQL Update operations, and execute the operations.
     * @param filename
     * @param model
     * @param initialBinding Presets for variables.
     */
    public static void readExecute(String filename, Model model, QuerySolution initialBinding)
    {
        readExecute(filename, GraphStoreFactory.create(model), BindingUtils.asBinding(initialBinding)) ; 
    }
    
    /** Read a file containing SPARQL Update operations, and execute the operations.
     * @param filename
     * @param dataset
     * @param initialBinding Presets for variables.
     */
    public static void readExecute(String filename, Dataset dataset, QuerySolution initialBinding)
    {
        readExecute(filename, GraphStoreFactory.create(dataset), BindingUtils.asBinding(initialBinding)) ;
    }

    /** Read a file containing SPARQL Update operations, and execute the operations.
     * @param filename
     * @param dataset
     * @param initialBinding Presets for variables.
     */
    public static void readExecute(String filename, DatasetGraph dataset, Binding initialBinding)
    {
        readExecute(filename, GraphStoreFactory.create(dataset), initialBinding) ;
    }


    /** Read a file containing SPARQL Update operations, and execute the operations.
     * @param filename
     * @param graph
     * @param binding Presets for variables.
     */
    public static void readExecute(String filename, Graph graph, Binding binding)
    {
        readExecute(filename, GraphStoreFactory.create(graph), binding) ; 
    }

    /** Read a file containing SPARQL Update operations, and execute the operations.
     * @param filename
     * @param graphStore
     * @param binding Presets for variables.
     */
    public static void readExecute(String filename, GraphStore graphStore, Binding binding)
    {
        UpdateRequest req = UpdateFactory.read(filename) ;
        execute(req, graphStore, binding) ;
    }
    
    /** Parse a string containing SPARQL Update operations, and execute the operations.
     * @param updateString
     * @param model
     */
    public static void parseExecute(String updateString, Model model)
    { 
        parseExecute(updateString, model, null) ; 
    }
    
    /** Parse a string containing SPARQL Update operations, and execute the operations.
     * @param updateString
     * @param dataset
     */
    public static void parseExecute(String updateString, Dataset dataset)
    {
        parseExecute(updateString, dataset, null) ; 
    }

    /** Parse a string containing SPARQL Update operations, and execute the operations.
     * @param updateString
     * @param dataset
     */
    public static void parseExecute(String updateString, DatasetGraph dataset)
    {
        parseExecute(updateString, dataset, null) ; 
    }

    /** Parse a string containing SPARQL Update operations, and execute the operations.
     * @param updateString
     * @param graph
     */
    public static void parseExecute(String updateString, Graph graph)
    {
        parseExecute(updateString, GraphStoreFactory.create(graph), null) ; 
    }

    /** Parse a string containing SPARQL Update operations, and execute the operations.
     * @param updateString
     * @param graphStore
     */
    public static void parseExecute(String updateString, GraphStore graphStore)
    {
        parseExecute(updateString, graphStore, null) ;
    }

    /** Parse a string containing SPARQL Update operations, and execute the operations.
     * @param updateString
     * @param model
     * @param initialBinding Presets for variables.
     */
    public static void parseExecute(String updateString, Model model, QuerySolution initialBinding)
    {
        parseExecute(updateString, GraphStoreFactory.create(model), BindingUtils.asBinding(initialBinding)) ; 
    }
    
    /** Parse a string containing SPARQL Update operations, and execute the operations.
     * @param updateString
     * @param dataset
     * @param initialBinding Presets for variables.
     */
    public static void parseExecute(String updateString, Dataset dataset, QuerySolution initialBinding)
    {
        parseExecute(updateString, GraphStoreFactory.create(dataset), BindingUtils.asBinding(initialBinding)) ; 
    }

    /** Parse a string containing SPARQL Update operations, and execute the operations.
     * @param updateString
     * @param dataset
     * @param initialBinding Presets for variables.
     */
    public static void parseExecute(String updateString, DatasetGraph dataset, Binding initialBinding)
    {
        parseExecute(updateString, GraphStoreFactory.create(dataset), initialBinding) ; 
    }


    /** Parse a string containing SPARQL Update operations, and execute the operations.
     * @param updateString
     * @param graph
     * @param binding Presets for variables.
     */
    public static void parseExecute(String updateString, Graph graph, Binding binding)
    {
        parseExecute(updateString, GraphStoreFactory.create(graph), binding) ; 
    }

    /** Parse a string containing SPARQL Update operations, and execute the operations.
     * @param updateString
     * @param graphStore
     * @param binding Presets for variables.
     */
    public static void parseExecute(String updateString, GraphStore graphStore, Binding binding)
    {
        UpdateRequest req = UpdateFactory.create(updateString) ;
        execute(req, graphStore, binding) ;
    }
    
    /** Execute SPARQL Update operations.
     * Warning - changes on named graphs not supported by this operation.
     * @see #execute(UpdateRequest, Dataset)
     * @param request
     * @param model
     */
    public static void execute(UpdateRequest request, Model model)
    { 
        execute(request, model, null) ; 
    }
    
    /** Execute SPARQL Update operations.
     * @param request
     * @param dataset
     */
    public static void execute(UpdateRequest request, Dataset dataset)
    {
        execute(request, dataset, null) ; 
    }

    /** Execute SPARQL Update operations.
     * @param request
     * @param dataset
     */
    public static void execute(UpdateRequest request, DatasetGraph dataset)
    {
        execute(request, dataset, null) ; 
    }

    /** Execute SPARQL Update operations.
     *  Warning - changes on named graphs not supported by this operation.
     * @see #execute(UpdateRequest, DatasetGraph)
     * @param request
     * @param graph
     */
    public static void execute(UpdateRequest request, Graph graph)
    {
        execute(request, GraphStoreFactory.create(graph), null) ; 
    }

    /** Execute SPARQL Update operations.
     * @param request
     * @param graphStore
     */
    public static void execute(UpdateRequest request, GraphStore graphStore)
    {
        execute(request, graphStore, null) ;
    }

    /** Execute SPARQL Update operations.
     * @param request
     * @param model
     * @param initialBinding Presets for variables.
     */
    public static void execute(UpdateRequest request, Model model, QuerySolution initialBinding)
    {
        execute(request, GraphStoreFactory.create(model), BindingUtils.asBinding(initialBinding)) ; 
    }
    
    /** Execute SPARQL Update operations.
     * @param request
     * @param dataset
     * @param initialBinding Presets for variables.
     */
    public static void execute(UpdateRequest request, Dataset dataset, QuerySolution initialBinding)
    {
        execute(request, GraphStoreFactory.create(dataset), BindingUtils.asBinding(initialBinding)) ; 
    }

    /** Execute SPARQL Update operations.
     * @param request
     * @param dataset
     * @param initialBinding Presets for variables.
     */
    public static void execute(UpdateRequest request, DatasetGraph dataset, Binding initialBinding)
    {
        execute(request, GraphStoreFactory.create(dataset), initialBinding) ; 
    }

    /** Execute SPARQL Update operations.
     * @param request
     * @param graph
     * @param binding Presets for variables.
     */
    public static void execute(UpdateRequest request, Graph graph, Binding binding)
    {
        execute(request, GraphStoreFactory.create(graph), binding) ; 
    }

    /** Execute SPARQL Update operations.
     * @param request
     * @param graphStore
     * @param binding Presets for variables.
     */
    public static void execute(UpdateRequest request, GraphStore graphStore, Binding binding)
    {
        execute$(request, graphStore, binding) ;
    }
    
    // All non-streaming updates come through here.
    private static void execute$(UpdateRequest request, GraphStore graphStore, Binding binding)
    {
        UpdateProcessor uProc = UpdateExecutionFactory.create(request, graphStore, binding) ;
        uProc.execute() ;
    }
    
    /** Execute a single SPARQL Update operation.
     * @param update
     * @param model
     */
    public static void execute(Update update, Model model)
    { 
        execute(update, model, null) ; 
    }
    
    /** Execute a single SPARQL Update operation.
     * @param update
     * @param dataset
     */
    public static void execute(Update update, Dataset dataset)
    {
        execute(update, dataset, null) ; 
    }

    /** Execute a single SPARQL Update operation.
     * @param update
     * @param dataset
     */
    public static void execute(Update update, DatasetGraph dataset)
    {
        execute(update, dataset, null) ; 
    }

    /** Execute a single SPARQL Update operation.
     * @param update
     * @param graph
     */
    public static void execute(Update update, Graph graph)
    {
        execute(update, GraphStoreFactory.create(graph), null) ; 
    }

    /** Execute a single SPARQL Update operation.
     * @param update
     * @param graphStore
     */
    public static void execute(Update update, GraphStore graphStore)
    {
        execute(update, graphStore, null) ;
    }

    /** Execute a single SPARQL Update operation.
     * @param update
     * @param model
     * @param initialBinding Presets for variables.
     */
    public static void execute(Update update, Model model, QuerySolution initialBinding)
    {
        execute(update, GraphStoreFactory.create(model), BindingUtils.asBinding(initialBinding)) ; 
    }
    
    /** Execute a single SPARQL Update operation.
     * @param update
     * @param dataset
     * @param initialBinding Presets for variables.
     */
    public static void execute(Update update, Dataset dataset, QuerySolution initialBinding)
    {
        execute(update, GraphStoreFactory.create(dataset), BindingUtils.asBinding(initialBinding)) ; 
    }

    /** Execute a single SPARQL Update operation.
     * @param update
     * @param dataset
     * @param initialBinding Presets for variables.
     */
    public static void execute(Update update, DatasetGraph dataset, Binding initialBinding)
    {
        execute(update, GraphStoreFactory.create(dataset), initialBinding) ; 
    }

    /** Execute a single SPARQL Update operation.
     * @param update
     * @param graph
     * @param binding Presets for variables.
     */
    public static void execute(Update update, Graph graph, Binding binding)
    {
        execute(update, GraphStoreFactory.create(graph), binding) ; 
    }

    /** Execute a single SPARQL Update operation.
     * @param update
     * @param graphStore
     * @param binding Presets for variables.
     */
    public static void execute(Update update, GraphStore graphStore, Binding binding)
    {
        execute$(update, graphStore, binding) ;
    }
    
    private static void execute$(Update update, GraphStore graphStore, Binding binding)
    {
        UpdateRequest request = new UpdateRequest() ;
        request.add(update) ;
        execute$(request, graphStore, binding) ;
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
     * @param input     The source of the update request (must be UTF-8). 
     */
    public static void parseExecute(UsingList usingList, DatasetGraph dataset, InputStream input)
    {
        parseExecute(usingList, dataset, input, Syntax.defaultUpdateSyntax) ;
    }

    /** 
     * Parse update operations into a GraphStore by parsing from an InputStream.
     * @param input     The source of the update request (must be UTF-8). 
     * @param syntax    The update language syntax 
     */
    public static void parseExecute(UsingList usingList, DatasetGraph dataset, InputStream input, Syntax syntax)
    {
        parseExecute(usingList, dataset, input, null, syntax) ;
    }
    
    /**
     * Parse update operations into a GraphStore by parsing from an InputStream.
     * @param input     The source of the update request (must be UTF-8). 
     * @param baseURI   The base URI for resolving relative URIs. 
     */
    public static void parseExecute(UsingList usingList, DatasetGraph dataset, InputStream input, String baseURI)
    { 
        parseExecute(usingList, dataset, input, baseURI, Syntax.defaultUpdateSyntax) ;
    }
    
    /**
     * Parse update operations into a GraphStore by parsing from an InputStream.
     * @param input     The source of the update request (must be UTF-8). 
     * @param baseURI   The base URI for resolving relative URIs. 
     * @param syntax    The update language syntax 
     */
    public static void parseExecute(UsingList usingList, DatasetGraph dataset, InputStream input, String baseURI, Syntax syntax)
    {
        GraphStore graphStore = GraphStoreFactory.create(dataset);
        
        UpdateProcessorStreaming uProc = UpdateExecutionFactory.createStreaming(graphStore) ;
        
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
