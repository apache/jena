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

package org.apache.jena.riot.resultset.rw;

import java.io.OutputStream;
import java.io.Writer;
import java.util.Objects;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Node_Triple;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.impl.Util;
import org.apache.jena.riot.out.NodeToLabel;
import org.apache.jena.riot.resultset.ResultSetLang;
import org.apache.jena.riot.resultset.ResultSetWriter;
import org.apache.jena.riot.resultset.ResultSetWriterFactory;
import org.apache.jena.riot.system.SyntaxLabels;
import org.apache.jena.sparql.SystemARQ;
import org.apache.jena.sparql.resultset.ResultSetApply;
import org.apache.jena.sparql.resultset.ResultSetException;
import org.apache.jena.sparql.resultset.ResultSetProcessor;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;

public class ResultSetWriterXML implements ResultSetWriter {

    public static final Symbol xmlInstruction = SystemARQ.allocSymbol("xmlInstruction");
    public static final Symbol xmlStylesheet = SystemARQ.allocSymbol("xmlStylesheet");
    
    public static ResultSetWriterFactory factory = lang->{
        if (!Objects.equals(lang, ResultSetLang.SPARQLResultSetXML ) )
            throw new ResultSetException("ResultSetWriter for XML asked for a "+lang); 
        return new ResultSetWriterXML(); 
    };
    
    private ResultSetWriterXML() {}
    
    @Override
    public void write(Writer out, ResultSet resultSet, Context context) {
        throw new UnsupportedOperationException("Writing XML results to a java.io.Writer. Use an OutputStream.") ;
    }

    @Override
    public void write(OutputStream outStream, boolean result, Context context) {
        XMLOutputASK out = new XMLOutputASK(outStream);
        if ( context != null && context.isDefined(xmlInstruction) )
            out.xmlInst = context.isTrue(xmlInstruction);
        if ( context != null && context.isDefined(xmlStylesheet) )
            out.stylesheetURL = (String)(context.get(xmlStylesheet));
        out.exec(result);
    }
    
    @Override
    public void write(OutputStream outStream, ResultSet resultSet, Context context) {
        XMLOutputResultSet xOut = new XMLOutputResultSet(outStream, context);
        if ( context != null && context.isDefined(xmlInstruction) )
            xOut.setXmlInst(context.isTrue(xmlInstruction));
        if ( context != null && context.isDefined(xmlStylesheet) )
            xOut.setStylesheetURL((String)(context.get(xmlStylesheet)));
        ResultSetApply a = new ResultSetApply(resultSet, xOut);
        a.apply();
    }
    
    private class XMLOutputASK implements XMLResults {
        String         stylesheetURL = null;
        IndentedWriter out;
        int            bNodeCounter  = 0;
        boolean        xmlInst       = true;

        public XMLOutputASK(OutputStream outStream) {
            this(outStream, null);
        }

        public XMLOutputASK(OutputStream outStream, String stylesheetURL) {
            this(new IndentedWriter(outStream), stylesheetURL);
        }

        public XMLOutputASK(IndentedWriter indentedOut, String stylesheetURL) {
            out = indentedOut;
            this.stylesheetURL = stylesheetURL;
        }

        public void exec(boolean result) {
            if ( xmlInst )
                out.println("<?xml version=\"1.0\"?>");

            if ( stylesheetURL != null )
                out.println("<?xml-stylesheet type=\"text/xsl\" href=\"" + stylesheetURL + "\"?>");

            out.println("<" + dfRootTag + " xmlns=\"" + dfNamespace + "\">");
            out.incIndent(INDENT);

            // Head
            out.println("<" + dfHead + ">");
            out.incIndent(INDENT);
            if ( false ) {
                String link = "UNSET";
                out.println("<link href=\"" + link + "\"/>");
            }
            out.decIndent(INDENT);
            out.println("</" + dfHead + ">");

            if ( result )
                out.println("<boolean>true</boolean>");
            else
                out.println("<boolean>false</boolean>");
            out.decIndent(INDENT);
            out.println("</" + dfRootTag + ">");
            out.flush();
        }
    }

    private static class XMLOutputResultSet implements ResultSetProcessor, XMLResults
    {
        private static boolean outputExplicitUnbound = false ;
        
        private int index = 0 ;                     // First index is 1 
        private String stylesheetURL = null ;
        private boolean xmlInst = true ;

        private final IndentedWriter  out ;
        private int bNodeCounter = 0 ;
        private final NodeToLabel bNodeMap;
        
        private XMLOutputResultSet(OutputStream outStream, Context context) {
            this(new IndentedWriter(outStream), context);
        }

        private XMLOutputResultSet(IndentedWriter indentedOut, Context context) {
            out = indentedOut;
            boolean outputGraphBNodeLabels = (context != null) && context.isTrue(ARQ.outputGraphBNodeLabels);
            bNodeMap = outputGraphBNodeLabels
                ? SyntaxLabels.createNodeToLabelAsGiven()
                : SyntaxLabels.createNodeToLabel();
        }

        @Override
        public void start(ResultSet rs) {
            if ( xmlInst )
                out.println("<?xml version=\"1.0\"?>");

            if ( stylesheetURL != null ) {
                out.print("<?xml-stylesheet type=\"text/xsl\" href=\"");
                out.print(stylesheetURL);
                out.println("\"?>");
            }

            // ---- Root
            out.print("<");
            out.print(dfRootTag);
            out.print(" xmlns=\"");
            out.print(dfNamespace);
            out.println("\">");

            // ---- Header

            out.incIndent(INDENT);
            out.print("<");
            out.print(dfHead);
            out.println(">");

            if ( false ) {
                String link = "UNSET";
                out.print("<link href=\"");
                out.print(link);
                out.println("\"/>");
            }

            for ( String n : rs.getResultVars() ) {
                out.incIndent(INDENT);
                out.print("<");
                out.print(dfVariable);
                out.print(" ");
                out.print(dfAttrVarName);
                out.print("=\"");
                out.print(n);
                out.print("\"");
                out.println("/>");
                out.decIndent(INDENT);
            }
            out.print("</");
            out.print(dfHead);
            out.println(">");
            out.decIndent(INDENT);

            // Start results proper
            out.incIndent(INDENT);
            out.print("<");
            out.print(dfResults);
            out.println(">");
            out.incIndent(INDENT);
        }

        @Override
        public void finish(ResultSet rs) {
            out.decIndent(INDENT);
            out.print("</");
            out.print(dfResults);
            out.println(">");
            out.decIndent(INDENT);
            out.print("</");
            out.print(dfRootTag);
            out.println(">");
            out.flush();
        }

        @Override
        public void start(QuerySolution qs) {
            out.print("<");
            out.print(dfSolution);
            out.println(">");
            index++;
            out.incIndent(INDENT);
        }

        @Override
        public void finish(QuerySolution qs) {
            out.decIndent(INDENT);
            out.print("</");
            out.print(dfSolution);
            out.println(">");
        }

        @Override
        public void binding(String varName, RDFNode rdfNode) {
            if ( rdfNode == null && !outputExplicitUnbound )
                return;

            out.print("<");
            out.print(dfBinding);
            out.print(" name=\"");
            out.print(varName);
            out.println("\">");
            out.incIndent(INDENT);
            printBindingValue(rdfNode);
            out.decIndent(INDENT);
            out.print("</");
            out.print(dfBinding);
            out.println(">");
        }

        private void printBindingValue(RDFNode rdfNode) {
            if ( rdfNode == null ) {
                // Unbound
                out.print("<");
                out.print(dfUnbound);
                out.println("/>");
                return;
            }

            Node node = rdfNode.asNode();
            printBindingValue(node);
        }
        
        private void printBindingValue(Node node) {
            if ( node == null )
                return;
        
            if ( node.isLiteral() ) {
                printLiteral(node);
                return;
            }

            if ( node.isURI() ) {
                printURI(node);
                return;
            }
            
            if ( node.isBlank() ) {
                printBlankNode(node);
                return;
            }
            if ( node.isNodeTriple() ) {
                printTripleTerm(node);
                return;
            }
            
            if ( node.isNodeGraph() )
                throw new UnsupportedOperationException("Graph terms");

            Log.warn(this, "Unknown RDFNode type in result set: " + node);
        }

        private void printURI(Node nodeURI) {
            String uri = nodeURI.getURI();
            out.print("<");
            out.print(dfURI);
            out.print(">");
            out.print(xml_escape(uri));
            out.print("</");
            out.print(dfURI);
            out.println(">");
        }            
            
        private void printBlankNode(Node node) {
            String label = bNodeMap.get(null, node);
            // Comes with leading "_:"
            label = label.substring(2);
            out.print("<");
            out.print(dfBNode);
            out.print(">");
            out.print(xml_escape(label));
            out.print("</");
            out.print(dfBNode);
            out.println(">");
        }

        private void printLiteral(Node literal) {
            out.print("<");
            out.print(dfLiteral);
        
            if ( Util.isLangString(literal) ) {
                String lang = literal.getLiteralLanguage();
                out.print(" xml:lang=\"");
                out.print(lang);
                out.print("\"");
            } else if ( !Util.isSimpleString(literal) ) {
                // Datatype
                // (RDF 1.1) not xsd:string nor rdf:langString.
                // (RDF 1.0) any datatype.
                String datatype = literal.getLiteralDatatypeURI();
                out.print(" ");
                out.print(dfAttrDatatype);
                out.print("=\"");
                out.print(datatype);
                out.print("\"");
            }
        
            out.print(">");
            out.print(xml_escape(literal.getLiteralLexicalForm()));
            out.print("</");
            out.print(dfLiteral);
            out.println(">");
        }

        private void printTripleTerm(Node node) {
            Triple triple = Node_Triple.triple(node);
            openTag(dfTriple);
            
            // Subject
            openTag(dfSubject);
            printBindingValue(triple.getSubject());
            closeTag(dfSubject);
            // Property
            openTag(dfProperty);
            printBindingValue(triple.getPredicate());
            closeTag(dfProperty);
            // Object
            openTag(dfObject);
            printBindingValue(triple.getObject());
            closeTag(dfObject);
            
            closeTag(dfTriple);
        }
        
        private void openTag(String name) {
            out.print("<");
            out.print(name);
            out.println(">");
            out.incIndent();
        }
        private void closeTag(String name) {
            out.decIndent();
            out.print("</");
            out.print(name);
            out.println(">");
        }

        private static String xml_escape(String string) {
            final StringBuilder sb = new StringBuilder(string);

            int offset = 0;
            String replacement;
            char found;
            for (int i = 0; i < string.length(); i++) {
                found = string.charAt(i);
                
                switch (found) {
                    case '&' : replacement = "&amp;"; break;
                    case '<' : replacement = "&lt;"; break;
                    case '>' : replacement = "&gt;"; break;
                    case '\r': replacement = "&#x0D;"; break;
                    case '\n': replacement = "&#x0A;"; break;
                    default  : replacement = null;
                }
                
                if (replacement != null) {
                    sb.replace(offset + i, offset + i + 1, replacement);
                    offset += replacement.length() - 1; // account for added chars
                }
            }
            
            return sb.toString();
        }

        /** @return Returns the stylesheetURL. */
        public String getStylesheetURL()
        { return stylesheetURL ; }

        /** @param stylesheetURL The stylesheetURL to set. */
        public void setStylesheetURL(String stylesheetURL)
        { this.stylesheetURL = stylesheetURL ; }

        /** @return Returns the xmlInst. */
        public boolean getXmlInst()
        { return xmlInst ; }

        /** @param xmlInst The xmlInst to set. */
        public void setXmlInst(boolean xmlInst)
        { this.xmlInst = xmlInst ; }
    }

}
