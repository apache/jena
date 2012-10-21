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

package com.hp.hpl.jena.rdf.model.test ;
import com.hp.hpl.jena.rdf.model.* ;

import com.hp.hpl.jena.shared.Lock;

import junit.framework.*;
public class TestConcurrency  extends TestSuite
{

    /** Creates new RDQLTestSuite */
        static public TestSuite suite() {
            return new TestConcurrency();
        }

	// Test suite to exercise the locking
	static long SLEEP = 100 ;
	static int threadCount = 0;

    // Note : reuse the model across tests.
    final static Model model1 = ModelFactory.createDefaultModel() ;
    final static Model model2 = ModelFactory.createDefaultModel() ;
        
    public TestConcurrency()
    {
        super("Model concurrency control") ;

        if ( true )
        {
            // Same model: inner and outer
            addTest(new Nesting("Lock nesting 1 - same model", 
                    model1, Lock.READ, Lock.READ, false)) ;
            addTest(new Nesting("Lock nesting 2 - same model",
                    model1, Lock.WRITE, Lock.WRITE, false)) ;
            addTest(new Nesting("Lock nesting 3 - same model",
                    model1, Lock.READ, Lock.WRITE, true)) ;
            addTest(new Nesting("Lock nesting 4 - same model",
                    model1, Lock.WRITE, Lock.READ, false)) ;
    
            // Different  model: inner and outer
            addTest(new Nesting("Lock nesting 1 - different models", 
                    model1, Lock.READ, model2, Lock.READ, false)) ;
            addTest(new Nesting("Lock nesting 2 - different models",
                    model1, Lock.WRITE, model2, Lock.WRITE, false)) ;
            addTest(new Nesting("Lock nesting 3 - different models",
                    model1, Lock.READ, model2, Lock.WRITE, false)) ;
            addTest(new Nesting("Lock nesting 4 - different models",
                    model1, Lock.WRITE, model2, Lock.READ, false)) ;
        }
        if ( true )
        {
            // Crude test                
            addTest(new Parallel("Parallel concurrency test")) ;
        }

    }

    static class Nesting extends TestCase
    {
        Model outerModel ;
        Model innerModel ;
        boolean outerLock ;
        boolean innerLock ;
        boolean exceptionExpected ;

        // Same model
        Nesting(String testName, Model model,
                boolean lock1, boolean lock2, boolean exExpected)
        {
            this(testName, model, lock1, model, lock2, exExpected) ;
        }

        // Potetnially different models
        Nesting(String testName,
                Model model1, boolean lock1,
                Model model2, boolean lock2, boolean exExpected)
        {
            super(testName);
            outerModel = model1 ; 
            outerLock = lock1 ;
            innerModel = model2 ;
            innerLock = lock2 ;
            exceptionExpected = exExpected ;
        }

        @Override
        protected void runTest() throws Throwable
        {
            boolean gotException = false ;
            try {
                outerModel.enterCriticalSection(outerLock) ;
                
                try {
                    try {
                        // Should fail if outerLock is READ and innerLock is WRITE
                        // and its on the same model, inner and outer.
                        innerModel.enterCriticalSection(innerLock) ;
                        
                    } finally { innerModel.leaveCriticalSection() ; }
                } catch (Exception ex)
                {
                    gotException = true ;
                }
                
            } finally { 
                outerModel.leaveCriticalSection() ;
            }
            
            if ( exceptionExpected )
                assertTrue("Failed to get expected lock promotion error", gotException) ;
            else
                assertTrue("Got unexpected lock promotion error", !gotException) ;
        }
    }
    
    
    static class Parallel extends TestCase
    {
        int threadTotal = 10 ; 
        
        
        Parallel(String testName)
        {
            super(testName) ;
        }
        
        @Override
        protected void runTest() throws Throwable
        {
            Model model = ModelFactory.createDefaultModel() ;
            Thread threads[] = new Thread[threadTotal] ;

            boolean getReadLock = Lock.READ ;
            for (int i = 0; i < threadTotal; i++)
            {
                String nextId = "T"+Integer.toString(++threadCount);
                threads[i] = new Operation(model, getReadLock) ;
                threads[i].setName(nextId) ;
                threads[i].start() ;
                
                getReadLock = ! getReadLock ;
            }
        
            boolean problems = false ;
            for ( int i = 0; i < threadTotal; i++)
            {
                try { threads[i].join(200*SLEEP) ; } catch (InterruptedException intEx) {}
            }

            // Try again for any we missed.
            for ( int i = 0; i < threadTotal; i++)
            {
                if ( threads[i].isAlive() )
                    try { threads[i].join(200*SLEEP) ; } catch (InterruptedException intEx) {}
                if ( threads[i].isAlive())
                {
                    System.out.println("Thread "+threads[i].getName()+" failed to finish") ;
                    problems = true ;
                }
            }
            
            
            
            assertTrue("Some thread failed to finish", !problems) ;
        }

        class Operation extends Thread
        {
             Model model ;
             boolean readLock ;
            
             Operation(Model m, boolean withReadLock)
             {
                 model = m ; readLock = withReadLock ;
             }
            
             @Override
            public void run()
             {
                 for ( int i = 0 ; i < 2 ; i++ )
                 {
                     try {
                         model.enterCriticalSection(readLock) ;
                         if ( readLock )
                             readOperation(false) ;
                         else
                             writeOperation(false) ;
                     } finally { model.leaveCriticalSection() ; }
                 }            
             }
        }
        // Operations ----------------------------------------------
    
        volatile int writers = 0 ; 
    
        // The example model operations
        void doStuff(String label, boolean doThrow)
        {
            String id = Thread.currentThread().getName() ;
            // Puase a while to cause other threads to (try to) enter the region.
            try { Thread.sleep(SLEEP) ; } catch (InterruptedException intEx){}
            if ( doThrow )
                throw new RuntimeException(label) ;
        }
    
        // Example operations
    
        public void readOperation(boolean doThrow)
        {
            if ( writers > 0 )
                System.err.println("Concurrency error: writers around!") ;
            doStuff("read operation", false) ;
            if ( writers > 0 )
                System.err.println("Concurrency error: writers around!") ;
        }

        public void writeOperation(boolean doThrow)
        {
            writers++ ;
            doStuff("write operation", false) ;
            writers-- ;

        }
    }
}
