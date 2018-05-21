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

package org.apache.jena.query.text ;

import java.util.Collection ;
import java.util.Iterator ;
import java.util.List ;
import java.util.function.Function ;

import org.apache.jena.atlas.io.IndentedLineBuffer;
import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.Cache ;
import org.apache.jena.atlas.lib.CacheFactory ;
import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.datatypes.RDFDatatype ;
import org.apache.jena.datatypes.xsd.XSDDatatype ;
import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.Node ;
import org.apache.jena.ext.com.google.common.base.Strings;
import org.apache.jena.ext.com.google.common.collect.LinkedListMultimap;
import org.apache.jena.ext.com.google.common.collect.ListMultimap;
import org.apache.jena.query.QueryBuildException ;
import org.apache.jena.query.QueryExecException ;
import org.apache.jena.sparql.core.* ;
import org.apache.jena.sparql.engine.ExecutionContext ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.engine.binding.BindingFactory ;
import org.apache.jena.sparql.engine.binding.BindingMap ;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper ;
import org.apache.jena.sparql.engine.iterator.QueryIterSlice ;
import org.apache.jena.sparql.mgt.Explain ;
import org.apache.jena.sparql.pfunction.PropFuncArg ;
import org.apache.jena.sparql.pfunction.PropertyFunctionBase ;
import org.apache.jena.sparql.util.IterLib ;
import org.apache.jena.sparql.util.NodeFactoryExtra ;
import org.apache.jena.sparql.util.Symbol ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

/** property function that accesses a text index */
public class TextQueryPF extends PropertyFunctionBase {
    private static Logger log = LoggerFactory.getLogger(TextQueryPF.class) ;
    /*
     * (?uri ?score) :queryPF (property? "string" limit? score?) 
     */

    private TextIndex     textIndex        = null ;
    private boolean       warningIssued = false ;

    public TextQueryPF() {}

    private static final Symbol cacheSymbol = Symbol.create("TextQueryPF.cache");
    private static final int CACHE_SIZE = 10;

    @Override
    public void build(PropFuncArg argSubject, Node predicate, PropFuncArg argObject, ExecutionContext execCxt) {
        super.build(argSubject, predicate, argObject, execCxt) ;
        DatasetGraph dsg = execCxt.getDataset() ;
        textIndex = chooseTextIndex(execCxt, dsg) ;

        if (argSubject.isList()) {
            int size = argSubject.getArgListSize();
            if (size == 0 || size > 4) {
                throw new QueryBuildException("Subject has "+argSubject.getArgList().size()+" elements, must be at least 1 and not greater than 4: "+argSubject);
            }
        }

        if (argObject.isList()) {
            List<Node> list = argObject.getArgList() ;

            if (list.size() == 0)
                throw new QueryBuildException("Zero-length argument list") ;

            if (list.size() > 5)
                throw new QueryBuildException("Too many arguments in list : " + list) ;
        }
    }

    /**
     * Find the text index from:
     * <ul>
     * <li>The execution context.
     * <li>The dataset.
     * </ul>
     * 
     * If the text index is set in the dataset context, it will have been merged
     * into the execution context. This is the normal route because
     * {@link TextDatasetFactory} sets the text index in the dataset context.
     * Asking the dataset directly is only needed for the case of no context set,
     * just in case of a unusually, progammatically constructed
     * {@code DatasetGraphText} is being used (a bug, or old code, probably).
     */
    private static TextIndex chooseTextIndex(ExecutionContext execCxt, DatasetGraph dsg) {
        
        Object obj = execCxt.getContext().get(TextQuery.textIndex) ;

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

    private String extractArg(String prefix, List<Node> objArgs) {
        String value = null;
        for (Node node : objArgs) {
            if (node.isLiteral()) {
                String arg = node.getLiteral().toString();
                if (arg.startsWith(prefix + ":")) {
                    value = arg.substring(prefix.length()+1);
                    break;
                }
            }
        }

        return value;
    }

    @Override
    public QueryIterator exec(Binding binding, PropFuncArg argSubject, Node predicate, PropFuncArg argObject,
                              ExecutionContext execCxt) {
        if (log.isTraceEnabled()) {
            IndentedLineBuffer subjBuff = new IndentedLineBuffer() ;
            argSubject.output(subjBuff, null) ;
            IndentedLineBuffer objBuff = new IndentedLineBuffer() ;
            argObject.output(objBuff, null) ;
            log.trace("exec: {} text:query {}", subjBuff, objBuff) ;
        }
        if (textIndex == null) {
            if (!warningIssued) {
                Log.warn(getClass(), "No text index - no text search performed") ;
                warningIssued = true ;
            }
            // Not a text dataset - no-op
            return IterLib.result(binding, execCxt) ;
        }

        argSubject = Substitute.substitute(argSubject, binding) ;
        argObject = Substitute.substitute(argObject, binding) ;
        
        Node s = null;
        Node score = null;
        Node literal = null;
        Node graph = null;

        if (argSubject.isList()) {
            // Length checked in build()
            s = argSubject.getArg(0);

            if (argSubject.getArgListSize() > 1) {
                score = argSubject.getArg(1);          
                if (!score.isVariable())
                    throw new QueryExecException("Hit score is not a variable: "+argSubject) ;
            }

            if (argSubject.getArgListSize() > 2) {
                literal = argSubject.getArg(2);
                if (!literal.isVariable())
                    throw new QueryExecException("Hit literal is not a variable: "+argSubject) ;
            }

            if (argSubject.getArgListSize() > 3) {
                graph = argSubject.getArg(3);
                if (!graph.isVariable())
                    throw new QueryExecException("Hit graph is not a variable: "+argSubject) ;
            }
        } else {
            s = argSubject.getArg() ;
        }

        if (s.isLiteral())
            // Does not match
            return IterLib.noResults(execCxt) ;

        StrMatch match = objectToStruct(argObject, true) ;
        if (match == null) {
            // can't match
            return IterLib.noResults(execCxt) ;
        }

        // ----

        QueryIterator qIter = (Var.isVar(s)) 
            ? variableSubject(binding, s, score, literal, graph, match, execCxt)
            : concreteSubject(binding, s, score, literal, graph, match, execCxt) ;
        if (match.getLimit() >= 0)
            qIter = new QueryIterSlice(qIter, 0, match.getLimit(), execCxt) ;
        return qIter ;
    }

    private QueryIterator resultsToQueryIterator(Binding binding, Node s, Node score, Node literal, Node graph, Collection<TextHit> results, ExecutionContext execCxt) {
        log.trace("resultsToQueryIterator: {}", results) ;
        Var sVar = Var.isVar(s) ? Var.alloc(s) : null ;
        Var scoreVar = (score==null) ? null : Var.alloc(score) ;
        Var literalVar = (literal==null) ? null : Var.alloc(literal) ;
        Var graphVar = (graph==null) ? null : Var.alloc(graph) ;

        Function<TextHit,Binding> converter = (TextHit hit) -> {
            if (score == null && literal == null)
                return sVar != null ? BindingFactory.binding(binding, sVar, hit.getNode()) : BindingFactory.binding(binding);
            BindingMap bmap = BindingFactory.create(binding);
            if (sVar != null)
                bmap.add(sVar, hit.getNode());
            if (scoreVar != null)
                bmap.add(scoreVar, NodeFactoryExtra.floatToNode(hit.getScore()));
            if (literalVar != null)
                bmap.add(literalVar, hit.getLiteral());
            if (graphVar != null && hit.getGraph() != null)
                bmap.add(graphVar, hit.getGraph());
            return bmap;
        } ;
        
        Iterator<Binding> bIter = Iter.map(results.iterator(), converter);
        QueryIterator qIter = new QueryIterPlainWrapper(bIter, execCxt);
        return qIter ;
    }

    private QueryIterator variableSubject(Binding binding, Node s, Node score, Node literal, Node graph, StrMatch match, ExecutionContext execCxt) {
        log.trace("variableSubject: {}", match) ;
        ListMultimap<String,TextHit> results = query(match.getProperty(), match.getQueryString(), match.getLang(), match.getLimit(), match.getHighlight(), execCxt) ;
        Collection<TextHit> r = results.values();
        return resultsToQueryIterator(binding, s, score, literal, graph, r, execCxt);
    }

    private QueryIterator concreteSubject(Binding binding, Node s, Node score, Node literal, Node graph, StrMatch match, ExecutionContext execCxt) {
        log.trace("concreteSubject: {}", match) ;
        ListMultimap<String,TextHit> x = query(match.getProperty(), match.getQueryString(), match.getLang(), -1, match.getHighlight(), execCxt) ;
        
        if ( x == null ) // null return value - empty result
            return IterLib.noResults(execCxt) ;
        
        List<TextHit> r = x.get(TextQueryFuncs.subjectToString(s));

        return resultsToQueryIterator(binding, s, score, literal, graph, r, execCxt);
    }

    private ListMultimap<String,TextHit> query(Node property, String queryString, String lang, int limit, String highlight, ExecutionContext execCxt) {
        String graphURI = chooseGraphURI(execCxt);
        
        if ( graphURI == null ) {
            Explain.explain(execCxt.getContext(), "Text query: "+queryString) ;
            log.debug("Text query: {} ({})", queryString, limit) ;
        } else {
            Explain.explain(execCxt.getContext(), "Text query <"+graphURI+">: "+queryString) ;
            log.debug("Text query: {} <{}> ({})", queryString, graphURI, limit) ;
        }

        ListMultimap<String,TextHit> results;
        
        if (textIndex.getDocDef().areQueriesCached()) {
            // Cache-key does not matter if lang or graphURI are null
            String cacheKey = limit + " " + property + " " + queryString + " " + lang + " " + graphURI ;
            @SuppressWarnings("unchecked")
            Cache<String,ListMultimap<String,TextHit>> queryCache = 
                (Cache<String,ListMultimap<String,TextHit>>) execCxt.getContext().get(cacheSymbol);
            if (queryCache == null) {
                /* doesn't yet exist, need to create it */
                queryCache = CacheFactory.createCache(CACHE_SIZE);
                execCxt.getContext().put(cacheSymbol, queryCache);
            }

            log.trace("Caching Text query: {} with key: >>{}<< in cache: {}", queryString, cacheKey, queryCache) ;

            results = queryCache.getOrFill(cacheKey, ()->performQuery(property, queryString, graphURI, lang, limit, highlight));
        } else {
            log.trace("Executing w/o cache Text query: {}", queryString) ;
            results = performQuery(property, queryString, graphURI, lang, limit, highlight);
        }

        return results;
    }

    private String chooseGraphURI(ExecutionContext execCxt) {
        // use the graph information in the text index if possible
        String graphURI = null;
        Graph activeGraph = execCxt.getActiveGraph();
        
        if (textIndex.getDocDef().getGraphField() != null && activeGraph instanceof NamedGraph) {
            NamedGraph namedGraph = (NamedGraph)activeGraph ;
            if (!Quad.isUnionGraph(namedGraph.getGraphName())) {
                graphURI = namedGraph.getGraphName() != null 
                        ? TextQueryFuncs.graphNodeToString(namedGraph.getGraphName())
                        : Quad.defaultGraphNodeGenerated.getURI() ;
            }
        }
        return graphURI;
    }
    
    private ListMultimap<String,TextHit> performQuery(Node property, String queryString, String graphURI, String lang, int limit, String highlight) {
        List<TextHit> resultList = textIndex.query(property, queryString, graphURI, lang, limit, highlight) ;
        ListMultimap<String,TextHit> results = LinkedListMultimap.create();
        for (TextHit result : resultList) {
            results.put(TextQueryFuncs.subjectToString(result.getNode()), result);
        }
        return results;
    }
    
    /** Deconstruct the node or list object argument and make a StrMatch 
     * The 'executionTime' flag indicates whether this is for a build time
     * static check, or for runtime execution.
     */
    private StrMatch objectToStruct(PropFuncArg argObject, boolean executionTime) {
        EntityDefinition docDef = textIndex.getDocDef() ;
        if (argObject.isNode()) {
            Node o = argObject.getArg() ;
            if (!o.isLiteral()) {
                if ( executionTime )
                    log.warn("Object to text query is not a literal") ;
                return null ;
            }

            String lang = o.getLiteralLanguage();
            RDFDatatype dt = o.getLiteralDatatype() ;
            if (lang.isEmpty()) {
                if (dt != null && dt != XSDDatatype.XSDstring) {
                    log.warn("Object to text query is not a string") ;
                    return null ;
                }
            }
            lang = Strings.emptyToNull(lang);

            String qs = o.getLiteralLexicalForm() ;
            return new StrMatch(null, qs, lang, -1, 0, null) ;
        }

        List<Node> list = argObject.getArgList() ;
        if (list.size() == 0 || list.size() > 5)
            throw new TextIndexException("Change in object list size") ;

        Node predicate = null ;
        String field = null ;       // Do not prepend the field name - rely on default field
        int idx = 0 ;
        Node x = list.get(0) ;
        // Property?
        if (x.isURI()) {
            predicate = x ;
            idx++ ;
            if (idx >= list.size())
                throw new TextIndexException("Property specificed but no query string : " + list) ;
            x = list.get(idx) ;
            field = docDef.getField(predicate) ;
            if (field == null) {
                log.warn("Predicate not indexed: " + predicate) ;
                return null ;
            }
        }

        // String!
        if (!x.isLiteral()) {
            if ( executionTime )
                log.warn("Text query string is not a literal " + list) ;
            return null ;
        }
        String lang = x.getLiteralLanguage();
        if (lang.isEmpty()) {
            if (x.getLiteralDatatype() != null && !x.getLiteralDatatype().equals(XSDDatatype.XSDstring)) {
                log.warn("Text query is not a string " + list) ;
                return null ;
            }
        }
        lang = Strings.emptyToNull(lang);

        String queryString = x.getLiteralLexicalForm() ;
        idx++ ;

        int limit = -1 ;
        float score = 0 ;

        if (idx < list.size()) {
            // Limit?
            x = list.get(idx) ;
            idx++ ;
            if ( ! x.isLiteral() ) {
                if ( executionTime )
                    log.warn("Text query limit is not an integer " + x) ;
                return null ;
            }
            
            int v = NodeFactoryExtra.nodeToInt(x) ;
            limit = (v < 0) ? -1 : v ;
        }

        //extract extra lang arg if present and if is usable.
        lang = lang == null ? extractArg("lang", list) : lang;

        if (lang != null && textIndex.getDocDef().getLangField() == null)
            log.warn("lang argument is ignored if langField not set in the index configuration");

        String highlight = extractArg("highlight", list);

        return new StrMatch(predicate, queryString, lang, limit, score, highlight) ;
    }

    class StrMatch {
        private final Node   property ;
        private final String queryString ;
        private final String lang ;
        private final int    limit ;
        private final float  scoreLimit ;
        private final String highlight ;

        public StrMatch(Node property, String queryString, String lang, int limit, float scoreLimit, String highlight) {
            super() ;
            this.property = property ;
            this.queryString = queryString ;
            this.lang = lang ;
            this.limit = limit ;
            this.scoreLimit = scoreLimit ;
            this.highlight = highlight;
        }

        public Node getProperty() {
            return property ;
        }

        public String getQueryString() {
            return queryString ;
        }

        public String getLang() {
            return lang ;
        }

        public int getLimit() {
            return limit ;
        }

        public float getScoreLimit() {
            return scoreLimit ;
        }

        public String getHighlight() {
            return highlight ;
        }
        
        @Override
        public String toString() {
            return "( property: " + property + "; query: " + queryString + "; limit: " + limit + "; lang: " + lang + "; maxFrags: " + highlight + " )";
        }
    }
}
