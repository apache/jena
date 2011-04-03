/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package reports.archive ;
import java.util.concurrent.ExecutionException ;
import java.util.concurrent.ExecutorService ;
import java.util.concurrent.Executors ;
import java.util.concurrent.Future ;
import java.util.concurrent.TimeUnit ;

import org.apache.log4j.Logger ;
import org.apache.lucene.queryParser.ParseException ;
import org.junit.Test ;

import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryExecution ;
import com.hp.hpl.jena.query.QueryExecutionFactory ;
import com.hp.hpl.jena.query.QueryFactory ;
import com.hp.hpl.jena.query.QuerySolution ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.query.larq.IndexBuilderString ;
import com.hp.hpl.jena.query.larq.IndexLARQ ;
import com.hp.hpl.jena.query.larq.LARQ ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.shared.Lock ;

public class ReportLARQConcurrent {
    
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
        
        @SuppressWarnings("rawtypes")
        Future[] future = new Future[numThreads];
        
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
                         QuerySolution soln = resultSet.next();
                     }
                } finally {
                    model.leaveCriticalSection();
                }
            }
        }
        
    }


    private static Logger logger = Logger.getLogger(ReportLARQConcurrent.class);
}

/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */