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

import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryExecution ;
import com.hp.hpl.jena.query.QueryExecutionFactory ;
import com.hp.hpl.jena.query.QueryFactory ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.rdf.model.Resource ;
import com.hp.hpl.jena.vocabulary.RDFS ;

public class ReportPathCycles
{
    public static void main(String...argv)
    {
        Model model = ModelFactory.createDefaultModel();
        // Create chain
        int count = 1000;
        for(int i = 0; i < count; i++) {
            Resource subClass = model.createResource("urn:x-test:class-" + i);
            Resource superClass = model.createResource("urn:x-test:class-" + (i + 1));
            model.add(subClass, RDFS.subClassOf, superClass);
        }
        // Create random cycles
        for(int i = 0; i < 100; i++) {
            Resource subClass = model.createResource("urn:x-test:class-" + (int)(Math.random() * count));
            Resource superClass = model.createResource("urn:x-test:class-" + (int)(Math.random() * count));
            model.add(subClass, RDFS.subClassOf, superClass);
        }
        Query query = QueryFactory.create("SELECT * WHERE { ?x <" + RDFS.subClassOf + ">* ?y }");
        QueryExecution qexec = QueryExecutionFactory.create(query, model);
        ResultSet rs = qexec.execSelect();
        int x = 0 ;
        while(rs.hasNext()) {
            x++ ;
            rs.next();
        }
        System.out.println("results = "+x) ;
    }
}
