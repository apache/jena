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

package com.hp.hpl.jena.tdb.transaction;

public class TransactionInfo implements TransactionInfoMBean {

	private TransactionManager transactionManager ;
	
	public TransactionInfo ( TransactionManager transactionManager ) {
		this.transactionManager = transactionManager ;		
	}
	
	@Override
	public long getTransactionCount() {
		return getReadTransactionCount() + getWriteTransactionCount() ;
	}

	@Override
	public long getReadTransactionCount() {
		return transactionManager.finishedReaders.get() ;
	}

	@Override
	public long getWriteTransactionCount() {
		return getWriteCommitTransactionCount() + getWriteAbortTransactionCount() ;
	}

	@Override
	public long getWriteAbortTransactionCount() {
		return transactionManager.abortedWriters.get() ;
	}

	@Override
	public long getWriteCommitTransactionCount() {
		return transactionManager.committedWriters.get() ;
	}

	@Override
	public long getWriteCommitTransactionPendingCount() {
		return transactionManager.commitedAwaitingFlush.size() ;
	}

	@Override
	public long getCurrentWriteTransactionCount() {
		return transactionManager.activeWriters.get() ;
	}

	@Override
	public long getCurrentReadTransactionCount() {
		return transactionManager.activeReaders.get() ;
	}

}
