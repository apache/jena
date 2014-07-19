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

import static org.apache.jena.riot.RDFLanguages.NQUADS ;
import static org.apache.jena.riot.RDFLanguages.NTRIPLES ;
import static org.apache.jena.riot.RDFLanguages.RDFJSON ;
import static org.apache.jena.riot.RDFLanguages.sameLang ;
import static org.apache.jena.riot.writer.WriterConst.PREFIX_IRI ;
import static org.apache.jena.riot.writer.WriterConst.RDF_type ;
import static org.apache.jena.riot.writer.WriterConst.rdfNS ;

import java.io.OutputStream ;
import java.io.Writer ;
import java.util.* ;

import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFLanguages ;
import org.apache.jena.riot.SysRIOT ;
import org.apache.jena.riot.WriterDatasetRIOT ;
import org.apache.jena.riot.lang.LabelToNode ;
import org.apache.jena.riot.tokens.Token ;
import org.apache.jena.riot.tokens.Tokenizer ;
import org.apache.jena.riot.tokens.TokenizerFactory ;
import org.apache.jena.riot.writer.WriterGraphRIOTBase ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.rdf.model.AnonId ;
import com.hp.hpl.jena.sparql.ARQConstants ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.DatasetGraphFactory ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.util.Context ;
import com.hp.hpl.jena.util.iterator.ExtendedIterator ;

/** Misc RIOT code */
public class RiotLib
{
    private final static String bNodeLabelStart = "_:" ;
    private final static boolean skolomizedBNodes = ARQ.isTrue(ARQ.constantBNodeLabels) ;
    
    /** Implement <_:....> as a 2bNode IRI"
     * that is, use the given label as the BNode internal label.
     * Use with care.
     */
    public static Node createIRIorBNode(String iri)
    {
        // Is it a bNode label? i.e. <_:xyz>
        if ( isBNodeIRI(iri) )
        {
            String s = iri.substring(bNodeLabelStart.length()) ;
            Node n = NodeFactory.createAnon(new AnonId(s)) ;
            return n ;
        }
        return NodeFactory.createURI(iri) ;
    }

    /** Test whether  */
    public static boolean isBNodeIRI(String iri)
    {
        return skolomizedBNodes && iri.startsWith(bNodeLabelStart) ;
    }
    
    private static ParserProfile profile = profile(RDFLanguages.TURTLE, null, null) ;
    static {
        PrefixMap pmap = profile.getPrologue().getPrefixMap() ;
        pmap.add("rdf",  ARQConstants.rdfPrefix) ;
        pmap.add("rdfs", ARQConstants.rdfsPrefix) ;
        pmap.add("xsd",  ARQConstants.xsdPrefix) ;
        pmap.add("owl" , ARQConstants.owlPrefix) ;
        pmap.add("fn" ,  ARQConstants.fnPrefix) ; 
        pmap.add("op" ,  ARQConstants.fnPrefix) ; 
        pmap.add("ex" ,  "http://example/ns#") ;
        pmap.add("" ,    "http://example/") ;
    }
    
    /** Parse a string to get one Node (the first token in the string) */ 
    public static Node parse(String string)
    {
        Tokenizer tokenizer = TokenizerFactory.makeTokenizerString(string) ;
        if ( ! tokenizer.hasNext() )
            return null ;
        Token t = tokenizer.next();
        Node n = profile.create(null, t) ;
        if ( tokenizer.hasNext() )
            Log.warn(RiotLib.class, "String has more than one token in it: "+string) ;
        return n ;
    }

    public static ParserProfile profile(Lang lang, String baseIRI)
    {
        return profile(lang, baseIRI, ErrorHandlerFactory.getDefaultErrorHandler()) ;
    }

    public static ParserProfile profile(Lang lang, String baseIRI, ErrorHandler handler)
    {
        if ( sameLang(NTRIPLES, lang) || sameLang(NQUADS, lang) )
        {
            boolean checking = SysRIOT.strictMode ;
            // If strict mode, do checking e.g. URIs
            return profile(baseIRI, false, checking, handler) ;
        }
        if ( sameLang(RDFJSON, lang) )
            return profile(baseIRI, false, true, handler) ;
        return profile(baseIRI, true, true, handler) ;
    }

    /** Create a parser profile for the given setup
     * @param baseIRI       Base IRI
     * @param resolveIRIs   Whether to resolve IRIs
     * @param checking      Whether to check 
     * @param handler       Error handler
     * @return ParserProfile
     * @see #profile for per-language setup
     */
    public static ParserProfile profile(String baseIRI, boolean resolveIRIs, boolean checking, ErrorHandler handler)
    {
        LabelToNode labelToNode = true
            ? SyntaxLabels.createLabelToNode()
            : LabelToNode.createUseLabelEncoded() ;
        
        Prologue prologue ;
        if ( resolveIRIs )
            prologue = new Prologue(PrefixMapFactory.createForInput(), IRIResolver.create(baseIRI)) ;
        else
            prologue = new Prologue(PrefixMapFactory.createForInput(), IRIResolver.createNoResolve()) ;
    
        if ( checking )
            return new ParserProfileChecker(prologue, handler, labelToNode) ;
        else
            return new ParserProfileBase(prologue, handler, labelToNode) ;
    }

    /** Get triples with the same subject */
    public static Collection<Triple> triplesOfSubject(Graph graph, Node subj) {
        return triples(graph, subj, Node.ANY, Node.ANY) ;
    }

    /** Get all the triples for the graph.find */
    public static List<Triple> triples(Graph graph, Node s, Node p, Node o) {
        List<Triple> acc = new ArrayList<>() ;
        accTriples(acc, graph, s, p, o) ;
        return acc ;
    }

    /* Count the triples for the graph.find */
    public static long countTriples(Graph graph, Node s, Node p, Node o) {
        ExtendedIterator<Triple> iter = graph.find(s, p, o) ;
        try { return Iter.count(iter) ; }
        finally { iter.close() ; }
    }

    /* Count the matches to a pattern across the dataset  */
    public static long countTriples(DatasetGraph dsg, Node s, Node p, Node o) {
        Iterator<Quad> iter = dsg.find(Node.ANY, s, p, o) ;
        return Iter.count(iter) ;
    }

    /** Collect all the matching triples */
    public static void accTriples(Collection<Triple> acc, Graph graph, Node s, Node p, Node o) {
        ExtendedIterator<Triple> iter = graph.find(s, p, o) ;
        for ( ; iter.hasNext() ; )
            acc.add(iter.next()) ;
        iter.close() ;
    }

    /** Get exactly one triple or null for none or more than one. */
    public static Triple triple1(Graph graph, Node s, Node p, Node o) {
        ExtendedIterator<Triple> iter = graph.find(s, p, o) ;
        try {
            if ( !iter.hasNext() )
                return null ;
            Triple t = iter.next() ;
            if ( iter.hasNext() )
                return null ;
            return t ;
        }
        finally {
            iter.close() ;
        }
    }

    /** Get exactly one triple, or null for none or more than one. */
    public static Triple triple1(DatasetGraph dsg, Node s, Node p, Node o) {
        Iterator<Quad> iter = dsg.find(Node.ANY, s, p, o) ;
            if ( !iter.hasNext() )
                return null ;
            Quad q = iter.next() ;
            if ( iter.hasNext() )
                return null ;
            return q.asTriple() ;
    }

    public static boolean strSafeFor(String str, char ch) {
        return str.indexOf(ch) == -1 ;
    }

    public static void writeBase(IndentedWriter out, String base) {
        if ( base != null ) {
            out.print("@base ") ;
            out.pad(PREFIX_IRI) ;
            out.print("<") ;
            out.print(base) ;
            out.print(">") ;
            out.print(" .") ;
            out.println() ;
        }
    }

    public static void writePrefixes(IndentedWriter out, PrefixMap prefixMap) {
        if ( prefixMap != null && !prefixMap.isEmpty() ) {
            for ( Map.Entry<String, String> e : prefixMap.getMappingCopyStr().entrySet() ) {
                out.print("@prefix ") ;
                out.print(e.getKey()) ;
                out.print(": ") ;
                out.pad(PREFIX_IRI) ;
                out.print("<") ;
                out.print(e.getValue()) ;
                out.print(">") ;
                out.print(" .") ;
                out.println() ;
            }
        }
    }

    /** Returns dataset that wraps a graph */
    public static DatasetGraph dataset(Graph graph)
    {
        return DatasetGraphFactory.createOneGraph(graph) ;
    }

    public static PrefixMap prefixMap(DatasetGraph dsg)
    {
        return PrefixMapFactory.create(dsg.getDefaultGraph().getPrefixMapping()) ;
    }

    private static int calcWidth(PrefixMap prefixMap, String baseURI, Node p)
    {
        if ( ! prefixMap.contains(rdfNS) && RDF_type.equals(p) )
            return 1 ;
        
        String x = prefixMap.abbreviate(p.getURI()) ;
        if ( x == null )
            return p.getURI().length()+2 ;
        return x.length() ;
    }

    public static int calcWidth(PrefixMap prefixMap, String baseURI, Collection<Node> nodes, int minWidth, int maxWidth)
    {
        Node prev = null ; 
        int nodeMaxWidth = minWidth ;
        
        for ( Node n : nodes )
        {
            if ( prev != null && prev.equals(n) )
                continue ;
            int len = calcWidth(prefixMap, baseURI, n) ;
            if ( len > maxWidth )
                continue ;
            if ( nodeMaxWidth < len )
                nodeMaxWidth = len ;
            prev = n ;
        }
        return nodeMaxWidth ; 
    }

    public static int calcWidthTriples(PrefixMap prefixMap, String baseURI, Collection<Triple> triples, int minWidth, int maxWidth)
    {
        Node prev = null ; 
        int nodeMaxWidth = minWidth ;
    
        for ( Triple triple : triples )
        {
            Node n = triple.getPredicate() ;
            if ( prev != null && prev.equals(n) )
                continue ;
            int len = calcWidth(prefixMap, baseURI, n) ;
            if ( len > maxWidth )
                continue ;
            if ( nodeMaxWidth < len )
                nodeMaxWidth = len ;
            prev = n ;
        }
        return nodeMaxWidth ;
    }

    /** IndentedWriter over a jaav.io.Writer (better to use an IndentedWriter over an OutputStream) */
    public static IndentedWriter create(Writer writer)  { return new IndentedWriterWriter(writer) ; }

    public static PrefixMap prefixMap(Graph graph)      { return PrefixMapFactory.create(graph.getPrefixMapping()) ; }

    public static WriterGraphRIOTBase adapter(WriterDatasetRIOT writer)
    { return new WriterAdapter(writer) ; }

    /** Hidden to direct program to using OutputStreams (for RDF, that gets the charset right) */ 
    private static class IndentedWriterWriter extends IndentedWriter
    {
        IndentedWriterWriter(Writer w) { super(w) ; }
    }

    private static class WriterAdapter extends WriterGraphRIOTBase
    {
        private WriterDatasetRIOT writer ;
    
        WriterAdapter(WriterDatasetRIOT writer) { this.writer = writer ; }
        @Override
        public Lang getLang()
        { return writer.getLang() ; }
    
        @Override
        public void write(OutputStream out, Graph graph, PrefixMap prefixMap, String baseURI, Context context)
        { writer.write(out, RiotLib.dataset(graph), prefixMap, baseURI, context) ; }
        
        @Override
        public void write(Writer out, Graph graph, PrefixMap prefixMap, String baseURI, Context context)
        { writer.write(out, RiotLib.dataset(graph), prefixMap, baseURI, context) ; }
    }
}
