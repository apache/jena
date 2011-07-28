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

package com.hp.hpl.jena.tdb.transaction;

public class SysTxnState
{
    final public int activeReaders ; 
    final public int activeWriters ;
    final public int finishedReads ;
    final public int committedWrite ;
    final public int abortedWrite ;
    final public int queuedCommits ;
    
    SysTxnState(TransactionManager tm)
    {
        activeReaders = tm.activeReaders ;
        activeWriters = tm.activeWriters ;
        finishedReads = tm.finishedReads ;
        committedWrite = tm.committedWrite ;
        abortedWrite = tm.abortedWrite ;
        queuedCommits = tm.commitedAwaitingFlush.size() ;
    }
    
    @Override
    public String toString()
    {
        return String.format("Active (R=%d W=%d) : Finished (R=%d, WC=%d, WA=%d) Queue %d",
                             activeReaders,
                             activeWriters,
                             finishedReads,
                             committedWrite,
                             abortedWrite,
                             queuedCommits
        		) ;
    }
}
