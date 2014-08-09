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

import java.io.InputStream ;
import java.io.Reader ;
import java.io.StringReader ;
import java.util.ArrayList ;
import java.util.List ;
import java.util.NoSuchElementException ;

import javax.xml.namespace.QName ;
import javax.xml.stream.XMLInputFactory ;
import javax.xml.stream.XMLStreamConstants ;
import javax.xml.stream.XMLStreamException ;
import javax.xml.stream.XMLStreamReader ;

import org.apache.jena.atlas.lib.Closeable ;
import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.atlas.logging.Log ;

import com.hp.hpl.jena.datatypes.RDFDatatype ;
import com.hp.hpl.jena.datatypes.TypeMapper ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.query.QuerySolution ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.rdf.model.AnonId ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.sparql.ARQConstants ;
import com.hp.hpl.jena.sparql.core.ResultBinding ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory ;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap ;
import com.hp.hpl.jena.sparql.graph.GraphFactory ;
import com.hp.hpl.jena.sparql.util.LabelToNodeMap ;

/**
 * Code that reads an XML Results format and builds the ARQ structure for the
 * same. Can read result set and boolean result forms. This is a streaming
 * implementation.
 */

class XMLInputStAX extends SPARQLResult {
    private static final String XML_NS = ARQConstants.XML_NS ;

    public static ResultSet fromXML(InputStream in) {
        return fromXML(in, null) ;
    }

    public static ResultSet fromXML(InputStream in, Model model) {
        XMLInputStAX x = new XMLInputStAX(in, model) ;
        if ( !x.isResultSet() )
            throw new ResultSetException("Not a result set") ;
        return x.getResultSet() ;
    }

    public static ResultSet fromXML(String str) {
        return fromXML(str, null) ;

    }

    public static ResultSet fromXML(String str, Model model) {
        XMLInputStAX x = new XMLInputStAX(str, model) ;
        if ( !x.isResultSet() )
            throw new ResultSetException("Not a result set") ;
        return x.getResultSet() ;
    }

    public static boolean booleanFromXML(InputStream in) {
        XMLInputStAX x = new XMLInputStAX(in) ;
        return x.getBooleanResult() ;
    }

    public static boolean booleanFromXML(String str) {
        XMLInputStAX x = new XMLInputStAX(str) ;
        return x.getBooleanResult() ;
    }

    public XMLInputStAX(InputStream in) {
        this(in, null) ;
    }

    public XMLInputStAX(InputStream in, Model model) {
        XMLInputFactory xf = XMLInputFactory.newInstance() ;
        try {
            XMLStreamReader xReader = xf.createXMLStreamReader(in) ;
            worker(xReader, model) ;
        } catch (XMLStreamException e) {
            throw new ResultSetException("Can't initialize StAX parsing engine", e) ;
        } catch (Exception ex) {
            throw new ResultSetException("Failed when initializing the StAX parsing engine", ex) ;
        }
    }

    public XMLInputStAX(Reader in, Model model) {
        XMLInputFactory xf = XMLInputFactory.newInstance() ;
        try {
            XMLStreamReader xReader = xf.createXMLStreamReader(in) ;
            worker(xReader, model) ;
        } catch (XMLStreamException e) {
            throw new ResultSetException("Can't initialize StAX parsing engine", e) ;
        } catch (Exception ex) {
            throw new ResultSetException("Failed when initializing the StAX parsing engine", ex) ;
        }
    }

    public XMLInputStAX(String str) {
        this(str, null) ;
    }

    public XMLInputStAX(String str, Model model) {
        XMLInputFactory xf = XMLInputFactory.newInstance() ;
        try {
            Reader r = new StringReader(str) ;
            XMLStreamReader xReader = xf.createXMLStreamReader(r) ;
            worker(xReader, model) ;
        } catch (XMLStreamException e) {
            throw new ResultSetException("Can't initialize StAX parsing engine", e) ;
        } catch (Exception ex) {
            throw new ResultSetException("Failed when initializing the StAX parsing engine", ex) ;
        }
    }

    private void worker(XMLStreamReader xReader, Model model) {
        if ( model == null )
            model = GraphFactory.makeJenaDefaultModel() ;

        ResultSetStAX rss = new ResultSetStAX(xReader, model) ;
        if ( rss.isResultSet )
            set(rss) ;
        else
            set(rss.askResult) ;
    }

    // private XMLInputStAX()

    // -------- Result Set

    static class ResultSetStAX implements ResultSet, Closeable {
        // ResultSet variables
        QuerySolution   current          = null ;
        XMLStreamReader parser           = null ;
        List<String>    variables        = new ArrayList<>() ;
        Binding         binding          = null ;                                 // Current
                                                                                   // binding
        // RefBoolean inputGraphLabels = new
        // RefBoolean(ARQ.inputGraphBNodeLabels, false) ;
        boolean         inputGraphLabels = ARQ.isTrue(ARQ.inputGraphBNodeLabels) ;

        LabelToNodeMap  bNodes           = LabelToNodeMap.createBNodeMap() ;

        // Type
        boolean         isResultSet      = false ;

        // Result set
        boolean         ordered          = false ;
        boolean         distinct         = false ;
        boolean         finished         = false ;
        Model           model            = null ;
        int             row              = 0 ;

        // boolean
        boolean         askResult        = false ;

        ResultSetStAX(XMLStreamReader reader, Model model) {
            parser = reader ;
            this.model = model ;
            init() ;
        }

        private void init() {
            try {
                // Because all the tags are different, we could use one big
                // switch statement!
                skipTo(XMLResults.dfHead) ;
                processHead() ;
                skipTo(new String[]{XMLResults.dfResults, XMLResults.dfBoolean}, new String[]{XMLResults.dfResults}) ;
                // Next should be a <result>, <boolean> element or </results>

                // Need to decide what sort of thing we are reading.

                String tag = parser.getLocalName() ;
                if ( tag.equals(XMLResults.dfResults) ) {
                    isResultSet = true ;
                    processResults() ;
                }
                if ( tag.equals(XMLResults.dfBoolean) ) {
                    isResultSet = false ;
                    processBoolean() ;
                }

            } catch (XMLStreamException ex) {
                Log.warn(this, "XMLStreamException: " + ex.getMessage(), ex) ;
            }
        }

        @Override
        public boolean hasNext() {
            if ( !isResultSet )
                throw new ResultSetException("Not an XML result set") ;

            if ( finished )
                return false ;

            try {
                if ( binding == null )
                    binding = getOneSolution() ;
            } catch (XMLStreamException ex) {
                staxError("XMLStreamException: " + ex.getMessage(), ex) ;
            }
            boolean b = (binding != null) ;
            if ( !b )
                close() ;
            return b ;
        }

        @Override
        public QuerySolution next() {
            return nextSolution() ;
        }

        @Override
        public Binding nextBinding() {
            if ( finished )
                throw new NoSuchElementException("End of XML Results") ;
            if ( !hasNext() )
                throw new NoSuchElementException("End of XML Results") ;
            Binding r = binding ;
            row++ ;
            binding = null ;
            return r ;
        }

        @Override
        public QuerySolution nextSolution() {
            Binding r = nextBinding() ;
            ResultBinding currentEnv = new ResultBinding(model, r) ;
            return currentEnv ;
        }

        @Override
        public int getRowNumber() {
            return row ;
        }

        @Override
        public List<String> getResultVars() {
            return variables ;
        }

        public boolean isOrdered() {
            return ordered ;
        }

        public boolean isDistinct() {
            return distinct ;
        }

        // No model - it was from a stream
        @Override
        public Model getResourceModel() {
            return null ;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException(XMLInputStAX.class.getName()) ;
        }

        @Override
        public void close() {
            if ( finished )
                return ;
            finished = true ;
            try { parser.close() ; } catch (XMLStreamException ex) {}
        }

        // -------- Boolean stuff

        private void processBoolean() throws XMLStreamException {
            try {
                // At start of <boolean>
                String s = parser.getElementText() ;
                if ( s.equalsIgnoreCase("true") ) {
                    askResult = true ;
                    return ;
                }
                if ( s.equalsIgnoreCase("false") ) {
                    askResult = false ;
                    return ;
                }
                throw new ResultSetException("Unknown boolean value: " + s) ;
            } finally {
                close() ;
            }
        }

        // --------

        private void skipTo(String tag1) throws XMLStreamException {
            skipTo(new String[]{tag1}, null) ;
        }

        private void skipTo(String[] startElementNames, String[] stopElementNames) throws XMLStreamException {
            boolean found = false ;
            loop : while (parser.hasNext()) {
                int event = parser.next() ;
                switch (event) {
                    case XMLStreamConstants.END_DOCUMENT :
                        break loop ;
                    case XMLStreamConstants.END_ELEMENT :
                        if ( stopElementNames == null )
                            break ;

                        String endTag = parser.getLocalName() ;
                        if ( endTag != null && containsName(stopElementNames, endTag) )
                            return ;
                        break ;
                    case XMLStreamConstants.START_ELEMENT :
                        if ( startElementNames == null )
                            break ;
                        QName qname = parser.getName() ;
                        if ( !qname.getNamespaceURI().equals(XMLResults.baseNamespace) )
                            staxError("skipToHead: Unexpected tag: " + qname) ;
                        if ( containsName(startElementNames, qname.getLocalPart()) )
                            return ;
                        break ;
                    default :
                        // Skip stuff
                }
            }

            if ( !found ) {
                String s1 = "" ;
                if ( startElementNames != null )
                    s1 = StrUtils.strjoin(", ", startElementNames) ;

                String s2 = "" ;
                if ( stopElementNames != null )
                    s2 = StrUtils.strjoin(", ", stopElementNames) ;
                Log.warn(this, "Failed to find start and stop of specified elements: " + s1 + " :: " + s2) ;
            }
        }

        private boolean containsName(String[] elementNames, String eName) {
            for ( String s : elementNames )
            {
                if ( s.equals( eName ) )
                {
                    return true;
                }
            }
            return false ;
        }

        private void processHead() throws XMLStreamException {
            // Should be at the start of head

            loop : while (parser.hasNext()) {
                int event = parser.next() ;
                String tag = null ;

                switch (event) {
                    case XMLStreamConstants.END_DOCUMENT :
                        break loop ;
                    case XMLStreamConstants.END_ELEMENT :
                        tag = parser.getLocalName() ;
                        if ( isTag(tag, XMLResults.dfHead) )
                            break loop ;
                        break ;
                    case XMLStreamConstants.START_ELEMENT :
                        tag = parser.getLocalName() ;
                        if ( isTag(tag, XMLResults.dfHead) )
                            break ; // This switch statement
                        if ( isTag(tag, XMLResults.dfVariable) ) {
                            String varname = parser.getAttributeValue(null, XMLResults.dfAttrVarName) ;
                            variables.add(varname) ;
                            break ;
                        }
                        if ( isTag(tag, XMLResults.dfLink) )
                            break ;

                        staxError("Unknown XML element: " + tag) ;
                        break ;
                    default :
                }
            }
        }

        // -------- Result Set

        private void processResults() {
            return ;
        }

        private Binding getOneSolution() throws XMLStreamException {
            if ( finished )
                return null ;
            // At the start of <result>
            BindingMap binding = BindingFactory.create() ;
            String varName = null ;
            while (parser.hasNext()) {
                int event = parser.next() ;
                String tag = null ;

                switch (event) {
                    case XMLStreamConstants.END_DOCUMENT :
                        staxError("End of document while processing solution") ;
                        return null ;
                    case XMLStreamConstants.END_ELEMENT :
                        tag = parser.getLocalName() ;
                        if ( isTag(tag, XMLResults.dfSolution) )
                            return binding ;
                        if ( isTag(tag, XMLResults.dfResults) )
                            // Hit the end of solutions.
                            return null ;
                        break ;
                    case XMLStreamConstants.START_ELEMENT :
                        tag = parser.getLocalName() ;
                        if ( isTag(tag, XMLResults.dfSolution) ) {
                            binding = BindingFactory.create() ;
                            break ;
                        }
                        if ( isTag(tag, XMLResults.dfBinding) ) {
                            varName = parser.getAttributeValue(null, XMLResults.dfAttrVarName) ;
                            break ;
                        }
                        // URI, literal, bNode, unbound.
                        if ( isTag(tag, XMLResults.dfBNode) ) {
                            String label = parser.getElementText() ;
                            Node node = null ;
                            // if ( inputGraphLabels.getValue() )
                            if ( inputGraphLabels )
                                node = NodeFactory.createAnon(new AnonId(label)) ;
                            else
                                node = bNodes.asNode(label) ;
                            addBinding(binding, Var.alloc(varName), node) ;
                            break ;
                        }

                        if ( isTag(tag, XMLResults.dfLiteral) ) {
                            String datatype = parser.getAttributeValue(null, XMLResults.dfAttrDatatype) ;

                            // String langTag = parser.getAttributeValue(null,
                            // "lang") ;

                            // Woodstox needs XML_NS despite the javadoc of StAX
                            // "If the namespaceURI is null the namespace is not checked for equality"
                            // StAX(.codehaus.org) copes both ways round
                            String langTag = parser.getAttributeValue(XML_NS, "lang") ;

                            // Works for XML literals (returning them as a
                            // string)
                            String text = parser.getElementText() ;

                            RDFDatatype dType = null ;
                            if ( datatype != null )
                                dType = TypeMapper.getInstance().getSafeTypeByName(datatype) ;

                            Node n = NodeFactory.createLiteral(text, langTag, dType) ;
                            if ( varName == null )
                                throw new ResultSetException("No name for variable") ;
                            addBinding(binding, Var.alloc(varName), n) ;
                            break ;
                        }

                        if ( isTag(tag, XMLResults.dfUnbound) ) {
                            break ;
                        }
                        if ( isTag(tag, XMLResults.dfURI) ) {
                            String uri = parser.getElementText() ;
                            Node node = NodeFactory.createURI(uri) ;
                            addBinding(binding, Var.alloc(varName), node) ;
                            break ;
                        }
                        break ;
                    default :
                }
            }
            staxError("getOneSolution: Hit end unexpectedly") ;
            return null ;
        }

        private boolean isTag(String localName, String expectedName) {
            if ( !parser.getNamespaceURI().equals(XMLResults.baseNamespace) )
                return false ;
            return localName.equals(expectedName) ;
        }

        private void staxError(String msg) {
            Log.warn(this, "StAX error: " + msg) ;
            throw new ResultSetException(msg) ;
        }

        private void staxError(String msg, Throwable th) {
            Log.warn(this, "StAX error: " + msg, th) ;
            throw new ResultSetException(msg, th) ;
        }
    }
}
