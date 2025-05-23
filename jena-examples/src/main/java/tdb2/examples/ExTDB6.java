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

package tdb2.examples;

import static java.lang.System.out ;

import java.util.Iterator ;

import org.apache.jena.graph.Graph ;
import org.apache.jena.query.* ;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.rdf.model.Property ;
import org.apache.jena.rdf.model.Resource ;
import org.apache.jena.rdf.model.Statement ;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFWriter;
import org.apache.jena.tdb2.TDB2Factory;

/** Example of single threaded use of TDB working with the Jena RDF API */
public class ExTDB6
{
    public static String MY_NS =
        "x-ns://example.org/ns1/";

    public static void main(String[] args) throws Exception {

        // From an exisiing database...
//        String DATASET_DIR_NAME = "data0";
//        Dataset dataset = TDB2Factory.connectDataset( DATASET_DIR_NAME );

        Dataset dataset = TDB2Factory.createDataset( );

        // show the currently registered names
        dataset.executeRead(()->{
            for (Iterator<String> it = dataset.listNames(); it.hasNext(); ) {
                out.println("NAME="+it.next());
            }
        });

        out.println("getting named model...");
        /// this is the OWL portion
        Model model = dataset.getNamedModel( MY_NS );
        dataset.executeRead(()-> {
            out.println("Model: "+model.size()+" statements");
            RDFWriter.source(model).lang(Lang.TTL).output(System.out);
        });
        out.println("getting graph...");
        /// this is the DATA in that MODEL
        Graph graph = model.getGraph();
        dataset.executeRead(()-> {
            out.println("Graph: "+model.size()+" triples");
            RDFWriter.source(graph).lang(Lang.TTL).output(System.out);
        });

        dataset.executeWrite(()->{
            if (graph.isEmpty()) {
                Resource product1 = model.createResource( MY_NS +"product/1");
                Property hasName = model.createProperty( MY_NS, "#hasName");
                Statement stmt = model.createStatement(
                                                             product1, hasName, model.createLiteral("Beach Ball","en") );
                out.println("Statement = " + stmt);

                model.add(stmt);

                // just for fun
                out.println("Triple := " + stmt.asTriple().toString());
            } else {
                out.println("Graph is not Empty; it has "+graph.size()+" Statements");
                long t0, t1;
                t0 = System.currentTimeMillis();
                Query q = QueryFactory.create("""
                                PREFIX exns: <"+MY_NS+"#>
                                PREFIX exprod: <"+MY_NS+"product/>
                                SELECT *
                                // if you don't provide the Model to the
                                // QueryExecutionFactory below, then you'll need
                                // to specify the FROM;
                                // you *can* always specify it, if you want
                                // FROM <"+MY_NS+">

                                // WHERE { ?node <"+MY_NS+"#hasName> ?name }
                                // WHERE { ?node exns:hasName ?name }
                                // WHERE { exprod:1 exns:hasName ?name }
                                WHERE { ?res ?pred ?obj }
                                """
                                        );
                out.println("Query := "+q);
                t1 = System.currentTimeMillis();
                out.println("QueryFactory.TIME="+(t1 - t0));

                t0 = System.currentTimeMillis();
                try ( QueryExecution qExec = QueryExecutionFactory
                        // if you query the whole DataSet,
                        // you have to provide a FROM in the SparQL
                        //.create(q, data0);
                        .create(q, model) ) {
                    t1 = System.currentTimeMillis();
                    out.println("QueryExecutionFactory.TIME="+(t1 - t0));

                    t0 = System.currentTimeMillis();
                    ResultSet rs = qExec.execSelect();
                    t1 = System.currentTimeMillis();
                    out.println("executeSelect.TIME="+(t1 - t0));
                    while (rs.hasNext()) {
                        QuerySolution sol = rs.next();
                        out.println("Solution := "+sol);
                        for (Iterator<String> names = sol.varNames(); names.hasNext(); ) {
                            String name = names.next();
                            out.println("\t"+name+" := "+sol.get(name));
                        }
                    }
                }
            }
        });
    }
}