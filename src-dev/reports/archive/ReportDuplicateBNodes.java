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

import com.hp.hpl.jena.ontology.Individual ;
import com.hp.hpl.jena.ontology.OntClass ;
import com.hp.hpl.jena.ontology.OntModel ;
import com.hp.hpl.jena.ontology.OntModelSpec ;
import com.hp.hpl.jena.ontology.OntProperty ;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryExecution ;
import com.hp.hpl.jena.query.QueryExecutionFactory ;
import com.hp.hpl.jena.query.QueryFactory ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.query.ResultSetFormatter ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;

public class ReportDuplicateBNodes
{
    public static void main(String...argv)
    {
        String queryString = "SELECT * { ?s ?p ?o }" ;
        
        
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        
        String NS = "http://mc-model.owl#";
        OntClass typeA = model.createClass(NS+"TypeA");
        OntClass typeB = model.createClass(NS+"TypeB");
        Individual indiA = model.createIndividual(NS+"IndiA", typeA);
        Individual indiB = model.createIndividual(NS+"IndiB", typeB);
        OntProperty rel = model.createOntProperty(NS+"rel");
        model.createStatement(indiA, rel, indiB).createReifiedStatement();
        Query query = QueryFactory.create(queryString);

        Model model2 = model.getBaseModel() ;
        QueryExecution qe = QueryExecutionFactory.create(query, model2);
        ResultSet results = qe.execSelect();
        ResultSetFormatter.out(results, query);

    }
}
