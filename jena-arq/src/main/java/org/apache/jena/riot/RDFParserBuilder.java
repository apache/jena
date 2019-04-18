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

package org.apache.jena.riot;

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.*;

import org.apache.http.Header;
import org.apache.http.client.HttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.jena.atlas.lib.IRILib;
import org.apache.jena.graph.BlankNodeId;
import org.apache.jena.graph.Graph;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFParser.LangTagForm;
import org.apache.jena.riot.lang.LabelToNode;
import org.apache.jena.riot.system.*;
import org.apache.jena.riot.system.stream.StreamManager;
import org.apache.jena.riot.web.HttpNames;
import org.apache.jena.riot.web.HttpOp ;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.util.Context;

/**
 * An {@link RDFParser} is a process that will generate triples; 
 * {@link RDFParserBuilder} provides the means to setup the parser.
 * <p>
 * An {@link RDFParser} has a predefined source; the target for output is given when the "parse" method is called. 
 * It can be used multiple times in which case the same source is reread. The destination can vary.
 * The application is responsible for concurrency of the destination of the parse operation.
 * 
 * The process is
 * <pre>
 *     StreamRDF destination = ...
 *     RDFParser parser = RDFParser.create()
 *          .source("filename.ttl")
 *          .build();
 *     parser.parse(destination); 
 * </pre>
 * or using a short cut: 
 * <pre>
 *     RDFParser parser = RDFParser.create()
 *          .source("filename.ttl")
 *          .parse(destination); 
 * </pre> 
 */
public class RDFParserBuilder {
    // The various sources
    // Reusable parser
    private String uri = null;
    private Path path = null;
    private String content = null;
    // The not reusable sources.
    private InputStream inputStream;
    private Reader javaReader = null;
    private StreamManager streamManager = null;

    // HTTP
    private Map<String, String> httpHeaders = new HashMap<>(); 
    private HttpClient httpClient = null;

    // Syntax
    private Lang hintLang = null;
    private Lang forceLang = null;
    
    private String baseUri = null;
    
    private boolean           canonicalValues = false;
    private LangTagForm  langTagForm = LangTagForm.NONE;
    
    private Optional<Boolean> checking = Optional.empty();
    
    private boolean strict = SysRIOT.isStrictMode();
    private boolean resolveURIs = true;
    private IRIResolver resolver = null;
    // ----
    
    // Construction for the StreamRDF 
    private FactoryRDF factory = null;
    private LabelToNode labelToNode = null;
    
    // Bad news.
    private ErrorHandler errorHandler = null;
    
    // Parsing process
    private Context context = null;
    
    public static RDFParserBuilder create() { return new RDFParserBuilder() ; }
    private RDFParserBuilder() {}
    
    /** 
     *  Set the source to {@link Path}. 
     *  This clears any other source setting.
     *  <p>
     *  The parser can be reused.
     *  @param path
     *  @return this
     */
    public RDFParserBuilder source(Path path) {
        clearSource();
        this.path = path;
        return this;
    }

    /** 
     *  Set the source to a URI; this includes OS file names.
     *  File URL should be of the form {@code file:///...}. 
     *  This clears any other source setting.
     *  <p>
     *  The parser can be reused.
     *  @param uriOrFile
     *  @return this
     */
    public RDFParserBuilder source(String uriOrFile) {
        clearSource();
        this.uri = uriOrFile;
        return this;
    }

    /** 
     *  Use the given string as the content to parse. 
     *  This clears any other source setting.
     *  <p>
     *  The syntax must be set with {@code .lang(...)}.
     *  <p>
     *  The parser can be reused.  
     *  @param string The characters to be parsed. 
     *  @return this
     */
    public RDFParserBuilder fromString(String string) {
        clearSource();
        this.content = string;
        return this;
    }
    
    /** 
     *  Set the source to {@link InputStream}. 
     *  This clears any other source setting.
     *  <p>
     *  The syntax must be set with {@code .lang(...)}.
     *  <p>
     *  The {@link InputStream} will be closed when the 
     *  parser is called and the parser can not be reused.  
     *  @param input
     *  @return this
     */
    public RDFParserBuilder source(InputStream input) {
        clearSource();
        this.inputStream = input;
        return this;
    }

    /**
     *  Set the source to {@link StringReader}. 
     *  This clears any other source setting.
     *  The {@link StringReader} will be closed when the 
     *  parser is called and the parser can not be reused.
     *  <p>
     *  The syntax must be set with {@code .lang(...)}.
     *  <p>
     *  Consider using {@link #fromString} instead.   
     *  @param reader
     *  @return this
     */
    public RDFParserBuilder source(StringReader reader) {
        clearSource();
        this.javaReader = reader;
        return this;
    }

    /** 
     *  Set the source to {@link Reader}. 
     *  This clears any other source setting.
     *  The {@link Reader} will be closed when the 
     *  parser is called and the parser can not be reused.
     *  <p>
     *  The syntax must be set with {@code .lang(...)}.
     *  @param reader
     *  @return this
     *  @deprecated Use {@link #fromString}, or an InputStream or a StringReader. 
     */
    @Deprecated
    public RDFParserBuilder source(Reader reader) {
        clearSource();
        this.javaReader = reader;
        return this;
    }
    
    /**
     * Set the StreamManager to use when opening a URI (including files by name, but not by {@code Path}). 
     * @param streamManager
     * @return this
     */
    public RDFParserBuilder streamManager(StreamManager streamManager) {
        this.streamManager = streamManager;
        return this;
    }

    private void clearSource() {
        this.uri = null;
        this.path = null;
        this.content = null;
        this.inputStream = null;
        this.javaReader = null;
    }

    /**
     * Set the hint {@link Lang}. This is the RDF syntax used when there is no way to
     * deduce the syntax (e.g. read from a InputStream, no recognized file extension, no
     * recognized HTTP Content-Type provided).
     * 
     * @param lang
     * @return this
     */
    public RDFParserBuilder lang(Lang lang) { this.hintLang = lang ; return this; }
    
    /**
     * Set the parser built to "strict" mode. The default is system wide setting of {@link SysRIOT#isStrictMode()}.
     * @param strictMode
     * @return this
     */
    public RDFParserBuilder strict(boolean strictMode) { this.strict = strictMode ; return this ; } 

    /**
     * Force the choice RDF syntax to be {@code lang}, and ignore any indications such as file extension
     * or HTTP Content-Type.
     * @see Lang
     * @param lang
     * @return this
     */
    public RDFParserBuilder forceLang(Lang lang) { this.forceLang = lang ; return this; }
    
    /**
     * Set the HTTP "Accept" header.
     * The default if not set is {@link WebContent#defaultRDFAcceptHeader}.
     * @param acceptHeader
     * @return this
     */
    public RDFParserBuilder httpAccept(String acceptHeader) { 
        httpHeader(HttpNames.hAccept, acceptHeader);
        return this; 
    }

    /**
     * Set an HTTP header. Any previous setting is lost.
     * <p> 
     * Consider setting up an {@link HttpClient} if more complicated
     * setting to an HTTP request is required.
     */
    public RDFParserBuilder httpHeader(String header, String value) {
        httpHeaders.put(header, value);
        return this;
    }
    
    /** Set the HttpClient to use.
     *  This will override any HTTP header settings set for this builder.
     */
    public RDFParserBuilder httpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
        return this;
    }

    /** Set the base URI for parsing.  The default is to have no base URI. */ 
    public RDFParserBuilder base(String base) { this.baseUri = base ; return this; }

    /** Choose whether to resolve URIs.<br/>
     *  This does not affect all languages: N-Triples and N-Quads never resolve URIs.<br/>
     *  Relative URIs are bad data.<br/>
     *  Only set this to false for debugging and development purposes. 
     */ 
    public RDFParserBuilder resolveURIs(boolean flag) { this.resolveURIs = flag ; return this; }

    /**
     * Convert the lexical form of literals to a canonical form.
     * @deprecated Use {@link #canonicalValues} and one of {@link #langTagCanonical} and {@link #langTagLowerCase} 
     * <p>
     * This operation is equivalent to 
     * <pre>
     *   this.canonicalValues(flag);
     *    if ( flag )
     *        this.langTagCanonical();
     *    else
     *        this.langTagAsGiven();
     *    return this;
     * </pre>
     */
    @Deprecated
    public RDFParserBuilder canonicalLiterals(boolean flag) {
        this.canonicalValues(flag);
        if ( flag )
            this.langTagCanonical();
        else
            this.langTagAsGiven();
        return this;
    }
    
    /**
     * Convert the lexical form of literals to a canonical form.
     * <p>
     * Two literals can be different RDF terms for the same value.
     * <p>
     * Examples include (first shown of the pair is the canonical form):
     * 
     * <pre>
     *    {@code "1"^^xsd:integer} and {@code "+01"^^xsd:integer} 
     *    {@code "1.0E0"^^xsd:double} and {@code "1"^^xsd:double}
     * </pre>
     * 
     * The canonical forms follow XSD 1.1
     * {@literal <href="https://www.w3.org/TR/xmlschema11-2/#canonical-lexical-representation">2.3.1
     * Canonical Mapping</a>} except in the case of xsd:decimal where it follows the older
     * XSD 1.0 which makes it legal for Turtle's short form ({@code "1.0"^^xsd:Decimal}
     * rather than {@code "1"^^xsd:decimal}). See XSD 1.0 <a href=
     * "https://www.w3.org/TR/xmlschema-2/#decimal-canonical-representation">3.2.3.2
     * Canonical representation</a>
     * <p>
     * The effect on literals where the lexical form does not represent a
     * valid value (for example, {@code "3000"^^xsd:byte}) is undefined.
     * <p>
     * This option is off by default.
     * <p>
     * This option can slow parsing down.
     * <p>
     * For consistent loading of data, it is recommended that data is cleaned and
     * canonicalized before loading so the conversion is done once.
     * 
     * @see #langTagLowerCase
     * @see #langTagCanonical
     */
    public RDFParserBuilder canonicalValues(boolean flag) {
        this.canonicalValues = flag;
        return this; 
    }
    
    /**
     * Convert language tags to lower case.
     * <p>
     * This is the suggested form in RDF 1.1 for comparsions. 
     * However, this is not the recommended canonical form in
     * <a href="https://tools.ietf.org/html/rfc5646">RFC 5646</a>.
     * <p>
     * Providing all data is converted consistently, language tag equality 
     * is maintained for either lower case or RFC canonicalization styles.
     * <p>
     * This option can slow parsing down.
     * <p>
     * @see #langTagCanonical
     */
    public RDFParserBuilder langTagLowerCase() {
        return langTagForm(LangTagForm.LOWER_CASE);
    }

    /**
     * Language tags are case-normalized as defined by
     * <a href="https://tools.ietf.org/html/rfc5646">RFC 5646</a>.
     * Example: {@code en-GB}, not {@code en-gb}.
     * <p>
     * This does not affect the RDF 1.1 requirement that the
     * value-space of language tags is lower-case.
     * <p>
     * Providing all data is converted consistently, lang tag equality is maintained for either
     * lower case or RFC canonicalization.
     * <p>
     * This option can slow parsing down.
     * <p>
     * @see #langTagLowerCase
     */
    public RDFParserBuilder langTagCanonical() {
        return langTagForm(LangTagForm.CANONICAL);
    }

    /**
     * The form of the language tags as given in the data is preserved.
     * This is the default behaviour of parsing.
     * @see #langTagLowerCase
     * @see #langTagCanonical
     */
    public RDFParserBuilder langTagAsGiven() {
        return langTagForm(LangTagForm.NONE);
    }

    private RDFParserBuilder langTagForm(LangTagForm form) {
        this.langTagForm = form;
        return this;
    }
    
    /** Set whether to perform checking, 
     * NTriples and NQuads default to no checking, other languages to checking.
     * <p>
     * Checking adds warnings over and above basic syntax errors.
     * <ul>
     * <li>URIs - whether IRs confirm to all the rules of the URI scheme
     * <li>Literals: whether the lexical form conforms to the rules for the datatype. 
     * <li>Triples and quads: check slots have a valid kind of RDF term (parsers usually make this a syntax error anyway).
     * </ul> 
     * <p>
     * See also {@link #errorHandler(ErrorHandler)} to control the output. The default is to log.
     * This can also be used to turn warnings into exceptions. 
     */
    public RDFParserBuilder checking(boolean flag) { this.checking = Optional.of(flag) ; return this; }
    
    /**
     * Set the {@link ErrorHandler} to use.
     * This replaces any previous setting.
     * The default is use slf4j logger "RIOT".   
     * @param handler
     * @return this
     */
    public RDFParserBuilder errorHandler(ErrorHandler handler) {
        this.errorHandler = handler;
        return this;
    }
    
    /**
     * Set the {@link FactoryRDF} to use. {@link FactoryRDF} control how parser output is
     * turned into {@code Node} and how {@code Triple}s and {@code Quad}s are built. This
     * replaces any previous setting. 
     * <br/>
     * The default is use {@link RiotLib#factoryRDF()} which is provides {@code Node}
     * reuse. 
     * <br/>
     * The {@code FactoryRDF} also determines how blank node labels in RDF syntax are
     * mapped to {@link BlankNodeId}. Use
     * <pre>
     *    new Factory(myLabelToNode) 
     * </pre>
     * to create an {@code FactoryRDF} and set the {@code LabelToNode} step.
     * @see #labelToNode
     * @param factory
     * @return this
     */
    public RDFParserBuilder factory(FactoryRDF factory) {
        this.factory = factory;
        return this;
    }
    
    /**
     * Use the given {@link LabelToNode}, the policy for converting blank node labels in
     * RDF syntax to Jena's {@code Node} objects (usually a blank node).
     * <br/>
     * Only applies when the {@link FactoryRDF} is not set in the
     * {@code RDFParserBuilder}, otherwise the {@link FactoryRDF} controls the
     * label-to-node process.
     * <br/>
     * {@link SyntaxLabels#createLabelToNode} is the default policy.
     * <br>
     * {@link LabelToNode#createUseLabelAsGiven()} uses the label in the RDF syntax directly. 
     * This does not produce safe RDF and should only be used for development and debugging.   
     * @see #factory
     * @param labelToNode
     * @return this
     */
    public RDFParserBuilder labelToNode(LabelToNode labelToNode) {
        this.labelToNode = labelToNode;
        return this;
    }
    
    // There are no strict/unstrict differences.
    // Strict is passed through to the RIOT reader.
//    /**
//     * Set "strict" mode.
//     * @param strictMode
//     * @return this
//     */
//    public RDFParserBuilder strict(boolean strictMode) {
//        this.strict = strictMode;
//        return this;
//    }
    
    public RDFParserBuilder context(Context context) {
        if ( context != null )
            context = context.copy();
        this.context = context;
        return this;
    }
    
    // ---- Terminals
    // "parse" are short cuts for {@code build().parse(...)}.
    
    /** 
     * Parse the source, sending the results to a {@link StreamRDF}.
     * Short form for {@code build().parse(stream)}.
     * @param stream
     */
    public void parse(StreamRDF stream) {
        build().parse(stream);
    }

    /**
     * Parse the source, sending the results to a {@link Graph}.
     * The source must be for triples; any quads are discarded. 
     * Short form for {@code build().parse(graph)}
     * which sends triples and prefixes to the {@code Graph}.
     * 
     * @param graph
     */
    public void parse(Graph graph) {
        build().parse(graph);
    }

    /**
     * Parse the source, sending the results to a {@link Model}.
     * The source must be for triples; any quads are discarded. 
     * Short form for {@code build().parse(model)}
     * which sends triples and prefixes to the {@code Model}.
     * 
     * @param model
     */
    public void parse(Model model) {
        build().parse(model);
    }

    /**
     * Parse the source, sending the results to a {@link DatasetGraph}.
     * Short form for {@code build().parse(dataset)}
     * which sends triples and prefixes to the {@code DatasetGraph}.
     * 
     * @param dataset
     */
    public void parse(DatasetGraph dataset) {
        build().parse(dataset);
    }

    /**
     * Parse the source, sending the results to a {@link Dataset}.
     * Short form for {@code build().parse(dataset)}
     * which sends triples and prefixes to the {@code Dataset}.
     * 
     * @param dataset
     */
    public void parse(Dataset dataset) {
        build().parse(dataset);
    }

    /** Build an {@link RDFParser}. The parser takes it's configuration from this builder and can not then be changed.
     * The source must be set.
     * When a parser is used, it is takes the source and sends output to an {@link StreamRDF}.
     * <p>  
     * Shortcuts:
     * <ul>
     * <li>{@link #parse(DatasetGraph)} - parse the source and output to a {@code DatasetGraph}
     * <li>{@link #parse(Graph)} - parse the source and output to a {@code Graph}
     * <li>{@link #parse(StreamRDF)} - parse the source and output to a {@code StreamRDF}
     * </ul>
     * 
     * @return RDFParser
     */
    public RDFParser build() {
        // Build what we can now - some things have to be built in the parser.
        if ( uri == null && path == null && content == null && inputStream == null && javaReader == null )
            throw new RiotException("No source specified");
        
        // Setup the HTTP client.
        HttpClient client = buildHttpClient();
        FactoryRDF factory$ = buildFactoryRDF();
        ErrorHandler errorHandler$ = errorHandler;
        if ( errorHandler$ == null )
            errorHandler$ = ErrorHandlerFactory.getDefaultErrorHandler();

        if ( path != null && baseUri == null )
            baseUri = IRILib.filenameToIRI(path.toString());
        if ( path == null && baseUri == null && uri != null )
            baseUri = uri;
        
        StreamManager sMgr = streamManager;
        if ( sMgr == null )
            sMgr = StreamManager.get(context);
        
        // Can't build the profile here as it is Lang/conneg dependent.
        return new RDFParser(uri, path, content, inputStream, javaReader, sMgr, 
                             client, hintLang, forceLang,
                             baseUri, strict, checking, 
                             canonicalValues, langTagForm,
                             resolveURIs, resolver, factory$, errorHandler$, context);
    }

    private FactoryRDF buildFactoryRDF() {
        FactoryRDF factory$ = factory;
        if ( factory$ == null ) { 
            if ( labelToNode != null )
                factory$ = RiotLib.factoryRDF(labelToNode);
            else
                factory$ = RiotLib.factoryRDF();
        }
        return factory$;
    }

    private HttpClient buildHttpClient() {
        if ( httpClient != null )
            return httpClient;
        if ( httpHeaders.isEmpty() )
            // System default.
            // In this case, RDFParser will use the current-at-parse-time,
            // settings of HttpOp, not frozen here. The HTTP step operation will use a
            // general purpose accept header, WebContent.defaultRDFAcceptHeader, that
            // gets any syntax of triples or quads. To freeze now to HttpOp settings, 
            // call httpClient(HttpOp.getDefaultHttpClient). 
            return null;
        List<Header> hdrs = new ArrayList<>();
        httpHeaders.forEach((k,v)->{
            Header header = new BasicHeader(k, v);
            hdrs.add(header);
        });
        HttpClient hc = HttpOp.createPoolingHttpClientBuilder()
            .setDefaultHeaders(hdrs)
            .build() ;
        return hc ;
    }

    /**
     * Duplicate this builder with current settings.
     * Changes to setting to this builder do not affect the clone. 
     */
    @Override
    public RDFParserBuilder clone() { 
        RDFParserBuilder builder = new RDFParserBuilder();
        builder.uri =               this.uri;
        builder.path =              this.path;
        builder.content =           this.content;
        builder.inputStream =       this.inputStream;
        builder.javaReader =        this.javaReader;
        builder.httpHeaders =       new HashMap<>(this.httpHeaders);
        builder.httpClient =        this.httpClient;
        builder.hintLang =          this.hintLang;
        builder.forceLang =         this.forceLang;
        builder.baseUri =           this.baseUri;
        builder.checking =          this.checking;
        builder.canonicalValues =   this.canonicalValues;
        builder.langTagForm =       this.langTagForm;
        builder.strict =            this.strict;
        builder.resolveURIs =       this.resolveURIs;
        builder.resolver =          this.resolver;
        builder.factory =           this.factory;
        builder.labelToNode =       this.labelToNode;
        builder.errorHandler =      this.errorHandler;
        builder.context =           this.context;
        return builder;
    }
}
