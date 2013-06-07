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

package org.apache.jena.query.text;

import java.util.List ;
import java.util.Map ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.InternalErrorException ;
import org.apache.jena.atlas.logging.Log ;

import com.hp.hpl.jena.datatypes.RDFDatatype ;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.query.QueryBuildException ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterExtendByVar ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterSlice ;
import com.hp.hpl.jena.sparql.pfunction.PropFuncArg ;
import com.hp.hpl.jena.sparql.pfunction.PropertyFunctionBase ;
import com.hp.hpl.jena.sparql.util.IterLib ;
import com.hp.hpl.jena.sparql.util.NodeFactoryExtra;

/** property function that accesses a Solr server */ 
public class QueryPF extends PropertyFunctionBase
{
    private TextIndex server = null ;  
    private boolean warningIssued = false ;
    
    public QueryPF() { } 

    @Override
    public void build(PropFuncArg argSubject, Node predicate, PropFuncArg argObject, ExecutionContext execCxt)
    {
        super.build(argSubject, predicate, argObject, execCxt) ;
        
        DatasetGraph dsg = execCxt.getDataset() ;
        server = chooseTextIndex(dsg) ;
        
        if ( ! argSubject.isNode() )
            throw new QueryBuildException("Subject is not a single node: "+argSubject) ;
        
        if ( argObject.isList() )
        {
            List<Node> list = argObject.getArgList() ;
            if ( list.size() == 0 )
                throw new QueryBuildException("Zero-length argument list") ;

            if ( list.size() > 4 )
                throw new QueryBuildException("Too many arguments in list : "+list) ;
        }
    }

    /*
     * ?uri :queryPF (property? "string" limit? score?)
     * score? not implemented
     */
    
    // score limit - float : new IteratorTruncate<SolrDocument>(...., iter) ; 
    
    static class StrMatch
    {
        private final Node property ;
        private final String queryString ;
        private final int limit ;
        private final float scoreLimit ;

        public StrMatch(Node property, String queryString, int limit, float scoreLimit)
        {
            super() ;
            this.property = property ;
            this.queryString = queryString ;
            this.limit = limit ;
            this.scoreLimit = scoreLimit ;
        }

        public Node getProperty()           { return property ; }

        public String getQueryString()      { return queryString ; }

        public int getLimit()               { return limit ; }

        public float getScoreLimit()        { return scoreLimit ; }
    }
    
    private static TextIndex chooseTextIndex(DatasetGraph dsg)
    {
        Object obj = dsg.getContext().get(TextQuery.textIndex) ;

        if ( obj != null )
        {
            try { return (TextIndex)obj ; } 
            catch (ClassCastException ex) { Log.warn(QueryPF.class, "Context setting '"+TextQuery.textIndex+"'is not a TextIndex") ; }
        }

        if ( dsg instanceof DatasetGraphText )
        {
            DatasetGraphText x = (DatasetGraphText)dsg ;
            return x.getTextIndex() ;
        }
        Log.warn(QueryPF.class, "Failed to find the text index : tried context and as a text-enabled dataset") ;
        return null ;
    }

    @Override
    public QueryIterator exec(Binding binding, PropFuncArg argSubject, Node predicate, PropFuncArg argObject, ExecutionContext execCxt)
    {
        if ( server == null )
        {
            if ( ! warningIssued )
            {
                Log.warn(getClass(), "No text index - no text search performed") ;
                warningIssued = true ;
            }
            // Not a text dataset - no-op
            return IterLib.result(binding, execCxt) ;
        }
     
        DatasetGraph dsg = execCxt.getDataset() ;
        
        if ( ! argSubject.isNode() )
            throw new InternalErrorException("Subject is not a node (it was earlier!)") ;
            
        Node s = argSubject.getArg() ;
        
        if ( s.isLiteral() )
            // Does not match
            return IterLib.noResults(execCxt) ;
        
        StrMatch match = objectToStruct(argObject) ;

        // ----
        
        QueryIterator qIter =  ( Var.isVar(s) ) 
            ? variableSubject(binding, s, match, execCxt)
            : concreteSubject(binding, s, match, execCxt) ;
        
        if ( match.getLimit() >= 0 )
            qIter = new QueryIterSlice(qIter, 0, match.getLimit(), execCxt) ;
        return qIter ;
    }

    private QueryIterator variableSubject(Binding binding, Node s, StrMatch match, ExecutionContext execCxt )
    {
        Var v = Var.alloc(s) ;
        List<Node> r = server.query(match.getQueryString(), match.getLimit()) ;
        // Make distinct.  Note interaction with limit is imperfect
        r = Iter.iter(r).distinct().toList() ;
        QueryIterator qIter = new QueryIterExtendByVar(binding, v, r.iterator(), execCxt) ;
        return qIter ;
    }

    private QueryIterator concreteSubject(Binding binding, Node s, StrMatch match, ExecutionContext execCxt )
    {
        if ( ! s.isURI() )
        {
            Log.warn(this, "Subject not a URI: "+s) ;
            return IterLib.noResults(execCxt) ; 
        }
        
        String uri = s.getURI() ;
        Map<String, Node> x = server.get(uri) ;
        if ( x == null || x.isEmpty() )
            return IterLib.noResults(execCxt) ;
        else
            return IterLib.result(binding, execCxt) ;
    }

    /** Deconstruct the node or list object argument and make a StrMatch */ 
    private StrMatch objectToStruct(PropFuncArg argObject)
    {
        
        EntityDefinition docDef = server.getDocDef()  ;
        if ( argObject.isNode() )
        {
            Node o = argObject.getArg() ;
            
            if ( ! o.isLiteral() )
            { System.err.println("Bad/4") ; }
            
            RDFDatatype dt = o.getLiteralDatatype() ;
            if ( dt != null && dt != XSDDatatype.XSDstring )
            { System.err.println("Bad") ; }
                
            String qs = o.getLiteralLexicalForm() ;
            return new StrMatch(docDef.getPrimaryPredicate(), qs, -1, 0) ; 
        }
         
        List<Node> list = argObject.getArgList() ;
        if ( list.size() == 0 || list.size() > 3 )
            throw new TextIndexException("Change in object list size") ; 

        Node p = docDef.getPrimaryPredicate() ;
        String field = docDef.getPrimaryField() ;
        int idx = 0 ;
        Node x = list.get(0) ;
        // Property?
        if ( x.isURI() )
        {
            p = x ;
            idx++ ;
            if ( idx >= list.size() )
                throw new TextIndexException("Property specificied but no query string : "+list) ;
            x = list.get(idx) ;
            field = docDef.getField(p) ; 
        }
        
        // String!
        if ( ! x.isLiteral() )
            throw new TextIndexException("Query isn't a literal string : "+list) ;
        if ( x.getLiteralDatatype() != null && ! x.getLiteralDatatype().equals(XSDDatatype.XSDstring) )
            throw new TextIndexException("Query isn't a string : "+list) ;
        String queryString = x.getLiteralLexicalForm() ;  
        idx++ ;
        
        int limit = -1 ;
        float score = 0 ;
        
        if ( idx < list.size() )
        {        
            // Limit?
            x = list.get(idx) ;
            idx++ ;
            int v = NodeFactoryExtra.nodeToInt(x) ;
            limit = ( v < 0 ) ? -1 : v ; 
        }

        String qs = queryString ;
        if ( field != null )
            qs = field+":"+qs ;
        
        return new StrMatch(p, qs, limit, score) ;
    }
}


