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

package reports;

import org.openjena.atlas.lib.StrUtils ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryExecution ;
import com.hp.hpl.jena.query.QueryExecutionFactory ;
import com.hp.hpl.jena.query.QueryFactory ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.query.ResultSetFormatter ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.sparql.algebra.Algebra ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.sparql.util.graph.GraphFactory ;

public class ReportFilterEquality
{
    public static void main(String...argv)
    {
        String qs = StrUtils.strjoinNL(
                           "PREFIX ex: <http://example.com/ns#>" ,
                           "SELECT ?x WHERE {" ,
                           "    ?s a ?c" ,
                           "    OPTIONAL { ?s ex:property ?x }" ,
                           "    FILTER (?c = ex:v)",
                           "}") ;
        Query query = QueryFactory.create(qs) ;
        
        Op op = Algebra.compile(query) ;
        Op op2 = Algebra.optimize(op) ;
        System.out.println(op) ;
        System.out.println(op2) ;
        
        Graph g = GraphFactory.createGraphMem() ;
        Triple t = SSE.parseTriple("(<x> rdf:type <T>)") ;
        g.add(t) ;
        Model m = ModelFactory.createModelForGraph(g) ;
        QueryExecution qExec = QueryExecutionFactory.create(query, m) ;
        ResultSet rs = qExec.execSelect() ;
        ResultSetFormatter.out(rs) ;
    }
}
