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

import org.openjena.atlas.logging.Log ;

import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.query.QueryExecution ;
import com.hp.hpl.jena.query.QueryExecutionFactory ;
import com.hp.hpl.jena.query.QuerySolution ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.rdf.model.RDFNode ;


public class ReportRemoteVirtuoso
{
    static { Log.setLog4j() ; }
    
    public static void main(String[] args) {
        
        String service = "http://pubmed.bio2rdf.org/sparql";
        String querystring = "PREFIX dc:<http://purl.org/dc/terms/> \n"
            + "PREFIX pub:<http://bio2rdf.org/pubmed:> \n"
            + "PREFIX pubres:<http://bio2rdf.org/pubmed_resource:> \n"
            + "PREFIX foaf:<http://xmlns.com/foaf/0.1/> \n"
            + "select ?title ?mesh ?last ?first \n"
            + "where { \n"
            + "pub:18231773 dc:title ?title . \n"
            + "pub:18231773 pubres:subject_headings ?mesh . \n"
            + "pub:18231773 pubres:author ?authorid . \n"
            + "?authorid foaf:lastName ?last . \n"
            + "?authorid foaf:firstName ?first . \n"
            + "} ";

        ResultSet results = remoteSelectQuery(service, querystring);
        String[] kws = {"title", "mesh", "author"};
        
//        long count = ResultSetFormatter.consume(results) ;
//        System.out.println("Count = "+count) ;
        
        printResults(results, kws);
    }

    private static ResultSet remoteSelectQuery(String service, String querystring) {
        System.out.println(querystring);
        
        if ( true )
            ARQ.getContext().setTrue(ARQ.useSAX) ;
        
        QueryExecution qexec = QueryExecutionFactory.sparqlService(service, querystring);
        
        try {
            return qexec.execSelect();
        } finally {
            //****** TOO EARLY
            qexec.close();
        }
    }

    private static void printResults(ResultSet results, String[] strings) {
        int line = 0 ; 
        while (results.hasNext()) {
            line++ ;
            System.out.printf("%03d: ", line) ;
            QuerySolution soln = results.nextSolution();
            for (String s : strings) {
                RDFNode x = soln.get(s);       // Get a result variable by name.
                if (x != null) {
                    System.out.println(s + ": " + x.toString());
                }
            }
        }
    }

}
