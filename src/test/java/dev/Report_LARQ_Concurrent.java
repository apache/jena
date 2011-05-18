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

package dev ;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.jena.larq.IndexBuilderString;
import org.apache.jena.larq.IndexLARQ;
import org.apache.jena.larq.LARQ;
import org.apache.log4j.Logger;
import org.apache.lucene.queryParser.ParseException;
import org.junit.Test;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.shared.Lock;

public class Report_LARQ_Concurrent {
    
    static final int numThreads = 2;
    static final long duration = 2;
    long startTime = System.currentTimeMillis();
    long endTime;
    Model model;

    @Test
    public void test_concurrent_larq_query() throws ParseException, InterruptedException, ExecutionException {
        endTime = startTime + duration * 1000; 
        model = ModelFactory.createDefaultModel();
        
          IndexBuilderString larqBuilder = new IndexBuilderString() ;
          IndexLARQ index = larqBuilder.getIndex() ;
          LARQ.setDefaultIndex(index) ;

        ExecutorService executorService = Executors.newCachedThreadPool();  
        
        Future<?>[] future = new Future<?>[numThreads];
        
        for (int t=0; t<numThreads; t++) {
            future[t]= executorService.submit(new R());
        }
        
        try {
            executorService.awaitTermination(duration + 2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.info("main thread interupted");
        }
        
        // make sure everything is stopped - as best we can
        executorService.shutdownNow();
        
        for (int t=0; t<numThreads; t++) {
            future[t].get();
        }
    }
    
    class R implements Runnable {
        final String queryString =  
            "PREFIX pf: <http://jena.hpl.hp.com/ARQ/property#>\n" +
            "SELECT ?doc ?score\n" +
            "WHERE {\n" +
            "   (?lit ?score ) pf:textMatch '+text' .\n" +
            "   ?doc ?p ?lit\n" +
            "}\n";

        //@Override
        public void run() {
            while (System.currentTimeMillis() < endTime) {
                model.enterCriticalSection(Lock.READ);
                try {
                     Query query = QueryFactory.create(queryString) ;
                     QueryExecution qExec = QueryExecutionFactory.create(query, model) ;
                     ResultSet resultSet = qExec.execSelect();
                     while (resultSet.hasNext()) {
                         resultSet.next();
                     }
                } finally {
                    model.leaveCriticalSection();
                }
            }
        }
        
    }


    private static Logger logger = Logger.getLogger(Report_LARQ_Concurrent.class);
}
