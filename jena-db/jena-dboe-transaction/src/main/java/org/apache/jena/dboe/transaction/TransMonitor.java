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

package org.apache.jena.dboe.transaction;

import java.io.PrintStream ;
import java.nio.ByteBuffer ;
import java.util.LinkedHashMap ;
import java.util.Map ;
import java.util.concurrent.atomic.LongAdder ;
import java.util.stream.Collectors ;

import org.apache.jena.dboe.transaction.txn.ComponentId;
import org.apache.jena.dboe.transaction.txn.SysTransState;
import org.apache.jena.dboe.transaction.txn.Transaction;
import org.apache.jena.dboe.transaction.txn.TransactionalComponent;

/** This class is stateless in the transaction but it records counts of
 * every {@link TransactionalComponent} operations.
 * For each operation called "ABC" there is a member field "counterABC".  
 */
public class TransMonitor implements TransactionalComponent {
    
    // LongAdder for stats (better from mostly write, not consistent read)
    
    /** Counters, in initialization order */
    private Map<String, LongAdder> counters = new LinkedHashMap<>() ;
    private final ComponentId componentId ;
    
    private LongAdder allocCounter(String string) {
        LongAdder counter = new LongAdder() ;
        counters.put(string, counter) ;
        return counter ;
    }
    public TransMonitor(ComponentId cid) { 
        this.componentId = cid ;
    }
    
    /** Reset all counters to zero */
    public void reset() {
        counters.forEach( (s,c) -> c.reset()) ; 
    }

    /** Get a copy of the counters with current values.
     * The values are as of the point of this being called and are not changed
     * by any later calls to monitored operations.  
     */
    public Map<String, Long> getAll() {
        return counters.entrySet().stream()
            .collect(Collectors.toMap(
                                      e -> e.getKey(),
                                      e -> e.getValue().sum()
                ));
    }
    
    /** Print the counters state. */
    public void print() {
        print(System.out) ;
    }

    /** Print the counters state. */
    public void print(PrintStream ps) {
        ps.println("Transaction Counters:") ;
        counters.forEach( (s,c) -> {
            ps.printf("   %-15s %4d\n", s, c.longValue()) ;
        }) ;
    }

    public LongAdder counterGetComponentId = allocCounter("getComponentId") ;

    @Override
    public ComponentId getComponentId() {
        counterGetComponentId.increment() ;
        return componentId ;
    }

    public LongAdder counterStartRecovery = allocCounter("startRecovery") ;

    @Override
    public void startRecovery() {
        counterStartRecovery.increment() ;
    }

    public LongAdder counterRecover = allocCounter("recover") ;

    @Override
    public void recover(ByteBuffer ref) {
        counterRecover.increment() ;
    }

    public LongAdder counterFinishRecovery = allocCounter("finishRecovery") ;

    @Override
    public void finishRecovery() {
        counterFinishRecovery.increment() ;
    }
    
    public LongAdder counterCleanStart = allocCounter("finishRecovery") ;
    
    @Override
    public void cleanStart() {
        counterCleanStart.increment() ;
    }
    
    public LongAdder counterBegin = allocCounter("begin") ;

    @Override
    public void begin(Transaction transaction) {
        counterBegin.increment() ;
    }
    
    public LongAdder counterPromote = allocCounter("promote") ;
    
    @Override
    public boolean promote(Transaction transaction) {
        counterPromote.increment() ;
        return true ;
    }

    public LongAdder counterCommitPrepare = allocCounter("commitPrepare") ;

    @Override
    public ByteBuffer commitPrepare(Transaction transaction) {
        counterCommitPrepare.increment() ;
        return null ;
    }

    public LongAdder counterCommit = allocCounter("commit") ;

    @Override
    public void commit(Transaction transaction) {
        counterCommit.increment() ;
    }

    public LongAdder counterCommitEnd = allocCounter("commitEnd") ;

    @Override
    public void commitEnd(Transaction transaction) {
        counterCommitEnd.increment() ;
    }

    public LongAdder counterAbort = allocCounter("abort") ;

    @Override
    public void abort(Transaction transaction) {
        counterAbort.increment() ;
    }

    public LongAdder counterComplete = allocCounter("complete") ;

    @Override
    public void complete(Transaction transaction) {
        counterComplete.increment() ;
    }

    public LongAdder counterDetach = allocCounter("detach") ;
    
    @Override
    public SysTransState detach() {
        counterDetach.increment() ;
        return null ;
    }

    public LongAdder counterAttach = allocCounter("attach") ;

    @Override
    public void attach(SysTransState systemState) {
        counterAttach.increment() ;
    }
    
    public LongAdder counterShutdown = allocCounter("shutdown") ;

    @Override
    public void shutdown() {
        counterShutdown.increment() ;
    }
}
