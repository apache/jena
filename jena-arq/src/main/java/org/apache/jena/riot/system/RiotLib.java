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

package org.apache.jena.riot.system;

import static org.apache.jena.riot.RDFLanguages.NQUADS;
import static org.apache.jena.riot.RDFLanguages.NTRIPLES;
import static org.apache.jena.riot.RDFLanguages.RDFJSON;
import static org.apache.jena.riot.RDFLanguages.sameLang;
import static org.apache.jena.riot.writer.WriterConst.PREFIX_IRI;
import static org.apache.jena.riot.writer.WriterConst.RDF_type;
import static org.apache.jena.riot.writer.WriterConst.rdfNS;

import java.io.OutputStream;
import java.io.Writer;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.ARQ;
import org.apache.jena.riot.*;
import org.apache.jena.riot.lang.LabelToNode;
import org.apache.jena.riot.tokens.Token;
import org.apache.jena.riot.tokens.Tokenizer;
import org.apache.jena.riot.tokens.TokenizerFactory;
import org.apache.jena.riot.writer.WriterGraphRIOTBase;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.util.iterator.ExtendedIterator;

/** Misc RIOT code */
public class RiotLib
{
    // ---- BlankNode skolemization as IRIs
    private final static boolean skolomizedBNodes = ARQ.isTrueOrUndef(ARQ.constantBNodeLabels);
    /** "Skolemize" to a node. 
     * Returns a Node_URI.
     */
    public static Node blankNodeToIri(Node node) {
        if ( node.isBlank() )
            return NodeFactory.createURI(blankNodeToIriString(node));
        return node;
    }

    /** "Skolemize" to a string. */ 
    public static String blankNodeToIriString(Node node) {
        if ( node.isBlank() ) {
            String x = node.getBlankNodeLabel();
            return "_:" + x;
        }
        if ( node.isURI())
            return node.getURI();
        throw new RiotException("Not a blank node or URI");
    }

    private final static String bNodeLabelStart = "_:";
    /** Implement {@code <_:....>} as a "Node IRI"
     * that is, use the given label as the BNode internal label.
     * Use with care.
     * Returns a Node_URI.
     */
    public static Node createIRIorBNode(String str) {
        // Is it a bNode label? i.e. <_:xyz>
        if ( skolomizedBNodes && isBNodeIRI(str) ) {
            String s = str.substring(bNodeLabelStart.length());
            Node n = NodeFactory.createBlankNode(s);
            return n;
        }
        return NodeFactory.createURI(str);
    }

    /** Test whether a IRI is a ARQ-encoded blank node. */
    public static boolean isBNodeIRI(String iri) {
        return iri.startsWith(bNodeLabelStart);
    }
    
    private static final String URI_PREFIX_FIXUP = "::";
    
    // These two must be in-step.
    /** Function applied to undefined prefixes to convert to a URI string */  
    public static final Function<String,String> fixupPrefixes      = (x) -> URI_PREFIX_FIXUP.concat(x);

    /** Function to test for undefined prefix URIs*/  
    public static final Predicate<String> testFixupedPrefixURI     = (x) -> x.startsWith(URI_PREFIX_FIXUP);
    
    /** Test whether a IRI is a ARQ-encoded blank node. */
    public static boolean isPrefixIRI(String iri) {
        return testFixupedPrefixURI.test(iri);
    }
    
    /** Convert an prefix name (qname) to an IRI, for when the prerix is nor defined.
     * @see ARQ#fixupUndefinedPrefixes
     */
    public static String fixupPrefixIRI(String prefix, String localPart) {
        return fixupPrefixIRI(prefix+":"+localPart);
    }

    /** Convert an prefix name (qname) to an IRI, for when the prefix is not defined.
     * @see ARQ#fixupUndefinedPrefixes
     */
    public static String fixupPrefixIRI(String prefixedName) {
        return fixupPrefixes.apply(prefixedName);
    }
    
    /** Internal ParserProfile used to create nodes from strings. */ 
    private static ParserProfile setupInternalParserProfile() {
        PrefixMap pmap = PrefixMapFactory.createForInput();
        pmap.add("rdf",  ARQConstants.rdfPrefix);
        pmap.add("rdfs", ARQConstants.rdfsPrefix);
        pmap.add("xsd",  ARQConstants.xsdPrefix);
        pmap.add("owl" , ARQConstants.owlPrefix);
        pmap.add("fn" ,  ARQConstants.fnPrefix); 
        pmap.add("op" ,  ARQConstants.fnPrefix); 
        pmap.add("ex" ,  "http://example/ns#");
        pmap.add("" ,    "http://example/");
        
        return new ParserProfileStd(RiotLib.factoryRDF(), 
                                    ErrorHandlerFactory.errorHandlerStd,
                                    IRIResolver.create(),
                                    pmap,
                                    RIOT.getContext().copy(),
                                    true, false);
    }
    
    private static ParserProfile profile = setupInternalParserProfile();
    
    /** Parse a string to get one Node (the first token in the string) */
    public static Node parse(String string)
    {
        Tokenizer tokenizer = TokenizerFactory.makeTokenizerString(string);
        if ( ! tokenizer.hasNext() )
            return null;
        Token t = tokenizer.next();
        Node n = profile.create(null, t);
        if ( tokenizer.hasNext() )
            Log.warn(RiotLib.class, "String has more than one token in it: "+string);
        return n;
    }

    public static ParserProfile profile(Lang lang, String baseIRI)
    {
        return profile(lang, baseIRI, ErrorHandlerFactory.getDefaultErrorHandler());
    }

    public static ParserProfile profile(Lang lang, String baseIRI, ErrorHandler handler)
    {
        if ( sameLang(NTRIPLES, lang) || sameLang(NQUADS, lang) )
        {
            boolean checking = SysRIOT.isStrictMode();
            // If strict mode, do checking e.g. URIs
            return profile(baseIRI, false, checking, handler);
        }
        if ( sameLang(RDFJSON, lang) )
            return profile(baseIRI, false, true, handler);
        return profile(baseIRI, true, true, handler);
    }

    /** Create a parser profile for the given setup
     * @param baseIRI       Base IRI
     * @param resolveIRIs   Whether to resolve IRIs
     * @param checking      Whether to check 
     * @param handler       Error handler
     * @return ParserProfile
     * @see #profile for per-language setup
     * @deprecated To be removed.
     */
    @Deprecated
    public static ParserProfile profile(String baseIRI, boolean resolveIRIs, boolean checking, ErrorHandler handler)
    {
        LabelToNode labelToNode = SyntaxLabels.createLabelToNode();
        
        IRIResolver resolver;
        if ( resolveIRIs )
            resolver = IRIResolver.create(baseIRI);
        else
            resolver = IRIResolver.createNoResolve();
        ParserProfile profile = RiotLib.createParserProfile(factoryRDF(labelToNode), handler, resolver, checking);
        return profile;
    }

    /** Create a new (not influenced by anything else) {@code FactoryRDF}
     * using the label to blank node scheme provided. 
     */
    public static FactoryRDF factoryRDF(LabelToNode labelMapping) {
        return new FactoryRDFCaching(FactoryRDFCaching.DftNodeCacheSize, labelMapping);
    }

    /** Create a new (not influenced by anything else) {@code FactoryRDF}
     * using the default label to blank node scheme. 
     */  
    public static FactoryRDF factoryRDF() {
        return factoryRDF(SyntaxLabels.createLabelToNode());
    }

    /** Create a {@link ParserProfile} with default settings. */
    public static ParserProfile dftProfile() {
        return createParserProfile(RiotLib.factoryRDF(), ErrorHandlerFactory.errorHandlerStd, true);
    }

    /** Create a {@link ParserProfile} with default settings, and a specific error handler. */
    public static ParserProfile createParserProfile(ErrorHandler errorHandler) {
        return createParserProfile(RiotLib.factoryRDF(), errorHandler, true);
    }

    /** Create a {@link ParserProfile} with default settings, and a specific error handler. */
    public static ParserProfile createParserProfile(FactoryRDF factory, ErrorHandler errorHandler, boolean checking) {
        return new ParserProfileStd(factory, 
                                    errorHandler,
                                    IRIResolver.create(),
                                    PrefixMapFactory.createForInput(),
                                    RIOT.getContext().copy(),
                                    checking, false);
    }
    
    /** Create a {@link ParserProfile}. */
    public static ParserProfile createParserProfile(FactoryRDF factory, ErrorHandler errorHandler, 
                                          IRIResolver resolver, boolean checking) {
        return new ParserProfileStd(factory, 
                                    errorHandler,
                                    resolver,
                                    PrefixMapFactory.createForInput(),
                                    RIOT.getContext().copy(),
                                    checking, false);
    }

    /** Get triples with the same subject */
    public static Collection<Triple> triplesOfSubject(Graph graph, Node subj) {
        return triples(graph, subj, Node.ANY, Node.ANY);
    }

    /** Get all the triples for the graph.find */
    public static List<Triple> triples(Graph graph, Node s, Node p, Node o) {
        List<Triple> acc = new ArrayList<>();
        accTriples(acc, graph, s, p, o);
        return acc;
    }

    /* Count the triples for the graph.find */
    public static long countTriples(Graph graph, Node s, Node p, Node o) {
        ExtendedIterator<Triple> iter = graph.find(s, p, o);
        try { return Iter.count(iter); }
        finally { iter.close(); }
    }

    /* Count the matches to a pattern across the dataset  */
    public static long countTriples(DatasetGraph dsg, Node s, Node p, Node o) {
        Iterator<Quad> iter = dsg.find(Node.ANY, s, p, o);
        return Iter.count(iter);
    }

    /** Collect all the matching triples */
    public static void accTriples(Collection<Triple> acc, Graph graph, Node s, Node p, Node o) {
        ExtendedIterator<Triple> iter = graph.find(s, p, o);
        for (; iter.hasNext(); )
            acc.add(iter.next());
        iter.close();
    }

    /** Get exactly one triple or null for none or more than one. */
    public static Triple triple1(Graph graph, Node s, Node p, Node o) {
        ExtendedIterator<Triple> iter = graph.find(s, p, o);
        try {
            if ( !iter.hasNext() )
                return null;
            Triple t = iter.next();
            if ( iter.hasNext() )
                return null;
            return t;
        }
        finally {
            iter.close();
        }
    }

    /** Get exactly one triple, or null for none or more than one. */
    public static Triple triple1(DatasetGraph dsg, Node s, Node p, Node o) {
        Iterator<Quad> iter = dsg.find(Node.ANY, s, p, o);
            if ( !iter.hasNext() )
                return null;
            Quad q = iter.next();
            if ( iter.hasNext() )
                return null;
            return q.asTriple();
    }

    public static boolean strSafeFor(String str, char ch) {
        return str.indexOf(ch) == -1;
    }
    
    public static void writeBase(IndentedWriter out, String base, boolean newStyle) {
        if ( newStyle )
            writeBaseNewStyle(out, base);
        else
            writeBaseOldStyle(out, base);
    }

    private static void writeBaseNewStyle(IndentedWriter out, String base) {
        if ( base != null ) {
            out.print("BASE ");
            out.pad(PREFIX_IRI);
            out.print("<");
            out.print(base);
            out.print(">");
            out.println();
        }
    }

    private static void writeBaseOldStyle(IndentedWriter out, String base) {
        if ( base != null ) {
            out.print("@base ");
            out.pad(PREFIX_IRI);
            out.print("<");
            out.print(base);
            out.print(">");
            out.print(" .");
            out.println();
        }
    }

    /** Write prefixes */ 
    public static void writePrefixes(IndentedWriter out, PrefixMap prefixMap, boolean newStyle) {
        if ( prefixMap != null && !prefixMap.isEmpty() ) {
            for ( Map.Entry<String, String> e : prefixMap.getMapping().entrySet() ) {
                if ( newStyle )
                    writePrefixNewStyle(out, e.getKey(), e.getValue());
                else
                    writePrefixOldStyle(out, e.getKey(), e.getValue());
            }
        }
    }
    
    /** Write a prefix.
     * Write using {@code @prefix} or {@code PREFIX}.
     */ 
    public static void writePrefix(IndentedWriter out, String prefix, String uri, boolean newStyle) {
        if ( newStyle )
            writePrefixNewStyle(out, prefix, uri);
        else
            writePrefixOldStyle(out, prefix, uri);
    }

    /** Write prefix, using {@code PREFIX} */ 
    private static void writePrefixNewStyle(IndentedWriter out, String prefix, String uri) {
        out.print("PREFIX ");
        out.print(prefix);
        out.print(": ");
        out.pad(PREFIX_IRI);
        out.print("<");
        out.print(uri);
        out.print(">");
        out.println();
    }

    /** Write prefixes, using {@code @prefix} */ 
    public static void writePrefixOldStyle(IndentedWriter out, String prefix, String uri) {
        out.print("@prefix ");
        out.print(prefix);
        out.print(": ");
        out.pad(PREFIX_IRI);
        out.print("<");
        out.print(uri);
        out.print(">");
        out.print(" .");
        out.println();
    }

    /** Returns dataset that wraps a graph
     * @deprecated Use {@link DatasetGraphFactory#wrap(Graph)}
     * @param graph
     * @return DatasetGraph
     */
    @Deprecated
    public static DatasetGraph dataset(Graph graph) {
        return DatasetGraphFactory.wrap(graph);
    }

    public static PrefixMap prefixMap(DatasetGraph dsg) {
        return PrefixMapFactory.create(dsg.getDefaultGraph().getPrefixMapping());
    }

    public static int calcWidth(PrefixMap prefixMap, String baseURI, Node p)
    {
        if ( ! prefixMap.containsPrefix(rdfNS) && RDF_type.equals(p) )
            return 1;
        
        String x = prefixMap.abbreviate(p.getURI());
        if ( x == null )
            return p.getURI().length()+2;
        return x.length();
    }

    public static int calcWidth(PrefixMap prefixMap, String baseURI, Collection<Node> nodes, int minWidth, int maxWidth)
    {
        Node prev = null; 
        int nodeMaxWidth = minWidth;
        
        for ( Node n : nodes )
        {
            if ( prev != null && prev.equals(n) )
                continue;
            int len = calcWidth(prefixMap, baseURI, n);
            if ( len > maxWidth )
                continue;
            if ( nodeMaxWidth < len )
                nodeMaxWidth = len;
            prev = n;
        }
        return nodeMaxWidth; 
    }

    public static int calcWidthTriples(PrefixMap prefixMap, String baseURI, Collection<Triple> triples, int minWidth, int maxWidth)
    {
        Node prev = null; 
        int nodeMaxWidth = minWidth;
    
        for ( Triple triple : triples )
        {
            Node n = triple.getPredicate();
            if ( prev != null && prev.equals(n) )
                continue;
            int len = calcWidth(prefixMap, baseURI, n);
            if ( len > maxWidth )
                continue;
            if ( nodeMaxWidth < len )
                nodeMaxWidth = len;
            prev = n;
        }
        return nodeMaxWidth;
    }

    /** IndentedWriter over a java.io.Writer (better to use an IndentedWriter over an OutputStream) */
    public static IndentedWriter create(Writer writer)  { return new IndentedWriterWriter(writer); }

    public static PrefixMap prefixMap(Graph graph)      { return PrefixMapFactory.create(graph.getPrefixMapping()); }

    public static WriterGraphRIOTBase adapter(WriterDatasetRIOT writer)
    { return new WriterAdapter(writer); }

    /** Hidden to direct program to using OutputStreams (for RDF, that gets the charset right) */ 
    private static class IndentedWriterWriter extends IndentedWriter
    {
        IndentedWriterWriter(Writer w) { super(w); }
    }

    private static class WriterAdapter extends WriterGraphRIOTBase
    {
        private WriterDatasetRIOT writer;
    
        WriterAdapter(WriterDatasetRIOT writer) { this.writer = writer; }
        @Override
        public Lang getLang()
        { return writer.getLang(); }
    
        @Override
        public void write(OutputStream out, Graph graph, PrefixMap prefixMap, String baseURI, Context context)
        { writer.write(out, DatasetGraphFactory.wrap(graph), prefixMap, baseURI, context); }
        
        @Override
        public void write(Writer out, Graph graph, PrefixMap prefixMap, String baseURI, Context context)
        { writer.write(out, DatasetGraphFactory.wrap(graph), prefixMap, baseURI, context); }
    }
}
