/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.update;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingUtils;
import com.hp.hpl.jena.sparql.modify.op.Update;

/** A class of convenience forms for executing SPARQL/Update operations */

public class UpdateAction
{
    /** Read a file containing SPARQL/Update operations, and execute the operations.
     * @param filename
     * @param model
     */
    public static void readExecute(String filename, Model model)
    { 
        readExecute(filename, model, null) ; 
    }
    
    /** Read a file containing SPARQL/Update operations, and execute the operations.
     * @param filename
     * @param dataset
     */
    public static void readExecute(String filename, Dataset dataset)
    {
        readExecute(filename, dataset, null) ; 
    }

    /** Read a file containing SPARQL/Update operations, and execute the operations.
     * @param filename
     * @param graph
     */
    public static void readExecute(String filename, Graph graph)
    {
        readExecute(filename, GraphStoreFactory.create(graph), null) ; 
    }

    /** Read a file containing SPARQL/Update operations, and execute the operations.
     * @param filename
     * @param graphStore
     */
    public static void readExecute(String filename, GraphStore graphStore)
    {
        readExecute(filename, graphStore, null) ;
    }


    /** Read a file containing SPARQL/Update operations, and execute the operations.
     * @param filename
     * @param model
     * @param initialBinding Presets for variables.
     */
    public static void readExecute(String filename, Model model, QuerySolution initialBinding)
    {
        readExecute(filename, GraphStoreFactory.create(model), BindingUtils.asBinding(initialBinding)) ; 
    }
    
    /** Read a file containing SPARQL/Update operations, and execute the operations.
     * @param filename
     * @param dataset
     * @param initialBinding Presets for variables.
     */
    public static void readExecute(String filename, Dataset dataset, QuerySolution initialBinding)
    {
        readExecute(filename, GraphStoreFactory.create(dataset), BindingUtils.asBinding(initialBinding)) ; 
    }

    /** Read a file containing SPARQL/Update operations, and execute the operations.
     * @param filename
     * @param graph
     * @param binding Presets for variables.
     */
    public static void readExecute(String filename, Graph graph, Binding binding)
    {
        readExecute(filename, GraphStoreFactory.create(graph), binding) ; 
    }

    /** Read a file containing SPARQL/Update operations, and execute the operations.
     * @param filename
     * @param graphStore
     * @param binding Presets for variables.
     */
    public static void readExecute(String filename, GraphStore graphStore, Binding binding)
    {
        UpdateRequest req = UpdateFactory.read(filename) ;
        execute(req, graphStore, binding) ;
    }
    
    /** Parse a string containing SPARQL/Update operations, and execute the operations.
     * @param updateString
     * @param model
     */
    public static void parseExecute(String updateString, Model model)
    { 
        parseExecute(updateString, model, null) ; 
    }
    
    /** Parse a string containing SPARQL/Update operations, and execute the operations.
     * @param updateString
     * @param dataset
     */
    public static void parseExecute(String updateString, Dataset dataset)
    {
        parseExecute(updateString, dataset, null) ; 
    }

    /** Parse a string containing SPARQL/Update operations, and execute the operations.
     * @param updateString
     * @param graph
     */
    public static void parseExecute(String updateString, Graph graph)
    {
        parseExecute(updateString, GraphStoreFactory.create(graph), null) ; 
    }

    /** Parse a string containing SPARQL/Update operations, and execute the operations.
     * @param updateString
     * @param graphStore
     */
    public static void parseExecute(String updateString, GraphStore graphStore)
    {
        parseExecute(updateString, graphStore, null) ;
    }

    /** Parse a string containing SPARQL/Update operations, and execute the operations.
     * @param updateString
     * @param model
     * @param initialBinding Presets for variables.
     */
    public static void parseExecute(String updateString, Model model, QuerySolution initialBinding)
    {
        if ( initialBinding == null )
            parseExecute(updateString, GraphStoreFactory.create(model), null) ;
        else
            parseExecute(updateString, GraphStoreFactory.create(model), BindingUtils.asBinding(initialBinding)) ; 
    }
    
    /** Parse a string containing SPARQL/Update operations, and execute the operations.
     * @param updateString
     * @param dataset
     * @param initialBinding Presets for variables.
     */
    public static void parseExecute(String updateString, Dataset dataset, QuerySolution initialBinding)
    {
        parseExecute(updateString, GraphStoreFactory.create(dataset), BindingUtils.asBinding(initialBinding)) ; 
    }

    /** Parse a string containing SPARQL/Update operations, and execute the operations.
     * @param updateString
     * @param graph
     * @param binding Presets for variables.
     */
    public static void parseExecute(String updateString, Graph graph, Binding binding)
    {
        parseExecute(updateString, GraphStoreFactory.create(graph), binding) ; 
    }

    /** Parse a string containing SPARQL/Update operations, and execute the operations.
     * @param updateString
     * @param graphStore
     * @param binding Presets for variables.
     */
    public static void parseExecute(String updateString, GraphStore graphStore, Binding binding)
    {
        UpdateRequest req = UpdateFactory.create(updateString) ;
        execute(req, graphStore, binding) ;
    }
    
    /** Execute SPARQL/Update operations.
     * @param request
     * @param model
     */
    public static void execute(UpdateRequest request, Model model)
    { 
        execute(request, model, null) ; 
    }
    
    /** Execute SPARQL/Update operations.
     * @param request
     * @param dataset
     */
    public static void execute(UpdateRequest request, Dataset dataset)
    {
        execute(request, dataset, null) ; 
    }

    /** Execute SPARQL/Update operations.
     * @param request
     * @param graph
     */
    public static void execute(UpdateRequest request, Graph graph)
    {
        execute(request, GraphStoreFactory.create(graph), null) ; 
    }

    /** Execute SPARQL/Update operations.
     * @param request
     * @param graphStore
     */
    public static void execute(UpdateRequest request, GraphStore graphStore)
    {
        execute(request, graphStore, null) ;
    }

    /** Execute SPARQL/Update operations.
     * @param request
     * @param model
     * @param initialBinding Presets for variables.
     */
    public static void execute(UpdateRequest request, Model model, QuerySolution initialBinding)
    {
        execute(request, GraphStoreFactory.create(model), BindingUtils.asBinding(initialBinding)) ; 
    }
    
    /** Execute SPARQL/Update operations.
     * @param request
     * @param dataset
     * @param initialBinding Presets for variables.
     */
    public static void execute(UpdateRequest request, Dataset dataset, QuerySolution initialBinding)
    {
        execute(request, GraphStoreFactory.create(dataset), BindingUtils.asBinding(initialBinding)) ; 
    }

    /** Execute SPARQL/Update operations.
     * @param request
     * @param graph
     * @param binding Presets for variables.
     */
    public static void execute(UpdateRequest request, Graph graph, Binding binding)
    {
        execute(request, GraphStoreFactory.create(graph), binding) ; 
    }

    /** Execute SPARQL/Update operations.
     * @param request
     * @param graphStore
     * @param binding Presets for variables.
     */
    public static void execute(UpdateRequest request, GraphStore graphStore, Binding binding)
    {
        UpdateProcessor uProc = UpdateFactory.create(request, graphStore, binding) ;
        uProc.execute() ;
    }
    /** Execute a single SPARQL/Update operation.
     * @param update
     * @param model
     */
    public static void execute(Update update, Model model)
    { 
        execute(update, model, null) ; 
    }
    
    /** Execute a single SPARQL/Update operation.
     * @param update
     * @param dataset
     */
    public static void execute(Update update, Dataset dataset)
    {
        execute(update, dataset, null) ; 
    }

    /** Execute a single SPARQL/Update operation.
     * @param update
     * @param graph
     */
    public static void execute(Update update, Graph graph)
    {
        execute(update, GraphStoreFactory.create(graph), null) ; 
    }

    /** Execute a single SPARQL/Update operation.
     * @param update
     * @param graphStore
     */
    public static void execute(Update update, GraphStore graphStore)
    {
        execute(update, graphStore, null) ;
    }

    /** Execute a single SPARQL/Update operation.
     * @param update
     * @param model
     * @param initialBinding Presets for variables.
     */
    public static void execute(Update update, Model model, QuerySolution initialBinding)
    {
        execute(update, GraphStoreFactory.create(model), BindingUtils.asBinding(initialBinding)) ; 
    }
    
    /** Execute a single SPARQL/Update operation.
     * @param update
     * @param dataset
     * @param initialBinding Presets for variables.
     */
    public static void execute(Update update, Dataset dataset, QuerySolution initialBinding)
    {
        execute(update, GraphStoreFactory.create(dataset), BindingUtils.asBinding(initialBinding)) ; 
    }

    /** Execute a single SPARQL/Update operation.
     * @param update
     * @param graph
     * @param binding Presets for variables.
     */
    public static void execute(Update update, Graph graph, Binding binding)
    {
        execute(update, GraphStoreFactory.create(graph), binding) ; 
    }

    /** Execute a single SPARQL/Update operation.
     * @param update
     * @param graphStore
     * @param binding Presets for variables.
     */
    public static void execute(Update update, GraphStore graphStore, Binding binding)
    {
        UpdateProcessor uProc = UpdateFactory.create(update, graphStore, binding) ;
        uProc.execute() ;
    }

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