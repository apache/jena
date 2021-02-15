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

package org.apache.jena.sparql.sse;

import java.io.* ;
import java.util.function.Consumer ;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.io.IndentedLineBuffer ;
import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.query.Dataset ;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.shared.NotFoundException ;
import org.apache.jena.shared.PrefixMapping ;
import org.apache.jena.shared.impl.PrefixMappingImpl ;
import org.apache.jena.sparql.ARQConstants ;
import org.apache.jena.sparql.ARQException ;
import org.apache.jena.sparql.algebra.Algebra ;
import org.apache.jena.sparql.algebra.Op ;
import org.apache.jena.sparql.algebra.Table ;
import org.apache.jena.sparql.core.BasicPattern ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.Quad ;
import org.apache.jena.sparql.expr.Expr ;
import org.apache.jena.sparql.expr.ExprList ;
import org.apache.jena.sparql.graph.NodeConst ;
import org.apache.jena.sparql.path.Path ;
import org.apache.jena.sparql.serializer.SerializationContext ;
import org.apache.jena.sparql.sse.builders.* ;
import org.apache.jena.sparql.sse.lang.ParseHandler ;
import org.apache.jena.sparql.sse.lang.ParseHandlerPlain ;
import org.apache.jena.sparql.sse.lang.ParseHandlerResolver ;
import org.apache.jena.sparql.sse.lang.SSE_Parser ;
import org.apache.jena.sparql.sse.writers.WriterExpr ;
import org.apache.jena.sparql.sse.writers.WriterGraph ;
import org.apache.jena.sparql.sse.writers.WriterNode ;
import org.apache.jena.sparql.sse.writers.WriterOp ;
import org.apache.jena.sparql.vocabulary.FOAF ;
import org.apache.jena.sparql.vocabulary.ListPFunction ;
import org.apache.jena.sys.JenaSystem ;
import org.apache.jena.util.FileUtils ;
import org.apache.jena.vocabulary.* ;

/**
 * <a href="https://jena.apache.org/documentation/notes/sse.html"
 * >SPARQL S-Expressions</a> is a unstandardized format for SPARQL-related and now
 * RDF-related objects. This includes use for writing down the SPARQL algebra in Apache
 * Jena ARQ.
 * <p>
 * It has regular syntax, inspired by schema (lisp), making it easy to create and maintain
 * builders and writers and to compose structures.
 * <p>
 * "()" and "[]" are interchangeable and used for visual effect. Expressions are parsed
 * and printed in prefix notation e.g. {@code (+ 1 ?x)}.
 * <p>
 * The operations are grouped into:
 * <ul>
 * <li>{@code parseTYPE} &ndash; parse a string to object of a specific kind.
 * <li>{@code readTYPE} &ndash; Read a file and produce an object of a specific kind.
 * <li>{@code write} &ndash; Write to a stream, default {@code System.out}
 * <li>{@code str} &ndash; Create human readable strings.
 * </ul>
 * <p>
 * {@code parse(...)}, which produces a {@link Item}, is direct access to the syntax parser.
 * Builders take parser {@code Item} and create the in-memory objects (package
 * {@code org.apache.jena.sparql.sse.builders}) and writers output in-memory objects
 * to an {@link IndentedWriter} (package {@code org.apache.jena.sparql.sse.writers}.
 * <p>
 * {@code SSE} should not be considered part of the public, stable Jena APIs.
 * <p>
 * If you don't like lots of "()" and indentation, look away now.
 * <p>
 * Efficiency at scale is not a primary design goal, though the core parser is streaming
 * and would scale.
 */
public class SSE
{
    static { JenaSystem.init(); }

    private SSE() {}

    // Prefix map for convenience (used in parsing and str(), not for writing).
    private static PrefixMapping defaultPrefixMapPretty = new PrefixMappingImpl() ;
    private static void readMap(String prefix, String uri) { defaultPrefixMapPretty.setNsPrefix(prefix, uri) ; }
    static {
        readMap("rdf",      ARQConstants.rdfPrefix) ;
        readMap("rdfs",     ARQConstants.rdfsPrefix) ;
        readMap("xsd",      ARQConstants.xsdPrefix) ;
        readMap("owl",      ARQConstants.owlPrefix) ;
        readMap("foaf",     FOAF.getURI()) ;
        readMap("skos",     SKOS.getURI()) ;
        readMap("skosxl",   SKOSXL.getURI()) ;

        readMap("dc",       DC.getURI()) ;
        readMap("dcterms",  DCTerms.getURI()) ;
        readMap("dctypes",  DCTypes.getURI()) ;

        readMap("fn",       ARQConstants.fnPrefix) ;
        readMap("op",       ARQConstants.fnPrefix) ;
        readMap("math",     ARQConstants.mathPrefix) ;
        readMap("fns",      ARQConstants.fnSparql) ;

        // ARQ
        readMap("afn",      ARQConstants.ARQFunctionLibraryURI) ;
        readMap("apf",      ARQConstants.ARQProcedureLibraryURI) ;
        readMap("agg",      ARQConstants.ARQAggregateLibraryURI) ;
        readMap("list",     ListPFunction.getURI()) ;

        readMap("ex",       "http://example.org/") ;
        readMap("ns",       "http://example.org/ns#") ;
        readMap("",         "http://example/") ;
    }

    protected static PrefixMapping prefixMapRead   = defaultPrefixMapPretty ;
    public static PrefixMapping getPrefixMapRead() { return prefixMapRead ; }
    public static void setPrefixMapRead(PrefixMapping pmap) { prefixMapRead =  pmap ; }

    protected static PrefixMapping prefixMapString = new PrefixMappingImpl() ;
    static {
        prefixMapString.setNsPrefixes(defaultPrefixMapPretty) ;
    }
    public static PrefixMapping getPrefixMapString() { return prefixMapString ; }
    public static void setPrefixMapString(PrefixMapping pmap) { prefixMapString =  pmap ; }

    // Short prefix map for convenience used in writing.
    private static PrefixMapping defaultPrefixMapWrite = new PrefixMappingImpl() ;
    static {
        defaultPrefixMapWrite.setNsPrefix("rdf",  ARQConstants.rdfPrefix) ;
        defaultPrefixMapWrite.setNsPrefix("rdfs", ARQConstants.rdfsPrefix) ;
        defaultPrefixMapWrite.setNsPrefix("xsd",  ARQConstants.xsdPrefix) ;
    }

    protected static PrefixMapping prefixMapWrite = defaultPrefixMapWrite ;
    public static PrefixMapping getPrefixMapWrite() { return prefixMapWrite ; }
    public static void setPrefixMapWrite(PrefixMapping pmap) { prefixMapWrite =  pmap ; }

    /** Parse a string to obtain a Node (see NodeFactory.parse() */
    public static Node parseNode(String str) { return parseNode(str, null) ; }

    /** Parse a string to obtain a Node */
    public static Node parseNode(String str, PrefixMapping pmap)
    {
        return parseNode(new StringReader(str), pmap) ;
    }

    /** Parse a string to obtain a Quad */
    public static Quad parseQuad(String s) { return parseQuad(s, null) ; }

    /** Parse a string to obtain a Quad */
    public static Quad parseQuad(String s, PrefixMapping pmap)
    {
        Item item = parse(s, pmap) ;
        if ( !item.isList() )
            throw new ARQException("Not a list: "+s) ;
        return BuilderGraph.buildQuad(item.getList()) ;
    }

    /** Parse a string to obtain a Triple */
    public static Triple parseTriple(String str) { return parseTriple(str, null) ; }

    /** Parse a string to obtain a Triple */
    public static Triple parseTriple(String str, PrefixMapping pmap)
    {
        Item item = parse(str, pmap) ;
        if ( !item.isList() )
            throw new ARQException("Not a list: "+str) ;
        return BuilderGraph.buildTriple(item.getList()) ;
    }

    /** Parse a string to obtain a path */
    public static Path parsePath(String str) { return parsePath(str, null) ; }

    /** Parse a string to obtain a path */
    public static Path parsePath(String str, PrefixMapping pmap)
    {
        Item item = parse(str, pmap) ;
        if ( !item.isList() )
            throw new ARQException("Not a list: "+str) ;
        return BuilderPath.buildPath(item) ;
    }


    /** Parse a string to obtain a SPARQL expression  */
    public static Expr parseExpr(String s) { return parseExpr(s, null) ; }

    /** Parse a string to obtain a SPARQL expression  */
    public static Expr parseExpr(String s, PrefixMapping pmap)
    {
        Item item = parse(s, pmap) ;
        return BuilderExpr.buildExpr(item) ;
    }

    /** Parse a string to obtain a list of SPARQL expressions  */
    public static ExprList parseExprList(String s) { return parseExprList(s, null) ; }

    /** Parse a string to obtain a list of SPARQL expressions  */
    public static ExprList parseExprList(String s, PrefixMapping pmap) {
        Item item = parse(s, pmap) ;
        return BuilderExpr.buildExprOrExprList(item) ;
    }

    /** Parse a string, and obtain a graph */
    public static Graph parseGraph(String string) { return parseGraph(string, null) ; }

    /** Parse a string, and obtain a graph */
    public static Graph parseGraph(String string, PrefixMapping pmap)
    {
        Item item = parse(string, pmap) ;
        return BuilderGraph.buildGraph(item) ;
    }

    /** Read in a file, parse, and obtain a graph */
    public static Graph readGraph(String filename) { return readGraph(filename, null) ; }

    /** Read in a file, parse, and obtain a graph */
    public static Graph readGraph(String filename, PrefixMapping pmap)
    {
        Item item = readFile(filename, pmap) ;
        return BuilderGraph.buildGraph(item) ;
    }

    /** Read in a file, parse, and obtain a graph */
    public static void readGraph(Graph graph, String filename) { readGraph(graph, filename, null) ; }

    /** Read in a file, parse, and obtain a graph */
    public static void readGraph(Graph graph, String filename, PrefixMapping pmap)
    {
        Item item = readFile(filename, pmap) ;
        BuilderGraph.buildGraph(graph, item) ;
    }

    /** Parse a string, and obtain a DatasetGraph */
    public static DatasetGraph parseDatasetGraph(String string) { return parseDatasetGraph(string, null) ; }

    /** Parse a string, and obtain a graph */
    public static DatasetGraph parseDatasetGraph(String string, PrefixMapping pmap)
    {
        Item item = parse(string, pmap) ;
        return BuilderGraph.buildDataset(item) ;
    }

    /** Read in a file, parse, and obtain a graph */
    public static DatasetGraph readDatasetGraph(String filename) { return readDatasetGraph(filename, null) ; }

    /** Read in a file, parse, and obtain a DatasetGraph */
    public static DatasetGraph readDatasetGraph(String filename, PrefixMapping pmap)
    {
        Item item = readFile(filename, pmap) ;
        return BuilderGraph.buildDataset(item) ;
    }

    /** Read in a file, parse, and obtain a DatasetGraph */
    public static void readDatasetGraph(DatasetGraph dsg, String filename) { readDatasetGraph(dsg, filename, null) ; }

    /** Read in a file, parse, and obtain a DatasetGraph */
    public static void readDatasetGraph(DatasetGraph dsg, String filename, PrefixMapping pmap)
    {
        Item item = readFile(filename, pmap) ;
        BuilderGraph.buildDataset(dsg, item) ;
    }

    /** Read in a file, parse, and obtain a SPARQL algebra op */
    public static Op readOp(String filename) { return Algebra.read(filename) ; }

    /** Parse a string and obtain a SPARQL algebra op */
    public static Op parseOp(String s) { return Algebra.parse(s) ; }

    /** Parse a string and obtain a SPARQL algebra op, given a prefix mapping */
    public static Op parseOp(String s, PrefixMapping pmap) { return Algebra.parse(s, pmap) ; }

    /** Read in a file, parse, and obtain a SPARQL algebra basic graph pattern */
    public static BasicPattern readBGP(String filename)
    {
        Item item = readFile(filename, null) ;
        return BuilderOp.buildBGP(item) ;
    }

    /** Parse a string and obtain a SPARQL algebra basic graph pattern */
    public static BasicPattern parseBGP(String s)
    { return parseBGP(s, getPrefixMapRead()) ; }

    /** Parse a string and obtain a SPARQL algebra basic graph pattern, given a prefix mapping */
    public static BasicPattern parseBGP(String s, PrefixMapping pmap)
    {
        Item item = parse(s, pmap) ;
        return BuilderOp.buildBGP(item) ;
    }

    /** Read a file and obtain a SPARQL algebra table */
    public static Table readTable(String filename) { return readTable(filename, null) ; }

    /** Read a file and obtain a SPARQL algebra table */
    public static Table readTable(String filename, PrefixMapping pmap)
    {
        Item item = readFile(filename, pmap) ;
        return BuilderTable.build(item) ;
    }

    /** Parse a string and obtain a SPARQL algebra table */
    public static Table parseTable(String s) { return parseTable(s, null) ; }

    /** Parse a string and obtain a SPARQL algebra table */
    public static Table parseTable(String s, PrefixMapping pmap)
    {
        Item item = parse(s, pmap) ;
        return BuilderTable.build(item) ;
    }

    /** Read a file and obtain an SSE item expression */
    public static Item readFile(String filename)
    { return readFile(filename, null) ; }


    /** Read a file and obtain an SSE item expression */
    public static Item readFile(String filename, PrefixMapping pmap)
    {
        FileInputStream in = null ;
        try {
            in = new FileInputStream(filename) ;
            long len = in.getChannel().size() ;
            if ( len == 0 )
                return Item.nil ;
            return parse(in, pmap) ;
        }
        catch (FileNotFoundException ex)
        { throw new NotFoundException("Not found: "+filename) ; }
        catch (IOException ex)
        { throw new ARQException("IOExeption: "+filename, ex) ; }
        finally { IO.close(in) ; }
    }

    /** Parse a string and obtain an SSE item expression (no additional prefix mappings)*/
    public static Item parseRaw(String str) { return parse(str, new PrefixMappingImpl()) ; }

    /** Parse a string and obtain an SSE item expression */
    public static Item parse(String str) { return parse(str, null) ; }

    /** Parse a string and obtain an SSE item expression */
    public static Item parse(String str, PrefixMapping pmap)
    {
        return parse(new StringReader(str), pmap) ;
    }

    /** Parse from an input stream and obtain an SSE item expression */
    public static Item parse(InputStream in) { return parse(in, null) ; }

    /** Parse from an input stream and obtain an SSE item expression */
    public static Item parse(InputStream in, PrefixMapping pmap)
    {
        Reader reader = FileUtils.asBufferedUTF8(in) ;
        return parse(reader, pmap) ;
    }

    // ---- Workers

    public static void setUseResolver(boolean flag) { useResolver = flag ; }
    private static boolean useResolver = true ;

    private static ParseHandler createParseHandler(PrefixMapping pmap)
    {
        if ( useResolver )
            return new ParseHandlerResolver(null, pmap) ;
        else
            return new ParseHandlerPlain() ;
    }

    private static Node parseNode(Reader reader, PrefixMapping pmap)
    {
        Item item = parseTerm(reader, pmap) ;
        if ( item.isSymbol() )
        {
            String str = item.getSymbol() ;
            if ( "true".equalsIgnoreCase(str) ) return NodeConst.nodeTrue ;
            if ( "false".equalsIgnoreCase(str) ) return NodeConst.nodeFalse ;
            throw new SSEParseException("Not a node: "+item, item.getLine(), item.getColumn()) ;
        }

        if ( ! item.isNode() )
            throw new SSEParseException("Not a node: "+item, item.getLine(), item.getColumn()) ;
        return item.getNode() ;
    }

    private static String parseSymbol(Reader reader, PrefixMapping pmap)
    {
        Item item = parseTerm(reader, pmap) ;
        if ( ! item.isSymbol() )
            throw new SSEParseException("Not a symbol: "+item, item.getLine(), item.getColumn()) ;
        return item.getSymbol() ;
    }

    public static Item parseItem(String str)
    {
        return parse(str, null) ;
    }

    public static Item parseItem(String str, PrefixMapping pmap)
    {
        return parse(new StringReader(str), pmap) ;
    }

    // --- Parse single elements.

    private static Item parseTerm(Reader reader, PrefixMapping pmap)
    {
        if ( pmap == null )
            pmap = getPrefixMapRead() ;
        ParseHandler handler = createParseHandler(pmap) ;
        SSE_Parser.term(reader, handler) ;
        return handler.getItem() ;
    }

    public static Item parse(Reader reader)
    { return parse(reader, null) ; }

    public static Item parse(Reader reader, PrefixMapping pmap)
    {
        if ( pmap == null )
            pmap = getPrefixMapRead() ;
        ParseHandler handler = createParseHandler(pmap) ;
        SSE_Parser.parse(reader, handler) ;
        return handler.getItem() ;
    }

    // ---- To String
    private static String strForNull = "<<null>>" ;

    public static String str(Node node) {
        return str(node, getPrefixMapString()) ;
    }

    public static String str(Node node, PrefixMapping pmap) {
        if ( node == null )
            return strForNull ;
        return string((out)->WriterNode.output(out, node, sCxt(pmap))) ;
    }

    public static String str(Triple triple) {
        return str(triple, getPrefixMapString()) ;
    }

    public static String str(Triple triple, PrefixMapping pmap) {
        if ( triple == null )
            return strForNull ;
        return string((out)->WriterNode.outputNoTag(out, triple, sCxt(pmap))) ;
    }

    /** Plain - with (), not (triple ...) */
    public static String strPlain(Triple triple) {
        return strPlain(triple, getPrefixMapString()) ;
    }

    /** Plain - with (), not (triple ...) */
    public static String strPlain(Triple triple, PrefixMapping pmap) {
        if ( triple == null )
            return strForNull ;
        return string((out)->WriterNode.outputPlain(out, triple, sCxt(pmap))) ;
    }

    public static String str(Quad quad) {
        return str(quad, getPrefixMapString()) ;
    }

    public static String str(Quad quad, PrefixMapping pmap) {
        if ( quad == null )
            return strForNull ;
        return string((out)->WriterNode.outputNoTag(out, quad, sCxt(pmap))) ;
    }

    /** Plain - with (), not (quad ...) */
    public static String strPlain(Quad quad) {
        return strPlain(quad, getPrefixMapString()) ;
    }

    /** Plain - with (), not (quad ...) */
    public static String strPlain(Quad quad, PrefixMapping pmap) {
        if ( quad == null )
            return strForNull ;
        return string((out)->WriterNode.outputPlain(out, quad, sCxt(pmap))) ;
    }

    public static String str(Graph graph) {
        return str(graph, getPrefixMapString()) ;
    }

    public static String str(Graph graph, PrefixMapping pmap) {
        if ( graph == null )
            return strForNull ;
        return string((out)->WriterGraph.output(out, graph, sCxt(pmap))) ;
    }

    public static String str(DatasetGraph dsg) {
        return str(dsg, getPrefixMapString()) ;
    }

    public static String str(DatasetGraph dsg,  PrefixMapping pmap) {
        if ( dsg == null )
            return strForNull ;
        return string((out)->WriterGraph.output(out, dsg, sCxt(pmap))) ;
    }

    public static String str(Expr expr) {
        return str(expr, getPrefixMapString()) ;
    }

    public static String str(Expr expr, PrefixMapping pmap) {
        if ( expr == null )
            return strForNull ;
        return string((out)->WriterExpr.output(out, expr, sCxt(pmap))) ;
    }

    public static String str(BasicPattern bgp) {
        return str(bgp, getPrefixMapString()) ;
    }

    public static String str(BasicPattern bgp, PrefixMapping pmap) {
        if ( bgp == null )
            return strForNull ;
        return string((out)->WriterGraph.output(out, bgp, sCxt(pmap))) ;
    }

    public static String str(Op op) {
        return str(op, getPrefixMapString()) ;
    }

    public static String str(Op op, PrefixMapping pmap) {
        if ( op == null )
            return strForNull ;
        return string((out)->WriterOp.output(out, op, sCxt(pmap))) ;
    }

//    public static String str(ResultSet rs) {
//        return str(rs, getPrefixMapString()) ;
//    }
//
//    public static String str(ResultSet rs, PrefixMapping pmap) {
//        return string((out)->Writer???.output(out, rs, sCxt(pmap))) ;
//    }


    private static String string(Consumer<IndentedLineBuffer> action) {
        IndentedLineBuffer x = new IndentedLineBuffer() ;
        action.accept(x);
        return x.asString() ;
    }

    // ----

    public static void write(Op op) { WriterOp.output(IndentedWriter.stdout, op) ; IndentedWriter.stdout.flush() ; }
    public static void write(OutputStream out, Op op) { WriterOp.output(out, op) ; }
    public static void write(IndentedWriter out, Op op) { WriterOp.output(out, op) ; }

    public static void write(Graph graph)
    {
        write(IndentedWriter.stdout, graph) ;
        IndentedWriter.stdout.flush() ;
    }
    public static void write(OutputStream out, Graph graph)
    {
        IndentedWriter iOut = new IndentedWriter(out) ;
        write(iOut, graph) ;
        iOut.flush();
    }

    public static void write(IndentedWriter out, Graph graph)
    {
        WriterGraph.output(out, graph,
                           new SerializationContext(graph.getPrefixMapping())) ;
        out.ensureStartOfLine() ;
    }

    public static void write(Model model)
    {
        write(IndentedWriter.stdout, model) ;
        IndentedWriter.stdout.flush() ;
    }
    public static void write(OutputStream out, Model model)
    {
        IndentedWriter iOut = new IndentedWriter(out) ;
        write(iOut, model) ;
        iOut.flush();
    }

    public static void write(IndentedWriter out, Model model)
    {
        WriterGraph.output(out, model.getGraph(), new SerializationContext(model)) ;
    }



    public static void write(DatasetGraph dataset) { write(IndentedWriter.stdout, dataset) ; IndentedWriter.stdout.flush() ; }
    public static void write(OutputStream out, DatasetGraph dataset)
    {
        IndentedWriter iOut = new IndentedWriter(out) ;
        write(iOut, dataset) ;
        iOut.flush();
    }

    public static void write(IndentedWriter out, DatasetGraph dataset)
    {
        WriterGraph.output(out, dataset, sCxt(dataset.getDefaultGraph())) ;
    }

    public static void write(Dataset dataset)                       { write(dataset.asDatasetGraph()) ; }
    public static void write(OutputStream out, Dataset dataset)     { write(out, dataset.asDatasetGraph()) ; }
    public static void write(IndentedWriter out, Dataset dataset)   { write(out, dataset.asDatasetGraph()) ; }

    public static void write(BasicPattern pattern)                  { write(IndentedWriter.stdout, pattern) ; IndentedWriter.stdout.flush() ; }

    public static void write(IndentedWriter out, BasicPattern pattern)
    { write(IndentedWriter.stdout, pattern, null) ; IndentedWriter.stdout.flush() ; }

    public static void write(IndentedWriter out, BasicPattern pattern, PrefixMapping pMap)
    {
        WriterGraph.output(out, pattern, sCxt(pMap)) ;
        out.flush() ;
    }

    public static void write(Triple triple) { write(IndentedWriter.stdout, triple) ; IndentedWriter.stdout.flush() ; }
    public static void write(OutputStream out, Triple triple)
    {
        IndentedWriter iOut = new IndentedWriter(out) ;
        write(iOut, triple) ;
        iOut.flush();
    }
    public static void write(IndentedWriter out, Triple triple)
    {
        WriterNode.output(out, triple, sCxt(getPrefixMapWrite())) ;
        out.flush() ;
    }

    public static void write(Quad quad) { write(IndentedWriter.stdout, quad) ; IndentedWriter.stdout.flush() ; }
    public static void write(OutputStream out, Quad quad)
    {
        IndentedWriter iOut = new IndentedWriter(out) ;
        write(iOut, quad) ;
        iOut.flush();
    }
    public static void write(IndentedWriter out, Quad quad)
    {
        WriterNode.output(out, quad, sCxt(getPrefixMapWrite())) ;
        out.flush() ;
    }


    public static void write(Node node) { write(IndentedWriter.stdout, node) ; IndentedWriter.stdout.flush() ; }
    public static void write(OutputStream out, Node node)
    {
        IndentedWriter iOut = new IndentedWriter(out) ;
        write(iOut, node) ;
        iOut.flush();
    }
    public static void write(IndentedWriter out, Node node)
    {
        WriterNode.output(IndentedWriter.stdout, node, sCxt(getPrefixMapWrite())) ;
        IndentedWriter.stdout.flush() ;
    }

    /** Return a SerializationContext appropriate for the graph */
    public static SerializationContext sCxt(Graph graph)
    {
        if ( graph != null )
            return sCxt(graph.getPrefixMapping()) ;
        return new SerializationContext() ;
    }

    /** Return a SerializationContext appropriate for the prefix mapping */
    public static SerializationContext sCxt(PrefixMapping pmap)
    {
        if ( pmap != null )
            return new SerializationContext(pmap) ;
        return new SerializationContext() ;
    }

}
