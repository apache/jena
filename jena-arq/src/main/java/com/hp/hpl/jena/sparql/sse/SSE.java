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

package com.hp.hpl.jena.sparql.sse;

import java.io.* ;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.io.IndentedWriter ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.shared.NotFoundException ;
import com.hp.hpl.jena.shared.PrefixMapping ;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl ;
import com.hp.hpl.jena.sparql.ARQConstants ;
import com.hp.hpl.jena.sparql.ARQException ;
import com.hp.hpl.jena.sparql.algebra.Algebra ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.Table ;
import com.hp.hpl.jena.sparql.core.BasicPattern ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.Prologue ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.expr.Expr ;
import com.hp.hpl.jena.sparql.graph.NodeConst ;
import com.hp.hpl.jena.sparql.path.Path ;
import com.hp.hpl.jena.sparql.serializer.SerializationContext ;
import com.hp.hpl.jena.sparql.sse.builders.* ;
import com.hp.hpl.jena.sparql.sse.lang.ParseHandler ;
import com.hp.hpl.jena.sparql.sse.lang.ParseHandlerPlain ;
import com.hp.hpl.jena.sparql.sse.lang.ParseHandlerResolver ;
import com.hp.hpl.jena.sparql.sse.lang.SSE_Parser ;
import com.hp.hpl.jena.sparql.sse.writers.WriterGraph ;
import com.hp.hpl.jena.sparql.sse.writers.WriterNode ;
import com.hp.hpl.jena.sparql.sse.writers.WriterOp ;
import com.hp.hpl.jena.sparql.util.FmtUtils ;
import com.hp.hpl.jena.util.FileUtils ;

public class SSE
{
    private SSE() {}
    
    // Short prefix map for convenience (used in parsing, not in writing).
    protected static PrefixMapping defaultDefaultPrefixMapRead = new PrefixMappingImpl() ;
    static {
        defaultDefaultPrefixMapRead.setNsPrefix("rdf",  ARQConstants.rdfPrefix) ;
        defaultDefaultPrefixMapRead.setNsPrefix("rdfs", ARQConstants.rdfsPrefix) ;
        defaultDefaultPrefixMapRead.setNsPrefix("xsd",  ARQConstants.xsdPrefix) ;
        defaultDefaultPrefixMapRead.setNsPrefix("owl" , ARQConstants.owlPrefix) ;
        defaultDefaultPrefixMapRead.setNsPrefix("fn" ,  ARQConstants.fnPrefix) ;
        defaultDefaultPrefixMapRead.setNsPrefix("ex" ,  "http://example.org/") ;
        defaultDefaultPrefixMapRead.setNsPrefix("ns" ,  "http://example.org/ns#") ;
        defaultDefaultPrefixMapRead.setNsPrefix("" ,    "http://example/") ;
    }
    
    public static PrefixMapping defaultPrefixMapRead = defaultDefaultPrefixMapRead ;
    public static PrefixMapping getDefaultPrefixMapRead() { return defaultPrefixMapRead ; }
    public static void setDefaultPrefixMapRead(PrefixMapping pmap) { defaultPrefixMapRead =  pmap ; }
    
    // Short prefix map for convenience used in writing.
    protected static PrefixMapping defaultDefaultPrefixMapWrite = new PrefixMappingImpl() ;
    static {
        defaultDefaultPrefixMapWrite.setNsPrefix("rdf",  ARQConstants.rdfPrefix) ;
        defaultDefaultPrefixMapWrite.setNsPrefix("rdfs", ARQConstants.rdfsPrefix) ;
        defaultDefaultPrefixMapWrite.setNsPrefix("xsd",  ARQConstants.xsdPrefix) ;
    }
    
    public static PrefixMapping defaultPrefixMapWrite = defaultDefaultPrefixMapWrite ;
    public static PrefixMapping getDefaultPrefixMapWrite() { return defaultPrefixMapWrite ; }
    public static void setDefaultPrefixMapWrite(PrefixMapping pmap) { defaultPrefixMapWrite =  pmap ; }
    
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
    { return parseBGP(s, getDefaultPrefixMapRead()) ; }
    
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
        {
            Prologue prologue = new Prologue(pmap) ;
            return new ParseHandlerResolver(prologue) ;
        }
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
            pmap = getDefaultPrefixMapRead() ;
        ParseHandler handler = createParseHandler(pmap) ;
        SSE_Parser.term(reader, handler) ; 
        return handler.getItem() ;
    }

    public static Item parse(Reader reader)
    { return parse(reader, null) ; }  

    public static Item parse(Reader reader, PrefixMapping pmap)
    {
        if ( pmap == null )
            pmap = getDefaultPrefixMapRead() ;
        ParseHandler handler = createParseHandler(pmap) ;
        SSE_Parser.parse(reader, handler) ; 
        return handler.getItem() ;
    }
    
    // ---- To String
    public static String format(Node node)                      { return FmtUtils.stringForNode(node) ; }
    public static String format(Node node, PrefixMapping pmap)  { return FmtUtils.stringForNode(node, pmap) ; }
    
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
        WriterNode.output(out, triple, sCxt(defaultDefaultPrefixMapWrite)) ; 
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
        WriterNode.output(out, quad, sCxt(defaultDefaultPrefixMapWrite)) ; 
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
        WriterNode.output(IndentedWriter.stdout, node, sCxt(defaultDefaultPrefixMapWrite)) ;
        IndentedWriter.stdout.flush() ;
    }
    
    /** Return a SerializationContext appropriate for the graph */
    public static SerializationContext sCxt(Graph graph)
    {
        if ( graph != null )
            return sCxt(graph.getPrefixMapping()) ;
        return new SerializationContext() ;
    }  
    
    /** Return a SerializationContext appropriate for the prfix mapping */
    public static SerializationContext sCxt(PrefixMapping pmap)
    {
        if ( pmap != null )
            return new SerializationContext(pmap) ;
        return new SerializationContext() ;
    }  

}
