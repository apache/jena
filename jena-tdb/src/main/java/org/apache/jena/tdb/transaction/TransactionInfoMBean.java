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

package org.apache.jena.tdb.transaction;

/** Interface to stats for a single Transaction Manager */ 
public interface TransactionInfoMBean
{
    /** Number of transactions executed */
    long getTransactionCount() ; 
    
    /** Number of read transactions executed */
    long getReadTransactionCount() ; 

    /** Number of write transactions executed */
    long getWriteTransactionCount() ; 

    /** Number of write transactions that aborted */
    long getWriteAbortTransactionCount() ; 

    /** Number of write transactions that committed */
    long getWriteCommitTransactionCount() ; 

    /** Number of write transactions that have committed but are not written to the base database */
    long getWriteCommitTransactionPendingCount() ; 

    /** Number of write transactions executing */
    long getCurrentWriteTransactionCount() ; 

    /** Number of read transactions executing */
    long getCurrentReadTransactionCount() ; 
}
