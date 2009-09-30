/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

import org.junit.Test;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.opt.Optimize;
import com.hp.hpl.jena.sparql.core.Substitute;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.sse.SSE;
import com.hp.hpl.jena.sparql.util.StrUtils;
import com.hp.hpl.jena.vocabulary.OWL;

public class ReportARQ
{
    @Test
    public void testNOTEXISTSWithBoundVariable() {

        Model model = ModelFactory.createDefaultModel();
        model.read("file:D.ttl", "TTL") ;
        //model.read("http://topquadrant.com/temp/usersForTestCases.owl");
        
        //op is null from E_Exists
        
        String queryString =
            "PREFIX owl: <http://www.w3.org/2002/07/owl#> \n" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
            "SELECT * \n" +
            "WHERE { \n" +
            "    ?cls a owl:Class ." +
            "    NOT EXISTS { \n" +
            "        ?cls rdfs:label ?y . \n" +
            "    }\n" +
            "}";
        
        Query query = QueryFactory.create(queryString, Syntax.syntaxARQ);
        QueryExecution qexec = QueryExecutionFactory.create(query, model);

        QuerySolutionMap map = new QuerySolutionMap();
        map.add("this", OWL.Thing);
        qexec.setInitialBinding(map);
        
        ResultSet rs = qexec.execSelect();
        while(rs.hasNext()) {
            QuerySolution qs = rs.nextSolution();
            System.out.println("Result: " + qs);
        }
    }

    
    public static void main(String[] argv) throws Exception
    {
        String qs = StrUtils.strjoinNL("PREFIX  rdfs:   <http://www.w3.org/2000/01/rdf-schema#>",
                                       "PREFIX fn:      <http://www.w3.org/2005/xpath-functions#>",
                                       "PREFIX : <http://example/>",
                                       "SELECT *" ,
                                       "WHERE {" ,
//                                       "    ?instance a :Person .",
//                                       "    ?instance rdfs:label ?label .",
//                                       "    {",
                                       "        LET (?lab := ?label) .",
                                       "         FILTER fn:starts-with(?lab, \"A\") .",
//                                       "    }",
                                       "} ") ; 
        Query query = QueryFactory.create(qs, Syntax.syntaxARQ) ;
        Op op1 = Algebra.compile(query) ;

        Op op1a = Substitute.substitute(op1, Var.alloc("label"), SSE.parseNode("'aa'")) ;
        System.out.println(op1a) ;
        System.exit(0) ;
        
        
        System.out.println(op1) ;
        Op op2 = Algebra.optimize(op1) ;
        System.out.println(op2) ;
        
        Op op2a = Substitute.substitute(op2, Var.alloc("label"), SSE.parseNode("'aa'")) ;
        System.out.println(op2a) ;
        
        Optimize.noOptimizer() ;
        Op op3 = Algebra.optimize(op1) ;
        System.out.println(op3) ;
        System.exit(0) ;
    }
}

/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
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