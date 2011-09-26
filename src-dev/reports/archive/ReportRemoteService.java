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

import com.hp.hpl.jena.query.QueryExecution ;
import com.hp.hpl.jena.query.QueryExecutionFactory ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.query.ResultSetFormatter ;

public class ReportRemoteService
{
    public static void main(String...argv)
    {
        System.out.println();
        
        // "snorql" is the web form, "sparql" is the service endpoint
        
        String service="http://www4.wiwiss.fu-berlin.de/dblp/sparql"; // or http://dblp.l3s.de/d2r/snorql/
        String query="select distinct ?Concept where {[] a ?Concept}";
        
        System.out.println("Remote: "+service);

        QueryExecution e = QueryExecutionFactory. sparqlService(service, query);
        try {
            ResultSet results = e.execSelect();
            ResultSetFormatter.out(results) ;
        }

        finally {
            System.out.println("closing!" );
            e.close() ;
        }
    }
}
