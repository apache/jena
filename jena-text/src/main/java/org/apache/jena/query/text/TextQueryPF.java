/**
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

package org.apache.jena.query.text ;

import java.util.Iterator;
import java.util.List ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.InternalErrorException ;
import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.datatypes.RDFDatatype ;
import org.apache.jena.datatypes.xsd.XSDDatatype ;
import org.apache.jena.graph.Node ;
import org.apache.jena.query.QueryBuildException ;
import org.apache.jena.sparql.core.* ;
import org.apache.jena.sparql.engine.ExecutionContext ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.engine.iterator.QueryIterExtendByVar ;
import org.apache.jena.sparql.engine.iterator.QueryIterSlice ;
import org.apache.jena.sparql.mgt.Explain ;
import org.apache.jena.sparql.pfunction.PropFuncArg ;
import org.apache.jena.sparql.pfunction.PropertyFunctionBase ;
import org.apache.jena.sparql.util.Context ;
import org.apache.jena.sparql.util.IterLib ;
import org.apache.jena.sparql.util.NodeFactoryExtra ;
import org.apache.lucene.queryparser.classic.QueryParserBase ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

/** property function that accesses a Solr server */
public class TextQueryPF extends PropertyFunctionBase {
    private static Logger log = LoggerFactory.getLogger(TextQueryPF.class) ;
    /*
     * ?uri :queryPF (property? "string" limit? score?) 
     * (?uri ?score) not implemented
     * Look for "//** score" in TextQueryPF, TextIndexLucene and TextIndexSolr
     */

    private TextIndex     server        = null ;
    private boolean       warningIssued = false ;

    public TextQueryPF() {}

    @Override
    public void build(PropFuncArg argSubject, Node predicate, PropFuncArg argObject, ExecutionContext execCxt) {
        super.build(argSubject, predicate, argObject, execCxt) ;
        //** score
        // Subject possibilities become ?foo or (?foo ?score) 
        DatasetGraph dsg = execCxt.getDataset() ;
        server = chooseTextIndex(dsg) ;

        if (!argSubject.isNode())
            throw new QueryBuildException("Subject is not a single node: " + argSubject) ;

        //extra lang arg for possible multilingual index
        String lang = null;

        if (argObject.isList()) {
            //extract of extra lang arg if present.
            //For the moment, arg is removed from the list to avoid conflict with order and args length
            //but should be managed with others args
            lang = extractArg("lang", argObject);

            List<Node> list = argObject.getArgList() ;
            if (list.size() == 0)
                throw new QueryBuildException("Zero-length argument list") ;

            if (list.size() > 4)
                throw new QueryBuildException("Too many arguments in list : " + list) ;
        }

        // If retrieved index is an instance of TextIndexLuceneMultilingual,
        // we need to switch with the right localized index.
        // The pattern is :
        // ?uri text:query (property 'string' ['lang:language'] [limit])
        // ex : ?uri text:query (rdfs:label 'book' 'lang:en')
        // note: default index is the unlocalized index (if lang arg is not present).
        if (server instanceof TextIndexLuceneMultilingual)
            server = ((TextIndexLuceneMultilingual)server).getIndex(lang);
    }

    private static TextIndex chooseTextIndex(DatasetGraph dsg) {
        
        Context c = dsg.getContext() ; 
        
        Object obj = dsg.getContext().get(TextQuery.textIndex) ;

        if (obj != null) {
            try {
                return (TextIndex)obj ;
            } catch (ClassCastException ex) {
                Log.warn(TextQueryPF.class, "Context setting '" + TextQuery.textIndex + "'is not a TextIndex") ;
            }
        }

        if (dsg instanceof DatasetGraphText) {
            DatasetGraphText x = (DatasetGraphText)dsg ;
            return x.getTextIndex() ;
        }
        Log.warn(TextQueryPF.class, "Failed to find the text index : tried context and as a text-enabled dataset") ;
        return null ;
    }

    private String extractArg(String prefix, PropFuncArg argObject) {
        String value = null;
        int pos = 0;
        for (Iterator it = argObject.getArgList().iterator(); it.hasNext(); ) {
            Node node = (Node)it.next();
            if (node.isLiteral()) {
                String arg = node.getLiteral().toString();
                if (arg.startsWith(prefix + ":")) {
                    value = arg.split(":")[1];
                    break;
                }
            }
            pos++;
        }
        if (value != null)
            argObject.getArgList().remove(pos);

        return value;
    }

    @Override
    public QueryIterator exec(Binding binding, PropFuncArg argSubject, Node predicate, PropFuncArg argObject,
                              ExecutionContext execCxt) {
        if (server == null) {
            if (!warningIssued) {
                Log.warn(getClass(), "No text index - no text search performed") ;
                warningIssued = true ;
            }
            // Not a text dataset - no-op
            return IterLib.result(binding, execCxt) ;
        }

        DatasetGraph dsg = execCxt.getDataset() ;

        argSubject = Substitute.substitute(argSubject, binding) ;
        argObject = Substitute.substitute(argObject, binding) ;

        if (!argSubject.isNode())
            throw new InternalErrorException("Subject is not a node (it was earlier!)") ;

        Node s = argSubject.getArg() ;

        if (s.isLiteral())
            // Does not match
            return IterLib.noResults(execCxt) ;

        StrMatch match = objectToStruct(argObject) ;
        if (match == null) {
            // can't match
            return IterLib.noResults(execCxt) ;
        }

        // ----

        QueryIterator qIter = (Var.isVar(s)) 
            ? variableSubject(binding, s, match, execCxt) 
            : concreteSubject(binding, s, match, execCxt) ;
        if (match.getLimit() >= 0)
            qIter = new QueryIterSlice(qIter, 0, match.getLimit(), execCxt) ;
        return qIter ;
    }

    private QueryIterator variableSubject(Binding binding, Node s, StrMatch match, ExecutionContext execCxt) {
        Var v = Var.alloc(s) ;
        List<Node> r = query(match.getQueryString(), match.getLimit(), execCxt) ;
        // Make distinct. Note interaction with limit is imperfect
        r = Iter.iter(r).distinct().toList() ;
        QueryIterator qIter = new QueryIterExtendByVar(binding, v, r.iterator(), execCxt) ;
        return qIter ;
    }

    private QueryIterator concreteSubject(Binding binding, Node s, StrMatch match, ExecutionContext execCxt) {
        if (!s.isURI()) {
            log.warn("Subject not a URI: " + s) ;
            return IterLib.noResults(execCxt) ;
        }

        String qs = match.getQueryString() ;
        List<Node> x = query(match.getQueryString(), -1, execCxt) ;
        if ( x == null || ! x.contains(s) )
            return IterLib.noResults(execCxt) ;
        else
            return IterLib.result(binding, execCxt) ;
    }

    private List<Node> query(String queryString, int limit, ExecutionContext execCxt) {
        // use the graph information in the text index if possible
        if (server.getDocDef().getGraphField() != null
            && execCxt.getActiveGraph() instanceof GraphView) {
            GraphView activeGraph = (GraphView)execCxt.getActiveGraph() ;
            if (!Quad.isUnionGraph(activeGraph.getGraphName())) {
                String uri = 
                    activeGraph.getGraphName() != null 
                    ? TextQueryFuncs.graphNodeToString(activeGraph.getGraphName())
                    : Quad.defaultGraphNodeGenerated.getURI() ;
                String escaped = QueryParserBase.escape(uri) ;
                String qs2 = server.getDocDef().getGraphField() + ":" + escaped ;
                queryString = "(" + queryString + ") AND " + qs2 ;
            }
        } 
    
        Explain.explain(execCxt.getContext(), "Text query: "+queryString) ;
        if ( log.isDebugEnabled())
            log.debug("Text query: {} ({})", queryString,limit) ;
        return server.query(queryString, limit) ;
    }
    
    /** Deconstruct the node or list object argument and make a StrMatch */
    private StrMatch objectToStruct(PropFuncArg argObject) {
        EntityDefinition docDef = server.getDocDef() ;
        if (argObject.isNode()) {
            Node o = argObject.getArg() ;
            if (!o.isLiteral()) {
                log.warn("Object to text query is not a literal") ;
                return null ;
            }

            RDFDatatype dt = o.getLiteralDatatype() ;
            if (dt != null && dt != XSDDatatype.XSDstring) {
                log.warn("Object to text query is not a string") ;
                return null ;
            }

            String qs = o.getLiteralLexicalForm() ;
            return new StrMatch(null, qs, -1, 0) ;
        }

        List<Node> list = argObject.getArgList() ;
        if (list.size() == 0 || list.size() > 3)
            throw new TextIndexException("Change in object list size") ;

        Node predicate = null ;
        String field = null ;       // Do not prepend the feild name - rely on default field
        int idx = 0 ;
        Node x = list.get(0) ;
        // Property?
        if (x.isURI()) {
            predicate = x ;
            idx++ ;
            if (idx >= list.size())
                throw new TextIndexException("Property specificied but no query string : " + list) ;
            x = list.get(idx) ;
            field = docDef.getField(predicate) ;
            if (field == null) {
                log.warn("Predicate not indexed: " + predicate) ;
                return null ;
            }
        }

        // String!
        if (!x.isLiteral()) {
            log.warn("Text query string is not a literal " + list) ;
            return null ;
        }
        if (x.getLiteralDatatype() != null && !x.getLiteralDatatype().equals(XSDDatatype.XSDstring)) {
            log.warn("Text query is not a string " + list) ;
            return null ;
        }
        String queryString = x.getLiteralLexicalForm() ;
        idx++ ;

        int limit = -1 ;
        float score = 0 ;

        if (idx < list.size()) {
            // Limit?
            x = list.get(idx) ;
            idx++ ;
            int v = NodeFactoryExtra.nodeToInt(x) ;
            limit = (v < 0) ? -1 : v ;
        }

        String qs = queryString ;
        if (field != null)
            qs = field + ":" + qs ;

        return new StrMatch(predicate, qs, limit, score) ;
    }

    class StrMatch {
        private final Node   property ;
        private final String queryString ;
        private final int    limit ;
        private final float  scoreLimit ;

        public StrMatch(Node property, String queryString, int limit, float scoreLimit) {
            super() ;
            this.property = property ;
            this.queryString = queryString ;
            this.limit = limit ;
            this.scoreLimit = scoreLimit ;
        }

        public Node getProperty() {
            return property ;
        }

        public String getQueryString() {
            return queryString ;
        }

        public int getLimit() {
            return limit ;
        }

        public float getScoreLimit() {
            return scoreLimit ;
        }
    }
}
