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


import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.update.GraphStore ;
import com.hp.hpl.jena.update.GraphStoreFactory ;
import com.hp.hpl.jena.update.UpdateAction ;
import com.hp.hpl.jena.update.UpdateFactory ;
import com.hp.hpl.jena.update.UpdateRequest ;


public class ReportUpdate1
{
    public static void main (String... argv)
    {
        GraphStore graphStore = GraphStoreFactory.create() ;

        String q = 
            "CREATE GRAPH <example:store> \n" +  // create a graph
            "INSERT INTO  <example:store> { _:foo a <example:Thing> . } \n" + // add some data
            "DELETE FROM  <example:store> { ?s ?p ?o } WHERE { ?s ?p ?o } \n"; // remove all data

        UpdateRequest req = UpdateFactory.create(q) ;
        
        UpdateRequest req1 = UpdateFactory.create("CREATE GRAPH <example:store>") ;
        UpdateRequest req2 = UpdateFactory.create("INSERT INTO  <example:store> { _:foo a <example:Thing> . }") ;
        UpdateRequest req3 = UpdateFactory.create("DELETE FROM  <example:store> { ?s ?p ?o } WHERE { ?s ?p ?o }") ;
        UpdateRequest req4 = UpdateFactory.create("DELETE FROM  <example:store> { ?s ?p ?o } WHERE { GRAPH <example:store> {?s ?p ?o } }") ;
        
        
        //System.out.println("---------------------") ;
        SSE.write(graphStore) ;
        UpdateAction.execute(req1, graphStore);
        System.out.println("---------------------") ;
        SSE.write(graphStore) ;
        UpdateAction.execute(req2, graphStore);
        System.out.println("---------------------") ;
        SSE.write(graphStore) ;
        UpdateAction.execute(req4, graphStore);
        System.out.println("---------------------") ;
        SSE.write(graphStore) ;
        System.out.println("---------------------") ;
        
        //UpdateAction.execute(UpdateFactory.create(q), graphStore);
        SSE.write(graphStore) ;
    }

}
