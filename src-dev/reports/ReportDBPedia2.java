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

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class ReportDBPedia2
{
    //    public class TestJena {
    public static void main(String[] args) {

        // http://dbpedia.org/sparql?default-graph-uri=http%3A%2F%2Fdbpedia.org&query=PREFIX+rdf%3A+%3Chttp%3A%2F%2Fwww.w3.org%2F1999%2F02%2F22-rdf-syntax-ns%23%3E%0D%0APREFIX+dbpedia-owl%3A+%3Chttp%3A%2F%2Fdbpedia.org%2Fontology%2F%3E%0D%0ASELECT+%3Fvar+%0D%0AWHERE+{+%3Fvar+rdf%3Atype+dbpedia-owl%3ACompany+.+}&debug=on&timeout=&format=text%2Fhtml&save=display&fname=
        
        String queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
            + "PREFIX dbpedia-owl: <http://dbpedia.org/ontology/>"
            + "SELECT ?var "
            + "WHERE { ?var rdf:type dbpedia-owl:Company . }";

        System.out.println(queryString) ;
        
        Query query = QueryFactory.create(queryString);

        QueryExecution qexec = QueryExecutionFactory.sparqlService(
                                                                   "http://dbpedia.org/sparql", query);
        ResultSet rs = null;

        rs = qexec.execSelect();
        // **** TOO EARLY
        //qexec.close();

        while(rs.hasNext()) {
            QuerySolution sqs = rs.next();
            RDFNode node = sqs.get("var");
            System.out.println(node);
        }
        qexec.close();
    }


}
