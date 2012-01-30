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

package arq.examples;

import java.util.ArrayList ;
import java.util.HashMap ;
import java.util.List ;
import java.util.Map ;

import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryExecution ;
import com.hp.hpl.jena.query.QueryExecutionFactory ;
import com.hp.hpl.jena.query.QueryFactory ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.query.ResultSetFormatter ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;

public class ExampleDBpedia3
{
    static public void main(String... argv) {
        String serviceURI  = "http://dbpedia-live.openlinksw.com/sparql" ;
        String queryString = 
            "SELECT * WHERE { " +
            "    SERVICE <" + serviceURI + "> { " +
            "        SELECT DISTINCT ?company where {?company a <http://dbpedia.org/ontology/Company>} LIMIT 20" +
            "    }" +
            "}" ;
        
        Query query = QueryFactory.create(queryString) ;
        QueryExecution qexec = QueryExecutionFactory.create(query, ModelFactory.createDefaultModel()) ;

        Map<String, Map<String,List<String>>> serviceParams = new HashMap<String, Map<String,List<String>>>() ;
        Map<String,List<String>> params = new HashMap<String,List<String>>() ;
        List<String> values = new ArrayList<String>() ;
        values.add("2000") ;
        params.put("timeout", values) ;
        serviceParams.put(serviceURI, params) ;
        qexec.getContext().set(ARQ.serviceParams, serviceParams) ;
        
        try {
            ResultSet rs = qexec.execSelect() ;
            ResultSetFormatter.out(System.out, rs, query) ;
        } finally {
            qexec.close() ;
        }
    }

}
