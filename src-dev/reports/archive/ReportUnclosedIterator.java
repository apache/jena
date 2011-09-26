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

import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryExecution ;
import com.hp.hpl.jena.query.QueryExecutionFactory ;
import com.hp.hpl.jena.query.QueryFactory ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.query.Syntax ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;

public class ReportUnclosedIterator
{
    public static void main(String...argv)
    {
        // Analysis
        // Aggregation reads whole results and then "replaces" the root iterator."
        // Order does the same? No, because ORDER is over everyting, this is per group. 
        
        Model model = ModelFactory.createDefaultModel();
        // Insert one triple here.
        //model.getGraph().add(SSE.parseTriple("(<x> <p> <y>)")) ;
        
        String str = "SELECT count(?object) WHERE { ?subject ?p ?object }";
        Query query = QueryFactory.create(str, Syntax.syntaxARQ);
        QueryExecution qexec = QueryExecutionFactory.create(query, model);
        ResultSet rs = qexec.execSelect();
//        ResultSetFormatter.out(rs) ;
//        if ( rs.hasNext() ) 
//            rs.next();
        //rs.hasNext() ; // If this, forcing iteraors to finish neatly, it works.
        
        qexec.close();
        System.out.println("Exit") ;
    }
}
