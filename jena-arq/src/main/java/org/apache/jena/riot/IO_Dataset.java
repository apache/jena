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
import org.openjena.riot.Lang ;
import org.openjena.riot.RiotException ;
import org.openjena.riot.RiotNotFoundException ;
import org.openjena.riot.RiotReader ;
import org.openjena.riot.lang.LangRIOT ;
import org.openjena.riot.lang.SinkQuadsToDataset ;
import org.openjena.riot.tokens.Tokenizer ;
import org.openjena.riot.tokens.TokenizerFactory ;

import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.util.Context ;

/** <p>General purpose reader framework for RDF quad syntaxes.</p>   
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
 *  @see WebReader2
 */

public class IO_Dataset
{
    /* Maybe:
     * static for global (singleton) and locally tailored. 
     */
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
        Sink<Quad> sink = new SinkQuadsToDataset(dsg) ;
        processQuads(sink, base, new TypedInputStream2(in), lang, null) ;
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
        Sink<Quad> sink = new SinkQuadsToDataset(dsg) ;
        processQuads(sink, base, in, lang, null) ;
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
        Sink<Quad> sink = new SinkQuadsToDataset(dsg) ;
        processQuads(sink, base, in, lang, null) ;
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
        TypedInputStream2 in = open(uri, context) ;
        if ( in == null )
            throw new RiotException("Not found: "+uri) ;
        processQuads(sink, base, in, hintLang, context) ;
        in.close() ;
    }
    
    private static void processQuads(Sink<Quad> sink, String uri, TypedInputStream2 in, Lang2 hintLang, Context context)
    {
        ContentType ct = determineCT(uri, in.getContentType(), hintLang ) ;
        if ( ct == null )
            throw new RiotException("Failed to determine the quads content type: (URI="+uri+" : stream="+in.getContentType()+" : hint="+hintLang+")") ;
        ReaderRIOT<Quad> reader = getReaderQuads(ct) ;
        if ( reader == null )
            throw new RiotException("No quads reader for content type: "+ct) ;
        
        reader.read(in.getInput(), uri, ct, sink, context) ;
    }

    private static ReaderRIOT<Quad> getReaderQuads(ContentType ct)
    {
        Lang2 lang = Langs.contentTypeToLang(ct) ;
        ReaderRIOTFactory<Quad> r = Langs.getFactoryQuads(lang) ;
        if ( r == null )
            return null ;
        return r.create(lang) ;
    }
    
    // java.io.Readers are NOT preferred.
    private static void processQuads(Sink<Quad> sink, String base, Reader in, Lang2 hintLang, Context context)
    {
        Tokenizer tokenizer = TokenizerFactory.makeTokenizer(in) ;
        Lang lang = Langs.convert(hintLang) ;
        LangRIOT parser = RiotReader.createParserQuads(tokenizer, lang, base, sink) ;
        parser.parse() ;
    }
}

