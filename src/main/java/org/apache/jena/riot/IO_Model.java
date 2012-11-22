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

import static org.apache.jena.riot.IO_Jena.determineCT ;
import static org.apache.jena.riot.IO_Jena.open ;

import java.io.InputStream ;
import java.io.Reader ;
import java.io.StringReader ;

import org.openjena.atlas.lib.Sink ;
import org.openjena.atlas.web.ContentType ;
import org.openjena.riot.* ;
import org.openjena.riot.lang.LangRDFXML ;
import org.openjena.riot.lang.LangRIOT ;
import org.openjena.riot.lang.SinkTriplesToGraph ;
import org.openjena.riot.tokens.Tokenizer ;
import org.openjena.riot.tokens.TokenizerFactory ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.sparql.util.Context ;

/** <p>General purpose reader framework for RDF triple syntaxes.</p>   
 *  <ul>
 *  <li>HTTP Content negotiation</li>
 *  <li>File type hint by the extension</li>
 *  <li>Application language hint</li>
 *  </ul>
 * <p>
 *  It also provide a way to lookup names in different
 *  locations and to remap URIs to other URIs. 
 *  </p>
 *  <p>
 *  Extensible - a new syntax can be added to the framework. 
 *  </p>
 *  
 *  @See WebReader2
 */

public class IO_Model
{
    /** Read triples into a Model from the given location. 
     *  The synatx is detemined from input source URI (content negotiation or extension). 
     * @see #read(Model,String,Lang2,Context) 
     * @param model Destination for the RDF read.
     * @param uri   URI to read from (includes file: and a plain file name).
     */
    public static void read(Model model, String uri)                    { read(model, uri, null, null, null) ; }
    
    /** Read triples into a Model from the given location, with a hint of the language (MIME type) 
     * @see #read(Model,String,String,Lang2,Context) 
     * @param model     Destination for the RDF read.
     * @param uri       URI to read from (includes file: and a plain file name).
     * @param hintLang  Hint for the syntax.
     */
    public static void read(Model model, String uri, Lang2 hintLang)   { read(model, uri, hintLang, null) ; }
    
    /** Read triples into a Model from the given location, with hint of langauge and the with some parameters for the reader 
     * @see #read(Model,String,String,Lang2,Context) 
     * Throws parse errors depending on the language and reader; the Model model may be partially updated.
     * @param model     Destination for the RDF read.
     * @param uri       URI to read from (includes file: and a plain file name).
     * @param base      Base URI (defaults to uri).
     * @param hintLang  Hint for the syntax
     * @throws RiotNotFoundException if the location is not found - the model is unchanged.
     */
    public static void read(Model model, String uri, String base, Lang2 hintLang) { read(model, uri, base, hintLang, null) ; }
    
    /** Read triples into a Model from the given location, with some parameters for the reader
     * @see #read(Model,String,String,Lang2,Context) 
     * @param model     Destination for the RDF read
     * @param uri       URI to read from (includes file: and a plain file name).
     * @param context   Content object to control reading process.
     */ 
    public static void read(Model model, String uri, Context context)   { read(model, uri, null, context) ; }
    
    /** Read triples into a Model from the given location, with hint of langauge and the with some parameters for the reader 
     * @see #read(Model,String,String,Lang2,Context) 
     * @param model     Destination for the RDF read
     * @param uri       URI to read from (includes file: and a plain file name).
     * @param hintLang  Hint for the syntax
     * @param context   Content object to control reading process.
    */
    public static void read(Model model, String uri, Lang2 hintLang, Context context)
    {
        read(model, uri, uri, hintLang, context) ;
    }
    
    /** Read triples into a Model from the given location, with hint of langauge and the with some parameters for the reader 
     * Throws parse errors depending on the language and reader; the Model model may be partially updated.
     * @param model     Destination for the RDF read.
     * @param uri       URI to read from (includes file: and a plain file name).
     * @param base      Base URI (defaults to uri).
     * @param hintLang  Hint for the syntax
     * @param context   Content object to control reading process.
     * @throws RiotNotFoundException if the location is not found - the model is unchanged.
     */
    public static void read(Model model, String uri, String base, Lang2 hintLang, Context context)
    {
        Graph g = model.getGraph() ;
        Sink<Triple> sink = new SinkTriplesToGraph(g) ;
        readTriples(sink, uri, base, hintLang, context) ;
    }

    /** Read triples into a Model with bytes from an InputStream.
     *  A base URI and a syntax can be provided.
     *  The base URI defualts to "no base" in which case the data should have no relative URIs.
     *  The lang gives the syntax of the stream. 
     * @param model     Destination for the RDF read.
     * @param in        InputStream
     * @param lang      Language syntax
     */

    public static void read(Model model, InputStream in, Lang2 lang)
    {
        read(model, in, null, lang) ;
    }
        
    /** Read triples into a Model with bytes from an InputStream.
     *  A base URI and a syntax can be provided.
     *  The base URI defualts to "no base" in which case the data should have no relative URIs.
     *  The lang gives the syntax of the stream. 
     * @param model     Destination for the RDF read.
     * @param in        InputStream
     * @param base      Base URI 
     * @param lang      Language syntax
     */
    public static void read(Model model, InputStream in, String base, Lang2 lang)
    {
        Graph g = model.getGraph() ;
        Sink<Triple> sink = new SinkTriplesToGraph(g) ;
        processTriples(sink, base, new TypedInputStream2(in), lang, null) ;
    }

    /** Read triples into a model with chars from an Reader.
     * Use java.io.Readers is not encouraged - use with a StringReader is the primary use case.
     * For files, open a {@link java.io.FileInputStream} to ensure correct character set handling.
     * @deprecated Use an InputStream or StringReader.
     * @param model     Destination for the RDF read.
     * @param in        Reader
     * @param base      Base URI 
     * @param lang      Language syntax
     */
    @Deprecated
    public static void read(Model model, Reader in, String base, Lang2 lang)
    {
        Graph g = model.getGraph() ;
        Sink<Triple> sink = new SinkTriplesToGraph(g) ;
        processTriples(sink, base, in, lang, null) ;
    }

    /** Read triples into a model with chars from a StringReader.
     * @param model     Destination for the RDF read.
     * @param in        InputStream
     * @param base      Base URI 
     * @param lang      Language syntax
     */
    public static void read(Model model, StringReader in, String base, Lang2 lang)
    {
        Graph g = model.getGraph() ;
        Sink<Triple> sink = new SinkTriplesToGraph(g) ;
        processTriples(sink, base, in, lang, null) ;
    }


    
    /** Read triples - send to a sink.
     * @param sink     Destination for the RDF read.
     * @param uri       URI to read from (includes file: and a plain file name).
     * @param hintLang  Hint for the syntax
     * @param context   Content object to control reading process.
     */
    public static void readTriples(Sink<Triple> sink, String uri, Lang2 hintLang, Context context)
    {
        readTriples(sink, uri, uri, hintLang, context) ;
    }
    
    /** Read triples - send to a sink.
     * @param sink     Destination for the RDF read.
     * @param uri       URI to read from (includes file: and a plain file name).
     * @param base      Base URI (defaults to uri).
     * @param hintLang  Hint for the syntax
     * @param context   Content object to control reading process.
     */
    public static void readTriples(Sink<Triple> sink, String uri, String base, Lang2 hintLang, Context context)
    {
        TypedInputStream2 in = open(uri, context) ;
        if ( in == null )
            throw new RiotException("Not found: "+uri) ;
        processTriples(sink, base, in, hintLang, context) ;
        in.close() ;
    }
    
    // ----- 
    // Readers are algorithms and must be stateless (or they must create a per run
    // instance of something) because they may be called concurrency from different threads.
    // The Context Readerobject gives the per-run configuration.  
    
    // Alternative: A two step factory-instance design means
    // readers can be created and passed around (e,.g. to set specific features)
    // We could have had two step design - ReaderFactory-ReaderInstance
    // no - put the bruden on complicated readers, not everyone. 
    
    private static void processTriples(Sink<Triple> sink, String baseUri, TypedInputStream2 in, Lang2 hintLang, Context context)
    {
        ContentType ct = determineCT(baseUri, in.getContentType(), hintLang ) ;
        
        if ( ct == null )
            throw new RiotException("Failed to determine the triples content type: (URI="+baseUri+" : stream="+in.getContentType()+" : hint="+hintLang+")") ;

        ReaderRIOT<Triple> reader = getReaderTriples(ct) ;
        if ( reader == null )
            throw new RiotException("No triples reader for content type: "+ct.getContentType()) ;
        
        reader.read(in.getInput(), baseUri, ct, sink, context) ;
    }

    private static ReaderRIOT<Triple> getReaderTriples(ContentType ct)
    {
        Lang2 lang = RDFLanguages.contentTypeToLang(ct) ;
        ReaderRIOTFactory<Triple> r = RDFParserRegistry.getFactoryTriples(lang) ;
        if ( r == null )
            return null ;
        return r.create(lang) ;
    }

    // java.io.Readers are NOT preferred.
    @SuppressWarnings("deprecation")
    private static void processTriples(Sink<Triple> sink, String base, Reader in, Lang2 hintLang, Context context)
    {
        // Not as good as from an InputStream - RDF/XML not supported 
        ContentType ct = determineCT(base, null, hintLang) ;
        if ( ct == null )
            throw new RiotException("Failed to determine the triples content type: (URI="+base+" : hint="+hintLang+")") ;
        
        Tokenizer tokenizer = TokenizerFactory.makeTokenizer(in) ;
        if ( hintLang == null )
            throw new RiotException("No language specificied") ;
        Lang lang = RDFLanguages.convert(hintLang) ;
        LangRIOT parser ;
        if ( lang == Lang.RDFXML )
            parser = LangRDFXML.create(in, base, base, ErrorHandlerFactory.errorHandlerStd, sink) ;
        else
            parser = RiotReader.createParserTriples(tokenizer, lang, base, sink) ;
        parser.parse() ;
    }
}

