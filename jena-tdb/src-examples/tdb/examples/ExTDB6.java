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

package tdb.examples;

import static java.lang.System.out ;

import java.util.Iterator ;

import org.apache.jena.iri.IRIFactory ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.query.* ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.Property ;
import com.hp.hpl.jena.rdf.model.Resource ;
import com.hp.hpl.jena.rdf.model.Statement ;
import com.hp.hpl.jena.tdb.TDB ;
import com.hp.hpl.jena.tdb.TDBFactory ;

/** Example of single threaded use of TDB workign with the Jena RDF API */
public class ExTDB6
{
    /// observe the non-http "protocol", because it is a bad precident
    /// to only use http IRI when there is no http protocol involved
    public static final String MY_NS =
        "x-ns://example.org/ns1/";

    public static void main(String[] args) throws Exception {
        /// turn off the "No BGP optimizer warning"
        TDB.setOptimizerWarningFlag(false);

        final IRIFactory iriFactory = IRIFactory.semanticWebImplementation();

        final String DATASET_DIR_NAME = "data0";
        final Dataset data0 = TDBFactory.createDataset( DATASET_DIR_NAME );

        // show the currently registered names
        for (Iterator<String> it = data0.listNames(); it.hasNext(); ) {
            out.println("NAME="+it.next());
        }

        out.println("getting named model...");
        /// this is the OWL portion
        final Model model = data0.getNamedModel( MY_NS );
        out.println("Model := "+model);

        out.println("getting graph...");
        /// this is the DATA in that MODEL
        final Graph graph = model.getGraph();
        out.println("Graph := "+graph);

        if (graph.isEmpty()) {
            final Resource product1 = model.createResource(
                    iriFactory.construct( MY_NS +"product/1" )
                        .toString() );

            final Property hasName = model.createProperty( MY_NS, "#hasName");
            final Statement stmt = model.createStatement(
                    product1, hasName, model.createLiteral("Beach Ball","en") );
            out.println("Statement = " + stmt);

            model.add(stmt);

            // just for fun
            out.println("Triple := " + stmt.asTriple().toString());
        } else {
            out.println("Graph is not Empty; it has "+graph.size()+" Statements");
            long t0, t1;
            t0 = System.currentTimeMillis();
            final Query q = QueryFactory.create(
                    "PREFIX exns: <"+MY_NS+"#>\n"+
                    "PREFIX exprod: <"+MY_NS+"product/>\n"+
                    " SELECT * "
                    // if you don't provide the Model to the
                    // QueryExecutionFactory below, then you'll need
                    // to specify the FROM;
                    // you *can* always specify it, if you want
                    // +" FROM <"+MY_NS+">\n"
                    // +" WHERE { ?node <"+MY_NS+"#hasName> ?name }"
                    // +" WHERE { ?node exns:hasName ?name }"
                    // +" WHERE { exprod:1 exns:hasName ?name }"
                    +" WHERE { ?res ?pred ?obj }"
                    );
            out.println("Query := "+q);
            t1 = System.currentTimeMillis();
            out.println("QueryFactory.TIME="+(t1 - t0));

            t0 = System.currentTimeMillis();
            final QueryExecution qExec = QueryExecutionFactory
                    // if you query the whole DataSet,
                    // you have to provide a FROM in the SparQL
                    //.create(q, data0);
                    .create(q, model);
            t1 = System.currentTimeMillis();
            out.println("QueryExecutionFactory.TIME="+(t1 - t0));

            try {
                t0 = System.currentTimeMillis();
                ResultSet rs = qExec.execSelect();
                t1 = System.currentTimeMillis();
                out.println("executeSelect.TIME="+(t1 - t0));
                while (rs.hasNext()) {
                    QuerySolution sol = rs.next();
                    out.println("Solution := "+sol);
                    for (Iterator<String> names = sol.varNames(); names.hasNext(); ) {
                        final String name = names.next();
                        out.println("\t"+name+" := "+sol.get(name));
                    }
                }
            } finally {
                qExec.close();
            }
        }
        out.println("closing graph");
        graph.close();
        out.println("closing model");
        model.close();
        //out.println("closing DataSetGraph");
        //dsg.close();
        out.println("closing DataSet");
        data0.close();
    }
}
