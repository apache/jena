/*
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.rdf.model.test ;
import com.hp.hpl.jena.rdf.model.* ;
import com.hp.hpl.jena.rdf.model.impl.ModelLockImpl ;


import junit.framework.*;
/**
 * @author		Andy Seaborne
 * @version 	$Id: TestConcurrency.java,v 1.1 2003-04-28 14:39:47 andy_seaborne Exp $
 */
public class TestConcurrency  extends TestSuite
{

    /** Creates new RDQLTestSuite */
        static public TestSuite suite() {
            return new TestConcurrency();
        }

	// Test suite to exercise the locking
	static long SLEEP = 250 ;
	static int threadCount = 0;


    public TestConcurrency()
    {
        super("Model concurrency control") ;
        //this.addTest(new ConcurrencyTestLockPromotion("Many readers")) ;
        //this.addTest(new ConcurrencyTestLockPromotion("Many writers")) ;
        //this.addTest(new ConcurrencyTestLockPromotion("Readers and writers")) ;
        
        addTest(new Nesting("Lock nesting 1", ModelLock.READ, ModelLock.READ, false)) ;
        addTest(new Nesting("Lock nesting 2", ModelLock.WRITE, ModelLock.WRITE, false)) ;
        addTest(new Nesting("Lock nesting 3", ModelLock.READ, ModelLock.WRITE, true)) ;
        addTest(new Nesting("Lock nesting 4", ModelLock.WRITE, ModelLock.READ, false)) ;
        
        addTest(new Parallel("Parallel concurrency test")) ;

    }

    static class Nesting extends TestCase
    {
        // Note : reuse the model across tests.
        final static Model model = ModelFactory.createDefaultModel() ;
        
        boolean outerLock ;
        boolean innerLock ;
        boolean exceptionExpected ;

        Nesting(String testName,
                boolean lock1, boolean lock2, boolean exExpected)
        {
            super(testName);
            outerLock = lock1 ;
            innerLock = lock2 ;
            exceptionExpected = exExpected ;
        }

        protected void runTest() throws Throwable
        {
            boolean gotException = false ;
            try {
                model.enterCriticalSection(outerLock) ;
                
                try {
                    try {
                        // Should fail if outerLock is READ and innerLock is WRITE
                        model.enterCriticalSection(innerLock) ;
                        
                    } finally { model.leaveCriticalSection() ; }
                } catch (Exception ex)
                {
                    gotException = true ;
                }
                
            } finally { 
                model.leaveCriticalSection() ;
            }
            
            if ( exceptionExpected )
                assertTrue("Failed to get expected lock promotion error", gotException) ;
            else
                assertTrue("Got unexpected lock promotion error", !gotException) ;
        }
    }
    
    
    static class Parallel extends TestCase
    {
        int threadTotal = 8 ; 
        
        Parallel(String testName)
        {
            super(testName) ;
        }
        
        protected void runTest() throws Throwable
        {
            Model model = ModelFactory.createDefaultModel() ;
            Thread threads[] = new Thread[threadTotal] ;

            boolean getReadLock = ModelLock.READ ;
            for (int i = 0; i < threadTotal; i++)
            {
                String nextId = "T"+Integer.toString(++threadCount);
                threads[i] = new Operation(model, getReadLock) ;
                threads[i].setName(nextId) ;
                threads[i].start() ;
                
                if ( ModelLockImpl.DEBUG )
                    System.err.println("Create: "+nextId+"  ("+(getReadLock?"READ":"WRITE")+")") ;
                getReadLock = ! getReadLock ;
            }
        
            boolean problems = false ;
            for ( int i = 0; i < threadTotal; i++)
            {
                try { threads[i].join(100*SLEEP) ; } catch (InterruptedException intEx) {}
                if ( threads[i].isAlive() )
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
            
             Operation(Model m, boolean _getReadLock)
             {
                 model = m ; readLock = _getReadLock ;
             }
            
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
            //if ( ModelLockImpl.DEBUG )
            //    System.err.println(id+": "+label+" - 1 - sleep="+SLEEP) ;
            try { Thread.sleep(SLEEP) ; } catch (InterruptedException intEx){}
            if ( doThrow )
                throw new RuntimeException(label) ;
            //if ( ModelLockImpl.DEBUG )
            //    System.err.println(id+": "+label+" - 2") ;
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


/*
 *  (c) Copyright Hewlett-Packard Company 2003
 *  All rights reserved.
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
