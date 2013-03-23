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

package org.apache.jena.riot;

import java.io.* ;
import java.util.Iterator ;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.io.PeekReader ;
import org.apache.jena.atlas.json.io.parser.TokenizerJSON ;
import org.apache.jena.atlas.web.ContentType ;
import org.apache.jena.atlas.web.TypedInputStream ;
import org.apache.jena.riot.lang.LangRDFXML ;
import org.apache.jena.riot.lang.LangRIOT ;
import org.apache.jena.riot.stream.StreamManager ;
import org.apache.jena.riot.system.* ;
import org.apache.jena.riot.tokens.Tokenizer ;
import org.apache.jena.riot.tokens.TokenizerFactory ;
import org.apache.jena.riot.writer.NQuadsWriter ;
import org.apache.jena.riot.writer.NTriplesWriter ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.query.DatasetFactory ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.DatasetGraphFactory ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.graph.GraphFactory ;
import com.hp.hpl.jena.sparql.util.Context ;
import com.hp.hpl.jena.sparql.util.Symbol ;
import com.hp.hpl.jena.sparql.util.Utils ;

/** <p>General purpose reader framework for RDF (triples and quads) syntaxes.</p>   
 *  <ul>
 *  <li>HTTP Content negotiation</li>
 *  <li>File type hint by the extension</li>
 *  <li>Application language hint</li>
 *  </ul>
 * <p>
 *  It also provides a way to lookup names in different
 *  locations and to remap URIs to other URIs. 
 *  </p>
 *  <p>
 *  Extensible - a new syntax can be added to the framework. 
 *  </p>
 *  <p>Operations fall into the follwoing categories:</p>
 *  <ul>
 *  <li>{@code read} -- Read data from a location into a Model/Dataset etc</li>
 *  <li>{@code loadXXX} -- Read data and return an in-memory object holding the data.</li>
 *  <li>{@code parse} -- Read data and send to an {@link StreamRDF}</li>
 *  <li>{@code open}  -- Open a typed input stream to the location, using any alternative locations</li>
 *  </ul> 
 */

public class RDFDataMgr
{
    static { RIOT.init() ; }
    /* Maybe:
     * static for global (singleton) and locally tailored. 
     */
    
    static Logger log = LoggerFactory.getLogger(RDFDataMgr.class) ;
    private static String riotBase = "http://jena.apache.org/riot/" ; 
    private static String streamManagerSymbolStr = riotBase+"streamManager" ; 
    public static Symbol streamManagerSymbol = Symbol.create(streamManagerSymbolStr) ; 
    
    /** Read triples into a Model from the given location. 
     *  The syntax is detemined from input source URI (content negotiation or extension). 
     * @param model Destination for the RDF read.
     * @param uri   URI to read from (includes file: and a plain file name).
     * @throws RiotNotFoundException if the location is not found - the model is unchanged.
     * @see #read(Model,String,Lang,Context) 
     */
    public static void read(Model model, String uri)                    { read(model.getGraph(), uri) ; }
    
    /** Read triples into a Model from the given location. 
     *  The syntax is detemined from input source URI (content negotiation or extension). 
     * @param graph Destination for the RDF read.
     * @param uri   URI to read from (includes file: and a plain file name).
     * @throws RiotNotFoundException if the location is not found - the graph is unchanged.
     * @see #read(Graph,String,Lang,Context) 
     */
    public static void read(Graph graph, String uri)                    { read(graph, uri, null, null, null) ; }

    /** Read triples into a Model from the given location, with a hint of the language (MIME type) 
     * @param model     Destination for the RDF read.
     * @param uri       URI to read from (includes file: and a plain file name).
     * @param hintLang  Hint for the syntax.
     * @throws RiotNotFoundException if the location is not found - the model is unchanged.
     * @see #read(Model,String,String,Lang,Context) 
     */
    public static void read(Model model, String uri, Lang hintLang)    { read(model.getGraph(), uri, hintLang) ; } 
    
    /** Read triples into a Model from the given location, with a hint of the language (MIME type or short name) 
     * @param graph     Destination for the RDF read.
     * @param uri       URI to read from (includes file: and a plain file name).
     * @param hintLang  Hint for the syntax.
     * @throws RiotNotFoundException if the location is not found - the graph is unchanged.
     * @see #read(Graph,String,Lang,Context) 
     */
    public static void read(Graph graph, String uri, Lang hintLang)    { read(graph, uri, hintLang, null) ; }
    
    /** Read triples into a Model from the given location, with hint of language and with some parameters for the reader 
     * @see #read(Model,String,String,Lang,Context) 
     * Throws parse errors depending on the language and reader; the model may be partially updated.
     * @param model     Destination for the RDF read.
     * @param uri       URI to read from (includes file: and a plain file name).
     * @param base      Base URI (defaults to uri).
     * @param hintLang  Hint for the syntax
     * @throws RiotNotFoundException if the location is not found - the model is unchanged.
     */
    public static void read(Model model, String uri, String base, Lang hintLang) { read(model.getGraph(), uri, base, hintLang) ; }
    
    /** Read triples into a Model from the given location, with hint of language and the with some parameters for the reader 
     * @see #read(Graph,String,String,Lang,Context) 
     * Throws parse errors depending on the language and reader; the Model model may be partially updated.
     * @param graph     Destination for the RDF read.
     * @param uri       URI to read from (includes file: and a plain file name).
     * @param base      Base URI (defaults to uri).
     * @param hintLang  Hint for the syntax
     * @throws RiotNotFoundException if the location is not found - the graph is unchanged.
     */
    public static void read(Graph graph, String uri, String base, Lang hintLang) { read(graph, uri, base, hintLang, null) ; }
    
    /** Read triples into a Model from the given location, with some parameters for the reader
     * @see #read(Model,String,String,Lang,Context) 
     * @param model     Destination for the RDF read
     * @param uri       URI to read from (includes file: and a plain file name).
     * @param context   Content object to control reading process.
     * @throws RiotNotFoundException if the location is not found - the model is unchanged.
     */ 
    public static void read(Model model, String uri, Context context)   { read(model.getGraph(), uri, context) ; }
    
    /** Read triples into a Model from the given location, with some parameters for the reader
     * @see #read(Graph,String,String,Lang,Context) 
     * @param graph     Destination for the RDF read
     * @param uri       URI to read from (includes file: and a plain file name).
     * @param context   Content object to control reading process.
     * @throws RiotNotFoundException if the location is not found - the graph is unchanged.
     */ 
    public static void read(Graph graph, String uri, Context context)   { read(graph, uri, null, context) ; }
    
    /** Read triples into a Model from the given location, with hint of language and the with some parameters for the reader 
     * @see #read(Model,String,String,Lang,Context) 
     * @param model     Destination for the RDF read
     * @param uri       URI to read from (includes file: and a plain file name).
     * @param hintLang  Hint for the syntax
     * @param context   Content object to control reading process.
     * @throws RiotNotFoundException if the location is not found - the model is unchanged.
     */
    public static void read(Model model, String uri, Lang hintLang, Context context)
    { read(model, uri, uri, hintLang, context) ; }
    
    /** Read triples into a Model from the given location, with hint of language and with some parameters for the reader 
     * @see #read(Graph,String,String,Lang,Context) 
     * @param graph     Destination for the RDF read
     * @param uri       URI to read from (includes file: and a plain file name).
     * @param hintLang  Hint for the syntax
     * @param context   Content object to control reading process.
     * @throws RiotNotFoundException if the location is not found - the graph is unchanged.
    */
    public static void read(Graph graph, String uri, Lang hintLang, Context context)
    { read(graph, uri, uri, hintLang, context) ; }
    
    /** Read triples into a Model from the given location, with hint of language 
     * and with some parameters for the reader. 
     * Throws parse errors depending on the language and reader; the model may be partially updated.
     * @param model     Destination for the RDF read.
     * @param uri       URI to read from (includes file: and a plain file name).
     * @param base      Base URI (defaults to uri).
     * @param hintLang  Hint for the syntax
     * @param context   Content object to control reading process.
     * @throws RiotNotFoundException if the location is not found - the model is unchanged.
     */
    public static void read(Model model, String uri, String base, Lang hintLang, Context context)
	{ read(model.getGraph(), uri, base, hintLang, context) ; }

    /** Read triples into a Model from the given location, with hint of language and the with some parameters for the reader 
     * Throws parse errors depending on the language and reader; the graph may be partially updated.
     * @param graph     Destination for the RDF read.
     * @param uri       URI to read from (includes file: and a plain file name).
     * @param base      Base URI (defaults to uri).
     * @param hintLang  Hint for the syntax
     * @param context   Content object to control reading process.
     * @throws RiotNotFoundException if the location is not found - the model is unchanged.
     */
    public static void read(Graph graph, String uri, String base, Lang hintLang, Context context)
    {
        StreamRDF dest = StreamRDFLib.graph(graph) ;
        parse(dest, uri, base, hintLang, context) ;
    }

    /** Read triples into a Model with bytes from an InputStream.
     *  A base URI and a syntax can be provided.
     *  The base URI defualts to "no base" in which case the data should have no relative URIs.
     *  The lang gives the syntax of the stream. 
     * @param model     Destination for the RDF read.
     * @param in        InputStream
     * @param lang      Language syntax
     */
    public static void read(Model model, InputStream in, Lang lang)    { read(model.getGraph(), in, lang) ; }
        
    /** Read triples into a Model with bytes from an InputStream.
     *  A base URI and a syntax can be provided.
     *  The base URI defualts to "no base" in which case the data should have no relative URIs.
     *  The lang gives the syntax of the stream. 
     * @param graph     Destination for the RDF read.
     * @param in        InputStream
     * @param lang      Language syntax
     */
    public static void read(Graph graph, InputStream in, Lang lang)    { read(graph, in, null, lang) ; }

    /** Read triples into a Model with bytes from an InputStream.
     *  A base URI and a syntax can be provided.
     *  The base URI defualts to "no base" in which case the data should have no relative URIs.
     *  The lang gives the syntax of the stream. 
     * @param model     Destination for the RDF read.
     * @param in        InputStream
     * @param base      Base URI 
     * @param lang      Language syntax
     */
    public static void read(Model model, InputStream in, String base, Lang lang)
    { read(model.getGraph(), in, base, lang) ; }

    /** Read triples into a Model with bytes from an InputStream.
     *  A base URI and a syntax can be provided.
     *  The base URI defualts to "no base" in which case the data should have no relative URIs.
     *  The lang gives the syntax of the stream. 
     * @param graph     Destination for the RDF read.
     * @param in        InputStream
     * @param base      Base URI 
     * @param lang      Language syntax
     */
    public static void read(Graph graph, InputStream in, String base, Lang lang)
    {
        StreamRDF dest = StreamRDFLib.graph(graph) ;
        process(dest, new TypedInputStream(in), base, lang, null) ;
    }

    /** Read triples into a model with chars from an Reader.
     * Use of java.io.Readers is not encouraged - use with a StringReader is the primary use case.
     * For files, open a {@link java.io.FileInputStream} to ensure correct character set handling.
     * @deprecated     Use an InputStream or StringReader.
     * @param model     Destination for the RDF read.
     * @param in        Reader
     * @param base      Base URI 
     * @param lang      Language syntax
     */
    @Deprecated
    public static void read(Model model, Reader in, String base, Lang lang)
    {
        read(model.getGraph(), in, base,  lang) ;
    }

    /** Read triples into a model with chars from an Reader.
     * Use of java.io.Readers is not encouraged - use with a StringReader is the primary use case.
     * For files, open a {@link java.io.FileInputStream} to ensure correct character set handling.
     * @deprecated     Use an InputStream or StringReader.
     * @param graph     Destination for the RDF read.
     * @param in        Reader
     * @param base      Base URI 
     * @param lang      Language syntax
     */
    @Deprecated
    public static void read(Graph graph, Reader in, String base, Lang lang)
    {
        StreamRDF dest = StreamRDFLib.graph(graph) ;
        processTriples(dest, base, in, lang, null) ;
    }

    /** Read triples into a model with chars from a StringReader.
     * @param model     Destination for the RDF read.
     * @param in        InputStream
     * @param base      Base URI 
     * @param lang      Language syntax
     */
    public static void read(Model model, StringReader in, String base, Lang lang)
    {
        Graph g = model.getGraph() ;
        StreamRDF dest = StreamRDFLib.graph(g) ;
        processTriples(dest, base, in, lang, null) ;
    }

    /** Read triples into a model with chars from a StringReader.
     * @param graph     Destination for the RDF read.
     * @param in        InputStream
     * @param base      Base URI 
     * @param lang      Language syntax
     */
    public static void read(Graph graph, StringReader in, String base, Lang lang)
    {
        StreamRDF dest = StreamRDFLib.graph(graph) ;
        processTriples(dest, base, in, lang, null) ;
    }
    
    private static Model createModel() { return ModelFactory.createDefaultModel() ; } 
    private static Graph createGraph() { return GraphFactory.createDefaultGraph() ; } 
    private static Dataset createDataset() { return DatasetFactory.createMem() ; } 
    private static DatasetGraph createDatasetGraph() { return DatasetGraphFactory.createMem() ; }
    
    // Load:
    // Macros would be nice.

    /** Create a memory Model and read in some data
     * @see #read(Model,String) 
     */ 
    public static Model loadModel(String uri)
    { 
        Model m = createModel() ;
        read(m, uri) ;
        return m ;
    }

    /** Create a memory Model and read in some data
     * @see #read(Model,String,Lang) 
     */
    public static Model loadModel(String uri, Lang lang)
	{
		Model m = createModel() ;
        read(m, uri,lang) ;
        return m ;
	}

    //public static Model loadModel(String uri, String base) { return null ; } 
    //public static Model loadModel(String uri, String base, Lang lang) { return null ; } 

    /** Create a memory Graph and read in some data
     * @see #read(Graph,String) 
     */ 
    public static Graph loadGraph(String uri)
	{ 
        Graph g = createGraph() ;
        read(g, uri) ;
        return g ;
    }

	/** Create a memory Graph and read in some data
     * @see #read(Graph,String,Lang) 
     */ 
    public static Graph loadGraph(String uri, Lang lang)
	{ 
        Graph g = createGraph() ;
        read(g, uri, lang) ;
        return g ;
    }

//  public static Graph loadGraph(String uri, String base) { return null ; } 
//  public static Graph loadGraph(String uri, String base, Lang lang) { return null ; } 

	/** Create a memory Dataset and read in some data
     * @see #read(Dataset,String) 
     */ 
    public static Dataset loadDataset(String uri)
	{ 
        Dataset ds = createDataset() ;
        read(ds, uri) ;
        return ds ;
    }

	/** Create a memory Dataset and read in some data
     * @see #read(Dataset,String,Lang) 
     */
    public static Dataset loadDataset(String uri, Lang lang)
	{
        Dataset ds = createDataset() ;
        read(ds, uri, lang) ;
        return ds ;
	}

//    public static Dataset loadDataset(String uri, String base) { return null ; } 
//    public static Dataset loadDataset(String uri, String base, Lang lang) { return null ; } 
//    public static Dataset loadDataset(String uri, String base, Lang lang, Context context) { return null ; } 

	/** Create a memory DatasetGraph and read in some data
     * @see #read(DatasetGraph,String) 
     */ 
    public static DatasetGraph loadDatasetGraph(String uri)
	{
		DatasetGraph ds = createDatasetGraph() ;
        read(ds, uri) ;
        return ds ;
	}
	/** Create a memory DatasetGraph and read in some data
     * @see #read(DatasetGraph,String,Lang) 
     */
    public static DatasetGraph loadDatasetGraph(String uri, Lang lang)
	{
		DatasetGraph ds = createDatasetGraph() ;
        read(ds, uri, lang) ;
        return ds ;	
	}

//    public static DatasetGraph loadDatasetGraph(String uri, String base) { return null ; } 
//    public static DatasetGraph loadDatasetGraph(String uri, String base, Lang lang) { return null ; } 
//    public static DatasetGraph loadDatasetGraph(String uri, String base, Lang lang, Context context) { return null ; } 
    
    /** Read quads or triples into a Dataset from the given location, with hint of language.
     * @see #read(Dataset, String, String, Lang, Context) 
     * @param dataset   Destination
     * @param uri       URI to read from (includes file: and a plain file name).
     */
    public static void read(Dataset dataset, String uri)
    {
        read(dataset.asDatasetGraph(), uri) ;
    }

    /** Read quads or triples into a Dataset from the given location, with hint of language.
     * @see #read(DatasetGraph, String, String, Lang, Context) 
     * @param dataset   Destination
     * @param uri       URI to read from (includes file: and a plain file name).
     */
    public static void read(DatasetGraph dataset, String uri)
    {
        read(dataset, uri, null, null) ;
    }

    /** Read quads or triples into a Dataset from the given location, with hint of language.
     * @see #read(Dataset, String, String, Lang, Context) 
     * @param dataset   Destination
     * @param uri       URI to read from (includes file: and a plain file name).
     * @param hintLang  Language syntax
     */
    public static void read(Dataset dataset, String uri, Lang hintLang)
    {
        read(dataset.asDatasetGraph(), uri, hintLang) ;
    }

    /** Read quads or triples into a Dataset from the given location, with hint of language.
     * @see #read(DatasetGraph, String, String, Lang, Context) 
     * @param dataset   Destination
     * @param uri       URI to read from (includes file: and a plain file name).
     * @param hintLang  Language syntax
     */
    public static void read(DatasetGraph dataset, String uri, Lang hintLang)
    {
        read(dataset, uri, hintLang, null) ;
    }

    /** Read quads or triples into a Dataset from the given location. 
     * @see #read(Dataset, String, String, Lang, Context) 
     * @param dataset   Destination
     * @param uri       URI to read from (includes file: and a plain file name).
     * @param hintLang  Language syntax
     */
    public static void read(Dataset dataset, String uri, Lang hintLang, Context context)
    {
        read(dataset.asDatasetGraph(), uri, hintLang, context) ;
    }
    
    /** Read quads or triples into a Dataset from the given location. 
     * @see #read(DatasetGraph, String, String, Lang, Context) 
     * @param dataset   Destination
     * @param uri       URI to read from (includes file: and a plain file name).
     * @param hintLang  Language syntax
     */
    public static void read(DatasetGraph dataset, String uri, Lang hintLang, Context context)
    {
        read(dataset, uri, uri, hintLang, context) ;
    }
    
    /** Read quads or triples into a Dataset from the given location.
     * @see #read(Dataset, String, String, Lang, Context) 
     * @param dataset   Destination
     * @param uri       URI to read from (includes file: and a plain file name).
     * @param hintLang  Language syntax
	 * @param context   Context for the reader
	 * @throws RiotNotFoundException if the location is not found - the dataset is unchanged.
	 * Throws parse errors depending on the language and reader; the dataset may be partially updated. 
	 */ 

    public static void read(Dataset dataset, String uri, String base, Lang hintLang, Context context)
    {
		read(dataset.asDatasetGraph(), uri, uri, hintLang, context) ;
    }

    /** Read quads or triples into a Dataset from the given location.
     * @see #read(Dataset, String, String, Lang, Context) 
     * @param dataset   Destination
     * @param uri       URI to read from (includes file: and a plain file name).
     * @param hintLang  Language syntax
	 * @param context   Context for the reader
	 * @throws RiotNotFoundException if the location is not found - the dataset is unchanged.
	 * Throws parse errors depending on the language and reader; the dataset may be partially updated. 
	 */ 

    public static void read(DatasetGraph dataset, String uri, String base, Lang hintLang, Context context)
    {
        StreamRDF sink = StreamRDFLib.dataset(dataset) ;
        parse(sink, uri, base, hintLang, context) ;
    }

    /** Read quads or triples into a dataset with bytes from an input stream.
     * @param dataset   Destination
     * @param in        InputStream
     * @param lang      Language syntax
     */
    public static void read(Dataset dataset, InputStream in, Lang lang)
    {
        read(dataset.asDatasetGraph(), in, lang) ;
    }
    
    /** Read quads or triples into a dataset with bytes from an input stream.
     * @param dataset   Destination
     * @param in        InputStream
     * @param lang      Language syntax
     */
    public static void read(DatasetGraph dataset, InputStream in, Lang lang)
    {
        read(dataset, in, null, lang) ;
    }
    
    /** Read quads or triples into a dataset with bytes from an input stream.
     * @param dataset   Destination
     * @param in        InputStream
     * @param base      Base URI
     * @param lang      Language syntax
     */
    public static void read(Dataset dataset, InputStream in, String base, Lang lang)
    {
        read(dataset.asDatasetGraph(), in, base, lang) ; 
    }
    
    /** Read quads or triples into a dataset with bytes from an input stream.
     * @param dataset   Destination
     * @param in        InputStream
     * @param base      Base URI
     * @param lang      Language syntax
     */
    public static void read(DatasetGraph dataset, InputStream in, String base, Lang lang)
    {
        StreamRDF dest = StreamRDFLib.dataset(dataset) ;
        process(dest, new TypedInputStream(in), base, lang, null) ;
    }
    
    /** Read quads into a dataset with chars from an Reader.
     * Use java.io.Readers is not encouraged - use with a StringReader is the primary use case.
     * For files, open a {@link java.io.FileInputStream} to ensure correct character set handling.
     * @param dataset   Destination
     * @param in        InputStream
     * @param base      Base URI
     * @param lang      Language syntax
     * @deprecated use an InputStream or a StringReader.
     */
    @Deprecated
    public static void read(Dataset dataset, Reader in, String base, Lang lang)
    {
		read(dataset.asDatasetGraph(), in, base, lang) ;
    }

    /** Read quads into a dataset with chars from an Reader.
     * Use java.io.Readers is not encouraged - use with a StringReader is the primary use case.
     * For files, open a {@link java.io.FileInputStream} to ensure correct character set handling.
     * @param dataset   Destination
     * @param in        InputStream
     * @param base      Base URI
     * @param lang      Language syntax
     * @deprecated use an InputStream or a StringReader.
     */
    @Deprecated
    public static void read(DatasetGraph dataset, Reader in, String base, Lang lang)
    {
        StreamRDF dest = StreamRDFLib.dataset(dataset) ;
        process(dest, base, in, lang, null) ;
    }

    /** Read quads into a dataset with chars from a StringReader.
     * Use java.io.Readers is not encouraged - use with a StringReader is the primary use case.
     * For files, open a {@link java.io.FileInputStream} to ensure correct character set handling.
     * @param dataset   Destination
     * @param in        InputStream
     * @param base      Base URI
     * @param lang      Language syntax
     */
    public static void read(Dataset dataset, StringReader in, String base, Lang lang)
    {
        read(dataset.asDatasetGraph(), in, base, lang) ;
    }

    /** Read quads into a dataset with chars from a StringReader.
     * Use java.io.Readers is not encouraged - use with a StringReader is the primary use case.
     * For files, open a {@link java.io.FileInputStream} to ensure correct character set handling.
     * @param dataset   Destination
     * @param in        InputStream
     * @param base      Base URI
     * @param lang      Language syntax
     */
    public static void read(DatasetGraph dataset, StringReader in, String base, Lang lang)
    {
        StreamRDF dest = StreamRDFLib.dataset(dataset) ;
        process(dest, base, in, lang, null) ;
    }

    /** Read RDF data.
     * @param sink     Destination for the RDF read.
     * @param uri       URI to read from (includes file: and a plain file name).
     */
    public static void parse(StreamRDF sink, String uri)
    {
        parse(sink, uri, null, null) ;
    }

    /** Read RDF data.
     * @param sink     Destination for the RDF read.
     * @param uri       URI to read from (includes file: and a plain file name).
     * @param hintLang  Hint for the syntax
     * @param context   Content object to control reading process.
     */
    public static void parse(StreamRDF sink, String uri, Lang hintLang, Context context)
    {
        parse(sink, uri, uri, hintLang, context) ;
    }

    /** Read RDF data.
     * @param sink     Destination for the RDF read.
     * @param uri       URI to read from (includes file: and a plain file name).
     * @param base      Base URI (defaults to uri).
     * @param hintLang  Hint for the syntax
     * @param context   Content object to control reading process.
     */
    public static void parse(StreamRDF sink, String uri, String base, Lang hintLang, Context context)
    {
        if ( uri == null )
            throw new IllegalArgumentException("URI to read from is null") ;
        if ( base == null )
            base = IRIResolver.resolveFileURL(uri) ;
        if ( hintLang == null )
            hintLang = RDFLanguages.filenameToLang(uri) ;
        TypedInputStream in = open(uri, context) ;
        if ( in == null )
            throw new RiotException("Not found: "+uri) ;
        process(sink, in, base, hintLang, context) ;
        IO.close(in) ;
    }

    /** Read RDF data.
     * @param sink      Destination for the RDF read.
     * @param in        Bytes to read.
     * @param base      Base URI (defaults to uri).
     * @param hintLang  Hint for the syntax
     */
    public static void parse(StreamRDF sink, InputStream in, String base, Lang hintLang)
    {
        parse(sink, in, base, hintLang, null) ;  
    }

    /** Read RDF data.
     * @param sink      Destination for the RDF read.
     * @param in        Bytes to read.
     * @param base      Base URI (defaults to uri).
     * @param hintLang  Hint for the syntax
     * @param context   Content object to control reading process.
     */
    public static void parse(StreamRDF sink, InputStream in, String base, Lang hintLang, Context context)
    {
        process(sink, new TypedInputStream(in), base, hintLang, context) ;
    }

    /** Read RDF data.
     * @param sink      Destination for the RDF read.
     * @param in        Bytes to read.
     * @param base      Base URI
     */
    public static void parse(StreamRDF sink, TypedInputStream in, String base)
    {
        Lang hintLang = RDFLanguages.contentTypeToLang(in.getMediaType()) ;
        process(sink, new TypedInputStream(in), base, hintLang, null) ;
    }

    /** Open a stream to the destination (URI or filename)
     * Performs content negotiation, including looking at file extension.
     * @param filenameOrURI
     * @return TypedInputStream 
     */
    public static TypedInputStream open(String filenameOrURI)
    { return open(filenameOrURI, (Context)null) ; }
    
    /** Open a stream to the destination (URI or filename)
     * Performs content negotiation, including looking at file extension. 
     * @param filenameOrURI
     * @param context
     * @return TypedInputStream
     */
    public static TypedInputStream open(String filenameOrURI, Context context)
    {
        StreamManager sMgr = StreamManager.get() ;
        if ( context != null )
        {
            try { sMgr = (StreamManager)context.get(streamManagerSymbol, context) ; }
            catch (ClassCastException ex) 
            { log.warn("Context symbol '"+streamManagerSymbol+"' is not a "+Utils.classShortName(StreamManager.class)) ; }
        }
        
        return open(filenameOrURI, sMgr) ;
    }
    
    /** Open a stream to the destination (URI or filename)
     * Performs content negotiation, including looking at file extension. 
     * @param filenameOrURI
     * @param streamManager
     * @return TypedInputStream
     */
    public static TypedInputStream open(String filenameOrURI, StreamManager streamManager)
    {
        TypedInputStream in = streamManager.open(filenameOrURI) ;
            
        if ( in == null )
        {
            if ( log.isDebugEnabled() )
                //log.debug("Found: "+filenameOrURI+" ("+loc.getName()+")") ;
                log.debug("Not Found: "+filenameOrURI) ;
            throw new RiotNotFoundException("Not found: "+filenameOrURI) ;
            //return null ;
        }
        if ( log.isDebugEnabled() )
            //log.debug("Found: "+filenameOrURI+" ("+loc.getName()+")") ;
            log.debug("Found: "+filenameOrURI) ;
        return in ;
    }
    
    // ----- 
    // Readers are algorithms and must be stateless (or they must create a per run
    // instance of something) because they may be called concurrency from different threads.
    // The Context Readerobject gives the per-run configuration.  
    
    // Alternative: A two step factory-instance design means
    // readers can be created and passed around (e,.g. to set specific features)
    // We could have had two step design - ReaderFactory-ReaderInstance
    // no - put the bruden on complicated readers, not everyone. 
    
    private static void process(StreamRDF destination, TypedInputStream in, String baseUri, Lang hintLang, Context context )
    {
        ContentType ct = determineCT(baseUri, in.getContentType(), hintLang ) ;
        if ( ct == null )
            throw new RiotException("Failed to determine the triples content type: (URI="+baseUri+" : stream="+in.getContentType()+" : hint="+hintLang+")") ;

        ReaderRIOT reader = getReader(ct) ;
        if ( reader == null )
            throw new RiotException("No triples reader for content type: "+ct.getContentType()) ;
        reader.read(in, baseUri, ct, destination, context) ;
    }

    private static ReaderRIOT getReader(ContentType ct)
    {
        Lang lang = RDFLanguages.contentTypeToLang(ct) ;
        ReaderRIOTFactory r = RDFParserRegistry.getFactory(lang) ;
        if ( r == null )
            return null ;
        return r.create(lang) ;
    }

    // With sink and ither ParserOutput
    
    // java.io.Readers are NOT preferred.
    @SuppressWarnings("deprecation")
    private static void processTriples(StreamRDF output, String base, Reader in, Lang lang, Context context)
    {
        // Not as good as from an InputStream - RDF/XML not supported 
        ContentType ct = determineCT(base, null, lang) ;
        if ( ct == null )
            throw new RiotException("Failed to determine the triples content type: (URI="+base+" : hint="+lang+")") ;
        LangRIOT parser ;
        if ( lang == null )
            throw new RiotException("No language specificied") ;

        if ( RDFLanguages.sameLang(RDFLanguages.RDFXML, lang) )
            parser = LangRDFXML.create(in, base, base, ErrorHandlerFactory.errorHandlerStd, output) ;
        else
        {
            Tokenizer tokenizer =
                RDFLanguages.RDFJSON.equals(lang)?
                              new TokenizerJSON(PeekReader.make(in)) :   
                              TokenizerFactory.makeTokenizer(in) ;
            parser = RiotReader.createParser(tokenizer, lang, base, output) ;
        }
        parser.parse() ;
    }
    
    // java.io.Readers are NOT preferred.
    private static void process(StreamRDF dest, String base, Reader in, Lang hintLang, Context context)
    {
        Tokenizer tokenizer = TokenizerFactory.makeTokenizer(in) ;
        LangRIOT parser = RiotReader.createParser(tokenizer, hintLang, base, dest) ;
        parser.parse() ;
    }

    private static ContentType determineCT(String target, String ctStr, Lang hintLang)
    {
        boolean isTextPlain = WebContent.contentTypeTextPlain.equals(ctStr) ;
        
        if ( ctStr != null )
            ctStr = WebContent.contentTypeCanonical(ctStr) ;
        ContentType ct = (ctStr==null) ? null : ContentType.parse(ctStr) ;
        
        // If it's text plain, we ignore it because a lot of naive
        // server setups return text/plain for any file type.
        // We use the file extension.
        
        if ( ct == null || isTextPlain )
            ct = RDFLanguages.guessContentType(target) ;
        
        if ( ct == null && hintLang != null ) 
            ct = hintLang.getContentType() ;
        return ct ;
    }
    
    // -------- WRITERS
    
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

    /** Write an iterator of triples (in N-Triples)
     * @param out
     * @param iterator
     */
    public static void writeTriples(OutputStream out, Iterator<Triple> iterator)
    {
        NTriplesWriter.write(out, iterator) ;        
    }
    

    /** Write an iterator of quads (in N-Quads)
     * @param out
     * @param iterator
     */
    public static void writeQuads(OutputStream out, Iterator<Quad> iterator)
    {
        NQuadsWriter.write(out, iterator) ;        
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
        w.write(out, graph, RiotLib.prefixMap(graph), null, null) ;
    }

    private static void write$(Writer out, Graph graph, RDFFormat serialization)
    {
        WriterGraphRIOT w = createGraphWriter$(serialization) ;
        w.write(out, graph, RiotLib.prefixMap(graph), null, null) ;
    }

    private static void write$(OutputStream out, DatasetGraph dataset, RDFFormat serialization)
    {
        WriterDatasetRIOT w = createDatasetWriter$(serialization) ;
        w.write(out, dataset, RiotLib.prefixMap(dataset), null, null) ;
    }

    private static void write$(Writer out, DatasetGraph dataset, RDFFormat serialization)
    {
        WriterDatasetRIOT w = createDatasetWriter$(serialization) ;
        w.write(out, dataset, RiotLib.prefixMap(dataset), null, null) ;
    }
}

