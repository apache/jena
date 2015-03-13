/**
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.seaborne.dboe.transaction;

import java.io.PrintStream ;
import java.nio.ByteBuffer ;
import java.util.ArrayList ;
import java.util.List ;
import java.util.concurrent.atomic.AtomicLong ;
import java.util.stream.Collectors ;

import org.apache.jena.atlas.lib.Pair ;
import org.seaborne.dboe.transaction.txn.* ;

/** This class is stateless in the transaction but it records counts of
 * every {@link TransactionalComponent} operations.
 * For each operation called "ABC" there is a member field "counterABC".  
 */
public class TransMonitor implements TransactionalComponent {
    
    /** Counters, in initialization order */
    private List<Pair<String, AtomicLong>> counters = new ArrayList<>() ; 
    private AtomicLong allocCounter(String string) {
        AtomicLong counter = new AtomicLong(0) ;
        counters.add(Pair.create(string, counter)) ;
        return counter ;
    }
    public TransMonitor() { }
    
    /** Reset all counters to zero */
    public void reset() {
        counters.forEach( p -> p.getRight().set(0)) ; 
    }

    /** Get a copy of the counters with current values.
     * The values are as of the point of this being called and are not changed
     * by any later calls to monitored operations.  
     */
    public List<Pair<String, Long>> getAll() {
        return counters.stream().map( p -> Pair.create(p.getLeft(), p.getRight().longValue())).collect(Collectors.toList()) ;
    }
    
    /** Print the counters state. */
    public void print() {
        print(System.out) ;
    }

    /** Print the counters state. */
    public void print(PrintStream ps) {
        ps.println("Transaction Counters:") ;
        counters.forEach( p -> {
            ps.printf("   %-15s %4d\n", p.getLeft(), p.getRight().longValue()) ;
        }) ;
    }

    public AtomicLong counterGetComponentId = allocCounter("getComponentId") ;

    @Override
    public ComponentId getComponentId() {
        counterGetComponentId.incrementAndGet() ;
        return ComponentIds.idMonitor ;
    }

    public AtomicLong counterStartRecovery = allocCounter("startRecovery") ;

    @Override
    public void startRecovery() {
        counterStartRecovery.incrementAndGet() ;
    }

    public AtomicLong counterRecover = allocCounter("recover") ;

    @Override
    public void recover(ByteBuffer ref) {
        counterRecover.incrementAndGet() ;
    }

    public AtomicLong counterFinishRecovery = allocCounter("finishRecovery") ;

    @Override
    public void finishRecovery() {
        counterFinishRecovery.incrementAndGet() ;
    }
    
    public AtomicLong counterCleanStart = allocCounter("finishRecovery") ;
    
    @Override
    public void cleanStart() {
        counterCleanStart.incrementAndGet() ;
    }
    
    public AtomicLong counterBegin = allocCounter("begin") ;

    @Override
    public void begin(Transaction transaction) {
        counterBegin.incrementAndGet() ;
    }

    public AtomicLong counterCommitPrepare = allocCounter("commitPrepare") ;

    @Override
    public ByteBuffer commitPrepare(Transaction transaction) {
        counterCommitPrepare.incrementAndGet() ;
        return null ;
    }

    public AtomicLong counterCommit = allocCounter("commit") ;

    @Override
    public void commit(Transaction transaction) {
        counterCommit.incrementAndGet() ;
    }

    public AtomicLong counterCommitEnd = allocCounter("commitEnd") ;

    @Override
    public void commitEnd(Transaction transaction) {
        counterCommitEnd.incrementAndGet() ;
    }

    public AtomicLong counterAbort = allocCounter("abort") ;

    @Override
    public void abort(Transaction transaction) {
        counterAbort.incrementAndGet() ;
    }

    public AtomicLong counterComplete = allocCounter("complete") ;

    @Override
    public void complete(Transaction transaction) {
        counterComplete.incrementAndGet() ;
    }

    public AtomicLong counterDetach = allocCounter("detach") ;
    
    @Override
    public SysTransState detach() {
        counterDetach.incrementAndGet() ;
        return null ;
    }

    public AtomicLong counterAttach = allocCounter("attach") ;

    @Override
    public void attach(SysTransState systemState) {
        counterAttach.incrementAndGet() ;
    }
    
    public AtomicLong counterShutdown = allocCounter("shutdown") ;

    @Override
    public void shutdown() {
        counterShutdown.incrementAndGet() ;
    }
}
