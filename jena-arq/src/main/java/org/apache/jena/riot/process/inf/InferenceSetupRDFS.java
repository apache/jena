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

package org.apache.jena.riot.process.inf;

import java.util.ArrayList ;
import java.util.HashMap ;
import java.util.List ;
import java.util.Map ;

import org.apache.jena.atlas.lib.StrUtils ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryExecution ;
import com.hp.hpl.jena.query.QueryExecutionFactory ;
import com.hp.hpl.jena.query.QueryFactory ;
import com.hp.hpl.jena.query.QuerySolution ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.query.Syntax ;
import com.hp.hpl.jena.rdf.model.Model ;

public class InferenceSetupRDFS
{
    /*package*/ final Map<Node, List<Node>> transClasses        = new HashMap<>() ;
    /*package*/ final Map<Node, List<Node>> transProperties     = new HashMap<>() ;
    /*package*/ final Map<Node, List<Node>> domainList          = new HashMap<>() ;
    /*package*/ final Map<Node, List<Node>> rangeList           = new HashMap<>() ;
    
    public InferenceSetupRDFS(Model vocab)
    {
        // Find classes - uses property paths
        exec("SELECT ?x ?y { ?x rdfs:subClassOf+ ?y }", vocab, transClasses) ;
        
        // Find properties
        exec("SELECT ?x ?y { ?x rdfs:subPropertyOf+ ?y }", vocab, transProperties) ;
        
        // Find domain
        exec("SELECT ?x ?y { ?x rdfs:domain ?y }", vocab, domainList) ;
        
        // Find range
        exec("SELECT ?x ?y { ?x rdfs:range ?y }", vocab, rangeList) ;
    }
    
    private static void exec(String qs, Model model, Map<Node, List<Node>> multimap)
    {
        String preamble = StrUtils.strjoinNL("PREFIX  rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#>",
                                             "PREFIX  rdfs:   <http://www.w3.org/2000/01/rdf-schema#>",
                                             "PREFIX  xsd:    <http://www.w3.org/2001/XMLSchema#>",
                                             "PREFIX  owl:    <http://www.w3.org/2002/07/owl#>",
                                             "PREFIX skos:    <http://www.w3.org/2004/02/skos/core#>") ;
        Query query = QueryFactory.create(preamble+"\n"+qs, Syntax.syntaxARQ) ;
        QueryExecution qexec = QueryExecutionFactory.create(query, model) ;
        ResultSet rs = qexec.execSelect() ;
        for ( ; rs.hasNext() ; )
        {
            QuerySolution soln= rs.next() ;
            Node x = soln.get("x").asNode() ;
            Node y = soln.get("y").asNode() ;
            if ( ! multimap.containsKey(x) )
                multimap.put(x, new ArrayList<Node>()) ;
            multimap.get(x).add(y) ;
        }
    }
}
