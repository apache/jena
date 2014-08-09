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

package com.hp.hpl.jena.sparql.resultset ;

import java.io.IOException ;
import java.io.InputStream ;
import java.io.Reader ;
import java.util.ArrayList ;
import java.util.List ;

import org.apache.jena.atlas.logging.Log ;
import org.xml.sax.* ;
import org.xml.sax.helpers.XMLReaderFactory ;

import com.hp.hpl.jena.datatypes.RDFDatatype ;
import com.hp.hpl.jena.datatypes.TypeMapper ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.ResultSetStream ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory ;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterPlainWrapper ;
import com.hp.hpl.jena.sparql.graph.GraphFactory ;
import com.hp.hpl.jena.sparql.util.FmtUtils ;
import com.hp.hpl.jena.sparql.util.LabelToNodeMap ;
import com.hp.hpl.jena.vocabulary.RDF ;

/** Code that reads an XML Result Set and builds the ARQ structure for the same. */

class XMLInputSAX extends SPARQLResult {
    // See also XMLInputStAX, which is preferred.
    // SAX is not a streaming API - the SAX handler is called as fast as the
    // parser wants to call it, so the parser is calling for all the XML
    // and we have to build an in-memory structure (or the client application
    // would need to be inside the code path of the SAX handler).

    public XMLInputSAX(InputStream in, Model model) {
        worker(new InputSource(in), model) ;
    }

    public XMLInputSAX(Reader in, Model model) {
        worker(new InputSource(in), model) ;
    }

    public XMLInputSAX(String str, Model model) {
        worker(new InputSource(str), model) ;
    }

    private void worker(InputSource in, Model model) {
        if ( model == null )
            model = GraphFactory.makeJenaDefaultModel() ;

        try {
            XMLReader xr = XMLReaderFactory.createXMLReader() ;
            xr.setFeature("http://xml.org/sax/features/namespace-prefixes", true) ;
            // ResultSetXMLHandler1 handler = new ResultSetXMLHandler1() ;
            ResultSetXMLHandler2 handler = new ResultSetXMLHandler2() ;
            xr.setContentHandler(handler) ;
            xr.parse(in) ;
            if ( handler.isBooleanResult ) {
                // Set superclass member
                set(handler.askResult) ;
                return ;
            }
            ResultSetStream rss = new ResultSetStream(handler.variables, model,
                                                      new QueryIterPlainWrapper(handler.results.iterator())) ;
            // Set superclass member
            set(rss) ;
        } catch (SAXException ex) {
            throw new ResultSetException("Problems parsing file (SAXException)", ex) ;
        } catch (IOException ex) {
            throw new ResultSetException("Problems parsing file (IOException)", ex) ;
        }

    }

    static class ResultSetXMLHandler2 implements ContentHandler {
        static final String namespace       = XMLResults.dfNamespace ;
        static final String variableElt     = XMLResults.dfVariable ;
        static final String resultElt       = XMLResults.dfSolution ;

        // Boolean
        boolean             isBooleanResult = false ;
        boolean             askResult       = false ;

        int                 rowCount        = 0 ;
        LabelToNodeMap      bNodes          = LabelToNodeMap.createBNodeMap() ;

        boolean             accumulate      = false ;
        StringBuffer        buff            = new StringBuffer() ;
        List<String>        variables       = new ArrayList<>() ;

        List<Binding>       results         = new ArrayList<>() ;
        // The current solution
        BindingMap          binding         = null ;

        // Note on terminology:
        // A "Binding" in ARQ is a set of name/value pairs
        // In the XML format is it one pair.

        // Current value
        String              varName ;
        String              datatype        = null ;
        String              langTag         = null ;

        String              rdfPrefix       = "rdf" ;

        ResultSetXMLHandler2() {}

        @Override
        public void setDocumentLocator(Locator locator) {}

        @Override
        public void startDocument() throws SAXException {}

        @Override
        public void endDocument() throws SAXException {}

        @Override
        public void startPrefixMapping(String prefix, String uri) throws SAXException {
            if ( uri.equals(RDF.getURI()) )
                rdfPrefix = prefix ;
        }

        @Override
        public void endPrefixMapping(String prefix) throws SAXException {}

        @Override
        public void startElement(String ns, String localName, String qName, Attributes attrs) throws SAXException {
            if ( !ns.equals(namespace) ) {
                // Wrong namespace
                return ;
            }

            // ---- Header

            if ( localName.equals(XMLResults.dfVariable) ) {
                if ( attrs.getValue(XMLResults.dfAttrVarName) != null ) {
                    String name = attrs.getValue(XMLResults.dfAttrVarName) ;
                    variables.add(name) ;
                }
                return ;
            }

            // ---- Results

            if ( localName.equals(XMLResults.dfResults) )
                return ;

            // Boolean
            if ( localName.equals(XMLResults.dfBoolean) ) {
                isBooleanResult = true ;
                // Wait for the content
            }

            // ---- One solution

            if ( localName.equals(XMLResults.dfSolution) ) {
                binding = BindingFactory.create() ;
                return ;
            }

            // One variable

            if ( localName.equals(XMLResults.dfBinding) ) {
                varName = attrs.getValue(XMLResults.dfAttrVarName) ;
                return ;
            }

            // One value

            if ( localName.equals(XMLResults.dfURI) ) {
                startElementURI(ns, localName, qName, attrs) ;
                return ;
            }

            if ( localName.equals(XMLResults.dfLiteral) ) {
                startElementLiteral(ns, localName, qName, attrs) ;
                return ;
            }

            if ( localName.equals(XMLResults.dfBNode) ) {
                startElementBNode(ns, localName, qName, attrs) ;
                return ;
            }

            if ( localName.equals(XMLResults.dfUnbound) )
                return ;

        }

        @Override
        public void endElement(String ns, String localName, String qName) throws SAXException {
            if ( !ns.equals(namespace) ) {
                // Wrong namespace
                return ;
            }

            // ---- Results

            if ( localName.equals(XMLResults.dfResults) )
                return ;

            if ( localName.equals(XMLResults.dfBoolean) ) {
                endElementBoolean() ;
                return ;
            }

            // ---- One solution

            if ( localName.equals(XMLResults.dfSolution) ) {
                varName = null ;
                datatype = null ;
                langTag = null ;
                results.add(binding) ;
                binding = null ;
                return ;
            }

            // ---- One variable

            if ( localName.equals(XMLResults.dfBinding) ) {
                varName = null ;
                return ;
            }
            // ---- One value.

            if ( localName.equals(XMLResults.dfURI) ) {
                endElementURI(ns, localName, qName) ;
                return ;
            }

            if ( localName.equals(XMLResults.dfLiteral) ) {
                endElementLiteral(ns, localName, qName) ;
                return ;
            }

            if ( localName.equals(XMLResults.dfBNode) ) {
                endElementBNode(ns, localName, qName) ;
                return ;
            }

            if ( localName.equals(XMLResults.dfUnbound) )
                return ;

        }

        private boolean checkVarName(String cxtMsg) {
            if ( cxtMsg == null )
                cxtMsg = "" ;

            if ( varName == null ) {
                Log.warn(this, "No variable name in scope: " + cxtMsg) ;
                return false ;
            }
            if ( !variables.contains(varName) ) {
                Log.warn(this, "Variable name '" + varName + "'not declared: " + cxtMsg) ;
                return false ;
            }
            return true ;

        }

        private void startElementURI(String ns, String localName, String name, Attributes attrs) {
            startAccumulate() ;
        }

        private void endElementURI(String ns, String localName, String name) {
            endAccumulate() ;
            String uri = buff.toString() ;
            Node n = NodeFactory.createURI(uri) ;
            if ( checkVarName("URI: " + uri) )
                addBinding(binding, Var.alloc(varName), n) ;
        }

        private void startElementLiteral(String ns, String localName, String name, Attributes attrs) {
            if ( attrs.getValue("datatype") != null )
                datatype = attrs.getValue("datatype") ;

            if ( attrs.getValue("xml:lang") != null )
                langTag = attrs.getValue("xml:lang") ;

            startAccumulate() ;
        }

        private void endElementLiteral(String ns, String localName, String name) {
            endAccumulate() ;
            String lexicalForm = buff.toString() ;

            RDFDatatype dType = null ;
            if ( datatype != null )
                dType = TypeMapper.getInstance().getSafeTypeByName(datatype) ;

            Node n = NodeFactory.createLiteral(lexicalForm.toString(), langTag, dType) ;
            if ( checkVarName("Literal: " + FmtUtils.stringForNode(n)) )
                addBinding(binding, Var.alloc(varName), n) ;

            // Finished value - clear intermediates (the wonders of event based
            // processing)
            this.datatype = null ;
            this.langTag = null ;
            this.varName = null ;
            return ;
        }

        private void endElementBoolean() {
            endAccumulate() ;
            String result = buff.toString() ;
            if ( result.equals("true") ) {
                this.askResult = true ;
                return ;
            }
            if ( result.equalsIgnoreCase("false") ) {
                askResult = false ;
                return ;
            }
            throw new ResultSetException("Unknown boolean value: " + result) ;
        }

        private void startElementBNode(String ns, String localName, String name, Attributes attrs) {
            startAccumulate() ;
        }

        private void endElementBNode(String ns, String localName, String name) {
            endAccumulate() ;
            String bnodeId = buff.toString() ;
            Node node = bNodes.asNode(bnodeId) ;
            if ( checkVarName("BNode: " + bnodeId) )
                addBinding(binding, Var.alloc(varName), node) ;
        }

        private void startAccumulate() {
            buff.setLength(0) ;
            accumulate = true ;
        }

        private void endAccumulate() {
            accumulate = false ;
        }

        @Override
        public void characters(char[] chars, int start, int finish) throws SAXException {
            if ( accumulate ) {
                if ( buff == null )
                    buff = new StringBuffer() ;
                buff.append(chars, start, finish) ;
            }
        }

        @Override
        public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {}

        @Override
        public void processingInstruction(String target, String data) throws SAXException {}

        @Override
        public void skippedEntity(String name) throws SAXException {}
    }
}
