/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: DBTransactionHandler.java,v 1.1 2003-05-02 03:16:36 csayers Exp $
*/

package com.hp.hpl.jena.db.impl;

import com.hp.hpl.jena.db.GraphRDB;
import com.hp.hpl.jena.graph.TransactionHandler;

/**
 * Transaction handler for graphs backed by a database.
 *
 * @author csayers based on SimpleTransactionHandler by kers 
 * @version $Revision: 1.1 $
 */
public class DBTransactionHandler implements TransactionHandler {
	private IRDBDriver m_driver = null;
	private GraphRDB m_graphRDB = null;

	public DBTransactionHandler(IRDBDriver driver, GraphRDB graphRDB) {
		super();
		m_driver = driver;
		m_graphRDB = graphRDB; // suspect we may need this later
	}

	public boolean transactionsSupported() {
		return false; // TODO change to m_driver.transactionsSupported();
	}

	public void begin() {
		notSupported(); // TODO change to m_driver.begin(); 
	}

	public void abort() {
		notSupported(); // TODO change to m_driver.abort();
	}

	public void commit() {
		notSupported(); // TODO change to m_driver.commit();
	}

	private void notSupported() {
		throw new UnsupportedOperationException("sorry, transactions are not supported yet.");
	}
}

/*
    (c) Copyright Hewlett-Packard Company 2003
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/