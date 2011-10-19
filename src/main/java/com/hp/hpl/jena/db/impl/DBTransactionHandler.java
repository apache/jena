/*
  (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: DBTransactionHandler.java,v 1.1 2009-06-29 08:55:37 castagna Exp $
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

/*
    (c) Copyright 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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