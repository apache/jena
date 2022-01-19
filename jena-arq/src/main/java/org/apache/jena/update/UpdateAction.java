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

package org.apache.jena.update;

import java.io.InputStream ;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.graph.Graph ;
import org.apache.jena.query.Dataset ;
import org.apache.jena.query.QuerySolution ;
import org.apache.jena.query.Syntax ;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.sparql.ARQException ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.DatasetGraphFactory ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.engine.binding.BindingLib;
import org.apache.jena.sparql.exec.UpdateExec;
import org.apache.jena.sparql.lang.UpdateParser ;
import org.apache.jena.sparql.modify.UpdateSink ;
import org.apache.jena.sparql.modify.UsingList ;
import org.apache.jena.sparql.modify.UsingUpdateSink ;
import org.apache.jena.sparql.modify.request.UpdateWithUsing ;

/** A class of forms for executing SPARQL Update operations.
 * parse means the update request is in a String or an InputStream;
 * read means read the contents of a file.
 */

public class UpdateAction
{
    /** Read a file containing SPARQL Update operations, and execute the operations.
     * @param filename
     * @param model
     */
    public static void readExecute(String filename, Model model)
    {
        readExecute(filename, toDatasetGraph(model.getGraph())) ;
    }

    /** Read a file containing SPARQL Update operations, and execute the operations.
     * @param filename
     * @param graph
     */
    public static void readExecute(String filename, Graph graph)
    {
        readExecute(filename, toDatasetGraph(graph)) ;
    }

    /** Read a file containing SPARQL Update operations, and execute the operations.
     * @param filename
     * @param dataset
     */
    public static void readExecute(String filename, Dataset dataset)
    {
        readExecute(filename, dataset.asDatasetGraph()) ;
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
     * @param dataset
     * @param inputBinding
     */
    public static void readExecute(String filename, Dataset dataset, QuerySolution inputBinding) {
        UpdateRequest req = UpdateFactory.read(filename) ;
        execute(req, dataset, inputBinding) ;
    }

    /** Read a file containing SPARQL Update operations, and execute the operations.
     * @param filename
     * @param datasetGraph
     * @param inputBinding
     */
    public static void readExecute(String filename, DatasetGraph datasetGraph, Binding inputBinding) {
        UpdateRequest req = UpdateFactory.read(filename) ;
        execute$(req, datasetGraph, inputBinding) ;
    }

    /** Parse a string containing SPARQL Update operations, and execute the operations.
     * @param updateString
     * @param model
     */
    public static void parseExecute(String updateString, Model model)
    {
        parseExecute(updateString, model.getGraph()) ;
    }

    /** Parse a string containing SPARQL Update operations, and execute the operations.
     * @param updateString
     * @param graph
     */
    public static void parseExecute(String updateString, Graph graph)
    {
        parseExecute(updateString, toDatasetGraph(graph)) ;
    }

    /** Parse a string containing SPARQL Update operations, and execute the operations.
     * @param updateString
     * @param dataset
     */
    public static void parseExecute(String updateString, Dataset dataset)
    {
        parseExecute(updateString, dataset.asDatasetGraph()) ;
    }

    /** Parse a string containing SPARQL Update operations, and execute the operations.
     * @param updateString
     * @param dataset
     */
    public static void parseExecute(String updateString, DatasetGraph dataset)
    {
        UpdateRequest req = UpdateFactory.create(updateString) ;
        execute(req, dataset) ;
    }

    /** Parse a string containing SPARQL Update operations, and execute the operations.
     * @param updateString
     * @param dataset
     * @param inputBinding
     */
    public static void parseExecute(String updateString, Dataset dataset, QuerySolution inputBinding)
    {
        parseExecute(updateString, dataset.asDatasetGraph(), BindingLib.asBinding(inputBinding)) ;
    }

    /** Parse a string containing SPARQL Update operations, and execute the operations.
     * @param updateString
     * @param dataset
     * @param inputBinding
     */
    public static void parseExecute(String updateString, DatasetGraph dataset, Binding inputBinding)
    {
        UpdateRequest req = UpdateFactory.create(updateString) ;
        execute(req, dataset, inputBinding) ;
    }

    /** Execute SPARQL Update operations.
     * @param request
     * @param model
     */
    public static void execute(UpdateRequest request, Model model)
    {
        execute(request, model.getGraph()) ;
    }

    /** Execute SPARQL Update operations.
     * @param request
     * @param graph
     */
    public static void execute(UpdateRequest request, Graph graph)
    {
        execute(request, toDatasetGraph(graph)) ;
    }

    /** Execute SPARQL Update operations.
     * @param request
     * @param dataset
     */
    public static void execute(UpdateRequest request, Dataset dataset)
    {
        execute(request, dataset.asDatasetGraph()) ;
    }

    /** Execute SPARQL Update operations.
     * @param request
     * @param dataset
     */
    public static void execute(UpdateRequest request, DatasetGraph dataset)
    {
        execute$(request, dataset, null) ;
    }

    /** Execute SPARQL Update operations.
     * @param request
     * @param dataset
     * @param inputBinding
     */
    public static void execute(UpdateRequest request, Dataset dataset, QuerySolution inputBinding)
    {
        execute(request, dataset.asDatasetGraph(), BindingLib.asBinding(inputBinding)) ;
    }

    /** Execute SPARQL Update operations.
     * @param request
     * @param datasetGraph
     * @param inputBinding
     */
    public static void execute(UpdateRequest request, DatasetGraph datasetGraph, Binding inputBinding)
    {
        execute$(request, datasetGraph, inputBinding) ;
    }


    private static DatasetGraph toDatasetGraph(Graph graph) {
        return DatasetGraphFactory.create(graph) ;
    }

    // All non-streaming updates come through here.
    private static void execute$(UpdateRequest request, DatasetGraph datasetGraph, Binding inputBinding)
    {
        UpdateExec uProc = UpdateExec.newBuilder().update(request).dataset(datasetGraph).initialBinding(inputBinding).build();
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
        execute(update, model.getGraph()) ;
    }

    /** Execute a single SPARQL Update operation.
     * @param update
     * @param graph
     */
    public static void execute(Update update, Graph graph)
    {
        execute(update, toDatasetGraph(graph)) ;
    }

    /** Execute a single SPARQL Update operation.
     * @param update
     * @param dataset
     */
    public static void execute(Update update, Dataset dataset)
    {
        execute(update, dataset.asDatasetGraph()) ;
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
     * @param dataset
     * @param inputBinding
     */
    public static void execute(Update update, Dataset dataset, QuerySolution inputBinding)
    {
        execute(update, dataset.asDatasetGraph(), BindingLib.asBinding(inputBinding)) ;
    }

    /** Execute a single SPARQL Update operation.
     * @param update
     * @param datasetGraph
     * @param inputBinding
     */
    public static void execute(Update update, DatasetGraph datasetGraph, Binding inputBinding)
    {
        execute$(update, datasetGraph, inputBinding) ;
    }

    private static void execute$(Update update, DatasetGraph datasetGraph, Binding inputBinding)
    {
        UpdateRequest request = new UpdateRequest() ;
        request.add(update) ;
        execute$(request, datasetGraph, inputBinding) ;
    }

    // Streaming Updates:

    /** Parse update operations into a DatasetGraph by reading it from a file */
    public static void parseExecute(UsingList usingList, DatasetGraph dataset, String fileName)
    {
        parseExecute(usingList, dataset, fileName, null, Syntax.defaultUpdateSyntax) ;
    }

    /** Parse update operations into a DatasetGraph by reading it from a file */
    public static void parseExecute(UsingList usingList, DatasetGraph dataset, String fileName, Syntax syntax)
    {
        parseExecute(usingList, dataset, fileName, null, syntax) ;
    }

    /** Parse update operations into a DatasetGraph by reading it from a file */
    public static void parseExecute(UsingList usingList, DatasetGraph dataset, String fileName, String baseURI, Syntax syntax)
    {
        parseExecute(usingList, dataset, fileName, (Binding)null, baseURI, syntax);
    }

    /** Parse update operations into a DatasetGraph by reading it from a file */
    public static void parseExecute(UsingList usingList, DatasetGraph dataset, String fileName, QuerySolution inputBinding, String baseURI, Syntax syntax)
    {
        parseExecute(usingList, dataset, fileName, BindingLib.asBinding(inputBinding), baseURI, syntax) ;
    }

    /** Parse update operations into a DatasetGraph by reading it from a file */
    @SuppressWarnings("resource")
    public static void parseExecute(UsingList usingList, DatasetGraph dataset, String fileName, Binding inputBinding, String baseURI, Syntax syntax)
    {
        InputStream in = null ;
        if ( fileName.equals("-") )
            in = System.in ;
        else {
            in = IO.openFile(fileName) ;
            if ( in == null )
                throw new UpdateException("File could not be opened: "+fileName) ;
        }
        parseExecute(usingList, dataset, in, inputBinding, baseURI, syntax) ;
        if ( in != System.in )
            IO.close(in) ;
    }

    /**
     * Parse update operations into a DatasetGraph by parsing from an InputStream.
     * @param usingList A list of USING or USING NAMED statements that be added to all {@link UpdateWithUsing} queries
     * @param input     The source of the update request (must be UTF-8).
     */
    public static void parseExecute(UsingList usingList, DatasetGraph dataset, InputStream input)
    {
        parseExecute(usingList, dataset, input, Syntax.defaultUpdateSyntax) ;
    }

    /**
     * Parse update operations into a DatasetGraph by parsing from an InputStream.
     * @param usingList A list of USING or USING NAMED statements that be added to all {@link UpdateWithUsing} queries
     * @param input     The source of the update request (must be UTF-8).
     * @param syntax    The update language syntax
     */
    public static void parseExecute(UsingList usingList, DatasetGraph dataset, InputStream input, Syntax syntax)
    {
        parseExecute(usingList, dataset, input, null, syntax) ;
    }

    /**
     * Parse update operations into a DatasetGraph by parsing from an InputStream.
     * @param usingList A list of USING or USING NAMED statements that be added to all {@link UpdateWithUsing} queries
     * @param input     The source of the update request (must be UTF-8).
     * @param baseURI   The base URI for resolving relative URIs.
     */
    public static void parseExecute(UsingList usingList, DatasetGraph dataset, InputStream input, String baseURI)
    {
        parseExecute(usingList, dataset, input, baseURI, Syntax.defaultUpdateSyntax) ;
    }

    /**
     * Parse update operations into a DatasetGraph by parsing from an InputStream.
     * @param usingList A list of USING or USING NAMED statements that be added to all {@link UpdateWithUsing} queries
     * @param dataset   The dataset to apply the changes to
     * @param input     The source of the update request (must be UTF-8).
     * @param baseURI   The base URI for resolving relative URIs (may be <code>null</code>)
     * @param syntax    The update language syntax
     */
    public static void parseExecute(UsingList usingList, DatasetGraph dataset, InputStream input, String baseURI, Syntax syntax)
    {
        parseExecute(usingList, dataset, input, (Binding)null, baseURI, syntax);
    }

    /**
     * Parse update operations into a DatasetGraph by parsing from an InputStream.
     * @param usingList    A list of USING or USING NAMED statements that be added to all {@link UpdateWithUsing} queries
     * @param dataset      The dataset to apply the changes to
     * @param input        The source of the update request (must be UTF-8).
     * @param inputBinding Initial binding to be applied to Update operations that can apply an initial binding
     *                     (i.e. UpdateDeleteWhere, UpdateModify).  May be <code>null</code>
     * @param baseURI      The base URI for resolving relative URIs (may be <code>null</code>)
     * @param syntax       The update language syntax
     */
    public static void parseExecute(UsingList usingList, DatasetGraph dataset, InputStream input, QuerySolution inputBinding, String baseURI, Syntax syntax)
    {
        parseExecute(usingList, dataset, input, BindingLib.asBinding(inputBinding), baseURI, syntax) ;
    }

    /**
     * Parse update operations into a DatasetGraph by parsing from an InputStream.
     * @param usingList    A list of USING or USING NAMED statements that be added to all {@link UpdateWithUsing} queries
     * @param dataset      The dataset to apply the changes to
     * @param input        The source of the update request (must be UTF-8).
     * @param inputBinding Initial binding to be applied to Update operations that can apply an initial binding
     *                     (i.e. UpdateDeleteWhere, UpdateModify).  May be <code>null</code>
     * @param baseURI      The base URI for resolving relative URIs (may be <code>null</code>)
     * @param syntax       The update language syntax
     */
    public static void parseExecute(UsingList usingList, DatasetGraph dataset, InputStream input, Binding inputBinding, String baseURI, Syntax syntax)
    {
        @SuppressWarnings("deprecation")
        UpdateProcessorStreaming uProc = UpdateExecutionFactory.createStreaming(dataset, inputBinding) ;
        if (uProc == null)
            throw new ARQException("No suitable update procesors are registered/able to execute your updates");

        uProc.startRequest();
        try
        {
            UpdateSink sink = new UsingUpdateSink(uProc.getUpdateSink(), usingList) ;
            try
            {
                UpdateParser parser = UpdateFactory.setupParser(uProc.getPrologue(), baseURI, syntax) ;
                parser.parse(sink, uProc.getPrologue(), input) ;
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
