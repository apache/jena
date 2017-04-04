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

import java.util.Collection ;
import java.util.Iterator ;
import java.util.List ;
import java.util.function.Function ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.Cache ;
import org.apache.jena.atlas.lib.CacheFactory ;
import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.datatypes.RDFDatatype ;
import org.apache.jena.datatypes.xsd.XSDDatatype ;
import org.apache.jena.graph.Node ;
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
        textIndex = chooseTextIndex(dsg) ;

        if (argSubject.isList()) {
            int size = argSubject.getArgListSize();
            if (size != 2 && size != 3) {
                throw new QueryBuildException("Subject has "+argSubject.getArgList().size()+" elements, not 2 or 3: "+argSubject);
            }
        }

        if (argObject.isList()) {
            List<Node> list = argObject.getArgList() ;

            if (list.size() == 0)
                throw new QueryBuildException("Zero-length argument list") ;

            if (list.size() > 4)
                throw new QueryBuildException("Too many arguments in list : " + list) ;
        }
    }

    private static TextIndex chooseTextIndex(DatasetGraph dsg) {
        
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

    private String extractArg(String prefix, List<Node> objArgs) {
        String value = null;
        int pos = 0;
        for (Node node : objArgs) {
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
            objArgs.remove(pos);

        return value;
    }

    @Override
    public QueryIterator exec(Binding binding, PropFuncArg argSubject, Node predicate, PropFuncArg argObject,
                              ExecutionContext execCxt) {
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

        if (argSubject.isList()) {
            // Length checked in build()
            s = argSubject.getArg(0);
            score = argSubject.getArg(1);
            
            if (!score.isVariable())
                throw new QueryExecException("Hit score is not a variable: "+argSubject) ;

            if (argSubject.getArgListSize() > 2) {
                literal = argSubject.getArg(2);
                if (!literal.isVariable())
                    throw new QueryExecException("Hit literal is not a variable: "+argSubject) ;
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
            ? variableSubject(binding, s, score, literal, match, execCxt)
            : concreteSubject(binding, s, score, literal, match, execCxt) ;
        if (match.getLimit() >= 0)
            qIter = new QueryIterSlice(qIter, 0, match.getLimit(), execCxt) ;
        return qIter ;
    }

    private QueryIterator resultsToQueryIterator(Binding binding, Node s, Node score, Node literal, Collection<TextHit> results, ExecutionContext execCxt) {
        Var sVar = Var.isVar(s) ? Var.alloc(s) : null ;
        Var scoreVar = (score==null) ? null : Var.alloc(score) ;
        Var literalVar = (literal==null) ? null : Var.alloc(literal) ;

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
            return bmap;
        } ;
        
        Iterator<Binding> bIter = Iter.map(results.iterator(), converter);
        QueryIterator qIter = new QueryIterPlainWrapper(bIter, execCxt);
        return qIter ;
    }

    private QueryIterator variableSubject(Binding binding, Node s, Node score, Node literal, StrMatch match, ExecutionContext execCxt) {
        ListMultimap<String,TextHit> results = query(match.getProperty(), match.getQueryString(), match.getLang(), match.getLimit(), execCxt) ;
        Collection<TextHit> r = results.values();
        return resultsToQueryIterator(binding, s, score, literal, r, execCxt);
    }

    private QueryIterator concreteSubject(Binding binding, Node s, Node score, Node literal, StrMatch match, ExecutionContext execCxt) {
        ListMultimap<String,TextHit> x = query(match.getProperty(), match.getQueryString(), match.getLang(), -1, execCxt) ;
        
        if ( x == null ) // null return value - empty result
            return IterLib.noResults(execCxt) ;
        
        List<TextHit> r = x.get(TextQueryFuncs.subjectToString(s));

        return resultsToQueryIterator(binding, s, score, literal, r, execCxt);
    }

    private ListMultimap<String,TextHit> query(Node property, String queryString, String lang, int limit, ExecutionContext execCxt) {
        // use the graph information in the text index if possible
        String graph = null;
        if (textIndex.getDocDef().getGraphField() != null
            && execCxt.getActiveGraph() instanceof GraphView) {
            GraphView activeGraph = (GraphView)execCxt.getActiveGraph() ;
            if (!Quad.isUnionGraph(activeGraph.getGraphName())) {
                graph =
                    activeGraph.getGraphName() != null 
                    ? TextQueryFuncs.graphNodeToString(activeGraph.getGraphName())
                    : Quad.defaultGraphNodeGenerated.getURI() ;
            }
        }

        Explain.explain(execCxt.getContext(), "Text query: "+queryString) ;
        if ( log.isDebugEnabled())
            log.debug("Text query: {} ({})", queryString,limit) ;

        String cacheKey = limit + " " + property + " " + queryString ;
        @SuppressWarnings("unchecked")
        Cache<String,ListMultimap<String,TextHit>> queryCache = 
            (Cache<String,ListMultimap<String,TextHit>>) execCxt.getContext().get(cacheSymbol);
        if (queryCache == null) { /* doesn't yet exist, need to create it */
            queryCache = CacheFactory.createCache(CACHE_SIZE);
            execCxt.getContext().put(cacheSymbol, queryCache);
        }

        final String queryStr = queryString; // final needed for the lambda function
        final String graphURI = graph; // final needed for the lambda function
        ListMultimap<String,TextHit> results = queryCache.getOrFill(cacheKey, () -> {
            List<TextHit> resultList = textIndex.query(property, queryStr, graphURI, lang, limit) ;
            ListMultimap<String,TextHit> resultMultimap = LinkedListMultimap.create();
            for (TextHit result : resultList) {
                resultMultimap.put(TextQueryFuncs.subjectToString(result.getNode()), result);
            }
            return resultMultimap;
        });
        return results;
    }
    
    /** Deconstruct the node or list object argument and make a StrMatch 
     * The 'executionTime' flag indciates whether this is for a build time
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

            RDFDatatype dt = o.getLiteralDatatype() ;
            if (dt != null && dt != XSDDatatype.XSDstring) {
                log.warn("Object to text query is not a string") ;
                return null ;
            }

            String qs = o.getLiteralLexicalForm() ;
            return new StrMatch(null, qs, null, -1, 0) ;
        }

        List<Node> list = argObject.getArgList() ;
        if (list.size() == 0 || list.size() > 4)
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
            if ( ! x.isLiteral() ) {
                if ( executionTime )
                    log.warn("Text query limit is not an integer " + x) ;
                return null ;
            }
            
            int v = NodeFactoryExtra.nodeToInt(x) ;
            limit = (v < 0) ? -1 : v ;
        }

        //extract extra lang arg if present and if is usable.
        String lang = extractArg("lang", list);

        if (lang != null && textIndex.getDocDef().getLangField() == null)
            log.warn("lang argument is ignored if langField not set in the index configuration");

        return new StrMatch(predicate, queryString, lang, limit, score) ;
    }

    class StrMatch {
        private final Node   property ;
        private final String queryString ;
        private final String lang ;
        private final int    limit ;
        private final float  scoreLimit ;

        public StrMatch(Node property, String queryString, String lang, int limit, float scoreLimit) {
            super() ;
            this.property = property ;
            this.queryString = queryString ;
            this.lang = lang ;
            this.limit = limit ;
            this.scoreLimit = scoreLimit ;
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
    }
}
