/** ;
 * Licensed to the Apache Software Foundation (ASF) under 
one
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

package org.apache.jena.riot;

import java.io.OutputStream ;
import java.io.StringWriter ;
import java.io.Writer ;

import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RiotException ;
import org.apache.jena.riot.system.RiotWriterLib ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;

public class RDFWriterMgr
{
    /** Write the model to the output stream in the default serialization for the language.
     * @param out       OutputStream
     * @param model     Graph to write
     * @param lang      Language for the seralization.
     */
    public static void write(OutputStream out, Model model, Lang lang)
    {
        write(out, model.getGraph(), lang) ;
    }

    /** Write the model to the output stream in the default serialization for the language.
     * @param out           OutputStream
     * @param model         Model to write
     * @param serialization Serialization format
     */
    public static void write(OutputStream out, Model model, RDFFormat serialization)
    {
        write(out, model.getGraph(), serialization) ;
    }
    
    /** Write the graph to the output stream in the default serialization for the language.
     * @param out           OutputStream
     * @param model         Model to write
     * @param lang          Serialization format
     */
    public static void write(StringWriter out, Model model, Lang lang)
    {
        write(out, model.getGraph(), lang) ;
    }
    
    /** Write the graph to the output stream in the default serialization for the language.
     * @param out           OutputStream
     * @param model         Model to write
     * @param serialization Serialization format
     */
    public static void write(StringWriter out, Model model, RDFFormat serialization)
    {
        write(out, model.getGraph(), serialization) ;
    }

    /** Write the graph to the output stream in the default serialization for the language.
     * @param out           OutputStream
     * @param model         Model to write
     * @param serialization Serialization format
     * @deprecated Use of writers is deprecated - use an OutputStream
     */
    @Deprecated
    public static void write(Writer out, Model model, RDFFormat serialization)
    {
        write(out, model.getGraph(), serialization) ;
    }

    /** Write the graph to the output stream in the default serialization for the language.
     * @param out       OutputStream
     * @param graph     Graph to write
     * @param lang      Language for the seralization.
     */
    public static void write(OutputStream out, Graph graph, Lang lang)
    {
        RDFFormat serialization = RDFWriterRegistry.defaultSerialization(lang) ;
        write(out, graph, serialization) ;
    }

    /** Write the graph to the output stream in the default serialization for the language.
     * @param out           OutputStream
     * @param graph         Graph to write
     * @param serialization Serialization format
     */
    public static void write(OutputStream out, Graph graph, RDFFormat serialization)
    {
        write$(out, graph, serialization) ;
    }

    /** Write the graph to the output stream in the default serialization for the language.
     * @param out           OutputStream
     * @param graph         Graph to write
     * @param lang          Serialization format
     */
    public static void write(StringWriter out, Graph graph, Lang lang)
    {
        // Only known reasonable use of a Writer
        write$(out, graph, RDFWriterRegistry.defaultSerialization(lang)) ;
    }

    /** Write the graph to the output stream in the default serialization for the language.
     * @param out           OutputStream
     * @param graph         Graph to write
     * @param serialization Serialization format
     */
    public static void write(StringWriter out, Graph graph, RDFFormat serialization)
    {
        // Only known reasonable use of a Writer
        write$(out, graph, serialization) ;
    }

    /** Write the graph to the output stream in the default serialization for the language.
     * @param out           OutputStream
     * @param graph         Graph to write
     * @param serialization Serialization format
     * @deprecated Use of writers is deprecated - use an OutputStream
     */
    @Deprecated
    public static void write(Writer out, Graph graph, RDFFormat serialization)
    {
        write$(out, graph, serialization) ;
    }
    
    /** Write the Dataset to the output stream in the default serialization for the language.
     * @param out       OutputStream
     * @param dataset   Dataset to write
     * @param lang      Language for the seralization.
     */
    public static void write(OutputStream out, Dataset dataset, Lang lang)
    {
        write(out, dataset.asDatasetGraph(), lang) ;
    }

    /** Write the graph to the output stream in the default serialization for the language.
     * @param out           OutputStream
     * @param dataset       Dataset to write
     * @param serialization Serialization format
     */
    public static void write(OutputStream out, Dataset dataset, RDFFormat serialization)
    {
        write(out, dataset.asDatasetGraph(), serialization) ;
    }

    /** Write the graph to the output stream in the default serialization for the language.
     * @param out           Writer
     * @param dataset       Dataset to write
     * @param serialization Serialization format
     */
    public static void write(StringWriter out, Dataset dataset, RDFFormat serialization)
    {
        write$(out, dataset.asDatasetGraph(), serialization) ;
    }

    /** Write the graph to the output stream in the default serialization for the language.
     * @param out           StringWriter
     * @param dataset       Dataset to write
     * @param lang      Language for the seralization.
     */
    public static void write(StringWriter out, Dataset dataset, Lang lang)
    {
        RDFFormat serialization = RDFWriterRegistry.defaultSerialization(lang) ;
        write$(out, dataset.asDatasetGraph(), serialization) ;
    }

    /** Write the graph to the output stream in the default serialization for the language.
     * @param out           Writer
     * @param dataset       Dataset to write
     * @param serialization Serialization format
     * @deprecated Use of writers is deprecated - use an OutputStream
     */
    @Deprecated
    public static void write(Writer out, Dataset dataset, RDFFormat serialization)
    {
        write$(out, dataset.asDatasetGraph(), serialization) ;
    }

    /** Write the DatasetGraph to the output stream in the default serialization for the language.
     * @param out       OutputStream
     * @param dataset   DatasetGraph to write
     * @param lang      Language for the seralization.
     */
    public static void write(OutputStream out, DatasetGraph dataset, Lang lang)
    {
        RDFFormat serialization = RDFWriterRegistry.defaultSerialization(lang) ;
        write(out, dataset, serialization) ;
    }

    /** Write the graph to the output stream in the default serialization for the language.
     * @param out           OutputStream
     * @param dataset       DatasetGraph to write
     * @param serialization Serialization format
     */
    public static void write(OutputStream out, DatasetGraph dataset, RDFFormat serialization)
    {
        write$(out, dataset, serialization) ;
    }

    /** Write the DatasetGraph to the output stream in the default serialization for the language.
     * @param out       StringWriter
     * @param dataset   DatasetGraph to write
     * @param lang      Language for the seralization.
     */
    public static void write(StringWriter out, DatasetGraph dataset, Lang lang)
    {
        RDFFormat serialization = RDFWriterRegistry.defaultSerialization(lang) ;
        write(out, dataset, serialization) ;
    }

    /** Write the graph to the output stream in the default serialization for the language.
     * @param out           StringWriter
     * @param dataset       DatasetGraph to write
     * @param serialization Serialization format
     */
    public static void write(StringWriter out, DatasetGraph dataset, RDFFormat serialization)
    {
        write$(out, dataset, serialization) ;
    }

    /** Write the graph to the output stream in the default serialization for the language.
     * @param out           Writer
     * @param dataset       DatasetGraph to write
     * @param serialization Serialization format
     * @deprecated Use of writers is deprecated - use an OutputStream
     */
    @Deprecated
    public static void write(Writer out, DatasetGraph dataset, RDFFormat serialization)
    {
        write$(out, dataset, serialization) ;
    }

    /** Create a writer for an RDF language
     * @param lang   Language for the seralization.
     * @return WriterGraphRIOT
     */
    
    public static WriterGraphRIOT createGraphWriter(Lang lang)
    {
        RDFFormat serialization = RDFWriterRegistry.defaultSerialization(lang) ;
        return createGraphWriter$(serialization) ;    
    }
    
    /** Create a writer for an RDF language
     * @param serialization Serialization format
     * @return WriterGraphRIOT
     */
    public static WriterGraphRIOT createGraphWriter(RDFFormat serialization)
    {
        return createGraphWriter$(serialization) ;    
    }

    /** Create a writer for an RDF language
     * @param lang   Language for the seralization.
     * @return WriterGraphRIOT
     */
    
    public static WriterDatasetRIOT createDatasetWriter(Lang lang)
    {
        RDFFormat serialization = RDFWriterRegistry.defaultSerialization(lang) ;
        return createDatasetWriter$(serialization) ;    
    }
    
    /** Create a writer for an RDF language
     * @param serialization Serialization format
     * @return WriterGraphRIOT
     */
    public static WriterDatasetRIOT createDatasetWriter(RDFFormat serialization)
    {
        return createDatasetWriter$(serialization) ;    
    }
    
    private static WriterGraphRIOT createGraphWriter$(RDFFormat serialization)
    {
        WriterGraphRIOTFactory wf = RDFWriterRegistry.getWriterGraphFactory(serialization) ;
        if ( wf == null )
            throw new RiotException("No graph writer for "+serialization) ; 
        return wf.create(serialization) ;
    }

    private static WriterDatasetRIOT createDatasetWriter$(RDFFormat serialization)
    {
        WriterDatasetRIOTFactory wf = RDFWriterRegistry.getWriterDatasetFactory(serialization) ;
        if ( wf == null )
            throw new RiotException("No dataset writer for "+serialization) ; 
        return wf.create(serialization) ;
    }

    private static void write$(OutputStream out, Graph graph, RDFFormat serialization)
    {
        WriterGraphRIOT w = createGraphWriter$(serialization) ;
        w.write(out, graph, RiotWriterLib.prefixMap(graph), null, null) ;
    }

    private static void write$(Writer out, Graph graph, RDFFormat serialization)
    {
        WriterGraphRIOT w = createGraphWriter$(serialization) ;
        w.write(out, graph, RiotWriterLib.prefixMap(graph), null, null) ;
    }

    private static void write$(OutputStream out, DatasetGraph dataset, RDFFormat serialization)
    {
        WriterDatasetRIOT w = createDatasetWriter$(serialization) ;
        w.write(out, dataset, RiotWriterLib.prefixMap(dataset), null, null) ;
    }

    private static void write$(Writer out, DatasetGraph dataset, RDFFormat serialization)
    {
        WriterDatasetRIOT w = createDatasetWriter$(serialization) ;
        w.write(out, dataset, RiotWriterLib.prefixMap(dataset), null, null) ;
    }
}

