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

package com.hp.hpl.jena.db.impl;

import com.hp.hpl.jena.db.GraphRDB;
import com.hp.hpl.jena.graph.impl.*;

/**
 * Transaction handler for graphs backed by a database.
 *
 * @author csayers based on SimpleTransactionHandler by kers 
 * @version $Revision: 1.1 $
 */
public class DBTransactionHandler extends TransactionHandlerBase {
	private IRDBDriver m_driver = null;
    
	/**
	 * Construct a transaction handler for the database.
	 * 
	 * @param driver the datatabase-specific IRDBDriver.
	 * @param graphRDB the specific GraphRDB for which the transaction applies.
	 * This is not currently needed - included for future use.
	 */
	public DBTransactionHandler(IRDBDriver driver, GraphRDB graphRDB ) {
		super();
		m_driver = driver;
	}

	@Override
    public boolean transactionsSupported() {
		return m_driver.transactionsSupported();
	}

	@Override
    public void begin() {
		m_driver.begin(); 
	}

	@Override
    public void abort() {
		m_driver.abort();
	}

	@Override
    public void commit() {
		m_driver.commit();
	}

}
