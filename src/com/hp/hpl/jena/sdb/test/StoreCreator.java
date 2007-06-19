/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.test;

import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.layout2.hash.StoreTriplesNodesHashDerby;
import com.hp.hpl.jena.sdb.layout2.hash.StoreTriplesNodesHashHSQL;
import com.hp.hpl.jena.sdb.layout2.hash.StoreTriplesNodesHashMySQL;
import com.hp.hpl.jena.sdb.layout2.hash.StoreTriplesNodesHashPGSQL;
import com.hp.hpl.jena.sdb.layout2.hash.StoreTriplesNodesHashSQLServer;
import com.hp.hpl.jena.sdb.layout2.index.StoreTriplesNodesIndexDerby;
import com.hp.hpl.jena.sdb.layout2.index.StoreTriplesNodesIndexHSQL;
import com.hp.hpl.jena.sdb.layout2.index.StoreTriplesNodesIndexMySQL;
import com.hp.hpl.jena.sdb.layout2.index.StoreTriplesNodesIndexPGSQL;
import com.hp.hpl.jena.sdb.layout2.index.StoreTriplesNodesIndexSQLServer;
import com.hp.hpl.jena.sdb.sql.JDBC;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.store.Store;

/**
 * A cheap (but not cheerful) class to give access to empty models,
 * sharing stores in the background. 
 * 
 * @author Damian Steer
 *
 */
public class StoreCreator {
	
	public static Store getIndexMySQL() {
		JDBC.loadDriverMySQL();
		
		SDBConnection sdb = SDBFactory.createConnection(
					"jdbc:mysql://localhost/sdb_test", "jena", "swara");
		
		Store store = new StoreTriplesNodesIndexMySQL(sdb);
		
		store.getTableFormatter().format();
		
		return store;
	}
	
	public static Store getHashMySQL() {
		JDBC.loadDriverMySQL();

		SDBConnection sdb = SDBFactory.createConnection(
				"jdbc:mysql://localhost/sdb_test", "jena", "swara");
		
		Store store = new StoreTriplesNodesHashMySQL(sdb);

		store.getTableFormatter().format();
		
		return store;
	}
	
	public static Store getIndexHSQL() {
		JDBC.loadDriverHSQL();

		SDBConnection sdb = SDBFactory.createConnection(
				"jdbc:hsqldb:mem:aname", "sa", "");

		Store store = new StoreTriplesNodesIndexHSQL(sdb);

		store.getTableFormatter().format();
		
		return store;
	}
	
	public static Store getHashHSQL() {
		JDBC.loadDriverHSQL();

		SDBConnection sdb = SDBFactory.createConnection(
				"jdbc:hsqldb:mem:bname", "sa", "");

		Store store = new StoreTriplesNodesHashHSQL(sdb);

		store.getTableFormatter().format();

		return store;
	}
	
	public static Store getIndexPgSQL() {
		JDBC.loadDriverPGSQL();

		SDBConnection sdb = SDBFactory.createConnection(
				"jdbc:postgresql://localhost/sdb_test", "jena", "swara");
		
		Store store = new StoreTriplesNodesIndexPGSQL(sdb);
		
		store.getTableFormatter().format();
			
		return store;
	}
	
	public static Store getHashPgSQL() {
		JDBC.loadDriverPGSQL();
		
		SDBConnection sdb = SDBFactory.createConnection(
				"jdbc:postgresql://localhost/sdb_test", "jena", "swara");
		
		Store store = new StoreTriplesNodesHashPGSQL(sdb);
		
		store.getTableFormatter().format();
			
		return store;
	}
	
	public static Store getIndexSQLServer() {
		JDBC.loadDriverSQLServer();

		SDBConnection sdb = SDBFactory.createConnection(
				"jdbc:sqlserver://localhost;databaseName=sdb2", "mtx", "mtx01");
		
		Store store = new StoreTriplesNodesIndexSQLServer(sdb);
		
		store.getTableFormatter().format();
		
		return store;
	}
	
	public static Store getHashSQLServer() {
		JDBC.loadDriverSQLServer();

		SDBConnection sdb = SDBFactory.createConnection(
					"jdbc:sqlserver://localhost;databaseName=sdb2", "mtx", "mtx01");

		Store store = new StoreTriplesNodesHashSQLServer(sdb);

		store.getTableFormatter().format();
		
		return store;
	}
	
	public static Store getHashDerby() {
		JDBC.loadDriverDerby() ;
		
		String url = JDBC.makeURL("derby", "localhost", "DB/test2") ;
		
		SDBConnection sdb = new SDBConnection(url, null, null) ;
        
		Store store = new StoreTriplesNodesHashDerby(sdb);
			
		store.getTableFormatter().format();
			
		return store;
	}
	
	public static Store getIndexDerby() {
		JDBC.loadDriverDerby() ;
		
		String url = JDBC.makeURL("derby", "localhost", "DB/test3") ;
		
		SDBConnection sdb = new SDBConnection(url, null, null) ;
        
		Store store = new StoreTriplesNodesIndexDerby(sdb);
			
		store.getTableFormatter().format();
			
		return store;
	}
}

/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
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