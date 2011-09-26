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

package reports.archive;

import org.junit.Test ;
import org.openjena.atlas.lib.StrUtils ;

import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryFactory ;
import com.hp.hpl.jena.query.Syntax ;
import com.hp.hpl.jena.sparql.algebra.Algebra ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.optimize.Optimize ;
import com.hp.hpl.jena.sparql.core.Substitute ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.util.NodeFactory ;

public class ReportAssignSubstitute
{
    @Test
    public void test()
    {
    }

    
    public static void main(String[] argv) throws Exception
    {
        // Test case needed.
        String qs = StrUtils.strjoinNL("PREFIX  rdfs:   <http://www.w3.org/2000/01/rdf-schema#>",
                                       "PREFIX fn:      <http://www.w3.org/2005/xpath-functions#>",
                                       "PREFIX : <http://example/>",
                                       "SELECT *" ,
                                       "WHERE {" ,
//                                       "    ?instance a :Person .",
                                       "    ?instance rdfs:label ?label .",
//                                       "    {",
//                                       "        LET (?lab := ?label) .",
                                       "        LET (?label := ?label) .",
                                       "         FILTER fn:starts-with(?lab, \"A\") .",
//                                       "    }",
                                       "} ") ; 
        Query query = QueryFactory.create(qs, Syntax.syntaxARQ) ;
        Op op1 = Algebra.compile(query) ;

        Op op1a = Substitute.substitute(op1, Var.alloc("label"), NodeFactory.parseNode("'aa'")) ;
        System.out.println(op1a) ;
        System.exit(0) ;
        
        
        System.out.println(op1) ;
        Op op2 = Algebra.optimize(op1) ;
        System.out.println(op2) ;
        
        Op op2a = Substitute.substitute(op2, Var.alloc("label"), NodeFactory.parseNode("'aa'")) ;
        System.out.println(op2a) ;
        
        Optimize.noOptimizer() ;
        Op op3 = Algebra.optimize(op1) ;
        System.out.println(op3) ;
        System.exit(0) ;
    }
}
