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

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.QueryBuildException;
import com.hp.hpl.jena.sparql.core.*;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIter;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterSlice;
import com.hp.hpl.jena.sparql.mgt.Explain;
import com.hp.hpl.jena.sparql.pfunction.PropFuncArg;
import com.hp.hpl.jena.sparql.pfunction.PropertyFunctionBase;
import com.hp.hpl.jena.sparql.util.Context;
import com.hp.hpl.jena.sparql.util.IterLib;
import com.hp.hpl.jena.sparql.util.NodeFactoryExtra;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.logging.Log;
import org.apache.lucene.queryparser.classic.QueryParserBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** property function that accesses a Solr server */
public class TextQueryPF extends PropertyFunctionBase {
    private static Logger log           = LoggerFactory.getLogger(TextQueryPF.class) ;
    /*
     * ?uri :queryPF (property? "string" limit? score?) score? not implemented
     * Look for "//** score" in TextIndexLucene and TextIndexSolr
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

        if ( argSubject.isList()) {
            List<Node> list = argSubject.getArgList();
            if (list.size() == 0) {
                throw new QueryBuildException(predicate + ": has empty subject list");
            } else if (list.size() > 2) {
                throw new QueryBuildException(predicate + ": subject list too long");
            }
        }

        if (argObject.isList()) {
            List<Node> list = argObject.getArgList() ;
            if (list.size() == 0)
                throw new QueryBuildException("Zero-length argument list") ;

            if (list.size() > 4)
                throw new QueryBuildException("Too many arguments in list : " + list) ;
        }

        // If retrieved index is an instance of TextIndexLuceneMultiLingual, we need to switch with the right index.
        // The pattern is :
        // (?uri ?score) text:query (property 'string' ['lang:language'])
        // ex : (?uri ?score) text:query (rdfs:label 'livre' 'lang:fr')
        // note: default index is the unlocalized index (if lang arg is not present).
        if (server instanceof TextIndexLuceneMultiLingual) {
            String lang = getArg("lang", argObject);
            server = ((TextIndexLuceneMultiLingual)server).getIndex(lang);
        }
    }

    private String getArg(String prefix, PropFuncArg argObject) {
        for (Iterator it = argObject.getArgList().iterator(); it.hasNext(); ) {
            Node node = (Node)it.next();
            if (node.isLiteral()) {
                String arg = node.getLiteral().toString();
                if (arg.startsWith(prefix + ":"))
                    return arg.split(":")[1];
            }
        }
        return null;
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

        //last chance case
        TextIndex index = TextDatasetFactory.getCtxtIndex();
        if (index != null)
            return index;

        Log.warn(TextQueryPF.class, "Failed to find the text index : tried context and as a text-enabled dataset") ;
        return null ;
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

        //DatasetGraph dsg = execCxt.getDataset() ;

        argSubject = Substitute.substitute(argSubject, binding) ;
        argObject = Substitute.substitute(argObject, binding) ;

        Node s;
        Node scoreVar = null;
        if (argSubject.isNode() )
        {
            s = argSubject.getArg() ;
        } else {
            List<Node> list = argSubject.getArgList();
            if (list.size() == 0 || list.size() > 2) {
                throw new TextIndexException(predicate + ": change in subject list size");
            }
            s = list.get(0);
            if (list.size() == 2) {
                scoreVar = list.get(1);
                // this could have been picked up at query build time
                if (! scoreVar.isVariable())
                    throw new TextIndexException(predicate + ": score variable is not a variable");
            }
        }

        if (s.isLiteral())
            // Does not match
            return IterLib.noResults(execCxt) ;
        StrMatch match = objectToStruct(scoreVar, argObject) ;
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

    private QueryIterator variableSubject(Binding binding, Node subject, StrMatch match, ExecutionContext execCxt) {
        List<Var> vars = new ArrayList<Var>();
        vars.add(Var.alloc(subject));
        if (match.getScoreVar() != null)
            vars.add(Var.alloc(match.getScoreVar()));

//        List<Node> r = server.query(match.getQueryString(), match.getLimit(), exceCtx) ;
        List<TextIndex.NodeAndScore> r = server.queryWithScore(match.getQueryString(), match.getLimit());
        // Make distinct. Note interaction with limit is imperfect
        r = Iter.iter(r).distinct().toList() ;
        QueryIterator qIter = new QueryIterExtendByVars(binding, vars, new NodeAndScoreToListIterator(r.iterator()), execCxt);
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
    private StrMatch objectToStruct(Node scoreVar, PropFuncArg argObject) {
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
            return new StrMatch(null, qs, -1, 0, scoreVar) ;
        }

        List<Node> list = argObject.getArgList() ;
//        if (list.size() == 0 || list.size() > 3)
        if (list.size() == 0 || list.size() > 4) //changed args size -AM
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
                //log.warn("Predicate not indexed: " + predicate) ;
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

        if (idx < list.size()) {
            // Score limit?
            x = list.get(idx) ;
            idx++ ;
            float v = NodeFactoryExtra.nodeToFloat(x) ;
            score = (v < 0) ? 0 : v ;
        }

        String qs = queryString ;
        if (field != null)
            qs = field + ":" + qs ;

        return new StrMatch(predicate, qs, limit, score, scoreVar) ;
    }

    class StrMatch {
        private final Node   property ;
        private final String queryString ;
        private final int    limit ;
        private final float  scoreLimit ;
        private final Node   scoreVar ;

        public StrMatch(Node property, String queryString, int limit, float scoreLimit, Node scoreVar) {
            super() ;
            this.property = property ;
            this.queryString = queryString ;
            this.limit = limit ;
            this.scoreLimit = scoreLimit ;
            this.scoreVar = scoreVar ;
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

        public Node getScoreVar() {
            return scoreVar;
        }
    }


    //
    // turn a NodeAndScoreIterator into an List<Node> iterator
    //
    private class NodeAndScoreToListIterator implements Iterator<List<Node>> {

        private Iterator<TextIndex.NodeAndScore> iter;

        NodeAndScoreToListIterator(Iterator<TextIndex.NodeAndScore> iter) {
            this.iter = iter;
        }

        @Override
        public boolean hasNext() {
            return iter.hasNext();
        }

        @Override
        public List<Node> next() {
            List<Node> result = new ArrayList<Node>();
            TextIndex.NodeAndScore ns = iter.next();
            result.add(ns.getNode());
            result.add(Node.createLiteral( Float.toString(ns.getScore()), XSDDatatype.XSDfloat));
            return result;
        }

        @Override
        public void remove() {
            iter.remove();
        }
    }

    /**
     * Yield new bindings, with a fixed parent, with multiple variables from an iterator.
     */

    // this code is cloned from QueryIterExtendByVar (not singular Var)
    // and generalized to handle a list of vars and nodes to bind.
    // Not being familiar with the internals of ARQ, there may be an existing
    // query iterator it would be better to use, but bwm didn't find it.

    private class QueryIterExtendByVars extends QueryIter
    {
        // Use QueryIterProcessBinding?
        private Binding binding ;
        private List<Var> vars ;
        private Iterator<List<Node>> members ;

        QueryIterExtendByVars(Binding binding, List<Var> vars, Iterator<List<Node>> members, ExecutionContext execCxt)
        {
            super(execCxt) ;
            this.binding = binding ;
            this.vars = vars ;
            this.members = members ;
        }

        @Override
        protected boolean hasNextBinding()
        {
            return members.hasNext() ;
        }

        @Override
        protected Binding moveToNextBinding()
        {
            List<Node> nodes = members.next() ;

            // silently ignore differences in the number of nodes and vars to bind them to
            // bind as many vars as there is data
            // ignore any nodes for which there are no variables

            Iterator<Node> nodeIter = nodes.iterator();
            Iterator<Var> varIter = vars.iterator();
            Binding b = binding;
            for ( ; nodeIter.hasNext() && varIter.hasNext(); )
            {
                b = BindingFactory.binding(b, varIter.next(), nodeIter.next()) ;
            }
            return b ;
        }

        @Override
        protected void closeIterator()
        { }

        @Override
        protected void requestCancel()
        { }
    }
}
