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

package com.hp.hpl.jena.tdb.extra;

import java.text.MessageFormat ;
import java.util.Date ;
import java.util.concurrent.TimeUnit ;

import com.hp.hpl.jena.query.* ;
import com.hp.hpl.jena.rdf.model.* ;
import com.hp.hpl.jena.tdb.TDBFactory ;

// From Jena-289.

public class T_TimeoutTDBPattern
{
    private static final int timeout1_sec = 3;
    private static final int timeout2_sec = 5;

    private static final int RESOURCES = 100000;
    private static final int COMMIT_EVERY = 1000;
    private static final int TRIPLES_PER_RESOURCE = 100;
    private static final String RES_NS = "http://example.com/";
    private static final String PROP_NS = "http://example.org/ns/1.0/";

    public static void main(String[] args) {
        String location = "DB_Jena289" ;
        Dataset ds = TDBFactory.createDataset(location);

        if (ds.asDatasetGraph().isEmpty())
            create(ds) ;
        
        // 10M triples.
        // No match to { ?a ?b ?c . ?c ?d ?e }

        final String sparql = "SELECT * WHERE { ?a ?b ?c . ?c ?d ?e }";
        
        Query query = QueryFactory.create(sparql);

        ds.begin(ReadWrite.READ);
        System.out.println(MessageFormat.format("{0,date} {0,time} Executing query [timeout1={1}s timeout2={2}s]: {3}",
                                                new Date(System.currentTimeMillis()), timeout1_sec, timeout2_sec, sparql));
        try(QueryExecution qexec = QueryExecutionFactory.create(query, ds)) {
            if ( true )
                qexec.setTimeout(timeout1_sec, TimeUnit.SECONDS, timeout2_sec, TimeUnit.SECONDS);
            long start = System.nanoTime() ;
            long finish = start ;
            ResultSet rs = qexec.execSelect();
            
            try {
                long x = ResultSetFormatter.consume(rs) ;
                finish = System.nanoTime() ;
                System.out.println("Results: "+x) ; 
            } catch (QueryCancelledException ex)
            {
                finish = System.nanoTime() ;
                System.out.println("Cancelled") ;
            }
            System.out.printf("%.2fs\n",(finish-start)/(1000.0*1000.0*1000.0)) ;
        } catch (Throwable t) {
            t.printStackTrace(); // OOME
        } finally {
            ds.end();
            ds.close();
            System.out.println(MessageFormat.format("{0,date} {0,time} Finished",
                                                    new Date(System.currentTimeMillis())));
        }
    }

    private static void create(Dataset ds)
    {
        for (int iR = 0; iR < RESOURCES; iR++) {    // 100,000
            if (iR % COMMIT_EVERY == 0) {
                if (ds.isInTransaction()) {
                    ds.commit();
                    ds.end();
                }
                ds.begin(ReadWrite.WRITE);
            }

            Model model = ModelFactory.createDefaultModel();
            Resource res = model.createResource(RES_NS + "resource" + iR);
            for (int iP = 0; iP < TRIPLES_PER_RESOURCE; iP++) {     // 100
                Property prop = ResourceFactory.createProperty(PROP_NS, "property" + iP);
                model.add(res, prop, model.createTypedLiteral("Property value " + iP));
            }
            //ds.addNamedModel(res.getURI(), model);
            ds.getDefaultModel().add(model);
            System.out.println("Created " + res.getURI());
        }
        ds.commit();
        ds.end();
    }
}


