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

package org.apache.jena.riot.writer;

import java.io.OutputStream ;
import java.util.Objects ;

import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.datatypes.xsd.impl.XMLLiteralType ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.rdf.model.impl.Util ;
import org.apache.jena.riot.RiotException ;
import org.apache.jena.riot.lang.ReaderTriX ;
import org.apache.jena.riot.lang.TriX ;
import org.apache.jena.riot.system.PrefixMap ;
import org.apache.jena.riot.system.PrefixMapFactory ;
import org.apache.jena.riot.system.StreamRDF ;
import org.apache.jena.sparql.core.Quad ;


/** Write TriX by streaming.
 * See {@link TriX} for details.
 *
 * @see TriX
 * @see ReaderTriX
 * @see WriterTriX
 */
public class StreamWriterTriX implements StreamRDF {
    /*
     * Notes on writing qnames:
     * 1/ Currently disabled in favour of the most regular XML output.
     * 2/ Only uses and writes a prefix map given at the start, not inline with StreamRDF.prefix.
     *    Therefore, not available in stream writing mode.
     * 4/ Does not use or write the empty namespace. The empty namespace is used by the trix tags.
     *    (to change, need to change the tags to trix:trix etc and write xmls:trix=)
     */

    private final static String rdfXMLLiteral = XMLLiteralType.XMLLiteralTypeURI;
    private final IndentedWriter out ;
    private Node gn = null ;
    private boolean inGraph = false ;
    private final PrefixMap pmap;
    // StreamRDF.start/finish count
    private int depth = 0 ;

    public StreamWriterTriX(OutputStream out, PrefixMap prefixMap) {
        this(new IndentedWriter(out), prefixMap);
    }

    /*package*/ StreamWriterTriX(IndentedWriter out, PrefixMap prefixMap) {
        this.out = out;
        // Disabled. See notes
        PrefixMap pm = null;
        //PrefixMap pm = prefixMap;
        if ( pm != null && pm.containsPrefix("") ) {
            pm = PrefixMapFactory.create(pm);
            // Can't write ""
            pm.delete("");
        }
        pmap = pm;
    }

    @Override public void start() {
        if ( depth == 0 )
            writeStart();
        depth ++ ;
    }

    @Override public void finish() {
        depth-- ;
        if ( depth != 0 )
            return ;
        writeFinish();
        out.flush() ;
    }

    @Override public void base(String base) {} // Ignore.

    @Override public void prefix(String prefix, String iri) {
        // StreamRDF prefix : ignored.
        // See notes.
    }

    @Override
    public void triple(Triple triple) {
        if ( inGraph && gn != null ) {
            StreamWriterTriX.endTag(out, TriX.tagGraph) ;
            out.println() ;
            inGraph = false ;
        }

        if ( ! inGraph ) {
            StreamWriterTriX.startTag(out, TriX.tagGraph) ;
            out.println() ;
        }
        inGraph = true ;
        gn = null ;
        //end graph?
        StreamWriterTriX.write(out, triple, pmap) ;
    }

    @Override
    public void quad(Quad quad) {
        Node g = quad.getGraph() ;

        if ( g == null || Quad.isDefaultGraph(g) ) {
            triple(quad.asTriple()) ;
            return ;
        }

        if ( inGraph ) {
            if ( ! Objects.equals(g, gn) ) {
                StreamWriterTriX.endTag(out, TriX.tagGraph) ;
                out.println() ;
                inGraph = false ;
            }
        }
        if ( ! inGraph ) {
            StreamWriterTriX.startTag(out, TriX.tagGraph) ;
            out.println() ;
            if ( gn == null || ! Quad.isDefaultGraph(gn) ) {
                gn = quad.getGraph() ;
                StreamWriterTriX.write(out, gn, pmap) ;
            }
        }
        inGraph = true ;
        gn = g ;
        StreamWriterTriX.write(out, quad.asTriple(), pmap) ;
    }

    private static void write(IndentedWriter out, Triple triple, PrefixMap prefixMap) {
        out.println("<triple>") ;
        out.incIndent();
        write(out, triple.getSubject(), prefixMap) ;
        write(out, triple.getPredicate(), prefixMap) ;
        write(out, triple.getObject(), prefixMap) ;
        out.decIndent();
        out.println("</triple>") ;
    }

    private static void write(IndentedWriter out, Node node, PrefixMap prefixMap) {
        if ( node.isURI() ) {
            String uri = node.getURI() ;
            // The decent use of TriX is very regular output as we do not use <qname>.
            // See notes on qnames above.
            if ( prefixMap != null ) {
                String abbrev = prefixMap.abbreviate(uri) ;
                if ( abbrev != null ) {
                    startTag(out, TriX.tagQName) ;
                    writeText(out, abbrev) ;
                    endTag(out, TriX.tagQName) ;
                    out.println() ;
                    return ;
                }
            }

            startTag(out, TriX.tagURI) ;
            writeText(out, node.getURI()) ;
            endTag(out, TriX.tagURI) ;
            out.println() ;
            return ;
        }

        if ( node.isBlank() ) {
            startTag(out, TriX.tagId) ;
            writeText(out, node.getBlankNodeLabel()) ;
            endTag(out, TriX.tagId) ;
            out.println() ;
            return ;
        }

        if ( node.isLiteral() ) {
            // RDF 1.1
            if ( Util.isSimpleString(node) ) {
                startTag(out, TriX.tagPlainLiteral) ;
                writeTextNoIndent(out, node.getLiteralLexicalForm()) ;
                endTag(out, TriX.tagPlainLiteral) ;
                out.println() ;
                return ;
            }

            if ( Util.isLangString(node) ) {
                String lang = node.getLiteralLanguage() ;
                startTag(out, TriX.tagPlainLiteral, "xml:lang", lang) ;
                writeTextNoIndent(out, node.getLiteralLexicalForm()) ;
                endTag(out, TriX.tagPlainLiteral) ;
                out.println() ;
                return ;
            }

            String dt = node.getLiteralDatatypeURI() ;
            startTag(out, TriX.tagTypedLiteral, TriX.attrDatatype, dt) ;
            String lex = node.getLiteralLexicalForm() ;
            if ( rdfXMLLiteral.equals(dt) ) {
                int x = out.getAbsoluteIndent() ;
                out.setAbsoluteIndent(0) ;
                out.print(lex) ;    // Write raw
                out.setAbsoluteIndent(x) ;
            }
            else
                writeTextNoIndent(out, lex) ;
            endTag(out, TriX.tagTypedLiteral) ;
            out.println() ;
            return ;
        }
        if ( node.isNodeTriple() ) {
            StreamWriterTriX.write(out, node.getTriple(), prefixMap) ;
            return;
        }

        throw new RiotException("Not a concrete node: "+node) ;
    }

    private static void writeText(IndentedWriter out, String string) {
        string = Util.substituteEntitiesInElementContent(string) ;
        out.print(string) ;
    }

    private static void writeTextNoIndent(IndentedWriter out, String string) {
        int x = out.getAbsoluteIndent() ;
        out.setAbsoluteIndent(0) ;
        writeText(out, string) ;
        out.setAbsoluteIndent(x) ;
    }

    private static void startXML(IndentedWriter out) {
        //out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>") ;
    }

     void writeStart() {
        StreamWriterTriX.startXML(out) ;
        // Write using the element name from the W3C DTD.
        if ( pmap == null || pmap.isEmpty() )
            StreamWriterTriX.startTag(out, TriX.tagTriX, "xmlns", TriX.NS) ;
        else
            StreamWriterTriX.startTagNS(out, TriX.tagTriX, "xmlns", TriX.NS, pmap) ;
        out.println() ;
    }

    private void writeFinish() {
        if ( inGraph ) {
            StreamWriterTriX.endTag(out, TriX.tagGraph) ;
            out.println() ;
        }
        StreamWriterTriX.endTag(out, TriX.tagTriX) ;
        out.println() ;
    }

    private static void startTag(IndentedWriter out, String text) {
        out.print("<") ;
        out.print(text) ;
        out.print(">") ;
        out.incIndent();
    }

    private static void startTag(IndentedWriter out, String text, String attr, String attrValue) {
        out.print("<") ;
        out.print(text) ;
        out.print(" ") ;
        out.print(attr) ;
        out.print("=\"") ;
        attrValue = Util.substituteStandardEntities(attrValue) ;
        out.print(attrValue) ;
        out.print("\"") ;
        out.print(">") ;
        out.incIndent();
    }

    // Write start with namespaces.
    private static void startTagNS(IndentedWriter out, String text, String attr, String attrValue, PrefixMap pmap) {
        out.print("<") ;
        out.print(text) ;
        out.print(" ") ;
        // Alignment of namespaces.
        int offset = out.getCurrentOffset();

        out.print("xmlns") ;
        out.print("=\"") ;
        attrValue = Util.substituteStandardEntities(attrValue) ;
        out.print(attrValue) ;
        out.print("\"") ;

        // prefixes as XML namespaces.
        // Can't write the empty namespace - it's used by TriX itself in this writer.
        out.incIndent(offset);
        pmap.forEach((ns,uri)->{
            out.println();
            out.print("xmlns:");
            out.print(ns);
            out.print("=");
            out.print("\"");
            uri = Util.substituteStandardEntities(uri);
            out.print(uri);
            out.print("\"");
        });
        out.decIndent(offset);

        out.print(">") ;
        out.incIndent();
    }

    private static void endTag(IndentedWriter out, String text) {
        out.decIndent();
        out.print("</") ;
        out.print(text) ;
        out.print(">") ;
    }
}

