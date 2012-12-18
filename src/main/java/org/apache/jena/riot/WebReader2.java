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

import java.io.InputStream ;
import java.io.Reader ;
import java.io.StringReader ;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.io.PeekReader ;
import org.apache.jena.atlas.json.io.parser.TokenizerJSON ;
import org.apache.jena.atlas.lib.Sink ;
import org.apache.jena.atlas.web.ContentType ;
import org.apache.jena.atlas.web.TypedInputStream ;
import org.apache.jena.riot.lang.* ;
import org.apache.jena.riot.stream.StreamManager ;
import org.apache.jena.riot.tokens.Tokenizer ;
import org.apache.jena.riot.tokens.TokenizerFactory ;
import org.openjena.riot.* ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.Quad ;
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
 *  It also provide a way to lookup names in different
 *  locations and to remap URIs to other URIs. 
 *  </p>
 *  <p>
 *  Extensible - a new syntax can be added to the framework. 
 *  </p>
 */

public class WebReader2
{
    /* Maybe:
     * static for global (singleton) and locally tailored. 
     */
    
    static Logger log = LoggerFactory.getLogger(WebReader2.class) ;
    private static String riotBase = "http://jena.apache.org/riot/" ; 
    private static String streamManagerSymbolStr = riotBase+"streammanager" ; 
    public static Symbol streamManagerSymbol = Symbol.create(streamManagerSymbolStr) ; 

    
    // Yukky hack to integrate into current jena-core where the structure of model.read assumes
    // the language is determined before the reading process starts.
    
    public static class RDFReaderRIOT_RDFXML extends RDFReaderRIOT   { public RDFReaderRIOT_RDFXML() { super("RDF/XML") ; } }
    public static class RDFReaderRIOT_TTL extends RDFReaderRIOT      { public RDFReaderRIOT_TTL() { super("TTL") ; } }
    public static class RDFReaderRIOT_NT extends RDFReaderRIOT       { public RDFReaderRIOT_NT() { super("N-TRIPLE") ; } }
    public static class RDFReaderRIOT_RDFJSON extends RDFReaderRIOT  { public RDFReaderRIOT_RDFJSON() { super("RDF/JSON") ; } }

    
    public static void resetJenaReaders()
    {
        SysRIOT.resetJenaReaders() ;
    }
    
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
        RDFParserOutput dest = RDFParserOutputLib.graph(g) ;
        readTriples(dest, uri, base, hintLang, context) ;
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
        RDFParserOutput dest = RDFParserOutputLib.graph(g) ;
        Sink<Triple> sink = new SinkTriplesToGraph(g) ;
        processTriples(dest, base, new TypedInputStream(in), lang, null) ;
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
        RDFParserOutput dest = RDFParserOutputLib.graph(g) ;
        processTriples(dest, base, in, lang, null) ;
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
        RDFParserOutput dest = RDFParserOutputLib.graph(g) ;
        processTriples(dest, base, in, lang, null) ;
    }

    
    /** Read quads into a Dataset from the given location, with hint of langauge.
     * @see #read(Dataset, String, String, Lang2, Context) 
     * @param dataset   Destination
     * @param uri       URI to read from (includes file: and a plain file name).
     * @param hintLang  Language syntax
     */
    public static void read(Dataset dataset, String uri, Lang2 hintLang)
    {
        read(dataset, uri, hintLang, null) ;
    }

    /** Read quads or triples into a Dataset from the given location. 
     * @see #read(Dataset, String, String, Lang2, Context) 
     * @param dataset   Destination
     * @param uri       URI to read from (includes file: and a plain file name).
     * @param hintLang  Language syntax
     */
    public static void read(Dataset dataset, String uri, Lang2 hintLang, Context context)
    {
        read(dataset, uri, uri, hintLang, context) ;
    }
    
    /** Read quads or triples into a Dataset from the given location.
     * @see #read(Dataset, String, String, Lang2, Context) 
     * @param dataset   Destination
     * @param uri       URI to read from (includes file: and a plain file name).
     * @param hintLang  Language syntax
    *  @throws RiotNotFoundException if the location is not found - the model is unchanged.
    *  Throws parse errors depending on the language and reader; the Model model may be partially updated. 
    */ 

    public static void read(Dataset dataset, String uri, String base, Lang2 hintLang, Context context)
    {
        DatasetGraph dsg = dataset.asDatasetGraph() ;
        Sink<Quad> sink = new SinkQuadsToDataset(dsg) ;
        readQuads(sink, uri, base, hintLang, context) ;
    }

    /** Read quads or triples into a dataset with bytes from an input stream.
     * @param dataset   Destination
     * @param in        InputStream
     * @param lang      Language syntax
     */
    public static void read(Dataset dataset, InputStream in, Lang2 lang)
    {
        read(dataset, in, null, lang) ;
    }
    
    /** Read quads or triples into a dataset with bytes from an input stream.
     * @param dataset   Destination
     * @param in        InputStream
     * @param base      Base URI
     * @param lang      Language syntax
     */
    public static void read(Dataset dataset, InputStream in, String base, Lang2 lang)
    {
        DatasetGraph dsg = dataset.asDatasetGraph() ;
        RDFParserOutput dest = RDFParserOutputLib.dataset(dsg) ;
        processQuads(dest, base, new TypedInputStream(in), lang, null) ;
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
    public static void read(Dataset dataset, Reader in, String base, Lang2 lang)
    {
        DatasetGraph dsg = dataset.asDatasetGraph() ;
        RDFParserOutput dest = RDFParserOutputLib.dataset(dsg) ;
        processQuads(dest, base, in, lang, null) ;
    }

    /** Read quads into a dataset with chars from a StringReader.
     * Use java.io.Readers is not encouraged - use with a StringReader is the primary use case.
     * For files, open a {@link java.io.FileInputStream} to ensure correct character set handling.
     * @param dataset   Destination
     * @param in        InputStream
     * @param base      Base URI
     * @param lang      Language syntax
     */
    public static void read(Dataset dataset, StringReader in, String base, Lang2 lang)
    {
        DatasetGraph dsg = dataset.asDatasetGraph() ;
        RDFParserOutput dest = RDFParserOutputLib.dataset(dsg) ;
        processQuads(dest, base, in, lang, null) ;
    }

//    public static void addTripleSyntax(Lang2 language, ContentType contentType, ReaderRIOTFactory<Triple> factory, String ... fileExt )
//    { 
//        RDFLanguages.addTripleSyntax$(language, contentType, factory, fileExt) ;
//    } 
//    
//    public static void addQuadSyntax(Lang2 language, ContentType contentType, ReaderRIOTFactory<Quad> factory, String ... fileExt )
//    {
//        RDFLanguages.addQuadSyntax$(language, contentType, factory, fileExt) ;
//    }
    
    //{ParserOut]
    // Add ParserOutput in public versionss.
    
    /** Read triples - send to a sink.
     * @param sink     Destination for the RDF read.
     * @param uri       URI to read from (includes file: and a plain file name).
     * @param hintLang  Hint for the syntax
     * @param context   Content object to control reading process.
     */
    public static void readTriples(RDFParserOutput sink, String uri, Lang2 hintLang, Context context)
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
    public static void readTriples(RDFParserOutput sink, String uri, String base, Lang2 hintLang, Context context)
    {
        TypedInputStream in = open(uri, context) ;
        if ( in == null )
            throw new RiotException("Not found: "+uri) ;
        if ( base == null )
            base = uri ;
        processTriples(sink, base, in, hintLang, context) ;
        IO.close(in) ;
    }
    
    /** Read quads - send to a sink.
     * @param sink     Destination for the RDF read.
     * @param uri       URI to read from (includes file: and a plain file name).
     * @param hintLang  Hint for the syntax
     * @param context   Content object to control reading process.
     */
    public static void readQuads(Sink<Quad> sink, String uri, Lang2 hintLang, Context context)
    {
        readQuads(sink, uri, uri, hintLang, context) ;
    }

    /** Read quads - send to a sink.
     * @param sink     Destination for the RDF read.
     * @param uri       URI to read from (includes file: and a plain file name).
     * @param base      Base URI (defaults to uri).
     * @param hintLang  Hint for the syntax
     * @param context   Content object to control reading process.
     */
    public static void readQuads(Sink<Quad> sink, String uri, String base, Lang2 hintLang, Context context)
    {
        TypedInputStream in = open(uri, context) ;
        if ( in == null )
            throw new RiotException("Not found: "+uri) ;
        RDFParserOutput dest = RDFParserOutputLib.sinkQuads(sink) ;
        processQuads(dest, base, in, hintLang, context) ;
        IO.close(in) ;
    }

    /** Open a stream to the destination (URI or filename)
     * Performs content negotition, including looking at file extension. 
     */
    public static TypedInputStream open(String filenameOrURI)
    { return open(filenameOrURI, (Context)null) ; }
    
    /** Open a stream to the destination (URI or filename)
     * Performs content negotition, including looking at file extension. 
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
    
    public static TypedInputStream open(String filenameOrURI, StreamManager sMgr)
    {
        TypedInputStream in = sMgr.open(filenameOrURI) ;
            
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
    
    private static void processTriples(RDFParserOutput destination, String baseUri, TypedInputStream in, Lang2 hintLang, Context context)
    {
        ContentType ct = determineCT(baseUri, in.getContentType(), hintLang ) ;
        
        if ( ct == null )
            throw new RiotException("Failed to determine the triples content type: (URI="+baseUri+" : stream="+in.getContentType()+" : hint="+hintLang+")") ;

        ReaderRIOT reader = getReaderTriples(ct) ;
        if ( reader == null )
        {
            throw new RiotException("No triples reader for content type: "+ct.getContentType()) ;
        }
        
        reader.read(in, baseUri, ct, destination, context) ;
    }

    private static ReaderRIOT getReaderTriples(ContentType ct)
    {
        Lang2 lang = RDFLanguages.contentTypeToLang(ct) ;
        ReaderRIOTFactory r = ParserRegistry.getFactoryTriples(lang) ;
        if ( r == null )
            return null ;
        return r.create(lang) ;
    }

    // With sink and ither ParserOutput
    
    // java.io.Readers are NOT preferred.
    @SuppressWarnings("deprecation")
    private static void processTriples(RDFParserOutput output, String base, Reader in, Lang2 hintLang, Context context)
    {
        // Not as good as from an InputStream - RDF/XML not supported 
        ContentType ct = determineCT(base, null, hintLang) ;
        if ( ct == null )
            throw new RiotException("Failed to determine the triples content type: (URI="+base+" : hint="+hintLang+")") ;
        
        Tokenizer tokenizer =
            RDFLanguages.RDFJSON.equals(hintLang)?
                          new TokenizerJSON(PeekReader.make(in)) :   
                          TokenizerFactory.makeTokenizer(in) ;
        
        if ( hintLang == null )
            throw new RiotException("No language specificied") ;
        Lang lang = RDFLanguages.convert(hintLang) ;
        LangRIOT parser ;
        if ( lang == Lang.RDFXML )
            parser = LangRDFXML.create(in, base, base, ErrorHandlerFactory.errorHandlerStd, output) ;
        else
            parser = RiotReader.createParserTriples(tokenizer, lang, base, output) ;
        parser.parse() ;
    }
    
    private static void processQuads(RDFParserOutput destination, String uri, TypedInputStream in, Lang2 hintLang, Context context)
    {
        ContentType ct = determineCT(uri, in.getContentType(), hintLang ) ;
        if ( ct == null )
            throw new RiotException("Failed to determine the quads content type: (URI="+uri+" : stream="+in.getContentType()+" : hint="+hintLang+")") ;
        ReaderRIOT reader = getReaderQuads(ct) ;
        if ( reader == null )
            throw new RiotException("No quads reader for content type: "+ct) ;
        
        reader.read(in, uri, ct, destination, context) ;
    }

    private static ReaderRIOT getReaderQuads(ContentType ct)
    {
        Lang2 lang = RDFLanguages.contentTypeToLang(ct) ;
        ReaderRIOTFactory r = ParserRegistry.getFactoryQuads(lang) ;
        if ( r == null )
            return null ;
        return r.create(lang) ;
    }
    
    // java.io.Readers are NOT preferred.
    private static void processQuads(RDFParserOutput dest, String base, Reader in, Lang2 hintLang, Context context)
    {
        Tokenizer tokenizer = TokenizerFactory.makeTokenizer(in) ;
        Lang lang = RDFLanguages.convert(hintLang) ;
        LangRIOT parser = RiotReader.createParserQuads(tokenizer, lang, base, dest) ;
        parser.parse() ;
    }

    private static ContentType determineCT(String target, String ctStr, Lang2 hintLang)
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
}

