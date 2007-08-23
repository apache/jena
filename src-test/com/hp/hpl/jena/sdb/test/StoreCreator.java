/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.test;

import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.StoreDesc;
import com.hp.hpl.jena.sdb.layout2.StoreBase;
import com.hp.hpl.jena.sdb.layout2.hash.StoreTriplesNodesHashDerby;
import com.hp.hpl.jena.sdb.layout2.hash.StoreTriplesNodesHashHSQL;
import com.hp.hpl.jena.sdb.layout2.hash.StoreTriplesNodesHashMySQL;
import com.hp.hpl.jena.sdb.layout2.hash.StoreTriplesNodesHashOracle;
import com.hp.hpl.jena.sdb.layout2.hash.StoreTriplesNodesHashPGSQL;
import com.hp.hpl.jena.sdb.layout2.hash.StoreTriplesNodesHashSQLServer;
import com.hp.hpl.jena.sdb.layout2.index.StoreTriplesNodesIndexDerby;
import com.hp.hpl.jena.sdb.layout2.index.StoreTriplesNodesIndexHSQL;
import com.hp.hpl.jena.sdb.layout2.index.StoreTriplesNodesIndexMySQL;
import com.hp.hpl.jena.sdb.layout2.index.StoreTriplesNodesIndexOracle;
import com.hp.hpl.jena.sdb.layout2.index.StoreTriplesNodesIndexPGSQL;
import com.hp.hpl.jena.sdb.layout2.index.StoreTriplesNodesIndexSQLServer;
import com.hp.hpl.jena.sdb.sql.JDBC;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.store.DatabaseType;
import com.hp.hpl.jena.sdb.store.LayoutType;

/**
 * A cheap (but not cheerful) class to give access to empty models,
 * sharing stores in the background. 
 * 
 * @author Damian Steer
 *
 */
public class StoreCreator {
	
	private static StoreTriplesNodesHashPGSQL sdbpgh;
	private static StoreTriplesNodesIndexPGSQL sdbpgi;
	private static StoreTriplesNodesHashMySQL sdbmsh;
	private static StoreTriplesNodesIndexMySQL sdbmsi;
	private static StoreTriplesNodesIndexSQLServer sdbssi;
	private static StoreTriplesNodesHashSQLServer sdbssh;
	private static StoreTriplesNodesIndexHSQL sdbhsi;
	private static StoreTriplesNodesHashHSQL sdbhsh;
	private static StoreTriplesNodesHashDerby sdbdh;
	private static StoreTriplesNodesIndexDerby sdbdi;
	private static StoreBase sdboh;
	private static StoreTriplesNodesIndexOracle sdboi;

	public static Store getIndexMySQL() {
		if (sdbmsi == null) {
			JDBC.loadDriverMySQL();
			
			SDBConnection sdb = SDBFactory.createConnection(
				"jdbc:mysql://localhost/sdb_test", "jena", "swara");
			
			StoreDesc desc = new StoreDesc(LayoutType.LayoutTripleNodesIndex, DatabaseType.MySQL) ;
			sdbmsi = new StoreTriplesNodesIndexMySQL(sdb, desc);
			
			sdbmsi.getTableFormatter().format();
		}
		
		sdbmsi.getTableFormatter().truncate();
		
		return sdbmsi;
	}
	
	public static Store getHashMySQL() {
		if (sdbmsh == null) {
			JDBC.loadDriverMySQL();
			
			SDBConnection sdb = SDBFactory.createConnection(
				"jdbc:mysql://localhost/sdb_test", "jena", "swara");
		
			StoreDesc desc = new StoreDesc(LayoutType.LayoutTripleNodesHash, DatabaseType.MySQL) ;
			sdbmsh = new StoreTriplesNodesHashMySQL(sdb, desc);
			
			sdbmsh.getTableFormatter().format();
		}
		
		sdbmsh.getTableFormatter().truncate();
		
		return sdbmsh;
	}
	
	public static Store getIndexHSQL() {
		if (sdbhsi == null) {
			JDBC.loadDriverHSQL();

			SDBConnection sdb = SDBFactory.createConnection(
					"jdbc:hsqldb:mem:aname", "sa", "");

			StoreDesc desc = new StoreDesc(LayoutType.LayoutTripleNodesIndex, DatabaseType.HSQLDB) ;
			sdbhsi = new StoreTriplesNodesIndexHSQL(sdb, desc);

			sdbhsi.getTableFormatter().format();
		}
		
		sdbhsi.getTableFormatter().truncate();
		
		return sdbhsi;
	}
	
	public static Store getHashHSQL() {
		if (sdbhsh == null) {
			JDBC.loadDriverHSQL();

			SDBConnection sdb = SDBFactory.createConnection(
					"jdbc:hsqldb:mem:bname", "sa", "");

            StoreDesc desc = new StoreDesc(LayoutType.LayoutTripleNodesHash, DatabaseType.HSQLDB) ;
			sdbhsh = new StoreTriplesNodesHashHSQL(sdb, desc);

			sdbhsh.getTableFormatter().format();
		}
		
		sdbhsh.getTableFormatter().truncate();
		
		return sdbhsh;
	}
	
	public static Store getIndexPgSQL() {
		if (sdbpgi == null) {
			JDBC.loadDriverPGSQL();
			SDBConnection sdb = SDBFactory.createConnection(
				"jdbc:postgresql://localhost/sdb_test", "jena", "swara");
            StoreDesc desc = new StoreDesc(LayoutType.LayoutTripleNodesIndex, DatabaseType.PostgreSQL) ;
			sdbpgi = new StoreTriplesNodesIndexPGSQL(sdb, desc);
			sdbpgi.getTableFormatter().format();
		}
		
		sdbpgi.getTableFormatter().truncate();
			
		return sdbpgi;
	}
	
	public static Store getHashPgSQL() {
		if (sdbpgh == null) {
			JDBC.loadDriverPGSQL();
			SDBConnection sdb = SDBFactory.createConnection(
				"jdbc:postgresql://localhost/sdb_test", "jena", "swara");
            StoreDesc desc = new StoreDesc(LayoutType.LayoutTripleNodesHash, DatabaseType.PostgreSQL) ;
			sdbpgh = new StoreTriplesNodesHashPGSQL(sdb, desc);
			sdbpgh.getTableFormatter().format();
		}
		
		sdbpgh.getTableFormatter().truncate();
			
		return sdbpgh;
	}
	
	public static Store getIndexSQLServer() {
		if (sdbssi == null) {
			JDBC.loadDriverSQLServer();

			SDBConnection sdb = SDBFactory.createConnection(
					"jdbc:sqlserver://localhost;databaseName=SWEB", "jena", "@ld1s1774");

            StoreDesc desc = new StoreDesc(LayoutType.LayoutTripleNodesIndex, DatabaseType.SQLServer) ;
			sdbssi = new StoreTriplesNodesIndexSQLServer(sdb, desc);
			sdbssi.getTableFormatter().format();
		}
		
		sdbssi.getTableFormatter().format();
		
		return sdbssi;
	}
	
	public static Store getHashSQLServer() {
		if (sdbssh == null) {
			JDBC.loadDriverSQLServer();

			SDBConnection sdb = SDBFactory.createConnection(
					"jdbc:sqlserver://localhost;databaseName=SWEB", "jena", "@ld1s1774");
			
            StoreDesc desc = new StoreDesc(LayoutType.LayoutTripleNodesHash, DatabaseType.SQLServer) ;
			sdbssh = new StoreTriplesNodesHashSQLServer(sdb, desc);
			sdbssh.getTableFormatter().format();
		}

		sdbssh.getTableFormatter().format();
		
		return sdbssh;
	}
	
	public static Store getHashDerby() {
		if (sdbdh == null) {
			JDBC.loadDriverDerby() ;
			
			String url = JDBC.makeURL("derby", "localhost", "DB/test2") ;
			
			SDBConnection sdb = new SDBConnection(url, null, null) ;
            StoreDesc desc = new StoreDesc(LayoutType.LayoutTripleNodesHash, DatabaseType.Derby) ;

			sdbdh = new StoreTriplesNodesHashDerby(sdb, desc);
			
			sdbdh.getTableFormatter().format();
		}
		
		sdbdh.getTableFormatter().truncate();
			
		return sdbdh;
	}
	
	public static Store getIndexDerby() {
		if (sdbdi == null) {
			JDBC.loadDriverDerby() ;
			
			String url = JDBC.makeURL("derby", "localhost", "DB/test2") ;
			
			SDBConnection sdb = new SDBConnection(url, null, null) ;
			
            StoreDesc desc = new StoreDesc(LayoutType.LayoutTripleNodesIndex, DatabaseType.Derby) ;
			sdbdi = new StoreTriplesNodesIndexDerby(sdb, desc);
			
			sdbdi.getTableFormatter().format();
		}
		
		sdbdi.getTableFormatter().truncate();
			
		return sdbdi;
	}
	
	public static Store getHashOracle() {
		if (sdboh == null) {
			JDBC.loadDriverOracle() ;
			
			String url = JDBC.makeURL("oracle:thin", "localhost:1521", "XE") ;
			
			SDBConnection sdb = new SDBConnection(url, "jena", "swara") ;
			
            StoreDesc desc = new StoreDesc(LayoutType.LayoutTripleNodesHash, DatabaseType.Oracle) ;
			sdboh = new StoreTriplesNodesHashOracle(sdb, desc);
			
			sdboh.getTableFormatter().format();
		}
		
		sdboh.getTableFormatter().truncate();
			
		return sdboh;
	}
	
	public static Store getIndexOracle() {
		if (sdboi == null) {
			JDBC.loadDriverOracle() ;
			
			String url = JDBC.makeURL("oracle:thin", "localhost:1521", "XE") ;
			
			SDBConnection sdb = new SDBConnection(url, "jena", "swara") ;
			
            StoreDesc desc = new StoreDesc(LayoutType.LayoutTripleNodesIndex, DatabaseType.Oracle) ;
			sdboi = new StoreTriplesNodesIndexOracle(sdb, desc);
			
			sdboi.getTableFormatter().format();
		}
		
		sdboi.getTableFormatter().truncate();
			
		return sdboi;
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
