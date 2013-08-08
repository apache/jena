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

package com.hp.hpl.jena.sdb.test;

import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.StoreDesc;
import com.hp.hpl.jena.sdb.layout2.hash.StoreTriplesNodesHashDB2;
import com.hp.hpl.jena.sdb.layout2.hash.StoreTriplesNodesHashDerby;
import com.hp.hpl.jena.sdb.layout2.hash.StoreTriplesNodesHashHSQL;
import com.hp.hpl.jena.sdb.layout2.hash.StoreTriplesNodesHashMySQL;
import com.hp.hpl.jena.sdb.layout2.hash.StoreTriplesNodesHashOracle;
import com.hp.hpl.jena.sdb.layout2.hash.StoreTriplesNodesHashPGSQL;
import com.hp.hpl.jena.sdb.layout2.hash.StoreTriplesNodesHashSAP;
import com.hp.hpl.jena.sdb.layout2.hash.StoreTriplesNodesHashSQLServer;
import com.hp.hpl.jena.sdb.layout2.index.StoreTriplesNodesIndexDB2;
import com.hp.hpl.jena.sdb.layout2.index.StoreTriplesNodesIndexDerby;
import com.hp.hpl.jena.sdb.layout2.index.StoreTriplesNodesIndexHSQL;
import com.hp.hpl.jena.sdb.layout2.index.StoreTriplesNodesIndexMySQL;
import com.hp.hpl.jena.sdb.layout2.index.StoreTriplesNodesIndexOracle;
import com.hp.hpl.jena.sdb.layout2.index.StoreTriplesNodesIndexPGSQL;
import com.hp.hpl.jena.sdb.layout2.index.StoreTriplesNodesIndexSAP;
import com.hp.hpl.jena.sdb.layout2.index.StoreTriplesNodesIndexSQLServer;
import com.hp.hpl.jena.sdb.sql.JDBC;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.store.DatabaseType;
import com.hp.hpl.jena.sdb.store.LayoutType;

/**
 * A cheap (but not cheerful) class to give access to empty models,
 * sharing stores in the background. 
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
	private static StoreTriplesNodesHashOracle sdboh;
	private static StoreTriplesNodesIndexOracle sdboi;
	private static StoreTriplesNodesIndexDB2 sdbdb2i;
	private static StoreTriplesNodesHashDB2 sdbdb2h;
	private static StoreTriplesNodesHashSAP sdbsaph;
	private static StoreTriplesNodesIndexSAP sdbsapi;

	public static Store getIndexMySQL() {
		if (sdbmsi == null) {
			JDBC.loadDriverMySQL();
			
			SDBConnection sdb = SDBFactory.createConnection(
                // "sdb_test", "jena", "swara"
				"jdbc:mysql://localhost/test2-index", "user", "password");
			
			StoreDesc desc = new StoreDesc(LayoutType.LayoutTripleNodesIndex, DatabaseType.MySQL) ;
			sdbmsi = new StoreTriplesNodesIndexMySQL(sdb, desc);
			
			sdbmsi.getTableFormatter().create();
		}
		else
		    sdbmsi.getTableFormatter().truncate();
		
		return sdbmsi;
	}
	
	public static Store getHashMySQL() {
		if (sdbmsh == null) {
			JDBC.loadDriverMySQL();
			
			SDBConnection sdb = SDBFactory.createConnection(
				"jdbc:mysql://localhost/test2-hash", "user", "password");
		
			StoreDesc desc = new StoreDesc(LayoutType.LayoutTripleNodesHash, DatabaseType.MySQL) ;
			sdbmsh = new StoreTriplesNodesHashMySQL(sdb, desc);
			
			sdbmsh.getTableFormatter().create();
		}
		else
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

			sdbhsi.getTableFormatter().create();
		}
		else
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
			sdbhsh.getTableFormatter().create();
		}
		else
		    sdbhsh.getTableFormatter().truncate();
		
		return sdbhsh;
	}
	
	public static Store getIndexPgSQL() {
		if (sdbpgi == null) {
			JDBC.loadDriverPGSQL();
			SDBConnection sdb = SDBFactory.createConnection(
				"jdbc:postgresql://localhost/test2-index", "user", "password");
            StoreDesc desc = new StoreDesc(LayoutType.LayoutTripleNodesIndex, DatabaseType.PostgreSQL) ;
			sdbpgi = new StoreTriplesNodesIndexPGSQL(sdb, desc);
			sdbpgi.getTableFormatter().create() ;
		}
		else
		    sdbpgi.getTableFormatter().truncate();
			
		return sdbpgi;
	}
	
	public static Store getHashPgSQL() {
		if (sdbpgh == null) {
			JDBC.loadDriverPGSQL();
			SDBConnection sdb = SDBFactory.createConnection(
				"jdbc:postgresql://localhost/test2-hash", "user", "password");
            StoreDesc desc = new StoreDesc(LayoutType.LayoutTripleNodesHash, DatabaseType.PostgreSQL) ;
			sdbpgh = new StoreTriplesNodesHashPGSQL(sdb, desc);
			sdbpgh.getTableFormatter().create();
		}
		else
		    sdbpgh.getTableFormatter().truncate();
			
		return sdbpgh;
	}
	
	// MS SQL express : jdbc:sqlserver://localhost\\SQLEXPRESS;databaseName=sdbtest"
	// user / password
	// MS SQL server: jdbc:sqlserver://localhost;databaseName=SWEB
	// "jena" / "@ld1s1774"
	
	
	private static final String MSSQL_url = "jdbc:sqlserver://localhost\\SQLEXPRESS;databaseName=" ;
	private static final String MSSQL_user = "user" ;
	private static final String MSSQL_password = "password" ;
	
	public static Store getIndexSQLServer() {
		if (sdbssi == null) {
			JDBC.loadDriverSQLServer();

			SDBConnection sdb = SDBFactory.createConnection(MSSQL_url+"test2-index", MSSQL_user, MSSQL_password) ;

            StoreDesc desc = new StoreDesc(LayoutType.LayoutTripleNodesIndex, DatabaseType.SQLServer) ;
			sdbssi = new StoreTriplesNodesIndexSQLServer(sdb, desc);
			sdbssi.getTableFormatter().create();
		}
		else
		    sdbssi.getTableFormatter().truncate();
		
		return sdbssi;
	}
	
	public static Store getHashSQLServer() {
		if (sdbssh == null) {
			JDBC.loadDriverSQLServer();

            SDBConnection sdb = SDBFactory.createConnection(MSSQL_url+"test2-hash", MSSQL_user, MSSQL_password) ;
			
            StoreDesc desc = new StoreDesc(LayoutType.LayoutTripleNodesHash, DatabaseType.SQLServer) ;
			sdbssh = new StoreTriplesNodesHashSQLServer(sdb, desc);
			sdbssh.getTableFormatter().create();
		}
		else
		    sdbssh.getTableFormatter().truncate();
		
		return sdbssh;
	}
	
	public static Store getHashDerby() {
		if (sdbdh == null) {
			JDBC.loadDriverDerby() ;
			
			String url = JDBC.makeURL("derby", "localhost", "DB/test2-hash") ;
			
			SDBConnection sdb = new SDBConnection(url, null, null) ;
            StoreDesc desc = new StoreDesc(LayoutType.LayoutTripleNodesHash, DatabaseType.Derby) ;

			sdbdh = new StoreTriplesNodesHashDerby(sdb, desc);
			sdbdh.getTableFormatter().create();
		}
		else
		    sdbdh.getTableFormatter().truncate();
			
		return sdbdh;
	}
	
	public static Store getIndexDerby() {
		if (sdbdi == null) {
			JDBC.loadDriverDerby() ;
			
			String url = JDBC.makeURL("derby", "localhost", "DB/test2-index") ;
			
			SDBConnection sdb = new SDBConnection(url, null, null) ;
			
            StoreDesc desc = new StoreDesc(LayoutType.LayoutTripleNodesIndex, DatabaseType.Derby) ;
			sdbdi = new StoreTriplesNodesIndexDerby(sdb, desc);
			sdbdi.getTableFormatter().create();
		}
		else
		    sdbdi.getTableFormatter().truncate();
			
		return sdbdi;
	}
	
	public static Store getHashOracle() {
		if (sdboh == null) {
			JDBC.loadDriverOracle() ;
			
			// "jena", "swara"
			String url = JDBC.makeURL("oracle:thin", "localhost:1521", "XE") ;
			
			SDBConnection sdb = new SDBConnection(url, "test2-hash", "test2-hash") ;
			
            StoreDesc desc = new StoreDesc(LayoutType.LayoutTripleNodesHash, DatabaseType.Oracle) ;
			sdboh = new StoreTriplesNodesHashOracle(sdb, desc);
			sdboh.getTableFormatter().create();
		}
		else
		    sdboh.getTableFormatter().truncate();
			
		return sdboh;
	}
	
	public static Store getIndexOracle() {
		if (sdboi == null) {
			JDBC.loadDriverOracle() ;
			
			String url = JDBC.makeURL("oracle:thin", "localhost:1521", "XE") ;
			
			SDBConnection sdb = new SDBConnection(url, "test2-index", "test2-index") ;
			
            StoreDesc desc = new StoreDesc(LayoutType.LayoutTripleNodesIndex, DatabaseType.Oracle) ;
			sdboi = new StoreTriplesNodesIndexOracle(sdb, desc);
			sdboi.getTableFormatter().create();
		}
		else
		    sdboi.getTableFormatter().truncate();
			
		return sdboi;
	}
	
	public static Store getHashDB2() {
		if (sdbdb2h == null) {
			JDBC.loadDriverDB2() ;
			
			String url = JDBC.makeURL("db2", "sweb-sdb-4:50000", "TEST2H") ;
			
			SDBConnection sdb = new SDBConnection(url, "user", "password") ;
			
            StoreDesc desc = new StoreDesc(LayoutType.LayoutTripleNodesHash, DatabaseType.DB2) ;
			sdbdb2h = new StoreTriplesNodesHashDB2(sdb, desc);
			sdbdb2h.getTableFormatter().create();
		}
		else
		    sdbdb2h.getTableFormatter().truncate();
			
		return sdbdb2h;
	}
	
	public static Store getIndexDB2() {
		if (sdbdb2i == null) {
			JDBC.loadDriverDB2() ;
			
			String url = JDBC.makeURL("db2", "sweb-sdb-4:50000", "TEST2I") ;
			
			SDBConnection sdb = new SDBConnection(url, "user", "password") ;
			
            StoreDesc desc = new StoreDesc(LayoutType.LayoutTripleNodesIndex, DatabaseType.DB2) ;
			sdbdb2i = new StoreTriplesNodesIndexDB2(sdb, desc);
			sdbdb2i.getTableFormatter().create();
		}
		
		sdbdb2i.getTableFormatter().truncate();
			
		return sdbdb2i;
	}
	
	public static Store getIndexSAP() {
		if (sdbsapi == null) {
			JDBC.loadDriverSAP();
			SDBConnection sdb = SDBFactory.createConnection(
				"jdbc:sap://localhost/test2-index", "user", "password");
            StoreDesc desc = new StoreDesc(LayoutType.LayoutTripleNodesIndex, DatabaseType.SAP) ;
			sdbsapi = new StoreTriplesNodesIndexSAP(sdb, desc);
			sdbsapi.getTableFormatter().create() ;
		}
		else
		    sdbsapi.getTableFormatter().truncate();
			
		return sdbsapi;
	}
	
	public static Store getHashSAP() {
		if (sdbsaph == null) {
			JDBC.loadDriverSAP();
			SDBConnection sdb = SDBFactory.createConnection(
				"jdbc:sap://localhost/test2-hash", "user", "password");
            StoreDesc desc = new StoreDesc(LayoutType.LayoutTripleNodesHash, DatabaseType.SAP) ;
			sdbsaph = new StoreTriplesNodesHashSAP(sdb, desc);
			sdbsaph.getTableFormatter().create();
		}
		else
		    sdbsaph.getTableFormatter().truncate();
			
		return sdbsaph;
	}
}
