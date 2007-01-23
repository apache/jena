/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.test;

import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.layout2.hash.StoreTriplesNodesHashDerby;
import com.hp.hpl.jena.sdb.layout2.hash.StoreTriplesNodesHashHSQL;
import com.hp.hpl.jena.sdb.layout2.hash.StoreTriplesNodesHashMySQL;
import com.hp.hpl.jena.sdb.layout2.hash.StoreTriplesNodesHashPGSQL;
import com.hp.hpl.jena.sdb.layout2.index.StoreTriplesNodesIndexDerby;
import com.hp.hpl.jena.sdb.layout2.index.StoreTriplesNodesIndexHSQL;
import com.hp.hpl.jena.sdb.layout2.index.StoreTriplesNodesIndexMySQL;
import com.hp.hpl.jena.sdb.layout2.index.StoreTriplesNodesIndexPGSQL;
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
public class ModelPool {
	protected static ModelPool instance;

	protected Map<String, Store> stores;

	protected ModelPool() {
		stores = new HashMap<String, Store>();
		JDBC.loadDriverDerby();
		JDBC.loadDriverHSQL();
		JDBC.loadDriverMySQL();
		JDBC.loadDriverPGSQL();
	}

	public static ModelPool get() {
		if (instance == null)
			instance = new ModelPool();
		return instance;
	}

	public void closeAll() {
		for (Store store : stores.values()) {
			store.getLoader().close();
			store.close();
			store.getConnection().close();
		}
		stores = new HashMap<String, Store>();
	}
	
	public Model getIndexMySQL() {
		Store store = stores.get("MYSQLINDEX");

		if (store == null) {
			JDBC.loadDriverMySQL();

			SDBConnection sdb = SDBFactory.createConnection(
					"jdbc:mysql://localhost/sdb_test", "jena", "swara");

			store = new StoreTriplesNodesIndexMySQL(sdb);

			store.getTableFormatter().format();

			stores.put("MYSQLINDEX", store);
		}
		
		Model model = SDBFactory.connectModel(store);
		model.removeAll();
		return model;
	}
	
	public Model getHashMySQL() {
		Store store = stores.get("MYSQLHASH");

		if (store == null) {
			JDBC.loadDriverMySQL();

			SDBConnection sdb = SDBFactory.createConnection(
					"jdbc:mysql://localhost/sdb_test", "jena", "swara");

			store = new StoreTriplesNodesHashMySQL(sdb);

			store.getTableFormatter().format();

			stores.put("MYSQLHASH", store);
		}
		
		Model model = SDBFactory.connectModel(store);
		model.removeAll();
		return model;
	}
	
	public Model getIndexHSQL() {
		Store store = stores.get("HSQLINDEX");

		if (store == null) {
			JDBC.loadDriverHSQL();

			SDBConnection sdb = SDBFactory.createConnection(
					"jdbc:hsqldb:mem:aname", "sa", "");

			store = new StoreTriplesNodesIndexHSQL(sdb);

			store.getTableFormatter().format();

			stores.put("HSQLINDEX", store);
		}
		
		Model model = SDBFactory.connectModel(store);
		model.removeAll();
		return model;
	}
	
	public Model getHashHSQL() {
		Store store = stores.get("HSQLHASH");

		if (store == null) {
			JDBC.loadDriverHSQL();

			SDBConnection sdb = SDBFactory.createConnection(
					"jdbc:hsqldb:mem:aname", "sa", "");

			store = new StoreTriplesNodesHashHSQL(sdb);

			store.getTableFormatter().format();

			stores.put("HSQLHASH", store);
		}
		
		Model model = SDBFactory.connectModel(store);
		model.removeAll();
		return model;
	}
	
	public Model getIndexPgSQL() {
		Store store = stores.get("PGSQLINDEX");

		if (store == null) {
			JDBC.loadDriverPGSQL();

			SDBConnection sdb = SDBFactory.createConnection(
					"jdbc:postgresql://localhost/sdb_test", "jena", "swara");

			store = new StoreTriplesNodesIndexPGSQL(sdb);

			store.getTableFormatter().format();
			
			stores.put("PGSQLINDEX", store);
		}
		
		Model model = SDBFactory.connectModel(store);
		model.removeAll();
		return model;
	}
	
	public Model getHashPgSQL() {
		Store store = stores.get("PGSQLHASH");

		if (store == null) {
			JDBC.loadDriverPGSQL();

			SDBConnection sdb = SDBFactory.createConnection(
					"jdbc:postgresql://localhost/sdb_test", "jena", "swara");

			store = new StoreTriplesNodesHashPGSQL(sdb);

			store.getTableFormatter().format();
			
			stores.put("PGSQLHASH", store);
		}
		
		Model model = SDBFactory.connectModel(store);
		model.removeAll();
		return model;
	}
	
	public Model getHashDerby() {
		Store store = stores.get("DERBYHASH");
		
		if (store == null) {
			JDBC.loadDriverDerby() ;
			String url = JDBC.makeURL("derby", "localhost", "DB/test2") ;
			SDBConnection sdb = new SDBConnection(url, null, null) ;
        
			store = new StoreTriplesNodesHashDerby(sdb);
			
			store.getTableFormatter().format();
			
			stores.put("DERBYHASH", store);
		}
        
        Model model = SDBFactory.connectModel(store);
        model.removeAll();
        return model;
	}
	
	public Model getIndexDerby() {
		Store store = stores.get("DERBYINDEX");
		
		if (store == null) {
			JDBC.loadDriverDerby() ;
			String url = JDBC.makeURL("derby", "localhost", "DB/test3") ;
			SDBConnection sdb = new SDBConnection(url, null, null) ;
        
			store = new StoreTriplesNodesIndexDerby(sdb);
			
			store.getTableFormatter().format();
			
			stores.put("DERBYINDEX", store);
		}
        
        Model model = SDBFactory.connectModel(store);
        model.removeAll();
        return model;
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