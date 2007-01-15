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
import com.hp.hpl.jena.sdb.layout2.StoreTriplesNodesIndexHSQL;
import com.hp.hpl.jena.sdb.layout2.StoreTriplesNodesIndexMySQL;
import com.hp.hpl.jena.sdb.layout2.StoreTriplesNodesIndexPGSQL;
import com.hp.hpl.jena.sdb.layout2.hash.StoreTriplesNodesHashDerby;
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

	public Model getMySQL() {
		Store store = stores.get("MYSQL");

		if (store == null) {
			JDBC.loadDriverMySQL();

			SDBConnection sdb = SDBFactory.createConnection(
					"jdbc:mysql://localhost/sdb_test", "jena", "swara");

			store = new StoreTriplesNodesIndexMySQL(sdb);

			store.getTableFormatter().format();

			stores.put("MYSQL", store);
		}
		
		Model model = SDBFactory.connectModel(store);
		model.removeAll();
		return model;
	}

	public Model getHSQL() {
		Store store = stores.get("HSQL");

		if (store == null) {
			JDBC.loadDriverHSQL();

			SDBConnection sdb = SDBFactory.createConnection(
					"jdbc:hsqldb:mem:aname", "sa", "");

			store = new StoreTriplesNodesIndexHSQL(sdb);

			store.getTableFormatter().format();

			stores.put("HSQL", store);
		}
		
		Model model = SDBFactory.connectModel(store);
		model.removeAll();
		return model;
	}

	public Model getPgSQL() {
		Store store = stores.get("PGSQL");

		if (store == null) {
			JDBC.loadDriverPGSQL();

			SDBConnection sdb = SDBFactory.createConnection(
					"jdbc:postgresql://localhost/sdb_test", "jena", "swara");

			store = new StoreTriplesNodesIndexPGSQL(sdb);

			store.getTableFormatter().format();
			
			stores.put("PGSQL", store);
		}
		
		Model model = SDBFactory.connectModel(store);
		model.removeAll();
		return model;
	}
	
	public Model getDerby() {
		Store store = stores.get("Derby");
		
		if (store == null) {
			JDBC.loadDriverDerby() ;
			String url = JDBC.makeURL("derby", "localhost", "DB/test2") ;
			SDBConnection sdb = new SDBConnection(url, null, null) ;
        
			store = new StoreTriplesNodesHashDerby(sdb);
			
			store.getTableFormatter().format();
			
			stores.put("Derby", store);
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