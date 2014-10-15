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

import java.security.SecureRandom ;
import java.util.Iterator ;
import java.util.concurrent.CountDownLatch ;
import java.util.concurrent.ExecutorService ;
import java.util.concurrent.Executors ;
import java.util.concurrent.atomic.AtomicInteger ;

import org.apache.jena.atlas.logging.LogCtl ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.query.ReadWrite ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.tdb.StoreConnection ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.transaction.DatasetGraphTxn ;
import com.hp.hpl.jena.tdb.transaction.TransactionManager ;

public class T_TxnDeadlockTest {

    static { 
        LogCtl.setLog4j() ; 
        //Log.enable("TDB") ;
        if ( false ) LogCtl.enable(TransactionManager.class) ;
        //Log.enable(LockMRSW.class) ;
    }
    
    private static final int CONCURRENT_RANDOM_OPERATIONS = 1000;

    private static final SecureRandom numberGenerator = new SecureRandom();

    public static void main(String ... argv)
    {
        for(int i = 0 ; i < 1000 ; i++ )
        {
            System.out.println("Loop = "+i) ;
            new T_TxnDeadlockTest().test() ;
        }
    }
    
    //@Test
    public void test() {
        final StoreConnection storeConnection =
                StoreConnection.make(Location.mem());

        //ExecutorService executor = Executors.newCachedThreadPool()  ;     // Not seen blocking. 
        // 4 blocks maybe 1 in 4 times
        // 8 blocks (quad core) 2 in 3 times.
        ExecutorService executor = Executors.newFixedThreadPool(8) ;

        final AtomicInteger nbQuadruplesAdded = new AtomicInteger();

        final CountDownLatch doneSignal =
                new CountDownLatch(CONCURRENT_RANDOM_OPERATIONS);

        for (int i = 0; i < CONCURRENT_RANDOM_OPERATIONS; i++) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (numberGenerator.nextInt(2) == 0) {
                            DatasetGraphTxn txnGraph =
                                    storeConnection.begin(ReadWrite.WRITE);
                            txnGraph.add(new Quad(
                                    NodeFactory.createURI("http://openjena.org/"
                                            + numberGenerator.nextInt()),
                                    NodeFactory.createURI("http://openjena.org/"
                                            + numberGenerator.nextInt()),
                                    NodeFactory.createURI("http://openjena.org/"
                                            + numberGenerator.nextInt()),
                                    NodeFactory.createURI("http://openjena.org/"
                                            + numberGenerator.nextInt())));
                            txnGraph.commit();
                            txnGraph.end();
                            nbQuadruplesAdded.incrementAndGet();
                        } else {
                            DatasetGraphTxn txnGraph =
                                    storeConnection.begin(ReadWrite.READ);
                            txnGraph.find(Node.ANY, Node.ANY, Node.ANY, Node.ANY);
                            //Iterator<Quad> iter = txnGraph.find(Node.ANY, Node.ANY, Node.ANY, Node.ANY);
                            //Iter.count(iter) ; // Consume
                            txnGraph.end();
                        }
                    } finally {
                        doneSignal.countDown();
                    }
                }
            });
        }

        // shutdown is orderly so sync'ing up before the shutdown is nice but not needed.
        try {
            doneSignal.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            executor.shutdown();
        }

        DatasetGraphTxn txnGraph =
                storeConnection.begin(ReadWrite.READ);
        Iterator<Quad> result = txnGraph.find(
                Node.ANY, Node.ANY, Node.ANY, Node.ANY);
        
        long count = 0;
        while (result.hasNext()) {
            result.next();
            count++;
        }
        
        txnGraph.end();
        
        StoreConnection.release(storeConnection.getLocation());

        //System.out.println() ;
        System.out.println("FINISHED") ;
        
//        // This is unsafe - the quad adds may generate duplicates (ity's unlikly 4 random number reoccur but it's possible). 
//        Assert.assertEquals(count, nbQuadruplesAdded.get());
    }
    
}
