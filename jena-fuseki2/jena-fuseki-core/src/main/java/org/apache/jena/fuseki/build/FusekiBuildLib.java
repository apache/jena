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

package org.apache.jena.fuseki.build;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.jena.fuseki.FusekiConfigException;
import org.apache.jena.query.* ;
import org.apache.jena.rdf.model.Literal ;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode ;
import org.apache.jena.rdf.model.Resource ;
import org.apache.jena.shared.JenaException;
import org.apache.jena.shared.PrefixMapping ;
import org.apache.jena.vocabulary.RDFS ;

/**
 * Library code for operations related to building Fuseki servers and services.
 */
public class FusekiBuildLib {

    // ---- Helper code
    public static ResultSet query(String string, Model m) {
        return query(string, m, null, null) ;
    }

    public static RDFNode queryOne(String string, Model m, String varname) {
        ResultSet rs = query(string, m) ;
        return getExactlyOne(rs, varname);
    }
    
    private static RDFNode getExactlyOne(ResultSet rs, String varname) {
        if ( ! rs.hasNext() )
            return null;
        QuerySolution qs = rs.next();
        if ( rs.hasNext() )
            return null;
        return qs.get(varname);
    }

    public static ResultSet query(String string, Dataset ds) {
        return query(string, ds, null, null) ;
    }

    public static ResultSet query(String string, Model m, String varName, RDFNode value) {
        Query query = QueryFactory.create(FusekiConst.PREFIXES + string) ;
        QuerySolutionMap initValues = null ;
        if ( varName != null && value != null )
            initValues = querySolution(varName, value) ;
        try ( QueryExecution qExec = QueryExecutionFactory.create(query, m, initValues) ) {
            return ResultSetFactory.copyResults(qExec.execSelect()) ;
        }
    }

    public static ResultSet query(String string, Dataset ds, String varName, RDFNode value) {
        Query query = QueryFactory.create(FusekiConst.PREFIXES + string) ;
        QuerySolutionMap initValues = null ;
        if ( varName != null && value != null )
            initValues = querySolution(varName, value) ;
        try ( QueryExecution qExec = QueryExecutionFactory.create(query, ds, initValues) ) {
            return ResultSetFactory.copyResults(qExec.execSelect()) ;
        }
    }

    private static QuerySolutionMap querySolution(String varName, RDFNode value) {
        QuerySolutionMap qsm = new QuerySolutionMap() ;
        querySolution(qsm, varName, value) ;
        return qsm ;
    }

    public static QuerySolutionMap querySolution(QuerySolutionMap qsm, String varName, RDFNode value) {
        qsm.add(varName, value) ;
        return qsm ;
    }

    public static RDFNode getOne(Resource svc, String property) {
        ResultSet rs = FusekiBuildLib.query("SELECT * { ?svc " + property + " ?x}", svc.getModel(), "svc", svc) ;
        if ( !rs.hasNext() )
            throw new FusekiConfigException("No property '" + property + "' for service " + FusekiBuildLib.nodeLabel(svc)) ;
        RDFNode x = rs.next().get("x") ;
        if ( rs.hasNext() )
            throw new FusekiConfigException("Multiple properties '" + property + "' for service " + FusekiBuildLib.nodeLabel(svc)) ;
        return x ;
    }

    /**
     * Get all object of a subject/property, whether repeated triples or RDF Lists or a
     * mixture. If the subject/property isn't present, return null, so a caller can tell
     * the difference between "not present" and an empty list value.
     */
    public static Collection<RDFNode> getAll(Resource svc, String property) {
        ResultSet rs = FusekiBuildLib.query("SELECT * { ?svc " + property + " ?x}", svc.getModel(), "svc", svc) ;
        if ( ! rs.hasNext() )
            return null;
        List<RDFNode> results = new ArrayList<>();
        rs.forEachRemaining(qs->{
            RDFNode n = qs.get("x");
            try {
                RDFList list = n.as(RDFList.class);
                results.addAll(list.asJavaList());
            } catch (JenaException x) {
                // Not a list.
                results.add(n);
            }
        });
        return results ;
    }

    // Node presentation
    public static String nodeLabel(RDFNode n) {
        if ( n == null )
            return "<null>" ;
        if ( n instanceof Resource )
            return strForResource((Resource)n) ;
    
        Literal lit = (Literal)n ;
        return lit.getLexicalForm() ;
    }

    public static String strForResource(Resource r) {
        return strForResource(r, r.getModel()) ;
    }

    public static String strForResource(Resource r, PrefixMapping pm) {
        if ( r == null )
            return "NULL " ;
        if ( r.hasProperty(RDFS.label) ) {
            RDFNode n = r.getProperty(RDFS.label).getObject() ;
            if ( n instanceof Literal )
                return ((Literal)n).getString() ;
        }
    
        if ( r.isAnon() )
            return "<<blank node>>" ;
    
        if ( pm == null )
            pm = r.getModel() ;
    
        return strForURI(r.getURI(), pm) ;
    }

    public static String strForURI(String uri, PrefixMapping pm) {
        if ( pm != null ) {
            String x = pm.shortForm(uri) ;
    
            if ( !x.equals(uri) )
                return x ;
        }
        return "<" + uri + ">" ;
    }
}
