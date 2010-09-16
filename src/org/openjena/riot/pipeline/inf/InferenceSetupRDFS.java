/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.riot.pipeline.inf;

import java.util.ArrayList ;
import java.util.HashMap ;
import java.util.List ;
import java.util.Map ;

import org.openjena.atlas.lib.StrUtils ;

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
    /*package*/ final Map<Node, List<Node>> transClasses        = new HashMap<Node, List<Node>>() ;
    /*package*/ final Map<Node, List<Node>> transProperties     = new HashMap<Node, List<Node>>() ;
    /*package*/ final Map<Node, List<Node>> domainList          = new HashMap<Node, List<Node>>() ;
    /*package*/ final Map<Node, List<Node>> rangeList           = new HashMap<Node, List<Node>>() ;  
    
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

/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */