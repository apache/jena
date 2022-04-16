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

package org.apache.jena.riot.rowset.rw;

import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.jena.atlas.lib.Closeable;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.ARQ;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.lang.LabelToNode;
import org.apache.jena.riot.resultset.ResultSetLang;
import org.apache.jena.riot.rowset.RowSetReader;
import org.apache.jena.riot.rowset.RowSetReaderFactory;
import org.apache.jena.riot.system.SyntaxLabels;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.exec.QueryExecResult;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.resultset.ResultSetException;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.util.JenaXMLInput;

public class RowSetReaderXML implements RowSetReader {

    public static final RowSetReaderFactory factory = lang -> {
        if (!Objects.equals(lang, ResultSetLang.RS_XML ) )
            throw new ResultSetException("Result set reader for XML asked for a "+lang);
        return new RowSetReaderXML();
    };

    private RowSetReaderXML() {}

    @Override
    public QueryExecResult readAny(InputStream in, Context context) {
        QueryExecResult result = ResultsStAX.read(in, context);
        return result;
    }

    @Override
    public RowSet read(Reader in, Context context) {
        QueryExecResult result = ResultsStAX.read(in, context);
        return result.rowSet();
    }

    static class ResultsStAX implements RowSet, Closeable {
        public static QueryExecResult read(InputStream in,Context context) {
            XMLStreamReader xReader;
            try {
                xReader = JenaXMLInput.newXMLStreamReader(in);
            } catch (XMLStreamException e) {
                throw new ResultSetException("Can't initialize StAX parsing engine", e) ;
            } catch (Exception ex) {
                throw new ResultSetException("Failed when initializing the StAX parsing engine", ex) ;
            }
            return worker(xReader, context);
        }

        public static QueryExecResult read(Reader in, Context context) {
            XMLStreamReader xReader;
            try {
                xReader = JenaXMLInput.newXMLStreamReader(in);
            } catch (XMLStreamException e) {
                throw new ResultSetException("Can't initialize StAX parsing engine", e) ;
            } catch (Exception ex) {
                throw new ResultSetException("Failed when initializing the StAX parsing engine", ex) ;
            }
            return worker(xReader, context);
        }

        private static QueryExecResult worker(XMLStreamReader xReader, Context context) {
            ResultsStAX rss = new ResultsStAX(xReader, context) ;
            return rss.read();
        }

        private XMLStreamReader parser           = null;
        private List<Var>    variables        = new ArrayList<>();
        // Current binding/query solution.
        private Binding         binding          = null;
        private boolean         inputGraphLabels = ARQ.isTrue(ARQ.inputGraphBNodeLabels);

        private final LabelToNode  bNodes;

        private boolean         isResultSet      = false;

        // Result set
        private boolean         ordered          = false;
        private boolean         distinct         = false;
        private boolean         finished         = false;
        private int             row              = 0;

        private boolean         askResult        = false;

        private ResultsStAX(XMLStreamReader reader, Context context) {
            parser = reader ;
            boolean inputGraphBNodeLabels = (context != null) && context.isTrue(ARQ.inputGraphBNodeLabels);
            this.bNodes = inputGraphBNodeLabels
                ? SyntaxLabels.createLabelToNodeAsGiven()
                : SyntaxLabels.createLabelToNode();
            init() ;
        }

        private QueryExecResult read() {
            if ( isResultSet )
                return new QueryExecResult(this);
            else
                return new QueryExecResult(askResult);
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
                String msg = ex.getMessage();
                msg = msg.replace("\n", " : ");
                throw new RiotException("XMLStreamException: " + msg, ex);
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
        public Binding next() {
            if ( finished )
                throw new NoSuchElementException("End of XML Results") ;
            if ( !hasNext() )
                throw new NoSuchElementException("End of XML Results") ;
            Binding r = binding;
            row++;
            binding = null;
            return r;
        }

        @Override
        public List<Var> getResultVars() {
            return variables;
        }

        @Override
        public long getRowNumber() {
            return row;
        }

        /*package*/ boolean isOrdered() {
            return ordered ;
        }

        /*package*/ boolean isDistinct() {
            return distinct ;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException(ResultsStAX.class.getName()) ;
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
                    s1 = String.join(", ", startElementNames) ;

                String s2 = "" ;
                if ( stopElementNames != null )
                    s2 = String.join(", ", stopElementNames) ;
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
                            variables.add(Var.alloc(varname)) ;
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


        static final String XML_NS = ARQConstants.XML_NS ;

        private Binding getOneSolution() throws XMLStreamException {
            if ( finished )
                return null ;
            // At the start of <result>
            BindingBuilder builder = Binding.builder() ;
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
                            return builder.build() ;
                        if ( isTag(tag, XMLResults.dfResults) )
                            // Hit the end of solutions.
                            return null ;
                        break ;
                    case XMLStreamConstants.START_ELEMENT :
                        tag = parser.getLocalName() ;
                        if ( isTag(tag, XMLResults.dfSolution) ) {
                            builder.reset();
                            break ;
                        }
                        if ( isTag(tag, XMLResults.dfBinding) ) {
                            varName = parser.getAttributeValue(null, XMLResults.dfAttrVarName) ;
                            break ;
                        }

                        Node value = parseOneTerm(tag);
                        if ( value != null ) {
                            if ( varName == null )
                                throw new ResultSetException("No name for variable") ;
                            addBinding(builder, Var.alloc(varName), value) ;
                            break;
                        }
                        break ;
                    default :
                }
            }
            staxError("getOneSolution: Hit end unexpectedly") ;
            return null ;
        }

        private Node parseOneTerm(String tag) throws XMLStreamException {
          // URI, literal, bNode, unbound, triple
          if ( isTag(tag, XMLResults.dfBNode) ) {
              String label = parser.getElementText() ;
              Node node = null ;
              // if ( inputGraphLabels.getValue() )
              if ( inputGraphLabels )
                  node = NodeFactory.createBlankNode(label) ;
              else
                  node = bNodes.get(null, label);
              return node;
          }

          if ( isTag(tag, XMLResults.dfLiteral) ) {
              String datatype = parser.getAttributeValue(null, XMLResults.dfAttrDatatype) ;

              // String langTag = parser.getAttributeValue(null, "lang") ;
              // Woodstox needs XML_NS despite the javadoc of StAX
              // "If the namespaceURI is null the namespace is not checked for equality"
              // StAX(.codehaus.org) copes both ways round
              String langTag = parser.getAttributeValue(XML_NS, "lang") ;

              // Works for XML literals (returning them as a string)
              String text = parser.getElementText() ;

              RDFDatatype dType = null ;
              if ( datatype != null )
                  dType = TypeMapper.getInstance().getSafeTypeByName(datatype) ;

              Node n = NodeFactory.createLiteral(text, langTag, dType) ;
              return n ;
          }

          if ( isTag(tag, XMLResults.dfUnbound) ) {
              return null;
          }

          if ( isTag(tag, XMLResults.dfURI) ) {
              String uri = parser.getElementText() ;
              Node node = NodeFactory.createURI(uri) ;
              return node;
          }

          if ( isTag(tag, XMLResults.dfTriple) || isTag(tag, XMLResults.dfStatement) ) {
              // <triple>
              Node s = null;
              Node p = null;
              Node o = null;
              while (parser.hasNext()) {
                  // Skip CHARACTERS
                  int event2 = parser.nextTag() ;
                  if ( event2 == XMLStreamConstants.END_ELEMENT ) {
                      // Early end.
                      String tagx = parser.getLocalName() ;
                      if ( tagx.equals(XMLResults.dfTriple) || tagx.equals(XMLResults.dfStatement) )
                          staxError("Incomplete triple term");
                      else
                          staxError("Mismatched tag: "+tagx);
                  }
                  //XMLStreamConstants.START_ELEMENT

                  String tag2 = parser.getLocalName() ;
                  // <subject> <predicate> <object>
                  // One of subject, property, object (s,p,o)

                  if ( ! isOneOf(tag2, XMLResults.dfSubject, XMLResults.dfProperty, XMLResults.dfPredicate, XMLResults.dfObject,
                                       XMLResults.dfSubjectAlt, XMLResults.dfPropertyAlt, XMLResults.dfObjectAlt) )
                      staxError("Unexpected tag in triple term: "+tag2);

                  int event3 = parser.nextTag() ;
                  String tag3 = parser.getLocalName() ;
                  Node x = parseOneTerm(tag3);

                  // Read end </subject> etc.
                  parser.nextTag() ;

                  // Check for double assignment.
                  if ( isOneOf(tag2, XMLResults.dfSubject, XMLResults.dfSubjectAlt) )
                      s = x ;
                  else if ( isOneOf(tag2, XMLResults.dfProperty, XMLResults.dfPredicate, XMLResults.dfPropertyAlt) )
                      p = x ;
                  else if ( isOneOf(tag2, XMLResults.dfObject, XMLResults.dfObjectAlt) )
                      o = x ;
                  if ( s != null && p != null && o != null ) {
                      int event4 = parser.nextTag() ;
                      String tagx = parser.getLocalName() ;
                      if ( event4 == XMLStreamConstants.START_ELEMENT )
                          staxError("Too many terms for a triple");
                      // XMLStreamConstants.END_ELEMENT )
                      if ( ! tagx.equals(XMLResults.dfTriple) && ! tagx.equals(XMLResults.dfStatement) )
                          staxError("Expecting </triple>: "+tagx);
                      // </triple>
                      break;
                  }
              }

              if ( s == null || p == null || o == null )
                  staxError("Bad <triple> term");
              Node node = NodeFactory.createTripleNode(s, p, o);
              return node;
          }
            return null;
        }

        static private boolean isOneOf (String string, String...values) {
            for ( String v : values ) {
                if ( Objects.equals(string, v) )
                    return true;
            }
            return false;
        }

        static protected void addBinding(BindingBuilder builder, Var var, Node value) {
            Node n = builder.get(var);
            if ( n != null ) {
                if ( n.equals(value) )
                    // Same - silently skip.
                    return;
                // Different - warn and skip.
                Log.warn(ResultsStAX.class,
                         String.format("Multiple occurences of a binding for variable '%s' with different values - ignored", var.getName()));
                return;
            }
            builder.add(var, value);
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
