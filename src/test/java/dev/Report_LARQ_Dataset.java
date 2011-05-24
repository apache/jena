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

package dev;

import java.io.IOException;

import org.apache.jena.larq.IndexLARQ;
import org.apache.jena.larq.LARQ;
import org.apache.jena.larq.assembler.AssemblerLARQ;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.vocabulary.RDFS;

public class Report_LARQ_Dataset {

    public static void main(String[] args) throws IOException {
        Dataset dataset = TDBFactory.createDataset();
        Model d = dataset.getDefaultModel();
        Model m1 = dataset.getNamedModel("u1");
        Model m2 = dataset.getNamedModel("u2");

        IndexLARQ indexLARQ = AssemblerLARQ.make(dataset, null);
        LARQ.setDefaultIndex(indexLARQ);

        d.add(ResourceFactory.createResource("x"), RDFS.label, "london");
        m1.add(ResourceFactory.createResource("y"), RDFS.label, "london");
        m2.add(ResourceFactory.createResource("z"), RDFS.label, "london");
        query(dataset);
        
        m1.remove(ResourceFactory.createResource("y"), RDFS.label, ResourceFactory.createPlainLiteral("london"));
        query(dataset);
        query(dataset);
    }
    
    private static void query(Dataset dataset) {
        Query q = QueryFactory.create(
                "PREFIX pf: <http://jena.hpl.hp.com/ARQ/property#>" +
                "SELECT * WHERE {" +
                "  GRAPH ?g { " +
                "    ?doc ?p ?lit ." +
                "    (?lit ?score ) pf:textMatch '+london' ." +
                "  } " +
                "}"
                );

        QueryExecution qe = QueryExecutionFactory.create(q, dataset);
        ResultSet res = qe.execSelect();
        ResultSetFormatter.out(res);
        qe.close();
    }

}